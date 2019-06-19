package com.kuaiyou.adbid.nativee.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.AdView;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;
import com.qq.e.ads.nativ.NativeADDataRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdBaiduNativeAdapter extends AdAdapterManager implements BaiduNative.BaiduNativeNetworkListener {
    private ArrayList<NativeResponse> adData = new ArrayList<NativeResponse>();

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdBaiduNativeAdapter");

    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.baidu.mobad.feeds.BaiduNative")) {
                AdBaiduNativeAdapter.this.onAdFailed("com.baidu.mobad.feeds.BaiduNative not found");
                return;
            }

            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");
            AdView.setAppSid(context, key1);
            BaiduNative baidu = new BaiduNative(context, key2, this);
            RequestParameters requestParameters =
                    new RequestParameters.Builder().confirmDownloading(false).build();
            baidu.makeRequest(requestParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNativeLoad(List<NativeResponse> list) {
        super.onAdReturned(toNativeInfoMap(list));
    }

    @Override
    public void onNativeFail(NativeErrorCode nativeErrorCode) {
        super.onAdFailed(nativeErrorCode.toString());
    }

    private List<HashMap<String, Object>> toNativeInfoMap(List<NativeResponse> list) {
        ArrayList<HashMap<String, Object>> nativeAdInfos = new ArrayList<HashMap<String, Object>>();
        try {
            for (int i = 0; i < list.size(); i++) {
                NativeResponse adItem = list.get(i);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("title", adItem.getTitle());
                item.put("iconUrl", adItem.getIconUrl());
                item.put("description", adItem.getDesc());
                item.put("imageUrl", adItem.getImageUrl());
                item.put("imageList", adItem.getMultiPicUrls());
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

    @Override
    public void reportClick(Object... o) {
        try {
            super.reportClick(o);
            int count = Integer.valueOf(o[1] + "");
            adData.get(count).handleClick((View) o[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportImpression(Object... o) {
        try {
            super.reportImpression(o);
            int count = Integer.valueOf(o[1] + "");
            adData.get(count).recordImpression((View) o[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
