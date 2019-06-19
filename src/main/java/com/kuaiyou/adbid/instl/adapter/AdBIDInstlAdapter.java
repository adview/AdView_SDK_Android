package com.kuaiyou.adbid.instl.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.kuaiyou.interfaces.KyViewListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.mraid.interfaces.MRAIDNativeFeatureListener;
import com.kuaiyou.mraid.interfaces.MRAIDViewListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.FixedPopupWindow;
import com.kuaiyou.utils.InstlView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class AdBIDInstlAdapter extends AdAdapterManager implements MRAIDViewListener, MRAIDNativeFeatureListener, KyInstalListener {
    private FixedPopupWindow fixedPopupWindow = null;
    private AlertDialog instlDialog = null;
    private int instlWidth;
    private int instlHeight;
    private int currentRotation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    private InstlView instlView;

    private KyInstalListener kyViewListener = null;

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

        bitmapPath = bundle.getString("bitmapPath");
        kyViewListener = (KyInstalListener) bundle.getSerializable("interface");
        adsBean = kyViewListener.getAdsBean();
        createInstlDialog(context, adsBean);
    }

    private void createInstlDialog(Context context, final AdsBean adsBean) {
        try {
            int width_tmp = 600, height_tmp = 500;
            if (null != instlDialog && instlDialog.isShowing())
                return;
            if (adsBean.getAdType() == ConstantValues.HTML) {
                if (null != adsBean.getXhtml()
                        && adsBean.getXhtml().length() > 0) {
                    height_tmp = adsBean.getAdHeight();
                    width_tmp = adsBean.getAdWidth();
                }
            } else {
                // 只获得图片尺寸
                if ( !AdViewUtils.bitmapOnLine ) { //wilder 2019
                    if (adsBean.getAdType() == ConstantValues.MIXED) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(bitmapPath, opts);
                        height_tmp = opts.outHeight;
                        width_tmp = opts.outWidth;
                    }
                }else {
                    height_tmp = adsBean.getAdHeight();
                    width_tmp = adsBean.getAdWidth();
                }
            }
            HashMap<String, Integer> sizeMap = KyAdBaseView.getLayoutSize(context, adsBean,
                    width_tmp, height_tmp);
            instlView = new InstlView(context, sizeMap, adsBean.getAdType(), this, this);
            instlView.setInstlViewListener(this);
            instlView.setContent(adsBean, bitmapPath);

            instlWidth = sizeMap.get(ConstantValues.INSTLWIDTH);
            instlHeight = sizeMap.get(ConstantValues.INSTLHEIGHT);
            adsBean.setRealAdHeight(sizeMap.get(ConstantValues.INSTLHEIGHT));
            adsBean.setRealAdWidth(sizeMap.get(ConstantValues.INSTLWIDTH));

            final WebView webView = instlView.getWebView();
            if (null != webView) {
                if (null != kyViewListener)
                    kyViewListener.setClickMotion(instlView.getMraidView(), null);
//                setClickMotion(instlView.getMraidView(), adsBean, null);
            }
            switch (adsBean.getAdType()) {
                case ConstantValues.MIXED:
                    break;
                case ConstantValues.HTML:
                    if (null != adsBean.getXhtml()
                            && adsBean.getXhtml().length() > 0) {
                    }
                    break;
                default:
                    if (null != bitmapPath) {
                        //wilder 2019 , bitmap mode should be loaded in showPopupWindows(), see below, else may cause webview not be shown
                        //KyAdBaseView.loadWebContentURL(instlView.getWebView(),bitmapPath, adsBean.getAdLink());
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean showPopupWindows(final Activity ctx) {
        try {
            final Activity activity = null == ctx ? (Activity) context : ctx;
            if (null == fixedPopupWindow || !fixedPopupWindow.isShowing()) {
                fixedPopupWindow = new FixedPopupWindow(instlView, adsBean.getRealAdWidth(), adsBean.getRealAdHeight());
                fixedPopupWindow.showAtLocation((activity).getWindow().getDecorView(), Gravity.CENTER);
                if (fixedPopupWindow.isShowing()) {
                    if (null != kyViewListener)
                        kyViewListener.onDisplay(null,false);
                    currentRotation = activity
                            .getRequestedOrientation();
                    switch (activity.getWindow().getWindowManager()
                            .getDefaultDisplay().getRotation()) {
                        case Surface.ROTATION_0:
                            activity
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            break;
                        case Surface.ROTATION_90:
                            activity
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            break;
                        case Surface.ROTATION_180:
                            activity
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                            break;
                        case Surface.ROTATION_270:
                            activity
                                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                            break;
                    }
                }
                fixedPopupWindow.setOnDismissListener(new FixedPopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        (activity).setRequestedOrientation(currentRotation);
                        if (null != kyViewListener)
                            kyViewListener.onCloseBtnClicked();
                        try {
                            if (null != instlView) {
                                if (null != instlView.getWebView()) {
                                    //修复了WebView.destroy() called while still attached!的bug
                                    ViewGroup vp = (ViewGroup) instlView.getParent();
                                    if (vp != null) {
                                        vp.removeView(instlView);
                                        instlView.getWebView().loadUrl("about:blank");
                                    }
                                    instlView.getWebView().removeAllViews();
                                    instlView.getWebView().destroy();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adsBean.getAdType() == ConstantValues.HTML) {
                            WebView webView = instlView.getWebView();
                            if (null != webView) {
                                if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                                    instlView.getWebView().loadDataWithBaseURL(ConstantValues.WEBVIEW_BASEURL,
                                                        adsBean.getXhtml(),
                                            "text/html", "UTF-8", null);
                                } else
                                    instlView.getWebView().loadUrl(adsBean.getXhtml());
                            } else {
                                AdViewUtils.logInfo("#######  webview is null error ########");
                            }

                        }else if (adsBean.getAdType() != ConstantValues.MIXED){
                            //wilder 2019
                            if (AdViewUtils.bitmapOnLine) {
                                KyAdBaseView.loadWebContentURL(instlView.getWebView(),bitmapPath, adsBean.getAdLink());
                            }else {
                                KyAdBaseView.loadWebContentLocal(instlView.getWebView(), bitmapPath, adsBean.getAdLink(), instlWidth, instlHeight);
                            }
                        }
                    }
                }, 0);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (null != fixedPopupWindow)
                fixedPopupWindow.dismiss();
            return false;
        }

    }

    private boolean showDialog(Activity context) {
        final Context ctx = null == context ? this.context : context;
        try {
            if (null == instlDialog || !instlDialog.isShowing()) {

                instlDialog = new AlertDialog.Builder(ctx).create();
                instlDialog.setCancelable(false);
                instlDialog.setCanceledOnTouchOutside(false);
                // 关闭插屏之后解除横竖屏限制
                instlDialog
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // TODO Auto-generated method stub
                                ((Activity) ctx)
                                        .setRequestedOrientation(currentRotation);
                                if (null != kyViewListener)
                                    kyViewListener.onCloseBtnClicked();
//                                if (null != instlAdListener)
//                                    instlAdListener
//                                            .onAdClose(AdInstlBIDView2.this);
//                                if (null != onAdInstlListener) {
//                                    onAdInstlListener
//                                            .onAdClosedAd(AdInstlBIDView2.this);
//                                    onAdInstlListener = null;
//                                }
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
                        if (null != kyViewListener)
                            kyViewListener.onDisplay(null, false);
//                        reportImpression(cacheAdsBean, retAdBean, applyAdBean, true);
//                        if (null != onAdInstlListener)
//                            onAdInstlListener
//                                    .onAdDisplayed(AdInstlBIDView2.this);
                        currentRotation = ((Activity) ctx)
                                .getRequestedOrientation();
                        switch (((Activity) ctx).getWindow().getWindowManager()
                                .getDefaultDisplay().getRotation()) {
                            case Surface.ROTATION_0:
                                ((Activity) ctx)
                                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                break;
                            case Surface.ROTATION_90:
                                ((Activity) ctx)
                                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                break;
                            case Surface.ROTATION_180:
                                ((Activity) ctx)
                                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                                break;
                            case Surface.ROTATION_270:
                                ((Activity) ctx)
                                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
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
                        if (adsBean.getAdType() == ConstantValues.HTML) {
                            final WebView webView = instlView.getWebView();
                            if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                                webView.loadDataWithBaseURL(ConstantValues.WEBVIEW_BASEURL, adsBean.getXhtml(), "text/html", "UTF-8", null);
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

    /**
     * 展示插屏广告
     *
     * @param activity
     * @return 展示成功返回true 否则false
     */
    @Override
    public boolean showInstl(final Activity activity) {
        try {
            int notifyType = 0;
            if (null != adsBean) {
                if (System.currentTimeMillis() - adsBean.getDataTime() > ConstantValues.AD_EXPIRE_TIME) {
                    return false;//超时 失败
                }
            } else {
                return false;
            }
            if (null != kyViewListener)
                notifyType = kyViewListener.getDisplayMode();
            switch (notifyType) {
                case AdInstlBIDView.DISPLAYMODE_DEFAULT:
                case AdInstlBIDView.DISPLAYMODE_POPUPWINDOWS:
//                return showDialog(activity);
                    return showPopupWindows(activity);
                case AdInstlBIDView.DISPLAYMODE_DIALOG:
                    return showDialog(activity);

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
                        instlView.getWebView().loadDataWithBaseURL(ConstantValues.WEBVIEW_BASEURL, adsBean.getXhtml(), "text/html", "UTF-8", null);
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
     * 手动调用 关闭插屏
     */
    @Override
    public void closeInstl() {
        try {
            if (null != instlDialog && instlDialog.isShowing())
                instlDialog.cancel();
            if (null != fixedPopupWindow && fixedPopupWindow.isShowing())
                fixedPopupWindow.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mraidNativeFeatureDownload(String url) {
        if (null != kyViewListener)
            kyViewListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureCallTel(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL); // android.intent.action.DIAL
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void mraidNativeFeatureOpenDeeplink(String url) {
        if (url.startsWith("mraid")) {
            try {
                url = URLDecoder.decode(url.replace("mraid://openDeeplink?url=", ""), "UTF-8");
                adsBean.setDeeplink(url);
                if (null != kyViewListener)
                    kyViewListener.checkClick(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mraidNativeFeatureOpenBrowser(String url) {
        if (null != kyViewListener)
            kyViewListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureSendSms(String url) {
        AdViewUtils.sendSms(context, url);
    }

    @Override
    public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {
    }

    @Override
    public void mraidNativeFeatureStorePicture(String url) {
    }

    /*
    ----------------------------------------------------------------
     */
    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
    }

    @Override
    public void mraidViewExpand(MRAIDView mraidView) {
    }

    @Override
    public void mraidViewClose(MRAIDView mraidView) {
    }

    @Override
    public boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX, int offsetY) {
        return false;
    }

    @Override
    public void onShouldOverride(String url) {
        // 至少触摸过才可以点击跳转
        if (adsBean.getTouchStatus() > MRAIDView.ACTION_DEFAULT) {
            if (null != kyViewListener)
                kyViewListener.checkClick(url);
//            clickCheck(url, adsBean, applyAdBean, retAdBean);
//                }
//            }
        }
    }

    @Override
    public WebResourceResponse onShouldIntercept(String url) {
        return kyViewListener.shouldInterceptRequest(url);
//        return KyAdBaseView.shouldInterceptRequest(url, adsBean, applyAdBean);
    }

    @Override
    public void loadDataError(int errorType) {
        if (null != kyViewListener)
            kyViewListener.onAdFailed(null,"Custom://" + errorType, false);
    }

    @Override
    public void onCloseBtnClicked() {
        if (null != instlDialog && instlDialog.isShowing()) {
            instlDialog.cancel();
//            isClosed = true;
        }
        if (null != fixedPopupWindow && fixedPopupWindow.isShowing()) {
            fixedPopupWindow.dismiss();
//            isClosed = true;
        }
        if (null != kyViewListener)
            kyViewListener.onCloseBtnClicked();
    }

    @Override
    public void onViewClicked(MotionEvent e, AgDataBean agDataBean,String url, float downX, float downY) {
        if (null != kyViewListener)
            kyViewListener.onViewClicked(e, agDataBean,url, downX, downY);
    }

    @Override
    public boolean isClickableConfirm() {
        return false;
    }

    @Override
    public void setClickMotion(MRAIDView view, Rect touchRect) {

    }

    @Override
    public WebResourceResponse shouldInterceptRequest(String url) {
        return null;
    }

    @Override
    public boolean needConfirmDialog() {
        return false;
    }

    @Override
    public void checkClick(String url) {

    }

    @Override
    public void onReady(AgDataBean agDataBean,boolean force) {

    }

    @Override
    public void onReceived(AgDataBean agDataBean,boolean force) {

    }

    @Override
    public void onAdFailed(AgDataBean agDataBean,String error, boolean force) {

    }

    @Override
    public void onDisplay(AgDataBean agDataBean,boolean force) {

    }

    @Override
    public boolean getCloseble() {
        if (null != kyViewListener)
            return kyViewListener.getCloseble();
        return false;
    }


    @Override
    public String getAdLogo() {
        if (null != kyViewListener)
            return kyViewListener.getAdLogo();
        return null;
    }

    @Override
    public String getAdIcon() {
        if (null != kyViewListener)
            return kyViewListener.getAdIcon();
        return null;
    }


    @Override
    public AdsBean getAdsBean() {
        return null;
    }

    @Override
    public void rotatedAd(Message msg) {

    }

    @Override
    public int getDisplayMode() {
        return 0;
    }

    @Override
    public void onVisiblityChange(int visible) {

    }
}
