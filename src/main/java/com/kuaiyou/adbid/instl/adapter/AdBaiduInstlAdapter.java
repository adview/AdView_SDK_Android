package com.kuaiyou.adbid.instl.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.baidu.mobads.AdView;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

import org.json.JSONObject;

public class AdBaiduInstlAdapter extends AdAdapterManager implements InterstitialAdListener {
    private InterstitialAd interstitialAd;
    private boolean isRecieved;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdBaiduInstlAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.baidu.mobads.InterstitialAd")) {
                AdBaiduInstlAdapter.this.onAdFailed("com.baidu.mobads.InterstitialAd not found");
                return;
            }
            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");
            AdView.setAppSid(context, key1);
            interstitialAd = new InterstitialAd(context, key2);
            interstitialAd.setListener(this);
            interstitialAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
            super.onAdFailed("com.baidu.mobads.InterstitialAd not found");
        }
    }


    @Override
    public void onAdReady() {
        isRecieved = true;
        super.onAdRecieved(true);
        super.onAdReady(true);
    }

    @Override
    public void onAdPresent() {
        super.onAdDisplay(true);
    }

    @Override
    public void onAdClick(InterstitialAd interstitialAd) {
        super.onAdClick(null, null, 888, 888);
    }

    @Override
    public void onAdDismissed() {
        super.onAdClosed();
    }

    @Override
    public void onAdFailed(String s) {
        super.onAdFailed(s);
        interstitialAd.destroy();
    }

    @Override
    public boolean showInstl(Activity activity) {
        try {
            if (isRecieved) {
                isRecieved = false;
                interstitialAd.showAd(activity);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
