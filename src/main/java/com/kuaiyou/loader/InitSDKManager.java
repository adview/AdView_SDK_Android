package com.kuaiyou.loader;

import android.content.Context;
import android.util.Log;

import com.iab.omid.library.adview.Omid;
import com.kuaiyou.loader.loaderInterface.InitSDKInterface;
import com.kuaiyou.utils.AdViewUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Administrator on 2017/3/9.
 */
public class InitSDKManager {

    protected final static String INSTL_CLASS_NAME      = "com.kuaiyou.adbid.AdInstlBIDView";
    protected final static String BANNER_CLASS_NAME     = "com.kuaiyou.adbid.AdBannerBIDView";
    protected final static String SPREAD_CLASS_NAME     = "com.kuaiyou.adbid.AdSpreadBIDView";
    protected final static String VIDEO_CLASS_NAME      = "com.kuaiyou.adbid.AdVideoBIDView";//"com.kuaiyou.video.AdViewVideoManager";
    protected final static String NATIVE_CLASS_NAME     = "com.kuaiyou.adbid.AdNativeBIDView";

    private final static String MD5_CLASS_NAME          = "com.kuaiyou.utils.MD5Utils";
    private final static String CONSTANT_CLASS_NAME     = "com.kuaiyou.utils.ConstantValues";
    protected final static String KY_INTERFACE_NAME     = "com.kuaiyou.interfaces.OnAdListener";//直投交换使用
    protected final static String INTERFACE_NAME        = "com.kuaiyou.interfaces.OnAdViewListener";//竞价使用
    protected final static String VIDEO_INTERFACE_NAME  = "com.kuaiyou.interfaces.AdViewVideoInterface";//视频广告
    protected final static String NATIVE_INTERFACE_NAME = "com.kuaiyou.interfaces.NativeAdCallBack";

    protected final static String SETGDPR_METHOD_NAME = "setGDPR"; //wilder 2019 for new IAB's GDPR
    //private static ScheduledExecutorService threadPool;
    //private MessageHandler messageHandler = null;
    private static InitSDKManager instance;
    //SDK壳版本号
    public final static int LOADER_VERSION = 3;

    public final static int RELEASE_VERSION = 410; //oversea version
    public final static String TAG = "SDK_LOADER";


    private boolean isSending = false;
    private Context context;

    public InitSDKManager() {
        //empty
    }


