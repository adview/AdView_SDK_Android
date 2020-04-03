package com.kuaiyou.adbid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.widget.RelativeLayout;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.interfaces.AdVGListener;

import com.kuaiyou.interfaces.OnAdViewListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ApplyAdBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.BannerView;
import com.kuaiyou.utils.ConstantValues;


import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AdBannerBIDView extends KyAdBaseView implements AdVGListener {

    private int reFreshTime = 30; // default 30 sec,-1 will not refresh
    private int routeType = ConstantValues.ROUTE_ADBID_TYPE;
    private String appID;
    private String posID;
    private ArrayList<KyAdBaseView> schedulerList = null;
    private boolean isEnded = false;
    private ApplyAdBean applyAdBean;
    private boolean closeAble;
    private boolean isImpressioned = false;
    private AdAdapterManager adAdapterManager;
    private boolean isFirst = true;

    private String adLogo, adIcon;
    private String bitmapPath;


    /**
     * 独立竞价使用
     *
     * @param context   Context
     * @param keyAdView SDK key
     * @param adSize    广告尺寸
     */
    public AdBannerBIDView(Context context, String keyAdView, int adSize) {
        this(context, keyAdView, ConstantValues.ROUTE_ADBID_TYPE, adSize, 30, null);
    }

    /**
     * 聚合使用
     *
     * @param context   Context
     * @param keyAdView SDK key
     * @param type      广告类型，区分竞价补余
     * @param adSize    广告尺寸
     * @deprecated
     */
    private AdBannerBIDView(Context context, String keyAdView, int type, int adSize, String vPosID) {
        this(context, keyAdView, type, adSize, -1, vPosID);
    }

    /**
     * 构造
     * @param context
     */
    public AdBannerBIDView(Context context, String appID, int type, int adSize, int reFreshTime, String vPosID) {
        super(context);
        super.adSize = adSize;
        //wilder 2019 here can get adsize for mrec
        calcAdSize();
        this.reFreshTime = reFreshTime;
        this.routeType = type;
        this.isEnded = false;
        this.appID = appID;
        this.posID = vPosID;

        if (null == schedulerList)
            schedulerList = new ArrayList<KyAdBaseView>();
        //got gpid
        AdViewUtils.getDeviceIdFirstTime(context, this);
    }

    /**
     * @param sec default 30 sec,-1 will not refresh,min time is 15 sec
     */
    public void setReFreshTime(int sec) {
        //can be set -1 means forever
        if (sec > -1 && sec < 15)
            sec = 15;

        reFreshTime = sec;
    }

    //for MREC video play
    public void playVideo(Context context) {
        AdViewUtils.logInfo("<----------------   ADBannerBIDView(): playVideo ------------->  " );
        //(wilder 2019) just covered, play video can by user , but now is auto play after vast parse done
        if (!getVideoMode())
            return;

        if (!AdViewUtils.videoAutoPlay && adAdapterManager != null) {
            adAdapterManager.playVideo(context);
        }
    }

    //20191105 for video autoplay
    public void setAutoPlay(boolean enable) {
        if (!getVideoMode())
            return;
        AdViewUtils.videoAutoPlay = enable;
    }

    /**
     * @param closeAble set false the ad can not be closed, true otherwise
     */
    public void setShowCloseBtn(boolean closeAble) {

        this.closeAble = closeAble;
    }

    //MREC used pos id
    public void setVideoMode(boolean enable) {

        super.setVideoMode(enable);
    }

    @Override
    protected void handlerMsgs(Message msg) {
        try {
            Bundle bundle = null;
            if (null == adsBean && msg.what != ConstantValues.NOTIFY_REQ_GPID_FETCH_DONE) {
                if (null != onAdViewListener)
                    onAdViewListener.onAdRecieveFailed(null,"NO_FILL - banner");

                if (reFreshTime != -1)
                    requestAd(reFreshTime);
                return;
            }
            switch (msg.what) {
                case ConstantValues.NOTIFY_REQ_GPID_FETCH_DONE:
                    requestAd(0);
                    break;
                case ConstantValues.NOTIFY_RESP_RECEIVEAD_OK:
                    // onAdRecieved
                    if (!selfTestMode_mrecVideo &&        //only test mode can accept all kinds of video type PDU
                        getVideoMode() &&
                        adsBean.getAdType() != ConstantValues.RESP_ADTYPE_VIDEO_EMBED) {
                        //类型不匹配，这里仅能接收Mrec视频或者普通视频
                        AdViewUtils.logInfo("!!!!!!ADBannerBIDView(): Mrec video type err, type = " + adsBean.getAdType() + "!!!!!!!" );
                        notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR, "INVALID_POS_MATCHED");
                        break;
                    }
                    if (null != onAdViewListener) {
                        onAdViewListener.onAdRecieved(this);
                    }

                    isImpressioned = false;
                    removeAdView();
                    adAdapterManager = handlerAd(true, -1, ConstantValues.SDK_REQ_TYPE_BANNER, null, this);
                    addAdView();
                    break;
                case ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR:
                    String failedMsg = "";
                    try {
                        isImpressioned = false;
                        if (null != adsBean && null != adsBean.getAgDataBean() && !TextUtils.isEmpty(adsBean.getAgDataBean().getAggsrc())) {
                            removeAdView();
                            adAdapterManager = handlerAd(false, -1, ConstantValues.SDK_REQ_TYPE_BANNER, adsBean.getAgDataBean(), this);
                            return;
                        }
                        // 如果刷新时间不等于-1 需要再次请求广告
                        if (reFreshTime != -1)
                            requestAd(reFreshTime);
                        failedMsg = String.valueOf(msg.obj);
                        if (failedMsg.equals(""))
                            failedMsg = "UNKNOW_ERROR";
                    } catch (Exception e) {
                        e.printStackTrace();
                        failedMsg = e.toString();

                        if (null != onAdViewListener) {
                            onAdViewListener.onAdRecieveFailed(null, failedMsg);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (null != onAdViewListener) {
                onAdViewListener.onAdRecieveFailed(null, e.toString());
            }
        }
    }

    private void removeAdView() {
        this.removeAllViews();
    }

    private void addAdView() {
        View v = adAdapterManager.getAdView();
        if (null == v || null != v.getParent())
            return;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(adShowWidth, adShowHeight);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.addView(v, layoutParams);
    }

    @Override
    protected boolean initAdLogo(Object object) {
        if (!TextUtils.isEmpty(adsBean.getAdLogoUrl())) {
            if (AdViewUtils.adLogoOnLine)
                adLogoBmp = AdViewUtils.getHttpBitmap(adsBean.getAdLogoUrl());//wilder 20200228 for test
            else
                adLogo = (String) AdViewUtils.getInputStreamOrPath( getContext(), adsBean.getAdLogoUrl(), 1);
        }
        if (!TextUtils.isEmpty(adsBean.getAdIconUrl())) {
            if (AdViewUtils.adLogoOnLine)
                adIconBmp = AdViewUtils.getHttpBitmap(adsBean.getAdIconUrl());//wilder 20200228 for test
            else
                adIcon = (String) AdViewUtils.getInputStreamOrPath( getContext(), adsBean.getAdIconUrl(), 1);
        }
//        else
//            adIcon = "/assets/icon_ad.png";
        return true;
    }


    @Override
    protected boolean createBitmap(Object object) {
        AdsBean adsBean = (AdsBean) object;
        String pic = null;
        try {
            switch (adsBean.getAdType()) {
                case ConstantValues.RESP_ADTYPE_FULLIMAGE:
                case ConstantValues.RESP_ADTYPE_INSTL:    //(wilder 2019) for instl mode picture
                    if (null != adsBean && null != adsBean.getAdPic())
                        pic = adsBean.getGetImageUrl() + adsBean.getAdPic();
                    if (AdViewUtils.bitmapOnLine) {
                        bitmapPath = pic;
                    }else {
                        bitmapPath = (String) AdViewUtils.getInputStreamOrPath(getContext(), pic, 1);
                    }
                    break;
                case ConstantValues.RESP_ADTYPE_INTERLINK:
                case ConstantValues.RESP_ADTYPE_MIXED:
                    if (adsBean.getAdIcon() != null && !adsBean.getAdIcon().trim().equals("")) {
                        pic = adsBean.getGetImageUrl() + adsBean.getAdIcon();
                    }
                    if (AdViewUtils.bitmapOnLine) {
                        bitmapPath = pic;
                    }else {
                        bitmapPath = (String) AdViewUtils.getInputStreamOrPath(getContext(), pic, 1);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_INTERLINK
                || adsBean.getAdType() == ConstantValues.RESP_ADTYPE_MIXED)
            return true;
        return null != bitmapPath;
    }

    @Override
    public void setOnAdViewListener(OnAdViewListener onAdViewListener) {
        super.setOnAdViewListener(onAdViewListener);
    }

    @Override
    protected void handleClick(MotionEvent e, int realX, int realY, String url) {  //点击跳转url的处理
        AdViewUtils.logInfo("--- AdBannerBIDView(): handleClick()-----");
        if (realX == 888 && realY == 888) {

            if (null != onAdViewListener) {
                onAdViewListener.onAdClicked(null);
            }
            return;
        }
        if (System.currentTimeMillis() - adsBean.getDataTime() > ConstantValues.AD_EXPIRE_TIME) {
            AdViewUtils.logInfo("ad has expired");
            return;// 广告过期不可点击
        }
        //点击汇报
        reportClick(e, realX, realY, applyAdBean, adsBean, respAdBean);
        clickEvent(getContext(), adsBean, url);

        if (null != onAdViewListener) {
            onAdViewListener.onAdClicked(null);
        }
    }

    @Override
    public String getBitmapPath() {
        return bitmapPath;
    }

    private void sendImpression(AgDataBean agDataBean, boolean force) {
        try {
            if (!isImpressioned) {
                if (null != onAdViewListener) {
                    onAdViewListener.onAdDisplayed(this);
                }
            }
            if (force || !isImpressioned) {
                // onAdDisplayAd
                if (null != agDataBean && null != agDataBean.getImpUrls()) {
                    reportOtherUrls(agDataBean.getImpUrls() + (adAdapterManager.getSufId() == 0 ? "" : "&sufid=" + adAdapterManager.getSufId()));
                    isImpressioned = true;
                }
                if (reportImpression(adsBean, respAdBean, applyAdBean, true)) {
                    isImpressioned = true;
                }

                //OMSDK v1.2, this must be last called, cause muti-called will cause error
                MRAIDView v = ((BannerView)adAdapterManager.getAdView()).getMraidView();
                if (null != v) {
                    v.sendOMImpression();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 请求广告，考虑是否正在显示、有网络连接、是否锁屏
    private void requestAd(int sec) {
        if (((ScheduledThreadPoolExecutor) bannerReqScheduler).getQueue().size() > 0) {
            AdViewUtils.logInfo("queue only need 1 thread,returned. ");
            return;
        }
        //wilder 2019 for change
        if (getVideoMode()) {
            //mrec video
            reFreshTime = -1;
        }

        if (!isFirst) {
            if (-1 == sec)
                return;
            if (isEnded)
                return;
            if (!isShown()) {
                bannerReqScheduler.schedule(new RequestRunable(sec), sec, TimeUnit.SECONDS);
                return;
            }
        }


        bannerReqScheduler.schedule(new RotatedRunnable(sec), sec, TimeUnit.SECONDS);
        isFirst = false;

    }

    public void stopRequest() {
        try {
            isEnded = true;
            bannerReqScheduler.shutdownNow();
            bannerReqScheduler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class RotatedRunnable implements Runnable {
        private int sec = 0;

        public RotatedRunnable(int sec) {
            this.sec = sec;
        }

        @Override
        public void run() {

            if (isEnded) {
                AdViewUtils.logInfo("Banner should be cancel ,isEended=" + isEnded);
                return;
            }
            if (null == bannerReqScheduler || bannerReqScheduler.isTerminated())
                bannerReqScheduler = Executors.newScheduledThreadPool(ConstantValues.REQUEST_THREADPOOL_NUM);

            if (sec == -1)
                return;
            if (!AdViewUtils.isConnectInternet(getContext())) {
                int rotateTime = sec;
                if (rotateTime == -1)
                    rotateTime = 15;
                bannerReqScheduler.schedule(new RequestRunable(rotateTime), rotateTime,
                        TimeUnit.SECONDS);
                return;
            }
            if (AdViewUtils.isScreenLocked(getContext())) {
                AdViewUtils.logInfo("isScreenLocked");
                if (schedulerList.contains(AdBannerBIDView.this))
                    bannerReqScheduler.schedule(new RotatedRunnable(sec * 2), sec * 2,
                            TimeUnit.SECONDS);
                else {
                    for (int i = 0; i < schedulerList.size(); i++) {
                        if (schedulerList.get(i).equals(AdBannerBIDView.this)) {
                            ((AdBannerBIDView) schedulerList.get(i)).bannerReqScheduler
                                    .shutdownNow();
                            schedulerList.remove(i);
                        }
                    }
                }
                return;
            }
            if (!schedulerList.contains(AdBannerBIDView.this)) {
                schedulerList.add(AdBannerBIDView.this);

            }
            String configUrl = getConfigUrl(routeType);
            int sdktype;
            //this sdk type need to upload to server so must obey SDK接口文档
            if(getVideoMode()) {
                //mrec可能承载video,这里没有对size进行限制
                sdktype = ConstantValues.SDK_REQ_TYPE_VIDEO;

            }else if (adSize == ConstantValues.BANNER_REQ_SIZE_MREC) {
                //mrec有特定类型
                sdktype = ConstantValues.SDK_REQ_TYPE_MREC;
            }else {
                //默认是普通banner
                sdktype = ConstantValues.SDK_REQ_TYPE_BANNER;
            }

            applyAdBean = initRequestBean(appID, posID, routeType, sdktype, 1);

            bannerReqScheduler.schedule(
                    new InitAdRunable(
                            makeRequestBeanString(applyAdBean).replace(" ", ""),
                            configUrl,
                            sdktype),
                        0, TimeUnit.SECONDS);
        }
    }

    public void setOpenAnim(boolean a) {
    }


    class RequestRunable implements Runnable {
        private int sec = -1;

        public RequestRunable(int sec) {
            this.sec = sec;
        }

        @Override
        public void run() {
            requestAd(sec);
        }
    }

    @Override
    public void onCloseBtnClicked() {

        //OMSDK v1.2 close session
        MRAIDView v = ((BannerView)adAdapterManager.getAdView()).getMraidView();
        if (null != v) {
            v.stopOMAdSession();
        }
        //for omsdk , stop session need  some time, so webview should be destroyed wait for a while
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null != onAdViewListener) {
                    onAdViewListener.onAdClosedAd(null);
                }
            }
        },100);

    }

    @Override
    public void onViewClicked(MotionEvent e, AgDataBean agDataBean, String url, float downX, float downY) {
        try {
            if (null != agDataBean)
                reportOtherUrls(agDataBean.getCliUrls() + (adAdapterManager.getSufId() == 0 ? "" : "&sufid=" + adAdapterManager.getSufId()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        handleClick(e, (int) downX, (int) downY, url);
    }

    @Override
    public boolean isClickableConfirm() {
        return isClickableConfirm(adsBean);
    }

    @Override
    public void setClickMotion(MRAIDView view, Rect touchRect) {
        setClickMotion(view, adsBean, null);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(String url) {
        return KyAdBaseView.shouldInterceptRequest(url, adsBean, applyAdBean);
    }

    @Override
    public boolean needConfirmDialog() {
        if (respAdBean.getSc() == 1)
            createConfirmDialog(getContext(), adsBean, null, true, null, null);
        return false;
    }

    @Override
    public void checkClick(String url) {
        clickCheck(url, adsBean, applyAdBean, respAdBean);

    }

    @Override
    public void onReady(AgDataBean agDataBean, boolean force) {
        //avoid embed video process to video process
        if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO_EMBED ||
                adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO ||
                adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO_PASTER) {
            AdViewUtils.logInfo("====== AdBannerBIDView(): onReady() MREC video mode ======");
            if (null != onAdViewListener) {
                onAdViewListener.onAdReady(null);
            }
            return;
        }
        if (force) {
            if (null != onAdViewListener) {
                onAdViewListener.onAdReady(null);
            }
        }
        requestAd(reFreshTime);
    }

    @Override
    public void onReceived(AgDataBean agDataBean, boolean force) {
        try {
            if (force) {
                if (null != onAdViewListener)
                    onAdViewListener.onAdRecieved(this);
            }
            if (null != agDataBean && null != agDataBean.getSuccUrls())
                reportOtherUrls(agDataBean.getSuccUrls());
            addAdView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdFailed(AgDataBean agDataBean, String error, boolean force) {
        try {
            if (error.startsWith("CustomError://")) {
                reportLoadError(adsBean, appID, Integer.valueOf(error.replace("CustomError://", "")));
            }
            else {
                if (null != agDataBean && null != agDataBean.getFailUrls())
                    reportOtherUrls(agDataBean.getFailUrls());
                int times = getAgDataBeanPosition(adsBean, agDataBean);
                if (times != -1) {
                    removeAdView();
                    adAdapterManager = handlerAd(false, times, ConstantValues.SDK_REQ_TYPE_BANNER, adsBean.getAgDataBeanList().get(times), this);
                } else {
                    bannerReqScheduler.schedule(new RequestRunable(reFreshTime), reFreshTime,
                            TimeUnit.SECONDS);
                    if (force) {
                        if (null != onAdViewListener)
                            onAdViewListener.onAdRecieveFailed(null, error);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisplay(AgDataBean agDataBean, boolean force) {
        try {
            sendImpression(agDataBean, force);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean getCloseble() {
        return closeAble;
    }

    @Override
    public String getAdLogo() {
        return adLogo;
    }

    @Override
    public String getAdIcon() {
        return adIcon;
    }

    //wilder 20200228
    @Override
    public Bitmap getAdLogoBmp() {
        return adLogoBmp;
    }
    @Override
    public Bitmap getAdIconBmp() {
        return adIconBmp;
    }
    //end wilder 20200228

    @Override
    public AdsBean getAdsBean() {
        return adsBean;
    }

    @Override
    public void rotatedAd(Message msg) {
        try {
            Message msgCopy = Message.obtain(msg);
            if (null == adsBean.getAgDataBeanList()) {
                bannerReqScheduler.schedule(new RequestRunable(reFreshTime), reFreshTime,
                        TimeUnit.SECONDS);
                return;
            }
            if (msgCopy.arg1 < adsBean.getAgDataBeanList().size()) {
                AgDataBean agDataBean = adsBean.getAgDataBeanList().get(msgCopy.arg1);
                removeAdView();
                adAdapterManager = handlerAd(false, msgCopy.arg1, ConstantValues.SDK_REQ_TYPE_BANNER, agDataBean, this);
                return;
            }
            bannerReqScheduler.schedule(new RequestRunable(reFreshTime), reFreshTime,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            bannerReqScheduler.schedule(new RequestRunable(reFreshTime), reFreshTime,
                    TimeUnit.SECONDS);
        }
    }

}
