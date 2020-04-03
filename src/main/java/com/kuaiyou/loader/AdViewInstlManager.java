package com.kuaiyou.loader;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.kuaiyou.loader.loaderInterface.AdViewInstlListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 2017/3/13.
 */
public class AdViewInstlManager extends InitSDKManager {
    private Object object;

    public final static int DISPLAYMODE_DEFAULT = 0;
    public final static int DISPLAYMODE_POPUPWINDOWS = 1;
    public final static int DISPLAYMODE_DIALOG = 2;


    private final static String SETHTMLSUPPORT_METHOD_NAME = "setHtmlSupport";
    private final static String SETDISPLAYMODE_METHOD_NAME = "setDisplayMode";
    private final static String REPORTIMPRESSION_METHOD_NAME = "reportImpression";
    private final static String REPORTCLICK_METHOD_NAME = "reportClick";
    private final static String CLOSEINSTL_METHOD_NAME = "closeInstl";
    private final static String DESTROY_METHOD_NAME = "destroy";
    private final static String SHOWINSTL_METHOD_NAME = "showInstl";
    private final static String GETDIALOGVIEW_METHOD_NAME = "getDialogView";
    private final static String GETINSTLWIDTH_METHOD_NAME = "getInstlWidth";
    private final static String GETINSTLHEIGHT_METHOD_NAME = "getInstlHeight";
    private final static String SETONADVIEWLISTENER_METHOD_NAME = "setOnAdInstlListener";

    public AdViewInstlManager(Context context, String key, String posID, boolean canClosed) {
        getInstance().init(context, key);
        Class[] params = new Class[4];
        params[0] = Context.class;
        params[1] = String.class;
        params[2] = String.class;
        params[3] = boolean.class;

        Object[] objects = new Object[4];
        objects[0] = context;
        objects[1] = key;
        objects[2] = posID;
        objects[3] = canClosed;

        object = requestAd(INSTL_CLASS_NAME, params, objects);
    }

    //widler 2019 for IAB's GDPR
    public void setGDPR(boolean cmpPresent,String subjectToGDPR, String consentString,String parsedPurposeConsents, String parsedVendorConsents) {
        invoke(object, SETGDPR_METHOD_NAME, new Class[]{boolean.class,String.class,String.class,String.class,String.class},
                new Object[]{cmpPresent,subjectToGDPR,consentString,parsedPurposeConsents,parsedVendorConsents});
    }

    public void setHtmlSupport(int htmlSupport) {
        invoke(object, SETHTMLSUPPORT_METHOD_NAME, new Class[]{int.class}, new Object[]{htmlSupport});
    }

    public void setDisplayMode(int displayMode) {
        invoke(object, SETDISPLAYMODE_METHOD_NAME, new Class[]{int.class}, new Object[]{displayMode});
    }

    public void reportImpression() {
        invoke(object, REPORTIMPRESSION_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public void reportClick() {
        invoke(object, REPORTCLICK_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public View getDialogView() {
        Object view = invoke(object, GETDIALOGVIEW_METHOD_NAME, new Class[]{}, new Object[]{});
        if (null != view)
            return (View) view;
        else return null;
    }

    public int getInstlWidth() {
        Object value = invoke(object, GETINSTLWIDTH_METHOD_NAME, new Class[]{}, new Object[]{});
        try {
            if (null != value)
                return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getInstlHeight() {
        Object value = invoke(object, GETINSTLHEIGHT_METHOD_NAME, new Class[]{}, new Object[]{});
        try {
            if (null != value)
                return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void closeInstl() {
        invoke(object, CLOSEINSTL_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public boolean showInstl(Context ctx) {
        Object value = invoke(object, SHOWINSTL_METHOD_NAME, new Class[]{Context.class}, new Object[]{ctx});
        try {
            if (null != value)
                return Boolean.valueOf(String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void destroy() {
        invoke(object, DESTROY_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public void setOnAdViewListener(AdViewInstlListener instlListener) {
        try {
            Class reflectListener = Class.forName(INTERFACE_NAME);
            Object listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{reflectListener}, new InstlInvocationImp(instlListener));
            invoke(object, SETONADVIEWLISTENER_METHOD_NAME, new Class[]{reflectListener}, new Object[]{listener});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class InstlInvocationImp implements InvocationHandler {

        private AdViewInstlListener instlListener;

        public InstlInvocationImp(AdViewInstlListener instlListener) {
            this.instlListener = instlListener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if ("onAdRecieved".equals(method.getName())) {
                    instlListener.onAdReceived();
                }
                if ("onAdRecieveFailed".equals(method.getName())) {
                    instlListener.onAdFailedReceived((String) args[1]);
                }
                if ("onAdClicked".equals(method.getName())) {
                    instlListener.onAdClicked();
                }
                if ("onAdDisplayed".equals(method.getName())) {
                    instlListener.onAdDisplayed();
                }
                if ("onAdClosedAd".equals(method.getName())) {
                    instlListener.onAdClosed();
                }
                if ("onAdReady".equals(method.getName())) {
                    instlListener.onAdReady();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