    private boolean classCheck() {
        try {
            //Class.forName(DEX_CHECK_PACKAGE_NAME);
            Class.forName(BANNER_CLASS_NAME);
            Class.forName(INSTL_CLASS_NAME);
            Class.forName(SPREAD_CLASS_NAME);
            Class.forName(NATIVE_CLASS_NAME);
            Class.forName(VIDEO_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static InitSDKManager getInstance() {
        if (null == instance)
            instance = new InitSDKManager();
        return instance;
    }

    public void init(final Context context, final String appId, final InitSDKInterface initSDKInterface) {
        Log.i(TAG, "============ OverSea SDK核心版本:" + RELEASE_VERSION + ";Loader Ver = " + LOADER_VERSION + "=============");
        //init OMSDK
        initOMSDK(context);
        //init gpid
        //AdViewUtils.getGpId(context,true);
    }

    public void init(final Context context, final String appId) {
        //如果此处判断通过，则表明并没有使用初始化方法而直接调用请求广告方法
        if (!classCheck()) {
            Log.w(TAG, "请使用初始化方法#InitSDKManager(final Context context, final String appId, final InitSDKInterface initSDKInterface),进行初始化");
            Log.w(TAG, "不建议不Init，直接调用请求广告方法");
        }
        init(context, appId, null);


    }

    //wilder 2019 for OMSDK
    private void initOMSDK(Context context) {
        //check if omsdk availiable
        AdViewUtils.checkOMSDKFeatrue();
        if (!AdViewUtils.canUseOMSDK())
            return;
        try {
            if (!Omid.isActive()) {
                boolean activated = Omid.activateWithOmidApiVersion(Omid.getVersion(), context);
                if (!activated) {
                    // SDK failed to activate. Handle appropriately.
                    Log.i(TAG, "!!!!!!!!! OverSea 初始化OMSDK失败:" + RELEASE_VERSION + ";Loader Ver = " + LOADER_VERSION + "!!!!!!!!");
                } else {
                    Log.i(TAG, "############ OverSea 初始化OMSDK成功:" + RELEASE_VERSION + ";Loader Ver = " + LOADER_VERSION + "#############");
                    AdViewUtils.createOMPartner();
                }
            }
        }catch (Exception e) {
            Log.w(TAG, "OMSDK 初始化异常!");
            e.printStackTrace();
        }

    }


    private String generalToken(String pgkName, String appId, String uuid, long time) {
        return getStaticMethodValue(MD5_CLASS_NAME,
                "MD5Encode",
                new Class[]{String.class},
                new Object[]{pgkName + appId + uuid + time + getDeclaredField(CONSTANT_CLASS_NAME, "UPDATE_ANDROID_MD5KEY")});
    }

    private String getDeclaredField(String className, String field) {
        try {
            Class clazz = Class.forName(className);
            return String.valueOf(clazz.getDeclaredField(field).get(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getStaticMethodValue(String className, String methodName, Class[] parameterTypes, Object[] args) {
        try {
            Class clazz = Class.forName(className);
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return String.valueOf(method.invoke(null, args));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        int i = 0;
        while (i < src.length) {
            int v;
            String hv;
            v = (src[i] >> 4) & 0x0F;
            hv = Integer.toHexString(v);
            stringBuilder.append(hv);
            v = src[i] & 0x0F;
            hv = Integer.toHexString(v);
            stringBuilder.append(hv);
            i++;
        }
        return stringBuilder.toString();
    }

    private boolean verifyMD5(String path, String md5) {
//        if (0 < 1)
//            return true;
        try {
            MessageDigest sig = MessageDigest.getInstance("MD5");
            File packageFile = new File(path);
            InputStream signedData = new FileInputStream(packageFile);
            byte[] buffer = new byte[4096];//每次检验的文件区大�?
            long toRead = packageFile.length();
            long soFar = 0;
            boolean interrupted = false;
            while (soFar < toRead) {
                //// TODO: 2017/3/16
                interrupted = Thread.interrupted();
                if (interrupted) break;
                int read = signedData.read(buffer);
                soFar += read;
                sig.update(buffer, 0, read);
            }
            byte[] digest = sig.digest();
            String digestStr = bytesToHexString(digest);//将得到的MD5值进行移�?
            digestStr = digestStr.toLowerCase();
            md5 = md5.toLowerCase();
            if (digestStr.equals(md5)) {//比较两个文件的MD5值，如果一样则返回true
                return true;
            }
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return false;
    }

    protected Object requestAd(String className, Class[] params, Object[] objects) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
            Constructor cons = clazz.getDeclaredConstructor(params);
            cons.setAccessible(true);
            return cons.newInstance(objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /** =======================================================================================**/
    /**
     * for static method
     *
     * @param className
     * @param methodName
     * @param classs
     * @param objects
     * @return
     */
    protected Object invoke(String className, String methodName, Class[] classs, Object[] objects) {
        Class clazz;
        try {
            clazz = Class.forName(className);
            Method refreashMethod = null;
            try {
                refreashMethod = clazz.getMethod(methodName, classs);
            } catch (NoSuchMethodException e) {
                refreashMethod = clazz.getSuperclass().getMethod(methodName, classs);
            }
            return refreashMethod.invoke(null, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Object invoke(Object object, String methodName, Class[] classs, Object[] objects) {
        Class clazz;
        try {
            clazz = Class.forName(object.getClass().getName());
            Method refreashMethod = null;
            try {
                refreashMethod = clazz.getMethod(methodName, classs);
            } catch (NoSuchMethodException e) {
                refreashMethod = clazz.getSuperclass().getMethod(methodName, classs);
            }
            return refreashMethod.invoke(object, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    protected Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = null;
        try {
            localField = cl.getDeclaredField(field);
        } catch (Exception e) {
            localField = cl.getSuperclass().getDeclaredField(field);
        }
        localField.setAccessible(true);
        return localField.get(obj);
    }


    private Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }


    private Field findField(Class<?> originClazz, String name) throws NoSuchFieldException {
        for (Class<?> clazz = originClazz; clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + originClazz);
    }

    private Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method "
                + name
                + " with parameters "
                + Arrays.asList(parameterTypes)
                + " not found in " + instance.getClass());
    }

    private static void setField(Object obj, Class<?> cl, String field,
                                 Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }


}
