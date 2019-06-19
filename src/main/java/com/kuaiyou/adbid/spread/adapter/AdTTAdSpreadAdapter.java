package com.kuaiyou.adbid.spread.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdManagerFactory;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.KySpreadListener;
import com.kuaiyou.utils.AdViewUtils;

public class AdTTAdSpreadAdapter extends AdAdapterManager implements TTAdNative.SplashAdListener, TTSplashAd.AdInteractionListener {
    private KySpreadListener kyViewListener;
    private int[] size;
    private boolean isClicked = false;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdTTAdSpreadAdapter");
        size = AdViewUtils.getWidthAndHeight(context, true);
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTSplashAd")) {
                super.onAdFailed("com.bytedance.sdk.openadsdk.TTSplashAd not found");
                return;
            }

            String key = bundle.getString("appId");
            String key2 = bundle.getString("posId");

//            key = "5001121";
//            key2 = "801121648";
            kyViewListener = (KySpreadListener) bundle.getSerializable("interface");

            TTAdManager ttAdManager = TTAdManagerFactory.getInstance(context);
            ttAdManager.setAppId(key);
            ttAdManager.setName(AdViewUtils.getAppName(context));
            TTAdNative ttAdNative = ttAdManager.createAdNative(context);


            AdSlot adSlot = new AdSlot.Builder().setCodeId(key2).setImageAcceptedSize(size[0], size[1]).build();
            ttAdNative.loadSplashAd(adSlot, this);
        } catch (Exception e) {
            e.printStackTrace();
            super.onAdFailed("com.bytedance.sdk.openadsdk.TTSplashAd not found");
        }
    }

    @Override
    public void onError(int i, String s) {
        super.onAdFailed(s);
    }

    @Override
    public void onTimeout() {
        super.onAdFailed("spread ad timeout");
    }

    @Override
    public void onSplashAdLoad(TTSplashAd ttSplashAd) {
        try {
            ttSplashAd.setSplashInteractionListener(this);
            super.onAdRecieved();
            (kyViewListener.getSpreadView()).addView(ttSplashAd.getSplashView(), new RelativeLayout.LayoutParams(size[0], size[1]));
            super.onAdReady();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onAdClicked(View view, int i) {
        isClicked = true;
        super.onAdClick(null, null, 888, 888);
    }

    @Override
    public void onAdShow(View view, int i) {
        if (isClicked && i == 3) {
            super.onAdClosed();
            return;
        }
        super.onAdDisplay();
    }

    @Override
    public void onAdSkip() {
        super.onAdClosed();
    }

    @Override
    public void onAdTimeOver() {
        super.onAdClosed();
    }

}
