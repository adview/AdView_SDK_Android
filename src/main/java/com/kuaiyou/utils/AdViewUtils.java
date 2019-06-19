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
import android.os.StrictMode;
import android.provider.Settings;
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
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.kuaiyou.interfaces.DownloadConfirmInterface;
import com.kuaiyou.obj.ApplyAdBean;

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
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;


public class AdViewUtils {
    public static boolean isTest = false;// false
    public static boolean cacheMode = false;
    public static boolean logMode = true;

    public static ScheduledExecutorService repScheduler;

    //wilder 2019 , play all video online
    public static boolean playOnLine = true;
    public static boolean bitmapOnLine = true; //wilder 2019 for load bitmap online
    public static boolean VideoAutoPlay = true;
    public static boolean htmlUseBlankErrorPage = true; //(wilder 2019) use self blank page for webview load xs error
    public static String htmlErrorPage = "http://www.adview.com/videoadtest";
    //定制化需求
    public static boolean useIMEI = false;
    //正式线
    public static String adbidAddr = "https://bid.adview.com/agent/getAd";
    //测试线 test server
    //public static String adbidAddr = "http://gbjtest.adview.com/agent/getAd";

    public static String adfillAddr = "https://adfill.adview.cn/agent/getAd";
    public static String adfillAgent1 = "https://adfill.adview.cn/agent/click";
    public static String adfillAgent2 = "https://adfill.adview.cn/agent/display";

    public static String adrtbAddr = "https://rtb.adview.cn/agent/getAd";
    public static String adrtbAgent1 = "https://rtb.adview.cn/agent/click";
    public static String adrtbAgent2 = "https://rtb.adview.cn/agent/display";



    public static String adbidClickLink = "https://bid.adview.com/agent/click";
    public static String adbidDisplayLink = "https://bid.adview.com/agent/display";
    public static String adbidErrorLink = "https://bid.adview.com/agent/reportError.php";


    public static String adfillAddr_test = "http://test2018.adview.cn/agent/getAd";


    public static String check_url = "https://bid.adview.com/agent/front/checkUpdate";//"http://test2018.adview.cn/agent/front/checkUpdate";
    public static String check_url_test = "http://test2018.adview.com/agent/front/checkUpdate";
    //  public final static String load_report_url = "https://bid.adview.cn/agent/front/dexLoadReport";
    //核心版本号
    public static int VERSION = 400;
    public static String ADVIEW = "AdView SDK v4.0.0";

    public final static String channel = "";

    public final static String BID_ANDROID_VIDEO = "rfkghh59eyryzx7wntlgry0mff0yx7z1";// "rfkghh59eyryzx7wntlgry0mff0yx7z1";//dur75pt6jlyim2910rroamiyv54qszuk

    public static String userAgent = "";

    private static Location location = null;

    public static boolean useSelfIcon = true;

    // private static int adWidth;
    // private static int adHeight;

