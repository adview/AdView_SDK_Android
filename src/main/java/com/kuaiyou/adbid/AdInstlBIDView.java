package com.kuaiyou.adbid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceResponse;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.interfaces.KyInstalListener;
import com.kuaiyou.interfaces.OnAdViewListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.mraid.interfaces.MRAIDNativeFeatureListener;
import com.kuaiyou.mraid.interfaces.MRAIDViewListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ApplyAdBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.InstlView;
import com.kuaiyou.utils.SharedPreferencesUtils;
import com.kuaiyou.video.AdViewVideoInterface;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 插屏竞价
 */
public class AdInstlBIDView extends KyAdBaseView implements MRAIDViewListener, MRAIDNativeFeatureListener, KyInstalListener {

    private ApplyAdBean applyAdBean = null;
    private String bitmapPath = null;
    private boolean canClosed = true;
    private AdsBean cacheAdsBean = null;
    private String adLogo;
    private String adIcon;
    private int displayMode = AdInstlBIDView.DISPLAYMODE_DEFAULT;

    public final static int DISPLAYMODE_DEFAULT = 0;
    public final static int DISPLAYMODE_POPUPWINDOWS = 1;
    public final static int DISPLAYMODE_DIALOG = 2;

    private AdAdapterManager adAdapterManager;
    private boolean isLoaded = false;
    //private int defaultINSTL_SIZE = ConstantValues.INSTL_320X480; //ConstantValues.INSTL_SIZE

    private AdInstlBIDView(Context context, String appID, int routeType, int sdkType, int adSize,
                           boolean canClosed, String gdpr) {
        super(context);
        String configUrl;
        String cacheData = null;
        long cacheTime;
        super.adSize = adSize; //ConstantValues.INSTL_SIZE;
        this.canClosed = canClosed;
        setGDPRConstent(gdpr);   //GDPR
        calcAdSize();
//        AdViewUtils.setServerUrl(isRtb);
        if (AdViewUtils.cacheMode) {
            SharedPreferences preferences = SharedPreferencesUtils
                    .getSharedPreferences(getContext(), ConstantValues.SP_INSTLINFO);
            cacheTime = preferences.getLong("sp_cacheTime", 0);
            if (System.currentTimeMillis() / 1000 - cacheTime < ConstantValues.DEFAULTCACHEPEROID) {
                cacheData = preferences.getString("sp_cacheData", null);
            }
        }
        configUrl = getConfigUrl(routeType);

        applyAdBean = initApplyBean(appID, null, routeType, sdkType, 1);

        if (TextUtils.isEmpty(cacheData)) {
            reqScheduler.execute(new InitAdRunable(
                    getApplyInfoContent(applyAdBean),
                    configUrl,
                    ConstantValues.INSTLTYPE));
        }
        else {
            reqScheduler.execute(new InitAdRunable(cacheData, ConstantValues.INSTLTYPE));
        }

        SharedPreferences sp = SharedPreferencesUtils.getSharedPreferences(getContext(), ConstantValues.SP_ADVINFO);
        // 插屏缓存模式开关
//        if (AdViewUtils.cacheMode) {
//            if (sp.getInt("html5", 0) != 1)
//                if (initCacheView(ConstantValues.INSTLTYPE)) {
//                    cacheAdsBean = AdsBean.copyAdsBean(adsBean);
//                    if (TextUtils.isEmpty(cacheAdsBean.getXhtml()))
//                        createInstlDialog(cacheAdsBean);
//                }
//        }
    }

    public AdInstlBIDView(Context context, String key, int type, boolean canClosed, String gdpr) {
        this(context, key, type, ConstantValues.INSTLTYPE, ConstantValues.INSTL_320X480, canClosed, gdpr);
    }

    public AdInstlBIDView(Context context, String key, boolean canClosed, String gdpr) {
        this(context, key, ConstantValues.ADBID_TYPE, ConstantValues.INSTLTYPE, ConstantValues.INSTL_320X480, canClosed, gdpr);
    }

