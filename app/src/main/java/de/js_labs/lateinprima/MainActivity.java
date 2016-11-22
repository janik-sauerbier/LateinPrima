package de.js_labs.lateinprima;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Button.OnClickListener, CheckBox.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    public static final String LOG_TAG = "de.js_labs.log";
    public static final String SHARED_PREF = "privateSharedPreferences";
    public static final String REMOVEADS_ITEM_SKU = "de.js_labs.lateinprima.removeads";
    public static final String CURRENT_LEKTION = "currentLektion";
    public static final String PROVE_INPUT = "proveInput";
    public static final String IGNORE_CASE = "ignoreCase";
    public static final String DEV_MODE = "devMode";
    public static final String DATA_TIMESTAMP = "dataTimeStamp";

    public static final String ANALYTICS_REMOVED_ADS = "removed_ads";
    public static final String ANALYTICS_TYPE_TEST_VOC = "Test Voc";
    public static final String ANALYTICS_TYPE_SHOW_VOC = "Show Voc";
    public static final String ANALYTICS_TYPE_SHOW_LEKTION = "Show Lektion";
    public static final String ANALYTICS_TYPE_MENU_ACTION = "Menu Action";

    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor sharedEditor;

    public Toolbar toolbar;
    public NavigationView navigationView;
    public ViewGroup appBarMain;
    public View contentSend;
    public View contentHome;
    public View contentRightsInfo;

    public Button sendEntryButton;
    public CheckBox nutzungsbedingungenCheckBox;
    public CheckBox erwähnenCheckBox;
    public EditText nameEditText;
    public EditText inhaltEditText;
    public StableArrayAdapter adapterListView;
    private AlertDialog.Builder alertBuilder;

    public static boolean dataIsLeast = true;
    public static ArrayList<Vokablel> testVocBuffer;
    public static Lektion[] lektions = new Lektion[50];
    public static boolean firstStart = true;

    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private StorageReference databaseRef;
    private String databaseStatus = "Status wird geladen...";
    private int databaseStatusColor = Color.GRAY;

    private DataStorage ds;

    private int devCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home");

        ds = DataStorage.getInstance();

        sharedPreferences = this.getSharedPreferences(SHARED_PREF, 0);
        sharedEditor = sharedPreferences.edit();
        sharedEditor.commit();
        ds.removeAds = sharedPreferences.getBoolean(REMOVEADS_ITEM_SKU, false);
        ds.currentLektion = sharedPreferences.getInt(CURRENT_LEKTION, 0);
        ds.proveInput = sharedPreferences.getBoolean(PROVE_INPUT, false);
        ds.ignoreCase = sharedPreferences.getBoolean(IGNORE_CASE, true);
        ds.devMode = sharedPreferences.getBoolean(DEV_MODE, false);
        ds.dataTimeStamp = sharedPreferences.getLong(DATA_TIMESTAMP, 0);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        if(ds.removeAds){
            navigationView.getMenu().removeItem(R.id.nav_remove_ads);
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        appBarMain = (ViewGroup) findViewById(R.id.appBarMainRl);
        contentSend = inflater.inflate(R.layout.content_send_main, (ViewGroup) findViewById(R.id.contentSendRl));
        contentHome = inflater.inflate(R.layout.content_home_main, (ViewGroup) findViewById(R.id.contentHomeRl));
        contentRightsInfo = inflater.inflate(R.layout.content_rightsinfo_main, (ViewGroup) findViewById(R.id.contentRightsInfoRl));
        appBarMain.addView(contentHome);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.letionen, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(ds.currentLektion);
        spinner.setOnItemSelectedListener(this);

        final ListView listview = (ListView) findViewById(R.id.listView);
        final String[] values = new String[] { "Übersetzungstexte und Übungen", "Vokabeln abfragen", "Vokabeln anzeigen", "Datenbank aktualisieren", "Bewerten und Weitererzählen nicht vergessen ;D" };

        adapterListView = new StableArrayAdapter(this, values);
        listview.setAdapter(adapterListView);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                adapterListView.notifyDataSetChanged();
                listViewAction(position);
            }

        });

        ds.testVocBuffer = new ArrayList<Vokablel>();
        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Wähle die Vokabeln");
        alertBuilder.setPositiveButton("Starten", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(MainActivity.this, DisplayTestVoc.class);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(ds.currentLektion+1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_TEST_VOC);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                startActivity(i);
            }
        });
        alertBuilder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        if(ds.firstStart){
            for(int i = 1; i < 51; i++){
                ds.lektions[i - 1] = new Lektion(i);
            }

            loadDataFile();

            ds.firstStart = false;
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setUserProperty(ANALYTICS_REMOVED_ADS, Boolean.toString(ds.removeAds));
        firebaseAnalytics.setAnalyticsCollectionEnabled(!ds.devMode);

        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReferenceFromUrl("gs://admob-app-id-6279012805.appspot.com");
        databaseRef = storageRef.child("databasePrima.xml");

        checkDatabase();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            toolbar.setTitle("Home");
            appBarMain.removeView(contentRightsInfo);
            appBarMain.removeView(contentHome);
            appBarMain.removeView(contentSend);
            appBarMain.addView(contentHome);
        } else if (id == R.id.nav_send) {
            toolbar.setTitle("Beitrag einsenden/Kontakt");
            appBarMain.removeView(contentRightsInfo);
            appBarMain.removeView(contentHome);
            appBarMain.removeView(contentSend);
            appBarMain.addView(contentSend);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            contentSend.setMinimumWidth(displaymetrics.widthPixels);
            contentSend.setMinimumHeight(displaymetrics.heightPixels);
            sendEntryButton = (Button) findViewById(R.id.buttonSendEntry);
            nutzungsbedingungenCheckBox = (CheckBox) findViewById(R.id.checkBoxNutzungsbedingungen);
            erwähnenCheckBox = (CheckBox) findViewById(R.id.checkBoxNameErwähnen);
            nameEditText = (EditText) findViewById(R.id.editTextAbsenderName);
            inhaltEditText = (EditText) findViewById(R.id.editTextInhalt);
            sendEntryButton.setOnClickListener(this);
            nutzungsbedingungenCheckBox.setOnCheckedChangeListener(this);
            erwähnenCheckBox.setOnCheckedChangeListener(this);
            if(!nutzungsbedingungenCheckBox.isChecked()){
                sendEntryButton.setEnabled(false);
                sendEntryButton.setBackgroundColor(Color.GRAY);
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "open kontact UI");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_MENU_ACTION);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } else if (id == R.id.nav_rightsinfo){
            toolbar.setTitle("Rechtliche Informationen");
            appBarMain.removeView(contentRightsInfo);
            appBarMain.removeView(contentHome);
            appBarMain.removeView(contentSend);
            appBarMain.addView(contentRightsInfo);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "open rights info UI");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_MENU_ACTION);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } else if (id == R.id.nav_remove_ads){
            Intent i = new Intent(this, RemoveAds.class);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "start remove ads activity");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_MENU_ACTION);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(i);
        } else if (id == R.id.nav_settings){
            Intent i = new Intent(this, SettingsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "start settings activity");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_MENU_ACTION);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(i);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton == nutzungsbedingungenCheckBox){
            if(b == true){
                sendEntryButton.setEnabled(true);
                sendEntryButton.setBackgroundColor(Color.parseColor("#cc0000"));
            }else{
                sendEntryButton.setEnabled(false);
                sendEntryButton.setBackgroundColor(Color.GRAY);

            }
        }
        if(compoundButton == erwähnenCheckBox){
            if(ds.devMode == false){
                devCounter++;
                if(devCounter > 20){
                    ds.devMode = true;
                    sharedEditor.putBoolean(DEV_MODE, true);
                    sharedEditor.apply();
                }
            }
        }
    }

    public void loadVocToDialog(){
        ds.testVocBuffer.clear();
        for(int i = 0; i < ds.lektions[ds.currentLektion].vokablels.length; i++){
            ds.testVocBuffer.add(ds.lektions[ds.currentLektion].vokablels[i]);
        }
        String[] array = new String[ds.lektions[ds.currentLektion].vokablels.length];
        for(int i = 0; i < array.length; i++){
            array[i] = ds.lektions[ds.currentLektion].vokablels[i].latein;
        }
        int L = array.length;
        boolean[] b2 = new boolean[L];
        for(int i=0 ; i<L ; i++){
            b2[i] =true;
        }
        alertBuilder.setMultiChoiceItems(array, b2, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    ds.testVocBuffer.add(ds.lektions[ds.currentLektion].vokablels[which]);
                } else if (ds.testVocBuffer.contains(ds.lektions[ds.currentLektion].vokablels[which])) {
                    ds.testVocBuffer.remove(ds.lektions[ds.currentLektion].vokablels[which]);
                }
            }
        });
    }

    public void listViewAction(int id){
        if(id == 0){
            if(isDataLoaded()){
                Intent i = new Intent(this, DisplayLektion.class);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(ds.currentLektion+1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_SHOW_LEKTION);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                startActivity(i);
            }
        }else if(id == 1){
            if(isDataLoaded()){
                loadVocToDialog();
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
        }else if(id == 2){
            if(isDataLoaded()){
                Intent i = new Intent(this, DisplayVoc.class);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(ds.currentLektion+1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_SHOW_VOC);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                startActivity(i);
            }
        } else if (id == 3) {
            if(ds.dataIsLeast == false){
                startUpdateData();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "update database");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_MENU_ACTION);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }else if(id == 4){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "rate app");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_TYPE_MENU_ACTION);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=de.js_labs.lateinprima")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=de.js_labs.lateinprima")));
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view == sendEntryButton) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:js-labs@web.de"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Latein Prima: Beitrag von " + nameEditText.getText() + " Erwähnen: " + erwähnenCheckBox.isChecked());
            emailIntent.putExtra(Intent.EXTRA_TEXT, inhaltEditText.getText());

            startActivity(Intent.createChooser(emailIntent, "Email senden mit..."));
        }
    }

    private boolean isDataLoaded(){
        if(ds.lektions != null ){
            if(ds.lektions[ds.currentLektion].vokablels != null){
                return true;
            }else{
                return false;
            }
        }else {
            return false;
        }
    }

    public void loadDataFile(){
        final File dataFile = new File(getFilesDir() + "/data.xml");

        if(dataFile.exists()){
            try{
                FileInputStream fis = new FileInputStream(dataFile);
                loadDataFromStream(fis);
            }catch (IOException e) {
                FirebaseCrash.report(e);
                dataFile.delete();

                System.exit(0);
            }
        } else {
            loadDataAssats();
        }
    }

    public void loadDataAssats(){
        InputStream fis;
        try {
            fis = getAssets().open("database.xml");
            loadDataFromStream(fis);
        } catch (IOException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
    }

    private void loadDataFromStream(InputStream fis){
        ArrayList<String> data = new ArrayList<String>();
        String dataPart = null;
        boolean doText = false;
        try {
            InputStreamReader isr = new InputStreamReader(fis);
            char [] inputBuffer = new char[fis.available()];
            isr.read(inputBuffer);
            dataPart = new String(inputBuffer);
            isr.close();
            fis.close();
        } catch (FileNotFoundException e3) {
            FirebaseCrash.report(e3);
            e3.printStackTrace();
        } catch (IOException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e2) {
            FirebaseCrash.report(e2);
            e2.printStackTrace();
        }
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
        try {
            xpp = factory.newPullParser();
        } catch (XmlPullParserException e2) {
            FirebaseCrash.report(e2);
            e2.printStackTrace();
        }
        try {
            xpp.setInput(new StringReader(dataPart));
        } catch (XmlPullParserException e1) {
            FirebaseCrash.report(e1);
            e1.printStackTrace();
        }
        int eventType = 0;
        try {
            eventType = xpp.getEventType();
        } catch (XmlPullParserException e1) {
            FirebaseCrash.report(e1);
            e1.printStackTrace();
        }
        while (eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
            } else if (eventType == XmlPullParser.END_TAG) {
            } else if(eventType == XmlPullParser.TEXT) {
                if(doText == true){
                    data.add(xpp.getText());
                    doText = false;
                }else{
                    doText = true;
                }
            }
            try {
                eventType = xpp.next();
            } catch (XmlPullParserException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            } catch (IOException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
        }
        for(int i = 0; i < ds.lektions.length;i++){
            ds.lektions[i].setData(data.get(i));
        }
        for(int i = ds.lektions.length; i < data.size();i++){
            ds.lektions[i - ds.lektions.length].setVoc(data.get(i));
        }
    }

    private void checkDatabase(){
        databaseRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata metadata) {
                if(ds.dataTimeStamp > metadata.getUpdatedTimeMillis()){
                    databaseStatus = "Aktuell";
                    databaseStatusColor = Color.rgb(0,153,0);
                    adapterListView.notifyDataSetChanged();
                    Log.d(ds.LOG_TAG, "Datenbank: Aktuell");
                    ds.dataIsLeast = true;
                }else {
                    databaseStatus = "Nicht Aktuell";
                    databaseStatusColor = Color.RED;
                    adapterListView.notifyDataSetChanged();
                    Log.d(ds.LOG_TAG, "Datenbank: Nicht Aktuell");
                    ds.dataIsLeast = false;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if(((StorageException)exception).getErrorCode() == StorageException.ERROR_RETRY_LIMIT_EXCEEDED){
                    databaseStatusColor = Color.GRAY;
                    databaseStatus = "Offline";
                    adapterListView.notifyDataSetChanged();
                }else{
                    databaseStatusColor = Color.GRAY;
                    databaseStatus = "Status konnte nicht geladen werden!";
                    adapterListView.notifyDataSetChanged();
                    FirebaseCrash.report(exception);
                }
            }
        });
    }

    private void startUpdateData(){
        final File dataFile = new File(getFilesDir() + "/data.xml");
        final File tempDownloadFile = new File(getCacheDir() + "/data.download");


        if(databaseRef.getActiveDownloadTasks().size() == 0){
            ds.dataIsLeast = true;
            databaseRef.getFile(tempDownloadFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    databaseStatus = "Aktuell";
                    databaseStatusColor = Color.rgb(0,153,0);
                    adapterListView.notifyDataSetChanged();
                    ds.dataTimeStamp = System.currentTimeMillis();
                    sharedEditor.putLong(DATA_TIMESTAMP, ds.dataTimeStamp);
                    sharedEditor.apply();
                    ds.dataIsLeast = true;
                    tempDownloadFile.renameTo(dataFile);
                    loadDataFile();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                    databaseStatus = "Aktualisieren fehlgeschlagen!";
                    databaseStatusColor = Color.RED;
                    adapterListView.notifyDataSetChanged();
                    FirebaseCrash.report(exception);
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    databaseStatusColor = Color.rgb(255,140,0);
                    double done = taskSnapshot.getBytesTransferred();
                    double toDo = taskSnapshot.getTotalByteCount();
                    double progress = done/toDo;
                    progress = progress*100;
                    databaseStatus = "Wird aktualisiert (" + (int) progress + "%)";
                    adapterListView.notifyDataSetChanged();
                    Log.d(ds.LOG_TAG, "Datenbank wird aktualisiert... (" + taskSnapshot.getBytesTransferred() + "/" + taskSnapshot.getTotalByteCount() + ")");
                }
            }).addOnPausedListener(new OnPausedListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onPaused(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    databaseStatusColor = Color.rgb(255,140,0);
                    databaseStatus = "Wird aktualisiert (pausiert)";
                    adapterListView.notifyDataSetChanged();
                    Log.d(ds.LOG_TAG, "Datenbank wird aktualisiert... (pausiert)");
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ds.currentLektion = position;
        sharedEditor.putInt(CURRENT_LEKTION, position);
        sharedEditor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    public class StableArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public StableArrayAdapter(Context context, String[] values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItemView = inflater.inflate(R.layout.listview_item, parent, false);
            TextView textViewTitle = (TextView) listItemView.findViewById(R.id.listViewTitle);
            ImageView imageView = (ImageView) listItemView.findViewById(R.id.icon);
            textViewTitle.setText(values[position]);
            String s = values[position];
            if (s.equals("Übersetzungstexte und Übungen")) {
                imageView.setImageResource(R.drawable.show_lektion_icon);
            } else if(s.equals("Vokabeln abfragen")) {
                imageView.setImageResource(R.drawable.test_voc_icon);
            } else if(s.equals("Vokabeln anzeigen")){
                imageView.setImageResource(R.drawable.show_voc_icon);
            }else if(s.equals("Datenbank aktualisieren")){
                View listItemViewDatabase = inflater.inflate(R.layout.listview_item_database, parent, false);
                ImageView imageViewDatabase = (ImageView) listItemViewDatabase.findViewById(R.id.icon);
                TextView textViewTitleDatabase = (TextView) listItemViewDatabase.findViewById(R.id.listViewTitle);
                TextView databaseStatusTextView = (TextView) listItemViewDatabase.findViewById(R.id.listViewSubTitle);
                databaseStatusTextView.setTextColor(databaseStatusColor);
                databaseStatusTextView.setText(databaseStatus);
                textViewTitleDatabase.setText(values[position]);
                imageViewDatabase.setImageResource(R.drawable.database_icon);
                return listItemViewDatabase;
            }else{
                textViewTitle.setTextSize(17f);
                textViewTitle.setTextColor(Color.parseColor("#2E2E2E"));
                imageView.setImageResource(R.drawable.like_icon);
            }

            return listItemView;
        }
    }
}
