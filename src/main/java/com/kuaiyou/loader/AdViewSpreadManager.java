package com.kuaiyou.loader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kuaiyou.loader.loaderInterface.AdViewSpreadListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 2017/3/13.
 */
public class  AdViewSpreadManager extends InitSDKManager {
    private Object object;

    private final static String SETSPREADNOTIFYTYPE_METHOD_NAME = "setSpreadNotifyType";

    private final static String GETPARENTLAYOUT_METHOD_NAME = "getParentLayout";
    private final static String SETHTMLSUPPORT_METHOD_NAME = "setHtmlSupport";
    private final static String SETBACKGROUNDCOLOR = "setBackgroundColor";
    private final static String SETBACKGROUNDDRAWABLE = "setBackgroundDrawable";
    private final static String SETLOGOID_METHOD_NAME = "setLogo";
    private final static String SETLOGOIMAGE_METHOD_NAME = "setLogo";
    private final static String GETLOGOVIEW_METHOD_NAME = "getLogoView";
    private final static String CANCELAD_METHOD_NAME = "cancelAd";
    private final static String DESTROY_METHOD_NAME = "destroy";

    // 顶部倒计时/跳过 功能类型
    public final static int NOTIFY_COUNTER_NULL = 0;
    public final static int NOTIFY_COUNTER_NUM = 1;
    public final static int NOTIFY_COUNTER_TEXT = 2;
    public final static int NOTIFY_COUNTER_CUSTOM = 3;

    private final static String SETONADVIEWLISTENER_METHOD_NAME = "setOnAdSpreadListener";

    public AdViewSpreadManager(Context context, String key, ViewGroup view) {
        getInstance().init(context, key);
        Class[] params = new Class[3];
        params[0] = Context.class;
        params[1] = String.class;
        params[2] = ViewGroup.class;

        Object[] objects = new Object[3];
        objects[0] = context;
        objects[1] = key;
        objects[2] = view;

        object = requestAd(SPREAD_CLASS_NAME, params, objects);
    }

    //widler 2019 for IAB's GDPR
    public void setGDPR(boolean cmpPresent,String subjectToGDPR, String consentString,String parsedPurposeConsents, String parsedVendorConsents) {
        invoke(object, SETGDPR_METHOD_NAME, new Class[]{boolean.class,String.class,String.class,String.class,String.class},
                new Object[]{cmpPresent,subjectToGDPR,consentString,parsedPurposeConsents,parsedVendorConsents});
    }

    public void setHtmlSupport(int htmlSupport) {
        invoke(object, SETHTMLSUPPORT_METHOD_NAME, new Class[]{int.class}, new Object[]{htmlSupport});
    }

    public void setBackgroundColor(int color) {
        invoke(object, SETBACKGROUNDCOLOR, new Class[]{int.class}, new Object[]{color});
    }

    public void setBackgroundDrawable(Drawable drawable) {
        invoke(object, SETBACKGROUNDDRAWABLE, new Class[]{Drawable.class}, new Object[]{drawable});
    }

    public void setLogo(int id) {
        invoke(object, SETLOGOID_METHOD_NAME, new Class[]{int.class}, new Object[]{id});
    }

    public View setLogo(String image) {
        Object view = invoke(object, SETLOGOIMAGE_METHOD_NAME, new Class[]{String.class}, new Object[]{image});
        if (null != view)
            return (View) view;
        else return null;
    }

    public ImageView getLogoView() {
        Object value = invoke(object, GETLOGOVIEW_METHOD_NAME, new Class[]{}, new Object[]{});
        try {
            if (null != value)
                return (ImageView) value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setSpreadNotifyType(int type) {
        invoke(object, SETSPREADNOTIFYTYPE_METHOD_NAME, new Class[]{int.class}, new Object[]{type});
    }

    public RelativeLayout getParentLayout() {
        Object value = invoke(object, GETPARENTLAYOUT_METHOD_NAME, new Class[]{}, new Object[]{});
        try {
            if (null != value)
                return (RelativeLayout) value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cancelAd() {
        invoke(object, CANCELAD_METHOD_NAME, new Class[]{}, new Object[]{});
    }

    public void destroy() {
        invoke(object, DESTROY_METHOD_NAME, new Class[]{}, new Object[]{});
    }


    public void setOnAdViewListener(AdViewSpreadListener spreadListener) {
        try {
            Class reflectListener = Class.forName(INTERFACE_NAME);
            Object listener = Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{reflectListener}, new SpreadInvocationImp(spreadListener));
            invoke(object, SETONADVIEWLISTENER_METHOD_NAME, new Class[]{reflectListener}, new Object[]{listener});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SpreadInvocationImp implements InvocationHandler {

        private AdViewSpreadListener spreadListener;

        public SpreadInvocationImp(AdViewSpreadListener spreadListener) {
            this.spreadListener = spreadListener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if ("onAdRecieved".equals(method.getName())) {
                    spreadListener.onAdReceived();
                }
                if ("onAdRecieveFailed".equals(method.getName())) {
                    spreadListener.onAdFailedReceived((String) args[1]);
                }
                if ("onAdClicked".equals(method.getName())) {
                    spreadListener.onAdClicked();
                }
                if ("onAdDisplayed".equals(method.getName())) {
                    spreadListener.onAdDisplayed();
                }
                if ("onAdClosedAd".equals(method.getName())) {
                    spreadListener.onAdClosed();
                }
                if ("onAdSpreadPrepareClosed".equals(method.getName())) {
                    spreadListener.onAdSpreadPrepareClosed();
                }

                if ("onAdClosedByUser".equals(method.getName())) {
                    spreadListener.onAdClosedByUser();
                }
                if ("onAdNotifyCustomCallback".equals(method.getName())) {
                    spreadListener.onAdNotifyCustomCallback(Integer.valueOf(args[1].toString()), Integer.valueOf(args[2].toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
