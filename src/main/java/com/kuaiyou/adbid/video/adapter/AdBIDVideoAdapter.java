package com.kuaiyou.adbid.video.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.VideoBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.Assets;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.video.AdViewVideoActivity;
import com.kuaiyou.video.vast.VASTPlayer;
import com.kuaiyou.video.vast.VASTPlayerListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

//#wilder

public class AdBIDVideoAdapter extends AdAdapterManager {


    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {

    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        Intent vastPlayerIntent = new Intent(context, AdViewVideoActivity.class);

//        Bundle bdle = new Bundle();
//        bdle.putBoolean("closeable", autoCloseAble);
//        bdle.putInt("vastOrientation", videoOrientation);
//        bdle.putBoolean("trafficWarnEnable", trafficWarnEnable);
//        bdle.putString("bgColor", bgColor.equals("#undefine") ? "#000000" : bgColor);

        vastPlayerIntent.putExtras(bundle);
        //vastPlayerIntent.putExtra("adsBean", (Serializable)adsBean);
        context.startActivity(vastPlayerIntent);
        //((Activity)context).startActivityForResult(vastPlayerIntent, SUBACTIVITY1);
        return ;
    }

    @Override
    public boolean playVideo(Context context) {
        try {
            if (null != newPlayer)
                newPlayer.play(context);
            else
                AdViewUtils.logInfo("video occurred error");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    /////////////////////////////////(wilder 2019) ///////////////////////////////////////////
    ///////////////////////////////VAST Player Listener /////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void vastDownloadReady(){
        onAdReady(); //wilder 2019 ,notify ready event to App
        //wilder 2019 for test mrec
    }

    @Override
    public void vastError(int error){
        AdViewUtils.logInfo("vastError");
        onAdFailed("error: " + error);
    }

    @Override
    public void vastClick(){
        onAdClick(null,null, 0,0); //wilder 2019

        AdViewUtils.logInfo("vastClick");
    }

    @Override
    public int vastOrientationChange(){
        return 0;
    }
    ///////////////////end VAST listener ////////////////////////////////////
}
