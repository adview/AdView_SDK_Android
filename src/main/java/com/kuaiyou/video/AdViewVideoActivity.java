package com.kuaiyou.video;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.kuaiyou.adbid.AdVideoBIDView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.utils.AdViewUtils;

public class AdViewVideoActivity extends Activity {

    private String bgColor = "#000000";//default is black
    private float density;
    private int screenWidth;
    private int screenHeight;

    private AdVASTView mvastView;
    private AdsBean adsBean;

    private AdViewVideoInterface mAppInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            hideTitleStatusBars();
            int currentOrientation = getIntent().getExtras().getInt("vastOrientation", -1);
            Bundle bd = getIntent().getExtras();
            bgColor = bd.getString("bgColor", "#000000");
            //closeable = getIntent().getExtras().getBoolean("closeable", false);
            adsBean = (AdsBean) bd.getSerializable("adsBean");
            //video BID direct use app's interface
            mAppInterface = AdVideoBIDView.getInstance(this.getParent()).getVideoAppListener();

            if (-1 == currentOrientation) {
                currentOrientation = this.getResources().getConfiguration().orientation;
            }
            if (2 != currentOrientation) {
                setRequestedOrientation(currentOrientation);
            }

            getScreenSize();

            mvastView = new AdVASTView(this, -1, -1, false, AdVideoBIDView.getInstance(this.getParent()).getAdAdapterManager());
            if (mvastView != null) {

                if (mAppInterface != null) {
                    mvastView.setVideoAppListener(mAppInterface);
                }
                setContentView(mvastView, new FrameLayout.LayoutParams(-1, -1));

                mvastView.processVastVideo(this, bd);
                //mvastView.video_handlerAd(this, adsBean, true, -1, null, getIntent().getExtras());
            }
        }catch ( Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(mvastView != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mvastView.getScreenSize(true);
            }else
                mvastView.getScreenSize(false);

        }

        getScreenSize();
    }
    @Override
    protected void onResume() {
        super.onResume();
        AdViewUtils.logInfo("entered on onResume --(life cycle event)");
        try {
//            showProgressBar();
            if (mvastView != null) {
                mvastView.onResume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        AdViewUtils.logInfo("entered on onPause --(life cycle event)");
        try {
           if(mvastView != null) {
               mvastView.onPause();
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            if (mvastView != null) {
                mvastView.onDestroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }

    private void getScreenSize() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        density = displayMetrics.density;
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }

    private void hideTitleStatusBars() {
        // hide title bar of application
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // hide status bar of Android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }


}
