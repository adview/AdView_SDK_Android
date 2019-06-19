package com.kuaiyou.loader.loaderInterface;

/**
 * Created by Administrator on 2017/3/14.
 */
public interface AdViewBannerListener {
    void onAdClicked();

    void onAdDisplayed();

    void onAdReceived();

    void onAdFailedReceived(String error);

    void onAdClosed();

    void onAdReady(); //new added 2019

}
