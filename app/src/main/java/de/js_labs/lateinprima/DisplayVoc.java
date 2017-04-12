package de.js_labs.lateinprima;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.google.firebase.crash.FirebaseCrash;

public class DisplayVoc extends AppCompatActivity {
    private TableLayout table;
    private TableRow.LayoutParams layoutParamsL;
    private TableRow.LayoutParams layoutParamsD;

    private DataStorage ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_voc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        ds = DataStorage.getInstance();
        if(ds.firstStart){
            finish();
            return;
        }

        layoutParamsL = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT, 4f);
        layoutParamsD = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT, 6f);

        table = (TableLayout) findViewById(R.id.table);

        toolbar.setTitle("Lektion " + (ds.currentLektion + 1));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        table.setColumnStretchable(0, true);
        table.setColumnStretchable(1, true);

        for(Vokablel vokablel : ds.lektions[ds.currentLektion].vokablels){
            addVoc(vokablel);
        }

        if(ds.removeAds || ds.surveyRemoveAds){
            Log.d(ds.LOG_TAG, "Werbung wird nicht angezeigt");
        }else {
            Log.d(ds.LOG_TAG, "Werbung wird angezeigt");


            Appodeal.setBannerViewId(R.id.dv_banner);
            Appodeal.show(this, Appodeal.BANNER_VIEW);
            Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
                @Override
                public void onInterstitialLoaded(boolean b) {}

                @Override
                public void onInterstitialFailedToLoad() {
                    FirebaseCrash.report(new Throwable("DisplayVoc: InterstitialFailedToLoad()"));
                }

                @Override
                public void onInterstitialShown() {}

                @Override
                public void onInterstitialClicked() {}

                @Override
                public void onInterstitialClosed() {
                    finish();
                }
            });
        }

    }

    private void addVoc(Vokablel vokablel){
        TextView latein = new TextView(this);
        TextView deutsch = new TextView(this);
        TableRow row = new TableRow(this);

        latein.setText(Html.fromHtml(vokablel.latein + "<i>" + vokablel.formen + "</i>"));
        deutsch.setText(vokablel.deutsch);
        latein.setTextColor(Color.BLACK);
        latein.setTextSize(17);
        deutsch.setTextSize(17);
        latein.setPadding(40,20,40,20);
        deutsch.setPadding(40,20,40,20);
        latein.setLayoutParams(layoutParamsL);
        deutsch.setLayoutParams(layoutParamsD);

        row.addView(latein);
        row.addView(deutsch);

        table.addView(row);
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
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        if(Appodeal.isLoaded(Appodeal.INTERSTITIAL) && !ds.surveyRemoveAds && !ds.removeAds)
            Appodeal.show(this, Appodeal.INTERSTITIAL);
        else
            finish();
    }

}
