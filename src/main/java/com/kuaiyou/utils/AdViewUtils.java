package com.kuaiyou.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;


import com.iab.omid.library.adview.Omid;
import com.iab.omid.library.adview.adsession.AdEvents;
import com.iab.omid.library.adview.adsession.AdSession;
import com.iab.omid.library.adview.adsession.AdSessionConfiguration;
import com.iab.omid.library.adview.adsession.AdSessionContext;
import com.iab.omid.library.adview.adsession.ErrorType;
import com.iab.omid.library.adview.adsession.Owner;
import com.iab.omid.library.adview.adsession.Partner;
import com.iab.omid.library.adview.adsession.VerificationScriptResource;
import com.iab.omid.library.adview.adsession.video.Position;
import com.iab.omid.library.adview.adsession.video.VastProperties;
import com.iab.omid.library.adview.adsession.video.VideoEvents;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.interfaces.DownloadConfirmInterface;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.ApplyAdBean;
import com.kuaiyou.obj.VideoBean;

import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import static com.iab.omid.library.adview.adsession.ErrorType.GENERIC;


public class AdViewUtils {
    /////////////////////////////// 核心版本号 ////////////////////////////////
    ////////////////改动版本号需要同时注意以下文件  ////////////////////////////
    /////////////// video_vast_js.html ， vast_vpaid_js.html在 resources中 ////

    public static int       VERSION = 415;
    public static String    ADVIEW = "AdView SDK v4.1.5.rev.002";

    private static String   OMSDK_PARTNER_NAME = "Adview";
    private static String   OMSDK_PARTNER_SDK_APP_VER = "4.1.5"; //最好是x.y

    /* -------------------------  正式线 ----------------------------------------*/
    public static String adbidAddr = "https://bid.adview.com/agent/getAd";  //正式线 server

    /* ------------------------- 测试线 -----------------------------------------*/
    //public static String adbidAddr = "https://gbjtest.adview.com/agent/getAd"; //测试线 test server

    ////////////////////////////////////////////////////////////////
    //public static boolean isTest = false;// false
    public static boolean cacheMode = false;
    public static boolean logMode = true;

    //测试客户用的客户的应用包名,正式release必须设为空
    public static String test_UserPackage = "";//"com.particlenews.newsbreak";

    //测试用客户的gpid，该值会和posid或者渠道绑定,正式release必须设为空
    //78629a4e-38b9-44c4-8df2-452242e3c14c , 这个gpid号是10002
    //3015ae49-a6f0-4f46-a193-cf310c3d50bc   这个gpid号是535
    //e46487ae-5536-49fb-aa9c-bba9419f213e; //tester number -> 535 chanel
    public static String test_UserGPID = "";

    //wilder 2019 , play all video online
    public static boolean playOnLine = true;    //是否在线播放视频
    public static boolean bitmapOnLine = true; //wilder 2019 for load bitmap online
    public static boolean videoAutoPlay = false;  //是否自动播放视频
    public static boolean htmlUseBlankErrorPage = false; //(wilder 2019) use self blank page for webview load xs error
    public static String htmlErrorPage = "http://www.adview.com/videoadtest";
    /////////////////    定制化需求    /////////////////////////
    public static boolean useIMEI = false;  //使用IMEI的话，必须提供 READ_PHONE_STATE 权限
    public static boolean useDownloadService = false;  //使用download功能对应 RESP_ACT_DOWNLOAD,现在海外版的点击打开模式是ACT_OPENWEB
                                                    //在国内使用download直接安装广告里的apk，需要以下权限和定义：
                                                    // permission : installpackage,
                                                    //<service android:name="com.kuaiyou.utils.DownloadService" />
		                                            //<activity android:name="com.kuaiyou.utils.AdActivity" />
    public static boolean useSelfIcon = true; //用于downloadservice: 下载的小图标为app的icon
    public static boolean useWechatApp = false;     //是否支持ACT_WECHATAPP，海外版目前不支持
    public static boolean useOMSDK = true;          //从3.1.5开始将始终支持omsdk
    public static boolean useCustomTab = true;      //采用chrome的Chrome Custom Tab 模式，我们默认是采用landingpage模块，即Chorme Webview
    public static boolean useHuaWeiOAID = true;     //支持华为OAID的上传在华为设备上
    public static boolean useVideoFullScreen = true; //支持mrec video全屏播放
    public static boolean useVastBehavedView = false; //支持vast的companion, icon等extension
    public static boolean useVastFinalPage = true; //支持从companion -> finalpage
    private static boolean useOMSDKFeature = false; //flag can be closed by compile

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    //用于video上行MD5
    public final static String BID_ANDROID_VIDEO = "rfkghh59eyryzx7wntlgry0mff0yx7z1";// "rfkghh59eyryzx7wntlgry0mff0yx7z1";//dur75pt6jlyim2910rroamiyv54qszuk

    public static ScheduledExecutorService repScheduler;
    public static String userAgent = "";

    private static Location location = null;

    //////////////OMSDK v1.2, these define must be same as IAB's site, see https://tools.iabtechlab.com/omsdk  /////////////////////
    private static Partner OMSDKPartner = null;
    private static List<VerificationScriptResource> verificationScriptResources;
    private static AdEvents adNativeEvents = null;
    private static VideoEvents videoNativeEvents = null;
    ///////////////////////////////// end omsdk ///////////////////////////////////////////////////
    //iab's gdpr
    //从Android 7.0开始，一个应用提供自身文件给其它应用使用时，如果给出一个file://格式的URI的话，应用会抛出FileUriExposedException
    //用以下方法解决
    static {
        try {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                builder.detectFileUriExposure();
                AdViewUtils.logInfo("detectFileUriExposure");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置所有的http连接由系统自动处理重定向
        HttpURLConnection.setFollowRedirects(true);
    }

    public AdViewUtils() {
        super();
    }

    ////////////////////  以下聚合所使用，目前海外版没有聚合 ///////////////////////////////////
    public static String adrtbAddr = "https://rtb.adview.cn/agent/getAd";
    public static String adbidErrorLink = "https://bid.adview.com/agent/reportError.php";
    public static String adfillAddr = "https://adfill.adview.cn/agent/getAd";
    public static String adfillAgent1 = "https://adfill.adview.cn/agent/click";
    public static String adfillAgent2 = "https://adfill.adview.cn/agent/display";

    public static void setServerUrl(boolean isRtb) {
        if (isRtb) {
               adfillAddr = adrtbAddr;
               adbidAddr = adrtbAddr;
        }
    }
    public static void setAdfillAddr(String adfillAddr) {
        AdViewUtils.adfillAddr = adfillAddr;
    }
    public static void setAdbidAddr(String adbidAddr) {
        AdViewUtils.adbidAddr = adbidAddr;
    }
    public static void setAdrtbAddr(String adrtbAddr) {
        AdViewUtils.adrtbAddr = adrtbAddr;
    }
    //////////////////////////////// end 聚合 ///////////////////////////////////////////////
    /**
     * 修改文本颜色
     *
     * @param content
     * @param matchRegion  匹配区域
     * @param ignoreRegion 忽略区域
     * @param color        颜色
     * @return
     */
    public static SpannableStringBuilder changeTextColorCateg(String content,
                                                              String matchRegion, String ignoreRegion, int color) {
        Pattern pattern = Pattern.compile(matchRegion);
        Matcher matcher;
        ArrayList<Integer> keyList = new ArrayList<Integer>();
        boolean isFound = false;
        matcher = pattern.matcher(content);

        while (matcher.find()) {
            int start = 0;
            int end = 0;
            String temp = matcher.group().replace("{", "").replace("}", "");
            content = content.replaceFirst(matchRegion, temp);
            start = end + content.indexOf(temp);
            end = start + temp.length();
            keyList.add(start);
            keyList.add(end);
            isFound = true;

        }
        SpannableStringBuilder style = new SpannableStringBuilder(content);
        if (isFound && keyList.size() % 2 == 0) {
            for (int i = 0; i < keyList.size(); i++) {
                if ((2 * i + 1) < keyList.size()
                        && keyList.get(2 * i) <= content.length()
                        && keyList.get(2 * i + 1) <= content.length())
                    style.setSpan(new ForegroundColorSpan(color),
                            keyList.get(2 * i), keyList.get(2 * i + 1),
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
        return style;
    }

    /**
     * 设置圆角颜色背景图片
     *
     * @param context Context
     * @param color   填充的颜色
     * @return Drawable
     */
    public static Drawable getColorDrawable(Context context, String color) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = WM.getDefaultDisplay().getWidth();
        int roundRadius = width / 72;
        return getColorDrawable(context, color, roundRadius);
    }

    /**
     * 设置圆角颜色背景图片附带边框
     *
     * @param context Context
     * @param color   填充的颜色
     * @return Drawable
     */
    public static Drawable getColorDrawableWithBounds(Context context, String color, String bColor) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = WM.getDefaultDisplay().getWidth();
        int roundRadius = width / 72;
        return getColorDrawable(context, color, roundRadius, bColor);
    }

    /**
     * 设置圆角颜色背景图片
     *
     * @param context Context
     * @param color   填充的颜色
     * @return Drawable
     */
    public static Drawable getColorDrawable(Context context, String color, int roundRadius) {
        // int strokeWidth = 5; // 3dp 描边的宽度
        // int strokeColor = Color.parseColor("#2E3135");描边的颜色
        int fillColor = Color.parseColor(color);
        float connerRadii[] = new float[]{roundRadius, roundRadius,
                roundRadius, roundRadius, roundRadius, roundRadius,
                roundRadius, roundRadius};
        GradientDrawable colorDrawable = new GradientDrawable();
        colorDrawable.setColor(fillColor);
        // colorDrawable.setCornerRadius(8);
        colorDrawable.setCornerRadii(connerRadii);
        // colorDrawable.setStroke(strokeWidth, strokeColor);
        return colorDrawable;
    }

    /**
     * 设置圆角颜色背景图片附带边框
     *
     * @param context Context
     * @param color   填充的颜色
     * @return Drawable
     */
    public static Drawable getColorDrawable(Context context, String color, int roundRadius, String bColor) {
        // int strokeWidth = 5; // 3dp 描边的宽度
        // int strokeColor = Color.parseColor("#2E3135");描边的颜色
        int fillColor = Color.parseColor(color);
        float connerRadii[] = new float[]{roundRadius, roundRadius,
                roundRadius, roundRadius, roundRadius, roundRadius,
                roundRadius, roundRadius};
        GradientDrawable colorDrawable = new GradientDrawable();
        colorDrawable.setColor(fillColor);
        // colorDrawable.setCornerRadius(8);
        colorDrawable.setStroke(2, Color.parseColor(bColor));
        colorDrawable.setCornerRadii(connerRadii);
        // colorDrawable.setStroke(strokeWidth, strokeColor);
        return colorDrawable;
    }

    /**
     * 获取导航栏高度，虚拟按键的高度
     *
     * @param context
     * @return
     */
    /**
     * 底部导航是否显示
     */
    private static boolean isNavigationBarShow(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Display display = windowManager.defaultDisplay
            WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = WM.getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back)
                return true;
            return false;
        }
    }

