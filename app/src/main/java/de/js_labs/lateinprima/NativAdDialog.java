package de.js_labs.lateinprima;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.NativeExpressAdView;

public class NativAdDialog extends AppCompatActivity implements View.OnClickListener {

    private Button closeBtn;
    private NativeExpressAdView ad;
    private RelativeLayout contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nativ_ad_dialog);
        setFinishOnTouchOutside(false);

        getWindow().getAttributes().width = RelativeLayout.LayoutParams.FILL_PARENT;

        contentView = (RelativeLayout) findViewById(R.id.nad_content_rl);

        closeBtn = (Button) findViewById(R.id.nad_btn_close);
        closeBtn.setOnClickListener(this);

        if(DisplayLektion.mNativAdView != null){
            ad = DisplayLektion.mNativAdView;
        }else if(DisplayVoc.mNativAdView != null){
            ad = DisplayVoc.mNativAdView;
        }else {
            finish();
        }

        if(!(ad == null))
            if(!(ad.getParent() == null)){
                Log.d("test", "finish()");
                finish();
            }
            else{
                contentView.addView(ad);
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(DisplayLektion.mNativAdView != null){
            DisplayLektion.mNativAdView = null;
        }else if(DisplayVoc.mNativAdView != null){
            DisplayVoc.mNativAdView = null;
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View view) {
        if(view == closeBtn){
            finish();
        }
    }
}
