package de.js_labs.lateinprima;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.crash.FirebaseCrash;

public class DisplayVoc extends AppCompatActivity {
    private TableLayout table;
    private TableRow.LayoutParams layoutParamsL;
    private TableRow.LayoutParams layoutParamsD;

    public static NativeExpressAdView mNativAdView;
    private AdView mAdView;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private RelativeLayout contentDisplayVoc;

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
        contentDisplayVoc = (RelativeLayout) findViewById(R.id.contentDisplayVocRl);

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
            mNativAdView = new NativeExpressAdView(this);

            if(ds.devMode){
                mNativAdView.setAdUnitId("ca-app-pub-3940256099942544/2177258514");
            }else {
                mNativAdView.setAdUnitId("ca-app-pub-2790218770120733/7756204402");
            }

            RelativeLayout.LayoutParams nativAdLP =  new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            nativAdLP.addRule(RelativeLayout.BELOW, R.id.nad_title);
            nativAdLP.setMargins(dpToPx(5), 0, dpToPx(5), dpToPx(5));

            mNativAdView.setLayoutParams(nativAdLP);

            int pxWidth = this.getResources().getDisplayMetrics().widthPixels;

            mNativAdView.setAdSize(new AdSize(pxToDp(pxWidth)- 42, pxToDp(pxWidth)));

            AdRequest nativAdRequest = new AdRequest.Builder().build();
            mNativAdView.loadAd(nativAdRequest);
            mNativAdView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    FirebaseCrash.report(new Throwable("Failed To Load Nativ Ad (DisplayVoc) Code: " + i));
                }
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.d("test", "onAdLoaded()");
                    Intent i = new Intent(DisplayVoc.this, NativAdDialog.class);
                    DisplayVoc.this.startActivity(i);
                }
            });

            mAdView = new AdView(this);
            mAdView.setAdSize(AdSize.SMART_BANNER);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            mAdView.setLayoutParams(layoutParams);
            if(ds.devMode){
                mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
            }else {
                mAdView.setAdUnitId("ca-app-pub-2790218770120733/8097531209");
            }
            contentDisplayVoc.addView(mAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            layoutListener = new ViewTreeObserver.OnGlobalLayoutListener(){
                @Override
                public void onGlobalLayout() {
                    int height = mAdView.getHeight();
                    if (height > 0) {
                        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        layoutParams2.topMargin = height;
                        TableRow titleRow = (TableRow) table.getChildAt(0);
                        titleRow.setLayoutParams(layoutParams2);
                        if(Build.VERSION.SDK_INT >= 16)
                            mAdView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                    }
                }
            };

            mAdView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        }

    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
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

}
