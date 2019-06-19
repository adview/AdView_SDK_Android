package com.kuaiyou.adbid.nativee.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

public class AdBIDNativeAdapter extends AdAdapterManager{
    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdBIDNativeAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {


    }
}
