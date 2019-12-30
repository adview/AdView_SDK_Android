package com.kuaiyou.loader;

import android.content.Context;

import com.kuaiyou.loader.loaderInterface.AdViewVideoListener;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Created by Administrator on 2017/8/22.
 */

public class AdViewVideoManager extends InitSDKManager {
    private Object object;

    private final static String SET_TRAFFIC_WARN_ENABLE_METHOD_NAME = "setTrafficWarnEnable";
    private final static String SET_AUTOCLOSE_METHOD_NAME = "autoCloseEnable";
    private final static String SET_VIDEO_ORIENTATION_METHOD_NAME = "setVideoOrientation";
    private final static String SET_AUTOPLAY_METHOD_NAME = "setAutoPlay";
    private final static String PLAYVIDEO_METHOD_NAME = "playVideo";
    private final static String SET_VIDEOAPPLISTENER_METHOD_NAME = "setVideoAppListener";
    private final static String GET_INSTANCE_METHOD_NAME = "getInstance";
    private final static String INIT_METHOD_NAME = "init";
    private final static String SET_VIDEOBACKGROUNDCOLOR_METHOD_NAME = "setVideoBackgroundColor";

    public AdViewVideoManager(Context context, String appId, String posId, AdViewVideoListener appListener, boolean isPaster) {
        getInstance().init(context, appId);
        try {
            Class[] params = new Class[1];
            params[0] = Context.class;
            Object[] objects = new Object[1];
            objects[0] = context;
            object = invoke(VIDEO_CLASS_NAME, GET_INSTANCE_METHOD_NAME, params, objects);

            if (null != object) {
               init(appId, posId, isPaster);
               if (null != appListener) {
                   setOnAdViewListener(appListener);
               }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(String appid, String posid, boolean isPaster) {
        try {
            Class[] initParams = new Class[3];
            initParams[0] = String.class;
            initParams[1] = String.class;
            initParams[2] = boolean.class;

            Object[] initObjects = new Object[3];
            initObjects[0] = appid;
            initObjects[1] = posid;
            initObjects[2] = isPaster;


            invoke(object, INIT_METHOD_NAME, initParams, initObjects);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnAdViewListener(AdViewVideoListener adViewVideoInterface) {
        try {
            Object listener = null;
            Class reflectListener = Class.forName(VIDEO_INTERFACE_NAME);

            if (null != adViewVideoInterface) {
                listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{reflectListener},
                        new AdViewVideoManager.VideoInvocationImp(adViewVideoInterface));
            }
            invoke(object, SET_VIDEOAPPLISTENER_METHOD_NAME, new Class[]{reflectListener}, new Object[]{listener});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //widler 2019 for IAB's GDPR
    public void setGDPR(boolean cmpPresent,String subjectToGDPR, String consentString,String parsedPurposeConsents, String parsedVendorConsents) {
        invoke(object, SETGDPR_METHOD_NAME, new Class[]{boolean.class,String.class,String.class,String.class,String.class},
                new Object[]{cmpPresent,subjectToGDPR,consentString,parsedPurposeConsents,parsedVendorConsents});
    }

    public void setVideoBackgroundColor(String color) {
        invoke(object, SET_VIDEOBACKGROUNDCOLOR_METHOD_NAME, new Class[]{String.class}, new Object[]{color});
    }

    public void setTrafficWarnEnable(boolean trafficWarnEnable) {
        invoke(object, SET_TRAFFIC_WARN_ENABLE_METHOD_NAME, new Class[]{boolean.class}, new Object[]{trafficWarnEnable});
    }

    public void autoCloseEnable(boolean enable) {
        invoke(object, SET_AUTOCLOSE_METHOD_NAME, new Class[]{boolean.class}, new Object[]{enable});
    }
    //wilder 20191105
    public void setAutoPlay(boolean enable) {
        invoke(object, SET_AUTOPLAY_METHOD_NAME, new Class[]{boolean.class}, new Object[]{enable});
    }

    public void setVideoOrientation(int orientation) {
        invoke(object, SET_VIDEO_ORIENTATION_METHOD_NAME, new Class[]{int.class}, new Object[]{orientation});
    }

    public String getVideoVast() {
        try {
            return (String) getField(object, object.getClass(), "vastStr");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void playVideo(Context context) {
        invoke(object, PLAYVIDEO_METHOD_NAME, new Class[]{Context.class}, new Object[]{context});
    }

    private class VideoInvocationImp implements InvocationHandler{

        private AdViewVideoListener videoListener;

        public VideoInvocationImp(AdViewVideoListener videoListener) {
            this.videoListener = videoListener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if ("onReceivedVideo".equals(method.getName())) {
                    videoListener.onReceivedVideo((String) args[0]);
                }
                if ("onFailedReceivedVideo".equals(method.getName())) {
                    videoListener.onFailedReceivedVideo((String) args[0]);
                }
                if ("onVideoReady".equals(method.getName())) {
                    videoListener.onVideoReady();
                }
                if ("onVideoStartPlayed".equals(method.getName())) {
                    videoListener.onVideoStartPlayed();
                }
                if ("onVideoFinished".equals(method.getName())) {
                    videoListener.onVideoFinished();
                }
                if ("onVideoClosed".equals(method.getName())) {
                    videoListener.onVideoClosed();
                }
                if ("onPlayedError".equals(method.getName())) {
                    videoListener.onPlayedError((String) args[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