    public static int getNaviBarHeight(Context context){
        int barHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Display display = windowManager.defaultDisplay
            WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = WM.getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            if (realSize.y != size.y) {
                barHeight = realSize.y - size.y;
            }
        } else {
            boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
                if (rid != 0) {
                    int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                    barHeight = context.getResources().getDimensionPixelSize(resourceId);
                }
            }
        }

        return barHeight;
    }

/*    public static int getNaviBarHeight(Context context) {
        try {
//            if (!checkDeviceHasNavigationBar(context)) wilder 2020,这个判断无实际意义
//                return 0;
            if (!isNavigationBarShow(context))
                return 0;
            int result = 0;
            int resourceId = 0;
            int barH = 0;
            int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
            if (rid != 0) {
                resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                barH = context.getResources().getDimensionPixelSize(resourceId);
                return barH;
            } else
                return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }*/

    public static int getOrientation(Context context) {
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                return 0;
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                return 1;
            return orientation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

//    public static boolean checkDeviceHasNavigationBar(Context context) {
//        try {
//            if (navigationBarExist(context)) {
//                String uiStr = Integer.toBinaryString(((Activity) context).getWindow().getDecorView().getSystemUiVisibility());
//                if (uiStr.length() >= 2) {
//                    byte[] a = uiStr.getBytes();
//                    if (a[a.length - 2] == 49)
//                        return false;
//                } else return true;
//            } else
//                return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return true;
//    }

    /*wilder, 该函数有问题，此种判断不准确，准确的方法请见 getNaviBarHeight()*/
//    public static boolean navigationBarExist(Context activity) {
//        try {
//            //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
//            boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
//            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
//
//            if (hasMenuKey && hasBackKey) {
//                // 这个设备有一个导航栏
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    /**
     * 获得屏幕尺寸
     *
     * @param context
     * @param isArrange 屏幕旋转，控制返回的受不受旋转影响
     * @return
     */
    @SuppressLint("NewApi")
    public static int[] getWidthAndHeight(Context context, boolean isRaw, boolean isArrange) {
        try {
            if (null == context)
                return new int[]{0, 0};
            if (isRaw)
                return getWidthAndHeight(context, isArrange);
            DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
            int width;
            int height;
            width = displayMetrics.widthPixels;
            height = displayMetrics.heightPixels;

            if (!isArrange)
                return new int[]{width, height};
            else {
                if (height > width)
                    return new int[]{width, height};
                else {
                    return new int[]{height, width};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{0, 0};
        }
    }


    @SuppressLint("NewApi")
    public static int[] getWidthAndHeight(Context context, boolean isArrange) {
        try {
            if (null == context)
                return new int[]{0, 0};
            WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            int width;
            int height;
            if (Build.VERSION.SDK_INT < 14) {
                width = WM.getDefaultDisplay().getWidth();
                height = WM.getDefaultDisplay().getHeight();
            } else if (Build.VERSION.SDK_INT < 17 && Build.VERSION.SDK_INT >= 14) {
                Display d = WM.getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                d.getMetrics(metrics);
                width = (Integer) Display.class
                        .getMethod("getRawWidth").invoke(d);
                height = (Integer) Display.class
                        .getMethod("getRawHeight").invoke(d);
            } else {
                Point size = new Point();
                WM.getDefaultDisplay().getRealSize(size);
                width = size.x;
                height = size.y;
            }

            if (!isArrange)
                return new int[]{width, height};
            else {
                if (height > width)
                    return new int[]{width, height};
                else {
                    return new int[]{height, width};
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
            return new int[]{0, 0};
        }
    }

    public static boolean isMediaMuted(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (current == 0)
            return true;
        return false;
    }

    public static void setCurrentVolume(Context context, boolean isMuted) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, isMuted ? 0 : (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2), 0);
    }

    public static void setCurrentVolumeValue(Context context, int value) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value,0);
    }

    /**
     * 锁屏
     *
     * @param context
     * @return
     */
    public static boolean isScreenLocked(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode() ? true : false;

    }

    public static String getInstalledPackageName(Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            List<PackageInfo> packages = context.getPackageManager()
                    .getInstalledPackages(0);
            SharedPreferences preferences = SharedPreferencesUtils
                    .getSharedPreferences(context, "dev");

            // 默认一天发送一次
            if (preferences.getInt("mount_package", 0) != packages.size()
                    || System.currentTimeMillis()
                    - preferences.getLong("time_package", 0) > 24 * 60 * 60 * 1000) {

                for (int i = 0; i < packages.size(); i++) {
                    if ((packages.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        sb.append(packages.get(i).packageName + ",");
                    }
                }
                SharedPreferencesUtils
                        .commitSharedPreferencesValue(preferences,
                                "time_package", System.currentTimeMillis());
                SharedPreferencesUtils.commitSharedPreferencesValue(
                        preferences, "mount_package", packages.size());

            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return sb.toString().endsWith(",") ? sb.toString() : sb.toString()
                .substring(0, sb.toString().length() - 1);
    }

    /**
     * 网络可用
     *
     * @param context
     * @return
     */
    public static boolean isConnectInternet(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if (ni != null)
                return ni.isAvailable();
            else
                return false;
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
    }

    /**
     * 判断应用是否已安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstalled(Context context, String packageName) {
        boolean hasInstalled = false;
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> list = pm
                .getInstalledPackages(PackageManager.PERMISSION_GRANTED);
        for (PackageInfo p : list) {
            if (packageName != null && packageName.equals(p.packageName)) {
                hasInstalled = true;
                break;
            }
        }
        return hasInstalled;
    }

    /**
     * 屏幕密度：根据sdkVersion版本号
     *
     * @param context Context
     * @return
     */
    public static double getDensity(Context context) {
        double mDensity = -1.0D;
        if (mDensity == -1.0D) {
            try {
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                mDensity = dm.density;
            } catch (Exception e) {
                logError("", e);
                mDensity = 1.0D;
            }
        }

        return mDensity;
    }

    /**
     * 屏幕密度：根据sdkVersion版本号
     *
     * @param context Context
     * @return
     */
    public static float getScaledDensity(Context context) {
        float mDensity = -1.0f;
        if (mDensity == -1.0f) {
            try {
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                mDensity = dm.scaledDensity;
            } catch (Exception e) {
                logError("", e);
                mDensity = 1.0f;
            }
        }

        return mDensity;
    }

    public static boolean openDeepLink(Context context, String url) {
        try {
            if (null != url && TextUtils.isEmpty(url) || url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("javacript"))
                return false;
            else {
                int flag = 0;
                if (url.startsWith("intent://"))
                    flag = Intent.URI_INTENT_SCHEME;
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    flag = Intent.URI_ANDROID_APP_SCHEME;
                }
                Intent intent = Intent.parseUri(url, flag);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String jumpPkg = intent.getPackage();
                if (TextUtils.isEmpty(jumpPkg)) {
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    if (AdViewUtils.isInstalled(context, jumpPkg)) {
                        try {
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void sendSms(Context context, String url) {
        String[] sms;
        try {
            if (null != url) {
                sms = url.split("&");
                Uri uri = Uri.parse("smsto:" + sms[0]);
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                if (sms.length > 2)
                    it.putExtra("sms_body", sms[1]);
                context.startActivity(it);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getGdtActionLink(String origin, String clickId, int action) {
        if (!TextUtils.isEmpty(origin)) {
            return origin.replace("__ACTION_ID__", action + "").replace("__CLICK_ID__", clickId);
        }
        return null;
    }

    public static void reportEffect(String[] links) {
        if (null != links) {
            if (null == AdViewUtils.repScheduler)
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
            for (int i = 0; i < links.length; i++) {
                if (!TextUtils.isEmpty(links[i])) {
                    AdViewUtils.repScheduler.execute(new ClientReportRunnable("", links[i], false));
                }
            }
        }
    }


    /**
     * 获得post请求响应后的字符串
     *
     * @param url
     * @param content
     * @return
     */
    public static String kyPostResponse(String url, String content) {
        StringBuilder sb = new StringBuilder();
        HttpURLConnection conn = null;
        boolean status_ok = false;
        try {
            URL urls = new URL(url);
            if (url.startsWith("https://"))
                conn = (HttpsURLConnection) urls.openConnection();
            else
                conn = (HttpURLConnection) urls.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            DataOutputStream out = new DataOutputStream(
                    conn.getOutputStream());
            content = "name=" + content;
            out.write(content.getBytes());
            out.flush();
            out.close();
            InputStreamReader isr;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                isr = new InputStreamReader(conn.getInputStream());
                status_ok = true;
            } else {
                isr = new InputStreamReader(conn.getErrorStream());
            }
            BufferedReader br = new BufferedReader(isr);
            String temp;
            while (null != (temp = br.readLine())) {
                sb.append(temp);
            }
            if (!status_ok) {
                AdViewUtils.logInfo(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != conn)
                conn.disconnect();
        }
        return sb.toString();
    }

    /**
     * 换成16进制
     *
     * @param data
     * @return
     */
    public static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;// 右移四位：相当于除以16
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            WindowManager.LayoutParams attrs = ((Activity) context).getWindow().getAttributes();
            if ((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                return statusBarHeight;
            }
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 转成屏幕分辨率
     *
     * @param dipPixels 宽高尺寸
     * @param density   密度
     * @return
     */
    public static int convertToScreenPixels(int dipPixels, double density) {
        double pix = 0;

        pix = density > 0.0D ? dipPixels * density : dipPixels;

        return (int) pix;
    }

    public static byte[] fileConnect(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int ch = 0;
            while ((ch = is.read()) != -1) {
                baos.write(ch);
            }
            byte[] datas = baos.toByteArray();
            baos.close();
            baos = null;
            is.close();
            is = null;
            return datas;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * InputStream Bitmap
     *
     * @param is InputStream
     * @return bitmap
     */
    public static Bitmap getBitmap(InputStream is) {
        Bitmap bitmap = null;
        try {
            if (is == null)
                return null;

            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String getResponse(String link, String content, boolean geContent, int timeout) {
        if (null == link)
            return null;
        return getHttpsResponse(link, content, geContent, timeout);
    }

    public static String getResponse(String link, String content, int timeout) {
        return getResponse(link, content, true, timeout);
    }

    public static String getResponse(String link, String content) {
        return getResponse(link, content, ConstantValues.REQUEST_CONNECT_TIMEOUT);
    }

    public static String postResponse(String link, String content, boolean getContent) {
        if (null == link)
            return null;
        return postHttpsResponse(link, content, getContent);
    }

    public static String postResponse(String link, String content) {
        return postResponse(link, content, true);
    }

    /**
     * post 请求
     *
     * @param urlString 请求头地址
     * @param postData  请求数据内容
     * @return 请求返回内容
     */
    public static String postHttpsResponse(String urlString, String postData, boolean getContent) {
        String result = null;
//        String tempUrl;
//        boolean isFirst = true;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);

            int responseCode = -1;
//            while (isFirst || 302 == responseCode) {
//                isFirst = false;
            if (urlString.startsWith("https://"))
                conn = (HttpsURLConnection) url.openConnection();
            else
                conn = (HttpURLConnection) url.openConnection();
            if (null != conn) {
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(ConstantValues.REQUEST_CONNECT_TIMEOUT);
                conn.setReadTimeout(ConstantValues.REQUEST_CONNECT_TIMEOUT);
                /* if get image */
                conn.setUseCaches(false);

                if (null != postData) {
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Length",
                            "" + postData.length());
                    conn.setRequestProperty("User-Agent", userAgent);
                    conn.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    DataOutputStream out = new DataOutputStream(
                            conn.getOutputStream());
                    out.write(postData.getBytes());
                    out.flush();
                    out.close();
                    responseCode = conn.getResponseCode();
                }
                if (!getContent)
                    return responseCode + "";
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    result = getContentString(conn.getInputStream());
                }
                conn.disconnect();
            }
        } catch (IOException e) {
            AdViewUtils.logInfo(e.toString());
        } finally {
            try {
                if (null != conn)
                    conn.disconnect();
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * get请求
     *
     * @param link    请求头地址
     * @param content 请求数据内容
     * @return 请求返回内容
     */
    public static String getHttpsResponse(String link, String content, boolean getContent, int timeout) {
        URL url;
        HttpURLConnection connection = null;
        String result = null;
        try {
            if (TextUtils.isEmpty(content))
                url = new URL(link);
            else
                url = new URL(link + "?" + content);
            AdViewUtils.logDebug(url + "");
            int responseCode = -1;
            if (link.startsWith("https://")) {
                connection = (HttpsURLConnection) url.openConnection();
               //((HttpsURLConnection) connection).setHostnameVerifier(new BrowserCompatHostnameVerifier());
               ((HttpsURLConnection) connection).setSSLSocketFactory(new TLSSocketFactory("mode2"));
                //end test
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate");

            responseCode = connection.getResponseCode();

            if (!getContent)
                return responseCode + "";
            if (responseCode == HttpsURLConnection.HTTP_OK
                    || responseCode == HttpsURLConnection.HTTP_NO_CONTENT) {
                result = getResponseResult(connection);
            } else {
                result = "getAd_Failed : " + responseCode;
            }
        } catch (Exception e) {
            AdViewUtils.logError("", e);
        } finally {
            if (null != connection)
                connection.disconnect();
        }
        return result;
    }

    private static String getResponseResult(HttpURLConnection connection) {
        String result = null;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        InputStream sbs = null;
        String temp;
//        ByteArrayBuffer bt = new ByteArrayBuffer(4096);
        try {
            if (null != connection.getHeaderField("Content-Encoding")
                    && connection.getHeaderField("Content-Encoding").equals("gzip")) {
                GZIPInputStream gis = new GZIPInputStream(new BufferedInputStream(connection.getInputStream()));
                InputStreamReader isr = new InputStreamReader(gis);
                br = new BufferedReader(isr);
                while ((temp = br.readLine()) != null) {
                    sb.append(temp);
                }
//                while ((l = gis.read(tmp)) != -1) {
//                    bt.append(tmp, 0, l);
//                }
//                return new String(bt.toByteArray(), StandardCharsets.UTF_8.name());
                return sb.toString();
            } else if (null != connection.getHeaderField("Content-Encoding")
                    && connection.getHeaderField("Content-Encoding").equals("deflate")) {
                ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                byte[] buff = new byte[100]; // buff用于存放循环读取的临时数据
                int rc = 0;
                InputStream inputStream = connection.getInputStream();
                while ((rc = inputStream.read(buff, 0, 100)) > 0) {
                    swapStream.write(buff, 0, rc);
                }
                byte[] in_b = swapStream.toByteArray();
                sbs = new ByteArrayInputStream(DeflateUtils.inflate(in_b));
            }
            if (null == sbs)
                sbs = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(sbs, Charset.forName("UTF-8").name()));
            // sb.append(new String(bt.toByteArray(), "utf-8"));
            while (null != (temp = br.readLine())) {
                sb.append(temp);
            }
            result = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != sbs)
                    sbs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    private static String getContentString(InputStream inStream)
            throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader2 = new BufferedReader( new InputStreamReader(inStream, "UTF-8"));
        for (String s = bufferedReader2.readLine(); s != null; s = bufferedReader2.readLine()) {
            builder.append(s);
        }
        return builder.toString();
    }


    /**
     * 获得图片路径的输入流 || 路径，该函数主要用于将广告内容存到本地在播放，海外版都改为在线显示图片
     *
     * @param context
     * @param url
     * @param type    0: getInputStream 1:getPath
     * @return
     */
    public static Object getInputStreamOrPath(Context context, String url,
                                              int type) {
        if (Thread.currentThread().getName().startsWith("main")) {
            AdViewUtils.logInfo("warn:Main Thread Work,returned");
            return null;
        }
        String tempUrl;
        URL myFileUrl;
        InputStream is = null;
        String picPath = null;
        File updateDir;
        File updateFile = null;
        boolean isFirst = true;
        int responseCode = -1;
        try {
            if (null == url || url.trim().equals(""))
                return null;
            myFileUrl = new URL(url);
            long modifysince = 0;
            SharedPreferences imagemodifysince = context.getSharedPreferences(ConstantValues.SP_LASTMODIFY_FILE, Context.MODE_PRIVATE);
            SharedPreferences lastVisitTime = context.getSharedPreferences(ConstantValues.SP_LASTVISIT_FILE, Context.MODE_PRIVATE);

            String filename = url.hashCode() + "";
            String a_path = context.getFilesDir() + "/Adview/";
            //针对海外版，采用应用内不的文件系统，随着app的卸载，广告所属应当清除
            updateDir = new File(/*ConstantValues.CACHE_AD_PATH*/a_path);
            updateFile = new File(updateDir.getPath(), filename);

//            if ("mounted".equals(Environment.getExternalStorageState())) { // SD卡状态：正常挂载
            if (updateDir.exists()) {
                if (updateFile.exists()) {
                    if (System.currentTimeMillis()
                            - lastVisitTime.getLong(
                            updateFile.getAbsolutePath(), -1) <= 10 * 60 * 1000) {// 10分钟
                        if (type == 1) {
                            return updateFile.getAbsolutePath();
                        } else {
                            return new FileInputStream(updateFile);
                        }
                    }
                    modifysince = imagemodifysince.getLong(filename, 0);
                }
            } else {
                if (!updateDir.mkdirs()) {
                    return null;
                }
            }
//            }
            HttpURLConnection conn = null;

            while (isFirst || 302 == responseCode) {
                isFirst = false;
                if (url.startsWith("https://"))
                    conn = (HttpsURLConnection) myFileUrl.openConnection();
                else
                    conn = (HttpURLConnection) myFileUrl.openConnection();
                if (modifysince > 0)
                    conn.setIfModifiedSince(modifysince);
                conn.setConnectTimeout(ConstantValues.REQUEST_CONNECT_TIMEOUT);
                conn.setReadTimeout(ConstantValues.REQUEST_CONNECT_TIMEOUT);
                //(wilder 20190321) add for TLS 1.2 for socket
                if(url.startsWith("https://")) {
                    ((HttpsURLConnection) conn).setSSLSocketFactory(new TLSSocketFactory("mode2"));
                    //((HttpsURLConnection) conn).setHostnameVerifier(new BrowserCompatHostnameVerifier());
                }
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", userAgent);
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
                //end wilder
                responseCode = conn.getResponseCode();
                tempUrl = conn.getHeaderField("Location");
                if (!TextUtils.isEmpty(tempUrl))
                    myFileUrl = new URL(tempUrl);
            }
            long lastModify;
            lastModify = conn.getLastModified();

            if (responseCode == 304) {
                Editor editor = lastVisitTime.edit();
                editor.putLong(updateFile.getAbsolutePath(),
                        System.currentTimeMillis());
                editor.commit();
                if (type == 1) {
                    picPath = updateFile.getAbsolutePath();
                } else {
                    is = new FileInputStream(updateFile);
                }
            } else if (responseCode == 200) {
                is = conn.getInputStream();
                if (updateDir.exists()) {
                    try {
                        FileOutputStream fileOutputStream = null;
                        fileOutputStream = new FileOutputStream(updateFile);

                        byte[] buf = new byte[1024 * 8];
                        int ch = -1;

                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                        }
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        if (type == 1) {
                            picPath = updateFile.getAbsolutePath();
                        } else {
                            is = new FileInputStream(updateFile);
                        }

                        Editor editor = imagemodifysince.edit();
                        editor.putLong(filename, lastModify);
                        editor.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (null != updateFile)
                            updateFile.delete();
                        return null;
                    }
                }
                Editor visitEditor = lastVisitTime.edit();
                visitEditor.putLong(updateFile.getAbsolutePath(),
                        System.currentTimeMillis());
                visitEditor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (null != updateFile)
                updateFile.delete();
            return null;
        }
        if (type == 1) {
            return picPath;
        } else {
            return is;
        }
    }

    /*get input stream from remote side */
    public static Object getInputStreamURL(String nextUri) {
        int responseCode = -1;
        HttpURLConnection conn = null;
        InputStream nextIs = null;
        try {
            URL nextUrl = new URL(nextUri);
            if (nextUri.startsWith("https://")) {
                conn = (HttpsURLConnection) nextUrl.openConnection();
            } else {
                conn = (HttpURLConnection) nextUrl.openConnection();
            }
            conn.setConnectTimeout(ConstantValues.REQUEST_CONNECT_TIMEOUT);
            conn.setReadTimeout(ConstantValues.REQUEST_CONNECT_TIMEOUT);
            //conn.connect();
            if (nextUri.startsWith("https://")) {
                ((HttpsURLConnection) conn).setSSLSocketFactory(new TLSSocketFactory());
            }
            responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                nextIs = conn.getInputStream();
            } else {
                AdViewUtils.logInfo("<<<<< getInputStream() is error :  " + responseCode + ">>>>>>");
                //return VASTPlayer.ERROR_XML_OPEN_OR_READ;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return nextIs;
    }


    /**
     * 获取当前activity
     *
     * @return
     */
    public static Activity getActivity() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到设备ID：Imei
     *
     * @param context
     * @return
     */
    public static String getImei(Context context) {
/*       (wilder 2019) for oversea version, we should not allow to get IMEI etc.*/
        String devid = "0000000000000000";
        try {
            if (useIMEI) {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (tm == null)
                        return "0000000000000000";
                    devid = tm.getDeviceId();
                    if (devid != null && devid.length() > 0) {
                        AdViewUtils.logInfo("########## imei = " + devid +" ###########");
                        return devid;
                    }
            }else {
                //oversea 版本使用逻辑，16个byte是和服务器约定好的，如果是全0,则bid服务器会转去认证gpid
                return "0000000000000000";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return devid;
    }

    /**
     * Manifest中的app_name：application标签下的lable名
     */
    public static String getAppName(Context context) {
        String appName = null;
        PackageInfo packInfo = null;
        try {
            packInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            appName = packInfo.applicationInfo.loadLabel(
                    context.getPackageManager()).toString();
            appName = URLEncoder.encode(appName, "utf-8");
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }
        return appName;
    }

    /**
     * 手机SIM卡的国家代号、网络代号，对应上传字段 se ( wilder 2019 海外版无需提供）
     *
     * @param context
     * @return
     */
    public static String getServicesPro(Context context) {
/*
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null)
                return "";
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                String service = tm.getSimOperator();
                if (service != null && service.length() > 0) {
                    return service;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }
*/
        return "";
    }

    /**
     * App的版本号：Manifest中
     *
     * @param context
     * @return
     */
    public static int getAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = null;
            packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            return packInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;// packInfo.versionName;
        }
    }


    public static String getAppVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo;
            packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    /**
     * 设备型号
     *
     * @return
     */
    public static String getDevType() {
        try {

            String devType = Build.MODEL.replace(" ", "");
            return TextUtils.isEmpty(devType) ? "" : URLEncoder.encode(devType, Charset.forName("UTF-8").name());
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }

    }

    /**
     * 生成MD5加密token
     *
     * @param reqAdBean applyAdBean
     * @return token token
     */
    public static String makeBIDMd5Token(ApplyAdBean reqAdBean) {
        return MD5Utils.MD5Encode(reqAdBean.getBundleId()
                + reqAdBean.getAppId() + reqAdBean.getAdSize()
                + reqAdBean.getUuid() + reqAdBean.getTime()
                + AdViewUtils.BID_ANDROID_VIDEO);
    }

    /**
     * 安卓设备操作系统版本号
     *
     * @return
     */
    public static String getDevOsVer() {
        try {
            String buildVer = Build.VERSION.RELEASE.replace(" ", "");
            if (!TextUtils.isEmpty(buildVer) && (buildVer.matches("\\d.\\d(.\\d)?")))
                return buildVer;
            else
                return "";
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }
    }

    /**
     * 设备制造商
     *
     * @return
     */
    public static String getDevName() {
        try {
            String devName = Build.MANUFACTURER.replace(" ", "");
            return TextUtils.isEmpty(devName) ? "" : URLEncoder.encode(devName, Charset.forName("UTF-8").name());
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }

    }

    /**
     * 获取基站信息 (wilder 2019 ,海外版无需该参数）
     *
     * @throws Exception
     */
    public static int[] getCellInfo(Context context) {
        int[] loc = new int[2];
        /** 调用API获取基站信息 ,wilder 2019 set 0*/
        loc[0] = 0;
        loc[1] = 0;
/*        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // 返回值MCC + MNC
            String operator = mTelephonyManager.getNetworkOperator();
            if (TextUtils.isEmpty(operator)) {
                return null;
            }
            // int mcc = Integer.parseInt(operator.substring(0, 3));
            // int mnc = Integer.parseInt(operator.substring(3));

            CellLocation cellLocation = null;
            int lac = 0;
            int cellId = 0;

            switch (Integer.valueOf(operator)) {
                // 中国移动
                case 46000:
                case 46002:
                case 46007:
                    // 中国联通
                case 46001:
                case 46006:
                    cellLocation = mTelephonyManager.getCellLocation();
                    lac = ((GsmCellLocation) cellLocation).getLac();
                    cellId = ((GsmCellLocation) cellLocation).getCid();
                    break;
                // 中国电信
                case 46003:
                case 46005:
                    cellLocation = mTelephonyManager.getCellLocation();
                    lac = ((CdmaCellLocation) cellLocation).getNetworkId();
                    cellId = ((CdmaCellLocation) cellLocation).getBaseStationId();
                    cellId /= 16;
                    break;
            }

            loc[0] = lac;
            loc[1] = cellId;

        } catch (Exception e) {
        }*/
        return loc;
    }

//    /**
//     * 网络类型
//     *
//     * @param context instead of getNetworkType(Context context);
//     * @return 2G/3G、 WIFI、 OTHER
//     */
//    @Deprecated
//    public static String getNetType(Context context) {
//        String netType;
//        try {
//            ConnectivityManager cm = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo ni = cm.getActiveNetworkInfo();
//            if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
//                netType = "2G/3G";
//            else if (ni.getType() == ConnectivityManager.TYPE_WIFI)
//                netType = "WIFI";
//            else
//                netType = "OTHER";
//        } catch (Exception e) {
//            // TODO: handle exception
//            return "OTHER";
//        }
//        return netType;
//    }

    /**
     * 连接指定网络
     */
    public static String getSSID(Context context) {
        String ssid = "";

/*  (wilder 20190818)海外版不传该字段
        try {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            if (mWifiInfo != null) {
                ssid = mWifiInfo.getSSID();
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
*/

        return ssid;
    }

    // 得到接入点的BSSID
    public static String getBSSID(Context context) {

/*      (wilder 20190818)海外版不传该字段
        try {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? "" : mWifiInfo.getBSSID();
        } catch (Exception e) {
            // TODO: handle exception
        }
*/
        return "";
    }

    public static String getMacAddress(Context context) {
/*        (wilder 20190818) android 7.0之后google禁止取得mac地址，直接返回空串，6.0以前取得mac需要WIFI_STATE
        try {
            String macAddress;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
                if (null != info) {
                    macAddress = info.getMacAddress();
                    if (!TextUtils.isEmpty(macAddress))
                        return macAddress;
                }
                return "";
            } else {
                String interfaceName = "wlan0";
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                        continue;
                    }

                    byte[] mac = intf.getHardwareAddress();
                    if (mac == null) {
                        return "";
                    }

                    StringBuilder buf = new StringBuilder();
                    for (byte aMac : mac) {
                        buf.append(String.format("%02X:", aMac));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    return buf.toString();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } // for now eat exceptions*/
        return "";
    }


//    /**
//     * 获取手机的MAC地址
//     *
//     * @return
//     */
//    public static String getMacAddress(Context context) {
//        String str = "";
//        String macSerial = "";
//        try {
//            Process pp = Runtime.getRuntime().exec(
//                    "cat /sys/class/net/wlan0/address ");
//            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
//            LineNumberReader input = new LineNumberReader(ir);
//
//            for (; null != str; ) {
//                str = input.readLine();
//                if (str != null) {
//                    macSerial = str.trim();// 去空格
//                    break;
//                }
//            }
//            macSerial = URLEncoder.encode(macSerial, Charset.forName("UTF-8").name());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        if (TextUtils.isEmpty(macSerial)) {
//            String macStr = null;
//            try {
//                File file = new File("/sys/class/net/eth0/address");
//                if (!file.exists()) {
//                    macStr = "00:00:00:00:00:00";
//                } else {
//                    macStr = loadFileAsString("/sys/class/net/eth0/address")
//                            .toUpperCase().substring(0, 17);
//                }
//                return null == macStr ? "00:00:00:00:00:00" : macStr;
//            } catch (Exception e) {
//                e.printStackTrace();
//
//            }
//
//        }
//        return macSerial;
//    }
//
//    private static String loadFileAsString(String fileName) throws Exception {
//        FileReader reader = new FileReader(fileName);
//        String text = loadReaderAsString(reader);
//        reader.close();
//        if (!TextUtils.isEmpty(text))
//            text = URLEncoder.encode(text, Charset.forName("UTF-8").name());
//        return text;
//    }
//
//    private static String loadReaderAsString(Reader reader) throws Exception {
//        StringBuilder builder = new StringBuilder();
//        char[] buffer = new char[4096];
//        int readLength = reader.read(buffer);
//        while (readLength >= 0) {
//            builder.append(buffer, 0, readLength);
//            readLength = reader.read(buffer);
//        }
//        return builder.toString();
//    }

    /**
     * 信号类型, 对应上传的 nt 字段  （wilder 2019 海外版无需TELEPHONY_SERVICE做进一步判断，只需返回是否wifi）
     *
     * @param context
     * @return 0：UNKNOW、 1：WIFI、 2：2_G、 3： 3_G、 4：4_G、
     */
    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            String netName = ni.getSubtypeName();
            //TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                return "4G"; //(wilder 2019) 海外版直接返回4G即可
/*                switch (manager.getNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return "2G";// NETWORK_CLASS_2_G;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return "3G";// NETWORK_CLASS_3_G;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return "4G";// NETWORK_CLASS_4_G;
                    default:
                        if (netName.equalsIgnoreCase("TD-SCDMA") || netName.equalsIgnoreCase("WCDMA") || netName.equalsIgnoreCase("CDMA2000")) {
                            return "3G";
                        }
                        return "OTHER";// NETWORK_CLASS_UNKNOWN;
                }*/
            } else if (ni.getType() == ConnectivityManager.TYPE_WIFI)
                return "WIFI";
            else
                return "OTHER";
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "NONETWORK";
    }

    /**
     *  打开落地页
     */
    public static void openLandingPage(final Context context, final String url, final boolean useCustom) {
        //采用 custom tab方式
        if (TextUtils.isEmpty(url))
            return;
        AdViewUtils.logInfo("######### openLandingPage() : " + url + "#########");
        if ( useCustom && !url.contains("market://")) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));
        }else {
            //采用landingpage activity的方式来打开落地页,spread要使用这种方式，因为要判断ondestroy事件
            final Intent i = new Intent();
            i.putExtra("adview_url", url);
            i.setClass(context, AdViewLandingPage.class);

            if (context instanceof Activity) {
            } else {
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(i);
        }
    }

    /**
     * 下载 || 打开网页： 以.apk结尾的开启下载服务，否则打开网页
     *
     * @param url
     * @param context
     */
    public static void openWebBrowser(final Context context, final String url) {
        try {
            if (url.length() >= 4) { // len >="http"
                String tmpUrl = url.substring(0, 4);
                int res = tmpUrl.compareTo(new String("http"));
                if (res == 0) {
                    if (url.toLowerCase().endsWith(".apk") && AdViewUtils.useDownloadService) { //只有支持downloadservice才能下载
                        if (AdViewUtils.getNetworkType(context).equals("WIFI")) {
                            Intent i = new Intent();
                            i.setClass(context, DownloadService.class);
                            i.putExtra("adview_url", url);
                            context.startService(i);
                        } else {
                            AdViewUtils.trafficConfirmDialog(context, new DownloadConfirmInterface() {
                                @Override
                                public void confirmDownload() {
                                    Intent i = new Intent();
                                    i.setClass(context, DownloadService.class);
                                    i.putExtra("adview_url", url);
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

                    } else {
                        Intent intent = new Intent();
                        intent.setClass(context, AdViewLandingPage.class);
                        if (context instanceof Activity) {
                        } else {
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("adview_url", url);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
//                        ((Activity) context).startActivityForResult(intent, ConstantValues.ACTIVITY_REQUEST_CODE);
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static void trafficConfirmDialog(final Context context, final DownloadConfirmInterface downloadConfirmInterface) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle("流量提醒");
                    dialogBuilder.setMessage("当前处在非WIFI环境下，继续下载将会消耗流量");
                    dialogBuilder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (null != downloadConfirmInterface)
                                downloadConfirmInterface.confirmDownload();
                        }
                    });
                    dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (null != downloadConfirmInterface)
                                downloadConfirmInterface.cancelDownload();
                        }
                    });
                    dialogBuilder.setCancelable(false);
                    dialogBuilder.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (null != downloadConfirmInterface)
                        downloadConfirmInterface.error();
                }
            }
        });
    }

    public static boolean checkClickLimitTime(Context context, int type, String adId) {
        SharedPreferences preferences = null;
        if (type == ConstantValues.SDK_REQ_TYPE_SPREAD) {
            preferences = context.getSharedPreferences(ConstantValues.SP_SPREADINFO_FILE, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.SDK_REQ_TYPE_INSTL) {
            preferences = context.getSharedPreferences(ConstantValues.SP_INSTLINFO_FILE, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.SDK_REQ_TYPE_BANNER) {
            preferences = context.getSharedPreferences(ConstantValues.SP_BANNERINFO_FILE, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.SDK_REQ_TYPE_MREC) {
            preferences = context.getSharedPreferences(ConstantValues.SP_BANNERINFO_FILE, Context.MODE_PRIVATE);
        }
        if (null == preferences)
            return false;
        long getAditme = preferences.getLong(adId, 0l);
        if (System.currentTimeMillis() - getAditme >= 15 * 60 * 1000)
            // if (System.currentTimeMillis() - getAditme >= 8 * 1000)
            return false;
        else
            return true;
    }

    public static boolean checkClickPermission(Context context, String permission, int getType) {
        if (!AdViewUtils.isConnectInternet(context)) {
            AdViewUtils.logInfo("Please check network");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            if (getType == PackageManager.GET_SERVICES)
                packageManager.getServiceInfo(new ComponentName(context.getPackageName(), permission), getType);
            else if (getType == PackageManager.GET_ACTIVITIES)
                packageManager.getActivityInfo(new ComponentName(context.getPackageName(), permission), getType);
            else
                AdViewUtils.logInfo("PackageManager GETTYPE ERROR");
        } catch (NameNotFoundException e) {
            //Toast.makeText(context,"请在AndroidManifest中添加" + permission + "声明",Toast.LENGTH_SHORT).show();
            AdViewUtils.logInfo("!!!! err: pls add " + permission + "declaration in AndroidManifest.xml !!!!");
            return false;
        }
//        try {
//            packageManager.getServiceInfo(new ComponentName(
//                            context.getPackageName(), "com.kuaiyou.utils.DownloadService"),
//                    PackageManager.GET_SERVICES);
//        } catch (NameNotFoundException e) {
//            Toast.makeText(context,
//                    "请在AndroidManifest中添加com.AdVG.DownloadService声明",
//                    Toast.LENGTH_SHORT).show();
//            return false;
//        }

        return true;
    }

    public static String[] getLocation(Context context) {
        String[] locas = new String[2];
        try {
            LocationManager locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setCostAllowed(false);
            // 设置位置服务免费
            criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 设置水平位置精度
            // getBestProvider 只有允许访问调用活动的位置供应商将被返回
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setCostAllowed(true); // 设置是否允许付费服
            criteria.setSpeedRequired(false);
            criteria.setBearingRequired(false); // 设置是否需要返回方位信息，不要求方位
            criteria.setAltitudeRequired(false); // 设置是否需要返回海拔信息,不要求海拔
            String providerName = locationManager.getBestProvider(criteria,
                    true);
            if (null == providerName)
                return new String[]{"", ""};
            // Logger.showLog5("providerName:"+providerName);
            if (locationManager != null && providerName != null) {
                location = locationManager.getLastKnownLocation(providerName);
            }
            if (null != location) {
                locas[0] = location.getLatitude() + "";
                locas[1] = location.getLongitude() + "";
                return locas;
            }
        } catch (Exception e) {
            return new String[]{"", ""};
        }
        return new String[]{"", ""};
    }

    /**
     * ? 分辨率
     *
     * @param context
     * @return
     */
    public static int getDevUse(Context context) {
        int devUse = 0;
        int screenWidth = 0;
        int screenHeight = 0;
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        if ((Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2)) / dm.densityDpi) >= 7)
            devUse = 1;
        else
            devUse = 0;
        return devUse;
    }

    /**
     * 调用拨号面板(暂时没用)
     *
     * @param ctx
     * @param number
     */
    public static void callNumber(Context ctx, String number) {
        PackageManager pManager = ctx.getPackageManager();
        int isHasPermissionCode = pManager.checkPermission(
                "android.permission.CALL_PHONE", ctx.getPackageName());
        if (isHasPermissionCode == PackageManager.PERMISSION_GRANTED) {

            logInfo("call number");
            Uri uri = Uri.parse("tel:" + number);
            Intent intent = new Intent("android.intent.action.CALL", uri); // 调用拨号面板
            // Intent intent = new Intent(Intent.ACTION_DIAL,uri);
            ctx.startActivity(intent);
        } else {
            logInfo("call number");
            Uri uri = Uri.parse("tel:" + number);
            // Intent intent = new Intent("android.intent.action.CALL", uri);
            // //DIAL
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);// 调用拨号面板
            ctx.startActivity(intent);
        }
    }

    /**
     * 发送短信面板(暂时没用)
     *
     * @param ctx
     * @param number
     */
    public static void sendMsg(Context ctx, String number) {
        PackageManager pManager = ctx.getPackageManager();
        int isHasPermissionCode = pManager.checkPermission(
                "android.permission.SEND_SMS", ctx.getPackageName());
        if (isHasPermissionCode == PackageManager.PERMISSION_GRANTED) {
            Uri uri = Uri.parse("smsto:" + number);
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
            it.putExtra("sms_body", "");
            ctx.startActivity(it);
        } else {
            logInfo("Need android.permission.SEND_SMS permission");
        }
    }

    /**
     * 发邮件面板(暂时没用)
     *
     * @param ctx
     * @param emailAddress
     */
    public static void sendEmail(Context ctx, String emailAddress) {
        if (null != emailAddress) {
            Uri uri = Uri.parse("mailto:" + emailAddress);
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
            ctx.startActivity(it);
        }
        // Intent it = new Intent(Intent.ACTION_SEND);
        // it.putExtra(Intent.EXTRA_EMAIL, Android123@163.com);
        // it.putExtra(Intent.EXTRA_TEXT, "The email body text");
        // it.setType("text/plain");
        // ctx.startActivity(Intent.createChooser(it, "Choose Email Client"));
        //
        // Intent it=new Intent(Intent.ACTION_SEND);
        // String[] tos={"me@abc.com"};
        // String[] ccs={"you@abc.com"};
        // it.putExtra(Intent.EXTRA_EMAIL, tos);
        // it.putExtra(Intent.EXTRA_CC, ccs);
        // it.putExtra(Intent.EXTRA_TEXT, "The email body text");
        // it.putExtra(Intent.EXTRA_SUBJECT, "The email subject text");
        // it.setType("message/rfc822");
        // ctx.startActivity(Intent.createChooser(it, "Choose Email Client"));
    }

    /**
     * 打开地图(暂时没用)
     *
     * @param ctx       ctx
     * @param latitude  纬度
     * @param longitude 经度
     */
    public static void openMap(Context ctx, String latitude, String longitude) {
        if (null != latitude && null != longitude) {
            Uri uri = Uri.parse("geo:" + latitude + "," + longitude);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            ctx.startActivity(it);
        }
    }

    /**
     * 打开音乐播放器
     *
     * @param ctx
     * @param url
     */
    public static void playVideo(Context ctx, String url) {
        if (null != url) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            it.setDataAndType(uri, "audio/mp3");
            ctx.startActivity(it);
        }
    }

    /**
     * 获取用户代理：客户端信息的HTTP头字段，他可以用来统计目标和违规协议。
     *
     * @param context
     * @return
     */
    public static String getUserAgent(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValues.SP_ADVINFO_FILE, Context.MODE_PRIVATE);
        String ua;
        try {
            ua = sharedPreferences.getString("ua", "");
            if (TextUtils.isEmpty(ua)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    ua = WebSettings.getDefaultUserAgent(context);
                else {
                    WebView webview = new WebView(context);
                    WebSettings settings = webview.getSettings();
                    ua = settings.getUserAgentString();
                }
                if (null != ua && ua.length() > 0) {
                    Editor editor = sharedPreferences.edit();
                    ua = URLEncoder.encode(ua, Charset.forName("UTF-8").name());
                    editor.putString("ua", ua);
                    editor.commit();
                } else
                    ua = "";
            }
            if (!TextUtils.isEmpty(ua) && ua.contains(" "))
                ua = URLEncoder.encode(ua, Charset.forName("UTF-8").name());
        } catch (Exception e) {
            e.printStackTrace();
            ua = "";
        }
        return ua;
    }

    /**
     * Google Play Service提供的广告id: 被条/插屏/开屏 initApplyBean调用,inmobi广告用到
     *
     * @param context
     * @return
     */
    public static String gpId = "00000000-0000-0000-0000-000000000000";
    public static boolean gpid_limited = false;

    //wilder 2019 for oaid
    public static String oaId = "00000000-0000-0000-0000-000000000000";
    public static boolean oaid_limited = false;

    public static boolean getInstallPackageInfo(final Context context , String name) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo;
            //getPackageInfo("com.huawei.hwid", 0);
            packInfo = packageManager.getPackageInfo(name, 0);
        }catch (Exception e ) {
            return false;
        }
        return true;
    }

    private static void getDevice_GPID(final Context context ) {

        if (!checkClass("com.google.android.gms.ads.identifier.AdvertisingIdClient"))
            return;

        final SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValues.SP_ADVINFO_FILE, Context.MODE_PRIVATE);
        com.google.android.gms.ads.identifier.AdvertisingIdClient.Info info;
        try {
            info = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context);
            gpid_limited = info.isLimitAdTrackingEnabled();
            if (!gpid_limited) {
                gpId = info.getId();
                AdViewUtils.logInfo("########## Google GPID ##########");
            }
        }catch (Exception e) {
            gpid_limited = true;
        }

        if (!gpid_limited) {
            AdViewUtils.logInfo("#### first ###### gpId = " + gpId + " ###########");
        }else {
            gpId = "00000000-0000-0000-0000-000000000000";
            AdViewUtils.logInfo("#### first ###### gpId is closed ###########");
        }
        Editor editor = sharedPreferences.edit();
        editor.putString("gpid", gpId);
        editor.putLong("gpid_time", System.currentTimeMillis());
        editor.putBoolean("gpid_limited",gpid_limited );
        editor.commit();
    }
    private static void getDevice_OAID(final Context context) {
        if (!useHuaWeiOAID || !checkClass("com.huawei.hms.ads.identifier.AdvertisingIdClient"))
            return;
        com.huawei.hms.ads.identifier.AdvertisingIdClient.Info info;
        final SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValues.SP_ADVINFO_FILE, Context.MODE_PRIVATE);

        try {
            info = com.huawei.hms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context);
            if (null != info) {
                oaid_limited = info.isLimitAdTrackingEnabled();
                if (!oaid_limited) {
                    oaId = info.getId();
                    AdViewUtils.logInfo("########## HuaWei OAID ##########");
                }
            } else {
                oaid_limited = true;
            }
        }catch (Exception e) {
            e.printStackTrace();
            oaid_limited = true;
        }

        if (!oaid_limited) {
            AdViewUtils.logInfo("#### first ###### oaId = " + oaId + " ###########");
        }else {
            oaId = "00000000-0000-0000-0000-000000000000";
            AdViewUtils.logInfo("#### first ###### oaId is closed ###########");
        }

        //put config
        Editor editor = sharedPreferences.edit();
        editor.putString("oaid", oaId);
        editor.putLong("oaid_time", System.currentTimeMillis());
        editor.putBoolean("oaid_limited",oaid_limited );
        editor.commit();
    }

    //第一次取得id等信息，必须在单独的线程中，之后给相关模块发送消息，之后才能发送广告请求
    public static void getDeviceIdFirstTime(final Context context, final KyAdBaseView baseView) {
        //final long lastGpidTime = sharedPreferences.getLong("gpid_time", 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //com.google.android.gms.ads.identifier.AdvertisingIdClient;
                //com.huawei.hms.ads.identifier.AdvertisingIdClient;
                try {
                    //oaid
                    getDevice_OAID(context);
                    //gpid
                    getDevice_GPID(context);
                } catch (Exception e) {
                    e.printStackTrace();
                    AdViewUtils.logInfo("#### first ###### device ID service  not available ###########");
                }
                //无论对错都不妨碍发送广告
                if (null != baseView) {
                    baseView.notifyMsg(ConstantValues.NOTIFY_REQ_GPID_FETCH_DONE, "OK");
                }
            }
        }).start();
    }

    public static String getGpId(final Context context) {
        //AdViewUtils.logInfo("=========== getGPId() ==========");
        if (null != test_UserGPID && !test_UserGPID.isEmpty()) {
            return test_UserGPID;
        }
        if (!checkClass("com.google.android.gms.ads.identifier.AdvertisingIdClient")) {
            return "00000000-0000-0000-0000-000000000000";
        }

        final SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValues.SP_ADVINFO_FILE, Context.MODE_PRIVATE);
        final long lastGpidTime = sharedPreferences.getLong("gpid_time", 0);
        if (System.currentTimeMillis() - lastGpidTime >  5 * 60 * 1000 )// 5mins
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getDevice_GPID(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AdViewUtils.logInfo("##########  google play service  not available ###########");
                    }
                }
            }).start();
        }
        //if no timeout, can use old one, the ad id user can change anytime
        if (!sharedPreferences.getBoolean("gpid_limited",false)/*gpid_enabled*/) {

            return sharedPreferences.getString("gpid", gpId);
            //return gpId;
        }
        else {
            return "00000000-0000-0000-0000-000000000000";
        }
    }

    public static String getOAId(final Context context) {
        //AdViewUtils.logInfo("=========== getOAId() ==========");
        if (!useHuaWeiOAID || !checkClass("com.huawei.hms.ads.identifier.AdvertisingIdClient"))
            return "00000000-0000-0000-0000-000000000000";

        final SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValues.SP_ADVINFO_FILE, Context.MODE_PRIVATE);
        final long lastOAIDTime = sharedPreferences.getLong("oaid_time", 0);
        if (System.currentTimeMillis() - lastOAIDTime >  5 * 60 * 1000 ) //30 sec
        {// 5mins
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getDevice_OAID(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AdViewUtils.logInfo("##########  HUAWEI OAID service  not available ###########");
                    }
                }
            }).start();
        }
        //if no timeout, can use old one, the ad id user can change anytime
        if (!sharedPreferences.getBoolean("oaid_limited",false)/*gpid_enabled*/)
            return sharedPreferences.getString("oaid", oaId);
            //return oaId;
        else
            return "00000000-0000-0000-0000-000000000000";
    }

