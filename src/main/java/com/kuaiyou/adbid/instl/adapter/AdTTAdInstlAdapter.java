package com.kuaiyou.adbid.instl.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdManagerFactory;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTInteractionAd;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

public class AdTTAdInstlAdapter extends AdAdapterManager implements TTAdNative.InteractionAdListener, TTInteractionAd.AdInteractionListener {

    private boolean isRecieved;
    private TTInteractionAd ttInteractionAd;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdTTAdInstlAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTInteractionAd")) {
                super.onAdFailed("com.bytedance.sdk.openadsdk.TTInteractionAd not found");
                return;
            }
            String key = bundle.getString("appId");
            String key2 = bundle.getString("posId");

            double density = AdViewUtils.getDensity(context);
            int adWidth = (int) (300 * density);

            TTAdManager ttAdManager = TTAdManagerFactory.getInstance(context);
            ttAdManager.setAppId(key);
            ttAdManager.setName(AdViewUtils.getAppName(context));
            TTAdNative ttAdNative = ttAdManager.createAdNative(context);

            AdSlot adSlot = new AdSlot.Builder().setCodeId(key2).setImageAcceptedSize(adWidth, adWidth).build();

            ttAdNative.loadInteractionAd(adSlot, this);

        } catch (Exception e) {
            e.printStackTrace();
            super.onAdFailed("com.bytedance.sdk.openadsdk.TTInteractionAd not found");
        }
    }

    @Override
    public void onError(int i, String s) {
        super.onAdFailed("errorCode=" + i + ";" + s);
    }

    @Override
    public void onInteractionAdLoad(TTInteractionAd ttInteractionAd) {
        this.ttInteractionAd = ttInteractionAd;
        ttInteractionAd.setAdInteractionListener(this);
        isRecieved = true;
        super.onAdRecieved();
        super.onAdReady();
    }


    @Override
    public void onAdClicked() {

        super.onAdClick(null, null, 888, 888);
    }

    @Override
    public void onAdShow() {

        super.onAdDisplay();
    }

    @Override
    public void onAdDismiss() {
        super.onAdClosed();

    }

    @Override
    public boolean showInstl(Activity activity) {
        try {
            if (isRecieved) {
                isRecieved = false;
                if (null != ttInteractionAd) {
                    ttInteractionAd.showInteractionAd(activity);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
