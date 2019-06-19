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

    private final static String SETTRAFFICWARNENABLE_METHOD_NAME = "setTrafficWarnEnable";
    private final static String AUTOCLOSEENABLE_METHOD_NAME = "autoCloseEnable";
    private final static String SETVIDEOORIENTATION_METHOD_NAME = "setVideoOrientation";
    private final static String GETVIDEOVAST_METHOD_NAME = "getVideoVast";
    private final static String PLAYVIDEO_METHOD_NAME = "playVideo";
    private final static String SETONADVIEWLISTENER_METHOD_NAME = "setVideoAppListener";
    private final static String GETINSTANCE_METHAD_NAME = "getInstance";
    private final static String INIT_METHAD_NAME = "init";
    private final static String SETVIDEOBACKGROUNDCOLOR_METHAD_NAME = "setVideoBackgroundColor";

    public AdViewVideoManager(Context context, String appId, String posId, AdViewVideoListener appListener, boolean isPaster, String gdpr) {
        getInstance().init(context, appId);
        try {
            /*
            Class[] params = new Class[1];
            params[0] = Context.class;
            Object[] objects = new Object[1];
            objects[0] = context;
            object = requestAd(VIDEO_CLASS_NAME, params, objects);
            */
            Class[] params = new Class[1];
            params[0] = Context.class;
            Object[] objects = new Object[1];
            objects[0] = context;
            object = invoke(VIDEO_CLASS_NAME, GETINSTANCE_METHAD_NAME, params, objects);

            if (null != object) {
               init(appId, posId, isPaster, gdpr);
               setAppInterface(appListener);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(String appid, String posid, boolean isPaster, String gdpr) {
        try {
            Class[] initParams = new Class[4];
            initParams[0] = String.class;
            initParams[1] = String.class;
            initParams[2] = boolean.class;
            initParams[3] = String.class;
            Object[] initObjects = new Object[4];
            initObjects[0] = appid;
            initObjects[1] = posid;
            initObjects[2] = isPaster;
            initObjects[3] = gdpr;

            invoke(object, INIT_METHAD_NAME, initParams, initObjects);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAppInterface(AdViewVideoListener adViewVideoInterface) {
        try {
            Object listener = null;
            Class reflectListener = Class.forName(VIDEO_INTERFACE_NAME);

            if (null != adViewVideoInterface) {
                listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{reflectListener}, new AdViewVideoManager.VideoInvocationImp(adViewVideoInterface));
            }
            invoke(object, SETONADVIEWLISTENER_METHOD_NAME, new Class[]{reflectListener}, new Object[]{listener});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoBackgroungColor(String color) {
        invoke(object, SETVIDEOBACKGROUNDCOLOR_METHAD_NAME, new Class[]{String.class}, new Object[]{color});
    }

    public void setTrafficWarnEnable(boolean trafficWarnEnable) {
        invoke(object, SETTRAFFICWARNENABLE_METHOD_NAME, new Class[]{boolean.class}, new Object[]{trafficWarnEnable});
    }

    public void autoCloseEnable(boolean enable) {
        invoke(object, AUTOCLOSEENABLE_METHOD_NAME, new Class[]{boolean.class}, new Object[]{enable});
    }

    public void setVideoOrientation(int orientation) {
        invoke(object, SETVIDEOORIENTATION_METHOD_NAME, new Class[]{int.class}, new Object[]{orientation});
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
