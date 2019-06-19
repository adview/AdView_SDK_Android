package com.kuaiyou.adbid.instl.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.adbid.banner.adapter.AdGDTBannerAdapter;
import com.kuaiyou.utils.AdViewUtils;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.comm.util.AdError;

//1101152570
//        条：9079537218417626401
//        插屏：8575134060152130849
//        开屏：8863364436303842593
//        原生：5010320697302671
public class AdGDTInstlAdapter extends AdAdapterManager {
    private InterstitialAD iad;
    private boolean isRecieved = false;
    private boolean isShow = false;

    private boolean isFirstClicked = true;
    private boolean isFirstReceived = true;
    private boolean isFirstFailed = true;


    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdGDTInstlAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.qq.e.ads.interstitial.InterstitialAD")) {
                AdGDTInstlAdapter.this.onAdFailed("com.qq.e.ads.interstitial.InterstitialAD not found");
                return;
            }
            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");
        //wilder 2019 for test
        key1 = "1101152570";
        key2 = "8575134060152130849";
            iad = new InterstitialAD((Activity) context, key1, key2);
            iad.setADListener(new AbstractInterstitialADListener() {

                @Override
                public void onADReceive() {
                    try {
                        if (isFirstReceived) {
                            isRecieved = true;
                            AdGDTInstlAdapter.this.onAdRecieved();
                            AdGDTInstlAdapter.this.onAdReady();
//                            if (!isShow) {
//                                isRecieved = false;
//                                isShow = true;
//                                show();
//                            }
                            isFirstReceived = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNoAD(AdError adError) {
                    try {
                        if (isFirstFailed) {
                            AdViewUtils.logInfo(String.format("gdt onNoAD, eCode=%d, errorMsg=%s", adError.getErrorCode(),
                                    adError.getErrorMsg()));
                            AdGDTInstlAdapter.this.onAdFailed(adError.getErrorMsg());
                            isFirstFailed = false;
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                @Override
                public void onADClicked() {
                    try {
//                    if(isFirstClicked)
                        AdGDTInstlAdapter.this.onAdClick(null, null, 888, 888);
                        iad.closePopupWindow();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onADClosed() {
                    try {
                        AdGDTInstlAdapter.this.onAdClosed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            iad.loadAD();
        } catch (Exception e) {
            AdGDTInstlAdapter.super.onAdFailed("com.qq.e.ads.interstial.InterstitialAd not found");
            e.printStackTrace();
        }
    }

    @Override
    public boolean showInstl(Activity activity) {
        try {
            if (isRecieved) {
                isRecieved = false;
                show();
                return true;
            }
//            super.showInstl(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public void show() {
        try {
            iad.show();
            super.onAdDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
