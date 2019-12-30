package com.kuaiyou.adbid.nativee.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdManagerFactory;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdTTAdNativeAdapter extends AdAdapterManager implements TTAdNative.FeedAdListener {
    private ArrayList<TTFeedAd> adData = new ArrayList<TTFeedAd>();
    private LinearLayout linearLayout;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundColor(Color.RED);
        ((Activity) context).addContentView(linearLayout, new LinearLayout.LayoutParams(400, 400));
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTFeedAd")) {
                super.onAdFailed("com.bytedance.sdk.openadsdk.TTFeedAd not found");
                return;
            }
            String key = bundle.getString("appId");
            String key2 = bundle.getString("posId");
            TTAdManager ttAdManager = TTAdManagerFactory.getInstance(context);
            ttAdManager.setAppId(key);
            ttAdManager.setName(AdViewUtils.getAppName(context));
            TTAdNative ttAdNative = ttAdManager.createAdNative(context);


            AdSlot adSlot = new AdSlot.Builder().setCodeId(key2).setImageAcceptedSize(640, 320).build();
            ttAdNative.loadFeedAd(adSlot, this);
        } catch (Exception e) {
            e.printStackTrace();
            super.onAdFailed("com.bytedance.sdk.openadsdk.TTFeedAd not found");
        }
    }

    @Override
    public void onError(int i, String s) {
        super.onAdFailed(s);
    }

    @Override
    public void onFeedAdLoad(List<TTFeedAd> list) {
        super.onAdReturned(toNativeInfoMap(list));
    }

    private List<HashMap<String, Object>> toNativeInfoMap(List<TTFeedAd> list) {
        ArrayList<View> viewList = new ArrayList<View>();
        viewList.add(linearLayout);
        list.get(0).registerViewForInteraction(linearLayout, linearLayout, new TTFeedAd.AdInteractionListener() {

            @Override
            public void onAdClicked(View view, TTNativeAd ttFeedAd) {

            }

            @Override
            public void onAdCreativeClick(View view, TTNativeAd ttFeedAd) {

            }

            @Override
            public void onAdShow(TTNativeAd ttFeedAd) {

            }
        });
        ArrayList<HashMap<String, Object>> nativeAdInfos = new ArrayList<HashMap<String, Object>>();
        try {
            for (int i = 0; i < list.size(); i++) {
                TTFeedAd adItem = list.get(i);
                HashMap<String, Object> item = new HashMap<String, Object>();
                TTImage logo = adItem.getIcon();
                ArrayList<String> imageList = new ArrayList<String>();

                if (null != logo)
                    item.put("iconUrl", logo.getImageUrl());
                for (int j = 0; j < adItem.getImageList().size(); j++) {
                    if (null != adItem.getImageList().get(j))
                        imageList.add(adItem.getImageList().get(j).getImageUrl());
                }
                if (imageList.size() > 0)
                    item.put("imageUrl", imageList.get(0));
                item.put("imageList", imageList);

                item.put("description", adItem.getDescription());
                item.put("adItem", adItem);
                item.put("title", adItem.getTitle());

                nativeAdInfos.add(item);
                adData.add(adItem);
            }
            return nativeAdInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
