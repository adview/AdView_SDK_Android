package com.kuaiyou.mraid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.kuaiyou.mraid.interfaces.MRAIDNativeFeatureListener;
import com.kuaiyou.mraid.interfaces.MRAIDViewListener;
import com.kuaiyou.mraid.utils.MRAIDNativeFeatureManager;
import com.kuaiyou.mraid.utils.MRAIDNativeFeatureProvider;
import com.kuaiyou.mraid.utils.MRAIDOrientationProperties;
import com.kuaiyou.mraid.utils.MRAIDParser;
import com.kuaiyou.mraid.utils.MRAIDResizeProperties;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


@SuppressLint("SetJavaScriptEnabled")
public class MRAIDView extends RelativeLayout {

    private GestureDetector mGestureDetector;
    private WebViewMotion webViewMotion;

    public static final int ACTION_DEFAULT = 0;
    public static final int ACTION_DOWN = 1;
    public static final int ACTION_TAP = 2;

    // private final static String AdViewUtils.ADVIEW = "MRAIDView";
    // public static final String VERSION = "1.1.1";

    public final static int STATE_LOADING = 0;
    public final static int STATE_DEFAULT = 1;
    public final static int STATE_EXPANDED = 2;
    public final static int STATE_RESIZED = 3;
    public final static int STATE_HIDDEN = 4;

    // in dip
    private final static int CLOSE_REGION_SIZE = 50;

    // UI elements
    private WebView webView;
    private WebView webViewPart2;
    private WebView currentWebView;
    private MRAIDWebChromeClient mraidWebChromeClient;
    private MRAIDWebViewClient mraidWebViewClient;
    private RelativeLayout expandedView;
    private RelativeLayout resizedView;
    private ImageButton closeRegion;

    private Context context;

    // state
    private final boolean isInterstitial;

    private int state;

    public int getState() {
        return state;
    }

    private boolean isViewable;

    // The only property of the MRAID expandProperties we need to keep track of
    // on the native side is the useCustomClose property.
    // The width, height, and isModal properties are not used in MRAID v2.0.
    private boolean useCustomClose;
    private MRAIDOrientationProperties orientationProperties;
    private MRAIDResizeProperties resizeProperties;

    public MRAIDNativeFeatureManager nativeFeatureManager;

    // listeners
    private MRAIDViewListener listener;
    private MRAIDNativeFeatureListener nativeFeatureListener;

    // used for setting positions and sizes (all in pixels, not dpi)
    private DisplayMetrics displayMetrics;
    private int contentViewTop;
    private Rect currentPosition;
    private Rect defaultPosition;

    private final class Size {
        public int width;
        public int height;
    }

    private static String js_Mraid; //MRaid.js

    private Size maxSize;
    private Size screenSize;
    // state to help set positions and sizes
    private boolean isPageFinished;
    private boolean isLaidOut;
    private boolean isForcingFullScreen;
    private boolean isExpandingFromDefault;
    private boolean isExpandingPart2;
    private boolean isClosing;

    // used to force full-screen mode on expand and to restore original state on
    // close
    private View titleBar;
    private boolean isFullScreen;
    private boolean isForceNotFullScreen;
    private int origTitleBarVisibility;
    private boolean isActionBarShowing;

    // Stores the requested orientation for the Activity to which this MRAIDView
    // belongs.
    // This is needed to restore the Activity's requested orientation in the
    // event that
    // the view itself requires an orientation lock.
    private final int originalRequestedOrientation;

    private float density = 0;

    // This is the contents of mraid.js. We keep it around in case we need to
    // inject it
    // into webViewPart2 (2nd part of 2-part expanded ad).
    private Handler handler;

    private boolean isFirstDown, isFirstUp, clickCheckable = true;

    private RelativeLayout mRootLayout;

    public MRAIDView(Context context, MRAIDNativeFeatureListener mraidNativeFeatureListener, MRAIDViewListener listener, boolean isInterstitial, int w, int h) {
        super(context);
        this.context = context;
        this.isInterstitial = isInterstitial;
        this.nativeFeatureListener = mraidNativeFeatureListener;
        this.density = (float) AdViewUtils.getDensity(context);
        state = STATE_LOADING;
        isViewable = false;
        useCustomClose = false;
        //
        orientationProperties = new MRAIDOrientationProperties();
        //
        resizeProperties = new MRAIDResizeProperties();
        //
        nativeFeatureManager = new MRAIDNativeFeatureManager(context);

        this.listener = listener;
        // this.nativeFeatureListener = nativeFeatureListener;

        displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);

        currentPosition = new Rect();
        defaultPosition = new Rect();

        // 最大尺寸?
        maxSize = new Size();
        if (w > 0 & h > 0) {
            maxSize.width = w;
            maxSize.height = h;
        }
        // 屏幕尺寸
        screenSize = new Size();

        if (context instanceof Activity) {
            originalRequestedOrientation = ((Activity) context).getRequestedOrientation();
        } else {
            originalRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
        AdViewUtils.logInfo("originalRequestedOrientation " + getOrientationString(originalRequestedOrientation));

        // ignore scroll gestures
        //        gestureDetector = new GestureDetector(getContext(),
        //                new SimpleOnGestureListener() {
        //                    @Override
        //                    public boolean onScroll(MotionEvent e1, MotionEvent e2,
        //                                            float distanceX, float distanceY) {
        //                        return true;
        //                    }
        //                });

        // 在主线程中执行
        handler = new Handler(Looper.getMainLooper());

        mraidWebChromeClient = new MRAIDWebChromeClient();
        mraidWebViewClient = new MRAIDWebViewClient();

        webView = createWebView();

        currentWebView = webView;

        // webView.setBackgroundColor(Color.TRANSPARENT);
        if (w <= 0 & h <= 0) {
            addView(webView);
        }
        else {
            addView(webView, new LayoutParams(w, h));
        }

        //load js from file
        try {
            js_Mraid = AdViewUtils.loadAssetsFile("MRAID.js");
        }catch (Exception e) {
            e.printStackTrace();
        }

        injectMraidJs(webView);

    }

    /**
     * @param context
     * @param listener
     * @param isInterstitial
     */
    public MRAIDView(Context context,
                     MRAIDNativeFeatureListener mraidNativeFeatureListener,
                     MRAIDViewListener listener, boolean isInterstitial) {
        this(context, mraidNativeFeatureListener, listener, isInterstitial, -2, -2);
    }

    public void setLayoutParmas(int w, int h) {
//        addView(webView,new LayoutParams(w,h));
//        currentWebView.getLayoutParams().width = w;
//        currentWebView.getLayoutParams().height = h;
    }

