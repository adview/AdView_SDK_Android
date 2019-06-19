package com.kuaiyou.interfaces;

import android.os.Message;
import android.view.View;

import com.kuaiyou.obj.AgDataBean;

import java.io.Serializable;
import java.util.List;

public interface KyNativeListener extends KyBaseListener {

    //    void onReceivedAd(AgDataBean agDataBean);
//
//    void onAdFailed(AgDataBean agDataBean, String error);
//
    void onNativeAdReturned(AgDataBean agDataBean, List list);

    //
    int onNativeStatusChange(int status);

    //
    void onNativAdClosed(View view);
//
//    void rotatedAd(Message msg);

    int getNativeWidth();

    int getNativeHeight();

    int getAdCount();

}
