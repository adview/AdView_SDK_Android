package com.kuaiyou.adbid.banner.adapter;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.baidu.mobads.AdView;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.comm.util.AdError;

//appid：1104709687
//posid: 开屏：5050157575992990    banner：4080755505690876    插屏：7050858575792805

public class AdGDTBannerAdapter extends AdAdapterManager {
    private BannerView banner;
    private int count = -1;
    private boolean isFirstReceived = true;
    private boolean isFirstFailed = true;

    private RelativeLayout parentView; //wilder 2019

    public AdGDTBannerAdapter() {

    }

    @Override
    public View getAdView() {
        return banner;
    }

    @Override
    protected void initAdapter(Context context) {
            AdViewUtils.logInfo("initAdapter AdGDTBannerAdapter");
    }

    @Override
    public void handleAd(final Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.qq.e.ads.banner.BannerView")) {
                AdGDTBannerAdapter.this.onAdFailed("com.qq.e.ads.banner.BannerView not found");
                return;
            }
            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");
            /*wilder 2019 test keys,these must be combine with app package name*/
            //key1 = "1101152570";  //appid
            //key2 = "9079537218417626401"; //pos id
//            key1 = "1104709687";
//            key2 = "4080755505690876";
            if (!TextUtils.isEmpty(key1) && !TextUtils.isEmpty(key2)) {
                banner = new BannerView((Activity) context, ADSize.BANNER, key1, key2);
                banner.setRefresh(0);
                banner.setADListener(new AbstractBannerADListener() {

                    @Override
                    public void onNoAD(AdError adError) {
                        try {
                            AdViewUtils.logInfo(String.format("gdt onNoAD, eCode=%d, errorMsg=%s", adError.getErrorCode(),
                                    adError.getErrorMsg()));
                            AdGDTBannerAdapter.this.onAdFailed(adError.getErrorMsg(), isFirstFailed);
                            isFirstFailed = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onADExposure() {
                        AdViewUtils.logInfo("On BannerAD Exposured");
                        AdGDTBannerAdapter.this.onAdDisplay(true);
                    }

                    @Override
                    public void onADReceiv() {
                        try {
                            AdGDTBannerAdapter.this.onAdRecieved(isFirstReceived);
                            AdGDTBannerAdapter.this.onAdReady(isFirstReceived);
                            //AdGDTBannerAdapter.this.onAdDisplay(true);
                            isFirstReceived = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onADClicked() {
                        try {
                            AdGDTBannerAdapter.this.onAdClick(null, null, 888, 888);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                /* (wilder 2019) for test, update UI should be in handler, if control UI component directly
                * it will be crash in those under Android 6.0 phones.*/
                /*  //widler 2019, use the following codes can fit the AD to full parentview, or the ad will be displayed
                    //with it's own size
                parentView = (RelativeLayout) bundle.getSerializable("parentView");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //parentView.removeAllViews();
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        parentView.addView(banner, layoutParams);
                    }
                }, 0);
                */
                banner.loadAD();
            }

        } catch (Exception e) {
            AdGDTBannerAdapter.this.onAdFailed("com.qq.e.ads.banner.BannerView not found");
            e.printStackTrace();
        }
    }

    @Override
    public int getSufId() {
        return count;
    }
}
