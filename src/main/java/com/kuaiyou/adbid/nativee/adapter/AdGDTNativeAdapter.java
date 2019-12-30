package com.kuaiyou.adbid.nativee.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.KyNativeListener;
import com.kuaiyou.utils.AdViewUtils;
import com.qq.e.ads.nativ.NativeAD;
import com.qq.e.ads.nativ.NativeADDataRef;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdGDTNativeAdapter extends AdAdapterManager implements NativeAD.NativeAdListener {
    private ArrayList<NativeADDataRef> adData = new ArrayList<NativeADDataRef>();
    private KyNativeListener kyNativeListener;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdGDTNativeAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.qq.e.ads.nativ.NativeAD"))
                AdGDTNativeAdapter.this.onAdFailed("com.qq.e.ads.nativ.NativeAD not found");
            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");
            kyNativeListener = (KyNativeListener) bundle.getSerializable("interface");

        //wilder 2019 for test keys ,must use GDTdemo package
        key1 = "1101152570";
        key2 = "5010320697302671";
            NativeAD nativeAD = new NativeAD(context, key1, key2, this);
            nativeAD.loadAD(kyNativeListener.getAdCount());
        } catch (Exception e) {
            e.printStackTrace();
            AdGDTNativeAdapter.this.onAdFailed("com.qq.e.ads.nativ.NativeAD not found");
        }
    }

    @Override
    public void onADLoaded(List<NativeADDataRef> list) {
        try {
            super.onAdReturned(toNativeInfoMap(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        try {
            AdViewUtils.logInfo(String.format("gdt onNoAD, eCode=%d, errorMsg=%s", adError.getErrorCode(),
                    adError.getErrorMsg()));
            super.onAdFailed(adError.getErrorMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onADStatusChanged(NativeADDataRef nativeADDataRef) {
        int status = castNativeStatus(nativeADDataRef.getAPPStatus());
        if (-1 != status) {
            super.onAdStatusChanged(status);
        }
    }

    @Override
    public void onADError(NativeADDataRef nativeADDataRef, AdError adError) {
        AdViewUtils.logInfo("onADError:" + adError);
    }


    private List<HashMap<String, Object>> toNativeInfoMap(List<NativeADDataRef> list) {
        ArrayList<HashMap<String, Object>> nativeAdInfos = new ArrayList<HashMap<String, Object>>();
        try {
            for (int i = 0; i < list.size(); i++) {
                NativeADDataRef adItem = list.get(i);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("title", adItem.getTitle());
                item.put("iconUrl", adItem.getIconUrl());
                item.put("description", adItem.getDesc());
                item.put("imageUrl", adItem.getImgUrl());
                if (null != adItem.getImgList())
                    item.put("imageList", adItem.getImgList());
                item.put("adItem", adItem);
                nativeAdInfos.add(item);
                adData.add(adItem);
            }
            return nativeAdInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private int castNativeStatus(int status) {
        try {
            if (status == 4 || status == 8 || status == 16)
                return status;

        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }

    @Override
    public void reportClick(Object... o) {
        try {
            super.reportClick(o);
            int count = Integer.valueOf(o[1] + "");
            adData.get(count).onClicked((View) o[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportImpression(Object... o) {
        try {
            super.reportImpression(o);
            int count = Integer.valueOf(o[1] + "");
            adData.get(count).onExposured((View) o[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
