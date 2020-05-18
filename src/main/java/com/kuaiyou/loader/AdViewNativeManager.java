package com.kuaiyou.loader;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.kuaiyou.loader.loaderInterface.AdViewNativeListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13.
 */
public class AdViewNativeManager extends InitSDKManager {
    //private Object object;

    private final static String SETHTMLSUPPORT_METHOD_NAME = "setHtmlSupport";
    private final static String SETBROWSERTYPE_METHOD_NAME = "setBrowserType";
    private final static String SETNATIVESIZE_METHOD_NAME = "setNativeSize";
    private final static String SETADACT_METHOD_NAME = "setAdAct";
    private final static String SETADTYPE_METHOD_NAME = "setAdType";
    private final static String GETADTYPE_METHOD_NAME = "getAdType";
    private final static String REQUESTAD_METHOD_NAME = "requestAd";
    private final static String REQUESTAD_COUNT_METHOD_NAME = "requestAd";
    private final static String REPORTCLICK_METHOD_NAME = "reportClick";
    private final static String REPORTVIDEOSTATUS_METHOD_NAME = "reportVideoStatus";
    private final static String REPORTIMPRESSION_METHOD_NAME = "reportImpression";
    private final static String DESTROYNATIVEAD_METHOD_NAME = "destroyNativeAd";
    private final static String SET_NATIVELISTENER_METHOD_NAME = "setAppNativeListener";


    //omsdk used
    private final static String SHOWPRIVACYINFO_METHOD_NAME = "showNativePrivacyInformation"; //wilder 2019 for privacy information
    private final static String OMSDK_METHOD_VIDEO_ADD_SCRIPT_RESOURCE = "addOMNativeScriptResrouce";
    private final static String OMSDK_METHOD_NATIVE_CREATE_SESSION = "createOMNativeSession";
    private final static String OMSDK_METHOD_VIDEO_CREATE_VIDEO_SESSION = "createOMNativeVideoSession";
    private final static String OMSDK_METHOD_VIDEO_ADD_VIDEO_OBSTRUCTIONS = "addOMNativeVideoObstructions";
    private final static String OMSDK_METHOD_VIDEO_START_SESSION = "startOMSession";
    private final static String OMSDK_METHOD_VIDEO_REG_VIDEOLOADED_EVENT = "regOMNativeVideoLoadedEvent";
    private final static String OMSDK_METHOD_NATIVE_STOP_SESSION = "stopOMSession";

