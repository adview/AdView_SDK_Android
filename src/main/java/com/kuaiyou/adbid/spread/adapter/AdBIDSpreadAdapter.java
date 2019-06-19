package com.kuaiyou.adbid.spread.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.adbid.AdSpreadBIDView;
import com.kuaiyou.interfaces.KySpreadListener;
import com.kuaiyou.interfaces.KyViewListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.mraid.interfaces.MRAIDNativeFeatureListener;
import com.kuaiyou.mraid.interfaces.MRAIDViewListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.InstlView;
import com.kuaiyou.utils.SpreadView;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;

public class AdBIDSpreadAdapter extends AdAdapterManager implements KySpreadListener, MRAIDViewListener, MRAIDNativeFeatureListener {

    private SpreadHandler handler;
    private KySpreadListener kyViewListener;
    private String bitmapPath;
    private Context context;

    private int screenWidth, screenHeight;
    private int adWidth, adHeight;

    private double density;
    private Rect touchRect;

    private boolean isDisplayed = false;

    static boolean isPageDone = false; //wilder 2019
    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdBIDSpreadAdapter");
        this.context = context;
    }


    @Override
    public void handleAd(Context context, Bundle bundle) {
        screenWidth = bundle.getInt("screenWidth");
        screenHeight = bundle.getInt("screenHeight");
        adWidth = bundle.getIntArray("adSize")[0];
        adHeight = bundle.getIntArray("adSize")[1];
        density = bundle.getDouble("density");
        kyViewListener = (KySpreadListener) bundle.getSerializable("interface");
        handler = new SpreadHandler(this);
        new Thread(new InitRunnable()).start();
    }

    public static class SpreadHandler extends Handler {
        private SoftReference<AdBIDSpreadAdapter> adSpreadBIDViewReference = null;
        private boolean hasClosed = false;
        private AdsBean adsBean;

        public SpreadHandler(AdBIDSpreadAdapter adSpreadBIDView) {
            super(Looper.getMainLooper());
            adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>(
                    adSpreadBIDView);
            adsBean = adSpreadBIDViewReference.get().kyViewListener.getAdsBean();
            adSpreadBIDViewReference.get().kyViewListener.getSpreadView().setSpreadViewListener(adSpreadBIDView);
            adSpreadBIDViewReference.get().kyViewListener.getSpreadView().init();
        }

       /**
         * 通知收到广告接口，并设置按钮背景颜色
         */
        private void notifyRecievedInterface(Message msg, boolean isHTML) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();

                //wilder 2019 fixed for web content loader will slow and may counter down faster
                if (isHTML) {
                    isPageDone = false; //wilder 2019 for start counter in page done, see bellow.
                }
                else {
                    notifyCountDown(adsBean.getRuleTime() + adsBean.getDelayTime(), adSpreadBIDView.kyViewListener.getNotifyType());
                }

                setNotifyBackground((TextView) getViewById(ConstantValues.SPREADADCOUNTER, msg));
                if (null != adSpreadBIDView.kyViewListener) {
                    adSpreadBIDView.kyViewListener.onReceived(null, false);
                }
//                if (null != onAdSpreadListener)
//                    adSpreadBIDView.onAdSpreadListener
//                            .onAdRecieved(adSpreadBIDView);
//                if (null != adSpreadBIDView.spreadAdListener)
//                    adSpreadBIDView.spreadAdListener.onReceivedAd(adSpreadBIDView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setNotifyBackground(TextView adCounter) {
            if (null == adCounter)
                return;
            int[] screenSize = AdViewUtils.getWidthAndHeight(adCounter.getContext(), false, true);
            // 设置跳过按钮背景颜色
            int fillColor = Color.parseColor("#bb404040");
            float connerRadii[] = new float[]{screenSize[0] / 36,
                    screenSize[0] / 36, screenSize[0] / 36, screenSize[0] / 36,
                    screenSize[0] / 36, screenSize[0] / 36, screenSize[0] / 36,
                    screenSize[0] / 36};
            GradientDrawable roundDrawable = new GradientDrawable();
            roundDrawable.setColor(fillColor);
            roundDrawable.setCornerRadii(connerRadii);
            adCounter.setBackground(roundDrawable);
        }

        /**
         * 设置广告文本，并延时通知倒计时handler
         */
        private void notifyReady(Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                removeMessages(ConstantValues.STRICT);
                removeMessages(ConstantValues.DELAY);
                Message msg2 = new Message();
                msg2.what = ConstantValues.STRICT;
                msg2.arg1 = adSpreadBIDView.kyViewListener.getNotifyType();
                sendMessageDelayed(msg2,
                        adsBean.getRuleTime() * 1000l);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * notify the counter
         */
        public void notifyCountDown(int count, int type) {
            switch (type) {
                case AdSpreadBIDView.NOTIFY_COUNTER_NULL:
                    break;
                case AdSpreadBIDView.NOTIFY_COUNTER_NUM:
                case AdSpreadBIDView.NOTIFY_COUNTER_TEXT:
                case AdSpreadBIDView.NOTIFY_COUNTER_CUSTOM:
                    Message msg = new Message();
                    msg.what = ConstantValues.COUNTDOWN;
                    msg.arg1 = count;
                    msg.arg2 = type;
                    sendMessage(msg);
                    break;
            }
        }

        /**
         * 通知开屏展示汇报/接口
         */
        private void notifyImpressionInterface(Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                adSpreadBIDView.kyViewListener.onDisplay(null, false);
//                if (null != adSpreadBIDView.onAdSpreadListener)
//                    adSpreadBIDView.onAdSpreadListener
//                            .onAdDisplayed(adSpreadBIDView);
//                if (null != adSpreadBIDView.spreadAdListener)
//                    adSpreadBIDView.spreadAdListener
//                            .onDisplayed(adSpreadBIDView);
//                reportImpression(adsBean,
//                        adSpreadBIDView.retAdBean, adSpreadBIDView.applyAdBean, true);
                adSpreadBIDView.isDisplayed = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏用户手动取消/接口
         */
        private void notifyUserCancelInterface(Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                WebView webView = (WebView) getViewById(ConstantValues.WEBVIEWID, msg);
                try {
                    if (null != webView) {
                        webView.getClass().getMethod("onPause").invoke(webView, (Object[]) null);
                        ViewGroup vp = (ViewGroup) webView.getParent();
                        vp.removeView(webView);
                        webView.removeAllViews();
                        webView.destroy();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                removeMessages(ConstantValues.STRICT);
                removeMessages(ConstantValues.DELAY);
                adSpreadBIDView.kyViewListener.onCloseBtnClicked();
//                if (null != adSpreadBIDView.onAdSpreadListener)
//                    adSpreadBIDView.onAdSpreadListener.onAdClosedByUser();
//                if (null != adSpreadBIDView.spreadAdListener)
//                    adSpreadBIDView.spreadAdListener
//                            .onAdClosedByUser(adSpreadBIDView);
//                adSpreadBIDView.onAdSpreadListener = null;
//                adSpreadBIDView.spreadAdListener = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏失败/接口
         */
        private void notifyFaildedIntetface(String mg, Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                adSpreadBIDView.kyViewListener.onAdFailed(null, mg, false);
//                if (null != adSpreadBIDView.spreadAdListener)
//                    adSpreadBIDView.spreadAdListener.onConnectFailed(
//                            adSpreadBIDView, mg);
//                if (null != adSpreadBIDView.onAdSpreadListener)
//                    adSpreadBIDView.onAdSpreadListener.onAdRecieveFailed(
//                            adSpreadBIDView, mg);
//                adSpreadBIDView.onAdSpreadListener = null;
//                adSpreadBIDView.spreadAdListener = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏倒计时/接口
         */
        private void notifyCustomCountDownInterface(Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                adSpreadBIDView.kyViewListener.onAdNotifyCustomCallback((RelativeLayout) getViewById(ConstantValues.SPREADNOTIFYLAYOUT, msg),
                        adsBean.getRuleTime(),
                        adsBean.getDelayTime());
//                if (null != adSpreadBIDView.spreadAdListener)
//                    adSpreadBIDView.spreadAdListener
//                            .onAdNotifyCustomCallback(
//                                    (RelativeLayout) getViewById(ConstantValues.SPREADNOTIFYLAYOUT, msg),
//                                    adsBean.getRuleTime(),
//                                    adsBean.getDelayTime());
//                if (null != adSpreadBIDView.onAdSpreadListener)
//                    adSpreadBIDView.onAdSpreadListener
//                            .onAdNotifyCustomCallback(
//                                    (RelativeLayout) getViewById(ConstantValues.SPREADNOTIFYLAYOUT, msg),
//                                    adsBean.getRuleTime(),
//                                    adsBean.getDelayTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏关闭/接口
         */
        private void notifyCloseInterface(Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                adSpreadBIDView.kyViewListener.onCloseBtnClicked();
//                if (null != adSpreadBIDView.onAdSpreadListener)
//                    adSpreadBIDView.onAdSpreadListener.onAdSpreadPrepareClosed();
//                if (null != adSpreadBIDView.spreadAdListener)
//                    adSpreadBIDView.spreadAdListener.onAdClose(adSpreadBIDView);
//                adSpreadBIDView.onAdSpreadListener = null;
//                adSpreadBIDView.spreadAdListener = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * find View by Id
         *
         * @param id view's id
         * @return view
         */
        private View getViewById(int id, Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                return adSpreadBIDView.kyViewListener.getSpreadView().findViewById(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void configAdData(AdsBean adsBean, int width, int height, Message msg) {
            try {
                if (null == adSpreadBIDViewReference.get()) {
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                }
                adSpreadBIDViewReference.get().kyViewListener.getSpreadView().loadAdLayout(
                        width, height,
                        adsBean.getAdType(),
                        KyAdBaseView.getAdLayoutType(adSpreadBIDViewReference.get().context,
                                                    adsBean.getVat(), height,
                                                    TextUtils.isEmpty(adsBean.getAdText()) ? 0 : 1,
                                                    adSpreadBIDViewReference.get().isHasSpreadLogo()),
                        adsBean.getDeformationMode(),
                        adSpreadBIDViewReference.get().isHasSpreadLogo(),
                        adSpreadBIDViewReference.get(),
                        adSpreadBIDViewReference.get());

                adSpreadBIDViewReference.get().kyViewListener.getSpreadView().setContent(adsBean, adSpreadBIDViewReference.get().bitmapPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            if (null == adSpreadBIDViewReference.get()) {
                adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
            }
            final AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
            if (null == adSpreadBIDView) {
                notifyFaildedIntetface("object is null", msg);
                return;
            }
            InstlView.CenterTextView adCounter;

            switch (msg.what) {
                // 通知倒计时功能
                case ConstantValues.COUNTDOWN:
                    adCounter = (InstlView.CenterTextView) getViewById(ConstantValues.SPREADADCOUNTER, msg);
                    switch (msg.arg2) {
                        // 倒计时
                        case AdSpreadBIDView.NOTIFY_COUNTER_NUM:
                            if (1 <= msg.arg1) {
                                if (null != adCounter) {
                                    adCounter.text = (msg.arg1 + "s | 跳过");
                                    adCounter.invalidate();
                                }
                                Message mg = new Message();
                                mg.what = ConstantValues.COUNTDOWN;
                                mg.arg1 = msg.arg1 - 1;
                                mg.arg2 = AdSpreadBIDView.NOTIFY_COUNTER_NUM;
                                sendMessageDelayed(mg, 1000);
                            }
                            break;
                        // 跳过按钮
                        case AdSpreadBIDView.NOTIFY_COUNTER_TEXT:
                            if (null != adCounter) {
                                adCounter.text = ("跳过");
                                adCounter.invalidate();
                            }
                            break;
                        // 自定义 --通知接口
                        case AdSpreadBIDView.NOTIFY_COUNTER_CUSTOM:
                            notifyCustomCountDownInterface(msg);
                            break;
                    }
                    break;
                // 当用户调用跳过广告时调用
                case ConstantValues.USERCANCEL:
                    if (!adSpreadBIDView.isDisplayed)
                        if (KyAdBaseView.spreadSettleType == KyAdBaseView.SpreadSettleType.CPM)
                            if ((null != getViewById(ConstantValues.WEBVIEWID, msg) && getViewById(ConstantValues.WEBVIEWID, msg).isShown()) || (null != getViewById(ConstantValues.ICONID, msg) && getViewById(ConstantValues.ICONID, msg).isShown())) {
                                notifyImpressionInterface(msg);
                            }
                    notifyUserCancelInterface(msg);
                    break;
                // 广告接受失败时调用
                case ConstantValues.FAILED:
                    removeMessages(ConstantValues.STRICT);
                    removeMessages(ConstantValues.DELAY);
                    notifyFaildedIntetface("failed" + msg.arg1, msg);
                    break;
                // 初始化成功后会接到此消息
                // 默认展示3秒
                case ConstantValues.INITSUCCESS:
                    Message initMsg = new Message();
                    initMsg.what = ConstantValues.STRICT;
                    initMsg.arg2 = -1;
                    sendMessageDelayed(initMsg, 3 * 1000l);
                    break;
                // 获取到web版广告时调用
                case ConstantValues.WEBVIEWRECIEVED:
                    try {
                        if (hasClosed)
                            break;
                        configAdData(adsBean, adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), msg);
                        final WebView adWebView = (WebView) getViewById(ConstantValues.WEBVIEWID, msg);
                        if (null != adWebView) {
                            if (null != adSpreadBIDView.kyViewListener)
                                adSpreadBIDView.kyViewListener.setClickMotion((MRAIDView) adSpreadBIDView.kyViewListener.getSpreadView().findViewById(ConstantValues.MRAIDVIEWID), adSpreadBIDView.touchRect);
//                            .setClickMotion((MRAIDView) adSpreadBIDView.spreadView.findViewById(ConstantValues.MRAIDVIEWID), adsBean, adSpreadBIDView.touchRect);
                            if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                                //adWebView.loadDataWithBaseURL(ConstantValues.WEBVIEW_BASEURL, adsBean.getXhtml(),"text/html", "UTF-8", null);
                                KyAdBaseView.loadWebScript(adWebView, adsBean.getXhtml());
                            } else
                                adWebView.loadUrl(adsBean.getXhtml());
                        }

                        notifyRecievedInterface(msg, true);

                        if (KyAdBaseView.spreadSettleType != KyAdBaseView.SpreadSettleType.CPM) {
                            if (null != getViewById(ConstantValues.WEBVIEWID, msg)
                                    && (getViewById(ConstantValues.WEBVIEWID, msg)).isShown()) {
                                notifyImpressionInterface(msg);
                            }
                        }
                        notifyReady(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyFaildedIntetface("WEBVIEWRECIEVED failed", msg);
                    }
                    break;
                // 图片下载完成后调用
                // 设置背景图片，并且取消初始化消息，重新展示计时
                case ConstantValues.BITMAPRECIEVED:
                    try {
                        if (hasClosed)
                            break;
                        //configAdData(adsBean, adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), msg);
                        configAdData(adsBean, adsBean.getAdWidth(), adsBean.getAdHeight(), msg); //widler 2019 changes
                        ImageView icon = (ImageView) getViewById(ConstantValues.ICONID, msg);
                        WebView adWebView = (WebView) getViewById(ConstantValues.WEBVIEWID, msg);
                        if (null != adSpreadBIDView.kyViewListener) {
                            adSpreadBIDView.kyViewListener.setClickMotion((MRAIDView) adSpreadBIDView.kyViewListener.getSpreadView().findViewById(ConstantValues.MRAIDVIEWID), adSpreadBIDView.touchRect);
                        }

                        if (AdViewUtils.bitmapOnLine) {
                            KyAdBaseView.loadWebContentURL(adWebView, adSpreadBIDView.bitmapPath, adsBean.getAdLink()); //wilder 2019 load online
                        }else {
                            KyAdBaseView.loadWebContentLocal(adWebView, adSpreadBIDView.bitmapPath,
                                                adsBean.getAdLink(),
                                                adsBean.getDeformationMode() >= ConstantValues.SCALE_NOHTML ? -99 : adsBean.getRealAdWidth(),
                                                adsBean.getDeformationMode() >= ConstantValues.SCALE_NOHTML ? -99 : adsBean.getRealAdHeight());
                        }
                        notifyRecievedInterface(msg, false);
                        if (KyAdBaseView.spreadSettleType != KyAdBaseView.SpreadSettleType.CPM) {
                            if ((null != adWebView && adWebView.isShown())
                                    || (null != icon && icon.isShown())
                                    || (null != getViewById(ConstantValues.WEBVIEWID, msg)
                                    && getViewById(ConstantValues.WEBVIEWID, msg).isShown())) {
                                notifyImpressionInterface(msg);
                            }
                        }
                        notifyReady(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyFaildedIntetface("BITMAPRECIEVED failed", msg);
                    }
                    break;
                // 到达规定展示时间后发展示报告
                // 并且发送延时信息
                case ConstantValues.STRICT:
                    try {
                        Message msg2 = new Message();
                        msg2.copyFrom(msg);
                        msg2.what = ConstantValues.UIDELAYUPDATE;
                        if (adsBean.getRuleTime() == 0 || adsBean.getDelayTime() != 0)
                            sendMessage(msg2);
                        sendEmptyMessageDelayed(
                                ConstantValues.DELAY,
                                null == adsBean ? 1
                                        : adsBean.getDelayTime() * 1000l);
                    } catch (Exception e) {
                    }
                    break;
                // 展示可以关闭，通知开发者
                case ConstantValues.DELAY:
                    try {
                        if (!adSpreadBIDView.isDisplayed) {
                            if (KyAdBaseView.spreadSettleType == KyAdBaseView.SpreadSettleType.CPM)
                                if ((null != getViewById(ConstantValues.WEBVIEWID, msg) && getViewById(ConstantValues.WEBVIEWID, msg).isShown()) || (null != getViewById(ConstantValues.ICONID, msg) && getViewById(ConstantValues.ICONID, msg).isShown())) {
                                    notifyImpressionInterface(msg);
                                }
                        }
                        if (adSpreadBIDView.kyViewListener.isClickableConfirm()) {
                            hasClosed = true;
                            removeMessages(ConstantValues.STRICT);
                            notifyCloseInterface(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyCloseInterface(msg);
                    }
                    break;
                case ConstantValues.IMPRESSION:
                    notifyImpressionInterface(msg);
                    break;
                case ConstantValues.UIDELAYUPDATE:
                    try {
                        adCounter = (InstlView.CenterTextView) getViewById(ConstantValues.SPREADADCOUNTER, msg);
                        if (msg.arg1 == AdSpreadBIDView.NOTIFY_COUNTER_TEXT || msg.arg1 == AdSpreadBIDView.NOTIFY_COUNTER_NUM) {
                            if (null != adCounter)
                                adCounter.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ConstantValues.CLOSEDSTATUSCHECK:
                    try {
                        if (!hasMessages(ConstantValues.STRICT))
                            if (!hasMessages(ConstantValues.DELAY))
                                sendEmptyMessage(ConstantValues.DELAY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    class InitRunnable implements Runnable {

        public InitRunnable() {
        }

        @Override
        public void run() {
            AdsBean adsBean = kyViewListener.getAdsBean();
            switch (adsBean.getAdType()) {
                case ConstantValues.HTML:
                    initRes(null);
                    break;
                case ConstantValues.MIXED:
                    initRes(adsBean.getGetImageUrl() + adsBean.getAdIcon());
                    break;
                default:
                    initRes(adsBean.getGetImageUrl() + adsBean.getAdPic());
                    break;
            }
        }
    }

    private void setTouchArea() {
        AdsBean adsBean = kyViewListener.getAdsBean();
        if (null != adsBean.getPointArea()) {
            String[] rectArray = adsBean.getPointArea()
                    .replace("(", "").replace(")", "").split(",");
            if (rectArray.length == 4) {
                touchRect = AdViewUtils.string2Rect(
                        Integer.valueOf(rectArray[0].trim()),
                        Integer.valueOf(rectArray[1].trim()),
                        Integer.valueOf(rectArray[2].trim()),
                        Integer.valueOf(rectArray[3].trim()));
            }
        }
    }

    private void initRes(String bitmapPath) {
        Message msg = new Message();
        Rect bitmapRect = null;
        int tempWidth = 0, tempHeight = 0;
        try {
            AdsBean adsBean = kyViewListener.getAdsBean();

            switch (adsBean.getAdType()) {
                case ConstantValues.HTML:
                    String html = adsBean.getXhtml();
                    if (TextUtils.isEmpty(html)) {
                        msg.what = ConstantValues.FAILED;
                        msg.obj = this;
                        handler.sendMessage(msg);
                        return;
                    }
                    tempHeight = (int) (adsBean.getAdHeight() * density);
                    tempWidth = (int) (adsBean.getAdWidth() * density);
                    msg.what = ConstantValues.WEBVIEWRECIEVED;
                    break;
                default:
                    if (TextUtils.isEmpty(bitmapPath)) {
                        msg.what = ConstantValues.FAILED;
                        msg.obj = this;
                        handler.sendMessage(msg);
                    }

                    if (AdViewUtils.bitmapOnLine) { //wilder 2019 , use online bitmap
                        this.bitmapPath = bitmapPath;
                        tempHeight = (int) (adsBean.getAdHeight() * density);
                        tempWidth = (int) (adsBean.getAdWidth() * density);
                    }else {
                        this.bitmapPath = (String) AdViewUtils.getInputStreamOrPath(context, bitmapPath, 1);
                        if (TextUtils.isEmpty(this.bitmapPath)) {
                            msg.what = ConstantValues.FAILED;
                            msg.obj = this;
                            handler.sendMessage(msg);
                            return;
                        }
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(this.bitmapPath, options);
                       tempWidth = screenWidth;
                       tempHeight = (int) ((float) screenWidth / (float) options.outWidth * options.outHeight);
                    }

                    msg.what = ConstantValues.BITMAPRECIEVED;
                    break;
            }
            switch (adsBean.getDeformationMode()) {
                case ConstantValues.SCALE_NOHTML:
                    if (adsBean.getAdType() != ConstantValues.HTML) {
                        tempHeight = (screenHeight - (screenWidth / 4 * isHasSpreadLogo()));
                    }
                    break;
                case ConstantValues.SCALE_INCLUDEHTML:
                    tempHeight = (screenHeight - (screenWidth / 4 * isHasSpreadLogo()));
                    break;
                case ConstantValues.NOSCALED:
                    break;
            }
            adsBean.setRealAdWidth(adWidth = tempWidth);
            adsBean.setRealAdHeight(adHeight = tempHeight);
            bitmapRect = new Rect(0, 0, adWidth, adHeight);
            setTouchArea();
            touchRect = reSizeRect(bitmapRect, touchRect);
            msg.obj = this;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新匹配点击区域
     *
     * @param rectA 图片rect
     * @param rectB 点击rect
     * @return 图片的可点击区域
     */
    private Rect reSizeRect(Rect rectA, Rect rectB) {
        Rect newRect;
        if (null == rectA || null == rectB)
            return null;
        newRect = new Rect(rectB.left * rectA.right / 1000, rectB.top
                * rectA.bottom / 1000, rectB.right * rectA.right / 1000,
                rectB.bottom * rectA.bottom / 1000);
        return newRect;
    }

    private int isHasSpreadLogo() {
        AdsBean adsBean = kyViewListener.getAdsBean();
        if (adsBean.getSpreadType() == ConstantValues.HASLOGO) {
//            if (null == getSpreadLogo())
//                return 0;
//            else
            return 1;
        } else
            return 0;
    }

    @Override
    public void sendMessage(int msg) {
        handler.sendEmptyMessage(msg);
    }

    @Override
    public void removeMessage(int msg) {
        handler.removeMessages(msg);
    }

    @Override
    public void cancelSpreadAd() {
        Message userCancelMsg = new Message();
        userCancelMsg.what = ConstantValues.USERCANCEL;
        userCancelMsg.obj = this;
        handler.sendMessage(userCancelMsg);
    }


    @Override
    public void onCloseBtnClicked() {
        cancelSpreadAd();
    }

    @Override
    public void onViewClicked(MotionEvent e, AgDataBean agDataBean, String url, float downX, float downY) {

    }


    @Override
    public boolean isClickableConfirm() {
        return false;
    }

    @Override
    public void setClickMotion(MRAIDView view, Rect touchRect) {
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(String url) {
        return kyViewListener.shouldInterceptRequest(url);
    }

    @Override
    public boolean needConfirmDialog() {
        return false;
    }

    @Override
    public void checkClick(String url) {

    }

    @Override
    public void onReady(AgDataBean agDataBean, boolean force) {

    }

    @Override
    public void onReceived(AgDataBean agDataBean, boolean force) {

    }

    @Override
    public void onAdFailed(AgDataBean agDataBean, String error, boolean force) {

    }

    @Override
    public void onDisplay(AgDataBean agDataBean, boolean force) {

    }

    @Override
    public boolean getCloseble() {
        return false;
    }

    @Override
    public String getAdLogo() {
        if (null != kyViewListener)
            return kyViewListener.getAdLogo();
        return null;
    }

    @Override
    public String getAdIcon() {
        if (null != kyViewListener)
            return kyViewListener.getAdIcon();
        return null;
    }

    @Override
    public Drawable getSpreadLogo() {
        if (null != kyViewListener)
            return kyViewListener.getSpreadLogo();
        return null;
    }

    @Override
    public int getNotifyType() {
        if (null != kyViewListener)
            return kyViewListener.getNotifyType();
        return AdSpreadBIDView.NOTIFY_COUNTER_NUM;
    }

    @Override
    public AdsBean getAdsBean() {
        return null;
    }

    @Override
    public void rotatedAd(Message msg) {

    }

    @Override
    public String getBehaveIcon() {
        return null;
    }

    @Override
    public SpreadView getSpreadView() {
        return null;
    }

    @Override
    public SpreadView getSkipView() {
        return null;
    }

    @Override
    public void onAdNotifyCustomCallback(ViewGroup view, int ruleTime, int delayTime) {

    }

    @Override
    public void mraidNativeFeatureDownload(String url) {
        if (null != kyViewListener)
            kyViewListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureCallTel(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL); // android.intent.action.DIAL
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void mraidNativeFeatureOpenDeeplink(String url) {
        if (url.startsWith("mraid")) {
            try {
                url = URLDecoder.decode(url.replace("mraid://openDeeplink?url=", ""), "UTF-8");
                if (null != kyViewListener) {
                    AdsBean adsBean = kyViewListener.getAdsBean();
                    adsBean.setDeeplink(url);
                }
                if (null != kyViewListener)
                    kyViewListener.checkClick(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mraidNativeFeatureOpenBrowser(String url) {
        if (null != kyViewListener)
            kyViewListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureSendSms(String url) {
        AdViewUtils.sendSms(context, url);
    }

    @Override
    public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {
    }

    @Override
    public void mraidNativeFeatureStorePicture(String url) {
    }


    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
        //(wilder 2019) for page finished then start counter
        if (handler != null && !isPageDone ) {
            isPageDone = true;
            handler.notifyCountDown(handler.adsBean.getRuleTime() + handler.adsBean.getDelayTime(),
                    kyViewListener.getNotifyType());
        }
        //end wilder 2019
    }

    @Override
    public void mraidViewExpand(MRAIDView mraidView) {
    }

    @Override
    public void mraidViewClose(MRAIDView mraidView) {
    }

    @Override
    public boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX, int offsetY) {
        return false;
    }

    @Override
    public void onShouldOverride(String url) {
        // 至少触摸过才可以点击跳转
        if (null != kyViewListener) {
            AdsBean adsBean = kyViewListener.getAdsBean();
            if (adsBean.getTouchStatus() > MRAIDView.ACTION_DEFAULT) {
                kyViewListener.checkClick(url);
            }
        }
    }

    @Override
    public WebResourceResponse onShouldIntercept(String data) {
        if (null != kyViewListener)
            return kyViewListener.shouldInterceptRequest(data);
        return null;
    }

    @Override
    public void loadDataError(int errorType) {
        if (null != kyViewListener)
            kyViewListener.onAdFailed(null, "Custom://" + errorType, false);
    }

}
