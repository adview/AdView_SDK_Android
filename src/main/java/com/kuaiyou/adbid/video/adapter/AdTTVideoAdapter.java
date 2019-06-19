package com.kuaiyou.adbid.video.adapter;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdManagerFactory;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

public class AdTTVideoAdapter extends AdAdapterManager {

    private int width, height;
    private TTFullScreenVideoAd ttFullScreenVideoAd;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdTTAdBannerAdapter");
        int[] sw = AdViewUtils.getWidthAndHeight(context, true);
        width = sw[0];
        height = sw[1];
    }

    @Override
    public void handleAd(final Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTFullScreenVideoAd")) {
                super.onAdFailed("com.bytedance.sdk.openadsdk.TTFullScreenVideoAd not found");
                return;
            }

            String key = bundle.getString("appId");
            String key2 = bundle.getString("posId");

//            key = "5001121";
//            key2 = "901121375";
//      kyViewListener = (KySpreadListener) bundle.getSerializable("interface");


            TTAdManager ttAdManager = TTAdManagerFactory.getInstance(context);
            ttAdManager.setAppId(key);
            ttAdManager.setName(AdViewUtils.getAppName(context) + "");
            TTAdNative ttAdNative = ttAdManager.createAdNative(context);

            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(key2)
                    .setSupportDeepLink(true)
                    .setImageAcceptedSize(width, height)
                    .setOrientation(TTAdConstant.HORIZONTAL)
                    .build();

            ttAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
                @Override
                public void onError(int i, String s) {
                    AdTTVideoAdapter.super.onAdFailed(s);
                }

                @Override
                public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {
                    AdTTVideoAdapter.super.onAdRecieved();
                    AdTTVideoAdapter.this.ttFullScreenVideoAd = ttFullScreenVideoAd;
                    ttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                        @Override
                        public void onAdShow() {
                            AdTTVideoAdapter.super.onVideoStartPlay();
                            AdTTVideoAdapter.super.onAdDisplay();
                        }

                        @Override
                        public void onAdVideoBarClick() {
                            AdTTVideoAdapter.super.onAdClick(null, null, 0, 0);
                        }

                        @Override
                        public void onAdClose() {
                            AdTTVideoAdapter.super.onAdClosed();
                        }

                        @Override
                        public void onVideoComplete() {
                            AdTTVideoAdapter.super.onVideoFinished();
                        }

                        @Override
                        public void onSkippedVideo() {
                            AdTTVideoAdapter.super.onVideoFinished();
                        }
                    });
                }

                @Override
                public void onFullScreenVideoCached() {
                    AdTTVideoAdapter.super.onAdReady();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean playVideo(Context context) {
        try {
            if (null != ttFullScreenVideoAd)
                ttFullScreenVideoAd.showFullScreenVideoAd((Activity) context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
