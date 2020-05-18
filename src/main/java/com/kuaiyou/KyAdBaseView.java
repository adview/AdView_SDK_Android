package com.kuaiyou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.adbid.AdSpreadBIDView;
import com.kuaiyou.interfaces.AdVGListener;
import com.kuaiyou.interfaces.AdViewVideoInterface;
import com.kuaiyou.interfaces.NativeAdCallBack;
import com.kuaiyou.interfaces.OnAdViewListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ApplyAdBean;
import com.kuaiyou.obj.GDPRBean;
import com.kuaiyou.obj.NativeAdBean;
import com.kuaiyou.obj.RetAdBean;
import com.kuaiyou.obj.VideoBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ClientReportRunnable;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.interfaces.DownloadConfirmInterface;
import com.kuaiyou.utils.MD5Utils;
import com.kuaiyou.utils.SharedPreferencesUtils;
import com.kuaiyou.utils.AdViewLandingPage;
import com.kuaiyou.utils.DownloadService;
//import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
//import com.tencent.mm.opensdk.openapi.IWXAPI;
//import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

/**
 * 竞价广告基类
 */
public abstract class KyAdBaseView extends RelativeLayout {

    ////////////////////wilder 2019 test module//////////////////////////
    protected static boolean selfTestMode_downlink_fake_local_PDU = false; //wilder 2019 for test PDU, can be any pdu mode, replace download PDU in Instl mode
    protected static boolean selfTestMode_downlink_fake_adsMode = false; //ads mode only , the upper define must be true

    protected static boolean selfTestMode_VIDEO = false; //for video type, can test paste / normal video for all
    protected static boolean selfTestMode_mrecVideo = false; //only test mode can accept all kinds of video type PDU
    protected static boolean selfTestMode_Spread = false; //spead test, for spread timeout not countdown
    protected static String testPDUPath = "test/";
    protected static String testADSPath = "test/ads-segment/xs/";
    /////////////////////end wilder for test ////////////////////////////
    //HK替换字段，主要用于上报状态
    protected final static String HK_CLICKAREA = "{CLICKAREA}";
    protected final static String HK_RELATIVE_COORD = "{RELATIVE_COORD}";
    protected final static String HK_ABSOLUTE_COORD = "{ABSOLUTE_COORD}";
    protected final static String HK_LONGITUDE = "{LONGITUDE}";
    protected final static String HK_LATITUDE = "{LATITUDE}";
    protected final static String HK_UUID = "{UUID}";

    protected final static String HK_GDT_DOWN_X = "__DOWN_X__";
    protected final static String HK_GDT_DOWN_Y = "__DOWN_Y__";
    protected final static String HK_GDT_UP_X = "__UP_X__";
    protected final static String HK_GDT_UP_Y = "__UP_Y__";

    protected final static String HK_DURATION = "__DURATION__";
    protected final static String HK_BEGINTIME = "__BEGINTIME__";
    protected final static String HK_ENDTIME = "__ENDTIME__";
    protected final static String HK_FIRST_FRAME = "__FIRST_FRAME__";
    protected final static String HK_LAST_FRAME = "__LAST_FRAME__";
    protected final static String HK_SCENE = "__SCENE__";
    protected final static String HK_TYPE = "__TYPE__";
    protected final static String HK_BEHAVIOR = "__BEHAVIOR__";
    protected final static String HK_STATUS = "__STATUS__";

    public static ScheduledExecutorService reqScheduler;
    public ScheduledExecutorService bannerReqScheduler;

    public final static String confirmDialog_PositiveButton = "OK";
    public final static String confirmDialog_NegativeButton = "Cancel";
    public final static String confirmDialog_Title = "Title";
    public final static String confirmDialog_Message = "See More Details ?";
    //for video
    public final static int VIDEO_ADS_NORMAL = 6;
    public final static int VIDEO_ADS_PASTER = 7;
    // 横幅广告颜色常量定义
    public static final String[] COLOR_KEYS = {
            ConstantValues.MIXED_PARENTBACKGROUND_COLOR,
            ConstantValues.MIXED_ICONBACKGROUND_COLOR,
            ConstantValues.MIXED_BEHAVEBACKGROUND_COLOR,
            ConstantValues.MIXED_TITLEBACKGROUND_COLOR,
            ConstantValues.MIXED_SUBTITLEBACKGROUND_COLOR,
            ConstantValues.MIXED_KEYWORDBACKGROUND_COLOR};
    // 横幅广告颜色池 - 背景色
    public static final String[][] COLOR_SETS = {
            {"#000000", "#252525", "#0876C1", "#ffffff", "#ffffff", "#00ffc6"},
            {"#76BBDC", "#D7C48C", "#19649B", "#ffffff", "#feeb9a", "#15fa04"},
            {"#9E8F88", "#BEBABB", "#3C373B", "#ffffff", "#ffffff", "#fb011e"},
            {"#5E6F8D", "#8BA1C8", "#33383E", "#ffffff", "#8f7a51", "#ff4301"},
            {"#3C5559", "#7B9091", "#111F20", "#ffffff", "#0099cc", "#ff0000"},
            {"#7D4E62", "#A1969A", "#474143", "#ffffff", "#dbedff", "#9bedff"}};
    /**
     * 外部接口 用于独立竞价与开发者沟通
     */
    public OnAdViewListener onAdSpreadListener = null;
    public OnAdViewListener onAdViewListener = null;
    public OnAdViewListener onAdInstlListener = null;
    public AdViewVideoInterface appVideoInterface = null;
    public NativeAdCallBack nativeAdCallBack = null;

    public static SpreadSettleType spreadSettleType = SpreadSettleType.CPC;
    public enum SpreadSettleType {
        CPC, CPM
    }

    // 广告尺寸
    protected int adShowHeight = 50;
    protected int adShowWidth = 320;

    protected int screenWidth = 320;
    protected int screenHeight = 480;

    protected int adHeight_applyBean = 250;
    protected int adWidth_applyBean = 300;

    //MREC 广告比率 300/250 = 1.2但是1.5似乎效果更好
    protected static float MrecRatio = 1.5f;
    // 广告尺寸类型
    protected int adSize = 0;
    protected static double gdtVurtalDensity;
    protected static int dpi;
    protected static double density;

    //返回广告内容的内层<ads>的 bean,实际包含的是下发的广告数据内容
    public AdsBean adsBean = null;
    protected RetAdBean respAdBean = null;
    //IAB's GDPR
    protected GDPRBean reqGDPRBean = null;
    // 广告列表
    protected ArrayList<AdsBean> adsBeanList = null;

    private KyAdBaseViewHandler handler = null;

    public static int isSupportHtml = 0;

    //打底sdk使用的参数
    public final static int SUPPORT_GDT_AD = 1;
    public final static String SUPPORT_GDT_PLATFORM = "1006";
    public final static String SUPPORT_BAIDU_PLATFORM = "1007";
    public final static String SUPPORT_TOUTIAO_PLATFORM = "1008";

    public static int isH5Changed = 1;
    public static int batteryLevel = 100;
    //mrec使用的
    protected boolean mrecVideoMode = false; //wilder 2020 默认mrec的video模式是false
    //video used
    protected int videoOrientation = -1;
    protected boolean trafficWarnEnable = true;
    protected String bgColor = "#undefine";
    protected boolean autoCloseAble = false;
    //end video used
    //adlogo & adicon 使用的
    protected Bitmap adLogoBmp; //wilder 20200228 for test
    protected Bitmap adIconBmp;//wilder 20200228 for test

    /////////////////// abstract method ////////////////////////////////
    public abstract String getBitmapPath();
    /**
     * 全局Handler处理
     */
    protected abstract void handlerMsgs(Message msg);
    protected abstract boolean initAdLogo(Object object);
    /**
     * 判断广告图片资源是否有效
     */
    protected abstract boolean createBitmap(Object object);
    protected abstract void handleClick(MotionEvent e, int realX, int realY, String url);

    ///////////////////////////////////////////////////////////////////////////////////////////
    public static class KyAdBaseViewHandler extends Handler {
        private KyAdBaseView kyAdBaseView = null;

