package com.kuaiyou.adbid.spread.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mobads.AdView;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.KySpreadListener;
import com.kuaiyou.utils.AdViewUtils;

public class AdBaiduSpreadAdapter extends AdAdapterManager implements SplashAdListener {
    private boolean isRecieved;
    private boolean isFailed;
    private SplashAd splashAd;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdBaiduSpreadAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.baidu.mobads.SplashAd")) {
                AdBaiduSpreadAdapter.this.onAdFailed("com.baidu.mobads.SplashAd not found");
                return;
            }
            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");

            KySpreadListener kyViewListener = (KySpreadListener) bundle.getSerializable("interface");
            if (null == kyViewListener) {
                AdBaiduSpreadAdapter.this.onAdFailed("layout is null");
                return;
            }
            AdView.setAppSid(context, key1);
            splashAd = new SplashAd(context, (ViewGroup) kyViewListener.getSpreadView().getParent(), this, key2, true);
        } catch (Exception e) {
            e.printStackTrace();
            AdBaiduSpreadAdapter.this.onAdFailed("com.baidu.mobads.SplashAd not found");
        }
    }


    @Override
    public void onAdReady() {

    }

    @Override
    public void onAdPresent() {
        isRecieved = true;
        super.onAdRecieved(true);
        super.onAdReady(true);
        super.onAdDisplay(true);
    }


    @Override
    public void onAdDismissed() {
        super.onAdClosed();
    }

    @Override
    public void onAdFailed(String s) {
        if (!isFailed) {
            isFailed = true;
            super.onAdFailed(s);
        } else {
            if (null != splashAd)
                splashAd.destroy();
        }

    }

    @Override
    public void onAdClick() {
        super.onAdClick(null, null, 888, 888);
    }

}