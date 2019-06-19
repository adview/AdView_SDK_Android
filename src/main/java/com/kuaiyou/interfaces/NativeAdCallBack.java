package com.kuaiyou.interfaces;


import android.view.View;

import java.util.HashMap;
import java.util.List;

public interface NativeAdCallBack {

    void onNativeAdReceived(List<HashMap<String, Object>> nativeAdList);

    void onNativeAdReceiveFailed(String msg);

    void onDownloadStatusChange(int status);

    void onNativeAdClosed(View view);
}
