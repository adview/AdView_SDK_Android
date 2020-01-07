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
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.InstlView;
import com.kuaiyou.utils.SpreadView;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;

public class AdBIDSpreadAdapter extends AdAdapterManager /*implements KySpreadListener */ {

    private SpreadHandler handler;
    private KySpreadListener kySpreadViewListener;
    private String bitmapPath;
    private Context context;

    private int screenWidth, screenHeight;
    private int adWidth, adHeight;

    private double density;
    private Rect touchRect;

    private boolean isDisplayed = false;

    static boolean isPageDone = false; //wilder 2019
    private AdsBean adsBean;

    public MRAIDView getMRaidView() {
        if (null != kySpreadViewListener) {
            return (MRAIDView) this.kySpreadViewListener.getSpreadView().findViewById(ConstantValues.UI_MRAIDVIEW_ID);
        }
        return null;
    }

    public /*static*/ class SpreadHandler extends Handler {
        //private SoftReference<AdBIDSpreadAdapter> adSpreadBIDViewReference = null;
        private boolean hasClosed = false;
        //private AdsBean adsBean;

        public SpreadHandler(AdBIDSpreadAdapter adSpreadBIDAdpt) {
            super(Looper.getMainLooper());
/*  wilder 2019 covered
            adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>(adSpreadBIDAdpt);
            adsBean = adSpreadBIDViewReference.get().kySpreadViewListener.getAdsBean();
            adSpreadBIDViewReference.get().kySpreadViewListener.getSpreadView().setSpreadViewListener(adSpreadBIDAdpt); //wilder : old is set to itself
            //adSpreadBIDViewReference.get().kySpreadViewListener.getSpreadView().setSpreadViewListener(adSpreadBIDAdpt.kySpreadViewListener);
            adSpreadBIDViewReference.get().kySpreadViewListener.getSpreadView().init();
*/
        }

       /**
         * 通知收到广告接口，并设置按钮背景颜色
         */
        private void notifyRecievedInterface(Message msg, boolean isHTML) {
            try {
/* wilder 2019 covered
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDAdpt = adSpreadBIDViewReference.get();
*/

                //wilder 2019 fixed for web content loader will slow and may counter down faster
                if (isHTML) {
                    isPageDone = false; //wilder 2019 for start counter in page done, see bellow.
                } else {
                    //notifyCountDown(adsBean.getRuleTime() + adsBean.getDelayTime(), adSpreadBIDAdpt.kySpreadViewListener.getNotifyType()); wilder 2019 covered
                    notifyCountDown(adsBean.getRuleTime() + adsBean.getDelayTime(), kySpreadViewListener.getNotifyType());
                }

                setNotifyBackground((TextView) getViewById(ConstantValues.SPREAD_UI_COUNTERID, msg));

/*               wilder 2019 covered
                if (null != adSpreadBIDAdpt.kySpreadViewListener) {
                    adSpreadBIDAdpt.kySpreadViewListener.onReceived(null, false);
                }*/
                if (null != kySpreadViewListener) {
                    kySpreadViewListener.onReceived(null, false);
                }
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
/*          wilder 2019 covered
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);

                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                */
                removeMessages(ConstantValues.SPREAD_RESP_STRICT);
                removeMessages(ConstantValues.SPREAD_RESP_DELAY);
                Message msg2 = new Message();
                msg2.what = ConstantValues.SPREAD_RESP_STRICT;
                //msg2.arg1 = adSpreadBIDView.kySpreadViewListener.getNotifyType(); wilder 2019 covered
                msg2.arg1 = kySpreadViewListener.getNotifyType();
                sendMessageDelayed(msg2,adsBean.getRuleTime() * 1000l);
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
                    msg.what = ConstantValues.SPREAD_RESP_COUNTDOWN;
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
/*              wilder 2019 covered
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDAdpt = adSpreadBIDViewReference.get();
                adSpreadBIDAdpt.kySpreadViewListener.onDisplay(null, false);
                adSpreadBIDAdpt.isDisplayed = true;
*/
                kySpreadViewListener.onDisplay(null, false);
                isDisplayed = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏用户手动取消/接口
         */
        private void notifyUserCancelInterface(Message msg) {
            try {
/*          wilder 2019 covered
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
*/

                WebView webView = (WebView) getViewById(ConstantValues.UI_WEBVIEW_ID, msg);
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
                removeMessages(ConstantValues.SPREAD_RESP_STRICT);
                removeMessages(ConstantValues.SPREAD_RESP_DELAY);
                //adSpreadBIDView.kySpreadViewListener.onCloseBtnClicked(); wilder 2019 covered
                kySpreadViewListener.onCloseBtnClicked();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏失败/接口
         */
        private void notifyFaildedIntetface(String mg, Message msg) {
            try {
/*          wilder 2019 covered
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                adSpreadBIDView.kySpreadViewListener.onAdFailed(null, mg, false);
*/
                kySpreadViewListener.onAdFailed(null, mg, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏倒计时/接口
         */
        private void notifyCustomCountDownInterface(Message msg) {
            try {
/*              wilder 2019 covered
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                adSpreadBIDView.kySpreadViewListener.onAdNotifyCustomCallback(
                        (RelativeLayout) getViewById(ConstantValues.SPREAD_UI_NOTIFYLAYOUTID, msg),
                        adsBean.getRuleTime(),
                        adsBean.getDelayTime());
*/
                kySpreadViewListener.onAdNotifyCustomCallback(
                        (RelativeLayout) getViewById(ConstantValues.SPREAD_UI_NOTIFYLAYOUTID, msg),
                        adsBean.getRuleTime(),
                        adsBean.getDelayTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 通知开屏关闭/接口
         */
        private void notifyCloseInterface(Message msg) {
            try {
                kySpreadViewListener.onCloseBtnClicked(); //widler 20190612
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
/*          wilder 2019 covered
                if (null == adSpreadBIDViewReference.get())
                    adSpreadBIDViewReference = new SoftReference<AdBIDSpreadAdapter>((AdBIDSpreadAdapter) msg.obj);
                AdBIDSpreadAdapter adSpreadBIDView = adSpreadBIDViewReference.get();
                return adSpreadBIDView.kySpreadViewListener.getSpreadView().findViewById(id);
*/
                return kySpreadViewListener.getSpreadView().findViewById(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 文字没有，则以下文字高为0.
         * 1.图片大于等于屏幕高，隐藏LOGO，文字压在图片底部上，宽等同于图片绘制宽，高为宽的1/4.(Extra 3)
         * 2.图片+LOGO大于等于屏幕高，图片TOP对齐，LOGO压在图片上，文字在挨着在LOGO上方，宽等同于图片绘制宽，高为宽的1/4.(Extra 2)
         * 3.图片+文字高+LOGO大于等于屏幕高，图片TOP对齐，文字在挨着在LOGO上方（可能压部分图片），宽等同于图片绘制宽，高为宽的1/4.(Extra 1)
         * 4.图片+文字高+LOGO小于屏幕高，文字在图片下方(Extra 0/4 暂时没处理)，TOP,CENTER,BOTTOM表示在LOGO固定在BOTTOM的情况下，上部区域中图片＋
         * 文字的对齐方式.All Center表示图片文字＋LOGO连接在一起居中。
         *
         * @param vat          1－Top；2-Center；3-Bottom；4-All Center
         * @param bitmapHeight 图片高度
         * @param hasText      是否有文字
         * @return
         */
        private int getAdLayoutType(Context context, int vat, int bitmapHeight, int hasText, int hasLogo) {
            int layoutType = 0;
            int[] screenSize = AdViewUtils.getWidthAndHeight(context, false, true);
            if (bitmapHeight >= screenSize[1])
                return ConstantValues.SPREAD_UI_EXTRA3;
            else if (bitmapHeight + screenSize[0] / 4 * hasLogo >= screenSize[1])
                return ConstantValues.SPREAD_UI_EXTRA2;
            else if (bitmapHeight <= screenSize[1] && hasLogo == 0 && hasText == 1)
                return ConstantValues.SPREAD_UI_CENTER;
            else if (bitmapHeight + screenSize[0] / 4 * hasText + screenSize[0] / 4 * hasLogo >= screenSize[1])
                return ConstantValues.SPREAD_UI_EXTRA1;

            layoutType = vat;
            return layoutType;
        }

        /**
         * 获取开屏说明文字
         *
         * @param adsBean
         * @return
         */
        private String getAdText(AdsBean adsBean) {
            String tempText = "";
            if (!TextUtils.isEmpty(adsBean.getAdTitle()))
                tempText = adsBean.getAdTitle();
            if (!TextUtils.isEmpty(tempText) && !TextUtils.isEmpty(adsBean.getAdSubTitle())) {
                tempText = tempText + "" + adsBean.getAdSubTitle();
            }
            return tempText;
        }

        private void configAdData(AdsBean adsBean, int width, int height, Message msg) {
            try {
                kySpreadViewListener.getSpreadView().initWidgetLayout(
                        width, height,
                        adsBean.getAdType(),
                        getAdLayoutType(context,
                                adsBean.getVat(), height,
                                TextUtils.isEmpty(adsBean.getAdText()) ? 0 : 1,
                                isHasSpreadLogo()),
                        adsBean.getDeformationMode(),
                        isHasSpreadLogo()
                );
                kySpreadViewListener.getSpreadView().setContent(adsBean, bitmapPath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            InstlView.CenterTextView adCounter;

            switch (msg.what) {
                // 通知倒计时功能
                case ConstantValues.SPREAD_RESP_COUNTDOWN:
                    adCounter = (InstlView.CenterTextView) getViewById(ConstantValues.SPREAD_UI_COUNTERID, msg);
                    switch (msg.arg2) {
                        // 倒计时
                        case AdSpreadBIDView.NOTIFY_COUNTER_NUM:
                            if (1 <= msg.arg1) {
                                if (null != adCounter) {
                                    adCounter.text = (msg.arg1 + "s | Skip");
                                    adCounter.invalidate();
                                }
                                Message mg = new Message();
                                mg.what = ConstantValues.SPREAD_RESP_COUNTDOWN;
                                mg.arg1 = msg.arg1 - 1;
                                mg.arg2 = AdSpreadBIDView.NOTIFY_COUNTER_NUM;
                                sendMessageDelayed(mg, 1000);
                            }
                            break;
                        // 跳过按钮
                        case AdSpreadBIDView.NOTIFY_COUNTER_TEXT:
                            if (null != adCounter) {
                                adCounter.text = ("Skip");
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
                case ConstantValues.SPREAD_RESP_USERCANCEL:
                    //if (!adSpreadBIDAdapter.isDisplayed)  wilder 2019 covered
                    if (!isDisplayed) {
                        if (KyAdBaseView.spreadSettleType == KyAdBaseView.SpreadSettleType.CPM)
                            if ((null != getViewById(ConstantValues.UI_WEBVIEW_ID, msg) &&
                                    getViewById(ConstantValues.UI_WEBVIEW_ID, msg).isShown()) || (null != getViewById(ConstantValues.MIXED_UI_ICONID, msg)
                                    && getViewById(ConstantValues.MIXED_UI_ICONID, msg).isShown())) {
                                notifyImpressionInterface(msg);
                            }
                    }
                    notifyUserCancelInterface(msg);
                    break;
                // 广告接受失败时调用
                case ConstantValues.SPREAD_RESP_FAILED:
                    removeMessages(ConstantValues.SPREAD_RESP_STRICT);
                    removeMessages(ConstantValues.SPREAD_RESP_DELAY);
                    notifyFaildedIntetface("failed" + msg.arg1, msg);
                    break;
                // 初始化成功后会接到此消息
                // 默认展示3秒
                case ConstantValues.SPREAD_REQ_INIT_SUCCESS:
                    Message initMsg = new Message();
                    initMsg.what = ConstantValues.SPREAD_RESP_STRICT;
                    initMsg.arg2 = -1;
                    sendMessageDelayed(initMsg, 3 * 1000l);
                    break;
                // 获取到web版广告时调用
                case ConstantValues.SPREAD_RESP_HTML_RECEIVED:
                    try {
                        if (hasClosed)
                            break;
                        configAdData(adsBean, adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), msg);
                        final WebView adWebView = (WebView) getViewById(ConstantValues.UI_WEBVIEW_ID, msg);
                        if (null != adWebView) {
                            if (null != kySpreadViewListener) {
                                kySpreadViewListener.setClickMotion(
                                        (MRAIDView) kySpreadViewListener.getSpreadView().findViewById(ConstantValues.UI_MRAIDVIEW_ID),
                                        touchRect);
                            }
                            if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                                AdViewUtils.loadWebContentExt(adWebView, adsBean.getXhtml());
                            } else {
                                adWebView.loadUrl(adsBean.getXhtml());
                            }
                        }

                        notifyRecievedInterface(msg, true);

                        if (KyAdBaseView.spreadSettleType != KyAdBaseView.SpreadSettleType.CPM) {
                            if (null != getViewById(ConstantValues.UI_WEBVIEW_ID, msg)
                                    && (getViewById(ConstantValues.UI_WEBVIEW_ID, msg)).isShown()) {
                                notifyImpressionInterface(msg);
                            }
                        }
                        notifyReady(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyFaildedIntetface("SPREAD_RESP_HTML_RECEIVED failed", msg);
                    }
                    break;
                // 图片下载完成后调用
                // 设置背景图片，并且取消初始化消息，重新展示计时
                case ConstantValues.SPREAD_RESP_BITMAP_RECEIVED:
                    try {
                        if (hasClosed)
                            break;
                        configAdData(adsBean, adsBean.getAdWidth(), adsBean.getAdHeight(), msg); //widler 2019 changes
                        ImageView icon = (ImageView) getViewById(ConstantValues.MIXED_UI_ICONID, msg);
                        WebView adWebView = (WebView) getViewById(ConstantValues.UI_WEBVIEW_ID, msg);
                        if (null != kySpreadViewListener) {
                            kySpreadViewListener.setClickMotion((MRAIDView) kySpreadViewListener.getSpreadView().
                                    findViewById(ConstantValues.UI_MRAIDVIEW_ID), touchRect);
                        }

                        if (AdViewUtils.bitmapOnLine) {
                            AdViewUtils.loadWebImageURL(adWebView, bitmapPath, adsBean.getAdLink());
                        }else {
                            AdViewUtils.loadWebImageLocal(adWebView, bitmapPath,
                                    adsBean.getAdLink(),
                                    adsBean.getDeformationMode() >= ConstantValues.SPREAD_UI_SCALE_NOHTML ? -99 : adsBean.getRealAdWidth(),
                                    adsBean.getDeformationMode() >= ConstantValues.SPREAD_UI_SCALE_NOHTML ? -99 : adsBean.getRealAdHeight());

                        }
                        notifyRecievedInterface(msg, false);
                        if (KyAdBaseView.spreadSettleType != KyAdBaseView.SpreadSettleType.CPM) {
                            if ((null != adWebView && adWebView.isShown())
                                    || (null != icon && icon.isShown())
                                    || (null != getViewById(ConstantValues.UI_WEBVIEW_ID, msg)
                                    && getViewById(ConstantValues.UI_WEBVIEW_ID, msg).isShown())) {
                                notifyImpressionInterface(msg);
                            }
                        }
                        notifyReady(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyFaildedIntetface("SPREAD_RESP_BITMAP_RECEIVED failed", msg);
                    }
                    break;
                // 到达规定展示时间后发展示报告
                // 并且发送延时信息
                case ConstantValues.SPREAD_RESP_STRICT:
                    try {
                        Message msg2 = new Message();
                        msg2.copyFrom(msg);
                        msg2.what = ConstantValues.SPREAD_RESP_UIDELAY_UPDATE;
                        if (adsBean.getRuleTime() == 0 || adsBean.getDelayTime() != 0)
                            sendMessage(msg2);
                        sendEmptyMessageDelayed(
                                ConstantValues.SPREAD_RESP_DELAY,
                                null == adsBean ? 1
                                        : adsBean.getDelayTime() * 1000l);
                    } catch (Exception e) {
                    }
                    break;
                // 展示可以关闭，通知开发者
                case ConstantValues.SPREAD_RESP_DELAY:
                    try {
                        //if (!adSpreadBIDAdapter.isDisplayed) wilder 2019 covered
                        if (!isDisplayed)
                        {
                            if (KyAdBaseView.spreadSettleType == KyAdBaseView.SpreadSettleType.CPM)
                                if ((null != getViewById(ConstantValues.UI_WEBVIEW_ID, msg) && getViewById(ConstantValues.UI_WEBVIEW_ID, msg).isShown()) ||
                                        (null != getViewById(ConstantValues.MIXED_UI_ICONID, msg) && getViewById(ConstantValues.MIXED_UI_ICONID, msg).isShown())) {
                                    notifyImpressionInterface(msg);
                                }
                        }
                        //if (adSpreadBIDAdapter.kySpreadViewListener.isClickableConfirm())  wilder 2019 covered
                        if (kySpreadViewListener.isClickableConfirm())
                        {
                            hasClosed = true;
                            removeMessages(ConstantValues.SPREAD_RESP_STRICT);
                            notifyCloseInterface(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyCloseInterface(msg);
                    }
                    break;
                case ConstantValues.SPREAD_RESP_IMPRESSION:
                    notifyImpressionInterface(msg);
                    break;
                case ConstantValues.SPREAD_RESP_UIDELAY_UPDATE:
                    try {
                        adCounter = (InstlView.CenterTextView) getViewById(ConstantValues.SPREAD_UI_COUNTERID, msg);
                        if (msg.arg1 == AdSpreadBIDView.NOTIFY_COUNTER_TEXT || msg.arg1 == AdSpreadBIDView.NOTIFY_COUNTER_NUM) {
                            if (null != adCounter)
                                adCounter.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ConstantValues.SPREAD_RESP_CLOSEDSTATUS_CHECK:
                    try {
                        if (!hasMessages(ConstantValues.SPREAD_RESP_STRICT))
                            if (!hasMessages(ConstantValues.SPREAD_RESP_DELAY))
                                sendEmptyMessage(ConstantValues.SPREAD_RESP_DELAY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

//    class InitRunnable implements Runnable {
//
//        public InitRunnable() {
//        }
//
//        @Override
//        public void run() {
//            AdsBean adsBean = kySpreadViewListener.getAdsBean();
//            switch (adsBean.getAdType()) {
//                case ConstantValues.RESP_ADTYPE_HTML:
//                    initRes(null);
//                    break;
//                case ConstantValues.RESP_ADTYPE_MIXED:
//                    initRes(adsBean.getGetImageUrl() + adsBean.getAdIcon());
//                    break;
//                default:
//                    initRes(adsBean.getGetImageUrl() + adsBean.getAdPic());
//                    break;
//            }
//        }
//    }

    private void setTouchArea() {
        //AdsBean adsBean = kySpreadViewListener.getAdsBean();
        if (null != adsBean.getPointArea()) {
            String[] rectArray = adsBean.getPointArea().replace("(", "").replace(")", "").split(",");
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
            //AdsBean adsBean = kySpreadViewListener.getAdsBean();
            switch (adsBean.getAdType()) {
                case ConstantValues.RESP_ADTYPE_HTML:
                    String html = adsBean.getXhtml();
                    if (TextUtils.isEmpty(html)) {
                        msg.what = ConstantValues.SPREAD_RESP_FAILED;
                        msg.obj = this;
                        handler.sendMessage(msg);
                        return;
                    }
                    tempHeight = (int) (adsBean.getAdHeight() * density);
                    tempWidth = (int) (adsBean.getAdWidth() * density);
                    msg.what = ConstantValues.SPREAD_RESP_HTML_RECEIVED;
                    break;
                default:
                    if (TextUtils.isEmpty(bitmapPath)) {
                        msg.what = ConstantValues.SPREAD_RESP_FAILED;
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
                            msg.what = ConstantValues.SPREAD_RESP_FAILED;
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

                    msg.what = ConstantValues.SPREAD_RESP_BITMAP_RECEIVED;
                    break;
            }
            switch (adsBean.getDeformationMode()) {
                case ConstantValues.SPREAD_UI_SCALE_NOHTML:
                    if (adsBean.getAdType() != ConstantValues.RESP_ADTYPE_HTML) {
                        tempHeight = (screenHeight - (screenWidth / 4 * isHasSpreadLogo()));
                    }
                    break;
                case ConstantValues.SPREAD_UI_SCALE_INCLUDEHTML:
                    tempHeight = (screenHeight - (screenWidth / 4 * isHasSpreadLogo()));
                    break;
                case ConstantValues.SPREAD_UI_NOSCALED:
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
        newRect = new Rect(rectB.left * rectA.right / 1000,
                rectB.top * rectA.bottom / 1000, rectB.right * rectA.right / 1000,
                rectB.bottom * rectA.bottom / 1000);
        return newRect;
    }

    private int isHasSpreadLogo() {
        //AdsBean adsBean = kySpreadViewListener.getAdsBean();
        if (adsBean.getSpreadType() == ConstantValues.SPREAD_RESP_HAS_LOGO) {
            //wilder 2020 for logo,该判断能够正确调整spread的坐标和大小
            if (null == kySpreadViewListener.getSpreadLogo())
                return 0;
            else
                return 1;
        } else
            return 0;
    }

    //////////////////////////////////////////////////////////////////////////////
    ////////////////////////// override for AdAdapterManager ////////////////////
    /////////////////////////////////////////////////////////////////////////////
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

        kySpreadViewListener = (KySpreadListener) bundle.getSerializable("interface");

        handler = new SpreadHandler(this);
        //new Thread(new InitRunnable()).start();
        adsBean = kySpreadViewListener.getAdsBean();
        switch (adsBean.getAdType()) {
            case ConstantValues.RESP_ADTYPE_HTML:
                initRes(null);
                break;
            case ConstantValues.RESP_ADTYPE_MIXED:
                initRes(adsBean.getGetImageUrl() + adsBean.getAdIcon());
                break;
            default:
                initRes(adsBean.getGetImageUrl() + adsBean.getAdPic());
                break;
        }
    }

    @Override
    public View getAdView() {
        return null;
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
        userCancelMsg.what = ConstantValues.SPREAD_RESP_USERCANCEL;
        userCancelMsg.obj = this;
        handler.sendMessage(userCancelMsg);
    }
    //wilder 2019 covered --- added
    public void mraidViewHasLoaded() {
        //(wilder 2019) for page finished then start counter
        if (handler != null && !isPageDone ) {
            isPageDone = true;
            handler.notifyCountDown(adsBean.getRuleTime() + adsBean.getDelayTime(),
                    kySpreadViewListener.getNotifyType());
        }
        //end wilder 2019
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////// AdVGListener //////////////////////////////


}
