package com.kuaiyou.adbid.nativee.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.KyNativeListener;
import com.kuaiyou.utils.AdViewUtils;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdGDTNativeExpressAdapter extends AdAdapterManager implements NativeExpressAD.NativeExpressADListener {
    private ArrayList<NativeExpressADView> adData = new ArrayList<NativeExpressADView>();
    private KyNativeListener kyViewListener;
    private int total = 0;
    private ArrayList<HashMap<String, Object>> nativeAdInfos = new ArrayList<HashMap<String, Object>>();

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdGDTNativeExpressAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        try {
            if (!KyAdBaseView.checkClass("com.qq.e.ads.nativ.NativeExpressADView"))
                AdGDTNativeExpressAdapter.this.onAdFailed("com.qq.e.ads.nativ.NativeExpressADView not found");
            String key1, key2;
            key1 = bundle.getString("appId");
            key2 = bundle.getString("posId");
//            key1 = "1107802763";
//            key2 = "1020847490414083";
            kyViewListener = (KyNativeListener) bundle.getSerializable("interface");
            NativeExpressAD nativeAd = new NativeExpressAD(context, new ADSize(kyViewListener.getNativeWidth(), kyViewListener.getNativeHeight()), key1, key2, this);
            nativeAd.loadAD(kyViewListener.getAdCount());
        } catch (Exception e) {
            e.printStackTrace();
            AdGDTNativeExpressAdapter.this.onAdFailed("com.qq.e.ads.nativ.NativeExpressADView not found,maybe other init error");
        }
    }


    @Override
    public void onNoAD(AdError adError) {
        try {
            AdViewUtils.logInfo(String.format("gdt failed, eCode=%d, errorMsg=%s", adError.getErrorCode(),
                    adError.getErrorMsg()));
            super.onAdFailed(adError.getErrorMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportClick(Object... o) {

    }

    @Override
    public void reportImpression(Object... o) {

    }

    @Override
    public void onADLoaded(List<NativeExpressADView> list) {
        try {
            if (list != null && list.size() > 0) {
                total = list.size();
                AdViewUtils.logInfo("gdt ad returned ,ad size is " + list.size());
                for (NativeExpressADView nativeExpressADView : list) {
                    nativeExpressADView.render();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRenderFail(NativeExpressADView nativeExpressADView) {
        AdViewUtils.logInfo("gdt ad returned onRenderFail");
        total--;
    }

    @Override
    public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
        try {
            AdViewUtils.logInfo("gdt ad returned onRenderSuccess");
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("nativeView", nativeExpressADView);
            nativeAdInfos.add(item);
            total--;
            if (total == 0)
                super.onAdReturned(nativeAdInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onADExposure(NativeExpressADView nativeExpressADView) {
        //super.reportImpression(new Class[]{});
        super.reportImpression(new Object[]{});  //wilder 20190220
    }

    @Override
    public void onADClicked(NativeExpressADView nativeExpressADView) {
        //super.reportClick(new Class[]{});
        super.reportClick(new Object[]{}); //wilder 2019
    }

    @Override
    public void onADClosed(NativeExpressADView nativeExpressADView) {
        super.onAdClosed(nativeExpressADView);
    }

    @Override
    public void onADLeftApplication(NativeExpressADView nativeExpressADView) {

    }

    @Override
    public void onADOpenOverlay(NativeExpressADView nativeExpressADView) {

    }

    @Override
    public void onADCloseOverlay(NativeExpressADView nativeExpressADView) {

    }

    @Override
    public void destroyAd() {
        try {
            super.destroyAd();
            for (int i = 0; i < nativeAdInfos.size(); i++) {
                NativeExpressADView nativeExpressADView = (NativeExpressADView) nativeAdInfos.get(i).get("nativeView");
                nativeExpressADView.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