    public void resetTouchStatus() {
        isFirstDown = isFirstUp = false;
    }

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    private WebView createWebView() {
        WebView wv = new WebView(context) {

            private MotionEvent downEvent;

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                try {
                    //如果是非二次确认就不做拦截
                    if (!webViewMotion.isNeedConfirm()) {
                        if (ev.getAction() == MotionEvent.ACTION_DOWN)
                            webViewMotion.onWebViewTouchDown(ev);
//                        else if (ev.getAction() == MotionEvent.ACTION_UP)
//                            webViewMotion.onWebViewClickedNormal(ev,null);
                        return super.dispatchTouchEvent(ev);
                    }

                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (isFirstDown) {
                                isFirstDown = false;
                                return super.dispatchTouchEvent(ev);
                            }
                            isFirstDown = true;
                            downEvent = MotionEvent.obtain(ev);
                            webViewMotion.onWebViewTouchDown(ev);

                            return true;
                        case MotionEvent.ACTION_UP:
                            if (isFirstUp) {
                                isFirstUp = false;
                                return super.dispatchTouchEvent(ev);
                            }
                            isFirstUp = true;
                            double density = AdViewUtils.getDensity(getContext());
                            if (clickCheckable)
                                if ((Math.abs(downEvent.getX() - ev.getX()) > density * 20 || Math.abs(downEvent.getY() - ev.getY()) > density * 20))
                                    return true;
                            if (webViewMotion.isNeedConfirm()) {
                                webViewMotion.onWebViewClickedConfirm(downEvent, ev);
                            } else {
                                webViewMotion.onWebViewClickedNormal(downEvent, ev);
                            }
                            return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return super.dispatchTouchEvent(ev);
            }

            @SuppressWarnings("deprecation")
            @Override
            protected void onLayout(boolean changed, int left, int top,
                                    int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                onLayoutWebView(this, changed, left, top, right, bottom);
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
                AdViewUtils.logInfo(
                        "onConfigurationChanged "
                                + (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait"
                                : "landscape"));
                if (isInterstitial) {
                    ((Activity) context).getWindowManager().getDefaultDisplay()
                            .getMetrics(displayMetrics);
                }
            }

            @Override
            protected void onVisibilityChanged(View changedView, int visibility) {
                super.onVisibilityChanged(changedView, visibility);
                AdViewUtils.logInfo("onVisibilityChanged "
                        + getVisibilityString(visibility));
                if (isInterstitial) {
                    setViewable(visibility);
                }
            }

            @Override
            protected void onWindowVisibilityChanged(int visibility) {
                super.onWindowVisibilityChanged(visibility);
                int actualVisibility = getVisibility();
                AdViewUtils.logInfo("onWindowVisibilityChanged "
                        + getVisibilityString(visibility) + " (actual "
                        + getVisibilityString(actualVisibility) + ")");
                if (isInterstitial && visibility != View.GONE) {  //wilder 2019
                    setViewable(actualVisibility);
                }

            }
        };

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        wv.setLayoutParams(params);
        // wv.clearCache(false);
        wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // wv.setAppCacheEnabled(true);
        wv.setScrollContainer(false);
        wv.setVerticalScrollBarEnabled(false);
        wv.setHorizontalScrollBarEnabled(false);
//        wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        wv.setFocusableInTouchMode(true);

        WebSettings webSetting = wv.getSettings();

        String appCacheDir = context.getApplicationContext()
                .getDir("cache", Context.MODE_PRIVATE).getPath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webSetting.setBlockNetworkImage(false);
        webSetting.setDomStorageEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDatabasePath(appCacheDir);
        webSetting.setAppCachePath(appCacheDir);
        webSetting.setAllowFileAccess(true);
        webSetting.setAppCacheEnabled(true);

        webSetting.setSaveFormData(true);;

        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        //webSetting.setCacheMode(WebSettings.LOAD_CACHE_ONLY); //wilder 2019
        webSetting.setJavaScriptEnabled(true);

        //(wilder 2019) added for auto fit height  in view, but if opened, the html may display very small,
        //so no suggested
//        wv.setInitialScale(1);
//        webSetting.setUseWideViewPort(true);
//        webSetting.setLoadWithOverviewMode(true);
//        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        //for cookie
        CookieManager.getInstance().setAcceptFileSchemeCookies(true);
        CookieManager.getInstance().acceptCookie();
        CookieManager.getInstance().setAcceptCookie(true);
        //end wilder 2019

        webSetting.setRenderPriority(RenderPriority.HIGH);

        wv.setWebChromeClient(mraidWebChromeClient);
        wv.setWebViewClient(mraidWebViewClient);
        wv.setDrawingCacheEnabled(true);
        return wv;
    }

    public void setClickCheckable(boolean clickCheckable) {
        this.clickCheckable = clickCheckable;
    }

    public void injectWebViewContent(String data) {
        // data = MRAIDHtmlProcessor.processRawHtml(data);
        if (null != webView)
            // webView.loadData(data, "text/html", "UTF-8");
            // webView.loadData(data, "text/html; charset=UTF-8", null);
            webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
        else
            Log.i(AdViewUtils.ADVIEW, "webView is null");
    }

//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (gestureDetector.onTouchEvent(event)) {
//            event.setAction(MotionEvent.ACTION_CANCEL);
//        }
//        return super.onTouchEvent(event);
//    }

    public void clearView() {
        if (webView != null) {
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.loadUrl("about:blank");
        }
    }

