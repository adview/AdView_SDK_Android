package com.kuaiyou.interfaces;

import android.content.Context;

import com.kuaiyou.obj.AdsBean;

public interface VideoReqCallback {
//    void onReceivedVideo(String vast, int cacheTime);

    void onReceivedVideo(AdsBean adsBean, int cacheTime);

    void onFailedReceived(AdsBean adsBean,int code, String message);

//    Context getContext();
//
//    int getOrientation();
}