    public AdViewNativeManager(Context context, String appId, String posId, AdViewNativeListener nativeListener) {
        getInstance().init(context, appId);
        try {
            //创建listener对象
            Class reflectListener = Class.forName(NATIVE_INTERFACE_NAME);
            Object listener = null;
            if (null != nativeListener) {
                listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{reflectListener},
                        new NativeInvocationImp(nativeListener));
            }

            Class[] params = new Class[4];
            params[0] = Context.class;
            params[1] = String.class;
            params[2] = String.class;
            params[3] = reflectListener;

            Object[] objects = new Object[4];
            objects[0] = context;
            objects[1] = appId;
            objects[2] = posId;
            objects[3] = listener;

            object = requestAd(NATIVE_CLASS_NAME, params, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setAdSize(int w, int h) {
        invoke(object, SETNATIVESIZE_METHOD_NAME, new Class[]{int.class, int.class}, new Object[]{w, h});
    }

    public void setHtmlSupport(int htmlSupport) {
        invoke(object, SETHTMLSUPPORT_METHOD_NAME, new Class[]{int.class}, new Object[]{htmlSupport});
    }

    public void setBrowserType(int type) {
        invoke(object, SETBROWSERTYPE_METHOD_NAME, new Class[]{int.class}, new Object[]{type});
    }

    @Deprecated
    public void reportClick(String adi, int x, int y) {
        invoke(object, REPORTCLICK_METHOD_NAME, new Class[]{String.class, int.class, int.class}, new Object[]{adi, x, y});
    }

    @Deprecated
    public void reportImpression(String adi) {
        invoke(object, REPORTIMPRESSION_METHOD_NAME, new Class[]{String.class}, new Object[]{adi});
    }

    @Deprecated
    public void reportClick(String adi) {
        invoke(object, REPORTCLICK_METHOD_NAME, new Class[]{String.class}, new Object[]{adi});
    }

    public void reportClick(View view, String adi, int x, int y) {
        invoke(object, REPORTCLICK_METHOD_NAME, new Class[]{View.class, String.class, int.class, int.class}, new Object[]{view, adi, x, y});
    }

    public void reportImpression(View view, String adi) {
        invoke(object, REPORTIMPRESSION_METHOD_NAME, new Class[]{View.class, String.class}, new Object[]{view, adi});
    }

    public void setAdAct(int adAct) {
        invoke(object, SETADACT_METHOD_NAME, new Class[]{int.class}, new Object[]{adAct});
    }

    public void destroyNativeAd() {
        invoke(object, DESTROYNATIVEAD_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public void reportVideoStatus(Context context, String adId, int status) {
        invoke(object, REPORTVIDEOSTATUS_METHOD_NAME, new Class[]{Context.class, String.class, int.class}, new Object[]{context, adId, status});
    }

    public void setAdType(int adType) {
        invoke(object, SETADTYPE_METHOD_NAME, new Class[]{int.class}, new Object[]{adType});
    }

    public int getAdType() {
        Object value = invoke(object, GETADTYPE_METHOD_NAME, new Class[]{}, new Object[]{});
        try {
            if (null != value)
                return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void requestAd() {
        invoke(object, REQUESTAD_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public void requestAd(int count) {
        invoke(object, REQUESTAD_COUNT_METHOD_NAME, new Class[]{int.class}, new Object[]{count});
    }

    //wilder 2019 for privacy info
    public void showPrivacyInfo() {
        invoke(object, SHOWPRIVACYINFO_METHOD_NAME, new Class[]{}, new Object[]{});
    }
    //wilder 20191128 add new native listener
    public void setOnAdViewListener(AdViewNativeListener nativeListener) {
        try {
            Class reflectListener = Class.forName(NATIVE_INTERFACE_NAME);
            Object listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{reflectListener},
                    new NativeInvocationImp(nativeListener));

            invoke(object, SET_NATIVELISTENER_METHOD_NAME, new Class[]{reflectListener}, new Object[]{listener});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////////////////////     omsdk native & video    /////////////////////////////////
    //(0) create native ad session
    public void omsdkNativeCreateSession(View v) {
        invoke(object, OMSDK_METHOD_NATIVE_CREATE_SESSION, new Class[]{View.class}, new Object[]{v});
    }
    //(1)prepare parameters
    public void omsdkVideoAddScriptResrouce(String vendorKey, String scriptUrl, String params) {
        invoke(object, OMSDK_METHOD_VIDEO_ADD_SCRIPT_RESOURCE, new Class[]{String.class, String.class, String.class},
                                                new Object[]{vendorKey, scriptUrl, params});
    }
    //(2)create session
    public void omsdkVideoCreateSession(View v) {
        invoke(object, OMSDK_METHOD_VIDEO_CREATE_VIDEO_SESSION, new Class[]{View.class}, new Object[]{v});
    }
    //(3)add view 's obstructions
    public void omsdkVideoAddObstructions(View v) {
        invoke(object, OMSDK_METHOD_VIDEO_ADD_VIDEO_OBSTRUCTIONS, new Class[]{View.class}, new Object[]{v});
    }
    //(4)start session
    public void omsdkVideoStartSession() {
        invoke(object, OMSDK_METHOD_VIDEO_START_SESSION, new Class[]{}, new Object[]{});
    }
    //(5)send video loaded event
    public void omsdkVideoRegLoadEvent(float skipOffset, boolean isAutoPlay) {
        invoke(object, OMSDK_METHOD_VIDEO_REG_VIDEOLOADED_EVENT, new Class[]{Float.class, Boolean.class}, new Object[]{skipOffset, isAutoPlay});
    }
    //(6)use reportVideoStatus(), during playback progress, it will send many video events
    //(7)after play done, if do not use native ,  stop session
    public void omsdkNativeStopSession() {
        invoke(object, OMSDK_METHOD_NATIVE_STOP_SESSION, new Class[]{}, new Object[]{});
    }
    //////////////////////    end omsdk   ///////////////////////////

    private class NativeInvocationImp implements InvocationHandler {

        private AdViewNativeListener nativeListener;

        public NativeInvocationImp(AdViewNativeListener nativeListener) {
            this.nativeListener = nativeListener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if ("onNativeAdReceived".equals(method.getName())) {
                    nativeListener.onNativeAdReceived((List<HashMap>) args[0]);
                }
                if ("onNativeAdReceiveFailed".equals(method.getName())) {
                    nativeListener.onNativeAdReceiveFailed((String) args[0]);
                }
                if ("onDownloadStatusChange".equals(method.getName())) {
                    nativeListener.onDownloadStatusChange((Integer) args[0]);
                }
                if ("onNativeAdClosed".equals(method.getName())) {
                    nativeListener.onNativeAdClosed((View) args[0]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