/*
    ////////////////////////////////////// newest method  //////////////////////////////////////
    private static String sAdvertisingIdClientClassName = "com.google.android.gms.ads.identifier.AdvertisingIdClient";
    @Nullable
    static public String fetchAdvertisingInfoSync(final Context context) {
        if (context == null) {
            return null;
        }
        Object adInfo = null;
        String advertisingId = null;
        boolean isLimitAdTrackingEnabled = false;

        try {
            Reflection.MethodBuilder methodBuilder = MethodBuilderFactory.create(null, "getAdvertisingIdInfo")
                    .setStatic(Class.forName(sAdvertisingIdClientClassName))
                    .addParam(Context.class, context);

            adInfo = methodBuilder.execute();
            advertisingId = reflectedGetAdvertisingId(adInfo, advertisingId);
            isLimitAdTrackingEnabled = reflectedIsLimitAdTrackingEnabled(adInfo, isLimitAdTrackingEnabled);
        } catch (Exception e) {
            //MoPubLog.log(CUSTOM, "Unable to obtain Google AdvertisingIdClient.Info via reflection.");
            return null;
        }

        //return new AdvertisingInfo(advertisingId, isLimitAdTrackingEnabled);
        return advertisingId;
    }

    static String reflectedGetAdvertisingId(final Object adInfo, final String defaultValue) {
        try {
            return (String) MethodBuilderFactory.create(adInfo, "getId").execute();
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    static boolean reflectedIsLimitAdTrackingEnabled(final Object adInfo, final boolean defaultValue) {
        try {
            Boolean result = (Boolean) MethodBuilderFactory.create(adInfo, "isLimitAdTrackingEnabled").execute();
            return (result != null) ? result : defaultValue;
        } catch (Exception exception) {
            return defaultValue;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
*/
    /**
     * 取得 GDPR,因为该值会变化，所以每次调用该函数的时候都去取一下
     *
     * @param context
     * @return
     */
    public static HashMap<String, Object> getGDPRInfo(Context context) {
        HashMap<String, Object> gdprList = new HashMap<String, Object>();
        try {
            SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            //String consentString = mPreferences.getString("IABConsent_ConsentString", "");
            gdprList.put("IABConsent_CMPPresent", mPreferences.getBoolean("IABConsent_CMPPresent", false));
            gdprList.put("IABConsent_ConsentString", mPreferences.getString("IABConsent_ConsentString", ""));
            gdprList.put("IABConsent_SubjectToGDPR", mPreferences.getString("IABConsent_SubjectToGDPR", ""));
            gdprList.put("IABConsent_ParsedPurposeConsents", mPreferences.getString("IABConsent_ParsedPurposeConsents", ""));
            gdprList.put("IABConsent_ParsedVendorConsents", mPreferences.getString("IABConsent_ParsedVendorConsents", ""));
        }catch(Exception e) {
            e.printStackTrace();
        }
        return gdprList;
    }

