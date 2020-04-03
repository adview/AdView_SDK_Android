package com.kuaiyou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.AdVGListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;

/*该activity 用于展示没有context的情形下的即mode = 2模式下instl插屏的情况*/
public class AdActivity extends Activity {
   // private final static int ACTION_ID_INSTALLED = 6;
//    private Uri uri = null;
//    private String[] instlReport;
//    private String gdtExtraUrls;
//    private String clickId_gdt;
    private String mode;
    private String bmpPath;
    private int instWidth;
    private int instHeight;

//    private int route = 1;
    private AdsBean adsBean;
    private InstlView instlView;
    public static AdActivity instance = null;

    public static AdActivity getInstance() {
        //instance.context = context;
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        //setTheme(android.R.style.Theme_Translucent_NoTitleBar);
           Bundle bundle = getIntent().getExtras();
           mode = bundle.getString("mode");
           bmpPath = bundle.getString("bmpPath");
           instWidth = bundle.getInt("insWidth");
           instHeight = bundle.getInt("insHeight");
           adsBean = (AdsBean)bundle.getSerializable("data");
           instlView = (InstlView)AdAdapterManager.getInstl();

           if (instlView == null)
               return;
           //setContentView(instlView);
//            uri = bundle.getParcelable("path");
//            instlReport = bundle.getStringArray("install_report");
//            gdtExtraUrls=bundle.getString("gdt_conversion_link");
//            clickId_gdt=bundle.getString("click_id_gdt");
//            Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uri);
//            installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
//            installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
//
//            startActivityForResult(installIntent, 1);
            FrameLayout.LayoutParams frameLayout = new FrameLayout.LayoutParams(-2, -2);
            frameLayout.gravity = Gravity.CENTER;
            try {
                getWindow().addContentView(instlView, frameLayout);
                //setContentView(instlView, frameLayout);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_HTML) {
                            try {
                                final WebView webView = instlView.getWebView();
                                if (!adsBean.getXhtml().startsWith("http://") && !adsBean.getXhtml().startsWith("https://")) {
                                    if (null == AdViewUtils.loadWebContentExt(webView, adsBean.getXhtml()))
                                        finish();
                                } else {
                                    webView.loadUrl(adsBean.getXhtml());
                                }
                            } catch (Exception e) {
                                //AdViewUtils.logInfo();
                                e.printStackTrace();
                                finish();
                            }

                        }else if (adsBean.getAdType() != ConstantValues.RESP_ADTYPE_MIXED) {
                            //wilder 2019
                            AdViewUtils.logInfo("### AdActivity(): in non html content ###");
                            if (AdViewUtils.bitmapOnLine) {
                                AdViewUtils.loadWebImageURL(instlView.getWebView(), bmpPath, adsBean.getAdLink());
                            } else {
                                AdViewUtils.loadWebImageLocal(instlView.getWebView(), bmpPath, adsBean.getAdLink(), instWidth, instHeight);
                            }
                        }
                    }
                }, 0);
            }catch (Exception e) {
                e.printStackTrace();
                this.finish();
            }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.i("AdActivity", "===== !! onBackPressed() !! ======");
        //onBack();
        if (instlView != null) {
            AdVGListener listener = instlView.getInstlViewListener();
            if (null != listener) {
                listener.onCloseBtnClicked();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*        if (resultCode == RESULT_OK) {
            // 汇报成功安装
            AdViewUtils.reportEffect(instlReport);
            if (!TextUtils.isEmpty(gdtExtraUrls))
                AdViewUtils.reportEffect(new String[]{
                        AdViewUtils.getGdtActionLink(gdtExtraUrls, clickId_gdt, ACTION_ID_INSTALLED)});

//
            //Toast.makeText(this, "应用安装成功", 0).show();
            AdViewUtils.logInfo("Install Done");
//            }
        } else {
            // 未能安装 do nothing
            AdViewUtils.logInfo("Install Cancel,RESULT_OK=" + resultCode);
        }*/
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //fix when destory still attached
//        try {
//            if (null != instlView) {
//                if (null != instlView.getWebView()) {
//                    ViewGroup vp = (ViewGroup) instlView.getParent();
//                    vp.removeView(instlView);
//                    instlView.getWebView().loadUrl("about:blank");
//                    instlView.getWebView().removeAllViews();
//                    instlView.getWebView().destroy();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        instance = null;
    }

//    private void adjustUISize() {
//        //move to  part
//        rootLayout = (LinearLayout)findViewById(R.id.mainLinear);
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)rootLayout.getLayoutParams();
//        Point sz = new Point();
//        getWindowManager().getDefaultDisplay().getSize(sz);
//
//        params.height = sz.y * 1/3;
//        params.width = sz.x * 3/4;
//        params.topMargin += (sz.y - params.height)/2;
//        params.leftMargin += (sz.x - params.width)/2;
//        rootLayout.setLayoutParams(params);
//        //ll.setVisibility(View.INVISIBLE);
//    }

}