    //new adsize passed
    public AdInstlBIDView(Context context, String key, boolean canClosed, int adSize, String gdpr) {
        this(context, key, ConstantValues.ADBID_TYPE, ConstantValues.INSTLTYPE, adSize, canClosed, gdpr);
    }

    /**
     * 设置插屏展现方式 dialog 或者 popupwindows
     *
     * @param displayMode
     */
    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }
    /**
     * 自定义插屏调用方法
     * 手动汇报展示
     */
    public void reportImpression() {
        if (System.currentTimeMillis() - adsBean.getDataTime() <= ConstantValues.AD_EXPIRE_TIME) {
            reportImpression(cacheAdsBean, retAdBean, applyAdBean, true);
        }
        else {
            AdViewUtils.logInfo("AD_EXPIRED - IMPRESSION WILL NOT RUN");
        }
    }
    /**
     * 自定义插屏调用方法
     * 手动点击汇报
     */
    public void reportClick() {
        try {
            if (null == adAdapterManager || null == adAdapterManager.getInstlView())
                return;
            InstlView instlView = (InstlView) adAdapterManager.getInstlView();
            if (System.currentTimeMillis() - adsBean.getDataTime() <= ConstantValues.AD_EXPIRE_TIME) {
                if (adsBean.getAdType() == ConstantValues.MIXED) {
                    clickMotion(null, -2, -2);
                    clickEvent(getContext(), cacheAdsBean, cacheAdsBean.getAdLink());
                } else {
                    long downTime = SystemClock.uptimeMillis();
                    //模拟随机位置
                    int detla = (int) (50 * Math.random());
                    if (downTime % 2 == 0) {
                        detla = -detla;
                    }
                    MotionEvent motionDown = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN,
                                        (adShowWidth / 2) + detla, (adShowHeight / 2) + detla, 0);//adHeight-((adHeight*140)/(600*2))
                    MotionEvent motionUp = MotionEvent.obtain(downTime, downTime + 100 + Math.abs(detla),
                                            MotionEvent.ACTION_UP, (adShowWidth / 2) + detla, (adShowHeight / 2) + detla, 0);
                    instlView.dispatchTouchEvent(motionDown);
                    instlView.dispatchTouchEvent(motionUp);
                    motionDown.recycle();
                    motionUp.recycle();
                }
            } else
                AdViewUtils.logInfo("AD_EXPIRED - CLICK WILL NOT RUN");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 自定义插屏调用方法
     * 获取当前插屏视图view
     */
    public View getDialogView() {
        if (null != adAdapterManager)
            return adAdapterManager.getDialogView();
        return null;
    }

    /**
     * 自定义插屏调用方法
     * 获取当前插屏视图view的宽
     */
    public int getInstlWidth() {
        return adAdapterManager.getInstlWidth();
    }
    /**
     * 自定义插屏调用方法
     * 获取当前插屏视图view的高
     */
    public int getInstlHeight() {
        return adAdapterManager.getInstlHeight();
    }
    /**
     * 展示插屏广告
     *
     * @param activity
     * @return 展示成功返回true 否则false
     */
    public boolean showInstl(final Activity activity) {
        try {
            if (null != adAdapterManager) {
                return adAdapterManager.showInstl(activity);
            }
            if (adsBean.getAit() == 2) {
                //(wilder 2019) here maybe change in embed-vastview
                AdVideoBIDView.getInstance(activity == null ? getContext() : activity).playVideo(activity == null ? getContext() : activity);
                return true;
            }

            if (null != onAdInstlListener) {
                onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this,
                                "show instl error adapter is=" + adAdapterManager + " adsben ait=" + adsBean.getAit());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 手动调用 关闭插屏
     */
    public void closeInstl() {

    }

    /**
     * 资源释放
     */
//    public void destroy() {
//        try {
//            AdViewUtils.logInfo("instl destroy");
//            if (null != instlView) {
//                if (null != instlView.getImageView())
//                    instlView.getImageView().stopLoading();
//                if (null != instlView.getMraidView())
//                    if (null != instlView.getMraidView().getMraidWebView())
//                        instlView.getMraidView().getMraidWebView().stopLoading();
//            }
//            onAdInstlListener = null;
////            adsBean = null;
////            cacheAdsBean = null;
////            retAdBean = null;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    private void clickMotion(MotionEvent event, int realX, int realY) {
        if (isClickableConfirm(adsBean)) {
            int isMissTouch = reportClick(event, realX, realY, applyAdBean, cacheAdsBean, retAdBean);// 先发汇报
            // 如果点击为误点击就不通知开发者 -手机电视需求
            if (isMissTouch == ConstantValues.CLICKNORMAL) {
                if (null != onAdInstlListener) {
                    onAdInstlListener.onAdClicked(AdInstlBIDView.this);
                }
            }
        }
    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
//    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////    KyAdBaseView   //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected boolean initAdLogo(Object object) {
        AdsBean adsBean = (AdsBean) object;
        if (null == adsBean)
            return false;
        if (!TextUtils.isEmpty(adsBean.getAdLogoUrl()))
            adLogo = (String) AdViewUtils.getInputStreamOrPath(
                    getContext(), adsBean.getAdLogoUrl(), 1);
//        else
//            adLogo = "/assets/logo_instl.png";
        if (!TextUtils.isEmpty(adsBean.getAdIconUrl()))
            adIcon = (String) AdViewUtils.getInputStreamOrPath(
                    getContext(), adsBean.getAdIconUrl(), 1);
//        else
//            adIcon = "/assets/icon_ad.png";
        return true;
    }

    /**
     * 联网生成图片
     *
     * @param object adsBean
     */
    @Override
    protected boolean createBitmap(Object object) {
        try {
            AdsBean currentAdsBean = (AdsBean) object;
            switch (currentAdsBean.getAdType()) {
                case ConstantValues.MIXED:
                    if (null != currentAdsBean.getAdIcon()
                            && !currentAdsBean.getAdIcon().trim().equals("")) {
                        String[] pics = currentAdsBean.getAdIcon().split(",");
                        SharedPreferences preferences = getContext()
                                .getSharedPreferences(
                                        ConstantValues.SP_BITMAPMAPPING,
                                        Context.MODE_PRIVATE);
                        bitmapPath = preferences.getString(pics[0], null);
                        if (null != bitmapPath && bitmapPath.length() > 0
                                && new File(bitmapPath).exists())
                            return true;
                        else {
                            bitmapPath = (String) AdViewUtils.getInputStreamOrPath(
                                    getContext(), currentAdsBean.getGetImageUrl()
                                            + pics[0], 1);
                            if (null != bitmapPath) {
                                Editor editor = preferences.edit();
                                editor.putString(pics[0], bitmapPath);
                                editor.apply();
                                return true;
                            }
                        }
                    }
                    return true;
                default:
                    if (adsBean.getAit() == 2) {
                        return true;
                    }
                    if (null != currentAdsBean.getAdPic()
                            && !currentAdsBean.getAdPic().trim().equals("")) {
                        SharedPreferences preferences = getContext().getSharedPreferences(
                                        ConstantValues.SP_BITMAPMAPPING,
                                        Context.MODE_PRIVATE);
                        String pic = currentAdsBean.getAdPic();
                        bitmapPath = preferences.getString(currentAdsBean.getAdPic(), null);
                        //(wilder 2019) for online show bitmap
                        if ( AdViewUtils.bitmapOnLine) {
                            bitmapPath = pic;
                        }else {
                            //download pic
                            if (null != bitmapPath && bitmapPath.length() > 0
                                    && new File(bitmapPath).exists()) {
                                return true;
                            }
                            else {
                                bitmapPath = (String) AdViewUtils.getInputStreamOrPath(
                                        getContext(), currentAdsBean.getGetImageUrl()
                                                + currentAdsBean.getAdPic(), 1);
                                if (null != bitmapPath) {
                                    Editor editor = preferences.edit();
                                    editor.putString(currentAdsBean.getAdPic(), bitmapPath);
                                    editor.apply();
                                    return true;
                                }
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null != bitmapPath;
    }

    @SuppressLint("NewApi")
    @Override
    protected void handlerMsgs(Message msg) {

        switch (msg.what) {
            case ConstantValues.NOTIFYRECEIVEADOK:
                // registerBroadcast(this);
                if (null == cacheAdsBean)
                    cacheAdsBean = AdsBean.copyAdsBean(adsBean);
                switch (cacheAdsBean.getAdType()) {
                    case ConstantValues.FULLIMAGE:
                    case ConstantValues.HTML:
                    case ConstantValues.MIXED:
                    case ConstantValues.INSTL:
                        try {
                            if (adsBean.getAit() == 2) {
                                if (AdViewUtils.checkClickPermission(getContext(), ConstantValues.VIDEOACTIVITY_DECLARATIONS, PackageManager.GET_ACTIVITIES)) {
                                    AdVideoBIDView.getInstance(getContext()).init(applyAdBean.getAppId(), "",
                                            adsBean.getRawData(), new AdViewVideoInterface() {
                                                @Override
                                                public void onReceivedVideo(String vast) {
                                                    if (null != onAdInstlListener)
                                                        onAdInstlListener.onAdRecieved(AdInstlBIDView.this);
                                                }
                                                @Override
                                                public void onFailedReceivedVideo(String error) {
                                                    if (null != onAdInstlListener)
                                                        onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, error);
                                                }
                                                @Override
                                                public void onVideoReady() {
                                                    if (null != onAdInstlListener)
                                                        onAdInstlListener.onAdReady(AdInstlBIDView.this);
                                                }
                                                @Override
                                                public void onVideoStartPlayed() {
                                                }
                                                @Override
                                                public void onVideoFinished() {
                                                }
                                                @Override
                                                public void onVideoClosed() {
                                                    if (null != onAdInstlListener)
                                                        onAdInstlListener.onAdClosedAd(AdInstlBIDView.this);
                                                }
                                                @Override
                                                public void onPlayedError(String error) {
                                                    if (null != onAdInstlListener)
                                                        onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, error);
                                                }
                                                @Override
                                                public int getOrientation() {
                                                    return -1;
                                                }

                                            });
                                    return;
                                } else {
                                    if (null != onAdInstlListener)
                                        onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this,
                                                "VideoActivtiy not declared");
                                    return;
                                }
                            }

                            adAdapterManager = handlerAd(true, -1, ConstantValues.INSTLTYPE, null, this);

                            if (null != onAdInstlListener) {
                                onAdInstlListener.onAdRecieved(this);
                            }
                            if (null != onAdInstlListener) {
                                onAdInstlListener.onAdReady(this);
                            }
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (null != onAdInstlListener) {
                                onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, "unknown error");
                            }
                        }
                }
                break;
            case ConstantValues.NOTIFYRECEIVEADERROR:
                String failedMsg = "";
                try {
                    if (null != adsBean && null != adsBean.getAgDataBean() && !TextUtils.isEmpty(adsBean.getAgDataBean().getAggsrc())) {
                        adAdapterManager = handlerAd(false, -1, ConstantValues.INSTLTYPE, adsBean.getAgDataBean(), this);
                        return;
                    }
                    if (null == msg.obj)
                        msg.obj = "";
                    failedMsg = String.valueOf(msg.obj);
                    if (failedMsg.equals(""))
                        failedMsg = "UNKNOW_ERROR";

                    if (null != onAdInstlListener) {
                        onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, failedMsg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (null != onAdInstlListener) {
                        onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, "unknown error");
                    }
                }
                break;
        }
    }


    @Override
    public void setOnAdInstlListener(OnAdViewListener onAdViewListener) {
        super.setOnAdInstlListener(onAdViewListener);
    }

    @Override
    protected void handleClick(MotionEvent event, int realX, int realY, String url) {
        clickMotion(event, realX, realY);
        clickEvent(getContext(), cacheAdsBean, url);
    }

    @Override
    public String getBitmapPath() {
        return bitmapPath;
    }

    @Override
    public boolean isClickableConfirm() {
        return false;
    }

    @Override
    public void setClickMotion(MRAIDView view, Rect touchRect) {
        setClickMotion(view, adsBean, null);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(String url) {
        return null;
    }

    @Override
    public boolean needConfirmDialog() {
        if (retAdBean.getSc() == 1)
            createConfirmDialog(getContext(), adsBean, null, true, null, null);
        return false;
    }

    @Override
    public void checkClick(String url) {
        clickCheck(url, adsBean, applyAdBean, retAdBean);
    }

    @Override
    public void onReady(AgDataBean agDataBean, boolean force) {
        if (null != onAdInstlListener) {
            onAdInstlListener.onAdReady(AdInstlBIDView.this);
        }
    }
    @Override
    public void onReceived(AgDataBean agDataBean, boolean force) {
        try {
            if (null != onAdInstlListener) {
                onAdInstlListener.onAdRecieved(AdInstlBIDView.this);
            }
            if (null != agDataBean && null != agDataBean.getSuccUrls())
                reportOtherUrls(agDataBean.getSuccUrls());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdFailed(AgDataBean agDataBean, String error, boolean force) {
        try {
            if (error.startsWith("CustomError://")) {
                reportLoadError(adsBean, applyAdBean.getAppId(), Integer.valueOf(error.replace("CustomError://", "")));
            }
            else {
                if (null != agDataBean && null != agDataBean.getFailUrls()) {
                    reportOtherUrls(agDataBean.getFailUrls());
                }
                int times = getAgDataBeanPosition(adsBean, agDataBean);
                if (times != -1) {
                    adAdapterManager = handlerAd(false, times, ConstantValues.INSTLTYPE, adsBean.getAgDataBeanList().get(times), this);
                    return;
                }
                if (null != onAdInstlListener) {
                    onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisplay(AgDataBean agDataBean, boolean force) {
        if (null != agDataBean && null != agDataBean.getImpUrls()) {
            reportOtherUrls(agDataBean.getImpUrls());
        }
        reportImpression(cacheAdsBean, retAdBean, applyAdBean, true);

        if (null != onAdInstlListener) {
            onAdInstlListener.onAdDisplayed(AdInstlBIDView.this);
        }
    }
    @Override
    public boolean getCloseble() {
        return canClosed;
    }
    @Override
    public void onCloseBtnClicked() {
        if (null != onAdInstlListener) {
            onAdInstlListener.onAdClosedAd(AdInstlBIDView.this);
            onAdInstlListener = null;
        }
    }
    @Override
    public void onViewClicked(MotionEvent e, AgDataBean agDataBean, String url, float downX, float downY) {
        AdViewUtils.logInfo("onViewClicked  ");
        if (downX == 888 && downY == 888) {
            if (null != agDataBean) {
                reportOtherUrls(agDataBean.getCliUrls());
            }
            if (null != onAdInstlListener) {
                onAdInstlListener.onAdClicked(AdInstlBIDView.this);
            }
            return;
        }
        adsBean.setAction_down_x((int) downX);
        adsBean.setAction_down_y((int) downY);
        adsBean.setAction_up_x((int) downX);
        adsBean.setAction_up_y((int) downY);

        // 接口调用 点击汇报发送方法
        if (isClickableConfirm(adsBean)) {
            if (retAdBean.getSc() == 1) {
                createConfirmDialog(getContext(), adsBean, null, false, null, null);
            } else {
                long actionTime = System.currentTimeMillis();
                MotionEvent motionUp = MotionEvent.obtain(actionTime, actionTime, MotionEvent.ACTION_UP, downX, downY, 99);
                handleClick(motionUp, adsBean.getAction_up_x(), adsBean.getAction_up_y(), null);
            }
        }
    }

    @Override
    public String getAdLogo() {
        return adLogo;
    }

    @Override
    public String getAdIcon() {
        return adIcon;
    }

    @Override
    public AdsBean getAdsBean() {
        return adsBean;
    }

    @Override
    public void rotatedAd(Message msg) {
        try {
            Message msgCopy = Message.obtain(msg);
            if (null == adsBean.getAgDataBeanList()) {
                if (null != onAdInstlListener) {
                    onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, "backup list is null");
                }
                return;
            }
            if (msgCopy.arg1 < adsBean.getAgDataBeanList().size()) {
                AgDataBean agDataBean = adsBean.getAgDataBeanList().get(msgCopy.arg1);
                adAdapterManager = handlerAd(false, msgCopy.arg1, ConstantValues.INSTLTYPE, agDataBean, this);
            }
        } catch (Exception e) {
            if (null == adsBean.getAgDataBeanList()) {
                if (null != onAdInstlListener)
                    onAdInstlListener.onAdRecieveFailed(AdInstlBIDView.this, "rotated error");
            }
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// ---- KyInstalListener ---- //////////////////////////////////////
    @Override
    public void onVisiblityChange(int visible) {
        if (0 == visible) {
            if (System.currentTimeMillis() - adsBean.getDataTime() > ConstantValues.AD_EXPIRE_TIME) {
                closeInstl();
            }
        }
    }
    @Override
    public int getDisplayMode() {
        return displayMode;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// --- MRAIDNativeFeatureListener --- ///////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void mraidNativeFeatureDownload(String url) {
        clickCheck(url, adsBean, applyAdBean, retAdBean);
    }
    @Override
    public void mraidNativeFeatureCallTel(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL); // android.intent.action.DIAL
        intent.setData(Uri.parse(url));
        getContext().startActivity(intent);
    }
    @Override
    public void mraidNativeFeatureOpenDeeplink(String url) {
        if (url.startsWith("mraid")) {
            try {
                url = URLDecoder.decode(url.replace("mraid://openDeeplink?url=", ""), "UTF-8");
                adsBean.setDeeplink(url);
                clickCheck(url, adsBean, applyAdBean, retAdBean);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void mraidNativeFeatureOpenBrowser(String url) {
        clickCheck(url, adsBean, applyAdBean, retAdBean);
    }
    @Override
    public void mraidNativeFeatureSendSms(String url) {
        AdViewUtils.sendSms(getContext(), url);
    }
    @Override
    public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {
    }
    @Override
    public void mraidNativeFeatureStorePicture(String url) {
    }

    /* **************************************************************************************
    -----------------------------    MRAIDViewListener   -----------------------------
     *************************************************************************************** */
    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
    }
    @Override
    public void mraidViewExpand(MRAIDView mraidView) {
    }
    @Override
    public void mraidViewClose(MRAIDView mraidView) {
    }
    @Override
    public boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX,
                                   int offsetY) {
        return false;
    }
    @Override
    public void onShouldOverride(String url) {
        // 至少触摸过才可以点击跳转
        if (adsBean.getTouchStatus() > MRAIDView.ACTION_DEFAULT) {
            clickCheck(url, adsBean, applyAdBean, retAdBean);
//                }
//            }
        }
    }
    @Override
    public WebResourceResponse onShouldIntercept(String url) {
        return KyAdBaseView.shouldInterceptRequest(url, adsBean, applyAdBean);
    }
    @Override
    public void loadDataError(int errorType) {
        reportLoadError(adsBean, applyAdBean.getAppId(), errorType);
    }

}