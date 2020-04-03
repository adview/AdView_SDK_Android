package com.kuaiyou.adbid.instl.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.adbid.AdInstlBIDView;
import com.kuaiyou.interfaces.KyInstalListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.utils.AdActivity;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.FixedPopupWindow;
import com.kuaiyou.utils.InstlView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class AdBIDInstlAdapter extends AdAdapterManager {
    private FixedPopupWindow fixedPopupWindow = null;
    private AlertDialog instlDialog = null;
    private int instlWidth;
    private int instlHeight;
    private int currentRotation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; //这里要用到activity,因为可能会触发旋转动作

    private KyInstalListener kyInstlViewListener = null;

    private AdsBean adsBean;
    private Context context;
    private String bitmapPath = null;
    private boolean isLoaded = false;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
     AdViewUtils.logInfo("initAdapter AdBIDInstlAdapter");
    }

    @Override
    public void handleAd(Context context, Bundle bundle) {

        bitmapPath = bundle.getString("bitmapPath"); //这种情况是纯图片
        kyInstlViewListener = (KyInstalListener) bundle.getSerializable("interface");
        adsBean = kyInstlViewListener.getAdsBean();
        this.context = context;
        createInstlView(context, adsBean);

    }

    private void createInstlView(Context context, final AdsBean adsBean) {
        try {
            int width_tmp = 500, height_tmp = 600;
            if (null != instlDialog && instlDialog.isShowing())
                return;
            if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_HTML) {
                if (null != adsBean.getXhtml() && adsBean.getXhtml().length() > 0) {
                    //double density = AdViewUtils.getDensity(context); //wilder 20191205
                    //+10能容纳不太规范的页面
                    height_tmp = adsBean.getAdHeight() + 10;
                    width_tmp = adsBean.getAdWidth() + 10;
                    //DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
                    //int screenSize[] = AdViewUtils.getWidthAndHeight(context, false, true);
//                    width_tmp = displayMetrics.widthPixels;
//                    height_tmp = displayMetrics.heightPixels;
                }
            } else {
                // 非H5的广告只获得图片尺寸
                if ( !AdViewUtils.bitmapOnLine ) { //wilder 2019
                    if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_MIXED) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(bitmapPath, opts);
                        height_tmp = opts.outHeight;
                        width_tmp = opts.outWidth;
                    }
                }else {
                    //用于at = 3等纯图片的情况
                    height_tmp = adsBean.getAdHeight();
                    width_tmp = adsBean.getAdWidth();
                }
            }
            //计算instl的宽高
            HashMap<String, Integer> sizeMap = getLayoutSize(context, adsBean,width_tmp, height_tmp);
            //(wilder 2019)
            instlView = new InstlView(context, sizeMap, adsBean.getAdType());
            instlView.setInstlViewListener(kyInstlViewListener);
            instlView.setContent(adsBean, bitmapPath);

            instlWidth = sizeMap.get(ConstantValues.INSTL_WIDTH_KEY);
            instlHeight = sizeMap.get(ConstantValues.INSTL_HEIGHT_KEY);

            adsBean.setRealAdWidth(instlWidth);
            adsBean.setRealAdHeight(instlHeight);

            final WebView webView = instlView.getWebView();
            if (null != webView) {
                if (null != kyInstlViewListener)
                    kyInstlViewListener.setClickMotion(instlView.getMraidView(), null);
//                setClickMotion(instlView.getMraidView(), adsBean, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //该方法使用于activity作为启动参数的情形，和dialog的区别是它背景可透明
    private boolean showPopupWindows(final Context ctx) {
        try {
            if (ctx instanceof Activity) {
                //wilder 2020 for context is not activity
                final Activity activity = null == ctx ? (Activity) context : (Activity) ctx;
                if (null == fixedPopupWindow || !fixedPopupWindow.isShowing()) {
                    //用 popupwindow加载instlView
                    fixedPopupWindow = new FixedPopupWindow(instlView, adsBean.getRealAdWidth(), adsBean.getRealAdHeight());

                    fixedPopupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER);
                    if (fixedPopupWindow.isShowing()) {
                        if (null != kyInstlViewListener)
                            kyInstlViewListener.onDisplay(null, false);

                        switch (activity.getWindow().getWindowManager().getDefaultDisplay().getRotation()) {
                            case Surface.ROTATION_0:
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                break;
                            case Surface.ROTATION_90:
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                break;
                            case Surface.ROTATION_180:
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                                break;
                            case Surface.ROTATION_270:
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                                break;
                        }
                    }
                    currentRotation = activity.getRequestedOrientation();;
                    fixedPopupWindow.setOnDismissListener(new FixedPopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            activity.setRequestedOrientation(currentRotation);
//                        if (null != kyInstlViewListener)              wilder 20190612
//                            kyInstlViewListener.onCloseBtnClicked();
                            try {
/* wilder 2020 still not use ，sometimes can cause exception
                                if (null != instlView) {
                                    if (null != instlView.getWebView()) {
                                        //修复了WebView.destroy() called while still attached!的bug
                                        ViewGroup vp = (ViewGroup) instlView.getParent();
                                        if (vp != null) {
                                            vp.removeView(instlView);
                                            //instlView.getWebView().loadUrl("about:blank");
                                        }
                                        instlView.getWebView().removeAllViews();
                                        instlView.getWebView().destroy();
                                    }
                                }
*/
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_HTML) {
                                WebView webView = instlView.getWebView();
                                if (null != webView) {
                                    if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                                        AdViewUtils.loadWebContentExt(instlView.getWebView(), adsBean.getXhtml());
                                    } else {
                                        instlView.getWebView().loadUrl(adsBean.getXhtml());
                                    }
                                } else {
                                    AdViewUtils.logInfo("###  webview is null error !!! ###");
                                }

                            } else if (adsBean.getAdType() != ConstantValues.RESP_ADTYPE_MIXED) {
                                //wilder 2019，纯图片的
                                if (AdViewUtils.bitmapOnLine) {
                                    AdViewUtils.loadWebImageURL(instlView.getWebView(), bitmapPath, adsBean.getAdLink());
                                } else {
                                    AdViewUtils.loadWebImageLocal(instlView.getWebView(), bitmapPath, adsBean.getAdLink(), instlWidth, instlHeight);
                                }
                            }
                        }
                    }, 0);
                }
                return true;
            }else {
                if (null != fixedPopupWindow)
                    fixedPopupWindow.dismiss();
                AdViewUtils.logInfo("====== error, you must use activity's context to handle popup window !! ========");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (null != fixedPopupWindow)
                fixedPopupWindow.dismiss();
            return false;
        }

    }

    //使用默认最顶层的activity的方法来驱动，使用的alertDialog,该dialog必须运行在Activity的UI线程中
    private boolean showDialog(final Context context) {
       //Context ctx = null == context ? this.context : context;
        final Context ctx = (context instanceof Activity) ? context : AdViewUtils.getActivity();
       if (context instanceof Activity) {
           AdViewUtils.logInfo("---- show dilaog ---");
       } else {
          AdViewUtils.logInfo("### showDialog() must use Activity ###");
//           return false;
           //ctx = AdViewUtils.getActivity().getBaseContext(); 这样的用法无法弹出，会报找不到activity
       }
        try {
            if (null == instlDialog || !instlDialog.isShowing()) {
                //alertdialog必须在UI线程，所以只适用于有activity的情况
                instlDialog = new AlertDialog.Builder(ctx).create();
                instlDialog.setCancelable(false);
                instlDialog.setCanceledOnTouchOutside(false);
                // 关闭插屏之后解除横竖屏限制
                instlDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // TODO Auto-generated method stub
                                //((Activity) ctx).setRequestedOrientation(currentRotation); wilder 20200226 for test
//                                if (null != kyInstlViewListener)          wilder 20190612
//                                    kyInstlViewListener.onCloseBtnClicked();

                                try {
                                    if (null != instlView) {
                                        if (null != instlView.getWebView()) {
                                            ViewGroup vp = (ViewGroup) instlView.getParent();
                                            vp.removeView(instlView);
                                            instlView.getWebView().loadUrl("about:blank");
                                            instlView.getWebView().removeAllViews();
                                            instlView.getWebView().destroy();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                // 展示插屏广告时禁止横竖屏切换
                instlDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        if (null != kyInstlViewListener)
                            kyInstlViewListener.onDisplay(null, false);

                        currentRotation = ((Activity) ctx).getRequestedOrientation();
                        //int rotation = context.getResources().getConfiguration().orientation;
                        switch (((Activity) ctx).getWindow().getWindowManager().getDefaultDisplay().getRotation()) {
                            case Surface.ROTATION_0:
                                ((Activity) ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                break;
                            case Surface.ROTATION_90:
                                ((Activity) ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                break;
                            case Surface.ROTATION_180:
                                ((Activity) ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                                break;
                            case Surface.ROTATION_270:
                                ((Activity) ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                                break;
                        }
                    }
                });
                instlDialog.show();
                FrameLayout.LayoutParams frameLayout = new FrameLayout.LayoutParams(-2, -2);
                frameLayout.gravity = Gravity.CENTER;
                instlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                instlDialog.getWindow().addContentView(instlView, frameLayout);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_HTML) {
                            final WebView webView = instlView.getWebView();
                            if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                                AdViewUtils.loadWebContentExt(webView, adsBean.getXhtml());
                            } else
                                webView.loadUrl(adsBean.getXhtml());
                        }
                    }
                }, 0);
                return true;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            instlDialog.cancel();
            return false;
        }
        instlDialog.cancel();
        return false;
    }

    //wilder 2020 该方法适用于没有activity的情况
    private boolean showDialogActivity(Context context) {
        final Context ctx = null == context ? this.context : context;
        try {
            if (null == instlDialog || !instlDialog.isShowing()) {

                Intent intent = new Intent(context, AdActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //不加这个参数对于可能无法显示
                //intent.putExtra("mode", "1");
                intent.putExtra("data",adsBean);
                intent.putExtra("bmpPath", bitmapPath);
                intent.putExtra("insWidth",instlWidth);
                intent.putExtra("insHeight", instlHeight);
                context.startActivity(intent);

                //发送展示汇报
                if (null != kyInstlViewListener)
                    kyInstlViewListener.onDisplay(null, false);


                return true;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            //instlDialog.cancel();
            return false;
        }
        //instlDialog.cancel();
        return false;
    }
    /**
     * 展示插屏广告
     *
     * @param context
     * @return 展示成功返回true 否则false
     */
    @Override
    public boolean showInstl(final Context context) {
        try {
            int notifyType = 0;
            if (null != adsBean) {
                if (System.currentTimeMillis() - adsBean.getDataTime() > ConstantValues.AD_EXPIRE_TIME) {
                    return false;//超时 失败
                }
            } else {
                return false;
            }
            if (null != kyInstlViewListener)
                notifyType = kyInstlViewListener.getDisplayMode();
            switch (notifyType) {
                case AdInstlBIDView.DISPLAYMODE_DEFAULT:
                case AdInstlBIDView.DISPLAYMODE_POPUPWINDOWS:
                    //缺省用的popup的window
                    return showPopupWindows(context); //context
                case AdInstlBIDView.DISPLAYMODE_DIALOG:
                    return showDialogActivity(context); //wilder 2020
                    //return showDialog(context); //wilder 20200226 for test
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public InstlView getInstlView() {
        return instlView;
    }

    /**
     * 自定义插屏调用方法
     * 获取当前插屏视图view
     */
    @Override
    public View getDialogView() {
        if (System.currentTimeMillis() - adsBean.getDataTime() <= ConstantValues.AD_EXPIRE_TIME) {
            if (!isLoaded && null != adsBean.getXhtml()) {
                if (null != instlView && null != instlView.getWebView()) {
//                    AdWebView.injectMraidJs(instlView.getWebView());
                    if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                        //instlView.getWebView().loadDataWithBaseURL(ConstantValues.WEBVIEW_BASEURL, adsBean.getXhtml(), "text/html", "UTF-8", null);
                        AdViewUtils.loadWebContentExt(instlView.getWebView(), adsBean.getXhtml());
                    } else
                        instlView.getWebView().loadUrl(adsBean.getXhtml());
//                    instlView.getWebView().loadData(adsBean.getXhtml(),
//                            "text/html; charset=UTF-8", null);
                    isLoaded = true;
                }
            }
            return instlView;
        } else {
            AdViewUtils.logInfo("AD_EXPIRED - VIEW RETURN NULL");
            return null;
        }

    }

    /**
     * 手动调用 关闭插屏，发生在点击close按钮
     */
    @Override
    public void closeInstl() {
        try {

            if (null != instlDialog && instlDialog.isShowing()) {
                instlDialog.cancel();
            }
            if (null != fixedPopupWindow && fixedPopupWindow.isShowing()) {
                fixedPopupWindow.dismiss();
            }
            //wilder 2020 for new dialog for context
            if (AdActivity.getInstance() != null) {
                AdActivity.getInstance().finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Integer>
    getLayoutSize(Context context, AdsBean adsBean, int width_tmp, int height_tmp) {
        int screenWidth = 0;
        int screenHeight = 0;
        int instl_Width = 0;
        int instl_Height = 0;
        int bitmapWidth = 0;
        int bitmapHeight = 0;
        int frameWidth = 0;
        int frameHeight = 0;
        float zoomPercent = 0F;
        int dScale;
        float density;

        HashMap<String, Integer> sizeMap = new HashMap<String, Integer>();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        density = dm.density;
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        bitmapWidth = width_tmp;
        bitmapHeight = height_tmp;

        frameWidth = screenWidth / 8 * 7;
        frameHeight = screenHeight - screenWidth / 8
                - AdViewUtils.getStatusBarHeight(context);

        if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_MIXED) {
            if (screenWidth > screenHeight) {
                instl_Width = screenHeight * 7 / 8;
                instl_Height = instlWidth * 5 / 6;
            } else {
                instl_Width = frameWidth;
                instl_Height = instlWidth * 5 / 6;
            }
        } else {
            if (null != adsBean.getXhtml() && adsBean.getXhtml().length() > 0) {
                if ((int) (width_tmp * AdViewUtils.getDensity(context)) > screenWidth
                        || (int) (height_tmp * AdViewUtils.getDensity(context)) > screenHeight) {
                    // 超出屏幕范围
                    if (((float) bitmapWidth / (float) frameWidth) > ((float) bitmapHeight / (float) frameHeight)) {
                        zoomPercent = ((float) bitmapWidth / (float) frameWidth);
                        instl_Width = (int) (bitmapWidth / zoomPercent);
                        instl_Height = (int) (bitmapHeight / zoomPercent);
                    } else {
                        zoomPercent = ((float) bitmapHeight / (float) frameHeight);
                        instl_Width = (int) (bitmapWidth / zoomPercent);
                        instl_Height = (int) (bitmapHeight / zoomPercent);
                    }
                    dScale = instl_Height * 100 / bitmapHeight;
                } else {
                    //合理范围
                    dScale = 0;
                    instl_Width = frameWidth = (int) (width_tmp * AdViewUtils.getDensity(context));
                    instl_Height = /*bitmapHeight*/frameHeight = (int) (height_tmp * AdViewUtils.getDensity(context));
                }
            } else {
                if (((float) bitmapWidth / (float) frameWidth) > ((float) bitmapHeight / (float) frameHeight)) {
                    zoomPercent = ((float) bitmapWidth / (float) frameWidth);
                    instl_Width = (int) (bitmapWidth / zoomPercent);
                    instl_Height = (int) (bitmapHeight / zoomPercent);
                } else {
                    zoomPercent = ((float) bitmapHeight / (float) frameHeight);
                    instl_Width = (int) (bitmapWidth / zoomPercent);
                    instl_Height = (int) (bitmapHeight / zoomPercent);
                }
            }
        }
        //instlWidth = (int) (instlWidth - density);
        //instlHeight = (int) (instlHeight - density);


        sizeMap.put("screenWidth", screenWidth);// 480
        sizeMap.put("screenHeight", screenHeight);// 854
        sizeMap.put("frameWidth", frameWidth);// 480/16*15
        sizeMap.put("frameHeight", frameHeight);// 854-480/16

        sizeMap.put(ConstantValues.INSTL_WIDTH_KEY, instl_Width);
        sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, instl_Height);

        return sizeMap;
    }


}
