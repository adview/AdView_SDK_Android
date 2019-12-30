package com.kuaiyou.adbid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.ApplyAdBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.SharedPreferencesUtils;
import com.kuaiyou.video.AdVASTView;
import com.kuaiyou.interfaces.AdViewVideoInterface;
import java.util.concurrent.TimeUnit;

public class AdVideoBIDView extends KyAdBaseView {
    /**
     * *******************************************
     */
    private final static int MESSAGE_FAILED = 2;
    private final static int MESSAGE_SUCCEED = 1;
    private final static int MESSAGE_PLAYENDED = 3;
    private final static int MESSAGE_PLAYSTART = 4;
    private final static int MESSAGE_CLOSE = 5;
    private final static int MESSAGE_FAILED_NOCLASS = 6;

    public final static int RESULT_CODE_OK = 1;
    public final static int RESULT_CODE_ERROR = 0;

    private boolean isReady = false;
    private boolean isPlaying, isProcessing;

    private boolean isPaster = false;
    private int videoType;  //normal or paste
    private int reTryTime = 30; //30 sec when disconnnect
//    private static int cacheTime = 30;
    private String vastStr = "hello,vast";
    //private AdViewVideoInterface appVideoInterface;
    private ApplyAdBean applyAdBean;

    private String appID;
    private String posID;

    private String bitmapPath;

    private Context context;

    private AdAdapterManager adAdapterManager;

    public static AdVideoBIDView instance;
    private AdVASTView mvastView;

    /* (wilder 2019) for newer*/
    public static AdVideoBIDView getInstance(Context context) {
        if (null == instance) {
            instance = new AdVideoBIDView(context);
        }
        instance.context = context;
        return instance;
    }

    public AdVideoBIDView(Context context) {
        super(context);
        isProcessing = false;
        this.context = context;
    }

    public void init(String appId, String posId, boolean isPaster) {
        if (isProcessing) {
            AdViewUtils.logInfo("video request is processing");
            return;
        }
        this.isPaster = isPaster;
        appID = appId;
        posID = posId;
        if(isPaster) {
            videoType = VIDEO_ADS_PASTER;
        }else {
            videoType = VIDEO_ADS_NORMAL;
        }

        KyAdBaseView.registerBatteryReceiver(context);
        isProcessing = true;

        //got gpid
        AdViewUtils.getDeviceIdFirstTime(context, this);
        //requestAd();

    }

    public void init(String appId, String posId, String data, AdViewVideoInterface appInterface) {
        if (isProcessing) {
            AdViewUtils.logInfo("video request is processing");
            return;
        }
        KyAdBaseView.registerBatteryReceiver(context);
        isProcessing = true;
        setVideoAppListener(appInterface);

        //got gpid
        AdViewUtils.getDeviceIdFirstTime(context, this);
        //requestAd();
    }

    public AdAdapterManager getAdAdapterManager() {
        return adAdapterManager;
    }

    public AdViewVideoInterface getVideoAppListener() {
        return appVideoInterface;
    }

    @Override
    public void setVideoAppListener(AdViewVideoInterface appInterface) {
        super.setVideoAppListener(appInterface);
    }

    @Override
    protected void handlerMsgs(Message msg) {
        try {
            Bundle bundle;
            if (null == adsBean && msg.what != ConstantValues.NOTIFY_REQ_GPID_FETCH_DONE) {
                onFailedReceived(null, MESSAGE_FAILED, "NO_FILL (没有填充）, video mode ");
                return;
            }
            switch (msg.what) {
                case ConstantValues.NOTIFY_REQ_GPID_FETCH_DONE:
                    requestAd();
                    break;
                case ConstantValues.NOTIFY_RESP_RECEIVEAD_OK:
                    // onAdRecieved
                    if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO_PASTER && !super.selfTestMode_VIDEO ) {
                        //for test mode, paste video can also be handled
                        onReceivedVideo(adsBean, -1);
                    }else {
                        adAdapterManager = handlerAd(true, -1, ConstantValues.SDK_REQ_TYPE_VIDEO, null, null);
                    }

                    break;
                case ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR:
                    String failedMsg = "";
                    try {
                       if (null != adsBean && null != adsBean.getAgDataBean() && !TextUtils.isEmpty(adsBean.getAgDataBean().getAggsrc())) {
                            //(wilder 2019) DADI 打底逻辑,此处isBid=false,最后一个参数是AdVGListener，是否需要在实现打底视频的时候再做修正
                            adAdapterManager = handlerAd(false, -1, ConstantValues.SDK_REQ_TYPE_VIDEO, adsBean.getAgDataBean(), null);
                            return;
                        }
                        // 如果刷新时间不等于-1 需要再次请求广告
                        //if (reFreshTime != -1)
                        //    requestAd(reFreshTime);
                        failedMsg = String.valueOf(msg.obj);
                        if (failedMsg.equals("")) {
                            failedMsg = "UNKNOW_ERROR";
                        }
                        onFailedReceived(null, MESSAGE_FAILED, failedMsg);

                    } catch (Exception e) {
                        e.printStackTrace();
                        failedMsg = e.toString();

                        onFailedReceived(null, MESSAGE_FAILED, failedMsg);
                    }

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailedReceived(null, MESSAGE_FAILED, e.toString());

        }
    }

