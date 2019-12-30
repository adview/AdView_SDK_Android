package com.kuaiyou.adbid.banner.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.BannerView;
import com.kuaiyou.interfaces.AdVGListener;

public class AdBIDBannerAdapter extends AdAdapterManager {
    BannerView adBidView;
    public AdBIDBannerAdapter() {

    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        adBidView = new BannerView(context, bundle, (AdVGListener) bundle.getSerializable("interface"), this);
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
                newPlayer.sendPlayReady(context);
            else
                AdViewUtils.logInfo("video occurred error");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