    public void destroy() {
        if (webView != null) {
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
    }

//    /**
//     * 手势识别
//     *
//     * @param context
//     * @param adsBean
//     */
//    public void setOnGestureListener(Context context, AdsBean adsBean) {
//        // setWebViewClient(new AdWebView.mWebViewClient(context, adsBean));
//        mGestureDetector = new GestureDetector(context, new MyGesTureLIstener(
//                adsBean));
//    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
//            return false;
//        }
//        if (null != mGestureDetector)
//            mGestureDetector.onTouchEvent(ev);
//        return super.dispatchTouchEvent(ev);
//    }

    public void setWebViewMotion(WebViewMotion webViewMotion) {
        this.webViewMotion = webViewMotion;
    }

    public abstract static class WebViewMotion {
//        private Context context;
//        private AdsBean adsBean = null;
//        private RetAdBean retAdBean = null;
//        private ScheduledExecutorService scheduler = null;
//        private ApplyAdBean applyAdBean = null;

        public WebViewMotion() {
//            this.context = context;
//            this.adsBean = adsBean;
//            this.retAdBean = retAdBean;
//            this.applyAdBean = applyAdBean;
        }

        public abstract boolean isNeedConfirm();

        public abstract void onWebViewTouchDown(MotionEvent down);

        public abstract void onWebViewClickedConfirm(MotionEvent down, MotionEvent up);

        public abstract void onWebViewClickedNormal(MotionEvent down, MotionEvent event);
//        {
        // if (AdViewUtils.checkClickPermission(context)
        // && AdViewUtils.checkClickLimitTime(context,
        // adsBean.getSdkType(), adsBean.getIdAd())
        // && AdViewUtils.checkClickLimitNum(adsBean, false)) {
        // // not set s w
        // KyAdBaseView.reportClick(event, applyAdBean, adsBean,
        // retAdBean, scheduler);
        // }
//        }
    }

//    public class MyGesTureLIstener extends SimpleOnGestureListener {
//        private AdsBean adsBean = null;
//
//        public MyGesTureLIstener(AdsBean adsBean) {
//            this.adsBean = adsBean;
//        }
//
//        @Override
//        public boolean onDown(MotionEvent e) {
//            if (null != webViewMotion) {
//                adsBean.setTouchStatus(ACTION_DOWN);
//                webViewMotion.onWebViewTouchDown(e);
//            }
//            return super.onDown(e);
//        }
//
//        @Override
//        public void onShowPress(MotionEvent e) {
//            super.onShowPress(e);
//        }
//
//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
////            if (null != webViewMotion) {
////                adsBean.setTouchStatus(ACTION_TAP);
////                webViewMotion.onWebViewClickedUp(e);
////            }
//            return true;
////            return super.onSingleTapConfirmed(e);
//        }
//    }


    /**************************************************************************
     * JavaScript --> native support
     * <p/>
     * These methods are (indirectly) called by JavaScript code. They provide
     * the means for JavaScript code to talk to native code
     **************************************************************************/

    // /////////////////////////////////////////////////////
    // These are methods in the MRAID API.
    // /////////////////////////////////////////////////////
    private void close() {
        AdViewUtils.logInfo("############## (JS callback:) close ##############");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (state == STATE_LOADING
                        || (state == STATE_DEFAULT && !isInterstitial)
                        || state == STATE_HIDDEN) {
                    // do nothing
                    if (listener != null)
                        listener.mraidViewClose(MRAIDView.this);
                    try {
                        webView.stopLoading();
                        webView.removeAllViews();
                        webView.clearHistory();
                        webView.destroy();
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }

                    return;
                } else if (state == STATE_DEFAULT || state == STATE_EXPANDED) {
                    closeFromExpanded();
                } else if (state == STATE_RESIZED) {
                    closeFromResized();
                }
            }
        });
    }

    @SuppressWarnings("unused")
    private void createCalendarEvent(String eventJSON) {
        AdViewUtils.logInfo("###########  (JS callback :)createCalendarEvent :" + eventJSON + "########");
        if (nativeFeatureListener != null) {
            nativeFeatureListener
                    .mraidNativeFeatureCreateCalendarEvent(eventJSON);
        }
    }

    // Note: This method is also used to present an interstitial ad.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void expand(String url) {
        AdViewUtils.logInfo("##########  (JS callback:) expand "
                + (url != null ? url : "(1-part)") + " ###########");
        try {
            // The only time it is valid to call expand on a banner ad is
            // when the ad is currently in either default or resized state.
            // The only time it is valid to (internally) call expand on an
            // interstitial ad is
            // when the ad is currently in loading state.
            if ((isInterstitial && state != STATE_LOADING)
                    || (!isInterstitial && state != STATE_DEFAULT && state != STATE_RESIZED)) {
                // do nothing
                return;
            }

            // 1-part expansion
            if (TextUtils.isEmpty(url)) {
//            if (isInterstitial || state == STATE_DEFAULT) {
//                if (webView.getParent() != null) {
//                    ((ViewGroup) webView.getParent()).removeView(webView);
//                } else {
//                    removeView(webView);
//                }
//            } else if (state == STATE_RESIZED) {
//                removeResizeView();
//            }
//            expandHelper(webView);
                return;
            }

            // 2-part expansion

            // First, try to get the content of the second (expanded) part of the
            // creative.

            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return;
            }

            // Check to see whether we've been given an absolute or relative URL.
            // If it's relative, prepend the base URL.
            // if (!url.startsWith("http://") && !url.startsWith("https://")) {
            // url = baseUrl + url;
            // }

            final String finalUrl = url;

            // Go onto a background thread to read the content from the URL.
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    final String content = getStringFromUrl(finalUrl);
                    if (!TextUtils.isEmpty(content)) {
                        // Get back onto the main thread to create and load a new
                        // WebView.
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state == STATE_RESIZED) {
                                    removeResizeView();
                                    addView(webView);
                                }
                                webView.setWebChromeClient(null);
                                webView.setWebViewClient(null);
                                webViewPart2 = createWebView();
                                injectMraidJs(webViewPart2);
                                // null baseUrl may cause error
                                webViewPart2.loadDataWithBaseURL(ConstantValues.WEBVIEW_BASEURL, content,
                                        "text/html", "UTF-8", null);
                                currentWebView = webViewPart2;
                                isExpandingPart2 = true;
                                expandHelper(currentWebView);
                            }
                        });
                    } else {
                        AdViewUtils.logInfo("Could not load part 2 expanded content for URL: "
                                + finalUrl);
                        // injectJavaScript("mraid.expand();");
                    }
                }
            }, "2-part-content")).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void open(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
            AdViewUtils.logInfo("################ (JS callback:) open: " + url + "################");
            if (nativeFeatureListener != null) {
                if (url.startsWith("sms")) {
                    nativeFeatureListener.mraidNativeFeatureSendSms(url);
                } else if (url.startsWith("tel")) {
                    nativeFeatureListener.mraidNativeFeatureCallTel(url);
                } else {
                    nativeFeatureListener.mraidNativeFeatureOpenBrowser(url);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void playVideo(String url) {
//        try {
//            url = URLDecoder.decode(url, "UTF-8");
//            AdViewUtils.logInfo("playVideo " + url);
//            if (nativeFeatureListener != null) {
//                nativeFeatureListener.mraidNativeFeaturePlayVideo(false, url);
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    @SuppressWarnings("unused")
    private void resize() {
        AdViewUtils.logInfo("################ (JS callback :) resize ################");

        // We need the cooperation of the app in order to do a resize.
        if (listener == null) {
            return;
        }
        boolean isResizeOK = listener.mraidViewResize(this,
                resizeProperties.width, resizeProperties.height,
                resizeProperties.offsetX, resizeProperties.offsetY);
        if (!isResizeOK) {
            return;
        }

        state = STATE_RESIZED;

        if (resizedView == null) {
            resizedView = new RelativeLayout(context);
            removeAllViews();
            resizedView.addView(webView);
//            addCloseRegion(resizedView);
            FrameLayout rootView = (FrameLayout) getRootView().findViewById(
                    android.R.id.content);
            rootView.addView(resizedView);
        }
        setCloseRegionPosition(resizedView);
        setResizedViewSize();
        setResizedViewPosition();

        handler.post(new Runnable() {
            @Override
            public void run() {
                fireStateChangeEvent();
            }
        });
    }

    @SuppressWarnings("unused")
    private void setOrientationProperties(Map<String, String> properties) {
        boolean allowOrientationChange = Boolean.parseBoolean(properties
                .get("allowOrientationChange"));
        String forceOrientation = properties.get("forceOrientation");
        AdViewUtils.logInfo("########### (JS callback:) setOrientationProperties :" + allowOrientationChange + " "
                + forceOrientation + "##########");
        if (orientationProperties.allowOrientationChange != allowOrientationChange
                || orientationProperties.forceOrientation != MRAIDOrientationProperties
                .forceOrientationFromString(forceOrientation)) {
            orientationProperties.allowOrientationChange = allowOrientationChange;
            orientationProperties.forceOrientation = MRAIDOrientationProperties
                    .forceOrientationFromString(forceOrientation);
            if (isInterstitial || state == STATE_EXPANDED) {
                applyOrientationProperties();
            }
        }
    }

    @SuppressWarnings("unused")
    private void setResizeProperties(Map<String, String> properties) {
        int width = Integer.parseInt(properties.get("width"));
        int height = Integer.parseInt(properties.get("height"));
        int offsetX = Integer.parseInt(properties.get("offsetX"));
        int offsetY = Integer.parseInt(properties.get("offsetY"));
        String customClosePosition = properties.get("customClosePosition");
        boolean allowOffscreen = Boolean.parseBoolean(properties
                .get("allowOffscreen"));
        AdViewUtils.logInfo("############ (JS callback:) setResizeProperties "
                + width + " " + height + " " + offsetX + " " + offsetY + " "
                + customClosePosition + " " + allowOffscreen + "#########");
        resizeProperties.width = width;
        resizeProperties.height = height;
        resizeProperties.offsetX = offsetX;
        resizeProperties.offsetY = offsetY;
        resizeProperties.customClosePosition = MRAIDResizeProperties
                .customClosePositionFromString(customClosePosition);
        resizeProperties.allowOffscreen = allowOffscreen;
    }

    @SuppressWarnings("unused")
    private void storePicture(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
            AdViewUtils.logInfo("########### (JS callback :) storePicture " + url + "##########");
            if (nativeFeatureListener != null) {
                nativeFeatureListener.mraidNativeFeatureStorePicture(url);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void useCustomClose(String useCustomCloseString) {
        AdViewUtils.logInfo("########## (JS callback) useCustomClose:  " + useCustomCloseString + "###########");
        try {
            boolean useCustomClose = Boolean.parseBoolean(useCustomCloseString);
            if (this.useCustomClose != useCustomClose) {
                this.useCustomClose = useCustomClose;
//            if (useCustomClose) {
//                removeDefaultCloseButton();
//            } else {
//                showDefaultCloseButton();
//            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**************************************************************************
     * JavaScript --> native support helpers
     * <p/>
     * These methods are helper methods for the ones above.
     **************************************************************************/

    private String getStringFromUrl(String url) {

        // Support second part from file system - mostly not used on real web
        // creatives
        if (url.startsWith("file:///")) {
            return getStringFromFileUrl(url);
        }

        String content = null;
        InputStream is = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) (new URL(url))
                    .openConnection();
            int responseCode = conn.getResponseCode();
            AdViewUtils.logInfo("response code " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                AdViewUtils.logInfo(
                        "getContentLength " + conn.getContentLength());
                is = conn.getInputStream();
                byte[] buf = new byte[1500];
                int count;
                StringBuilder sb = new StringBuilder();
                while ((count = is.read(buf)) != -1) {
                    String data = new String(buf, 0, count);
                    sb.append(data);
                }
                content = sb.toString();
                AdViewUtils.logInfo("getStringFromUrl ok, length="
                        + content.length());
            }
            conn.disconnect();
        } catch (IOException e) {
            AdViewUtils.logInfo("########## (JS callback:) getStringFromUrl failed :" + e.getLocalizedMessage() + "######");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
        return content;
    }

    private String getStringFromFileUrl(String fileURL) {

        StringBuffer mLine = new StringBuffer("");
        String[] urlElements = fileURL.split("/");
        if (urlElements[3].equals("android_asset")) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(context.getAssets().open(
                                urlElements[4])));

                // do reading, usually loop until end of file reading
                String line = reader.readLine();
                mLine.append(line);
                while (line != null) {
                    line = reader.readLine();
                    mLine.append(line);
                }

                reader.close();
            } catch (IOException e) {
                AdViewUtils.logInfo("Error fetching file: " + e.getMessage());
            }

            return mLine.toString();
        } else {
            AdViewUtils.logInfo("Unknown location to fetch file content");
        }

        return "";
    }

    protected void showAsInterstitial() {
        expand(null);
    }

    private void expandHelper(WebView webView) {
        if (!isInterstitial) {
            state = STATE_EXPANDED;
        }
        // If this MRAIDView is an interstitial, we'll set the state to default
        // and
        // fire the state change event after the view has been laid out.
        applyOrientationProperties();
        forceFullScreen();
        expandedView = new RelativeLayout(context);
        expandedView.addView(webView);
//        addCloseRegion(expandedView);
//        setCloseRegionPosition(expandedView);
        ((Activity) context).addContentView(expandedView,
                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
        isExpandingFromDefault = true;
        if (isInterstitial) {
            isLaidOut = true;
            state = STATE_DEFAULT;
            this.fireStateChangeEvent();
        }
    }

    private void setResizedViewSize() {
        AdViewUtils.logInfo("setResizedViewSize");
        int widthInDip = resizeProperties.width;
        int heightInDip = resizeProperties.height;
        Log.d(AdViewUtils.ADVIEW, "setResizedViewSize " + widthInDip + "x"
                + heightInDip);
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, widthInDip, displayMetrics);
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, heightInDip, displayMetrics);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,
                height);
        resizedView.setLayoutParams(params);
    }

    private void setResizedViewPosition() {
        AdViewUtils.logInfo("setResizedViewPosition");
        // resizedView could be null if it has been closed.
        if (resizedView == null) {
            return;
        }
        int widthInDip = resizeProperties.width;
        int heightInDip = resizeProperties.height;
        int offsetXInDip = resizeProperties.offsetX;
        int offsetYInDip = resizeProperties.offsetY;
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, widthInDip, displayMetrics);
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, heightInDip, displayMetrics);
        int offsetX = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, offsetXInDip, displayMetrics);
        int offsetY = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, offsetYInDip, displayMetrics);
        int x = defaultPosition.left + offsetX;
        int y = defaultPosition.top + offsetY;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) resizedView
                .getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        resizedView.setLayoutParams(params);
        if (x != currentPosition.left || y != currentPosition.top
                || width != currentPosition.width()
                || height != currentPosition.height()) {
            currentPosition.left = x;
            currentPosition.top = y;
            currentPosition.right = x + width;
            currentPosition.bottom = y + height;
            setCurrentPosition();
        }
    }

    private void closeFromExpanded() {
        if (state == STATE_DEFAULT && isInterstitial) {
            state = STATE_HIDDEN;
            clearView();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    fireStateChangeEvent();
                    if (listener != null) {
                        listener.mraidViewClose(MRAIDView.this);
                    }
                }
            });
        } else if (state == STATE_EXPANDED || state == STATE_RESIZED) {
            state = STATE_DEFAULT;
        }
        isClosing = true;

        expandedView.removeAllViews();

        FrameLayout rootView = (FrameLayout) ((Activity) context)
                .findViewById(android.R.id.content);
        rootView.removeView(expandedView);
        expandedView = null;
        closeRegion = null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                restoreOriginalOrientation();
                restoreOriginalScreenState();
            }
        });
        if (webViewPart2 == null) {
            // close from 1-part expansion
            addView(webView);
        } else {
            // close from 2-part expansion
            webViewPart2.setWebChromeClient(null);
            webViewPart2.setWebViewClient(null);
            webViewPart2.destroy();
            webViewPart2 = null;
            webView.setWebChromeClient(mraidWebChromeClient);
            webView.setWebViewClient(mraidWebViewClient);
            currentWebView = webView;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                fireStateChangeEvent();
                if (listener != null) {
                    listener.mraidViewClose(MRAIDView.this);
                }
            }
        });
    }

    private void closeFromResized() {
        state = STATE_DEFAULT;
        isClosing = true;
        removeResizeView();
        addView(webView);
        handler.post(new Runnable() {
            @Override
            public void run() {
                fireStateChangeEvent();
                if (listener != null) {
                    listener.mraidViewClose(MRAIDView.this);
                }
            }
        });
    }

    private void removeResizeView() {
        resizedView.removeAllViews();
        FrameLayout rootView = (FrameLayout) ((Activity) context)
                .findViewById(android.R.id.content);
        rootView.removeView(resizedView);
        resizedView = null;
        closeRegion = null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void forceFullScreen() {
        AdViewUtils.logInfo("forceFullScreen");
        Activity activity = (Activity) context;

        // store away the original state
        int flags = activity.getWindow().getAttributes().flags;
        isFullScreen = ((flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0);
        isForceNotFullScreen = ((flags & WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN) != 0);
        origTitleBarVisibility = -9;

        // First, see if the activity has an action bar.
        boolean hasActionBar = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                hasActionBar = true;
                isActionBarShowing = actionBar.isShowing();
                actionBar.hide();
            }
        }

        // If not, see if the app has a title bar
        if (!hasActionBar) {
            // http://stackoverflow.com/questions/6872376/how-to-hide-the-title-bar-through-code-in-android
            titleBar = null;
            try {
                titleBar = (View) activity.findViewById(android.R.id.title)
                        .getParent();
            } catch (NullPointerException npe) {
                // do nothing
            }
            if (titleBar != null) {
                origTitleBarVisibility = titleBar.getVisibility();
                titleBar.setVisibility(View.GONE);
            }
        }

        AdViewUtils.logInfo("isFullScreen " + isFullScreen);
        AdViewUtils.logInfo("isForceNotFullScreen "
                + isForceNotFullScreen);
        AdViewUtils.logInfo("isActionBarShowing "
                + isActionBarShowing);
        AdViewUtils.logInfo("origTitleBarVisibility "
                + getVisibilityString(origTitleBarVisibility));

        // force fullscreen mode
        ((Activity) context).getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ((Activity) context).getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        isForcingFullScreen = !isFullScreen;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void restoreOriginalScreenState() {
        Activity activity = (Activity) context;
        if (!isFullScreen) {
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (isForceNotFullScreen) {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && isActionBarShowing) {
            ActionBar actionBar = activity.getActionBar();
            actionBar.show();
        } else if (titleBar != null) {
            titleBar.setVisibility(origTitleBarVisibility);
        }
    }

    private static String getVisibilityString(int visibility) {
        switch (visibility) {
            case View.GONE:
                return "GONE";
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.VISIBLE:
                return "VISIBLE";
            default:
                return "UNKNOWN";
        }
    }

//    private void addCloseRegion(View view) {
//        // The input parameter should be either expandedView or resizedView.
//
//        closeRegion = new ImageButton(context);
//        closeRegion.setBackgroundColor(Color.TRANSPARENT);
//        closeRegion.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                close();
//            }
//        });
//
//        // The default close button is shown only on expanded banners and
//        // interstitials,
//        // but not on resized banners.
//        if (view == expandedView && !useCustomClose) {
//            showDefaultCloseButton();
//        }
//
//        ((ViewGroup) view).addView(closeRegion);
//    }
//
//    private void showDefaultCloseButton() {
//        if (closeRegion != null) {
//            Drawable closeButtonNormalDrawable = Assets.getDrawableFromBase64(
//                    getResources(), Assets.new_close);
//            Drawable closeButtonPressedDrawable = Assets.getDrawableFromBase64(
//                    getResources(), Assets.new_close_pressed);
//
//            StateListDrawable states = new StateListDrawable();
//            states.addState(new int[]{-android.R.attr.state_pressed},
//                    closeButtonNormalDrawable);
//            states.addState(new int[]{android.R.attr.state_pressed},
//                    closeButtonPressedDrawable);
//
//            closeRegion.setImageDrawable(states);
//            closeRegion.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        }
//    }
//
//    private void removeDefaultCloseButton() {
//        if (closeRegion != null) {
//            closeRegion.setImageResource(android.R.color.transparent);
//        }
//    }

    private void setCloseRegionPosition(View view) {
        // The input parameter should be either expandedView or resizedView.

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CLOSE_REGION_SIZE, displayMetrics);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                size, size);

        // The close region on expanded banners and interstitials is always in
        // the top right corner.
        // Its position on resized banners is determined by the
        // customClosePosition property of the
        // resizeProperties.
        if (view == expandedView) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (view == resizedView) {

            switch (resizeProperties.customClosePosition) {
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_LEFT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_LEFT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_CENTER:
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_RIGHT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_RIGHT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
            }

            switch (resizeProperties.customClosePosition) {
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_LEFT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_RIGHT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_CENTER:
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_LEFT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_RIGHT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    break;
            }
        }

        closeRegion.setLayoutParams(params);
    }

    /**************************************************************************
     * native --> JavaScript support
     * <p/>
     * These methods provide the means for JavaScript code to talk to native
     * code.
     **************************************************************************/

    @SuppressLint("NewApi")
    private void injectMraidJs(final WebView wv) {
        try {
            //byte[] mraidjsBytes = Base64.decode(ConstantValues.mraidJs3, Base64.DEFAULT);
            //String mraidJs = new String(mraidjsBytes);
            AdViewUtils.logInfo("<<<<< injectMraidJs ok :length = " + js_Mraid.length() + ">>>>>");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                wv.loadDataWithBaseURL(null, "<html></html>", "text/html", "UTF-8", null);

                wv.evaluateJavascript(js_Mraid, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i(AdViewUtils.ADVIEW, value);
                    }
                });
            } else {
                wv.loadUrl("javascript:" + js_Mraid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void injectJavaScript(String js) {
        injectJavaScript(currentWebView, js);
    }

    @SuppressLint("NewApi")
    private void injectJavaScript(WebView webView, String js) {
        try {
            if (!TextUtils.isEmpty(js)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    AdViewUtils.logInfo("evaluating js: " + js);
                    webView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            AdViewUtils.logInfo( "evaluate js complete: " + value);
                        }
                    });
                } else {
                    AdViewUtils.logInfo("loading url: " + js);
                    webView.loadUrl("javascript:" + js);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // convenience methods
    private void fireReadyEvent() {
        AdViewUtils.logInfo("fireReadyEvent");
        injectJavaScript("mraid.fireReadyEvent();");
    }

    // We don't need to explicitly call fireSizeChangeEvent because it's taken
    // care
    // of for us in the mraid.setCurrentPosition method in mraid.js.
    @SuppressLint("DefaultLocale")
    private void fireStateChangeEvent() {
        String[] stateArray = {"loading", "default", "expanded", "resized",
                "hidden"};
        AdViewUtils.logInfo("fireStateChangeEvent "
                + stateArray[state]);
        injectJavaScript("mraid.fireStateChangeEvent('" + stateArray[state]
                + "');");
    }

    private void fireViewableChangeEvent() {
        AdViewUtils.logInfo("fireViewableChangeEvent");
        injectJavaScript("mraid.fireViewableChangeEvent(" + isViewable + ");");
    }

    private int px2dip(int pixels) {
        return pixels * DisplayMetrics.DENSITY_DEFAULT
                / displayMetrics.densityDpi;
        // return pixels;
    }

    private void setCurrentPosition() {
        int x = currentPosition.left;
        int y = currentPosition.top;
        int width = currentPosition.width();
        int height = currentPosition.height();
        AdViewUtils.logInfo("setCurrentPosition [" + x + "," + y
                + "] (" + width + "x" + height + ")");
        injectJavaScript("mraid.setCurrentPosition(" + px2dip(x) + ","
                + px2dip(y) + "," + px2dip(width) + "," + px2dip(height) + ");");
    }

    private void setDefaultPosition() {
        int x = defaultPosition.left;
        int y = defaultPosition.top;
        int width = defaultPosition.width();
        int height = defaultPosition.height();
        AdViewUtils.logInfo("setDefaultPosition [" + x + "," + y
                + "] (" + width + "x" + height + ")");
        injectJavaScript("mraid.setDefaultPosition(" + px2dip(x) + ","
                + px2dip(y) + "," + px2dip(width) + "," + px2dip(height) + ");");
    }

    private void setMaxSize() {
        AdViewUtils.logInfo("setMaxSize");
        int width = maxSize.width;
        int height = maxSize.height;
        AdViewUtils.logInfo("setMaxSize " + width + "x" + height);
        injectJavaScript("mraid.setMaxSize(" + px2dip(width) + ","
                + px2dip(height) + ");");
    }

    private void setScreenSize() {
        AdViewUtils.logInfo("setScreenSize");
        int width = screenSize.width;
        int height = screenSize.height;
        AdViewUtils.logInfo("setScreenSize " + width + "x" + height);
        injectJavaScript("mraid.setScreenSize(" + px2dip(width) + ","
                + px2dip(height) + ");");
    }

    private void setSupportedServices() {
        AdViewUtils.logInfo("setSupportedServices");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.CALENDAR, "
                + nativeFeatureManager.isCalendarSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.INLINEVIDEO, "
                + nativeFeatureManager.isInlineVideoSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.SMS, "
                + nativeFeatureManager.isSmsSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.STOREPICTURE, "
                + nativeFeatureManager.isStorePictureSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.TEL, "
                + nativeFeatureManager.isTelSupported() + ");");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void pauseWebView(WebView webView) {
        AdViewUtils.logInfo("pauseWebView " + webView.toString());
        // Stop any video/animation that may be running in the WebView.
        // Otherwise, it will keep playing in the background.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // webView.onPause();
                webView.getClass().getMethod("onPause")
                        .invoke(webView, (Object[]) null);
                // webView.pauseTimers();
            } else {
                webView.loadUrl("about:blank");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void resumeWebView(WebView webView) {
        AdViewUtils.logInfo("resumeWebView " + webView.toString());
        // Stop any video/animation that may be running in the WebView.
        // Otherwise, it will keep playing in the background.
        try {
            webView.reload();
            // webView.getClass().getMethod("onResume")
            // .invoke(webView, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**************************************************************************
     * WebChromeClient and WebViewClient
     **************************************************************************/

    private class MRAIDWebChromeClient extends WebChromeClient {

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            if (cm == null || cm.message() == null) {
                return false;
            }
            if (!cm.message().contains("Uncaught ReferenceError")) {
                AdViewUtils.logInfo("########## (JS callback :) " + cm.message()
                        + (cm.sourceId() == null ? "" : " at " + cm.sourceId())
                        + ":" + cm.lineNumber() + "#######");
            }
            return true;
        }


        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 JsResult result) {
            AdViewUtils.logInfo("JS alert " + message);
            return handlePopups(result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   JsResult result) {
            AdViewUtils.logInfo("JS confirm " + message);
            return handlePopups(result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, JsPromptResult result) {
            AdViewUtils.logInfo("JS prompt" + message);
            return handlePopups(result);
        }

        private boolean isRun;

        @Override
        public void onProgressChanged(final WebView view, final int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100 && !isRun) {
                isRun = true;
                //view.buildDrawingCache();
                view.destroyDrawingCache(); //wilder 2019, if redirec, it should clear it first ,and then detect it .
                if (null != view && view.isShown()) {
                    try {
                        Timer timer = new Timer();
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    if (null != view && view.isShown()) {
                                        boolean valid = true;
                                        //view.destroyDrawingCache(); //wilder 2019, if redirec, it should clear it first ,and then detect it .
                                        view.buildDrawingCache(); //rebuild it again
                                        final Bitmap bitmap = view.getDrawingCache(true);
                                        if (null != bitmap && !bitmap.isRecycled()) {
                                            if (null != bitmap && !bitmap.isRecycled()) {
                                                valid = isVaildView(getColorList(bitmap, getPixLocation(bitmap.getWidth(), bitmap.getHeight())));
                                            }
                                            if (!valid && null != listener) {
                                                listener.loadDataError(ConstantValues.LOADERROR_BLANK);//1==自动跳转,2==白条
                                            }
                                            if (!valid && AdViewUtils.htmlUseBlankErrorPage) {
                                                //view.destroyDrawingCache();
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //load url must handled in main loop thread
                                                        view.loadUrl(AdViewUtils.htmlErrorPage);
                                                        //view.loadUrl("about:blank");
                                                    }
                                                });
                                            }
                                      //view.destroyDrawingCache();
//                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            android.widget.ImageView tempIv = ((Activity) view.getContext()).findViewById(123456);
//                                            if (tempIv == null) {
//                                                tempIv = new android.widget.ImageView(webView.getContext());
//                                                tempIv.setId(123456);
//                                                ((Activity) view.getContext()).addContentView(tempIv, new ViewGroup.LayoutParams(-1, -1));
//                                            }
//                                            tempIv.setImageBitmap(bitmap);
//                                        }
//                                    });
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        timer.schedule(timerTask, 5500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        private ArrayList getPixLocation(int w, int h) {
            ArrayList location = null;
            try {
                if (w <= 0 || h <= 0)
                    return null;
                location = new ArrayList();
                int arrSize = 1;
                /* 8 x 8*/
                int[] locationX = new int[]{
                        (int) (w * 1/8 - arrSize / 2 - 2),
                        (int) (w * 2/8 - arrSize / 2 + 2),
                        (int) (w * 3/8 - arrSize / 2 - 2),
                        (int) (w * 4/8 - arrSize / 2 + 2),
                        (int) (w * 5/8 - arrSize / 2 + 1),
                        (int) (w * 6/8 - arrSize / 2 - 2),
                        (int) (w * 7/8 - arrSize / 2 + 2),
                        (int) (w * 8/8 - 6)};
                int[] locationY = new int[]{
                        (int) (h * 1/8 - arrSize / 2),
                        (int) (h * 2/8 - arrSize / 2),
                        (int) (h * 3/8 - arrSize / 2),
                        (int) (h * 4/8 - arrSize / 2),
                        (int) (h * 5/8 - arrSize / 2),
                        (int) (h * 6/8 - arrSize / 2),
                        (int) (h * 7/8 - arrSize / 2),
                        (int) (h * 8/8 - 7),
                };
                /*6 x 6*/
//                int[] locationX = new int[]{
//                        (int) (w * 1/6 - arrSize / 2 - 2),
//                        (int) (w * 2/6 - arrSize / 2 + 2),
//                        (int) (w * 3/6 - arrSize / 2 - 2),
//                        (int) (w * 4/6 - arrSize / 2 + 2),
//                        (int) (w * 5/6 - arrSize / 2 - 1),
//                        (int) (w * 6/6 - 2)};
//                int[] locationY = new int[]{
//                        (int) (h * 1/6 - arrSize / 2),
//                        (int) (h * 2/6 - arrSize / 2),
//                        (int) (h * 3/6 - arrSize / 2),
//                        (int) (h * 4/6 - arrSize / 2),
//                        (int) (h * 5/6 - arrSize / 2),
//                        (int) (h * 6/6 - 2),
//                };

                int[] pixesLocationX = new int[locationX.length * arrSize * arrSize];
                int[] pixesLocationY = new int[locationX.length * arrSize * arrSize];
                for (int k = 0; k < locationX.length; k++) {
                    for (int i = 0; i < arrSize; i++) {
                        for (int j = 0; j < arrSize; j++) {
                            pixesLocationX[(arrSize * arrSize) * k + i * arrSize + j] = locationX[k] + i;
                            pixesLocationY[(arrSize * arrSize) * k + i * arrSize + j] = locationY[k] + j;
                        }
                    }
                }
                location.add(pixesLocationX);
                location.add(pixesLocationY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return location;
        }

        private ArrayList getColorList(Bitmap bitmap, ArrayList location) {
            ArrayList<Integer> colorList = null;
            try {
                if (bitmap == null || location == null || location.size() <= 0)
                    return null;
                colorList = new ArrayList();
                int[] locationXs = (int[]) location.get(0);
                int[] locationYs = (int[]) location.get(1);

                for (int i = 0; i < locationXs.length; i++) {
                    for (int j = 0; j < locationYs.length; j++) {
                        try {
                            if (bitmap.isRecycled())
                                break;
                            int tempColor = bitmap.getPixel(locationXs[i], locationYs[j]);
//                            Log.i("colors", "color is :" + tempColor + " locationX =" + locationXs[i] + " locationY=" + locationYs[j]);
                            colorList.add(tempColor);
//                            bitmap.setPixel(locationXs[i], locationYs[j], Color.RED);
                        } catch (Exception e) {
                            AdViewUtils.logError("getPixel failed", e);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return colorList;
        }

        private boolean isVaildView(ArrayList<Integer> colorList) {
            int tempColor = 0;
            try {
                if (null == colorList || colorList.size() <= 0)
                    return false;

                for (int i = 0; i < colorList.size(); i++) {
                    if (colorList.get(i) == 0)
                        continue;
                    else {
                        if (tempColor == 0) {
                            tempColor = colorList.get(i);
                            continue;
                        }
                        if (colorList.get(i) != tempColor)
                            return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return false;
        }

        private boolean handlePopups(JsResult result) {
            /*
             * if (NexageAdManager.areJavascriptPromptsAndAlertsAllowed()) {
             * NexageAdManager.setIsPopupDisplayed(true); return false; } else {
             *
             * result.cancel(); return true; }
             */
            result.cancel();
            return true;
        }

    }

    private class MRAIDWebViewClient extends WebViewClient {


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (state == STATE_LOADING) {
                isPageFinished = true;

//                injectMraidJs(view);
                injectJavaScript("mraid.getVersion();");

//                injectJavaScript("mraid.open(\"http://www.nexage.com\");");

                injectJavaScript("mraid.setPlacementType('"
                        + (isInterstitial ? "interstitial" : "inline") + "');");
                setSupportedServices();
                if (isLaidOut) {
                    setScreenSize();
                    setMaxSize();
                    setCurrentPosition();
                    setDefaultPosition();
                    if (isInterstitial) {
                        showAsInterstitial();
                    } else {
                        state = STATE_DEFAULT;
                        fireStateChangeEvent();
                        fireReadyEvent();
                        if (isViewable) {
                            fireViewableChangeEvent();
                        }
                    }
                }
                if (listener != null) {
                    listener.mraidViewLoaded(MRAIDView.this);
                }
            }
            if (isExpandingPart2) {
                isExpandingPart2 = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        injectJavaScript("mraid.setPlacementType('"
                                + (isInterstitial ? "interstitial" : "inline")
                                + "');");
                        setSupportedServices();
                        setScreenSize();
                        setDefaultPosition();
                        AdViewUtils.logInfo(
                                "calling fireStateChangeEvent 2");
                        fireStateChangeEvent();
                        fireReadyEvent();
                        if (isViewable) {
                            fireViewableChangeEvent();
                        }
                    }
                });
            }

            //(wilder 2019)
            /*
            int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            // 重新测量
            view.measure(w, h);
            */
            //end wilder
            webView.invalidate();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                if (null != listener)
                    return listener.onShouldIntercept(url);
            }

            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            AdViewUtils.logInfo("shouldOverrideUrlLoading: " + url);
            if (url.startsWith("mraid://")) {
                parseCommandUrl(url, nativeFeatureListener);
                return true;
            } else {
                if (null != listener)
                    listener.onShouldOverride(url);
                return true;
            }
        }

        //wilder 2019
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
            //fix https can not access
            //super.onReceivedSslError(view, handler, error); wilder 2019 must remove this line.
            AdViewUtils.logInfo("------------ [MRAIDWebViewClient] onReceivedSslError : " + error.toString() + "------------");
            if(error.getPrimaryError() == android.net.http.SslError.SSL_INVALID
                    || error.getPrimaryError() == android.net.http.SslError.SSL_UNTRUSTED ){
                // 校验过程遇到了bug
                handler.proceed();
            }else{
                handler.cancel();
            }

//            final AlertDialog.Builder builder = new AlertDialog.Builder(WechatLoginActivity.this);
//            builder.setMessage(R.string.notification_error_ssl_cert_invalid);
//            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    handler.proceed();
//                }
//            });
//            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    handler.cancel();
//                }
//            });
//            final AlertDialog dialog = builder.create();
//            dialog.show();

        }



    }


    // This is the entry point to all the "actual" MRAID methods below.
    protected void parseCommandUrl(String commandUrl, MRAIDNativeFeatureListener nativeFeatureListener) {
        AdViewUtils.logInfo("parseCommandUrl " + commandUrl);

        MRAIDParser parser = new MRAIDParser();
        Map<String, String> commandMap = parser.parseCommandUrl(commandUrl);

        String command = commandMap.get("command");

        final String[] commandsWithNoParam = {"close", "resize",};

        final String[] commandsWithString = {"createCalendarEvent", "expand",
                "open", "playVideo", "storePicture", "useCustomClose",};

        final String[] commandsWithMap = {"setOrientationProperties",
                "setResizeProperties",};

        final String[] commandsWithCustom = {"volumeON", "volumeOFF",
                "onPlayStarted", "onPlayEnded", "action-web", "action-app",
                "action-call", "action-sms", "action-play", "action-replay", "action-undefined", "openDeeplink"};

        try {
            if (Arrays.asList(commandsWithNoParam).contains(command)) {
                Method method = getClass().getDeclaredMethod(command);
                method.invoke(this);
            } else if (Arrays.asList(commandsWithString).contains(command)) {
                Method method = getClass().getDeclaredMethod(command,
                        String.class);
                String key;
                if (command.equals("createCalendarEvent")) {
                    key = "eventJSON";
                } else if (command.equals("useCustomClose")) {
                    key = "useCustomClose";
                } else {
                    key = "url";
                }
                // some fix by mc
                String val = commandMap.get(key);
                MRAIDNativeFeatureProvider featureProvider = new MRAIDNativeFeatureProvider(getContext(), new MRAIDNativeFeatureManager(getContext()));
                if (command.equals("open")) {
                    method.invoke(this, val);
                } else if (command.equals("expand")) {
                    method.invoke(this, val);
                } else if (command.equals("playVideo")) {
                    featureProvider.playVideo(val);
                } else if (command.equals("storePicture")) {
                    featureProvider.storePicture(val);
                } else if (command.equals("useCustomClose")) {
                    method.invoke(this, val);
                } else if (command.equals("createCalendarEvent")) {
                    featureProvider.createCalendarEvent(val);
                }
            } else if (Arrays.asList(commandsWithMap).contains(command)) {
                Method method = getClass()
                        .getDeclaredMethod(command, Map.class);
                method.invoke(this, commandMap);
            } else if (Arrays.asList(commandsWithCustom).contains(command)) {
                String val = commandMap.get(command);
                AudioManager mAudioManager = (AudioManager) getContext()
                        .getSystemService(Context.AUDIO_SERVICE);
                if (command.equals("volumeON")) {
                    // 最大音量一半
                    int maxVolume = mAudioManager
                            .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            maxVolume / 2, 0);
                } else if (command.equals("volumeOFF")) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
                            0);
                } else if (command.equals("action-app")) {
                    if (null != nativeFeatureListener)
                        nativeFeatureListener.mraidNativeFeatureDownload(val);
                    // method.invoke(this, val);
                } else if (command.equals("action-call")) {
                    if (null != nativeFeatureListener)
                        nativeFeatureListener.mraidNativeFeatureCallTel(val);
                    // method.invoke(this, val);
                } else if (command.equals("action-sms")) {
                    if (null != nativeFeatureListener)
                        nativeFeatureListener.mraidNativeFeatureSendSms(val);
                    // method.invoke(this, val);
                } else if (command.equals("action-web")) {
                    if (null != nativeFeatureListener)
                        nativeFeatureListener.mraidNativeFeatureOpenBrowser(val);
                    // method.invoke(this, val);
                    // featureProvider.openBrowser(val);
                    // AdViewUtils.openWebBrowser(context, adsBean)
                } else if (command.equals("openDeeplink")) {
                    if (null != nativeFeatureListener)
                        nativeFeatureListener.mraidNativeFeatureOpenBrowser(val);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**************************************************************************
     * Methods for responding to changes of size and position.
     **************************************************************************/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AdViewUtils.logInfo("onConfigurationChanged "
                + (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait"
                : "landscape"));
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
    }

    @Override
    protected void onAttachedToWindow() {
        AdViewUtils.logInfo("onAttachedToWindow");
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        AdViewUtils.logInfo("onDetachedFromWindow");
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        AdViewUtils.logInfo("onVisibilityChanged "
                + getVisibilityString(visibility));
        setViewable(visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        int actualVisibility = getVisibility();
        AdViewUtils.logInfo("onWindowVisibilityChanged " + getVisibilityString(visibility)
                                + " (actual "  + getVisibilityString(actualVisibility) + ")");
        if (visibility != View.GONE) { //wilder 2019 changed for exception
            setViewable(actualVisibility);
        }
    }

    private void setViewable(int visibility) {
        boolean isCurrentlyViewable = (visibility == View.VISIBLE);
        if (isCurrentlyViewable != isViewable) {
            isViewable = isCurrentlyViewable;
            if (isPageFinished && isLaidOut) {
                fireViewableChangeEvent();
            }
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        AdViewUtils.logInfo("onLayout (" + state + ") " + changed
                + " " + left + " " + top + " " + right + " " + bottom);
        if (isForcingFullScreen) {
            AdViewUtils.logInfo("onLayout ignored");
            return;
        }
        if (state == STATE_EXPANDED || state == STATE_RESIZED) {
            calculateScreenSize();
            calculateMaxSize();
        }
        if (isClosing) {
            isClosing = false;
            currentPosition = new Rect(defaultPosition);
            setCurrentPosition();
        } else {
            calculatePosition(false);
        }
        if (state == STATE_RESIZED && changed) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setResizedViewPosition();
                }
            });
        }
        isLaidOut = true;
        if (state == STATE_LOADING && isPageFinished && !isInterstitial) {
            state = STATE_DEFAULT;
            fireStateChangeEvent();
            fireReadyEvent();
            if (isViewable) {
                fireViewableChangeEvent();
            }
        }
    }

    private void onLayoutWebView(WebView wv, boolean changed, int left,
                                 int top, int right, int bottom) {
        boolean isCurrent = (wv == currentWebView);
        AdViewUtils.logInfo("onLayoutWebView "
                + (wv == webView ? "1 " : "2 ") + isCurrent + " (" + state
                + ") " + changed + " " + left + " " + top + " " + right + " "
                + bottom);
        if (!isCurrent) {
            AdViewUtils.logInfo(
                    "onLayoutWebView ignored, not current");
            return;
        }
        if (isForcingFullScreen) {
            AdViewUtils.logInfo(
                    "onLayoutWebView ignored, isForcingFullScreen");
            isForcingFullScreen = false;
            return;
        }
        if (state == STATE_LOADING || state == STATE_DEFAULT) {
            calculateScreenSize();
            calculateMaxSize();
        }

        // If closing from expanded state, just set currentPosition to default
        // position in onLayout above.
        if (!isClosing) {
            calculatePosition(true);
            if (isInterstitial) {
                // For interstitials, the default position is always the current
                // position
                if (!defaultPosition.equals(currentPosition)) {
                    defaultPosition = new Rect(currentPosition);
                    setDefaultPosition();
                }
            }
        }

        if (isExpandingFromDefault) {
            isExpandingFromDefault = false;
            if (isInterstitial) {
                state = STATE_DEFAULT;
                isLaidOut = true;
            }
            if (!isExpandingPart2) {
                AdViewUtils.logInfo("calling fireStateChangeEvent 1");
                fireStateChangeEvent();
            }
            if (isInterstitial) {
                fireReadyEvent();
                if (isViewable) {
                    fireViewableChangeEvent();
                }
            }
            if (listener != null) {
                listener.mraidViewExpand(this);
            }
        }
    }

    private void calculateScreenSize() {
        try {
            int orientation = getResources().getConfiguration().orientation;
            boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);
            AdViewUtils.logInfo("calculateScreenSize orientation "
                    + (isPortrait ? "portrait" : "landscape"));
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            density = displayMetrics.density;
            AdViewUtils.logInfo("calculateScreenSize screen size "
                    + width + "x" + height);
            if (width != screenSize.width || height != screenSize.height) {
                screenSize.width = width;
                screenSize.height = height;
                // if (isPageFinished) {
                setScreenSize();
                // }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void calculateMaxSize() {
        int width, height;
        Rect frame = new Rect();
        Window window = ((Activity) context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(frame);
        AdViewUtils.logInfo("calculateMaxSize frame [" + frame.left
                + "," + frame.top + "][" + frame.right + "," + frame.bottom
                + "] (" + frame.width() + "x" + frame.height() + ")");
        int statusHeight = frame.top;
        contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
                .getTop();
        int titleHeight = contentViewTop - statusHeight;
        AdViewUtils.logInfo("calculateMaxSize statusHeight "
                + statusHeight);
        AdViewUtils.logInfo("calculateMaxSize titleHeight "
                + titleHeight);
        AdViewUtils.logInfo("calculateMaxSize contentViewTop "
                + contentViewTop);
        width = frame.width();
        height = screenSize.height - contentViewTop;
        AdViewUtils.logInfo("calculateMaxSize max size " + width
                + "x" + height);
        if (width != maxSize.width || height != maxSize.height) {
            maxSize.width = width;
            maxSize.height = height;
            // if (isPageFinished) {
            setMaxSize();
            // }
        }
    }

    private void calculatePosition(boolean isCurrentWebView) {
        int x, y, width, height;
        int[] location = new int[2];

        View view = isCurrentWebView ? currentWebView : this;
        String name = (isCurrentWebView ? "current" : "default");

        // This is the default location regardless of the state of the
        // MRAIDView.
        view.getLocationOnScreen(location);
        x = location[0];
        y = location[1];
        AdViewUtils.logInfo("calculatePosition " + name
                + " locationOnScreen [" + x + "," + y + "]");
        AdViewUtils.logInfo("calculatePosition " + name
                + " contentViewTop " + contentViewTop);
        y -= contentViewTop;
        width = view.getWidth();
        height = view.getHeight();

        AdViewUtils.logInfo("calculatePosition " + name
                + " position [" + x + "," + y + "] (" + width + "x" + height
                + ")");

        Rect position = isCurrentWebView ? currentPosition : defaultPosition;

        if (x != position.left || y != position.top
                || width != position.width() || height != position.height()) {
            if (isCurrentWebView) {
                currentPosition = new Rect(x, y, x + width, y + height);
            } else {
                defaultPosition = new Rect(x, y, x + width, y + height);
            }
            if (isPageFinished) {
                if (isCurrentWebView) {
                    setCurrentPosition();
                } else {
                    setDefaultPosition();
                }
            }
        }
    }

    /**************************************************************************
     * Methods for forcing orientation.
     **************************************************************************/

    private static String getOrientationString(int orientation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                return "UNSPECIFIED";
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return "LANDSCAPE";
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return "PORTRAIT";
            default:
                return "UNKNOWN";
        }
    }

    private void applyOrientationProperties() {
        AdViewUtils.logInfo("applyOrientationProperties "
                + orientationProperties.allowOrientationChange + " "
                + orientationProperties.forceOrientationString());

        Activity activity = (Activity) context;
        try {
            int currentOrientation = getResources().getConfiguration().orientation;
            boolean isCurrentPortrait = (currentOrientation == Configuration.ORIENTATION_PORTRAIT);
            AdViewUtils.logInfo("currentOrientation "
                    + (isCurrentPortrait ? "portrait" : "landscape"));

            int orientation = originalRequestedOrientation;
            if (orientationProperties.forceOrientation == MRAIDOrientationProperties.FORCE_ORIENTATION_PORTRAIT) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientationProperties.forceOrientation == MRAIDOrientationProperties.FORCE_ORIENTATION_LANDSCAPE) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else {
                // orientationProperties.forceOrientation ==
                // MRAIDOrientationProperties.FORCE_ORIENTATION_NONE
                if (orientationProperties.allowOrientationChange) {
                    // orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                    // orientation = orientation;
                } else {
                    // orientationProperties.allowOrientationChange == false
                    // lock the current orientation
                    orientation = (isCurrentPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
            activity.setRequestedOrientation(orientation);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreOriginalOrientation() {
        AdViewUtils.logInfo("restoreOriginalOrientation");
        Activity activity = (Activity) context;
        int currentRequestedOrientation = activity.getRequestedOrientation();
        if (currentRequestedOrientation != originalRequestedOrientation) {
            activity.setRequestedOrientation(originalRequestedOrientation);
        }
    }

    public WebView getMraidWebView() {
        return webView;
    }
}
