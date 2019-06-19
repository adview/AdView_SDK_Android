package com.kuaiyou.loader;

import android.content.Context;
import android.view.View;

import com.kuaiyou.loader.loaderInterface.AdViewBannerListener;
import com.kuaiyou.utils.ConstantValues;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 2017/3/13.
 */
public class AdViewBannerManager extends InitSDKManager {

    // banner大小，based on ConstantValues
    public static int BANNER_AUTO_FILL = 0;
    public static int BANNER_MREC = 1;     //300x250
    public static int BANNER_480X75 = 2;
    public static int BANNER_728X90 = 3;

    public static int BANNER_SMART = 5;

    private Object object;

    private final static String SETHTMLSUPPORT_METHOD_NAME = "setHtmlSupport";
    private final static String REFRESHTIME_METHOD_NAME = "setReFreshTime";
    private final static String SETSHOWCLOSEBTN_METHOD_NAME = "setShowCloseBtn";
    private final static String SETOPENANIM_METHOD_NAME = "setOpenAnim";
    private final static String SETONADVIEWLISTENER_METHOD_NAME = "setOnAdViewListener";
    private final static String STOPREQUEST_METHOD_NAME = "stopRequest";
    private final static String SETVIDEOINFO_METHOD_NAME = "setVideoInfo"; //wilder 2019 for mrec
    private final static String PLAYVIDEO_METHOD_NAME = "playVideo"; //wilder 2019 for mrec

    public AdViewBannerManager(Context context, String key, String vPosID, int routeType, int adSize, boolean canClosed, String gdpr) {
        getInstance().init(context, key);
        Class[] params = new Class[6];
        params[0] = Context.class;
        params[1] = String.class;
        params[2] = int.class;
        params[3] = int.class;
        params[4] = String.class;
        params[5] = String.class;
        Object[] objects = new Object[6];
        objects[0] = context;
        objects[1] = key;
        objects[2] = routeType;
        objects[3] = adSize;
        objects[4] = vPosID;
        objects[5] = gdpr;
        object = requestAd(BANNER_CLASS_NAME, params, objects);

        if (null != object) {
            setShowCloseBtn(canClosed);
        }

    }

    public AdViewBannerManager(Context context, String key, String vPosID, int adSize, boolean canClosed, String gdpr) {
        //ConstantValues.ADBID_TYPE
        this(context, key, vPosID, 998, adSize, canClosed, gdpr);
    }

    //wilder 2019 for MRec
    public void playVideo(Context context) {
        invoke(object, PLAYVIDEO_METHOD_NAME, new Class[]{Context.class}, new Object[]{context});
    }

    public void setVideoInfo(String posID) {
        invoke(object, SETVIDEOINFO_METHOD_NAME, new Class[]{String.class}, new Object[]{posID});
    }

    public void setHtmlSupport(int htmlSupport) {
        invoke(object, SETHTMLSUPPORT_METHOD_NAME, new Class[]{int.class}, new Object[]{htmlSupport});
    }

    public void setRefreshTime(int sec) {
        invoke(object, REFRESHTIME_METHOD_NAME, new Class[]{int.class}, new Object[]{sec});
    }

    public void setShowCloseBtn(boolean closeAble) {
        invoke(object, SETSHOWCLOSEBTN_METHOD_NAME, new Class[]{boolean.class}, new Object[]{closeAble});
    }

    public void setOpenAnim(boolean openAnim) {
        invoke(object, SETOPENANIM_METHOD_NAME, new Class[]{boolean.class}, new Object[]{openAnim});
    }

    public void setBannerStopRequest() {
        invoke(object, STOPREQUEST_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public void setOnAdViewListener(AdViewBannerListener bannerListener) {
        try {
            Class reflectListener = Class.forName(INTERFACE_NAME);
            Object listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{reflectListener},
                    new BannerInvocationImp(bannerListener));

            invoke(object, SETONADVIEWLISTENER_METHOD_NAME, new Class[]{reflectListener}, new Object[]{listener});
            /*
            final JIEKO jieKou = new ShiXian();
            Object jieKouProxy = Proxy.newProxyInstance(
                    jieKou.getClass().getClassLoader(),
                    jieKou.getClass().getInterfaces(),
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            System.out.println("我开始说了");
                            return method.invoke(jieKou, args);
                        }
                    });
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     /* (wilder 2019) just for test
    private interface JIEKO {
        int sellFish();
    }
    public class ShiXian implements JIEKO {
        public int sellFish() {
            System.out.println("my fish is delicious!!");
            return 10;
        }
    }
    */

    public View getAdViewLayout() {
        if (null != object)
            try {
                return (View) object;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }


    private class BannerInvocationImp implements InvocationHandler {

        private AdViewBannerListener bannerListener;

        public BannerInvocationImp(AdViewBannerListener bannerListener) {
            this.bannerListener = bannerListener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if ("onAdReady".equals(method.getName())) {
                    bannerListener.onAdReady();
                }
                if ("onAdRecieved".equals(method.getName())) {
                    bannerListener.onAdReceived();
                }
                if ("onAdRecieveFailed".equals(method.getName())) {
                    bannerListener.onAdFailedReceived((String) args[1]);
                }
                if ("onAdClicked".equals(method.getName())) {
                    bannerListener.onAdClicked();
                }
                if ("onAdDisplayed".equals(method.getName())) {
                    bannerListener.onAdDisplayed();
                }
                if ("onAdClosedAd".equals(method.getName())) {
                    bannerListener.onAdClosed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
