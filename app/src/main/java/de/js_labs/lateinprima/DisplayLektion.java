package de.js_labs.lateinprima;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class DisplayLektion extends AppCompatActivity {

    private final String COPY_MESSAGE = "Hol dir die Latein Prima App im Playstore :D\nbit.ly/Latein-Prima-App\n\n";
    private TextView text;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private RelativeLayout contentDisplayLektion;
    private ScrollView scrollView;
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
        scrollView = (ScrollView) findViewById(R.id.scrollView2);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentDisplayLektion = (RelativeLayout)findViewById(R.id.contentDisplayLektionRl);

        if(ds.removeAds == true){
            Log.d(ds.LOG_TAG, "Werbung wird nicht angezeigt");
        }else {
            Log.d(ds.LOG_TAG, "Werbung wird angezeigt");
            mInterstitialAd = new InterstitialAd(this);

            if(ds.devMode){
                mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            }else {
                mInterstitialAd.setAdUnitId("ca-app-pub-2790218770120733/3527730803");
            }

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    closeLektion();
                }
            });
            requestNewInterstitial();

            mAdView = new AdView(this);
            mAdView.setAdSize(AdSize.SMART_BANNER);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            mAdView.setLayoutParams(layoutParams);

            if(ds.devMode){
                mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
            }else {
                mAdView.setAdUnitId("ca-app-pub-2790218770120733/3667331608");
            }
            contentDisplayLektion.addView(mAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            layoutListener = new ViewTreeObserver.OnGlobalLayoutListener(){
                @Override
                public void onGlobalLayout() {
                    int height = mAdView.getHeight();
                    if (height > 0) {
                        CoordinatorLayout.LayoutParams params1 = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params1.bottomMargin = height + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                        params1.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                        params1.gravity = Gravity.BOTTOM + Gravity.RIGHT;
                        fab.setLayoutParams(params1);

                        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        parms.bottomMargin = height;
                        scrollView.setLayoutParams(parms);
                        if(Build.VERSION.SDK_INT >= 16)
                            mAdView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                    }
                }
            };

            mAdView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        }

        toolbar.setTitle("Lektion " + (ds.currentLektion + 1));
        text.setText(ds.lektions[ds.currentLektion].data);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(COPY_MESSAGE + text.getText());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(toolbar.getTitle().toString() + " wurde kopiert", COPY_MESSAGE + text.getText());
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(DisplayLektion.this, "Text wurde kopiert", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onBackPressed(){
        showAd();
    }

    public void showAd(){
        if(ds.removeAds == false){
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                finish();
            }
        }else {
            finish();
        }

    }

    public void closeLektion(){
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(ds.LOG_TAG, "onDestroy()");
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


}