        public KyAdBaseViewHandler(KyAdBaseView kyAdBaseView) {
            super(Looper.getMainLooper());
            this.kyAdBaseView = kyAdBaseView;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (null != kyAdBaseView && null != msg)
                    kyAdBaseView.handlerMsgs(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 构造
     *
     * @param context
     */
    public KyAdBaseView(Context context) {
        super(context);
        registerBatteryReceiver(context);
        if (null == handler)
            handler = new KyAdBaseViewHandler(this);
        if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
            AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
        if (null == reqScheduler || reqScheduler.isTerminated())
            reqScheduler = Executors.newScheduledThreadPool(ConstantValues.REQUEST_THREADPOOL_NUM);
        if (null == bannerReqScheduler || bannerReqScheduler.isTerminated())
            bannerReqScheduler = Executors.newScheduledThreadPool(ConstantValues.REQUEST_THREADPOOL_NUM);
        AdViewUtils.userAgent = AdViewUtils.getUserAgent(context);
        cleanCacheFile();
    }

    public static void registerBatteryReceiver(final Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        try {
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                            batteryLevel = intent.getIntExtra("level", -1);
                            context.unregisterReceiver(this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            context.unregisterReceiver(this);
                        } catch (Exception e2) {
                        }
                    }
                }
            }, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLogMode(boolean logMode) {

        AdViewUtils.logMode = logMode;
    }

    public static void setHtmlSupport(int isSupport) {

        KyAdBaseView.isSupportHtml = isSupport;
    }

    //wilder 2020 for mrec
    public void setVideoMode(boolean enable) {
        mrecVideoMode  = enable;
    }

    public boolean getVideoMode() { return mrecVideoMode; }

//    for GDPR 2019
/*    public void setGDPR(boolean cmpPresent, String subjectToGDPR, String consentString,String parsedPurposeConsents, String parsedVendorConsents){
        if (null == reqGDPRBean) {
            reqGDPRBean = new GDPRBean();
        }
        reqGDPRBean.setIabCMPPresent(cmpPresent);
        reqGDPRBean.setIabSubjectToGDPR(subjectToGDPR);
        reqGDPRBean.setIabConsentString(consentString);
        reqGDPRBean.setIabParsedPurposeConsents(parsedPurposeConsents);
        reqGDPRBean.setIabParsedVendorConsents(parsedVendorConsents);
    }*/

    //for GDPR 1.0
    public void setGDPR(boolean cmpPresent, String consentString ){
        if (null == reqGDPRBean) {
            reqGDPRBean = new GDPRBean();
        }

        reqGDPRBean.setIabCMPPresent(cmpPresent ? 1 : 0);
        reqGDPRBean.setIabConsentString(consentString);
    }

    //for GDPR TCF 2.0
    public void setGDPR2(int cmpPresent, String consentString ){
        if (null == reqGDPRBean) {
            reqGDPRBean = new GDPRBean();
        }
        //if has already got , just skip
//        if (!reqGDPRBean.getIabConsentString().isEmpty())
//            return;
        reqGDPRBean.setIabCMPPresent(cmpPresent);
        reqGDPRBean.setIabConsentString(consentString);
    }

    //在applyAdsBean上传之前要get一下最终确认下值, wilder 20200420 changed for TCF2.0 spec
    protected void getGDPR() {
        //先已系统设置的gdpr为主，如果没有，则用app传入的值
        if (null == reqGDPRBean) {
            reqGDPRBean = new GDPRBean();
        }
        HashMap<String, Object> gdpr = AdViewUtils.getGDPRInfo(getContext());
        if ( null != gdpr ) {
            //更新内容
            String value;
            //boolean con = ((Boolean)gdpr.get("IABConsent_CMPPresent")).booleanValue();
            int con = (Integer) gdpr.get("IABConsent_CMPPresent"); //可能是0,1,-1
            reqGDPRBean.setIabCMPPresent(con);

            value = (String)gdpr.get("IABConsent_ConsentString");
            //if (value.length() > 0) //wilder 20200506 不做合法性判断，直接上传
                reqGDPRBean.setIabConsentString(value);

//            value = (String)gdpr.get("IABConsent_SubjectToGDPR");
//            if (value.length() > 0)
//                reqGDPRBean.setIabSubjectToGDPR(value);
//            value = (String)gdpr.get("IABConsent_ParsedPurposeConsents");
//            if (value.length() > 0)
//                reqGDPRBean.setIabParsedPurposeConsents(value);
//            value = (String)gdpr.get("IABConsent_ParsedVendorConsents");
//            if (value.length() > 0)
//                reqGDPRBean.setIabParsedVendorConsents(value);

        }
    }

    //end gdpr 2019
    public static void setIsH5Changed(int isH5Changed) {
        KyAdBaseView.isH5Changed = isH5Changed;
    }

    /**
     * 执行文件清理，至多每天一次
     */
    private void cleanCacheFile() {
        try {
            final SharedPreferences preferences = SharedPreferencesUtils
                    .getSharedPreferences(getContext(), ConstantValues.SP_ADVINFO_FILE);
            long lastCleanTime = preferences.getLong("lastCleanTime", 0);
            if (System.currentTimeMillis() - lastCleanTime > 24 * 60 * 60 * 1000) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        AdViewUtils.delCacheFiles(ConstantValues.BASE_PATH);
                        SharedPreferencesUtils.commitSharedPreferencesValue(
                                preferences, "lastCleanTime",
                                System.currentTimeMillis());
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkClass(String classes) {
        try {
            if (Class.forName(classes) != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
        }
        return false;
    }

    public static String getAgadn(int sdkType) {
        String temp = "";
        try {
            switch (sdkType) {
                case ConstantValues.SDK_REQ_TYPE_BANNER:
                case ConstantValues.SDK_REQ_TYPE_MREC:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.banner.BannerView"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobads.AdView"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTAdNative"))
                        temp = temp + SUPPORT_TOUTIAO_PLATFORM + ",";
                    break;
                case ConstantValues.SDK_REQ_TYPE_INSTL:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.interstitial.InterstitialAD"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobads.InterstitialAd"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTAdNative"))
                        temp = temp + SUPPORT_TOUTIAO_PLATFORM + ",";
                    break;
                case ConstantValues.SDK_REQ_TYPE_SPREAD:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.splash.SplashAD"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobads.SplashAd"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTAdNative"))
                        temp = temp + SUPPORT_TOUTIAO_PLATFORM + ",";
                    break;
                case ConstantValues.SDK_REQ_TYPE_NATIVE:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.nativ.NativeAD"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobad.feeds.BaiduNative"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    break;
                case ConstantValues.SDK_REQ_TYPE_VIDEO:
                    if (KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTFullScreenVideoAd"))
                        temp = temp + SUPPORT_TOUTIAO_PLATFORM + ",";
//                    if (KyAdBaseView.checkClass("com.baidu.mobad.feeds.BaiduNative"))
//                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    break;
            }
            if (!TextUtils.isEmpty(temp)) {
                if (temp.endsWith(","))
                    temp = temp.substring(0, temp.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String getConfigUrl(int routeType) {
        switch (routeType) {
            case ConstantValues.ROUTE_ADRTB_TYPE: // 996
                return AdViewUtils.adrtbAddr;
            case ConstantValues.ROUTE_ADFILL_TYPE: // 997
                return AdViewUtils.adfillAddr;
            case ConstantValues.ROUTE_ADBID_TYPE: // 998
                return AdViewUtils.adbidAddr;
        }
        return null;
    }

    public static void setIsSupportHtml(int isSupportHtml) {
        KyAdBaseView.isSupportHtml = isSupportHtml;
    }

    /**
     * 生成MD5加密token
     *
     * @param applyAdBean applyAdBean
     * @return token token
     */
    public static String makeBIDMd5Token(ApplyAdBean applyAdBean) {
        String keys = null;
        if (applyAdBean.getRoute() == ConstantValues.SDK_REQ_ROUTE_ADFILL)
            keys = ConstantValues.ROUTE_ADFILL_ANDROID_MD5KEY;
        else if (applyAdBean.getRoute() == ConstantValues.SDK_REQ_ROUTE_SSP)
            keys = ConstantValues.ROUTE_SSP_ANDROID_MD5KEY;
        else
            keys = ConstantValues.ROUTE_RTB_ANDROID_MD5KEY;
        return MD5Utils.MD5Encode(applyAdBean.getBundleId()
                + applyAdBean.getAppId() + applyAdBean.getAdSize()
                + applyAdBean.getUuid() + applyAdBean.getTime() + keys);
    }

    //wilder 2019 for privacy information
    public static void showNativePrivacyInformation(final Context context, final String url ) {
        try {
            AdViewUtils.openLandingPage(context, url, AdViewUtils.useCustomTab);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    //end privacy
    /**
     * 点击事件： 打开网页 || 下载
     *
     * @param context
     * @param adsBean
     * @param url
     */
    public  boolean clickEvent(final Context context, final AdsBean adsBean, final String url, final ServiceConnection conn) {
        try {
            final Intent i = new Intent();
            i.putExtra("adview_url", TextUtils.isEmpty(url) ? adsBean.getAdLink() : url);
//            i.putExtra("adview_url", "http://zt.jd.com/ad/appjump.shtml?turl=https%3A%2F%2Fpro.m.jd.com%2Fmall%2Factive%2F3YBQ3PcjM23pKXyRujN6G9UautLu%2Findex.html%3Fgaid%3D2383636437_26541041&platform=2&qz_gdt=7ujjswtoaaapoamqgjxa");
            i.putExtra("altype", adsBean.getAlType());
            i.putExtra("package", adsBean.getdPackageName());
            i.putExtra("appicon", adsBean.getdAppIcon());
            i.putExtra("appname", adsBean.getdAppName());
            i.putExtra("browser_fallback_url", adsBean.getFallback());
            i.putExtra("deep_link", adsBean.getDeeplink());
            i.putExtra("gdt_conversion_link", adsBean.getGdtConversionLink());
            //效果监测地址
            i.putExtra("downloadstart_report", adsBean.getSaUrl());
            i.putExtra("downloaded_report", adsBean.getFaUrl());
            i.putExtra("install_report", adsBean.getIaUrl());

            if (AdViewUtils.openDeepLink(context, i.getStringExtra("deep_link"))) {
                AdViewUtils.logInfo("已经使用deeplink链接打开，后续操作已跳过");
                return true;
            }

            switch (adsBean.getAdAct()) {
                // 部分地址可能有问题
                default:
                case ConstantValues.RESP_ACT_OPENWEB:
                    //if (onAdSpreadListener != null)
//                    if (this instanceof AdSpreadBIDView){
//                        //spread view 要采用 landingpage activity不建议用custom tab ,因为要获取页面关闭的事件
//                        AdViewUtils.openLandingPage(context, TextUtils.isEmpty(url) ? adsBean.getAdLink() : url, false);
//                    }else {
                        AdViewUtils.openLandingPage(context, TextUtils.isEmpty(url) ? adsBean.getAdLink() : url, AdViewUtils.useCustomTab);
//                    }
                    break;
                case ConstantValues.RESP_ACT_DOWNLOAD:
                    //wilder 20190813 for AppLoving dsp 's resp: though download but url = market://xxxx
                    if (url.contains("market://")) {
                        //就算是download模式，但如果是market的格式则启动openweb模式打开,采用落地页接口，而不采用安装apk的模式
                        AdViewUtils.openLandingPage(context,TextUtils.isEmpty(url) ? adsBean.getAdLink() : url, AdViewUtils.useCustomTab);
                        break;
                    }
                    //end wilder 20190813
                    if (AdViewUtils.useDownloadService) {
                        i.setClass(context, DownloadService.class);
                        if (AdViewUtils.getNetworkType(context).equals("WIFI")) {
                            if (null != conn)
                                context.bindService(i, conn, Context.BIND_AUTO_CREATE);
                            else
                                context.startService(i);
                        } else {
                            AdViewUtils.trafficConfirmDialog(context, new DownloadConfirmInterface() {
                                @Override
                                public void confirmDownload() {
                                    if (null != conn)
                                        context.bindService(i, conn, Context.BIND_AUTO_CREATE);
                                    else
                                        context.startService(i);
                                }

                                @Override
                                public void cancelDownload() {

                                }

                                @Override
                                public void error() {

                                }
                            });
                        }
                    }//end useDownloadService
                    return true;
                case ConstantValues.RESP_ACT_WECHATAPP:  //海外版暂不支持
                    if (AdViewUtils.useWechatApp) {
                        try {
                            if (checkClass("com.tencent.mm.opensdk.openapi.IWXAPI")) {
//                                String wxAppId = adsBean.getAptAppId();
//                                IWXAPI api = WXAPIFactory.createWXAPI(context, wxAppId);
//                                api.registerApp(wxAppId);
//                                WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
//                                req.userName = adsBean.getAptOrgId(); // 填小程序原始id
//                                req.path = adsBean.getAptPath();                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
//                                req.miniprogramType = adsBean.getAptType();// 可选打开 开发版，体验版和正式版
//                                api.sendReq(req);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected boolean isClickableConfirm(AdsBean adsBean) {
        boolean isClickable = checkClickLimitNum(adsBean, true);
        if (!isClickable) {
            //Toast.makeText(getContext(), "不能多次重复点击", Toast.LENGTH_SHORT).show();
            AdViewUtils.logInfo("!!!! [KyAdBaseView] can not click more times !!!");
            return false;
        }

        if (AdViewUtils.useDownloadService) {
            //支持从广告上下载安装apk这样的功能
            return  AdViewUtils.checkClickPermission(getContext(), ConstantValues.DOWNLOADSERVICE_CLASS, PackageManager.GET_SERVICES)
                    && AdViewUtils.checkClickPermission(getContext(), ConstantValues.ADVIEW_LANDINGPAGE_CLASS, PackageManager.GET_ACTIVITIES)
                    && AdViewUtils.checkClickLimitTime(getContext(), adsBean.getSdkType(), adsBean.getIdAd())
                    && isClickable;
        }else {
            //海外版不支持download功能
            return  AdViewUtils.checkClickLimitTime(getContext(), adsBean.getSdkType(), adsBean.getIdAd())
                    && isClickable;
        }
    }

    protected void clickCheck(String url, AdsBean adsBean, ApplyAdBean applyAdBean, RetAdBean retAdBean) {
        if (isClickableConfirm(adsBean) || (retAdBean.getAdSource() == -9999)) {
            url = replaceKeys(url, 0 + "", getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), true).toString(), getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), false).toString(), applyAdBean.getLatitude(), applyAdBean.getLongitude(), applyAdBean.getUuid());
            handleClick(null, adsBean.getAction_down_x(), adsBean.getAction_down_y(), url);
        }
    }

    /**
     * 点击事件： 打开网页 || 下载
     *
     * @param context
     * @param adsBean
     * @param url
     */
    public boolean clickEvent(Context context, AdsBean adsBean, String url) {
        String finalUrl = "";
        try {
            finalUrl = TextUtils.isEmpty(url) ? adsBean.getAdLink() : url;
            if (!TextUtils.isEmpty(finalUrl) && containKeywords(finalUrl)) {
                finalUrl = replaceHotKey4GDT(adsBean, finalUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clickEvent(context, adsBean, finalUrl, null);
    }


    public void reportLoadError(AdsBean adsBean, String key, int errorType) {
        AdViewUtils.logInfo("### [HTML-load]reportLoadError: key = " + key + ", type=" + errorType + "###");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("abd", adsBean.getXhtml());
            jsonObject.put("arg", "");
            jsonObject.put("et", errorType);
            jsonObject.put("eqs", adsBean.getEqs());
            jsonObject.put("adi", adsBean.getIdAd());
            jsonObject.put("ai", adsBean.getAdInfo());
            jsonObject.put("aid", key);
            jsonObject.put("ud", AdViewUtils.getImei(getContext()));
            if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
            AdViewUtils.repScheduler.execute(new ClientReportRunnable(jsonObject.toString(), AdViewUtils.adbidErrorLink,ConstantValues.POST));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////上报接口 ////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 汇报展示
     *
     * @param adsBean
     * @param retAdBean
     * @param applyAdBean
     */
    public static boolean reportImpression(AdsBean adsBean, RetAdBean retAdBean,
                                           ApplyAdBean applyAdBean, boolean isBid) {
        try {
            if (null == adsBean || null == retAdBean)
                return false;
            if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated()) {
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
            }
            if (retAdBean.getServerAgent() == ConstantValues.RESP_SDKAGENT && !TextUtils.isEmpty(adsBean.getAdLogLink())) {
                AdViewUtils.repScheduler.execute(new ClientReportRunnable("", adsBean.getAdLogLink(), ConstantValues.GET));
            }
            if (retAdBean.getAgt() == ConstantValues.RESP_SDKAGENT && !TextUtils.isEmpty(adsBean.getMon_s())) {
                AdViewUtils.repScheduler.execute(new ClientReportRunnable("", adsBean.getMon_s(),ConstantValues.GET));
            }
            if (null != adsBean && null != adsBean.getExtSRpt()) {
                HashMap<String, String[]> rptMaps = adsBean.getExtSRpt();
                Set<String> keySet = rptMaps.keySet();
                String[] ketsString = new String[keySet.size()];
                ketsString = keySet.toArray(ketsString);
                for (int i = 0; i < ketsString.length; i++) {
                    String[] urls = rptMaps.get(ketsString[i]);
                    for (int j = 0; j < urls.length; j++) {
                        if (TextUtils.isEmpty(urls[j]))
                            continue;

                        //对url中的特定字符进行替换
                        String url = replaceKeys(urls[j], "0",
                                getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(),
                                        adsBean.getSpecialAdWidth(),adsBean.getSpecialAdHeight(),true).toString(),
                                getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(),
                                        adsBean.getSpecialAdWidth(),adsBean.getSpecialAdHeight(),false).toString(),
                                applyAdBean.getLatitude(),
                                applyAdBean.getLongitude(),
                                applyAdBean.getUuid());

                        //AdViewUtils.logInfo("### reportImpression(): " + url + "###");
                        AdViewUtils.repScheduler.schedule(
                                new ClientReportRunnable("", url,ConstantValues.GET ),
                                Integer.valueOf(ketsString[i]),
                                TimeUnit.SECONDS);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            return false;
        }
        return true;
    }

    /**
     * 汇报点击,向adview汇报
     *
     * @param e
     * @param applyAdBean
     * @param adsBean
     * @param retAdBean
     */
    protected static int reportClick(MotionEvent e, int realX, int realY, ApplyAdBean applyAdBean,
                                     final AdsBean adsBean, RetAdBean retAdBean) {
        int isMissTouch = ConstantValues.CLICK_ERROR;
        try {
            if (e == null)
                isMissTouch = ConstantValues.CLICK_NORMAL;
            else {
                try {
                    boolean isBannerType = (applyAdBean.getSdkType() == ConstantValues.SDK_REQ_TYPE_BANNER ||
                            applyAdBean.getSdkType() == ConstantValues.SDK_REQ_TYPE_MREC);

                    isMissTouch = isMissTouch(e, realX, realY, adsBean.getRealAdWidth(),
                            (e.getX() == -999 && e.getY() == -999) ? adsBean.getRealAdHeight() / 4 : adsBean.getRealAdHeight(),
                            isBannerType ? 6 : 16);
                } catch (Exception exepction) {
                    exepction.printStackTrace();
                    isMissTouch = ConstantValues.CLICK_ERROR;
                }
            }
            if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
            if (null != adsBean && null != adsBean.getExtCRpt()) {
                HashMap<String, String[]> rptMaps = adsBean.getExtCRpt();
                Set<String> keySet = rptMaps.keySet();
                String[] ketsString = new String[keySet.size()];
                ketsString = keySet.toArray(ketsString);

                for (int i = 0; i < ketsString.length; i++) {
                    String[] urls = rptMaps.get(ketsString[i]);
                    for (int j = 0; j < urls.length; j++) {
                        if (TextUtils.isEmpty(urls[j]))
                            continue;
                        String url = replaceKeys(urls[j],
                                isMissTouch + "",
                                        getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(),
                                                    adsBean.getSpecialAdWidth(), adsBean.getSpecialAdHeight(), true).toString(),
                                        getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getSpecialAdWidth(),
                                                    adsBean.getSpecialAdHeight(), false).toString(),
                                        applyAdBean.getLatitude(),
                                        applyAdBean.getLongitude(),
                                        applyAdBean.getUuid());

                        AdViewUtils.repScheduler.schedule(
                                new ClientReportRunnable("", url, ConstantValues.GET),
                                Integer.valueOf(ketsString[i]),
                                TimeUnit.SECONDS);
                    }
                }
            }
            if (retAdBean.getAgt() == ConstantValues.RESP_SDKAGENT && !TextUtils.isEmpty(adsBean.getMon_c())) {
                AdViewUtils.repScheduler.schedule( new ClientReportRunnable("", adsBean.getMon_c(),ConstantValues.GET),0, TimeUnit.SECONDS);
            }
        } catch (Exception exec) {
            exec.printStackTrace();
        }
        return isMissTouch;
    }

    public static void reportOtherUrls(String urls) {
        try {
            if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
            AdViewUtils.repScheduler.execute(new ClientReportRunnable("", urls, "GET"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void vast_reportUrls(List<String> urls, HashMap<String, String> map) {
        //AdViewUtils.logInfo("entered fireUrls");
        if (urls != null) {
            for (String url : urls) {
                //Log.i("=========  fireUrls ", "url:" + url + "=========");
                AdViewUtils.logInfo("======== vast_reportUrls()url:" + url + "====");
                if (null != url) {
                    url = KyAdBaseView.replace4GDTKeys(url, map);
                    if (null == KyAdBaseView.reqScheduler || KyAdBaseView.reqScheduler.isTerminated()) {
                        KyAdBaseView.reqScheduler = Executors.newScheduledThreadPool(ConstantValues.REQUEST_THREADPOOL_NUM);
                    }
                    KyAdBaseView.reqScheduler.execute(new ClientReportRunnable("", url, "GET"));
                }
            }
        } else {
            AdViewUtils.logInfo("========= url list is null  ========");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean checkClickLimitNum(AdsBean adsBean, boolean isReduce) {
        if (adsBean.getClickNumLimit() <= 0)
            return false;
        else {
            if (isReduce)
                adsBean.setClickNumLimit(adsBean.getClickNumLimit() - 1);
            return true;
        }
    }

    //applybean 是请求的bean
    protected ApplyAdBean initRequestBean(String appId, String posID, int routeType, int sdkType, int adCount) {

        ApplyAdBean reqAdBean = new ApplyAdBean();
        int[] width_height = AdViewUtils.getWidthAndHeight(getContext(), true, true);
        long time = System.currentTimeMillis() / 1000;

        // 设置广告的类型，0：banner 1：插屏  4：开屏 5：视频  6：原生
        reqAdBean.setSdkType(sdkType); //ok
        // 设置adview申请的ID
        reqAdBean.setAppId(appId); //ok
        // 设置广告位id，st=5|6 时必传
        reqAdBean.setAdPosId(posID); //video + natives
        // 固定值Ro, 业务类型, BID使用 ROUTE_ADBID_TYPE
        if (routeType == ConstantValues.ROUTE_ADFILL_TYPE)
            reqAdBean.setRoute(ConstantValues.SDK_REQ_ROUTE_ADFILL);      //ok
        else if (routeType == ConstantValues.ROUTE_ADBID_TYPE)
            reqAdBean.setRoute(ConstantValues.SDK_REQ_ROUTE_SSP);
        else if (routeType == ConstantValues.ROUTE_ADRTB_TYPE)
            reqAdBean.setRoute(ConstantValues.SDK_REQ_ROUTE_RTB);
        // 设备系统，android =>0, iOS => 1
        reqAdBean.setSystem(0); //ok
        // 设置请求的数目，广告数量，一般情况下，Icon广告10个，轮播5个, 除native外，基本设为1
        reqAdBean.setAdCount(adCount);  //ok
        //==============请求广告的尺寸===================
        if (sdkType == ConstantValues.SDK_REQ_TYPE_NATIVE ) {
            reqAdBean.setAdSize("");       //ok
        }else {
            if (sdkType == ConstantValues.SDK_REQ_TYPE_VIDEO) {
                reqAdBean.setAdSize(width_height[0] + "x" + width_height[1]); //video adsize will be fullscreen
                //applyAdBean.setAdSize(adWidth_applyBean + "x" + adHeight_applyBean); //normal
            }else {
                reqAdBean.setAdSize(adWidth_applyBean + "x" + adHeight_applyBean); //normal
            }
            //applyAdBean.setAdSize(adShowWidth + "x" + adShowHeight); //here will send wanted ad size
        }
        //精确定位,经度和纬度
        String[] location = AdViewUtils.getLocation(getContext());
        if (null != location) {         //ok
            try {
                reqAdBean.setLatitude(URLEncoder.encode(location[0], Charset.forName("UTF-8").name()));
                reqAdBean.setLongitude(URLEncoder.encode(location[1], Charset.forName("UTF-8").name()));
            }catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                reqAdBean.setLatitude("");
                reqAdBean.setLongitude("");
            }
        } else {
            reqAdBean.setLatitude("");
            reqAdBean.setLongitude("");
        }
        // APP 版本
        reqAdBean.setAppVer(AdViewUtils.getAppVersionName(getContext())); //ok
        // APP Name 名称
        reqAdBean.setAppName(AdViewUtils.getAppName(getContext())); //wilder 20200421 for app name
        // OS 版本
        reqAdBean.setOsVer(AdViewUtils.getDevOsVer()); //ok
        // SDK 版本
        reqAdBean.setSdkVer(AdViewUtils.VERSION); //ok
        // 设备型号
        reqAdBean.setDevType(AdViewUtils.getDevType()); //ok;
        // 设备品牌
        reqAdBean.setDevBrand(AdViewUtils.getDevName());//ok
        // 设备分辨率
        reqAdBean.setResolution(width_height[0] + "x" + width_height[1]);//ok
        // 设备屏幕密度，Double类型
        reqAdBean.setDeny(AdViewUtils.getDensity(getContext())); //ok
        // 设备imei，海外版已经禁止取得，返回全0
        reqAdBean.setUuid(AdViewUtils.getImei(getContext()));//ok
        // 设备网络类型
        reqAdBean.setNetType(AdViewUtils.getNetworkType(getContext()));//ok
        // 手机运营商
        reqAdBean.setService(AdViewUtils.getServicesPro(getContext()));//ok
        // 设备类型
        reqAdBean.setDevUse(AdViewUtils.getDevUse(getContext())); //ok
        // googleplay ID
        reqAdBean.setGpId(AdViewUtils.getGpId(getContext()));//ok
        //华为 OAID
        reqAdBean.setOAId(AdViewUtils.getOAId(getContext()));
        // AndroidID
        reqAdBean.setAndroid_ID(AdViewUtils.getAndroidID(getContext()));//ok
        // user agent
        reqAdBean.setUa(AdViewUtils.userAgent); //ok
        // 应用包名
        reqAdBean.setBundleId(getContext().getPackageName());//ok
        //MAC地址
        reqAdBean.setMacAddress(AdViewUtils.getMacAddress(getContext()));//ok
        // time
        reqAdBean.setTime(String.valueOf(time));//ok
        // battery，整数
        reqAdBean.setBatteryLevel(KyAdBaseView.batteryLevel);//ok
        //是否支持广点通，选填
        //requestMap.put(SUPPORTGDT, KyAdBaseView.SUPPORT_GDT_AD);
        // 秘钥token md5(appid+sn+os+nop+pack+time+secretKey)
        reqAdBean.setToken(AdViewUtils.makeBIDMd5Token(reqAdBean));//ok
        // supportGdt,打底SDK的取得决定于SDK的type,见下
        //requestMap.put(AGADN, KyAdBaseView.getAgadn(ConstantValues.NATIVEADTYPE));
        //GDPR, 2019 完整iab GDPR规范，这里更新gdpr的最终值
        getGDPR();
        //end GDPR
        ///////////////// normal //////////////////////////
        //测试模式
        reqAdBean.setTestMode(0);
        //是否支持HTML
        reqAdBean.setHtml5(isSupportHtml);
        //旋转
        reqAdBean.setOrientation(AdViewUtils.getOrientation(getContext()));
        //config
        reqAdBean.setConfigVer(0);
        // wifi 信息,可选，本例未上传
        reqAdBean.setBssid_wifi(AdViewUtils.getBSSID(getContext()));
        reqAdBean.setSsid_wifi(AdViewUtils.getSSID(getContext()));

        return reqAdBean;
    }

    //组合requst 请求字串
    protected String makeRequestBeanString(ApplyAdBean reqAdBean) {
        String buffer =
                "bi=" + reqAdBean.getBundleId() //包名
                + "&an=" + reqAdBean.getAppName() // 应用名
                + "&aid=" + reqAdBean.getAppId()//sdk—key
                + "&posId=" + reqAdBean.getAdPosId() //======== video pos id - video used ===========
                + "&sv=" + reqAdBean.getSdkVer()//sdk版本
                + "&cv=" + reqAdBean.getConfigVer() //配置版本
                + "&sy=" + reqAdBean.getSystem()//系统
                + "&st=" + reqAdBean.getSdkType()//===============广告类型==================
                + "&as=" + reqAdBean.getAdSize()//==============尺寸===================
                + "&ac=" + reqAdBean.getAdCount()//广告条数
                + "&at=" + reqAdBean.getAdType()// =======adtype   - video used ===========
                + "&tm=" + reqAdBean.getTestMode() // 测试模式
                + "&se=" + reqAdBean.getService()//运营商
                + "&ti=" + reqAdBean.getTime()//时间
                + "&ud=" + reqAdBean.getUuid()//imei
                + "&to=" + reqAdBean.getToken()//token
                + "&re=" + reqAdBean.getResolution()//分辨率
                + "&ro=" + reqAdBean.getRoute() //业务类型
                + "&dt=" + reqAdBean.getDevType() //手机型号
                + "&db=" + reqAdBean.getDevBrand()//手机厂商
                + "&lat=" + reqAdBean.getLatitude()// 经纬度
                + "&lon=" + reqAdBean.getLongitude() //经纬度
                + "&nt=" + reqAdBean.getNetType()// 联网类型
                + "&src=" + reqAdBean.getAdSource() //广告源
                + "&du=" + reqAdBean.getDevUse()//设备类型
                + "&gd=" + reqAdBean.getGpId()//谷歌
                + "&oaid=" + reqAdBean.getOAId()//华为oaid
                + "&ua=" + reqAdBean.getUa()// useragent
                + "&andid=" + reqAdBean.getAndroid_ID() // android id
                + "&html5=" + reqAdBean.getHtml5() // 是否支持html
                + "&deny=" + reqAdBean.getDeny()//屏幕密度
                + "&blac=" + reqAdBean.getBlac()// 位置区域码
                + "&cid=" + reqAdBean.getCid()// 基站编号
                + "&ov=" + reqAdBean.getOsVer() // 版本号
                + "&mc=" + reqAdBean.getMac()
                + "&av=" + reqAdBean.getAppVer()//appVersion
                + "&bty=" + reqAdBean.getBatteryLevel()
                + "&supGdtUrl=" + SUPPORT_GDT_AD
                + "&agadn=" + getAgadn(reqAdBean.getSdkType())
                + "&apt=" + isSupportWXAPI()
                + "&hv=" + reqAdBean.getOrientation()
                + "&gdpr=" + reqGDPRBean.getIabCMPPresent()     //reqAdBean.getGdpr()              //GDPR - 0或者1
                + "&consent=" + reqGDPRBean.getIabConsentString()       //gdpr - consent string
//                + "&subject=" + reqGDPRBean.getIabSubjectToGDPR()       //gdpr - subject (TCF 2.0 ignored)
//                + "&purpose=" + reqGDPRBean.getIabParsedPurposeConsents()  //gdpr - purpose consent (TCF 2.0 ignored)
//                + "&vendor=" + reqGDPRBean.getIabParsedVendorConsents()    //gdpr - vendor consent (TCF 2.0 ignored)
                + "&omid=" + (AdViewUtils.canUseOMSDK() ? 1 : 0)  //omsdk v1.2 support
                + "&omidpn=" + AdViewUtils.getOMPartnerName()  //omidpn , parnter名称
                + "&omidpv=" + AdViewUtils.getOMPartnerVer()  //omidpv, parnter版本
                + "&us_privacy=" + AdViewUtils.getCCPA_String(getContext()); //20191204 for CCPA

        return buffer;
    }

    private int isSupportWXAPI() {
        /* wilder 2019 covered
        try {
            if (checkClass("com.tencent.mm.opensdk.openapi.IWXAPI")) {
                IWXAPI iwxapi = WXAPIFactory.createWXAPI(getContext(), "");
                if (iwxapi.isWXAppInstalled())
                    return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return 0;
    }

    public static boolean isVideoType(int adType) {
        if (adType == ConstantValues.RESP_ADTYPE_VIDEO ||
            adType == ConstantValues.RESP_ADTYPE_VIDEO_EMBED ||
                adType == ConstantValues.RESP_ADTYPE_VIDEO_PASTER)
            return true;

        return false;
    }

    public static String getActIcon(int act) {
        String iconPath = "icon_web.png";
        switch (act) {
            case ConstantValues.RESP_ACT_OPENWEB:
                iconPath = "icon_web.png";
                break;
            case ConstantValues.RESP_ACT_DOWNLOAD:
                iconPath = "icon_down.png";
                break;
            case ConstantValues.RESP_ACT_OPENMAP:
                iconPath = "icon_maps.png";
                break;
            case ConstantValues.RESP_ACT_SENDMSG:
                iconPath = "icon_ems.png";
                break;
            case ConstantValues.RESP_ACT_SENDEMAIL:
                iconPath = "icon_email.png";
                break;
            case ConstantValues.RESP_ACT_CALL:
                iconPath = "icon_call.png";
                break;
            case ConstantValues.RESP_ACT_PLAYVIDEO:
                iconPath = "icon_video.png";
                break;
        }
        //return ConstantValues.WEBVIEW_IMAGE_BASE_PATH + iconPath;
        return iconPath;
    }

    public static HashMap<String, String> getColorSet(int random) {
        HashMap<String, String> colorMap = new HashMap<String, String>();
        colorMap.put(COLOR_KEYS[0], COLOR_SETS[random][0]);
        colorMap.put(COLOR_KEYS[1], COLOR_SETS[random][1]);
        colorMap.put(COLOR_KEYS[2], COLOR_SETS[random][2]);
        colorMap.put(COLOR_KEYS[3], COLOR_SETS[random][3]);
        colorMap.put(COLOR_KEYS[4], COLOR_SETS[random][4]);
        colorMap.put(COLOR_KEYS[5], COLOR_SETS[random][5]);
        return colorMap;
    }

    /**
     * 横幅/插屏 广告尺寸适配,这里是根据设备显示的最终尺寸
     */
    public void calcAdSize() {
        DisplayMetrics displayMetrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        dpi = displayMetrics.densityDpi;
        density = displayMetrics.density;
        if (adSize <= 4) {
            if (dpi < 160)
                gdtVurtalDensity = 0.75;
            else if (dpi < 240 && dpi >= 160)
                gdtVurtalDensity = 1;
            else if (dpi < 320 && dpi >= 240)
                gdtVurtalDensity = 1 / 5;
            else if (dpi >= 320)
                gdtVurtalDensity = 2;
        }
//        density = displayMetrics.density;
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        screenWidth = screenWidth > screenHeight ? screenHeight : screenWidth;
        double density = AdViewUtils.getDensity((this.getContext()));

        switch (adSize) {
            // 初始化为--INSTL_REQ_SIZE = 4
            case ConstantValues.BANNER_REQ_SIZE_480X75:
                adShowWidth = (int) (480 * density);
                adShowHeight = (int) (75 * density);
                break;
            case ConstantValues.BANNER_REQ_SIZE_728X90:
                adShowWidth = (int) (728 * density);
                adShowHeight = (int) (90 * density);
                break;
            case ConstantValues.BANNER_REQ_SIZE_AUTO_FILL:
                adShowWidth = screenWidth;
                adShowHeight = (int) (screenWidth / 6.4);
                break;
            case ConstantValues.BANNER_REQ_SIZE_MREC:  //300/250 = 1.2 / 1.35 will be fit
                //adShowWidth = screenWidth;
                //adShowHeight = (int) (adShowWidth / 1.5);
                adShowWidth = (int) (300 * density);
                adShowHeight = (int) (250 * density);

                break;
            case ConstantValues.BANNER_REQ_SIZE_SMART:
                adShowWidth = (int) (320 * density);
                adShowHeight = (int) (50 * density);
                break;

            //INSTL & SPREAD use this
            case ConstantValues.INSTL_REQ_SIZE:
                adShowWidth = (int) (300 * density);
                adShowHeight = (int) (300 * density);
                break;

            case ConstantValues.INSTL_REQ_SIZE_300X250:
                adShowWidth = (int) (300 * density);
                adShowHeight = (int) (250 * density);
                break;
            case ConstantValues.INSTL_REQ_SIZE_600X500:
                adShowWidth = (int) (600 * density);
                adShowHeight = (int) (500 * density);
                break;
            case ConstantValues.INSTL_REQ_SIZE_320X480:
                adShowWidth = (int) (320 * density);
                adShowHeight = (int) (480 * density);
                break;
        }

        //wilder 2019
        if (adSize == ConstantValues.BANNER_REQ_SIZE_MREC) {
            //use MREC size
            adWidth_applyBean = (int) (300 * density);
            adHeight_applyBean = (int) (250 * density);
        }else {
            adWidth_applyBean = adShowWidth;
            adHeight_applyBean = adShowHeight;
        }

    }

    public boolean isValidClick(AdsBean adsBean, long eventTime) {
        if (eventTime > 3 * 1000)
            return false;
        int adWidth = adsBean.getSpecialAdWidth() == -1 ? adsBean.getRealAdWidth() : adsBean.getSpecialAdWidth();
        int adHeight = adsBean.getSpecialAdHeight() == -1 ? adsBean.getRealAdHeight() : adsBean.getSpecialAdHeight();
        int actionDown_x = (adsBean.getAction_down_x() < 0 || (adsBean.getAction_down_x() > adWidth)) ? -1 : (adsBean.getAction_down_x() * 1000 / adWidth);
        int actionDown_y = (adsBean.getAction_down_y() < 0 || (adsBean.getAction_down_y() > adHeight)) ? -1 : (adsBean.getAction_down_y() * 1000 / adHeight);
        int actionUp_x = (adsBean.getAction_up_x() < 0 || (adsBean.getAction_up_x() > adWidth)) ? -1 : (adsBean.getAction_up_x() * 1000 / adWidth);
        int actionUp_y = (adsBean.getAction_up_y() < 0 || (adsBean.getAction_up_y() > adHeight)) ? -1 : (adsBean.getAction_up_y() * 1000 / adHeight);
        if (actionDown_x == -1 || actionDown_y == -1 || actionUp_x == -1 || actionUp_y == -1)
            return false;
        return true;
    }

    public static String replaceHotKey4GDT(AdsBean adsBean, String url) {
        try {
            if (TextUtils.isEmpty(url))
                return url;
            JSONObject object = new JSONObject(getClickArea4GDT(adsBean));
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String tempKey = keys.next();
                String value = object.optString(tempKey);
                url = url.replace("__" + tempKey.toUpperCase() + "__", value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static JSONObject getClickArea(int x, int y, int width, int height, boolean isRelative) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (width == 0 || height == 0)
                return jsonObject;
            jsonObject.put("down_x", "" + (x > width ? -999 : (isRelative ? (x * 1000 / width) : x)));
            jsonObject.put("down_y", "" + (y > height ? -999 : (isRelative ? (y * 1000 / height) : y)));
            jsonObject.put("up_x", "" + (x > width ? -999 : (isRelative ? (x * 1000 / width) : x)));
            jsonObject.put("up_y", "" + (y > height ? -999 : (isRelative ? (y * 1000 / height) : y)));
//            jsonObject.put("down_x", "" + (adsBean.getAction_down_x() > adWidth ? -1 : (isRelative ? (adsBean.getAction_down_x() * 1000 / adWidth) : adsBean.getAction_down_x())));
//            jsonObject.put("down_y", "" + (adsBean.getAction_down_y() > adHeight ? -1 : (isRelative ? (adsBean.getAction_down_y() * 1000 / adHeight) : adsBean.getAction_down_y())));
//            jsonObject.put("up_x", "" + (adsBean.getAction_up_x() > adWidth ? -1 : (isRelative ? (adsBean.getAction_up_x() * 1000 / adWidth) : adsBean.getAction_up_x())));
//            jsonObject.put("up_y", "" + (adsBean.getAction_up_y() > adHeight ? -1 : (isRelative ? (adsBean.getAction_up_y() * 1000 / adHeight) : adsBean.getAction_up_y())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static String getClickArea4GDT(AdsBean adsBean) {
        JSONObject jsonObject = new JSONObject();
        try {
            float delta = (float) (gdtVurtalDensity / density);
            switch (adsBean.getSdkType()) {
                case ConstantValues.SDK_REQ_TYPE_BANNER:
                case ConstantValues.SDK_REQ_TYPE_MREC:
                    break;
                case ConstantValues.SDK_REQ_TYPE_INSTL:
                    delta = adsBean.getRealAdWidth() / (dpi >= 320 ? 600 : 300);
                    break;
                case ConstantValues.SDK_REQ_TYPE_SPREAD:
                    delta = adsBean.getRealAdWidth() / (dpi >= 320 ? 640 : 320);
                    break;
                case ConstantValues.SDK_REQ_TYPE_NATIVE:
                    delta = 1;
                    break;
            }
            jsonObject.put("down_x", "" + (int) ((adsBean.getAction_down_x() * delta) <= 0 ? -999 : (adsBean.getAction_down_x() * delta)));
            jsonObject.put("down_y", "" + (int) ((adsBean.getAction_down_y() * delta) <= 0 ? -999 : (adsBean.getAction_down_y() * delta)));
            jsonObject.put("up_x", "" + (int) ((adsBean.getAction_up_x() * delta) <= 0 ? -999 : (adsBean.getAction_up_x() * delta)));
            jsonObject.put("up_y", "" + (int) ((adsBean.getAction_up_y() * delta) <= 0 ? -999 : (adsBean.getAction_up_y() * delta)));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
//        Log.i("clickMap", "realSize w= " + adsBean.getRealAdWidth() + "  :  h=" + adsBean.getRealAdHeight());
//        Log.i("clickMap", "gdtClick = " + jsonObject.toString());
        return jsonObject.toString();
    }

    //在一些rtb的上报事件中会有固定的url的key的替换，以满足特定dsp的要求
    public static HashMap<String, String> getHK_Values(Context context, int x, int y, boolean isFinished, boolean hasError, Bundle bundle) {
        HashMap<String, String> hk_Map = new HashMap<String, String>();
        try {
            String[] location = AdViewUtils.getLocation(context);
            boolean isLand = false;
            try {
//                if (context instanceof Activity) { //wilder 2020 for non-context
//                    isLand = ((Activity) context).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || ((Activity) context).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
//                }
                isLand = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE; //wilder 2020 for non-activity
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject clickJson = getClickArea(x, y, bundle.getInt("desireWidth"), bundle.getInt("desireHeight"), false);
            hk_Map.put(HK_CLICKAREA, "0");
            hk_Map.put(HK_RELATIVE_COORD, getClickArea(x, y, bundle.getInt("desireWidth"), bundle.getInt("desireHeight"), true).toString());
            hk_Map.put(HK_ABSOLUTE_COORD, clickJson.toString());
            hk_Map.put(HK_LONGITUDE, location[1]);
            hk_Map.put(HK_LATITUDE, location[0]);
            hk_Map.put(HK_UUID, AdViewUtils.getImei(context));
            hk_Map.put(HK_GDT_DOWN_X, clickJson.has("down_x") ? clickJson.getString("down_x") : "-999");
            hk_Map.put(HK_GDT_DOWN_Y, clickJson.has("down_y") ? clickJson.getString("down_y") : "-999");
            hk_Map.put(HK_GDT_UP_X, clickJson.has("up_x") ? clickJson.getString("up_x") : "-999");
            hk_Map.put(HK_GDT_UP_Y, clickJson.has("up_y") ? clickJson.getString("up_y") : "-999");
            hk_Map.put(HK_DURATION, bundle.getInt("duration") + "");// + "");
            hk_Map.put(HK_BEGINTIME, bundle.getInt("lastPauseVideoTime") + "");
            hk_Map.put(HK_ENDTIME, bundle.getInt("currentVideoPlayTime") + "");
            hk_Map.put(HK_FIRST_FRAME, bundle.getInt("lastPauseVideoTime") == 0 ? "1" : "0");
            hk_Map.put(HK_LAST_FRAME, isFinished ? "1" : "0");
            hk_Map.put(HK_SCENE, isLand ? "4" : "2");
            hk_Map.put(HK_TYPE, bundle.getInt("lastPauseVideoTime") == 0 ? "1" : "2");
            hk_Map.put(HK_BEHAVIOR, "1");
            hk_Map.put(HK_STATUS, hasError ? "2" : "1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hk_Map;
    }

    /**
     * 根据宏定义替换字段 -999表示不替换
     *
     * @param ori  被替换的字符串
     * @param maps 0=是否误点击；1=相对点击坐标；2=绝对点击坐标；3=经度；4=纬度；
     *             5=uuid；6=总时长；7=起始时间；8=结束时间；9；是否从头播放；
     *             10=是否播放到最后一帧；11=场景；12=播放类型；13=播放行为；14=播放状态；
     * @return new String
     */
    public static String replace4GDTKeys(String ori, HashMap<String, String> maps) {
        if (maps.size() == 0 || null == ori)
            return ori;

        Set<String> keySet = maps.keySet();
        Iterator iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = maps.get(key);
            ori = ori.replace(key, value);
        }
        return ori;
    }

    public boolean initCacheView(int type) {
        String cacheData = null;
        long cacheTime = 0l;
        SharedPreferences preferences = getContext().getSharedPreferences(
                ConstantValues.SP_INSTLINFO_FILE, Context.MODE_PRIVATE);
        cacheTime = preferences.getLong("sp_cacheTime", 0l);
        cacheData = preferences.getString("sp_cacheData", null);
        if (null != cacheData
                && System.currentTimeMillis() / 1000 - cacheTime <= ConstantValues.DEFAULT_CACHE_PEROID) {
            // Log.i(AdViewUtils.ADVIEW, "cache " + cacheData);
            respAdBean = parseRespOuterJson(cacheData);
            if (respAdBean.getResult() != 0)
                adsBeanList = parseRespAdsJson(respAdBean.getAds(), type);
            else
                return false;
            if (adsBeanList != null && !adsBeanList.isEmpty()) {
                adsBean = adsBeanList.get(0);
                //html类型不加载缓存
                if (!TextUtils.isEmpty(adsBean.getXhtml()))
                    return false;
                // 设置sdkTYPE
                adsBean.setSdkType(type);

                if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_HTML) {
                    if (null != adsBean.getXhtml()
                            && adsBean.getXhtml().length() > 0)
                        // notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_OK, "OK");
                        return true;
                }
                if (createBitmap(adsBean)) {
                    // notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_OK, "OK");
                    return true;
                }
            }
        }
        return false;
    }
    //5种广告类型app interface设置
    public void setOnAdSpreadListener(OnAdViewListener onAdViewListener) {
        this.onAdSpreadListener = onAdViewListener;
    }
    public void setOnAdViewListener(OnAdViewListener onAdViewListener) {
        this.onAdViewListener = onAdViewListener;
    }
    public void setOnAdInstlListener(OnAdViewListener onAdViewListener) {
        this.onAdInstlListener = onAdViewListener;
    }
    public void setVideoAppListener(AdViewVideoInterface appInterface) {
        this.appVideoInterface = appInterface;
    }
    public void setAppNativeListener(NativeAdCallBack appInterface) {
        this.nativeAdCallBack = appInterface;
    }
    /**
     * 误点击 判断
     *
     * @param event
     * @param adWidth  广告宽
     * @param adHeight 广告高
     * @return
     */
    private static int isMissTouch(MotionEvent event, int adWidth, int adHeight, int value) {
        return isMissTouch(event, -2, -2, adWidth, adHeight, value);
    }

    private static int isMissTouch(MotionEvent event, int realX, int realY, int adWidth,
                                   int adHeight, int value) {
        if (null == event)
            return ConstantValues.CLICK_NORMAL;
        float x = event.getX();
        float y = event.getY();
        if ((x == -1 && y == -1) || (x == -999 && y == -999))
            return ConstantValues.CLICK_NORMAL;
        return x >= adWidth / 16 && x <= adWidth * 15 / 16
                && y >= adHeight / value && y <= adHeight * (value - 1) / value ? ConstantValues.CLICK_NORMAL
                : ConstantValues.CLICK_ERROR;
    }

    public static String getAdMsg(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            return jsonObject.optString("mg", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAds(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            return jsonObject.optString("ad", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isVaildAd(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            return jsonObject.optInt("res", 0) == 1;
//            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveCache(int type, String key, Object value) {
        AdViewUtils.logInfo("### saveCache(" + key + "): " + type + "###");
        SharedPreferences preferences = null;
        if (type == ConstantValues.SDK_REQ_TYPE_SPREAD) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_SPREADINFO_FILE, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.SDK_REQ_TYPE_INSTL) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_INSTLINFO_FILE, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.SDK_REQ_TYPE_BANNER) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_BANNERINFO_FILE, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.SDK_REQ_TYPE_MREC) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_BANNERINFO_FILE, Context.MODE_PRIVATE);
        }
        if (null != preferences) {
                Editor editor = preferences.edit();
                if (value instanceof String)
                    editor.putString(key, (String) value);
                else if (value instanceof Long)
                    editor.putLong(key, (Long) value);
                else if (value instanceof Boolean)
                    editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Integer)
                    editor.putInt(key, (Integer) value);
                // editor.putLong("sp_cacheTime",
                // System.currentTimeMillis()/1000);
                editor.commit();
        }
    }

    /**
     * 广告消息处理 方法,互推继承
     *
     * @param status
     * @param msgs
     */
    public void notifyMsg(int status, Object msgs) {
        Message msg = new Message();
        if (null == msgs)
            msgs = "result is null";

        msg.what = status;
        msg.obj = (Object) msgs;

        AdViewUtils.logInfo("++++ KyAdBaseView::notifyMsg(status = "+ status + ",msg = "+ msgs + ")++++"); //wilder 2019
        try {
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析Resp竞价内层广告数组
     *
     * @param jsonStr
     * @return 广告队列
     */
    public static ArrayList<AdsBean>
    parseRespAdsJson(String jsonStr, int sdkType) {
        if (null == jsonStr)
            return null;
        ArrayList<AdsBean> adsBeanList = new ArrayList<AdsBean>();
        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        AdsBean adsBean = null;
        try {
            jsonArray = new JSONArray(jsonStr);

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                adsBean = new AdsBean();
                ///////////////   通用字段  //////////////////////////////
                adsBean.setIdAd(jsonObject.optString("adi", null)); //广告交易ID
                adsBean.setServicesUrl(jsonObject.optString("su", "")); //服务器地址
                adsBean.setGetImageUrl(jsonObject.optString("giu", "")); //图片地址首部分
                adsBean.setAdSource(jsonObject.optInt("src", 0)); //广告来源渠道号
                //2016-8-18,AdIcon 和 AdLogo的图标，显示在广告左下和右下的图标
                adsBean.setAdIconUrl(jsonObject.optString("adIcon", ""));
                adsBean.setAdLogoUrl(jsonObject.optString("adLogo", ""));

                /////////////   下载页动作  1：广告页   2：下载  /////////////////
                adsBean.setAdAct(jsonObject.optInt("act", 0)); //open web 或者是 download

                //adsBean.setAdAct(1); //1-open web 或者是 2-download
                adsBean.setAdBehavIcon(jsonObject.optString("abi", null));
                adsBean.setAdPhoneNum(jsonObject.optString("apn", null)); //广告电话号码
                adsBean.setAdLogLink(jsonObject.optString("adl", null));

                adsBean.setAdInfo(jsonObject.optString("ai", null)); //附加信息
                //广告内容类型
                //0：banner纯图片
                //1：banner文字链
                //2：banner图文混合
                //3：插屏
                //4：html
                //5：开屏纯图片
                //6：激励视频
                //7：贴片视频
                //8：原生
                adsBean.setAdType(jsonObject.optInt("at", 0));

                // 广告尺寸处理，at = 8 时不用返回，视频类型返回有尺寸，但是意义不大
                boolean isValid = false;
                if (jsonObject.has("as")) {
                    String adSize = jsonObject.optString("as", null);
                    if (adSize.matches("(\\d)+x(\\d)+")) {
                        try {
                            adsBean.setAdHeight(Integer.valueOf(adSize.split("x")[1]));
                            adsBean.setAdWidth(Integer.valueOf(adSize.split("x")[0]));
                            isValid = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                //如果没有返回尺寸，则使用一些默认值便于adapter操作
                if (!isValid) {
                    switch (sdkType) {
                        case ConstantValues.SDK_REQ_TYPE_BANNER: //banner
                            adsBean.setAdWidth(320);
                            adsBean.setAdHeight(50);
                            break;
                        case ConstantValues.SDK_REQ_TYPE_INSTL: //插屏
                            adsBean.setAdWidth(300);
                            adsBean.setAdHeight(300);
                            break;
                        case ConstantValues.SDK_REQ_TYPE_SPREAD: //开屏
                            adsBean.setAdWidth(640);
                            adsBean.setAdHeight(960);
                            break;
                        case ConstantValues.SDK_REQ_TYPE_MREC: //mrec
                            adsBean.setAdWidth(300);
                            adsBean.setAdHeight(250);
                            break;
                    }
                }
                /////////  act = 128 时，填充以下字段: 2018年9月7日 增加微信小程序呼起  ////////////
                adsBean.setAptAppId(jsonObject.optString("aptAppId", ""));
                adsBean.setAptOrgId(jsonObject.optString("aptOrgId", ""));
                adsBean.setAptPath(jsonObject.optString("aptPath", ""));
                adsBean.setAptType(jsonObject.optInt("aptType", 0));

                //////////////   at = 4 时，填充以下字段:  html广告内容 ///////////////////
                adsBean.setXhtml(jsonObject.optString("xs", null)); //html广告内容

                ////////////  at = 6|7 时，填充以下字段：视频广告 ///////////////
                if (jsonObject.has("video")) {
                    parseVideo(adsBean, jsonObject.getJSONObject("video")); //视频广告
                }
                ////////////   at = 8 时，填充以下字段： 原生广告 //////////
                if (jsonObject.has("native")) {
                    parseNative(adsBean, jsonObject.getJSONObject("native")); //原生素材内容，原生广告请求时返回该字段
                }
                ///////////////////  at = 0|1|2|3|5 时，填充以下字段  /////////////////
                JSONArray adPics = jsonObject.optJSONArray("api"); //图片名称（纯图片广告）
                if (null != adPics && adPics.length() > 0) {
                    //adsBean.setAdPic(adPics.getString(0)); //设置纯图片广告
                    //需要进行替换
                    adsBean.setAdPic(adPics.getString(0)
                                .replace("\"", "")
                                .replace("[", "")
                                .replace("]", ""));
                }
                adsBean.setAdIcon(jsonObject.optString("aic", null)); //广告图标（Icon）的url地址
                adsBean.setAdText(jsonObject.optString("ate", null)); //广告文字内容
                adsBean.setAdTitle(jsonObject.optString("ati", null)); //广告标题
                adsBean.setAdSubTitle(jsonObject.optString("ast", null)); //广告副标题
                adsBean.setAit(jsonObject.optInt("ait", 0)); //插屏类型 0：单纯图片 1：富媒体  2：视频
                adsBean.setAdBgColor(jsonObject.optString("abc", null)); //广告背景色
                adsBean.setAdTitleColor(jsonObject.optString("atc", null)); //广告前景色

                /////////////////// at = 5 为开屏广告时可能返回下面几个字段： ////////////////
                adsBean.setRuleTime(jsonObject.optInt("rlt", 3)); //开屏规定时间，单位秒
                adsBean.setDelayTime(jsonObject.optInt("dlt", 0)); //开屏延长时间，单位秒
                adsBean.setPointArea(jsonObject.optString("pta", "(0,0,1000,1000)"));//开屏可点击区域
                adsBean.setCacheTime(jsonObject.optLong("cet", 0l)); //开屏缓存时间(时间戳)
                adsBean.setSpreadType(jsonObject.optInt("sdt", 1));//开屏种类：1：有Logo  2：没有
                adsBean.setDeformationMode(jsonObject.optInt("dm", 0)); //控制图片是否拉伸平铺展示 0—>不拉伸 1—>仅拉伸图片广告 2—>拉伸全部广告
                adsBean.setVat(jsonObject.optInt("vat", 2)); //开屏图片布局 1：top 2：center 3：buttom 4：allcenter

                // 0-	普通落地页 1- 广点通专用落地页，需要替换广点通的点击坐标宏以及转化上报处理
                adsBean.setAlType(jsonObject.optInt("altype", 0));
                adsBean.setGdtConversionLink(jsonObject.optString("gdt_conversion_link", "")); //广点通Android下载类广告有该字段

                adsBean.setDeeplink(jsonObject.optString("dl")); //包含deeplink的点击跳转地址，无法打开则使用al
                adsBean.setAdLink(jsonObject.optString("al", null)); //落地页链接

                //////////////////////// 下载类广告可能会填充的字段 ///////////////////
                adsBean.setdPackageName(jsonObject.optString("dpn", null)); //被下载应用的包名，Android是包名，iOS是itunesId
                adsBean.setdAppName(jsonObject.optString("dan", null)); //被下载的应用名称
                adsBean.setdAppIcon(jsonObject.optString("dai", null)); //被下载的应用图标在墙中对应详情页的图标（大）
                adsBean.setdAppSize(jsonObject.optInt("das", 0)); //被下载应用的大小
                //Android下载类广告需要处理以下字段
                adsBean.setSaUrl(parseJsonArray(jsonObject.optJSONArray("surl"))); //下载效果，下载开始
                adsBean.setFaUrl(parseJsonArray(jsonObject.optJSONArray("furl"))); //下载效果，下载完成
                adsBean.setIaUrl(parseJsonArray(jsonObject.optJSONArray("iurl"))); //下载效果，安装完成

                //错误通知字段
                adsBean.setEqs(jsonObject.optString("eqs", "")); //出错通知的信息数据,后面必须拼接一定参数后POST到错误汇报地址，见4.错误报告

                //// 重复点击的字段
                try {
                    adsBean.setClickNumLimit(jsonObject.optInt("cnl", 2));
                } catch (Exception e) {
                    adsBean.setClickNumLimit(2);
                }
                if (adsBean.getClickNumLimit() > 10 || adsBean.getClickNumLimit() < 0) {
                    adsBean.setClickNumLimit(2);
                }
                //////   未知参数   ///////////////////
                JSONArray mon = jsonObject.optJSONArray("mon");
                if (null != mon && mon.length() > 0) {
                    for (int j = 0; j < mon.length(); j++) {
                        JSONObject jsonObject2 = mon.getJSONObject(j);
                        if (jsonObject2.has("s"))
                            adsBean.setMon_s(jsonObject2.optString("s", null));
                        if (jsonObject2.has("c"))
                            adsBean.setMon_c(jsonObject2.optString("c", null));
                    }
                }
                ////////////////////    以下为聚合广告使用 ///////////////////////////
                if (null != adsBean.getServicesUrl() && !adsBean.getServicesUrl().equals("")) {
                    String servicesUrl = adsBean.getServicesUrl();
                    if (servicesUrl.endsWith("/")) {
                        servicesUrl = servicesUrl.substring(0,servicesUrl.length() - 1);
                    }
                    AdViewUtils.adfillAgent1 = servicesUrl + "/agent/click";
                    AdViewUtils.adfillAgent2 = servicesUrl + "/agent/display";
                }

                ////////////////////////////// 上报字段 ////////////////////////////////
                JSONArray ecJson = jsonObject.optJSONArray("ec"); //点击汇报url数组
                JSONObject esJson = jsonObject.optJSONObject("es"); //展示汇报Object对象
                parseExt(esJson, adsBean);
                parseExt(ecJson, adsBean);

                //设置时间戳
                adsBean.setDataTime(System.currentTimeMillis());

                adsBeanList.add(adsBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return adsBeanList;
    }

    public static AgDataBean parseAgdata(String jsonStr) {
        AgDataBean agDataBean = new AgDataBean();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            agDataBean.setAggsrc(jsonObject.optString("aggsrc"));
            agDataBean.setResAppId(jsonObject.optString("appid"));
            agDataBean.setResPosId(jsonObject.optString("posid"));
            agDataBean.setImpUrls(jsonObject.optString("impurl"));
            agDataBean.setCliUrls(jsonObject.optString("clkurl"));
            agDataBean.setFailUrls(jsonObject.optString("failurl"));
            agDataBean.setSuccUrls(jsonObject.optString("succurl"));
            agDataBean.setRequestType(jsonObject.optInt("reqtype"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agDataBean;
    }

    //native 广告内层解析
    private static void parseNative(AdsBean adsBean, JSONObject jsonObject) {
        try {
            if (jsonObject.has("video")) {
                //视频格式的native
                parseVideo(adsBean, jsonObject.getJSONObject("video"));
                return;
            }
            NativeAdBean nativeAdBean = new NativeAdBean();
            nativeAdBean.setAdId(adsBean.getIdAd());
            if (jsonObject.has("icon")) {
//                Icon数据，形如：
//                {
//                    "url": "<image-url>",
//                        "w": 75,
//                        "h": 75
//                }
                String iconStr = jsonObject.optJSONObject("icon").toString();
                nativeAdBean.setIcon(iconStr);
                JSONObject iconJson = new JSONObject(iconStr);
                if (null != iconJson && iconJson.length() > 0) {
                    if (iconJson.has("url"))
                        nativeAdBean.setIconUrl(iconJson.optString("url", null));
                    if (iconJson.has("height"))
                        nativeAdBean.setIconHeight(iconJson.optInt("h", 0));
                    if (iconJson.has("width"))
                        nativeAdBean.setIconWidth(iconJson.optInt("w", 0));
                }
            }
            if (jsonObject.has("logo")) {
                //Logo数据，数据形式如icon
//                {
//                    "url": "<image-url>",
//                        "w": 75,
//                        "h": 75
//                }
                String logoStr = jsonObject.optJSONObject("logo").toString();
                nativeAdBean.setLogo(logoStr);
                JSONObject logoJson = new JSONObject(logoStr);
                if (null != logoJson && logoJson.length() > 0) {
                    if (logoJson.has("url"))
                        nativeAdBean.setLogoUrl(logoJson.optString("url", null));
                    if (logoJson.has("h"))
                        nativeAdBean.setLogoHeight(logoJson.optInt("h", 0));
                    if (logoJson.has("w"))
                        nativeAdBean.setLogoWidth(logoJson.optInt("w", 0));
                }
            }
            if (jsonObject.has("images")) {
                //大图数据，数据元素形式如icon
//                [
//                {
//                    "url": "<image-url>",
//                        "w": 75,
//                        "h": 75
//                },
//	            ……
//                ]
                String imagesStr = jsonObject.optString("images").toString();
                JSONArray jsonArray = null;
                JSONObject imageJson = null;
                nativeAdBean.setImages(imagesStr);
                jsonArray = new JSONArray(imagesStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    imageJson = jsonArray.getJSONObject(i);
                    if (null != imageJson && imageJson.length() > 0) {
                        if (imageJson.has("url"))
                            nativeAdBean.setImageUrl(imageJson.optString("url", null));
                        if (imageJson.has("h"))
                            nativeAdBean.setImageHeight(imageJson.optInt("h", 0));
                        if (imageJson.has("w"))
                            nativeAdBean.setImageWidth(imageJson.optInt("w", 0));
                    }
                }
            }

            if (jsonObject.has("ver"))
                nativeAdBean.setVer(jsonObject.optInt("ver", 0));
            if (jsonObject.has("title")) //广告标题
                nativeAdBean.setTitle(jsonObject.optString("title", null));
            if (jsonObject.has("desc")) //广告描述内容
                nativeAdBean.setDesc(jsonObject.optString("desc", null));
            if (jsonObject.has("ctatext")) //动作行为按钮显示文本,例如 "安装"
                nativeAdBean.setCtaText(jsonObject.optString("ctatext", null));
            if (jsonObject.has("desc2"))   //补充广告描述文本
                nativeAdBean.setDesc2(jsonObject.optString("desc2", null));
            if (jsonObject.has("rating"))   //广告产品的评级，例如 "4"
                nativeAdBean.setRating(jsonObject.optString("rating", null));
            if (jsonObject.has("likes"))    //多少人喜欢或好评, 例如 "5000"
                nativeAdBean.setLikes(jsonObject.optString("likes", null));
            if (jsonObject.has("downloads"))    //多少下载或安装, 例如 "300000"
                nativeAdBean.setDownloads(jsonObject.optString("downloads", null));
            if (jsonObject.has("price"))    //产品/APP/应用内价格，包括价格单位, 例如 "￥99.0"
                nativeAdBean.setPrice(jsonObject.optString("price", null));
            if (jsonObject.has("saleprice"))    //折扣价。包括价格单位， 例如 "￥65.0"
                nativeAdBean.setSalePrice(jsonObject.optString("saleprice", null));
            if (jsonObject.has("phone"))    //电话号码
                nativeAdBean.setPhone(jsonObject.optString("phone", null));
            if (jsonObject.has("address"))  //广告产品厂家或代理商联系地址
                nativeAdBean.setAddress(jsonObject.optString("address", null));
            if (jsonObject.has("displayurl"))   //广告上显示出对应的广告内容网址
                nativeAdBean.setDisplayUrl(jsonObject.optString("displayurl", null));
            if (jsonObject.has("sponsored"))    //广告产品对应生产或销售厂商或公司名
                nativeAdBean.setSponsored(jsonObject.optString("sponsored", null));
            //wilder 2019 新加入设置 adlogo & adIcon 给app
            nativeAdBean.setAdLogoFlag(adsBean.getAdLogoUrl());
            nativeAdBean.setAdIconFlag(adsBean.getAdIconUrl());
            //wilder 2019 for privacy information
            if (jsonObject.has("pimage")) {
                nativeAdBean.setPrivacyImageUrl(jsonObject.optString("pimage", null));
            }
            if (jsonObject.has("pclick")) {
                nativeAdBean.setPrivacyClickUrl(jsonObject.optString("pclick", null));
            }
            //OMSDK native parameters,用于omsdk
            if (jsonObject.has("omurl")) {
                nativeAdBean.setOMUrl(jsonObject.optString("omurl", null));
            }
            if (jsonObject.has("omvendor")) {
                nativeAdBean.setOmVendor(jsonObject.optString("omvendor", null));
            }
            if (jsonObject.has("ompara")) {
                nativeAdBean.setOMPara(jsonObject.optString("ompara", null));
            }

            adsBean.setNativeAdBean(nativeAdBean);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //return;
    }
    //video 广告内层解析
    private static void parseVideo(AdsBean adsBean, JSONObject jsonObject) {
        try {
            adsBean.setXmlType(jsonObject.optInt("xmltype"));
            if (adsBean.getXmlType() == 1) {
                adsBean.setVastXml(jsonObject.optString("vastxml"));
            } else if (adsBean.getXmlType() == 2) {
                VideoBean videoBean = new VideoBean();
                videoBean.setAdId(adsBean.getIdAd());
                videoBean.setVideoUrl(jsonObject.optString("videourl"));
                videoBean.setIconUrl(jsonObject.optString("iconurl"));
                videoBean.setTitle(jsonObject.optString("title"));
                videoBean.setDesc(jsonObject.optString("desc"));
                videoBean.setDuration(jsonObject.optInt("duration"));
                videoBean.setIconButtonText(jsonObject.optString("iconbuttontext"));
                videoBean.setWidth(jsonObject.optInt("width"));
                videoBean.setHeight(jsonObject.optInt("height"));
                videoBean.setIconEndTime(jsonObject.getInt("iconoffsettime"));
                videoBean.setIconStartTime(jsonObject.optInt("iconstarttime"));
                if (null != jsonObject.getJSONArray("sptrackers"))
                    adsBean.setSpTrackers(parseJsonArray(jsonObject.getJSONArray("sptrackers")));
                if (null != jsonObject.getJSONArray("mptrackers"))
                    adsBean.setMpTrackers(parseJsonArray(jsonObject.getJSONArray("mptrackers")));
                if (null != jsonObject.getJSONArray("cptrackers"))
                    adsBean.setCpTrackers(parseJsonArray(jsonObject.getJSONArray("cptrackers")));
                if (jsonObject.has("ext")) {
                    JSONObject tempObject = jsonObject.optJSONObject("ext");
                    videoBean.setPreImgUrl(tempObject.optString("preimgurl"));
                    videoBean.setEndHtml(tempObject.optString("endhtml"));
                    videoBean.setEndImgUrl(tempObject.optString("endimgurl"));
                    //2019.1.2新增
                    videoBean.setEndIconUrl(tempObject.optString("endiconurl"));
                    videoBean.setEndDesc(tempObject.optString("enddesc"));
                    videoBean.setEndTitle(tempObject.optString("endtitle"));
                    videoBean.setEndComments(tempObject.optInt("endcomments"));
                    videoBean.setEndButtonText(tempObject.optString("endbuttontext"));
                    videoBean.setEndRating(tempObject.optInt("endrating"));
                    videoBean.setEndButtonUrl(tempObject.optString("endbuttonurl"));

                }
                //wilder 2019 add adlogo & adIcon
                videoBean.setAdLogoFlag(adsBean.getAdLogoUrl());
                videoBean.setAdIconFlag(adsBean.getAdIconUrl());
                //wilder 2019 for privacy information
                if (jsonObject.has("pimage")) {
                    videoBean.setPrivacyImageUrl(jsonObject.optString("pimage", null));
                }
                if (jsonObject.has("pclick")) {
                    videoBean.setPrivacyClickUrl(jsonObject.optString("pclick", null));
                }
                adsBean.setVideoBean(videoBean);
            }
            //20191106 添加pbm，video的playmethod: 1- 自动播放 3- 手动播放
            adsBean.setPlayMethod(jsonObject.optInt("pbm"));
            if (adsBean.getPlayMethod() == 3) {
                AdViewUtils.videoAutoPlay = false;
            }else {
                AdViewUtils.videoAutoPlay = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] parseJsonArray(JSONArray jsonObject) {
        if (null == jsonObject)
            return null;
        String[] urls = new String[(jsonObject).length()];
        try {
            for (int i = 0; i < (jsonObject).length(); i++) {
                urls[i] = (String) (jsonObject).get(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urls;
    }

    @SuppressLint("NewApi")
    protected static WebResourceResponse getWebResourceResponse(String addr) {
        HttpURLConnection urlConnection = null;
        WebResourceResponse response = null;
        try {
            URL url = new URL(addr);
            if (url.toString().startsWith("https://"))
                urlConnection = (HttpsURLConnection) url.openConnection();
            else
                urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(15 * 1000);
            urlConnection.setReadTimeout(15 * 1000);
            urlConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");

            urlConnection.getResponseCode();
            response = new WebResourceResponse(urlConnection.getContentType(),
                    urlConnection.getContentEncoding(),
                    urlConnection.getInputStream());
            return response;
        } catch (IOException e) {
            //出错的时候才关闭，否则不要轻易关闭
            if (null != urlConnection)
                urlConnection.disconnect();
            e.printStackTrace();
            return null;
        }

    }

    public static WebResourceResponse shouldInterceptRequest(String url, AdsBean adsBean, ApplyAdBean reqAdBean) {
        try {
            if (url == null || adsBean == null || reqAdBean == null)
                return null;
            if (url.contains(adsBean.getServicesUrl()) && containKeywords(url)) {
                String newUrl = replaceKeys(url, "0", getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), true).toString(), getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), false).toString(), reqAdBean.getLatitude(), reqAdBean.getLongitude(), reqAdBean.getUuid());
                return getWebResourceResponse(newUrl);
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    protected static boolean containKeywords(String url) {
        if (url.contains(HK_CLICKAREA)
                || url.contains(HK_RELATIVE_COORD)
                || url.contains(HK_ABSOLUTE_COORD)
                || url.contains(HK_LATITUDE)
                || url.contains(HK_LONGITUDE)
                || url.contains(HK_UUID)
                || url.contains(HK_GDT_DOWN_X)
                || url.contains(HK_GDT_DOWN_Y)
                || url.contains(HK_GDT_UP_X)
                || url.contains(HK_GDT_UP_Y))
            return true;
        return false;
    }

    /**
     * 根据宏定义替换字段 -999表示不替换
     *
     * @param ori    被替换的字符串
     * @param values 0=是否误点击；1=相对点击坐标；2=绝对点击坐标；3=经度；4=纬度；5=uuid
     * @return new String
     */
    public static String replaceKeys(String ori, String... values) {
        try {
            if (values.length == 6)
                return ori.replace(HK_CLICKAREA, values[0].equals("-999") ? HK_CLICKAREA : values[0])
                        .replace(HK_RELATIVE_COORD, values[1].equals("-999") ? HK_RELATIVE_COORD : values[1])
                        .replace(HK_ABSOLUTE_COORD, values[2].equals("-999") ? HK_ABSOLUTE_COORD : values[2])
                        .replace(HK_LATITUDE, values[3].equals("-999") ? HK_LATITUDE : values[3])
                        .replace(HK_LONGITUDE, values[4].equals("-999") ? HK_LONGITUDE : values[4])
                        .replace(HK_UUID, values[5].equals("-999") ? HK_UUID : values[5]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ori;
    }

//    protected static boolean contain

    /**
     * 解析 广告内容 es ec 字段, es - 展示汇报 ， ec - click汇报
     *
     * @param jsonObject
     * @param adsBean
     * @return
     */
    public static AdsBean parseExt(Object jsonObject, AdsBean adsBean) {
        JSONArray urlArray = null;
        HashMap<String, String[]> urlMaps = new HashMap<String, String[]>();
        if (null == jsonObject)
            return adsBean;
        try {
            if (jsonObject instanceof JSONObject) {
                Iterator<String> key = ((JSONObject) jsonObject).keys();
                while (key.hasNext()) {
                    String keyName = (String) key.next();
                    if (null != keyName && keyName.length() > 0) {
                        // value
                        urlArray = (JSONArray) ((JSONObject) jsonObject)
                                .opt(keyName);
                        String[] urls = new String[urlArray.length()];
                        for (int i = 0; i < urlArray.length(); i++) {
                            urls[i] = (String) urlArray.get(i);

                        }
                        urlMaps.put(keyName, urls);
                        adsBean.setExtSRpt(urlMaps);
                        // urlArray.
                    }
                }
            } else if (jsonObject instanceof JSONArray) {
                String[] urls = new String[((JSONArray) jsonObject).length()];
                for (int i = 0; i < ((JSONArray) jsonObject).length(); i++) {
                    urls[i] = (String) ((JSONArray) jsonObject).get(i);
                }
                urlMaps.put("0", urls);
                adsBean.setExtCRpt(urlMaps);
            } else {
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return adsBean;
    }

    /**
     * 解析竞价外层布局数据
     *
     * @param jsonStr jsonString
     * @return RetAdBean
     */
    public static RetAdBean parseRespOuterJson(String jsonStr) {

        RetAdBean retAdBean = new RetAdBean();
        JSONObject jsonObject = null;
        int result = 0;
        try {
            jsonObject = new JSONObject(jsonStr);
            result = jsonObject.optInt("res", 0);
            if (result == 1) {
                retAdBean.setResult(result);
                int actSrc = jsonObject.optInt("actSrc", 0);
                retAdBean.setAdSource(actSrc == 0 ? jsonObject.optInt("src", 0) : actSrc);
                retAdBean.setCount(jsonObject.optInt("ac", 0));
                retAdBean.setAds(jsonObject.optString("ad", null));// 广告数组，以下数据为ads元素的属性
                retAdBean.setLastAd(jsonObject.optString("la", null));// 上一条广告
                retAdBean.setSc(jsonObject.optInt("sc", 0));// 下载时二次确认
                retAdBean.setServerAgent(jsonObject.optInt("sgt",// 服务器是否代发向百度的展示
                        ConstantValues.RESP_SDKAGENT));
                retAdBean.setAgt(jsonObject.optInt("agt",
                        ConstantValues.RESP_SDKAGENT));
            } else {
                retAdBean.setResult(result);
                retAdBean.setMsg(jsonObject.optString("mg", null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retAdBean;
    }

    /**
     * 下载确认对话框
     *
     * @param context
     */
    public void createConfirmDialog(Context context, String url, boolean isAddLimit) {
        createConfirmDialog(context, null, url, isAddLimit, null, null);
    }


    /**
     * 下载确认对话框
     *
     * @param adsBean
     */
    public void createConfirmDialog(Context mContext, final AdsBean adsBean, final String url, final boolean isAddLimit,
                                    final DialogInterface.OnClickListener postitiveInterface,
                                    final DialogInterface.OnClickListener negativeInterface) {
        boolean isFinished = false;
        boolean isRoot = false;
        try {
            if (mContext instanceof Activity) {
                isFinished = ((Activity) mContext).isFinishing();
                isRoot = ((Activity) mContext).isTaskRoot();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isFinished && isRoot) {
            //已经是activity
        } else {
            mContext = AdViewUtils.getActivity(); //仅在这里要取得最顶层activity
        }

        if (null == mContext) {
            handleClick(null, -2, -2, url);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(confirmDialog_Title)
                .setMessage(confirmDialog_Message)
                .setNegativeButton(confirmDialog_NegativeButton, null != negativeInterface ? negativeInterface :
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (isAddLimit)
                                    if (null != adsBean)
                                        adsBean.setClickNumLimit(adsBean.getClickNumLimit() + 1);
                            }
                        })
                .setPositiveButton(confirmDialog_PositiveButton, null != postitiveInterface ? postitiveInterface :
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                handleClick(null, -2, -2, url);
                            }
                        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // system_alert窗口可以获得焦点，响应操作,system_overlay窗口显示的时候焦点在后面的Activity上，仍旧可以操作后面的Activity
        // 类似系统电量提示窗口或statusbar那样在service中启动窗口
        // dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        dialog.show();
    }

    /**
     * 用于生 请求广告字段. 将参数按照后面括号内三个字段进行md5加密形成的字符串{idApp + serviceId + adSize}
     *
     * @param reqAdBean
     * @return
     */
    public String makeRequestToken(ApplyAdBean reqAdBean) {
        return MD5Utils.MD5Encode(reqAdBean.getAppId()
                + reqAdBean.getServiceId() + reqAdBean.getAdSize());

    }

    /**
     * 用于生成展示/点击汇报 请求 . 将参数按照后面括号内三个字段进行md5加密形成的字符串{idApp + idad + keyDev}
     *
     * @param reqAdBean
     * @return
     */
    public String makeReportToken(ApplyAdBean reqAdBean, AdsBean adsBean) {
        return MD5Utils.MD5Encode(adsBean.getAppId() + adsBean.getIdAd() + reqAdBean.getUuid());

    }

    /**
     * no use ?
     */
    public void setAnimRotated() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(600);
        LayoutAnimationController controller = new LayoutAnimationController(
                animation, 0.25f);
        this.setLayoutAnimation(controller);
    }

    //对于html类型广告，这里是click事件的处理，
    protected void setClickMotion(final MRAIDView mraidView, final AdsBean adsBean, final Rect touchRect) {
        if (null == mraidView)
            return;
        final WebView webView = mraidView.getMraidWebView();
        if (null == webView)
            return;
        //这里将mraidview的webmotion等事件拦截到此处理，包括click
        mraidView.setWebViewMotion(new MRAIDView.WebViewMotion() {
            private long downTime;

            private void click(final MotionEvent down, final MotionEvent up) {
                long detlaTime = System.currentTimeMillis() - downTime;
                MotionEvent motionDown = MotionEvent.obtain(down.getDownTime() + detlaTime, down.getDownTime() + detlaTime, MotionEvent.ACTION_DOWN, down.getX(), down.getY(), 0);//adHeight-((adHeight*140)/(600*2))
                MotionEvent motionUp = MotionEvent.obtain(down.getDownTime() + detlaTime, down.getDownTime() + detlaTime + 50, MotionEvent.ACTION_UP, up.getX(), up.getY(), 0);
                webView.dispatchTouchEvent(motionDown);
                webView.dispatchTouchEvent(motionUp);
                motionDown.recycle();
                motionUp.recycle();
            }

            private void clickMotion(final MotionEvent down, final MotionEvent up) {
                try {
                    if (null != touchRect) {
                        if (!touchRect.contains((int) up.getX(), (int) up.getY()) &&
                                !touchRect.contains((int) down.getX(), (int) down.getY())) {
                            mraidView.resetTouchStatus();
                        } else {
                            click(down, up);
                        }
                    } else {
                        click(down, up);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean isNeedConfirm() {
                return respAdBean.getSc() == 1;
            }

            @Override
            public void onWebViewTouchDown(MotionEvent event) {
                try {
                    downTime = System.currentTimeMillis();
                    adsBean.setAction_down_x((int) event.getX());
                    adsBean.setAction_down_y((int) event.getY());
                    adsBean.setAction_up_x((int) event.getX());
                    adsBean.setAction_up_y((int) event.getY());
                    adsBean.setTouchStatus(MRAIDView.ACTION_DOWN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onWebViewClickedNormal(final MotionEvent down, final MotionEvent up) {
                //381版本开始注释
//                clickMotion(down, up);
                handleClick(down, (int) down.getX(), (int) down.getY(), null);
            }

            @Override
            public void onWebViewClickedConfirm(final MotionEvent down, final MotionEvent up) {
                final MotionEvent downCopy = MotionEvent.obtain(down);
                final MotionEvent upCopy = MotionEvent.obtain(up);
                createConfirmDialog(getContext(), adsBean, "", true, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickMotion(down, upCopy);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mraidView.resetTouchStatus();
                    }
                });
            }
        });
    }

    /**
     * 请求广告 Runnable
     *
     * @author Magic_Chen
     */
    public class InitAdRunable implements Runnable {
        private String content;
        private String url;
        private String result;
        private String cacheData;
        private int sdkType;

        public InitAdRunable(String cacheData, int type) {
            this.cacheData = cacheData;
            this.sdkType = type;
        }
        public InitAdRunable(String content, String url) {
            this.content = content;
            this.url = url;
        }
        public InitAdRunable(String content, String url, int sdkType) {
            this.content = content;
            this.url = url;
            this.sdkType = sdkType;
        }
        @Override
        public void run() {
            if (!AdViewUtils.isConnectInternet(getContext())) {
                notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR,"网络不可用");
                return;
            }
            if (TextUtils.isEmpty(cacheData) || cacheData.contains("no suitable ad")) {
                ////////////////////// 向BID服务器发送请求并取得返回值///////////////////////////
                result = AdViewUtils.getResponse(url, content, 8 * 1000);
                //AdViewUtils.logInfo("use network");
            } else {
                result = cacheData;
                // AdViewUtils.logInfo("use cache");
            }

            if (null != result) {
                // Log.i(AdViewUtils.ADVIEW, result + "");
                //wilder 2000229 for unit test
//                if (isVaildAd(result)) {
//                    notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR, "====[server] " + getAdMsg(result) + " [server]===");  ///here will cause DADI SDK trigger
//                    return;
//                }
                if (isVaildAd(result)) {
                    /* wilder 2019 for test for PDU*/
                    if(selfTestMode_downlink_fake_local_PDU && sdkType == ConstantValues.SDK_REQ_TYPE_INSTL) { //Mrec must be INSTLTYPE, SPREADTYPE,VIDEOTYPE
                        if ( selfTestMode_downlink_fake_adsMode ) {
                            //mode 1: only ads segment
                            respAdBean = test_makeBIDOuterJson(result);
                        }else {
                            //mode 2 : it's whole PDU
                            result = test_PDUResult();
                            respAdBean = parseRespOuterJson(result);
                        }
                    }else {
                        //解析resp返回的外层字段
                        respAdBean = parseRespOuterJson(result);
                    }
                }
                else {
                    //出错逻辑和打底逻辑，这里直接返回了
                    adsBean = getAdData(result, adsBean);  //wilder 2019 fix for DADISDK
                    //将server的信息返回
                    notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR, "====[server] " + getAdMsg(result) + " [server]===");  ///here will cause DADI SDK trigger
                    return;
                }
                ////////////////解析广告Ads数据/////////////////////////////////
                adsBeanList = parseRespAdsJson(respAdBean.getAds(), sdkType);
                if (adsBeanList != null && !adsBeanList.isEmpty()) {
                    adsBean = adsBeanList.get(0);       //这里取到返回的内容bean
                    // 设置sdkTYPE
                    adsBean.setSdkType(sdkType);
                    adsBean.setRawData(result);

                    initAdLogo(adsBean);
                    if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_HTML) {
                        if (null != adsBean.getXhtml()
                                && adsBean.getXhtml().length() > 0) {
                            notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_OK, "OK");
                            if (TextUtils.isEmpty(cacheData)) {
                                saveCache(sdkType, "sp_cacheData", result);
                                saveCache(sdkType, "sp_cacheTime",System.currentTimeMillis() / 1000);
                                saveCache(sdkType, adsBean.getIdAd(),System.currentTimeMillis());
                            }
                        } else {
                            notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR, "GET_HTML_AD_FAILED");
                        }
                        return;
                    } else if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO ||
                            adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO_PASTER) {
                            //目前只有标准视频广告位OK,测试模式下可接收任意视频
                            if (null != adsBean.getVastXml() && adsBean.getVastXml().length() > 0) {
                                notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_OK, "OK");
                                if (TextUtils.isEmpty(cacheData)) {
                                    saveCache(sdkType, "sp_cacheData", result);
                                    saveCache(sdkType, "sp_cacheTime", System.currentTimeMillis() / 1000);
                                    saveCache(sdkType, adsBean.getIdAd(), System.currentTimeMillis());
                                }
                            }

                    }else if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO_EMBED) {  //wilder for MREC or video
                        //横幅视频或嵌入式视频仅在sdk = Mrec的模式下有效, 测试模式下可接收任意视频
                            if (null != adsBean.getVastXml() && adsBean.getVastXml().length() > 0) {
                                notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_OK, "OK");
                                if (TextUtils.isEmpty(cacheData)) {
                                    saveCache(sdkType, "sp_cacheData", result);
                                    saveCache(sdkType, "sp_cacheTime", System.currentTimeMillis() / 1000);
                                    saveCache(sdkType, adsBean.getIdAd(), System.currentTimeMillis());
                                }
                            }
                    }else if(adsBean.getAdType() == ConstantValues.RESP_ADTYPE_NATIVE) {
                        //native
                        ArrayList<Object> nativeMaps = new ArrayList<Object>();
                        for (int i = 0; i < adsBeanList.size(); i++) {
                            if (adsBeanList.get(i).getXmlType() == 2)
                                nativeMaps.add(adsBeanList.get(i).getVideoBean());
                            else
                                nativeMaps.add(adsBeanList.get(i).getNativeAdBean());
                        }
                        notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_OK, nativeMaps);

                    }else {
                        //other mode: bitmap, mix, INSTL , etc . should have bitmap
                        if (createBitmap(adsBean)) {
                            notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_OK, "OK");
                            if (TextUtils.isEmpty(cacheData)) {
                                saveCache(sdkType, "sp_cacheData", result);
                                saveCache(sdkType, adsBean.getIdAd(),System.currentTimeMillis());
                                saveCache(sdkType, "sp_cacheTime",System.currentTimeMillis() / 1000);
                            }
                        } else {
                            notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR,"CREATE_BITMAP_FAILED");
                        }
                    }
                } else {
                    notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR, "NO_FILL");
                }
            } else {
                notifyMsg(ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR, "GET_AD_FAILED");
            }
        }

    }

    public static AdsBean getAdData(String result, AdsBean adsBean) {
        try {
            JSONObject otherJson = new JSONObject(result);
            if (otherJson.has("agdata")) {
                if (null == adsBean)
                    adsBean = new AdsBean();
                adsBean.setAgDataBean(KyAdBaseView.parseAgdata(otherJson.optString("agdata")));
            }
            if (otherJson.has("agext")) {
                if (null == adsBean)
                    adsBean = new AdsBean();
                JSONArray jsonArray = otherJson.getJSONArray("agext");

                ArrayList<AgDataBean> agDataBeanArrayList = new ArrayList<AgDataBean>();
                for (int i = 0; i < jsonArray.length(); i++)
                    agDataBeanArrayList.add(KyAdBaseView.parseAgdata((jsonArray.getJSONObject(i)).toString()));
                adsBean.setAgDataBeanList(agDataBeanArrayList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return adsBean;
    }

    public static int getAgDataBeanPosition(AdsBean adsBean, AgDataBean agDataBean) {
        try {
            if (null == agDataBean || null == adsBean || null == adsBean.getAgDataBeanList())
                return -1;
            if (adsBean.getAgDataBean() == agDataBean)
                return 0;
            for (int i = 0; i < adsBean.getAgDataBeanList().size(); i++) {
                if (adsBean.getAgDataBeanList().get(i) == agDataBean) {
                    if (i + 1 < adsBean.getAgDataBeanList().size())
                        return i + 1;
                    else return -1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    protected AdAdapterManager handlerAd(boolean isBid, int times, int adType, AgDataBean agDataBean, AdVGListener adVGListener) {

        Bundle bundle = getBundle(isBid, adType, getBitmapPath(), agDataBean, adVGListener);
        AdAdapterManager adAdapterManager = AdAdapterManager.initAd(getContext(), adType, bundle.getString("aggsrc"));
        adAdapterManager.setCallback(adVGListener);
        adAdapterManager.handleAd(getContext(), bundle);
        if (!isBid) {
            adAdapterManager.setTimeoutListener(times, agDataBean);
        }
        return adAdapterManager;
    }

    protected void calcParentLayoutSize(int adsizeType) {
        //(widler 2019) if parent layout already set, adjust show size fit for parent's app layout size
        //this function must before upload PDU, but if in init or construct process, the parent view may not be
        //confirmed, so put it here .
        View v = (View) this.getParent();
        if(v != null) {
            int w = 0, h = 0;
            w = v.getLayoutParams().width;
            h = v.getLayoutParams().height;

            if (adsizeType == ConstantValues.BANNER_REQ_SIZE_MREC ) {
                AdViewUtils.logInfo("[BANNER_REQ_SIZE_MREC] parent size = (" + w + ")x(" + h + ")");
                if (w > 0) {
                    adShowWidth = w; //(int)(w * density);
                } else {
                    //means auto fit or wrapper content
                    adShowWidth = screenWidth;
                }
                if (h > 0) {
                    adShowHeight = h;
                } else {
                    //means auto fit or wrapper content
                    adShowHeight = (int) (adShowWidth / MrecRatio);
                }
            }else if (adsizeType == ConstantValues.BANNER_REQ_SIZE_AUTO_FILL ) {
                AdViewUtils.logInfo("[BANNER_REQ_SIZE_AUTO_FILL] parent size = " + w + "x" + h);
                if (w > 0) {
                    adShowWidth = w;
                }
                if (h > 0) {
                    adShowHeight = h;
                }
            }
        }
    }

    /*(wilder 2019) this function will be called by adapter , after PDU has been received */
    protected Bundle getBundle(boolean isBid, int adType, String path, AgDataBean agDataBean, Serializable interfaces) {
        Bundle bundle = new Bundle();
        if (isBid) {
            bundle.putString("aggsrc", "9999");
        } else {
            bundle.putString("aggsrc", agDataBean.getAggsrc());
            bundle.putString("appId", agDataBean.getResAppId());
            bundle.putString("posId", agDataBean.getResPosId());
        }
        //wilder add for video type
        if (adType == ConstantValues.SDK_REQ_TYPE_VIDEO) {
            bundle.putBoolean("isPaster", false);
            bundle.putBoolean("closeable", autoCloseAble);
            bundle.putInt("vastOrientation", videoOrientation);
            bundle.putBoolean("trafficWarnEnable", trafficWarnEnable);
            bundle.putString("bgColor", bgColor.equals("#undefine") ? "#000000" : bgColor);
            bundle.putSerializable("adsBean", (Serializable)adsBean);
        }else {
            bundle.putSerializable("parentView", (Serializable) this);
        }
        //if parent size is fix, must match parent, this action must before uplink, cause in calcadSize()
        //our ad layout still not be added to parent layout
        calcParentLayoutSize(adSize);

        bundle.putIntArray("adSize", new int[]{ adShowWidth, adShowHeight });  //(wilder 2019) set display ad size , not PDU ad size
        bundle.putDouble("density", density);
        bundle.putSerializable("interface", interfaces);
        bundle.putString("bitmapPath", path);
        bundle.putInt("type", adsBean.getAdAct());
        bundle.putInt("screenWidth", screenWidth);
        bundle.putInt("screenHeight", screenHeight);

        return bundle;
    }

    /********************************** test *****************************************/
    //wilder 2019 test function
    private RetAdBean test_makeBIDOuterJson(String jsonStr) {
        String newADs;
        RetAdBean retAdBean = new RetAdBean();
        JSONObject jsonObject = null;
        int result = 0;
        try {
            jsonObject = new JSONObject(jsonStr);
            result = jsonObject.optInt("res", 0);
            if (result == 1) {
                retAdBean.setResult(result);
                int actSrc = jsonObject.optInt("actSrc", 0);
                retAdBean.setAdSource(actSrc == 0 ? jsonObject.optInt("src", 0)
                        : actSrc);
                retAdBean.setCount(jsonObject.optInt("ac", 0));

                String oldAD = jsonObject.optString("ad", null);
                newADs = "[" + test_PDUResult() + "]";

                //retAdBean.setAds(jsonObject.optString("ad", null));// 广告数组，以下数据为ads元素的属性
                retAdBean.setAds(newADs);

                retAdBean.setLastAd(jsonObject.optString("la", null));// 上一条广告
                retAdBean.setSc(jsonObject.optInt("sc", 0));// 下载时二次确认
                retAdBean.setServerAgent(jsonObject.optInt("sgt",// 服务器是否代发向百度的展示
                        ConstantValues.RESP_SDKAGENT));
                retAdBean.setAgt(jsonObject.optInt("agt",
                        ConstantValues.RESP_SDKAGENT));
            } else {
                retAdBean.setResult(result);
                retAdBean.setMsg(jsonObject.optString("mg", null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retAdBean;
    }

    private String test_PDUResult() {
            //byte[] imgdata = null;
            //get file from native source
            String abpath;
            String PDUData;
            //ByteArrayOutputStream bytestream;
            //int ch;
            //try {
                if (selfTestMode_downlink_fake_adsMode) {
                    abpath = AdViewUtils.getLineFromAssets(testPDUPath + "[testADSList].txt",getContext());
                    //abpath = getClass().getResourceAsStream(testPDUPath + "[testADSList].txt");
                }else {
                    abpath = AdViewUtils.getLineFromAssets(testPDUPath + "[testPDUList].txt", getContext());
                }
//                bytestream = new ByteArrayOutputStream();
//                while ((ch = abpath.read()) != -1) {
//                    if (ch == '\r' || ch == '\n') {
//                        break;
//                    }
//                    bytestream.write(ch);
//                }
//                imgdata = bytestream.toByteArray();
//                bytestream.close();
                //get file 's content
                String fName = abpath;//new String(abpath);

                if (selfTestMode_downlink_fake_adsMode) {
                    fName = testADSPath + fName.replace("\r\n", "");
                }else {
                    fName = testPDUPath + fName.replace("\r\n", "");
                }
                //abpath = getClass().getResourceAsStream(fName);
                //abpath = getClass().getResourceAsStream(testADSPath + "vast-hk01-at-7-00042.txt");
                PDUData = AdViewUtils.loadAssetsFile(fName, getContext());

            return PDUData;
    }

}
