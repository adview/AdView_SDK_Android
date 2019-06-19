package com.kuaiyou.loader.loaderInterface;

import android.view.View;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/3/14.
 */
public interface AdViewNativeListener {
    void onNativeAdReceived(List<HashMap> nativeAdList);

    void onNativeAdReceiveFailed(String error);

    void onDownloadStatusChange(int status);

    void onNativeAdClosed(View view);

}