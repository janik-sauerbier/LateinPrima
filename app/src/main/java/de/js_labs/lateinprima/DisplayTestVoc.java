package de.js_labs.lateinprima;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.AppodealMediaView;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DisplayTestVoc extends AppCompatActivity implements Button.OnClickListener {
    public static final String ANALYTICS_EVENT_TEST_VOC_RESULT = "testing_results";

    private TextView latein;
    private TextView deutsch;
    private Button rightBtn;
    private Button wrongBtn;
    private Button justRightBtn;
    private Button proveBtn;
    private Button exit;
    private Button again;
    private Button closeAdBtn;
    private EditText input;
    private AlertDialog.Builder alertBuilder;
    private boolean clock;
    private Vokablel currentVoc;
    private ArrayList<Boolean> result;
    private ArrayList<Vokablel> tempAllVoc;
    private ArrayList<Vokablel> tempAllVocWorng;
    private int againSelected;
    private int resultCounter = 0;

    private NativeAd nativeAd;

    private FirebaseAnalytics firebaseAnalytics;

    private DataStorage ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ds = DataStorage.getInstance();
        if(ds.firstStart){
            finish();
            return;
        }

        if(ds.proveInput){
            setContentView(R.layout.activity_display_test_voc_prove);
        }else {
            setContentView(R.layout.activity_display_test_voc_notprove);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Lektion " + (ds.currentLektion + 1));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        result = new ArrayList<Boolean>();

        tempAllVoc = new ArrayList<Vokablel>();
        tempAllVoc.addAll(ds.testVocBuffer);
        tempAllVocWorng = new ArrayList<Vokablel>();
        againSelected = 0;

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Wähle die Vokabeln");
        alertBuilder.setPositiveButton("Nochmal", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resultCounter = 0;
                if(againSelected == 0) {
                    ds.testVocBuffer.clear();
                    ds.testVocBuffer.addAll(Arrays.asList(ds.lektions[ds.currentLektion].vokablels));
                }else if(againSelected == 1){
                    ds.testVocBuffer = tempAllVoc;
                }else if(againSelected == 2){
                    ds.testVocBuffer = tempAllVocWorng;
                }
                recreate();
            }
        });
        alertBuilder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        String [] array = {"Alle Vokabeln der Lektion" ,"Alle eben abgefragten Vokabeln" ,"Nur nicht gewussten Vokabeln"};
        alertBuilder.setSingleChoiceItems(array, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                againSelected = which;
            }
        });

        if(ds.proveInput){

            latein = (TextView) findViewById(R.id.textViewTestVocLateinP);
            deutsch = (TextView) findViewById(R.id.textViewTestVocDeutschP);
            justRightBtn = (Button) findViewById(R.id.buttonTestVocJustRight);
            proveBtn = (Button) findViewById(R.id.buttonTestVocProve);
            input = (EditText) findViewById(R.id.editTextTestVocInput);

            justRightBtn.setOnClickListener(this);
            proveBtn.setOnClickListener(this);

            setupAds();

            initProveTest(true);
        }else{
            latein = (TextView) findViewById(R.id.textViewTestVocLateinNP);
            deutsch = (TextView) findViewById(R.id.textViewTestVocDeutschNP);
            rightBtn = (Button) findViewById(R.id.buttonTestVocRight);
            wrongBtn = (Button) findViewById(R.id.buttonTestVocWrong);

            rightBtn.setOnClickListener(this);
            wrongBtn.setOnClickListener(this);

            setupAds();

            initNotProveTest(true);
        }
    }

    private void setupAds(){
        if(ds.removeAds || ds.surveyRemoveAds){
            Log.d(ds.LOG_TAG, "Werbung wird nicht angezeigt");
        }else {
            Log.d(ds.LOG_TAG, "Werbung wird angezeigt");

            Appodeal.setBannerViewId(R.id.tv_banner);
            Appodeal.show(this, Appodeal.BANNER_VIEW);
            Appodeal.setAutoCacheNativeMedia(true);
            Appodeal.setAutoCacheNativeIcons(true);
            Appodeal.cache(this, Appodeal.NATIVE);
            Appodeal.setNativeCallbacks(new NativeCallbacks() {
                @Override
                public void onNativeLoaded(List<NativeAd> list) {
                    nativeAd = list.get(0);
                }

                @Override
                public void onNativeFailedToLoad() {
                }

                @Override
                public void onNativeShown(NativeAd nativeAd) {

                }

                @Override
                public void onNativeClicked(NativeAd nativeAd) {

                }
            });
        }
    }

    public void showAd(){
        if(ds.removeAds || ds.surveyRemoveAds || nativeAd == null){
            showResults();
        } else {
            setContentView(R.layout.activity_display_test_voc_nativ_ad);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Lektion " + (ds.currentLektion + 1));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            closeAdBtn = (Button) findViewById(R.id.buttonCloseAd);
            closeAdBtn.setOnClickListener(this);

            TextView adTitle = (TextView) findViewById(R.id.textViewAdTitle);
            ImageView adLogo = (ImageView) findViewById(R.id.imageViewAdLogo);
            Button adBtn = (Button) findViewById(R.id.buttonAd);
            RatingBar adRating = (RatingBar) findViewById(R.id.ratingBarAd);
            TextView adRatingText = (TextView) findViewById(R.id.textViewRatingAd);
            TextView adDescription = (TextView) findViewById(R.id.textViewDescriptionAd);
            AppodealMediaView appodealMediaView = (AppodealMediaView) findViewById(R.id.appodealMediaView);
            View providerView = nativeAd.getProviderView(this);

            adTitle.setText(nativeAd.getTitle());
            adLogo.setImageBitmap(nativeAd.getIcon());
            adBtn.setText(nativeAd.getCallToAction());
            adRating.setMax(5);
            adRating.setStepSize(0.1f);
            adRating.setRating(nativeAd.getRating());
            BigDecimal roundfinalPrice = new BigDecimal(nativeAd.getRating()).setScale(1, BigDecimal.ROUND_HALF_UP);
            adRatingText.setText(roundfinalPrice.toString());
            adDescription.setText(nativeAd.getDescription());
            if (providerView != null) {
                RelativeLayout providerViewContainer = (RelativeLayout) findViewById(R.id.providerViewContainer);
                providerViewContainer.addView(providerView);
            }
            nativeAd.setAppodealMediaView(appodealMediaView);

            RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.adContainerRl);
            nativeAd.registerViewForInteraction(adContainer);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
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

    private void showResults(){
        int right = 0;
        int wrong = 0;
        for(int i = 0; i< result.size(); i++){
            if (result.get(i)){
                right++;
            }else {
                wrong++;
            }
        }
        setContentView(R.layout.activity_display_test_voc_result);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Ergebnis");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        exit = (Button) findViewById(R.id.buttonTestVocExit);
        exit.setOnClickListener(this);
        again = (Button) findViewById(R.id.buttonTestVocAgain);
        again.setOnClickListener(this);
        TextView rightCount = (TextView) findViewById(R.id.textViewRightCount);
        rightCount.setText(Integer.toString(right));
        rightCount.setTextSize(20 + right * 2);
        TextView wrongCount = (TextView) findViewById(R.id.textViewWrongCount);
        wrongCount.setText(Integer.toString(wrong));
        wrongCount.setTextSize(20 + wrong * 2);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setStepSize(24f);
        double dRight = (double) right;
        double dWrong = (double) wrong;
        double rating = (dRight / (dRight + dWrong)) * 6;
        ratingBar.setRating(((float) rating));
        ratingBar.setIsIndicator(true);
        TextView grade = (TextView) findViewById(R.id.textViewGrade);
        rating = dRight / (dRight + dWrong) * 1000;
        rating = Math.round(rating) / 10;
        grade.setText(Double.toString(rating) + " %");

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(ds.currentLektion+1));
        bundle.putInt(FirebaseAnalytics.Param.VALUE, (int) rating);
        firebaseAnalytics.logEvent(ANALYTICS_EVENT_TEST_VOC_RESULT, bundle);
    }


    private void initNotProveTest(boolean clock){
        if(ds.testVocBuffer.size() == 0 && clock == true){
            showAd();
        }else {
            if(clock){
                Random rand = new Random();
                currentVoc = ds.testVocBuffer.get(rand.nextInt(ds.testVocBuffer.size()));
                ds.testVocBuffer.remove(currentVoc);
                deutsch.setText("");
                latein.setText(currentVoc.latein);
                wrongBtn.setVisibility(View.INVISIBLE);
                rightBtn.setText("Aufdecken");
                rightBtn.setBackgroundColor(Color.parseColor("#cc0000"));
            }else {
                latein.setText(Html.fromHtml(currentVoc.latein + "<i>" + currentVoc.formen + "</i>"));
                deutsch.setText(currentVoc.deutsch);
                wrongBtn.setVisibility(View.VISIBLE);
                rightBtn.setText("Gewusst");
                rightBtn.setBackgroundColor(Color.parseColor("#00A827"));
                resultCounter++;
            }
        }
    }

    private void initProveTest(boolean clock){
        if(ds.testVocBuffer.size() == 0 && clock == true){
            showAd();
        }else {
            if(clock){
                Random rand = new Random();
                currentVoc = ds.testVocBuffer.get(rand.nextInt(ds.testVocBuffer.size()));
                ds.testVocBuffer.remove(currentVoc);
                deutsch.setText(currentVoc.deutsch);
                deutsch.setVisibility(View.INVISIBLE);
                input.setText("");
                input.setVisibility(View.VISIBLE);
                latein.setText(currentVoc.latein);
                justRightBtn.setVisibility(View.INVISIBLE);
                proveBtn.setText("Prüfen");
            }else {
                deutsch.setVisibility(View.VISIBLE);
                if (ds.ignoreCase){
                    if (input.getText().toString().trim().equalsIgnoreCase(currentVoc.deutsch.trim())){
                        View view = this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        result.add(true);
                        input.setVisibility(View.INVISIBLE);
                        deutsch.setTextColor(Color.parseColor("#00A827"));
                        deutsch.setVisibility(View.VISIBLE);
                        latein.setText(Html.fromHtml(currentVoc.latein + "<i>" + currentVoc.formen + "</i>"));
                        proveBtn.setText("Weiter");
                        resultCounter++;
                    }else {
                        View view = this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        justRightBtn.setVisibility(View.VISIBLE);
                        input.setVisibility(View.INVISIBLE);
                        deutsch.setTextColor(Color.parseColor("#FF0000"));
                        deutsch.setVisibility(View.VISIBLE);
                        latein.setText(Html.fromHtml(currentVoc.latein + "<i>" + currentVoc.formen + "</i>"));
                        proveBtn.setText("Weiter");
                        resultCounter++;
                    }
                }else {
                    if (input.getText().toString().trim().equals(currentVoc.deutsch.trim())){
                        View view = this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        result.add(true);
                        input.setVisibility(View.INVISIBLE);
                        deutsch.setTextColor(Color.parseColor("#00A827"));
                        deutsch.setVisibility(View.VISIBLE);
                        latein.setText(Html.fromHtml(currentVoc.latein + "<i>" + currentVoc.formen + "</i>"));
                        proveBtn.setText("Weiter");
                        resultCounter++;
                    }else {
                        View view = this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        justRightBtn.setVisibility(View.VISIBLE);
                        input.setVisibility(View.INVISIBLE);
                        deutsch.setTextColor(Color.parseColor("#FF0000"));
                        deutsch.setVisibility(View.VISIBLE);
                        latein.setText(Html.fromHtml(currentVoc.latein + "<i>" + currentVoc.formen + "</i>"));
                        proveBtn.setText("Weiter");
                        resultCounter++;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == rightBtn){
            if(clock){
                if(resultCounter - 1 == result.size())
                    result.add(resultCounter - 1, true);
                initNotProveTest(clock);
                clock = false;
            }else {
                initNotProveTest(clock);
                clock = true;
            }
        } else if (v == wrongBtn) {
            if(resultCounter - 1 == result.size()){
                result.add(resultCounter - 1, false);
                tempAllVocWorng.add(currentVoc);
            }
            if(clock){
                initNotProveTest(clock);
                clock = false;
            }else {
                initNotProveTest(clock);
                clock = true;
            }
        }else if(v == justRightBtn){
            if(resultCounter - 1 == result.size())
                result.add(resultCounter - 1, true);
            initProveTest(clock);
            clock = false;
        }else if(v == proveBtn){
            if(clock){
                if (justRightBtn.getVisibility() == View.VISIBLE){
                    if(resultCounter - 1 == result.size()){
                        result.add(resultCounter - 1, false);
                        tempAllVocWorng.add(currentVoc);
                    }
                }
                initProveTest(clock);
                clock = false;
            }else {
                initProveTest(clock);
                clock = true;
            }
        }else if(v == exit){
            onBackPressed();
        }else if(v == again){
            alertBuilder.show();
        }else if(v == closeAdBtn){
            showResults();
        }
    }
}
