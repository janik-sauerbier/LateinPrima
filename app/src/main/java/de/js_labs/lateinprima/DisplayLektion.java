package de.js_labs.lateinprima;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;

public class DisplayLektion extends AppCompatActivity {

    private final String COPY_MESSAGE = "Hol dir die Latein Prima App im Playstore :D\nbit.ly/Latein-Prima-App\n\n";
    private TextView text;
    private FloatingActionButton fab;

    private DataStorage ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_lektion);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Lösungen");
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ds = DataStorage.getInstance();
        if(ds.firstStart){
            finish();
            return;
        }

        text = (TextView)findViewById(R.id.textView3);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(ds.removeAds || ds.surveyRemoveAds){
            Log.d(ds.LOG_TAG, "Werbung wird nicht angezeigt");
        }else {
            Log.d(ds.LOG_TAG, "Werbung wird angezeigt");

            Appodeal.setBannerViewId(R.id.dl_banner);
            Appodeal.show(this, Appodeal.BANNER_VIEW);
            Appodeal.isLoaded(Appodeal.INTERSTITIAL);
        }

        toolbar.setTitle("Lektion " + (ds.currentLektion + 1));
        text.setText(ds.lektions[ds.currentLektion].data);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText(toolbar.getTitle().toString() + " wurde kopiert", COPY_MESSAGE + text.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(DisplayLektion.this, "Text wurde kopiert", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(Appodeal.isLoaded(Appodeal.INTERSTITIAL))
            Appodeal.show(this, Appodeal.INTERSTITIAL);
    }

}
