package com.kuaiyou.adbid.banner.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.baidu.mobads.AdService;
import com.baidu.mobads.AdSettings;
import com.baidu.mobads.AdSize;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

import org.json.JSONObject;

public class AdBaiduBannerAdapter extends AdAdapterManager implements AdViewListener {
    private RelativeLayout adLayout;
    private boolean isSwitch;
    private boolean isFailed, isReady, isShow;
    private int adWidth, adHeight;

    @Override
    public View getAdView() {
        return adLayout;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("currentThread " + Thread.currentThread());
        AdViewUtils.logInfo("initAdapter AdBaiduBannerAdapter");
//        AdViewUtils.logInfo("initAdapter");
        adLayout = new RelativeLayout(context);
        double density = AdViewUtils.getDensity(context);
        adWidth = (int) (320 * density);
        adHeight = (int) (50 * density);
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.baidu.mobads.AdService")) {
                AdBaiduBannerAdapter.this.onAdFailed("com.baidu.mobads.AdService not found");
                return;
            }
            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");
            RelativeLayout parentView = (RelativeLayout) bundle.getSerializable("parentView");
            AdView.setAppSid(context, key1);
//            AdSettings.setSupportHttps(true);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            parentView.addView(adLayout, layoutParams);
            new AdService(context, adLayout, new ViewGroup.LayoutParams(adWidth, adHeight), AdBaiduBannerAdapter.this, AdSize.Banner, key2);
        } catch (Exception e) {
            AdBaiduBannerAdapter.this.onAdFailed("com.baidu.mobads.AdService not found");
            e.printStackTrace();
        }
    }

    @Override
    public void onAdReady(AdView adView) {
        if (isReady)
            return;
//        if (!isSwitch) {
        super.onAdRecieved(true);
        super.onAdReady(true);
        isReady = true;
//        }
    }

    @Override
    public void onAdShow(JSONObject jsonObject) {
        if (!isShow) {
            super.onAdDisplay(true);
            isShow = true;
        }
    }

    @Override
    public void onAdClick(JSONObject jsonObject) {
        super.onAdClick(null, null, 888, 888);
    }

    @Override
    public void onAdFailed(String s) {
        if (isFailed)
            return;
        isFailed = true;
        if (!isSwitch)
            super.onAdFailed(s);

    }

    @Override
    public void onAdSwitch() {
        isSwitch = true;
    }

    @Override
    public void onAdClose(JSONObject jsonObject) {
        super.onAdClosed();
    }
}