    public void setVideoBackgroundColor(String color) {
        if (color.startsWith("#") && (color.length() == 7 || color.length() == 9))
            bgColor = color;
        else
            AdViewUtils.logInfo("color is not valid");
    }

    public void setTrafficWarnEnable(boolean trafficWarnEnable) {
        trafficWarnEnable = trafficWarnEnable;
    }

    public void setAutoPlay(boolean enable ) {
        AdViewUtils.videoAutoPlay = enable;
    }

    public void autoCloseEnable(boolean enable) {
        autoCloseAble = enable;
    }
    public void setVideoOrientation(int orientation) {
        videoOrientation = orientation;
    }

    /*********************************************************/
    /**
     * 获取视屏广告播放地址，仅限于贴片广告
     *
     * @return urls
     */
    public String getVideoVast() {
        return vastStr;
    }

    public void playVideo(Context context) {
        AdViewUtils.logInfo("<----------------   ADVideoBIDView(): playVideo ------------->  " );
//        if (mvastView != null) {
//            mvastView.playVideo(context);
//        }
        //(wilder 2019) just covered, play video can by user , but now is auto play after vast parse done
        if (!AdViewUtils.videoAutoPlay && adAdapterManager != null) {
            adAdapterManager.playVideo(context);
        }
    }

    //wilder 2019 for if video activity closed
    public void resetState() {
        isProcessing = false;
    }

    private void onReceivedVideo(AdsBean adsBean, int cacheTime) {
        //this.adsBean = adsBean;
        String vast = adsBean.getVastXml();
        if (!TextUtils.isEmpty(vast)) {
            AdViewUtils.logInfo("ADVideoBIDView(): --> onReceivedVideo()  " );
            if (null != appVideoInterface) {
                appVideoInterface.onReceivedVideo(vast);
            }
        }
        //成功回调
    }

    private void onFailedReceived(AdsBean adsBean, int code, String message) {
        if (message.equals("VideoActivity NOT Found")) {
            isProcessing = false;
            isReady = false;
            AdViewUtils.logInfo("AdViewVideoActivity not found in manifest");
            return;
        }

        //打底功能未能走到 失败处理
        isProcessing = false;
        isReady = false;
        AdViewUtils.logInfo("[VideoBIDView]onFailedReceived = " + code + "   " + message);
        if (null != appVideoInterface) {
            appVideoInterface.onFailedReceivedVideo(message);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////wilder 2019 for new arch video BID mode ///////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 请求广告，考虑是否正在显示、有网络连接、是否锁屏
    private void requestAd() {
        /*
        if (((ScheduledThreadPoolExecutor) scheduler).getQueue().size() > 0) {
            AdViewUtils.logInfo("queue only need 1 thread,returned. ");
            return;
        }
        */
        bannerReqScheduler.execute(new RequestRunable(videoType));

    }

    class RequestRunable implements Runnable {
        private int videoType; //normal or paste

        public RequestRunable(int videotype) {
            videoType = videotype;
        }

        @Override
        public void run() {

            if (!AdViewUtils.isConnectInternet(getContext())) {
                bannerReqScheduler.schedule(new RequestRunable(videoType), reTryTime, TimeUnit.SECONDS);
                return;
            }
            SharedPreferences preferences = SharedPreferencesUtils.getSharedPreferences(context, "video_req");

            if (null != preferences) {
                long reqTime = preferences.getLong("time_req", 0);
                long currentTime = System.currentTimeMillis();
                if (currentTime - reqTime <= 3 * 1000) {
                    notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR,"请求间隔过短");
                    return;
                }
            }
            String configUrl = getConfigUrl(ConstantValues.ROUTE_ADBID_TYPE);
            //video type may normal / paste
            applyAdBean = initRequestBean(appID, posID, ConstantValues.ROUTE_ADBID_TYPE, ConstantValues.SDK_REQ_TYPE_VIDEO, 1);

            bannerReqScheduler.schedule(
                    new InitAdRunable(makeRequestBeanString(applyAdBean).replace(" ", ""),
                            configUrl,
                            ConstantValues.SDK_REQ_TYPE_VIDEO),
                    0, TimeUnit.SECONDS);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// from KyAdBaseView.java ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    String adLogo, adIcon;
    @Override
    protected boolean initAdLogo(Object object) {
        if (!TextUtils.isEmpty(adsBean.getAdLogoUrl())) {
            adLogo = (String) AdViewUtils.getInputStreamOrPath(
                    getContext(), adsBean.getAdLogoUrl(), 1);
        }
        if (!TextUtils.isEmpty(adsBean.getAdIconUrl())) {
            adIcon = (String) AdViewUtils.getInputStreamOrPath(
                    getContext(), adsBean.getAdIconUrl(), 1);
        }
//        else
//            adIcon = "/assets/icon_ad.png";
        return true;
    }

    @Override
    protected boolean createBitmap(Object object) {
        switch (adsBean.getAdType()) {
            case ConstantValues.RESP_ADTYPE_VIDEO:
                if (!TextUtils.isEmpty(adsBean.getAdIcon()))
                    return true;
            default:
                if (!TextUtils.isEmpty(adsBean.getAdPic()))
                    return true;
        }
        return false;
    }

    @Override
    protected void handleClick(MotionEvent e, int realX, int realY, String url) {

        //will be handled by vast video
    }
    @Override
    public String getBitmapPath() {
        return bitmapPath;
    }
}
