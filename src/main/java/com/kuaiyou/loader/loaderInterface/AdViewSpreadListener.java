package com.kuaiyou.loader.loaderInterface;

/**
 * Created by Administrator on 2017/3/14.
 */
public interface AdViewSpreadListener {
    void onAdClicked();

    void onAdDisplayed();

    void onAdReceived();

    void onAdFailedReceived(String error);

    void onAdClosed();

    void onAdSpreadPrepareClosed();

    void onAdClosedByUser();

    void onAdNotifyCustomCallback(int ruleTime,int delayTime);
}
