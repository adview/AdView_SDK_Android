package com.kuaiyou.adbid.spread.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.KySpreadListener;
import com.kuaiyou.utils.AdViewUtils;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

public class AdGDTSpreadAdapter extends AdAdapterManager implements com.qq.e.ads.splash.SplashADListener {

    private SplashAD splashAD;
    private KySpreadListener kyViewListener;
    private RelativeLayout parentView;
    //appid：1104709687
    //posid: 开屏：5050157575992990    banner：4080755505690876    插屏：7050858575792805

    public AdGDTSpreadAdapter() {

    }

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdGDTSpreadAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.qq.e.ads.splash.SplashAD")) {
                AdGDTSpreadAdapter.this.onAdFailed("com.qq.e.ads.splash.SplashAD not found");
                return;
            }
            kyViewListener = (KySpreadListener) bundle.getSerializable("interface");
            String appID, posID;
            appID = bundle.getString("appId");
            posID = bundle.getString("posId");
            //wilder 2019 test key
            //appID = "1101152570";
            //posID = "8863364436303842593";

            /*
            parentView = (RelativeLayout) bundle.getSerializable("parentView");
            //splashAD = new SplashAD(activity, adContainer, skipContainer, appId, posId, adListener, fetchDelay);
            FrameLayout f_layout = new FrameLayout(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            //layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            parentView.addView(f_layout, layoutParams);
            */

            splashAD = new SplashAD((Activity) context,
                    //parentView,
                    (ViewGroup) kyViewListener.getSpreadView().getParent(),
                    kyViewListener.getSkipView(),
                    appID, posID, this, 0);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onADDismissed() {
        this.onAdClosed();
    }

    @Override
    public void onNoAD(AdError adError) {
        AdViewUtils.logInfo(String.format("gdt onNoAD, eCode=%d, errorMsg=%s", adError.getErrorCode(),
                adError.getErrorMsg()));
        super.onAdFailed(adError.getErrorMsg());
    }

    @Override
    public void onADPresent() {
        super.onAdRecieved();
        super.onAdDisplay();
    }

    @Override
    public void onADClicked() {

        super.onAdClick(null, null, 888, 888);
    }

    @Override
    public void onADTick(long millisUntilFinished) {
        AdViewUtils.logInfo("SplashADTick " + millisUntilFinished + "ms");
    }

    @Override
    public void onADExposure() {

        AdViewUtils.logInfo("onADExposure");

    }
}
