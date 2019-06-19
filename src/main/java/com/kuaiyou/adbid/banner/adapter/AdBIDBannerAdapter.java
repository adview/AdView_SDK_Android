package com.kuaiyou.adbid.banner.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.adbid.video.adapter.AdBIDVideoAdapter;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.BannerView;
import com.kuaiyou.interfaces.KyViewListener;
import com.kuaiyou.video.vast.VASTPlayer;
import com.kuaiyou.video.vast.VASTPlayerListener;
import com.kuaiyou.video.vast.model.VASTModel;

import java.util.ArrayList;

public class AdBIDBannerAdapter extends AdAdapterManager {
    BannerView adBidView;
    //private VideoBroadcastManager videoBroadcastManagerMrec; //for mrec
    public AdBIDBannerAdapter() {

    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        adBidView = new BannerView(context, bundle, (KyViewListener) bundle.getSerializable("interface"), this);
        //adBidView.setAdapterManager(this);
    }

    @Override
    public View getAdView() {
        return adBidView;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdBIDBannerAdapter");

    }

    //vast video interfaces
    @Override
    public boolean playVideo(Context context) {
        AdViewUtils.logInfo("<--------- AdBIDBnnerAdapter:: playVideo() ----------->");
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

    @Override
    public void vastClick(){
        onAdClick(null,null, 0,0); //wilder 2019
        AdViewUtils.logInfo("vastClick");
    }

    @Override
    public int vastOrientationChange(){
        return 0;
    }
}
