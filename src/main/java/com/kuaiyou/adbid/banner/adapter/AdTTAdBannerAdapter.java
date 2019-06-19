package com.kuaiyou.adbid.banner.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdManagerFactory;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

public class AdTTAdBannerAdapter extends AdAdapterManager implements TTAdNative.BannerAdListener, TTBannerAd.AdInteractionListener {

    private RelativeLayout adLayout;
    private int adWidth, adHeight;

    @Override
    public View getAdView() {
        return adLayout;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdTTAdBannerAdapter");
        adLayout = new RelativeLayout(context);
        double density = AdViewUtils.getDensity(context);
        adWidth = (int) (320 * density);
        adHeight = (int) (50 * density);
        adLayout.setLayoutParams(new RelativeLayout.LayoutParams(adWidth, adHeight));
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTBannerAd")) {
                super.onAdFailed("com.bytedance.sdk.openadsdk.TTBannerAd not found");
                return;
            }
            String key = bundle.getString("appId");
            String key2 = bundle.getString("posId");
//            key = "5001121";//"5002623";
//            key2 = "901121895";//"902623068";
            TTAdManager ttAdManager = TTAdManagerFactory.getInstance(context);

            ttAdManager.setAppId(key);
            ttAdManager.setName(AdViewUtils.getAppName(context));
            TTAdNative ttAdNative = ttAdManager.createAdNative(context);

            AdSlot adSlot = new AdSlot.Builder().setCodeId(key2).setImageAcceptedSize(adWidth, adHeight).build();
            ttAdNative.loadBannerAd(adSlot, this);
        } catch (Exception e) {
            e.printStackTrace();
            super.onAdFailed("com.bytedance.sdk.openadsdk.TTBannerAd not found");
        }
    }

    @Override
    public void onError(int i, String s) {
        super.onAdFailed(s,true);
    }

    @Override
    public void onBannerAdLoad(TTBannerAd ttBannerAd) {
        try {
            ((FrameLayout)ttBannerAd.getBannerView()).getChildAt(0).setLayoutParams(new FrameLayout.LayoutParams(adWidth, adHeight));
            adLayout.addView(ttBannerAd.getBannerView(),new RelativeLayout.LayoutParams(adWidth, adHeight));
            ttBannerAd.setBannerInteractionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onAdRecieved(true);
    }

    @Override
    public void onAdClicked(View view, int i) {
        super.onAdClick(null, null, 888, 888);
    }

    @Override
    public void onAdShow(View view, int i) {
        super.onAdReady(true);
        super.onAdDisplay(true);
    }
}
