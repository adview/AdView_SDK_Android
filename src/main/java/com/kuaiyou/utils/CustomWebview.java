package com.kuaiyou.utils;

import android.content.Context;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class CustomWebview extends WebView {

    private boolean enable = false, isClicked = false;
    private int type, tag;

    private CustomInterface customInterface;
    private CustomClickInterface customClickInterface;

    public CustomWebview(Context context, boolean isVideo) {
        super(context);

        WebSettings webSetting = getSettings();
        webSetting.setJavaScriptEnabled(true);
        //wilder 2019 for cookie
        CookieManager.getInstance().setAcceptFileSchemeCookies(true);
        CookieManager.getInstance().acceptCookie();
        CookieManager.getInstance().setAcceptCookie(true);
        //end wilder 2019
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSetting.setMediaPlaybackRequiresUserGesture(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //wilder 2019 fix SSL issue
            CookieManager.getInstance().setAcceptThirdPartyCookies(this,true);
            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //settings
        String appCacheDir = context.getApplicationContext().
                            getDir("cache", Context.MODE_PRIVATE).getPath();

        webSetting.setBlockNetworkImage(false);
        webSetting.setDomStorageEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDatabasePath(appCacheDir);

        webSetting.setAllowFileAccess(true);
        //webSetting.setAppCacheMaxSize( 10 * 1024 * 1024 ); // 10MB
        //wilder for test 20191206 ,covered
//        webSetting.setAppCachePath(appCacheDir);
//        webSetting.setAppCacheEnabled(true);
//        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        // webSetting.setCacheMode(WebSettings.LOAD_CACHE_ONLY);

        //end settings
        if (isVideo) {
            //以下参数不是很好控制
            //webSetting.setBuiltInZoomControls(true);// 隐藏缩放按钮
            //webSetting.setUseWideViewPort(true);// 可任意比例缩放
            //webSetting.setSupportZoom(true);
            webSetting.setLoadWithOverviewMode(true);// setUseWideViewPort方法设置webview推荐使用的窗口。setLoadWithOverviewMode方法是设置webview加载的页面的模式。
            this.setLayerType(View.LAYER_TYPE_HARDWARE, null); //(wiler 2019) if show blank, pls use LAYER_TYPE_SOFTWARE

        }
        else {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null); //如果不是video,最好使用software,便于网页缩放显示
            //webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
            webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        }
        this.setScrollbarFadingEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
    }

    public void setTouchEventEnable(boolean enable) {
        this.enable = enable;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCustomInterface(CustomInterface customInterface) {
        this.customInterface = customInterface;
    }

    public void setCustomClickInterface(CustomClickInterface customClickInterface) {
        this.customClickInterface = customClickInterface;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //AdViewUtils.logInfo("customWebView:dispatchTouchEvent()");
        if (enable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:

                    if (null != customClickInterface)
                        customClickInterface.onWebviewClicked(type, tag);
                    break;
            }
        }
        this.isClicked = true;
        return super.dispatchTouchEvent(ev);
    }

    public void setClicked(boolean b) {
        isClicked = b;
    }

    public boolean isClicked() {
        return isClicked;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top,
                            int right, int bottom) {
        if(customInterface != null) {
            customInterface.onWebviewLayout(changed, left, top, right, bottom);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public interface CustomClickInterface {

        void onWebviewClicked(int type, int tag);

    }
    public interface CustomInterface {
        void onWebviewLayout(boolean changed, int left, int top,
                             int right, int bottom);
    }
}