//    public static String getGDPR_ConsentString(final Context context) {
//        //Context mContext = getApplicationContext();
//        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        String consentString = mPreferences.getString("IABConsent_ConsentString", "");
//        return consentString;
//    }
//    public static boolean getGDPR_CMPPresent(final Context context) {
//        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        Boolean cmpPresent = mPreferences.getBoolean("IABConsent_CMPPresent", false);
//        return cmpPresent;
//    }

    //取得US-Pirvacy-User-Signal : CCPA 的值从系统设置
    public static String getCCPA_String(final Context context) {
        //Context mContext = getApplicationContext();
        String  ccpaString = "";
        try {
            SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            ccpaString = mPreferences.getString("IABUSPrivacy_String", "");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ccpaString;
    }
    /**
     * 应用程序列表(暂未用)
     *
     * @param context
     * @return
     */
    public static HashMap<String, HashMap<String, Object>> getApplicationList(Context context) {
        HashMap<String, HashMap<String, Object>> packcageList = new HashMap<String, HashMap<String, Object>>();
        List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(
                                        PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < packageInfos.size(); i++) {
            HashMap<String, Object> itemInfo = new HashMap<String, Object>();
            itemInfo.put("lable", packageInfos.get(i).applicationInfo.loadLabel(context.getPackageManager()).toString());
            itemInfo.put("pakcage", packageInfos.get(i).applicationInfo.loadLabel(context.getPackageManager()).toString());
            itemInfo.put("icon", packageInfos.get(i).applicationInfo.loadIcon(context.getPackageManager()));
            packcageList.put(packageInfos.get(i).applicationInfo.loadLabel(context.getPackageManager()).toString(), itemInfo);
        }
        return packcageList;
    }

    /**
     * ANDROID_ID:一串64位的编码（十六进制的字符串），是随机生成的设备的第一个引导，其记录着一个固定值，通过它可以知道设备的寿命（
     * 在设备恢复出厂设置后，该值可能会改变）
     *
     * @param context
     * @return
     */
    public static String getAndroidID(Context context) {
        String androidId = "0000000000000000";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            // logInfo(androidId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidId;
    }

    /**
     * 画矩形
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static Rect string2Rect(int x, int y, int width, int height) {
        return new Rect(x, y, x + width, y + height);
    }

    /**
     * 删除 指定目录下的文件，修改时间大于1天；
     *
     * @param filePath 指定文件路径
     */
    public static void delCacheFiles(String filePath) {
        try {
            File files = new File(filePath);
            if (!files.exists())
                return;
            if(null == files.listFiles())
                return;
            for (File file : files.listFiles()) {
                if (file.isFile()) {
                    long currentTime = System.currentTimeMillis();
                    long lastModifiedTime = file.lastModified();
                    if (currentTime - lastModifiedTime > 24 * 60 * 60
                            * 1000) {
                        file.delete();
                    }
                }
                if (file.isDirectory()) {
                    delCacheFiles(file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 通过解析apk安装包，获取保内图标，包名，版本等信息，判断该apk是否完整有效（可能还不完善）
     *
     * @param context  Context
     * @param filePath apk文件路径
     * @return true表示apk文件完整有效，反之亦然
     */
    public boolean isValidAchiveFile(Context context, String filePath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(filePath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            String appName = pm.getApplicationLabel(appInfo).toString();
            String packageName = appInfo.packageName; // 得到安装包名称
            String version = info.versionName; // 得到版本信息
            if (null != appName && null != packageName && null != version)
                return true;
        }
        return false;
    }

    public static boolean isEmulator() {
        return Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK")
                || Build.MANUFACTURER.contains("Genymotion");
    }

    /**
     * @param duration in format hh:mm:ss
     * @return in seconds
     */
    public static int parseDuration(String duration) {
        String[] data = duration.split(":");
        int hours = Integer.parseInt(data[0]);
        int minutes = Integer.parseInt(data[1]);
        int seconds = Integer.parseInt(data[2]);
        return seconds + 60 * minutes + 3600 * hours;
    }

    public static int parsePercent(String duration) {
        String progress = duration.replace("%", "").trim();
        return Integer.parseInt(progress);
    }

    //wilder 2019 test function
    public static String loadAssetsFile(String path) {
        byte[] Data = null;
        ///assets/test/vpaid--fix2.xml  , vast-test1.xml
        //InputStream abpath = getActivity().getClass().getResourceAsStream(path); //
        InputStream abpath = null;
        try {
            abpath = getActivity().getResources().getAssets().open(path);
            //ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            //int ch;
            int lenght = abpath.available();
            Data = new byte[lenght];
            abpath.read(Data);
            abpath.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Data);
    }

    public static String loadAssetsFileByContext(String path, Context ctx) {
        byte[] Data = null;
        ///assets/test/vpaid--fix2.xml  , vast-test1.xml
        //InputStream abpath = getActivity().getClass().getResourceAsStream(path); //
        InputStream abpath = null;
        try {
            abpath = ctx.getResources().getAssets().open(path);
            //ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            //int ch;
            int lenght = abpath.available();
            Data = new byte[lenght];
            abpath.read(Data);
            abpath.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Data);
    }
    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(String path)
    {
        Bitmap image = null;
        Activity act = null;
        try
        {
            act = getActivity();
            if (act != null) {
                InputStream abpath = getActivity().getResources().getAssets().open(path);
                //InputStream is = am.open(fileName);
                image = BitmapFactory.decodeStream(abpath);
                abpath.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;
    }
    /*//从assets 文件夹中获取文件并读取数据
    public String getFromAssets(String fileName){
        String result = "";
        try {
            InputStream in = getResources().getAssets().open(fileName);
//获取文件的字节数
            int lenght = in.available();
//创建byte数组
            byte[] buffer = new byte[lenght];
//将文件中的数据读到byte数组中
            in.read(buffer);
            result = EncodingUtils.getString(buffer, ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }*/
     //get 1 line from txt file
     public static String getLineFromAssets(String fileName) {
         try {
             InputStreamReader inputReader = new InputStreamReader(getActivity().getResources().getAssets().open(fileName));
             BufferedReader bufReader = new BufferedReader(inputReader);
             String line = "";
             String Result = "";
             while ((line = bufReader.readLine()) != null) {
                 //Result += line;
                 return line;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "";
     }

    /*
                   xmltype = 2，混合Vast元素格式的时候根据内容自己生成Vast格式下发

    */
    public static String generalMixedVast(AdsBean adsBean) {
        VideoBean videoBean = adsBean.getVideoBean();
        String videoVast = Assets.getJsFromBase64(Assets.NATIVEVIDEOVAST);
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_DURATION_STR, vastMixedLinkPart(ConstantValues.MIXED_VAST_DURATION_TYPE, videoBean.getDuration() + ""));
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_IMPRESSION_STR, vastMixedEsEc(ConstantValues.MIXED_VAST_IMPRESSION, adsBean.getExtSRpt()));
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_STARTEVENT_STR, vastMixedLinkPart(ConstantValues.MIXED_VAST_START_TYPE, adsBean.getSpTrackers()));
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_MIDDLEEVENT_STR, vastMixedLinkPart(ConstantValues.MIXED_VAST_MIDDLE_TYPE, adsBean.getMpTrackers()));
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_ENDEVENT_STR, vastMixedLinkPart(ConstantValues.MIXED_VAST_END_TYPE, adsBean.getCpTrackers()));
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_CLICKTHROUGHT_STR, vastMixedLinkPart(ConstantValues.MIXED_VAST_CLICKTHROUGH, adsBean.getAdLink()));
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_CLICKTRACKING_STR, vastMixedEsEc(ConstantValues.MIXED_VAST_CLICKTRACKING, adsBean.getExtCRpt()));
        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_MEDIAFILE_STR, vastMixedLinkPart(ConstantValues.MIXED_VAST_MEDIA_FILE, videoBean.getVideoUrl(), videoBean.getHeight() + "", videoBean.getWidth() + ""));

        String[] extensionLinks = new String[7];
        extensionLinks[0] = videoBean.getEndHtml();
        extensionLinks[1] = videoBean.getEndImgUrl();
        extensionLinks[2] = videoBean.getEndIconUrl();
        extensionLinks[3] = videoBean.getEndDesc();
        extensionLinks[4] = videoBean.getEndTitle();
        extensionLinks[5] = videoBean.getEndButtonText();
        extensionLinks[6] = videoBean.getEndButtonUrl();

        videoVast = videoVast.replace(ConstantValues.MIXED_VAST_EXTENSION_STR, vastMixedLinkPart(ConstantValues.MIXED_VAST_EXTENSION, extensionLinks));

        return videoVast;
    }

    public static String vastMixedEsEc(int type, HashMap<String, String[]> maps) {
        String temp = "";
        Iterator iterator = maps.keySet().iterator();
        while (iterator.hasNext()) {
            String[] links = maps.get(iterator.next());
            temp = temp + vastMixedLinkPart(type, links);
        }
        return temp;
    }

    public static String vastMixedLinkPart(int type, String... links) {
        String temp = "";
        for (String link : links) {
            switch (type) {
                case ConstantValues.MIXED_VAST_START_TYPE:
                    temp = temp + "<Tracking event=\"start\">\n" +
                            "   <![CDATA[" + link + "]]>" +
                            "</Tracking>";
                    break;
                case ConstantValues.MIXED_VAST_MIDDLE_TYPE:
                    temp = temp + "<Tracking event=\"midpoint\">\n" +
                            "   <![CDATA[" + link + "]]>" +
                            "</Tracking>";
                    break;
                case ConstantValues.MIXED_VAST_END_TYPE:
                    temp = temp + "<Tracking event=\"complete\">\n" +
                            "   <![CDATA[" + link + "]]>" +
                            "</Tracking>";
                    break;
                case ConstantValues.MIXED_VAST_IMPRESSION:
                    temp = temp + "<Impression>\n" +
                            "   <![CDATA[" + link + "]]>\n" +
                            "</Impression>";
                    break;
                case ConstantValues.MIXED_VAST_CLICKTHROUGH:
                    temp = temp + "<ClickThrough>\n" +
                            "   <![CDATA[" + link + "]]>\n" +
                            " </ClickThrough>";
                    break;
                case ConstantValues.MIXED_VAST_CLICKTRACKING:
                    temp = temp + "<ClickTracking>\n" +
                            "   <![CDATA[" + link + "]]>\n" +
                            "</ClickTracking>";
                    break;
            }
        }

        switch (type) {
            case ConstantValues.MIXED_VAST_DURATION_TYPE:
                int duration = Integer.valueOf(links[0]);
                int seconds = duration % 60;
                int minutes = duration / 60;
                int hours = minutes / 24;
                minutes = minutes % 60;
                temp = "<Duration>" + hours + ":" + minutes + ":" + seconds + "</Duration>";
                break;
            case ConstantValues.MIXED_VAST_MEDIA_FILE:
                temp = "<MediaFile type=\"video/mp4\" width=\"" + links[2] + "\" height=\"" + links[1] + "\">\n" +
                        "        <![CDATA[" + links[0] + "]]>\n" +
                        " </MediaFile>";
                break;
            case ConstantValues.MIXED_VAST_EXTENSION:
                temp = (TextUtils.isEmpty(links[0]) ? "" : ("<Ky_EndHtml><![CDATA[" + links[0] + "]]></Ky_EndHtml>\n")) +
                        (TextUtils.isEmpty(links[1]) ? "" : "<Ky_EndImage><![CDATA[" + links[1] + "]]></Ky_EndImage>\n") +
                        (TextUtils.isEmpty(links[2]) ? "" : "<Ky_EndIconUrl><![CDATA[" + links[2] + "]]></Ky_EndIconUrl>\n") +
                        (TextUtils.isEmpty(links[3]) ? "" : "<Ky_EndDesc><![CDATA[" + links[3] + "]]></Ky_EndDesc>\n") +
                        (TextUtils.isEmpty(links[4]) ? "" : "<Ky_EndTitle><![CDATA[" + links[4] + "]]></Ky_EndTitle>\n") +
                        (TextUtils.isEmpty(links[5]) ? "" : "<Ky_EndText><![CDATA[" + links[5] + "]]></Ky_EndText>\n") +
                        (TextUtils.isEmpty(links[6]) ? "" : "<Ky_EndLink><![CDATA[" + links[6] + "]]></Ky_EndLink>");
                break;
        }
        return temp;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////load url interfaces ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Mraid + OMSDK support template
    public static WebView loadWebContentExt(WebView webView, String script) {
        String otherHtml = ConstantValues.MRAID_SCRIPT_HTMLSTYLE;
        //otherHtml = otherHtml + ConstantValues.h5_style; //wilder 2020
        try {
            if (null == webView )
                return null;

            otherHtml = otherHtml.replace("__SCRIPT__", script);

            webView.loadDataWithBaseURL(
                                    //ConstantValues.WEBVIEW_BASEURL
                                    ConstantValues.VAST_OMSDK_ASSETS_URL,
                                    otherHtml, "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return webView;
    }

    //vast video holder
    public static WebView loadWebVideoURL(WebView webView, String html) {
        try {
            if (null == webView )
                return null;

//            webView.loadData(html,"text/html","UTF-8");
            webView.loadDataWithBaseURL(
                    //(url.startsWith("http")||url.startsWith("https")) ? "" :"file://" + url.substring(0,url.lastIndexOf("/") + 1),
                    //ConstantValues.WEBVIEW_BASEURL
                    ConstantValues.VAST_OMSDK_ASSETS_URL,
                    html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return webView;
    }

    //use by load url with picture or other resource
    public static WebView loadWebImageURL(WebView webView, String bitmapURL, String adLink) {
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
    public static WebView loadWebImageLocal(WebView webView, String bitmapPath, String adLink, int adWidth, int adHeight) {
        String imageHtml = new String(ConstantValues.MRAID_BITMAP_HTMLSTYLE);
        try {
            if (null == webView || null == bitmapPath)
                return null;
//            if (adHeight > 0 && adWidth > 0) {
//                adWidth = ((int) (adWidth / (float) density)) + 1;
//                adHeight = ((int) (adHeight / (float) density)) + 1;
//            }
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

    public static void loadVPAIDURL(WebView wv, String vpaidURL) {
        //if (!vpaidURL.startsWith("https"))
        {
            String vpURL = vpaidURL;
            //mWebView = wv;
            if(vpURL.startsWith("//")){
                vpURL = "http:" + vpURL;
            }
            AdViewUtils.logInfo("++++++++++++++ loadVPAIDURL(): vpaid mode +++++++++++++");
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        //String html = ConstantValues.VPAID_HTML;
                        String html = AdViewUtils.loadAssetsFile("VAST_VPAID_JS.html");
                        String finalHtml = html.replace(ConstantValues.VPAID_CREATIVE_URL_STRING, vpaidURL);
                        //finalHtml = finalHtml.replace(ConstantValues.VPAID_BRIDGE_JS_STRING,js_bridge);
                        //AdViewUtils.logInfo("++++ html: " + finalHtml);
                        //(wilder 2019) for google IMA3.0, if not set baseURL , it will cause "Uncaught SecurityError"
                        wv.loadDataWithBaseURL( ConstantValues.VAST_OMSDK_ASSETS_URL, //ConstantValues.WEBVIEW_BASEURL,
                                finalHtml,
                                "text/html",
                                "UTF-8",//StandardCharsets.UTF_8.toString(),
                                null);
                    } catch (Exception e) {
                        AdViewUtils.logInfo("+++ Can't read assets: " + e.getMessage() + "++++");
                    }
                } else {
                    wv.loadUrl(vpURL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    ///////////////////////////////////////////////////////////////////////////////
    /////////////////////////  for OMSDK v1.2  ////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////
    public static void checkOMSDKFeatrue() {

        if (!useOMSDK) {
            //彻底关闭omsdk功能
            useOMSDKFeature = false;
        }else if (KyAdBaseView.checkClass("com.iab.omid.library.adview.adsession.AdSession")) {
            useOMSDKFeature = true;
        }
    }

    public static boolean canUseOMSDK() {
        if (Omid.isCompatibleWithOmidApiVersion(Omid.getVersion())) {
            useOMSDKFeature = true;
        }else
            useOMSDKFeature = false;

        return useOMSDKFeature;

    }
    //wilder 2019 for sdk -> BID
    public static String getOMPartnerName() {
        if (canUseOMSDK())
            return OMSDK_PARTNER_NAME;
        else
            return "";
    }

    public static String getOMPartnerVer() {
        if (canUseOMSDK())
            return OMSDK_PARTNER_SDK_APP_VER;
        else
            return "";
    }

    public static void createOMPartner() {
        if (!canUseOMSDK())
            return;
        try {

            OMSDKPartner = Partner.createPartner( OMSDK_PARTNER_NAME, OMSDK_PARTNER_SDK_APP_VER );
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static Partner getOMPartner() {
         return OMSDKPartner;
    }

    public static boolean addOMVerificationScriptResource(String vendorKey, String scriptUrl, String params) {
        if (!canUseOMSDK())
            return false;
        verificationScriptResources = new ArrayList<VerificationScriptResource>();
        //new OMIDVerificationScriptResource(vendorKey, url,params);
        try {
            URL url = new URL(scriptUrl);
            VerificationScriptResource veriScriptResource =
                    VerificationScriptResource.createVerificationScriptResourceWithParameters(vendorKey,url, params);
            //VerificationScriptResource.createVerificationScriptResourceWithoutParameters(vendorKey, url, params);
            verificationScriptResources.add(veriScriptResource);
        } catch (IllegalArgumentException e ) {
            e.printStackTrace();
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static AdSession startOMAdSessionNATIVE(View v) {
        if (!canUseOMSDK())
            return null;
        AdSession adSession = null;
        try {
            String customReferenceData = "";
            if (verificationScriptResources.size() == 0)
                return null;

            String OMID_JS_SERVICE_CONTENT = loadAssetsFile("omsdk-v1.js");
            AdSessionContext adSessionContext = AdSessionContext.createNativeAdSessionContext(
                    AdViewUtils.getOMPartner(),
                    OMID_JS_SERVICE_CONTENT,
                    verificationScriptResources,
                    customReferenceData);

            //(2)Designate event layer
            Owner owner = Owner.NATIVE;
            AdSessionConfiguration adSessionConfiguration = null;
            adSessionConfiguration = AdSessionConfiguration.createAdSessionConfiguration(owner, null, false);
            //(3)Create the session.
            adSession = AdSession.createAdSession(adSessionConfiguration, adSessionContext);
            //register view
            adSession.registerAdView(v);
            // Start the session
            adSession.start();
            AdViewUtils.logInfo("++++++++++++++ [OMSDK-Native] startOMAdSessionNATIVE() : successfull ++++++++++++++++");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return adSession;
    }

    public static AdSession createOMAdSessionNATIVEVideo(View v) {
        if (!canUseOMSDK())
            return null;
        AdSession adSession = null;
        try {
            String customReferenceData = "";
            String OMID_JS_SERVICE_CONTENT = loadAssetsFile("omsdk-v1.js");
            AdSessionContext adSessionContext = AdSessionContext.createNativeAdSessionContext(
                    AdViewUtils.getOMPartner(),
                    OMID_JS_SERVICE_CONTENT,
                    verificationScriptResources,
                    customReferenceData);

            //(2)Designate event layer
            Owner owner = Owner.NATIVE;
            AdSessionConfiguration adSessionConfiguration = null;
            //native video
            adSessionConfiguration = AdSessionConfiguration.createAdSessionConfiguration(owner, owner, false);
            //(3)Create the session.
            adSession = AdSession.createAdSession(adSessionConfiguration, adSessionContext);
            //register view
            adSession.registerAdView(v);
            //create event publisher
            adNativeEvents = AdEvents.createAdEvents(adSession); //该事件没有用上
            videoNativeEvents = VideoEvents.createVideoEvents(adSession);
            // Start the session
            //adSession.start();
            AdViewUtils.logInfo("++++++++++++++ [OMSDK-Native] createOMAdSessionNATIVEVideo-VIDEO() : successfull ++++++++++++++++");

        } catch (IllegalArgumentException e ) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        return adSession;
    }
    public static AdSession createOMAdSessionHTML(WebView wv) {
        if (!canUseOMSDK())
            return null;
        AdSession adSession = null;
        try {
            //(1)Create context
            String customReferenceData = "";
            AdSessionContext context = AdSessionContext.createHtmlAdSessionContext(
                    AdViewUtils.getOMPartner(),
                    wv,
                    customReferenceData);
            //(2)Designate event layer
            Owner owner = Owner.NATIVE;
            AdSessionConfiguration adSessionConfiguration =
                    AdSessionConfiguration.createAdSessionConfiguration(owner, null, false);
            //(3)Create the session.
            adSession = AdSession.createAdSession(adSessionConfiguration, context);
            adSession.registerAdView(wv);
            // Start the session, here or in app side ?
            //adSession.start();
            AdViewUtils.logInfo("++++++++++++++ [OMSDK-HTML] createOMAdSessionHTML() : successfull ++++++++++++++++");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return adSession;
    }

    public static AdSession startOMAdSessionJS(WebView wv) {
        if (!canUseOMSDK())
            return null;
        AdSession adSession = null;
        try {
            //(1)Create context
            String customReferenceData = "";
            AdSessionContext context = AdSessionContext.createHtmlAdSessionContext(
                    AdViewUtils.getOMPartner(),
                    wv,
                    customReferenceData);
            //(2)Designate event layer
            Owner owner = Owner.JAVASCRIPT;
            /*
            you should decide on the appropriate value for the isolateVerificationScripts parameter.
            The effect of a true value is that measurement resources will be placed in a sandboxed iframe where they cannot access the video ad element.
            If you specify false, they will be placed into a same-origin iframe.
             */
            AdSessionConfiguration adSessionConfiguration =
                        AdSessionConfiguration.createAdSessionConfiguration(owner, owner, false /*isolateVerificationScripts*/);

            //(3)Create the session.
            adSession = AdSession.createAdSession(adSessionConfiguration, context);
            adSession.registerAdView(wv);
            // Start the session
            adSession.start();

            AdViewUtils.logInfo("++++++++++++++ [SDK] startOMAdSessionJS() : successfull ++++++++++++++++");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return adSession;
    }

    public static void startAdSession(AdSession adSession) {
        if (!canUseOMSDK())
            return ;
//        if (Omid.isCompatibleWithOmidApiVersion(Omid.getVersion())) {
//            useOMSDKFeature = false;
//            return;
//        }
        //for html / native video , may have friendly observers
        try {
            if (null != adSession) {
                adSession.start();
                AdViewUtils.logInfo("++++++++++++++ [SDK] startAdSession() : successfull ++++++++++++++++");
            }
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void AddOMObstructions(View v, AdSession adSession) {
        if (!canUseOMSDK())
            return ;
        if (adSession == null || v == null)
            return;
        try {
            AdViewUtils.logInfo("++++++++++++++[SDK]  AddOMObstructions():" + v.getId() +  "++++++++++++++");
            adSession.addFriendlyObstruction(v);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    //if call this function multi times, it will cause error, so it must be triggered at last
    public static void  signalImpressionEvent(AdSession adSession) {
        if (!canUseOMSDK())
            return ;
        try {
            if (null != adSession /*&& !isImpressionTrigged*/) {
                AdViewUtils.logInfo("++++++++++++++[SDK]  signalImpressionEvent ()  ++++++++++++++");
                AdEvents adEvents = AdEvents.createAdEvents(adSession);
                adEvents.impressionOccurred();
                //isImpressionTrigged = true;
            }
        } catch (IllegalArgumentException e ) {
            e.printStackTrace();
        } catch ( IllegalStateException e ) {
            e.printStackTrace();
        }
    }

    public static void signalErrorEvent (AdSession adSession, ErrorType type, String info) {
        if (!canUseOMSDK())
            return ;
        if (null != adSession) {
            AdViewUtils.logInfo("++++++++++++++[SDK]  signalErrorEvent ():" + info + "++++++++++++++");
            adSession.error(type,info);
        }
    }

    public static void stopOMAdSession(AdSession adSession) {
        if (!canUseOMSDK())
            return ;
        if (null != adSession) {
            AdViewUtils.logInfo("++++++++++++++[SDK]  stopOMAdSession ()  ++++++++++++++");

            adSession.finish();
            adSession = null;
        }
    }
    //native video used
    public static void signalOMNativeVideoEvent(int status) {
        if (!canUseOMSDK())
            return ;
        try {
            if (null == videoNativeEvents)
                return;
            switch(status) {
                case NativeVideoStatus.START:
                    videoNativeEvents.start(0.0f, 1.0f);
                    break;
                case NativeVideoStatus.FIRSTQUARTILE:
                    videoNativeEvents.firstQuartile();
                    break;
                case NativeVideoStatus.MEDIUM:
                    videoNativeEvents.midpoint();
                    break;
                case NativeVideoStatus.END:
                    videoNativeEvents.complete();
                    break;
                case NativeVideoStatus.STOP:
                    videoNativeEvents.complete();
                    break;
                case NativeVideoStatus.RESUME:
                    videoNativeEvents.resume();
                    break;
                case NativeVideoStatus.THIRDQUARTILE:
                    videoNativeEvents.thirdQuartile();
                    break;
                case NativeVideoStatus.PAUSE:
                    videoNativeEvents.pause();
                    break;
                case NativeVideoStatus.SKIPPED:
                    videoNativeEvents.skipped();
                    break;
                case NativeVideoStatus.VOLUMECHANGE:
                    videoNativeEvents.volumeChange(0.5f);
                    break;

            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
    public static void signalNativeVideoLoad(float skipOffset, boolean isAutoPlay) {
        if (!canUseOMSDK())
            return ;
        //Position pos = Position.STANDALONE.toString();
        final VastProperties vProps = VastProperties.createVastPropertiesForSkippableVideo(skipOffset, isAutoPlay, Position.STANDALONE);
        try {
            videoNativeEvents.loaded(vProps);
        } catch (Exception e) {
        }
    }
    ///////////////////////// end wilder for OMSDK v1.2 //////////////////////////////////
    //////////////////
    /**
     * *************** Android output logs ****************************
     */
    public static void logWarn(String info, Throwable r) {
        if (logMode)
            Log.w(ADVIEW, info + "", r);
    }

    public static void logDebug(String info) {
        if (logMode)
            Log.d(ADVIEW, info + "");
    }

    public static void logError(String info, Throwable r) {
        if (logMode)
            Log.e(ADVIEW, info + "", r);
    }

    public static void logInfo(String info) {
        if (logMode)
            Log.i(ADVIEW, info + "");
    }
    //判断是否为模拟器
    public static boolean isSimulator(Context context)
    {
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        // 是否可以处理跳转到拨号的 Intent
        boolean canCallPhone = intent.resolveActivity(context.getPackageManager()) != null;
        return !canCallPhone;
//        return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.toLowerCase()
//                .contains("vbox") || Build.FINGERPRINT.toLowerCase()
//                .contains("test-keys") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL
//                .contains("MuMu") || Build.MODEL.contains("virtual") || Build.SERIAL.equalsIgnoreCase("android") || Build.MANUFACTURER
//                .contains("Genymotion") || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) || "google_sdk"
//                .equals(Build.PRODUCT) ||
//                ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName()
//                .toLowerCase()
//                .equals("android") || !canCallPhone;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
