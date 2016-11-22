package de.js_labs.lateinprima;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.js_labs.lateinprima.util.IabHelper;
import de.js_labs.lateinprima.util.IabResult;
import de.js_labs.lateinprima.util.Purchase;

public class RemoveAds extends AppCompatActivity implements Button.OnClickListener{
    public final static String BASE64_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh4UkxE4kY71TWNH0QxP3YlEbyGBgofufKDC0K8F5UpMy2gCUkzJjZhdtsI7t5iOx3Hypig9/dD1mzE8bOiIYrEtN0xJwQLZXUb9Bp1uObWVQylRgSFJah/eiIGfasOvs1pNyEuNIOrieLh3VNhVPvJG6U37Ydz07asNmy+df4fI29neFkUzsoerlpHEggNkIxw7+AFNe5z8BqlBpb2glSYo2MmqlWH82VHp5iazpFj3VNaBkoEx7/cACbsdkUIx7dT8x92DhE+FF37UQCHgWZTukoISpFy24C5vwaaY8oQFs3OZ3JMX0O6nFjNs7uVrfAU1l82QnqYElrENNqAF9HwIDAQAB";
    public static final String REMOVEADS_ITEM_SKU = "de.js_labs.lateinprima.removeads";
    public static final String REMOVEADS_ITEM_SKU_DEBUG = "android.test.purchase";
    public static final String REMOVEADS_ITEM_TAG = "removeAds";

    private Button removeAdsBtn;
    private ViewGroup removeAdsContent;
    private IabHelper mHelper;

    public SharedPreferences.Editor sharedEditor;
    public SharedPreferences sharedPreferences;

    private DataStorage ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_ads);

        getSupportActionBar().setTitle("Werbung entfernen");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ds = DataStorage.getInstance();
        if(ds.firstStart){
            finish();
            return;
        }

        sharedPreferences = this.getSharedPreferences(MainActivity.SHARED_PREF, 0);
        sharedEditor = sharedPreferences.edit();

        removeAdsContent = (ViewGroup) findViewById(R.id.removeAdsRl);
        removeAdsBtn = (Button) findViewById(R.id.buttonBuy);
        removeAdsBtn.setOnClickListener(this);
        if(ds.removeAds){
            removeAdsBtn.setText("Bereits Freigeschaltet");
            removeAdsBtn.setEnabled(false);
        }

        mHelper = new IabHelper(this, BASE64_KEY);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(ds.LOG_TAG, "Billig faild: " + result);
                    onBackPressed();
                } else {
                    Log.d(ds.LOG_TAG, "In-app Billing is set up OK");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == removeAdsBtn){
            if (ds.devMode){
                debugBuy();
            }else {
                mHelper.launchPurchaseFlow(this, REMOVEADS_ITEM_SKU, 10001,
                        mPurchaseFinishedListener, REMOVEADS_ITEM_TAG);
                removeAdsBtn.setEnabled(false);
                removeAdsBtn.setText("Warten ...");
            }
        }
    }

    public void debugBuy(){
        if(ds.removeAds){
            ds.removeAds = false;
            Log.d(ds.LOG_TAG, "removeAds = " + Boolean.toString(ds.removeAds));
            Snackbar.make(removeAdsContent, "Debug Kauf wurde rückgängig gemacht", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            removeAdsBtn.setText("Werbung entfernen");
        }else {
            ds.removeAds = true;
            Log.d(ds.LOG_TAG, "removeAds = " + Boolean.toString(ds.removeAds));
            Snackbar.make(removeAdsContent, "Debug Kauf wurde durchgeführt", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            removeAdsBtn.setText("Rückgängig");
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if(mHelper == null){
                return;
            }

            if (result.isFailure()) {

                removeAdsBtn.setEnabled(true);
                removeAdsBtn.setText("Werbung entfernen");

                if(result.getResponse() == 7){
                    Log.d(ds.LOG_TAG, "Kauf wird wieder hergestellt...");
                    sharedEditor.putBoolean(REMOVEADS_ITEM_SKU, true);
                    sharedEditor.commit();
                    ds.removeAds = sharedPreferences.getBoolean(REMOVEADS_ITEM_SKU, false);
                    Log.d(ds.LOG_TAG, "removeAds = " + Boolean.toString(ds.removeAds));
                    Snackbar.make(removeAdsContent, "Kauf erfolgreich wiederhergestellt", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    removeAdsBtn.setEnabled(false);
                    removeAdsBtn.setText("Freigeschaltet");
                }

                Log.e(ds.LOG_TAG, "Fehler beim ausführen des Kaufauftrags: " + result.getMessage());
                return;
            } else if (purchase.getSku().equals(REMOVEADS_ITEM_SKU)) {

                Log.d(ds.LOG_TAG, "Kaufauftrag wird ausgeführt...");
                sharedEditor.putBoolean(REMOVEADS_ITEM_SKU, true);
                sharedEditor.commit();
                ds.removeAds = sharedPreferences.getBoolean(REMOVEADS_ITEM_SKU, false);
                Log.d(ds.LOG_TAG, "removeAds = " + Boolean.toString(ds.removeAds));
                Snackbar.make(removeAdsContent, "Kauf erfolgreich ausgeführt", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                removeAdsBtn.setEnabled(false);
                removeAdsBtn.setText("Bereits Freigeschaltet");

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
    public void onDestroy(){
        super.onDestroy();
        if (mHelper != null){
            try{
                mHelper.dispose();
            }catch (Exception e){
                e.printStackTrace();
            }
            mHelper = null;
        }
    }
}