    static {
        if (isTest) {
            adfillAddr = adfillAddr_test;
            adbidAddr = adfillAddr_test;
            check_url = check_url_test;
            adbidErrorLink = "http://test2018.adview.cn/agent/reportError.php";
        }
        try {
            if (Build.VERSION.SDK_INT > 23) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                builder.detectFileUriExposure();
                AdViewUtils.logInfo("detectFileUriExposure");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpURLConnection.setFollowRedirects(true);
    }

    public AdViewUtils() {
        super();
    }

//    public final static int ADFILL_TYPE = 997;
//    public final static int ADBID_TYPE = 998;
//    public final static int ADRTB_TYPE = 996;
//
//    public final static int ADFILL_ROUTE = 0; // 补余
//    public final static int SSP_ROUTE = 1; // 聚合
//    public final static int ADRTB_ROUTE = 2;// 竞价

    public static void setServerUrl(boolean isRtb) {
        if (!isTest) {
            if (isRtb) {
                adfillAddr = adrtbAddr;
                adbidAddr = adrtbAddr;
            }
        } else {
            adfillAddr = adfillAddr_test;
            adbidAddr = adfillAddr_test;
            adrtbAddr = adfillAddr_test;
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
     * 获取导航栏高度
     *
     * @param context
     * @return
     */
    public static int getDaoHangHeight(Context context) {
        try {
            if (!checkDeviceHasNavigationBar(context))
                return 0;
            int result = 0;
            int resourceId = 0;
            int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
            if (rid != 0) {
                resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                return context.getResources().getDimensionPixelSize(resourceId);
            } else
                return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

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

    public static boolean checkDeviceHasNavigationBar(Context context) {
        try {
            if (navigationBarExist(context)) {
                String uiStr = Integer.toBinaryString(((Activity) context).getWindow().getDecorView().getSystemUiVisibility());
                if (uiStr.length() >= 2) {
                    byte[] a = uiStr.getBytes();
                    if (a[a.length - 2] == 49)
                        return false;
                } else return true;
            } else
                return false;
        } catch (Exception e) {
        }
        return true;
    }

    public static boolean navigationBarExist(Context activity) {
        try {
            //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
            boolean hasMenuKey = ViewConfiguration.get(activity)
                    .hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap
                    .deviceHasKey(KeyEvent.KEYCODE_BACK);

            if (!hasMenuKey && !hasBackKey) {
                // 这个设备有一个导航栏
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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
            DisplayMetrics displayMetrics = context.getApplicationContext().getResources()
                    .getDisplayMetrics();
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
            e.printStackTrace();
            return new int[]{0, 0};
        }
    }


    @SuppressLint("NewApi")
    public static int[] getWidthAndHeight(Context context, boolean isArrange) {
        try {
            if (null == context)
                return new int[]{0, 0};
            WindowManager WM = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
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
            if (TextUtils.isEmpty(url) || url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("javacript"))
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
                AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
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
            if (!status_ok)
                AdViewUtils.logInfo(sb.toString());

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
        return getResponse(link, content, ConstantValues.REQUESTTIMEOUT);
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
                conn.setConnectTimeout(ConstantValues.REQUESTTIMEOUT);
                conn.setReadTimeout(ConstantValues.REQUESTTIMEOUT);
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
        BufferedReader br;
        InputStream sbs = null;
        String temp;
//        ByteArrayBuffer bt = new ByteArrayBuffer(4096);
        try {
            if (null != connection.getHeaderField("Content-Encoding")
                    && connection.getHeaderField("Content-Encoding").equals(
                    "gzip")) {
                GZIPInputStream gis = new GZIPInputStream(
                        new BufferedInputStream(connection.getInputStream()));
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
                    && connection.getHeaderField("Content-Encoding").equals(
                    "deflate")) {
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
        BufferedReader bufferedReader2 = new BufferedReader(
                new InputStreamReader(inStream, "UTF-8"));
        for (String s = bufferedReader2.readLine(); s != null; s = bufferedReader2
                .readLine()) {
            builder.append(s);
        }
        return builder.toString();
    }


    /**
     * 获得图片路径的输入流 || 路径
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
            SharedPreferences imagemodifysince = context.getSharedPreferences(ConstantValues.SP_LASTMODIFY, Context.MODE_PRIVATE);
            SharedPreferences lastVisitTime = context.getSharedPreferences(ConstantValues.SP_LASTVISIT, Context.MODE_PRIVATE);

            String filename = url.hashCode() + "";
            //String a_path = context.getFilesDir() + "/Adview/";

            updateDir = new File(ConstantValues.CACHE_AD_PATH);
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
                conn.setConnectTimeout(ConstantValues.REQUESTTIMEOUT);
                conn.setReadTimeout(ConstantValues.REQUESTTIMEOUT);
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
            conn.setConnectTimeout(ConstantValues.REQUESTTIMEOUT);
            conn.setReadTimeout(ConstantValues.REQUESTTIMEOUT);
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
     * 手机SIM卡的国家代号、网络代号
     *
     * @param context
     * @return
     */
    public static String getServicesPro(Context context) {
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
     * @param applyAdBean applyAdBean
     * @return token token
     */
    public static String makeBIDMd5Token(ApplyAdBean applyAdBean) {
        return MD5Utils.MD5Encode(applyAdBean.getBundleId()
                + applyAdBean.getAppId() + applyAdBean.getAdSize()
                + applyAdBean.getUuid() + applyAdBean.getTime()
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
     * 获取基站信息
     *
     * @throws Exception
     */
    public static int[] getCellInfo(Context context) {
        int[] loc = new int[2];
        /** 调用API获取基站信息 */
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
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
        }
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
        try {
            WifiManager mWifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);

            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            if (mWifiInfo != null) {
                ssid = mWifiInfo.getSSID();
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return ssid;
    }

    // 得到接入点的BSSID
    public static String getBSSID(Context context) {
        try {
            WifiManager mWifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);

            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? "" : mWifiInfo.getBSSID();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "";
    }

    public static String getMacAddress(Context context) {
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
        } // for now eat exceptions
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
     * 信号类型
     *
     * @param context
     * @return 0：UNKNOW、 1：WIFI、 2：2_G、 3： 3_G、 4：4_G、
     */
    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            String netName = ni.getSubtypeName();
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (manager.getNetworkType()) {
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
                }
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
                    if (url.toLowerCase().endsWith(".apk")) {
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
        if (type == ConstantValues.SPREADTYPE) {
            preferences = context.getSharedPreferences(ConstantValues.SP_SPREADINFO, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.INSTLTYPE) {
            preferences = context.getSharedPreferences(ConstantValues.SP_INSTLINFO, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.BANNERTYPE) {
            preferences = context.getSharedPreferences(ConstantValues.SP_BANNERINFO, Context.MODE_PRIVATE);
        } else if (type == ConstantValues.MRECTYPE) {
            preferences = context.getSharedPreferences(ConstantValues.SP_BANNERINFO, Context.MODE_PRIVATE);
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

    public static boolean checkClickPermission(Context context, String premission, int getType) {
        if (!AdViewUtils.isConnectInternet(context)) {
            AdViewUtils.logInfo("Please check network");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            if (getType == PackageManager.GET_SERVICES)
                packageManager.getServiceInfo(new ComponentName(context.getPackageName(), premission), getType);
            else if (getType == PackageManager.GET_ACTIVITIES)
                packageManager.getActivityInfo(new ComponentName(context.getPackageName(), premission), getType);
            else
                AdViewUtils.logInfo("PackageManager GETTYPE ERROR");
        } catch (NameNotFoundException e) {
            Toast.makeText(context,
                    "请在AndroidManifest中添加" + premission + "声明",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
//        try {
//            packageManager.getServiceInfo(new ComponentName(
//                            context.getPackageName(), "com.kuaiyou.utils.DownloadService"),
//                    PackageManager.GET_SERVICES);
//        } catch (NameNotFoundException e) {
//            Toast.makeText(context,
//                    "请在AndroidManifest中添加com.kyview.DownloadService声明",
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
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(ConstantValues.SP_ADVINFO,
                        Context.MODE_PRIVATE);
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
    public static String getGpId(final Context context) {
        //AdViewUtils.logInfo("=========== getGPId() ==========");
        try {
            if (null == Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient"))
                return "";
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            return "";
        }
        final SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantValues.SP_ADVINFO,
                                        Context.MODE_PRIVATE);
        final long lastGpidTime = sharedPreferences.getLong("gpid_time", 0);
        if (System.currentTimeMillis() - lastGpidTime > 5 * 1000) {// 5s
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AdvertisingIdClient.Info info;
                    String gpId = "";
                    try {
                        info = AdvertisingIdClient.getAdvertisingIdInfo(context);
                        gpId = info.getId();
                        Editor editor = sharedPreferences.edit();
                        editor.putString("gpid", gpId);
                        editor.putLong("gpid_time", System.currentTimeMillis());
                        AdViewUtils.logInfo("########## gpId = " + gpId +" ###########");
                        editor.commit();
                    } catch (Exception e) {
                        AdViewUtils.logInfo("########## Google Play not available ###########");
                    }
                }
            }).start();
        }
        return sharedPreferences.getString("gpid", "");
    }

    /**
     * 应用程序列表(暂未用)
     *
     * @param context
     * @return
     */
    public static HashMap<String, HashMap<String, Object>> getApplicationList(
            Context context) {
        HashMap<String, HashMap<String, Object>> packcageList = new HashMap<String, HashMap<String, Object>>();
        List<PackageInfo> packageInfos = context.getPackageManager()
                .getInstalledPackages(
                        PackageManager.GET_UNINSTALLED_PACKAGES
                                | PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < packageInfos.size(); i++) {
            HashMap<String, Object> itemInfo = new HashMap<String, Object>();
            itemInfo.put("lable", packageInfos.get(i).applicationInfo
                    .loadLabel(context.getPackageManager()).toString());
            itemInfo.put("pakcage", packageInfos.get(i).applicationInfo
                    .loadLabel(context.getPackageManager()).toString());
            itemInfo.put("icon", packageInfos.get(i).applicationInfo
                    .loadIcon(context.getPackageManager()));
            packcageList.put(
                    packageInfos.get(i).applicationInfo.loadLabel(
                            context.getPackageManager()).toString(), itemInfo);
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



}
