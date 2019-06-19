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
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.KyViewListener;
import com.kuaiyou.interfaces.OnAdViewListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ApplyAdBean;
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
import com.qq.e.ads.ADActivity;
import com.qq.e.comm.util.StringUtil;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

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
    public static boolean selfTestMode_downlink_fake_local_PDU = false; //wilder 2019 for test PDU, can be any pdu mode, replace download PDU in Instl mode
    public static boolean selfTestMode_downlink_fake_adsMode = false; //ads mode only

    public static boolean selfTestMode_VIDEO = false; //for video type, can test paste / normal video for all
    public static boolean selfTestMode_mrecUpLinkInstPDU = false; //wilder 2019 , send Instl uplink PDU instead MREC
    public static boolean selfTestMode_mrecVideo = false; //only test mode can accept all kinds of video type PDU
    public static boolean selfTestMode_Spread = false; //spead test, for spread timeout not countdown
    private static String testPDUPath = "test/";
    private static String testADSPath = "test/ads-segment/xs/";
    /////////////////////end wilder for test ////////////////////////////

    public static ScheduledExecutorService reqScheduler;
    public ScheduledExecutorService bannerReqScheduler;

    public final static String confirmDialog_PositiveButton = "确定";
    public final static String confirmDialog_NegativeButton = "取消";
    public final static String confirmDialog_Title = "提示";
    public final static String confirmDialog_Message = "确定要查看详情吗？";
    //for video
    public final static int VIDEO_ADS_NORMAL = 6;
    public final static int VIDEO_ADS_PASTER = 7;
    // 横幅广告颜色常量定义
    public static final String[] COLOR_KEYS = {
            ConstantValues.PARENTBACKGROUNDCOLOR,
            ConstantValues.ICONBACKGROUNDCOLOR,
            ConstantValues.BEHAVEBACKGROUNDCOLOR,
            ConstantValues.TITLEBACKGROUNDCOLOR,
            ConstantValues.SUBTITLEBACKGROUNDCOLOR,
            ConstantValues.KEYWORDBACKGROUNDCOLOR};
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
    public AdsBean adsBean = null;
    protected RetAdBean retAdBean = null;
    // 广告列表
    protected ArrayList<AdsBean> adsBeanList = null;

    private KyAdBaseViewHandler handler = null;

    public static int isSupportHtml = 0;

    //GDPR
    protected String gdpr_consent = "";

    public final static int SUPPORT_GDT_AD = 1;

    public final static String SUPPORT_GDT_PLATFORM = "1006";
    public final static String SUPPORT_BAIDU_PLATFORM = "1007";
    public final static String SUPPORT_TOUTIAO_PLATFORM = "1008";
    public static int isH5Changed = 1;
    public static int batteryLevel = 100;

    protected String videoPosID = null;  //wilder 2019 for mrec
    //video used
    protected int videoOrientation = -1;
    protected boolean trafficWarnEnable = true;
    protected String bgColor = "#undefine";
    protected boolean autoCloseAble = false;
    //end video used
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
            AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
        if (null == reqScheduler || reqScheduler.isTerminated())
            reqScheduler = Executors.newScheduledThreadPool(ConstantValues.REQUEST_THREADPOOLNUM);
        if (null == bannerReqScheduler || bannerReqScheduler.isTerminated())
            bannerReqScheduler = Executors.newScheduledThreadPool(ConstantValues.REQUEST_THREADPOOLNUM);
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

    //wilder 2019 for mrec
    public void setVideoPosID(String posID) {
                videoPosID = posID;
    }
    //for GDPR
    public void setGDPRConstent(String consent){
        if(consent != null) {
            gdpr_consent = consent;
        }
    }
    public static void setIsH5Changed(int isH5Changed) {
        KyAdBaseView.isH5Changed = isH5Changed;
    }

    /**
     * 执行文件清理，至多每天一次
     */
    private void cleanCacheFile() {
        try {
            final SharedPreferences preferences = SharedPreferencesUtils
                    .getSharedPreferences(getContext(), ConstantValues.SP_ADVINFO);
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
                case ConstantValues.BANNERTYPE:
                case ConstantValues.MRECTYPE:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.banner.BannerView"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobads.AdView"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTAdNative"))
                        temp = temp + SUPPORT_TOUTIAO_PLATFORM + ",";
                    break;
                case ConstantValues.INSTLTYPE:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.interstitial.InterstitialAD"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobads.InterstitialAd"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTAdNative"))
                        temp = temp + SUPPORT_TOUTIAO_PLATFORM + ",";
                    break;
                case ConstantValues.SPREADTYPE:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.splash.SplashAD"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobads.SplashAd"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.bytedance.sdk.openadsdk.TTAdNative"))
                        temp = temp + SUPPORT_TOUTIAO_PLATFORM + ",";
                    break;
                case ConstantValues.NATIVEADTYPE:
                    if (KyAdBaseView.checkClass("com.qq.e.ads.nativ.NativeAD"))
                        temp = temp + SUPPORT_GDT_PLATFORM + ",";
                    if (KyAdBaseView.checkClass("com.baidu.mobad.feeds.BaiduNative"))
                        temp = temp + SUPPORT_BAIDU_PLATFORM + ",";
                    break;
                case ConstantValues.VIDEOTYPE:
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
            case ConstantValues.ADRTB_TYPE: // 996
                return AdViewUtils.adrtbAddr;
            case ConstantValues.ADFILL_TYPE: // 997
                return AdViewUtils.adfillAddr;
            case ConstantValues.ADBID_TYPE: // 998
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
        if (applyAdBean.getRoute() == ConstantValues.ADFILL_ROUTE)
            keys = ConstantValues.ADFILL_ANDROID;
        else if (applyAdBean.getRoute() == ConstantValues.SSP_ROUTE)
            keys = ConstantValues.SSP_ANDROID;
        else
            keys = ConstantValues.RTB_ANDROID;
        return MD5Utils.MD5Encode(applyAdBean.getBundleId()
                + applyAdBean.getAppId() + applyAdBean.getAdSize()
                + applyAdBean.getUuid() + applyAdBean.getTime() + keys);
    }

    /**
     * 点击事件： 打开网页 || 下载
     *
     * @param context
     * @param adsBean
     * @param url
     */
    public static boolean clickEvent(final Context context, final AdsBean adsBean, final String url, final ServiceConnection conn) {
        try {
            final Intent i = new Intent();
            i.putExtra("adview_url", TextUtils.isEmpty(url) ? adsBean.getAdLink()
                    : url);
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
                case ConstantValues.ACT_OPENWEB:
                    i.setClass(context, AdViewLandingPage.class);
                    if (context instanceof Activity) {
                    } else {
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    context.startActivity(i);
                    break;
                case ConstantValues.ACT_DOWNLOAD:
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
                    return true;
                case ConstantValues.ACT_WECHATAPP:
                    try {
                        if (checkClass("com.tencent.mm.opensdk.openapi.IWXAPI")) {
                            String wxAppId = adsBean.getAptAppId();
                            IWXAPI api = WXAPIFactory.createWXAPI(context, wxAppId);
                            api.registerApp(wxAppId);
                            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
                            req.userName = adsBean.getAptOrgId(); // 填小程序原始id
                            req.path = adsBean.getAptPath();                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
                            req.miniprogramType = adsBean.getAptType();// 可选打开 开发版，体验版和正式版
                            api.sendReq(req);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            Toast.makeText(getContext(), "不能多次重复点击", Toast.LENGTH_SHORT).show();
            return false;
        }
        return AdViewUtils.checkClickPermission(getContext(), ConstantValues.DOWNLOADSERVICE_DECLARATIONS, PackageManager.GET_SERVICES)
                && AdViewUtils.checkClickPermission(getContext(), ConstantValues.ADVIEWWEBVIEW_DECLARATIONS, PackageManager.GET_ACTIVITIES)
                && AdViewUtils.checkClickLimitTime(getContext(), adsBean.getSdkType(),adsBean.getIdAd())
                && isClickable;
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
    public static boolean clickEvent(Context context, AdsBean adsBean, String url) {
        String finalUrl = null;
        try {
            finalUrl = TextUtils.isEmpty(url) ? adsBean.getAdLink() : url;
            if (containKeywords(finalUrl)) {
                finalUrl = replaceHotKey4GDT(adsBean, finalUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clickEvent(context, adsBean, finalUrl, null);
    }


    public void reportLoadError(AdsBean adsBean, String key, int errorType) {
        AdViewUtils.logInfo("############## [HTML-load]reportLoadError: key = " + key + ", type=" + errorType + "##################");
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
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
            AdViewUtils.repScheduler.execute(new ClientReportRunnable(jsonObject.toString(), AdViewUtils.adbidErrorLink,ConstantValues.POST));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
            if (retAdBean.getServerAgent() == ConstantValues.SDKAGENT
                    && !TextUtils.isEmpty(adsBean.getAdLogLink())) {
                AdViewUtils.repScheduler.execute(new ClientReportRunnable("", adsBean
                        .getAdLogLink(), ConstantValues.GET));
            }
            if (retAdBean.getAgt() == ConstantValues.SDKAGENT
                    && !TextUtils.isEmpty(adsBean.getMon_s())) {
                AdViewUtils.repScheduler.execute(new ClientReportRunnable("", adsBean.getMon_s(),
                        ConstantValues.GET));
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
                        AdViewUtils.repScheduler.schedule(new ClientReportRunnable("", replaceKeys(urls[j], "0", getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getSpecialAdWidth(), adsBean.getSpecialAdHeight(), true).toString(), getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getSpecialAdWidth(), adsBean.getSpecialAdHeight(), false).toString(), applyAdBean.getLatitude(), applyAdBean.getLongitude(), applyAdBean.getUuid()),
                                        ConstantValues.GET),
                                Integer.valueOf(ketsString[i]), TimeUnit.SECONDS);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            return false;
        }
        return true;
    }

    public static WebView loadWebScript(WebView webView, String script) {
        String otherHtml = new String(ConstantValues.MRAID_SCRIPT_HTMLSTYLE);
        try {
            if (null == webView )
                return null;

            otherHtml = otherHtml.replace("__SCRIPT__", script);

            webView.loadDataWithBaseURL(ConstantValues.WEBVIEW_BASEURL,
                    otherHtml, "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return webView;
    }

    //use by load url with picture or other resource
    public static WebView loadWebContentURL(WebView webView, String bitmapURL, String adLink) {
        String imageHtml = new String(ConstantValues.MRAID_BITMAP_HTMLSTYLE);
        try {
            if (null == webView || null == bitmapURL)
                return null;

            imageHtml = imageHtml
                    .replace("IMAGE_PATH", bitmapURL)
                    .replace("BITMAP_WIDTH", /*adWidth == -99 ? "auto" : adWidth + ""*/ "100%") //(wilder) 2019 for let pic fit the view
                    .replace("BITMAP_HEIGHT", /*adHeight == -99 ? "100%" : adHeight + ""*/ "auto")
                    .replace("AD_LINK", adLink);

            webView.loadData(imageHtml,"text/html","UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return webView;
    }


    //load bitmap & picture
    public static WebView loadWebContentLocal(WebView webView, String bitmapPath, String adLink, int adWidth, int adHeight) {
        String imageHtml = new String(ConstantValues.MRAID_BITMAP_HTMLSTYLE);
        try {
            if (null == webView || null == bitmapPath)
                return null;
            if (adHeight > 0 && adWidth > 0) {
                adWidth = ((int) (adWidth / (float) density)) + 1;
                adHeight = ((int) (adHeight / (float) density)) + 1;
            }
            imageHtml = imageHtml
                    .replace("IMAGE_PATH", bitmapPath.substring( bitmapPath.lastIndexOf("/") + 1, bitmapPath.length()))
                    .replace("BITMAP_WIDTH", /*adWidth == -99 ? "auto" : adWidth + ""*/ "auto") //(wilder) 2019 for let pic fit the view
                    .replace("BITMAP_HEIGHT", /*adHeight == -99 ? "100%" : adHeight + ""*/ "100%")
                    .replace("AD_LINK", adLink);

            webView.loadDataWithBaseURL(
                    "file://" + bitmapPath.substring(0,bitmapPath.lastIndexOf("/") + 1),
                    imageHtml,
                    "text/html",
                    "UTF-8", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return webView;
    }

    /**
     * 汇报点击
     *
     * @param e
     * @param applyAdBean
     * @param adsBean
     * @param retAdBean
     */
    protected static int reportClick(MotionEvent e, int realX, int realY, ApplyAdBean applyAdBean,
                                     final AdsBean adsBean, RetAdBean retAdBean) {
        int isMissTouch = ConstantValues.CLICKERROR;
        try {
            if (e == null)
                isMissTouch = ConstantValues.CLICKNORMAL;
            else {
                try {
                    boolean isBannerType = (applyAdBean.getSdkType() == ConstantValues.BANNERTYPE ||
                            applyAdBean.getSdkType() == ConstantValues.MRECTYPE);

                    isMissTouch = isMissTouch(e, realX, realY, adsBean.getRealAdWidth(),
                            (e.getX() == -999 && e.getY() == -999) ? adsBean.getRealAdHeight() / 4 : adsBean.getRealAdHeight(),
                            isBannerType ? 6 : 16);
                } catch (Exception exepction) {
                    exepction.printStackTrace();
                    isMissTouch = ConstantValues.CLICKERROR;
                }
            }
            if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
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
                        AdViewUtils.repScheduler.schedule(new ClientReportRunnable("", replaceKeys(urls[j],
                                isMissTouch + "", getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(),
                                        adsBean.getSpecialAdWidth(), adsBean.getSpecialAdHeight(), true).toString(),
                                        getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getSpecialAdWidth(),
                                                adsBean.getSpecialAdHeight(), false).toString(),
                                        applyAdBean.getLatitude(),
                                        applyAdBean.getLongitude(),
                                        applyAdBean.getUuid()),
                                        ConstantValues.GET),
                                Integer.valueOf(ketsString[i]), TimeUnit.SECONDS);
                    }
                }
            }
            if (retAdBean.getAgt() == ConstantValues.SDKAGENT
                    && !TextUtils.isEmpty(adsBean.getMon_c())) {
                AdViewUtils.repScheduler.schedule(new ClientReportRunnable("", adsBean.getMon_c(),
                        ConstantValues.GET), 0, TimeUnit.SECONDS);
            }
        } catch (Exception exec) {
            exec.printStackTrace();
        }
        return isMissTouch;
    }

    public static void reportOtherUrls(String urls) {
        try {
            if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
            AdViewUtils.repScheduler.execute(new ClientReportRunnable("", urls, "GET"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkClickLimitNum(AdsBean adsBean, boolean isReduce) {
        if (adsBean.getClickNumLimit() <= 0)
            return false;
        else {
            if (isReduce)
                adsBean.setClickNumLimit(adsBean.getClickNumLimit() - 1);
            return true;
        }
    }

    protected ApplyAdBean initApplyBean(String appId, String posID, int routeType, int sdkType, int adCount) {

        ApplyAdBean applyAdBean = new ApplyAdBean();
        int[] width_height = AdViewUtils.getWidthAndHeight(getContext(), true, true);
        long time = System.currentTimeMillis() / 1000;

        // 设置广告的类型，0：banner 1：插屏  4：开屏 5：视频  6：原生
        applyAdBean.setSdkType(sdkType); //ok
        // 设置adview申请的ID
        applyAdBean.setAppId(appId); //ok
        // 设置广告位id，st=5|6 时必传
        applyAdBean.setAdPosId(posID); //video + natives
        // 固定值Ro, 业务类型, BID使用 ADBID_TYPE
        if (routeType == ConstantValues.ADFILL_TYPE)
            applyAdBean.setRoute(ConstantValues.ADFILL_ROUTE);      //ok
        else if (routeType == ConstantValues.ADBID_TYPE)
            applyAdBean.setRoute(ConstantValues.SSP_ROUTE);
        else if (routeType == ConstantValues.ADRTB_TYPE)
            applyAdBean.setRoute(ConstantValues.ADRTB_ROUTE);
        // 设备系统，android =>0, iOS => 1
        applyAdBean.setSystem(0); //ok
        // 设置请求的数目，广告数量，一般情况下，Icon广告10个，轮播5个, 除native外，基本设为1
        applyAdBean.setAdCount(adCount);  //ok
        //==============请求广告的尺寸===================
        if (sdkType == ConstantValues.NATIVEADTYPE ) {
            applyAdBean.setAdSize("");       //ok
        }else {
            if (sdkType == ConstantValues.VIDEOTYPE) {
                applyAdBean.setAdSize(width_height[0] + "x" + width_height[1]); //video adsize will be fullscreen
                //applyAdBean.setAdSize(adWidth_applyBean + "x" + adHeight_applyBean); //normal
            }else {
                applyAdBean.setAdSize(adWidth_applyBean + "x" + adHeight_applyBean); //normal
            }
            //applyAdBean.setAdSize(adShowWidth + "x" + adShowHeight); //here will send wanted ad size
        }
        //精确定位,经度和纬度
        String[] location = AdViewUtils.getLocation(getContext());
        if (null != location) {         //ok
            try {
                applyAdBean.setLatitude(URLEncoder.encode(location[0], Charset.forName("UTF-8").name()));
                applyAdBean.setLongitude(URLEncoder.encode(location[1], Charset.forName("UTF-8").name()));
            }catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                applyAdBean.setLatitude("");
                applyAdBean.setLongitude("");
            }
        } else {
            applyAdBean.setLatitude("");
            applyAdBean.setLongitude("");
        }
        // APP 版本
        applyAdBean.setAppVer(AdViewUtils.getAppVersionName(getContext())); //ok
        // OS 版本
        applyAdBean.setOsVer(AdViewUtils.getDevOsVer()); //ok
        // SDK 版本
        applyAdBean.setSdkVer(AdViewUtils.VERSION); //ok
        // 设备型号
        applyAdBean.setDevType(AdViewUtils.getDevType()); //ok;
        // 设备品牌
        applyAdBean.setDevBrand(AdViewUtils.getDevName());//ok
        // 设备分辨率
        applyAdBean.setResolution(width_height[0] + "x" + width_height[1]);//ok
        // 设备屏幕密度，Double类型
        applyAdBean.setDeny(AdViewUtils.getDensity(getContext())); //ok
        // 设备imei，海外版已经禁止取得，返回全0
        applyAdBean.setUuid(AdViewUtils.getImei(getContext()));//ok
        // 设备网络类型
        applyAdBean.setNetType(AdViewUtils.getNetworkType(getContext()));//ok
        // 手机运营商
        applyAdBean.setService(AdViewUtils.getServicesPro(getContext()));//ok
        // 设备类型
        applyAdBean.setDevUse(AdViewUtils.getDevUse(getContext())); //ok
        // googleplay ID
        applyAdBean.setGpId(AdViewUtils.getGpId(getContext()));//ok
        // AndroidID
        applyAdBean.setAndroid_ID(AdViewUtils.getAndroidID(getContext()));//ok
        // user agent
        applyAdBean.setUa(AdViewUtils.userAgent); //ok
        // 应用包名
        applyAdBean.setBundleId(getContext().getPackageName());//ok
        //MAC地址
        applyAdBean.setMacAddress(AdViewUtils.getMacAddress(getContext()));//ok
        // time
        applyAdBean.setTime(String.valueOf(time));//ok
        // battery，整数
        applyAdBean.setBatteryLevel(KyAdBaseView.batteryLevel);//ok
        //是否支持广点通，选填
        //requestMap.put(SUPPORTGDT, KyAdBaseView.SUPPORT_GDT_AD);
        // 秘钥token md5(appid+sn+os+nop+pack+time+secretKey)
        applyAdBean.setToken(AdViewUtils.makeBIDMd5Token(applyAdBean));//ok
        // supportGdt,打底SDK的取得决定于SDK的type,见下
        //requestMap.put(AGADN, KyAdBaseView.getAgadn(ConstantValues.NATIVEADTYPE));
        //GDPR
        if (!TextUtils.isEmpty(gdpr_consent)) {
            applyAdBean.setGdpr(1);
            applyAdBean.setConsent(gdpr_consent);
        }else {
            applyAdBean.setGdpr(0);
            applyAdBean.setConsent("0");
        }
        //end GDPR
        ///////////////// normal //////////////////////////
        //测试模式
        applyAdBean.setTestMode(0);
        //是否支持HTML
        applyAdBean.setHtml5(isSupportHtml);
        //旋转
        applyAdBean.setOrientation(AdViewUtils.getOrientation(getContext()));
        //config
        applyAdBean.setConfigVer(0);
        // wifi 信息,可选，本例未上传
        applyAdBean.setBssid_wifi(AdViewUtils.getBSSID(getContext()));
        applyAdBean.setSsid_wifi(AdViewUtils.getSSID(getContext()));

        return applyAdBean;
    }

    //组合requst 请求字串
    protected String getApplyInfoContent(ApplyAdBean applyAdBean) {
        String buffer = "bi=" + applyAdBean.getBundleId() //包名
                + "&an=" + applyAdBean.getAppName() // 应用名
                + "&aid=" + applyAdBean.getAppId()//sdk—key
                + "&posId=" + applyAdBean.getAdPosId() //======== video pos id - video used ===========
                + "&sv=" + applyAdBean.getSdkVer()//sdk版本
                + "&cv=" + applyAdBean.getConfigVer() //配置版本
                + "&sy=" + applyAdBean.getSystem()//系统
                + "&st=" + applyAdBean.getSdkType()//===============广告类型==================
                + "&as=" + applyAdBean.getAdSize()//==============尺寸===================
                + "&ac=" + applyAdBean.getAdCount()//广告条数
                + "&at=" + applyAdBean.getAdType()// =======adtype   - video used ===========
                + "&tm=" + applyAdBean.getTestMode() // 测试模式
                + "&se=" + applyAdBean.getService()//运营商
                + "&ti=" + applyAdBean.getTime()//时间
                + "&ud=" + applyAdBean.getUuid()//imei
                + "&to=" + applyAdBean.getToken()//token
                + "&re=" + applyAdBean.getResolution()//分辨率
                + "&ro=" + applyAdBean.getRoute() //业务类型
                + "&dt=" + applyAdBean.getDevType() //手机型号
                + "&db=" + applyAdBean.getDevBrand()//手机厂商
                + "&lat=" + applyAdBean.getLatitude()// 经纬度
                + "&lon=" + applyAdBean.getLongitude() //经纬度
                + "&nt=" + applyAdBean.getNetType()// 联网类型
                + "&src=" + applyAdBean.getAdSource() //广告源
                + "&du=" + applyAdBean.getDevUse()//设备类型
                + "&gd=" + applyAdBean.getGpId()//谷歌id
                + "&ua=" + applyAdBean.getUa()// useragent
                + "&andid=" + applyAdBean.getAndroid_ID() // android id
                + "&html5=" + applyAdBean.getHtml5() // 是否支持html
                + "&deny=" + applyAdBean.getDeny()//屏幕密度
                + "&blac=" + applyAdBean.getBlac()// 位置区域码
                + "&cid=" + applyAdBean.getCid()// 基站编号
                + "&ov=" + applyAdBean.getOsVer() // 版本号
                + "&mc=" + applyAdBean.getMac()
                + "&av=" + applyAdBean.getAppVer()//appVersion
                + "&bty=" + applyAdBean.getBatteryLevel()
                + "&supGdtUrl=" + SUPPORT_GDT_AD
                + "&agadn=" + getAgadn(applyAdBean.getSdkType())
                + "&apt=" + isSupportWXAPI()
                + "&hv=" + applyAdBean.getOrientation()
                + "&gdpr=" + applyAdBean.getGdpr()              //GDPR - 0或者1
                + "&consent=" + applyAdBean.getConsent();       //vendor string,由app端提供
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
        if (adType == ConstantValues.VIDEO ||
            adType == ConstantValues.VIDEO_EMBED ||
                adType == ConstantValues.VIDEO_PASTER)
            return true;

        return false;
    }

    public static String getActIcon(int act) {
        String iconPath = "icon_web.png";
        switch (act) {
            case ConstantValues.ACT_OPENWEB:
                iconPath = "icon_web.png";
                break;
            case ConstantValues.ACT_DOWNLOAD:
                iconPath = "icon_down.png";
                break;
            case ConstantValues.ACT_OPENMAP:
                iconPath = "icon_maps.png";
                break;
            case ConstantValues.ACT_SENDMSG:
                iconPath = "icon_ems.png";
                break;
            case ConstantValues.ACT_SENDEMAIL:
                iconPath = "icon_email.png";
                break;
            case ConstantValues.ACT_CALL:
                iconPath = "icon_call.png";
                break;
            case ConstantValues.ACT_PALYVIDEO:
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
        DisplayMetrics displayMetrics = getContext().getApplicationContext().getResources()
                .getDisplayMetrics();
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
            // 初始化为--INSTL_SIZE = 4
            case ConstantValues.BANNER_480X75:
                adShowWidth = (int) (480 * density);
                adShowHeight = (int) (75 * density);
                break;
            case ConstantValues.BANNER_728X90:
                adShowWidth = (int) (728 * density);
                adShowHeight = (int) (90 * density);
                break;
            case ConstantValues.BANNER_AUTO_FILL:
                adShowWidth = screenWidth;
                adShowHeight = (int) (screenWidth / 6.4);
                break;
            case ConstantValues.BANNER_MREC:  //300/250 = 1.2 / 1.35 will be fit
                //adShowWidth = screenWidth;
                //adShowHeight = (int) (adShowWidth / 1.5);
                adShowWidth = (int) (300 * density);
                adShowHeight = (int) (250 * density);

                break;
            case ConstantValues.BANNER_SMART:
                adShowWidth = (int) (320 * density);
                adShowHeight = (int) (50 * density);
                break;

            //INSTL & SPREAD use this
            case ConstantValues.INSTL_SIZE:
                adShowWidth = (int) (300 * density);
                adShowHeight = (int) (300 * density);
                break;

            case ConstantValues.INSTL_300X250:
                adShowWidth = (int) (300 * density);
                adShowHeight = (int) (250 * density);
                break;
            case ConstantValues.INSTL_600X500:
                adShowWidth = (int) (600 * density);
                adShowHeight = (int) (500 * density);
                break;
            case ConstantValues.INSTL_320X480:
                adShowWidth = (int) (320 * density);
                adShowHeight = (int) (480 * density);
                break;
        }

        //wilder 2019
        if (adSize == ConstantValues.BANNER_MREC) {
            if (selfTestMode_mrecUpLinkInstPDU) {
                //uplink apply bean with instl size
                adWidth_applyBean = (int) (300 * density);
                adHeight_applyBean = (int) (300 * density);
            }else {
                //use MREC size
                adWidth_applyBean = (int) (300 * density);
                adHeight_applyBean = (int) (250 * density);
            }
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
//            int adWidth = width == -1 ? adsBean.getRealAdWidth() : width;
//            int adHeight = height == -1 ? adsBean.getRealAdHeight() : height;
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
                case ConstantValues.BANNERTYPE:
                case ConstantValues.MRECTYPE:
                    break;
                case ConstantValues.INSTLTYPE:
                    delta = adsBean.getRealAdWidth() / (dpi >= 320 ? 600 : 300);
                    break;
                case ConstantValues.SPREADTYPE:
                    delta = adsBean.getRealAdWidth() / (dpi >= 320 ? 640 : 320);
                    break;
                case ConstantValues.NATIVEADTYPE:
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

    public static HashMap<String, String> getHK_Values(Context context, int x, int y, boolean isFinished, boolean hasError, Bundle bundle) {
        HashMap<String, String> hk_Map = new HashMap<String, String>();
        try {
            String[] location = AdViewUtils.getLocation(context);
            boolean isLand = false;
            try {
                isLand = ((Activity) context).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || ((Activity) context).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject clickJson = getClickArea(x, y, bundle.getInt("desireWidth"), bundle.getInt("desireHeight"), false);
            hk_Map.put(ConstantValues.HK_CLICKAREA, "0");
            hk_Map.put(ConstantValues.HK_RELATIVE_COORD, getClickArea(x, y, bundle.getInt("desireWidth"), bundle.getInt("desireHeight"), true).toString());
            hk_Map.put(ConstantValues.HK_ABSOLUTE_COORD, clickJson.toString());
            hk_Map.put(ConstantValues.HK_LONGITUDE, location[1]);
            hk_Map.put(ConstantValues.HK_LATITUDE, location[0]);
            hk_Map.put(ConstantValues.HK_UUID, AdViewUtils.getImei(context));
            hk_Map.put(ConstantValues.HK_GDT_DOWN_X, clickJson.has("down_x") ? clickJson.getString("down_x") : "-999");
            hk_Map.put(ConstantValues.HK_GDT_DOWN_Y, clickJson.has("down_y") ? clickJson.getString("down_y") : "-999");
            hk_Map.put(ConstantValues.HK_GDT_UP_X, clickJson.has("up_x") ? clickJson.getString("up_x") : "-999");
            hk_Map.put(ConstantValues.HK_GDT_UP_Y, clickJson.has("up_y") ? clickJson.getString("up_y") : "-999");
            hk_Map.put(ConstantValues.HK_DURATION, bundle.getInt("duration") + "");// + "");
            hk_Map.put(ConstantValues.HK_BEGINTIME, bundle.getInt("lastPauseVideoTime") + "");
            hk_Map.put(ConstantValues.HK_ENDTIME, bundle.getInt("currentVideoPlayTime") + "");
            hk_Map.put(ConstantValues.HK_FIRST_FRAME, bundle.getInt("lastPauseVideoTime") == 0 ? "1" : "0");
            hk_Map.put(ConstantValues.HK_LAST_FRAME, isFinished ? "1" : "0");
            hk_Map.put(ConstantValues.HK_SCENE, isLand ? "4" : "2");
            hk_Map.put(ConstantValues.HK_TYPE, bundle.getInt("lastPauseVideoTime") == 0 ? "1" : "2");
            hk_Map.put(ConstantValues.HK_BEHAVIOR, "1");
            hk_Map.put(ConstantValues.HK_STATUS, hasError ? "2" : "1");
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
                ConstantValues.SP_INSTLINFO, Context.MODE_PRIVATE);
        cacheTime = preferences.getLong("sp_cacheTime", 0l);
        cacheData = preferences.getString("sp_cacheData", null);
        if (null != cacheData
                && System.currentTimeMillis() / 1000 - cacheTime <= ConstantValues.DEFAULTCACHEPEROID) {
            // Log.i(AdViewUtils.ADVIEW, "cache " + cacheData);
            retAdBean = parseBIDOuterJson(cacheData);
            if (retAdBean.getResult() != 0)
                adsBeanList = parseFromAds(retAdBean.getAds(), type);
            else
                return false;
            if (adsBeanList != null && !adsBeanList.isEmpty()) {
                adsBean = adsBeanList.get(0);
                //html类型不加载缓存
                if (!TextUtils.isEmpty(adsBean.getXhtml()))
                    return false;
                // 设置sdkTYPE
                adsBean.setSdkType(type);

                if (adsBean.getAdType() == ConstantValues.HTML) {
                    if (null != adsBean.getXhtml()
                            && adsBean.getXhtml().length() > 0)
                        // notifyMsg(ConstantValues.NOTIFYRECEIVEADOK, "OK");
                        return true;
                }
                if (createBitmap(adsBean)) {
                    // notifyMsg(ConstantValues.NOTIFYRECEIVEADOK, "OK");
                    return true;
                }
            }
        }
        return false;
    }

    public void setOnAdSpreadListener(OnAdViewListener onAdViewListener) {
        this.onAdSpreadListener = onAdViewListener;
    }

    public void setOnAdViewListener(OnAdViewListener onAdViewListener) {
        this.onAdViewListener = onAdViewListener;
    }

    public void setOnAdInstlListener(OnAdViewListener onAdViewListener) {
        this.onAdInstlListener = onAdViewListener;
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
            return ConstantValues.CLICKNORMAL;
        float x = event.getX();
        float y = event.getY();
        if ((x == -1 && y == -1) || (x == -999 && y == -999))
            return ConstantValues.CLICKNORMAL;
        return x >= adWidth / 16 && x <= adWidth * 15 / 16
                && y >= adHeight / value && y <= adHeight * (value - 1) / value ? ConstantValues.CLICKNORMAL
                : ConstantValues.CLICKERROR;
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
        // Log.i("adview", "saveCache " + type);
        SharedPreferences preferences = null;
        if (type == ConstantValues.SPREADTYPE) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_SPREADINFO, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.INSTLTYPE) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_INSTLINFO, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.BANNERTYPE) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_BANNERINFO, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.MRECTYPE) {
            preferences = getContext().getSharedPreferences(ConstantValues.SP_BANNERINFO, Context.MODE_PRIVATE);
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
     * 解析竞价内层广告数组
     *
     * @param jsonStr
     * @return 广告队列
     */
    public static ArrayList<AdsBean> parseFromAds(String jsonStr, int sdkType) {
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
                adsBean.setAdIcon(jsonObject.optString("aic", null));
                adsBean.setAdInfo(jsonObject.optString("ai", null));
                JSONArray adPics = jsonObject.optJSONArray("api");
                if (null != adPics && adPics.length() > 0) {
                    adsBean.setAdPic(adPics.getString(0));
                }
                adsBean.setAdLink(jsonObject.optString("al", null));
                adsBean.setAdSource(jsonObject.optInt("src", 0)); //wilder 2019 here add src = channel number
                adsBean.setAdBehavIcon(jsonObject.optString("abi", null));
                adsBean.setAdBgColor(jsonObject.optString("abc", null));
                adsBean.setAdTitleColor(jsonObject.optString("atc", null));

                adsBean.setAdSubTitle(jsonObject.optString("ast", null));
                adsBean.setAdTitle(jsonObject.optString("ati", null));
                adsBean.setAdType(jsonObject.optInt("at", 0));

                adsBean.setIdAd(jsonObject.optString("adi", null));
                adsBean.setAdText(jsonObject.optString("ate", null));
                adsBean.setServicesUrl(jsonObject.optString("su", ""));
                adsBean.setGetImageUrl(jsonObject.optString("giu", ""));
                // gdt增加
                adsBean.setAlType(jsonObject.optInt("altype", 0));
                adsBean.setGdtConversionLink(jsonObject.optString("gdt_conversion_link", ""));
                // 百度增加
                adsBean.setAdAct(jsonObject.optInt("act", 0));
                adsBean.setAdPhoneNum(jsonObject.optString("apn", null));
                adsBean.setAdLogLink(jsonObject.optString("adl", null));
                adsBean.setdAppName(jsonObject.optString("dan", null));
                adsBean.setdAppIcon(jsonObject.optString("dai", null));
                adsBean.setdPackageName(jsonObject.optString("dpn", null));
                adsBean.setdAppSize(jsonObject.optInt("das", 0));

                adsBean.setRuleTime(jsonObject.optInt("rlt", 3));
                adsBean.setDelayTime(jsonObject.optInt("dlt", 0));

                adsBean.setPointArea(jsonObject.optString("pta", "(0,0,1000,1000)"));
                adsBean.setCacheTime(jsonObject.optLong("cet", 0l));
                adsBean.setSpreadType(jsonObject.optInt("sdt", 1));
                adsBean.setVat(jsonObject.optInt("vat", 2));
                adsBean.setDeformationMode(jsonObject.optInt("dm", 0));
                adsBean.setAit(jsonObject.optInt("ait", 0));

//                2018年9月7日 增加微信小程序乎起
                adsBean.setAptAppId(jsonObject.optString("aptAppId", ""));
                adsBean.setAptOrgId(jsonObject.optString("aptOrgId", ""));
                adsBean.setAptPath(jsonObject.optString("aptPath", ""));
                adsBean.setAptType(jsonObject.optInt("aptType", 0));

//                adsBean.setAdTitle("ceshi ggggggg");
//                adsBean.setPointArea("(0,0,1000,500)");
//                adsBean.setSpreadType(2);
//                adsBean.setDeformationMode(1);
//                adsBean.setVat(2);
                if (jsonObject.has("video")) {
                    parseVideo(adsBean, jsonObject.getJSONObject("video"));
                }
                parseNativeAd(jsonObject.optString("native"), adsBean);

                //2016-5-6
                adsBean.setDeeplink(jsonObject.optString("dl"));
                adsBean.setFaUrl(parseJsonArray(jsonObject.optJSONArray("furl")));
                adsBean.setSaUrl(parseJsonArray(jsonObject.optJSONArray("surl")));
                adsBean.setIaUrl(parseJsonArray(jsonObject.optJSONArray("iurl")));

                //2016-8-18
                adsBean.setAdIconUrl(jsonObject.optString("adIcon", ""));
                adsBean.setAdLogoUrl(jsonObject.optString("adLogo", ""));

                adsBean.setEqs(jsonObject.optString("eqs", ""));

                try {
                    adsBean.setClickNumLimit(jsonObject.optInt("cnl", 2));
                } catch (Exception e) {
                    adsBean.setClickNumLimit(2);
                }
                if (adsBean.getClickNumLimit() > 10
                        || adsBean.getClickNumLimit() < 0) {
                    adsBean.setClickNumLimit(2);
                }

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

                if (null != adsBean.getServicesUrl()
                        && !adsBean.getServicesUrl().equals("")) {
                    String servicesUrl = adsBean.getServicesUrl();
                    if (servicesUrl.endsWith("/")) {
                        servicesUrl = servicesUrl.substring(0,
                                servicesUrl.length() - 1);
                    }
                    AdViewUtils.adfillAgent1 = servicesUrl + "/agent/click";
                    AdViewUtils.adfillAgent2 = servicesUrl + "/agent/display";
                }
                if (null != adsBean.getAdPic()
                        && !adsBean.getAdPic().equals(""))
                    adsBean.setAdPic(adsBean.getAdPic().replace("\"", "")
                            .replace("[", "").replace("]", ""));
                adsBean.setXhtml(jsonObject.optString("xs", null));

                // 放置超范围，不做处理，使用默认值
                boolean isValid = false;
                if (jsonObject.has("as")) {
                    String adSize = jsonObject.optString("as", null);
                    if (adSize.matches("(\\d)+x(\\d)+")) {
                        try {
                            adsBean.setAdHeight(Integer.valueOf(adSize
                                    .split("x")[1]));
                            adsBean.setAdWidth(Integer.valueOf(adSize
                                    .split("x")[0]));
                            isValid = true;
                        } catch (Exception e) {
                        }
                    }
                }
                if (!isValid) {
                    switch (sdkType) {
                        case ConstantValues.BANNERTYPE:
                            adsBean.setAdWidth(320);
                            adsBean.setAdHeight(50);
                            break;
                        case ConstantValues.INSTLTYPE:
                            adsBean.setAdWidth(300);
                            adsBean.setAdHeight(300);
                            break;
                        case ConstantValues.SPREADTYPE:
                            adsBean.setAdWidth(640);
                            adsBean.setAdHeight(960);
                            break;
                        case ConstantValues.MRECTYPE:
                            adsBean.setAdWidth(300);
                            adsBean.setAdHeight(250);
                            break;
                    }
                }
                JSONArray ecJson = jsonObject.optJSONArray("ec");
                JSONObject esJson = jsonObject.optJSONObject("es");
                parseExt(esJson, adsBean);
                parseExt(ecJson, adsBean);

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

    private static AdsBean parseNativeAd(String jsonStr, AdsBean adsBean) {
        if (TextUtils.isEmpty(jsonStr))
            return adsBean;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);

            if (jsonObject.has("video")) {
                parseVideo(adsBean, jsonObject.getJSONObject("video"));
                return adsBean;
            }
            NativeAdBean nativeAdBean = new NativeAdBean();
            nativeAdBean.setAdId(adsBean.getIdAd());
            if (jsonObject.has("icon")) {
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
//                nativeAdBean.setImages(imagesStr);
//                JSONObject imageJson = new JSONObject(imagesStr);
            }
            if (jsonObject.has("ver"))
                nativeAdBean.setVer(jsonObject.optInt("ver", 0));
            if (jsonObject.has("title"))
                nativeAdBean.setTitle(jsonObject.optString("title", null));
            if (jsonObject.has("desc"))
                nativeAdBean.setDesc(jsonObject.optString("desc", null));
            if (jsonObject.has("ctatext"))
                nativeAdBean.setCtaText(jsonObject.optString("ctatext", null));
            if (jsonObject.has("desc2"))
                nativeAdBean.setDesc2(jsonObject.optString("desc2", null));
            if (jsonObject.has("rating"))
                nativeAdBean.setRating(jsonObject.optString("rating", null));
            if (jsonObject.has("likes"))
                nativeAdBean.setLikes(jsonObject.optString("likes", null));
            if (jsonObject.has("downloads"))
                nativeAdBean.setDownloads(jsonObject.optString("downloads", null));
            if (jsonObject.has("price"))
                nativeAdBean.setPrice(jsonObject.optString("price", null));
            if (jsonObject.has("saleprice"))
                nativeAdBean.setSalePrice(jsonObject.optString("saleprice", null));
            if (jsonObject.has("phone"))
                nativeAdBean.setPhone(jsonObject.optString("phone", null));
            if (jsonObject.has("address"))
                nativeAdBean.setAddress(jsonObject.optString("address", null));
            if (jsonObject.has("displayurl"))
                nativeAdBean.setDisplayUrl(jsonObject.optString("displayurl", null));
            if (jsonObject.has("sponsored"))
                nativeAdBean.setSponsored(jsonObject.optString("sponsored", null));

            adsBean.setNativeAdBean(nativeAdBean);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return adsBean;
    }

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
                adsBean.setVideoBean(videoBean);
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
            if (null != urlConnection)
                urlConnection.disconnect();
            e.printStackTrace();
            return null;
        }

    }

    public static WebResourceResponse shouldInterceptRequest(String url, AdsBean adsBean, ApplyAdBean applyAdBean) {
        try {
            if (url == null || adsBean == null || applyAdBean == null)
                return null;
            if (url.contains(adsBean.getServicesUrl()) && containKeywords(url)) {
                String newUrl = replaceKeys(url, "0", getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), true).toString(), getClickArea(adsBean.getAction_down_x(), adsBean.getAction_down_y(), adsBean.getRealAdWidth(), adsBean.getRealAdHeight(), false).toString(), applyAdBean.getLatitude(), applyAdBean.getLongitude(), applyAdBean.getUuid());
                return getWebResourceResponse(newUrl);
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    protected static boolean containKeywords(String url) {
        if (url.contains(ConstantValues.HK_CLICKAREA)
                || url.contains(ConstantValues.HK_RELATIVE_COORD)
                || url.contains(ConstantValues.HK_ABSOLUTE_COORD)
                || url.contains(ConstantValues.HK_LATITUDE)
                || url.contains(ConstantValues.HK_LONGITUDE)
                || url.contains(ConstantValues.HK_UUID)
                || url.contains(ConstantValues.HK_GDT_DOWN_X)
                || url.contains(ConstantValues.HK_GDT_DOWN_Y)
                || url.contains(ConstantValues.HK_GDT_UP_X)
                || url.contains(ConstantValues.HK_GDT_UP_Y))
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
                return ori.replace(ConstantValues.HK_CLICKAREA, values[0].equals("-999") ? ConstantValues.HK_CLICKAREA : values[0])
                        .replace(ConstantValues.HK_RELATIVE_COORD, values[1].equals("-999") ? ConstantValues.HK_RELATIVE_COORD : values[1])
                        .replace(ConstantValues.HK_ABSOLUTE_COORD, values[2].equals("-999") ? ConstantValues.HK_ABSOLUTE_COORD : values[2])
                        .replace(ConstantValues.HK_LATITUDE, values[3].equals("-999") ? ConstantValues.HK_LATITUDE : values[3])
                        .replace(ConstantValues.HK_LONGITUDE, values[4].equals("-999") ? ConstantValues.HK_LONGITUDE : values[4])
                        .replace(ConstantValues.HK_UUID, values[5].equals("-999") ? ConstantValues.HK_UUID : values[5]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ori;
    }

//    protected static boolean contain

    /**
     * 解析 广告内容 es ec 字段
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
    public static RetAdBean parseBIDOuterJson(String jsonStr) {

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
                        ConstantValues.SDKAGENT));
                retAdBean.setAgt(jsonObject.optInt("agt",
                        ConstantValues.SDKAGENT));
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
    public void createConfirmDialog(Context mContext, final AdsBean adsBean, final String url, final boolean isAddLimit, final DialogInterface.OnClickListener postitiveInterface, final DialogInterface.OnClickListener negativeInterface) {
        boolean isFinished = false;
        boolean isRoot = false;
        try {
            isFinished = ((Activity) mContext).isFinishing();
            isRoot = ((Activity) mContext).isTaskRoot();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isFinished && isRoot) ;
        else
            mContext = AdViewUtils.getActivity();

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
                                        adsBean.setClickNumLimit(adsBean
                                                .getClickNumLimit() + 1);
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
        // dialog.getWindow().setType(
        // (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        dialog.show();
    }

    /**
     * 用于生 请求广告字段. 将参数按照后面括号内三个字段进行md5加密形成的字符串{idApp + serviceId + adSize}
     *
     * @param applyAdBean
     * @return
     */
    public String makeRequestToken(ApplyAdBean applyAdBean) {
        return MD5Utils.MD5Encode(applyAdBean.getAppId()
                + applyAdBean.getServiceId() + applyAdBean.getAdSize());

    }

    /**
     * 用于生成展示/点击汇报 请求 . 将参数按照后面括号内三个字段进行md5加密形成的字符串{idApp + idad + keyDev}
     *
     * @param applyAdBean
     * @return
     */
    public String makeReportToken(ApplyAdBean applyAdBean, AdsBean adsBean) {
        return MD5Utils.MD5Encode(adsBean.getAppId() + adsBean.getIdAd()
                + applyAdBean.getUuid());

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

    protected void setClickMotion(final MRAIDView mraidView, final AdsBean adsBean, final Rect touchRect) {
        if (null == mraidView)
            return;
        final WebView webView = mraidView.getMraidWebView();
        if (null == webView)
            return;
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
                return retAdBean.getSc() == 1;
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
                notifyMsg(ConstantValues.NOTIFYRECEIVEADERROR,"Network is unavaliable");
                return;
            }
            if (TextUtils.isEmpty(cacheData) || cacheData.contains("no suitable ad")) {
                result = AdViewUtils.getResponse(url, content, 8 * 1000);
//                 Log.i(AdViewUtils.AdViewUtils.ADVIEW, "use network");
            } else {
                result = cacheData;
                // Log.i(AdViewUtils.AdViewUtils.ADVIEW, "use cache");
            }

            if (null != result) {
                // Log.i(AdViewUtils.ADVIEW, result + "");
                if (isVaildAd(result)) {
                    /* wilder 2019 for test for PDU*/
                    if(selfTestMode_downlink_fake_local_PDU && sdkType == ConstantValues.INSTLTYPE) { //Mrec must be INSTLTYPE, SPREADTYPE,VIDEOTYPE
                        if ( selfTestMode_downlink_fake_adsMode ) {
                            //mode 1: only ads segment
                            retAdBean = test_makeBIDOuterJson(result);
                        }else {
                            //mode 2 : it's whole PDU
                            result = test_PDUResult();
                            retAdBean = parseBIDOuterJson(result);
                        }
                    }else {
                        retAdBean = parseBIDOuterJson(result);
                    }
                }
                else {
                    adsBean = getAdData(result, adsBean);  //wilder 2019 fix for DADISDK
                    notifyMsg(ConstantValues.NOTIFYRECEIVEADERROR, getAdMsg(result));  ///here will cause DADI SDK trigger
                    return;
                }

                adsBeanList = parseFromAds(retAdBean.getAds(), sdkType);
                if (adsBeanList != null && !adsBeanList.isEmpty()) {
                    adsBean = adsBeanList.get(0);
                    // 设置sdkTYPE
                    adsBean.setSdkType(sdkType);
                    adsBean.setRawData(result);

                    initAdLogo(adsBean);
                    if (adsBean.getAdType() == ConstantValues.HTML) {
                        if (null != adsBean.getXhtml()
                                && adsBean.getXhtml().length() > 0) {
                            notifyMsg(ConstantValues.NOTIFYRECEIVEADOK, "OK");
                            if (TextUtils.isEmpty(cacheData)) {
                                saveCache(sdkType, "sp_cacheData", result);
                                saveCache(sdkType, "sp_cacheTime",
                                        System.currentTimeMillis() / 1000);
                                saveCache(sdkType, adsBean.getIdAd(),System.currentTimeMillis());
                            }
                        } else {
                            notifyMsg(ConstantValues.NOTIFYRECEIVEADERROR, "GET_AD_FAILED");
                        }
                        return;
                    } else if (adsBean.getAdType() == ConstantValues.VIDEO ||
                            adsBean.getAdType() == ConstantValues.VIDEO_PASTER) {
                            //目前只有标准视频广告位OK,测试模式下可接收任意视频
                            if (null != adsBean.getVastXml() && adsBean.getVastXml().length() > 0) {
                                notifyMsg(ConstantValues.NOTIFYRECEIVEADOK, "OK");
                                if (TextUtils.isEmpty(cacheData)) {
                                    saveCache(sdkType, "sp_cacheData", result);
                                    saveCache(sdkType, "sp_cacheTime", System.currentTimeMillis() / 1000);
                                    saveCache(sdkType, adsBean.getIdAd(), System.currentTimeMillis());
                                }
                            }

                    }else if (adsBean.getAdType() == ConstantValues.VIDEO_EMBED) {  //wilder for MREC or video
                        //横幅视频或嵌入式视频仅在sdk = Mrec的模式下有效, 测试模式下可接收任意视频
                            if (null != adsBean.getVastXml() && adsBean.getVastXml().length() > 0) {
                                notifyMsg(ConstantValues.NOTIFYRECEIVEADOK, "OK");
                                if (TextUtils.isEmpty(cacheData)) {
                                    saveCache(sdkType, "sp_cacheData", result);
                                    saveCache(sdkType, "sp_cacheTime", System.currentTimeMillis() / 1000);
                                    saveCache(sdkType, adsBean.getIdAd(), System.currentTimeMillis());
                                }
                            }
                    }else if(adsBean.getAdType() == ConstantValues.NATIVE) {
                        //native
                        ArrayList<Object> nativeMaps = new ArrayList<Object>();
                        for (int i = 0; i < adsBeanList.size(); i++) {
                            if (adsBeanList.get(i).getXmlType() == 2)
                                nativeMaps.add(adsBeanList.get(i).getVideoBean());
                            else
                                nativeMaps.add(adsBeanList.get(i).getNativeAdBean());
                        }
                        notifyMsg(ConstantValues.NOTIFYRECEIVEADOK, nativeMaps);

                    }else {
                        //other mode: bitmap, mix, INSTL , etc . should have bitmap
                        if (createBitmap(adsBean)) {
                            notifyMsg(ConstantValues.NOTIFYRECEIVEADOK, "OK");
                            if (TextUtils.isEmpty(cacheData)) {
                                saveCache(sdkType, "sp_cacheData", result);
                                saveCache(sdkType, adsBean.getIdAd(),System.currentTimeMillis());
                                saveCache(sdkType, "sp_cacheTime",System.currentTimeMillis() / 1000);
                            }
                        } else {
                            notifyMsg(ConstantValues.NOTIFYRECEIVEADERROR,"CREATE_BITMAP_FAILED");
                        }
                    }
                } else {
                    notifyMsg(ConstantValues.NOTIFYRECEIVEADERROR, "NO_FILL");
                }
            } else {
                notifyMsg(ConstantValues.NOTIFYRECEIVEADERROR, "GET_AD_FAILED");
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


    protected AdAdapterManager handlerAd(boolean isBid, int times, int adType, AgDataBean agDataBean, KyViewListener kyViewListener) {

        Bundle bundle = getBundle(isBid, adType, getBitmapPath(), agDataBean, kyViewListener);
        AdAdapterManager adAdapterManager = AdAdapterManager.initAd(getContext(), adType, bundle.getString("aggsrc"));
        adAdapterManager.setCallback(kyViewListener);
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

            if (adsizeType == ConstantValues.BANNER_MREC ) {
                AdViewUtils.logInfo("[BANNER_MREC] parent size = (" + w + ")x(" + h + ")");
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
            }else if (adsizeType == ConstantValues.BANNER_AUTO_FILL ) {
                AdViewUtils.logInfo("[BANNER_AUTO_FILL] parent size = " + w + "x" + h);
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
        if (adType == ConstantValues.VIDEOTYPE) {
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
    public static int getAdLayoutType(Context context, int vat, int bitmapHeight, int hasText, int hasLogo) {
        int layoutType = 0;
        int[] screenSize = AdViewUtils.getWidthAndHeight(context, false, true);
        if (bitmapHeight >= screenSize[1])
            return ConstantValues.EXTRA3;
        else if (bitmapHeight + screenSize[0] / 4 * hasLogo >= screenSize[1])
            return ConstantValues.EXTRA2;
        else if (bitmapHeight <= screenSize[1] && hasLogo == 0 && hasText == 1)
            return ConstantValues.CENTER;
        else if (bitmapHeight + screenSize[0] / 4 * hasText + screenSize[0] / 4 * hasLogo >= screenSize[1])
            return ConstantValues.EXTRA1;

        layoutType = vat;
        return layoutType;
    }

    /**
     * 获取开屏说明文字
     *
     * @param adsBean
     * @return
     */
    public String getAdText(AdsBean adsBean) {
        String tempText = "";
        if (!TextUtils.isEmpty(adsBean.getAdTitle()))
            tempText = adsBean.getAdTitle();
        if (!TextUtils.isEmpty(tempText) && !TextUtils.isEmpty(adsBean.getAdSubTitle())) {
            tempText = tempText + "" + adsBean.getAdSubTitle();
        }
        return tempText;
    }

    public static HashMap<String, Integer> getLayoutSize(Context context, AdsBean adsBean, int width_tmp, int height_tmp) {
        int screenWidth = 0;
        int screenHeight = 0;
        int instlWidth = 0;
        int instlHeight = 0;
        int bitmapWidth = 0;
        int bitmapHeight = 0;
        int frameWidth = 0;
        int frameHeight = 0;
        float zoomPercent = 0F;
        int dScale;

        HashMap<String, Integer> sizeMap = new HashMap<String, Integer>();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        density = dm.density;
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        bitmapWidth = width_tmp;
        bitmapHeight = height_tmp;

        frameWidth = screenWidth / 8 * 7;
        frameHeight = screenHeight - screenWidth / 8
                - AdViewUtils.getStatusBarHeight(context);

        if (adsBean.getAdType() == ConstantValues.MIXED) {
            if (screenWidth > screenHeight) {
                instlWidth = screenHeight * 7 / 8;
                instlHeight = instlWidth * 5 / 6;
            } else {
                instlWidth = frameWidth;
                instlHeight = instlWidth * 5 / 6;
            }
        } else {
            if (null != adsBean.getXhtml() && adsBean.getXhtml().length() > 0) {
                if ((int) (width_tmp * AdViewUtils.getDensity(context)) > screenWidth
                        || (int) (height_tmp * AdViewUtils.getDensity(context)) > screenHeight) {
                    if (((float) bitmapWidth / (float) frameWidth) > ((float) bitmapHeight / (float) frameHeight)) {
                        zoomPercent = ((float) bitmapWidth / (float) frameWidth);
                        instlWidth = (int) (bitmapWidth / zoomPercent);
                        instlHeight = (int) (bitmapHeight / zoomPercent);
                    } else {
                        zoomPercent = ((float) bitmapHeight / (float) frameHeight);
                        instlWidth = (int) (bitmapWidth / zoomPercent);
                        instlHeight = (int) (bitmapHeight / zoomPercent);
                    }
                    dScale = instlHeight * 100 / bitmapHeight;
                } else {
                    // 超范围
                    dScale = 0;
                    instlWidth = frameWidth = (int) (width_tmp * AdViewUtils.getDensity(context));
                    instlHeight = bitmapHeight = (int) (height_tmp * AdViewUtils.getDensity(context));
                }
            } else {
                if (((float) bitmapWidth / (float) frameWidth) > ((float) bitmapHeight / (float) frameHeight)) {
                    zoomPercent = ((float) bitmapWidth / (float) frameWidth);
                    instlWidth = (int) (bitmapWidth / zoomPercent);
                    instlHeight = (int) (bitmapHeight / zoomPercent);
                } else {
                    zoomPercent = ((float) bitmapHeight / (float) frameHeight);
                    instlWidth = (int) (bitmapWidth / zoomPercent);
                    instlHeight = (int) (bitmapHeight / zoomPercent);
                }
            }
        }
        instlWidth = (int) (instlWidth - density);
        instlHeight = (int) (instlHeight - density);
        sizeMap.put(ConstantValues.SCREENWIDTH, screenWidth);// 480
        sizeMap.put(ConstantValues.SCREENHEIGHT, screenHeight);// 854
        sizeMap.put(ConstantValues.FRAMEWIDTH, frameWidth);// 480/16*15
        sizeMap.put(ConstantValues.FRAMEHEIGHT, frameHeight);// 854-480/16
        sizeMap.put(ConstantValues.INSTLWIDTH, instlWidth);//
        sizeMap.put(ConstantValues.INSTLHEIGHT, instlHeight);// \
        return sizeMap;
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
                        ConstantValues.SDKAGENT));
                retAdBean.setAgt(jsonObject.optInt("agt",
                        ConstantValues.SDKAGENT));
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
            //get file
            String abpath;
            String PDUData;
            //ByteArrayOutputStream bytestream;
            //int ch;
            //try {
                if (selfTestMode_downlink_fake_adsMode) {
                    abpath = AdViewUtils.getLineFromAssets(testPDUPath + "[testADSList].txt");
                    //abpath = getClass().getResourceAsStream(testPDUPath + "[testADSList].txt");
                }else {
                    abpath = AdViewUtils.getLineFromAssets(testPDUPath + "[testPDUList].txt");
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
                PDUData = AdViewUtils.loadAssetsFile(fName);

            return PDUData;
    }

}
