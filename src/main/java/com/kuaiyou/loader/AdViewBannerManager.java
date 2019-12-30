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

    // banner大小，based on ConstantValues,这是对外接口，不宜改动
    public static int BANNER_AUTO_FILL = 0; //320 x 50
    public static int BANNER_MREC = 1;     //300x250
    public static int BANNER_480X75 = 2;
    public static int BANNER_728X90 = 3;
    public static int BANNER_SMART = 5;

    private Object object;

    private final static String SET_HTMLSUPPORT_METHOD_NAME = "setHtmlSupport";
    private final static String SET_REFRESHTIME_METHOD_NAME = "setReFreshTime";
    private final static String SET_SHOWCLOSEBTN_METHOD_NAME = "setShowCloseBtn";
    private final static String SET_OPENANIM_METHOD_NAME = "setOpenAnim";
    private final static String SET_ONADVIEWLISTENER_METHOD_NAME = "setOnAdViewListener";
    private final static String STOP_REQUEST_METHOD_NAME = "stopRequest";
    private final static String SET_VIDEOINFO_METHOD_NAME = "setVideoInfo"; //wilder 2019 for mrec
    private final static String PLAYVIDEO_METHOD_NAME = "playVideo"; //wilder 2019 for mrec
    private final static String SET_AUTOPLAY_METHOD_NAME = "setAutoPlay";

    public AdViewBannerManager(Context context, String key, String vPosID, int routeType, int adSize, boolean canClosed) {
        getInstance().init(context, key);
        Class[] params = new Class[5];
        params[0] = Context.class;
        params[1] = String.class;
        params[2] = int.class;
        params[3] = int.class;
        params[4] = String.class;
        Object[] objects = new Object[5];
        objects[0] = context;
        objects[1] = key;
        objects[2] = routeType;
        objects[3] = adSize;
        objects[4] = vPosID;
        object = requestAd(BANNER_CLASS_NAME, params, objects);

        if (null != object) {
            setShowCloseBtn(canClosed);
        }

    }

    public AdViewBannerManager(Context context, String key, String vPosID, int adSize, boolean canClosed) {
        //ConstantValues.ROUTE_ADBID_TYPE
        this(context, key, vPosID, 998, adSize, canClosed);
    }

    //widler 2019 for IAB's GDPR
    public void setGDPR(boolean cmpPresent,String subjectToGDPR, String consentString,String parsedPurposeConsents, String parsedVendorConsents) {
        invoke(object, SETGDPR_METHOD_NAME, new Class[]{boolean.class,String.class,String.class,String.class,String.class},
                        new Object[]{cmpPresent,subjectToGDPR,consentString,parsedPurposeConsents,parsedVendorConsents});
    }

    //wilder 2019 for MRec
    public void playVideo(Context context) {
        invoke(object, PLAYVIDEO_METHOD_NAME, new Class[]{Context.class}, new Object[]{context});
    }
    //wilder 20191105
    public void setAutoPlay(boolean enable) {
        invoke(object, SET_AUTOPLAY_METHOD_NAME, new Class[]{boolean.class}, new Object[]{enable});
    }
    public void setVideoInfo(String posID) {
        invoke(object, SET_VIDEOINFO_METHOD_NAME, new Class[]{String.class}, new Object[]{posID});
    }

    public void setHtmlSupport(int htmlSupport) {
        invoke(object, SET_HTMLSUPPORT_METHOD_NAME, new Class[]{int.class}, new Object[]{htmlSupport});
    }

    public void setRefreshTime(int sec) {
        invoke(object, SET_REFRESHTIME_METHOD_NAME, new Class[]{int.class}, new Object[]{sec});
    }

    public void setShowCloseBtn(boolean closeAble) {
        invoke(object, SET_SHOWCLOSEBTN_METHOD_NAME, new Class[]{boolean.class}, new Object[]{closeAble});
    }

    public void setOpenAnim(boolean openAnim) {
        invoke(object, SET_OPENANIM_METHOD_NAME, new Class[]{boolean.class}, new Object[]{openAnim});
    }

    public void setBannerStopRequest() {
        invoke(object, STOP_REQUEST_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public void setOnAdViewListener(AdViewBannerListener bannerListener) {
        try {
            Class reflectListener = Class.forName(INTERFACE_NAME);
            Object listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{reflectListener},
                    new BannerInvocationImp(bannerListener));

            invoke(object, SET_ONADVIEWLISTENER_METHOD_NAME, new Class[]{reflectListener}, new Object[]{listener});
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
