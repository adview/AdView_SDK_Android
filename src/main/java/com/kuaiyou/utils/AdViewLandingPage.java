package com.kuaiyou.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kuaiyou.interfaces.DownloadConfirmInterface;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * WebView 网页类广告，落地页
 */
public class AdViewLandingPage extends Activity {
    // webview 控件id
    public static final int BTN_TO_PREV = 1;
    public static final int BTN_TO_NEXT = 2;
    public static final int BTN_DO_REFRESH = 3;
    public static final int BTN_DO_SHARE = 4;
    public static final int BTN_DO_CLOSE = 5;
    public static final int BTN_DO_STOP = 6;
    public static final int TOOLBAR_ID = 88;

    WebView adWebView;
    WebViewProgressBar adWebViewProgressBar;

    String adLink;
    boolean isLoading = false;

    ImageView btnToPrev;
    ImageView btnToNext;
    ImageView btnDoRefresh;

    ArrayList<BtnOnTouchListener> touchList;
    int screenWidth = -1;

    Handler setWedViewScaleHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            boolean canZoomOut = AdViewLandingPage.this.adWebView.zoomOut();
            while (canZoomOut) {
                canZoomOut = AdViewLandingPage.this.adWebView.zoomOut();
            }
            super.handleMessage(msg);
        }
    };

    @SuppressLint("JavascriptInterface")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        double density = AdViewUtils.getDensity(this);
        int toolbar_height = AdViewUtils.convertToScreenPixels(48, density);
        int progressbar_height = AdViewUtils.convertToScreenPixels(5, density);

        FrameLayout frameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams webFrameParams = new FrameLayout.LayoutParams(
                -1, -1);
        FrameLayout.LayoutParams porFrameParams = new FrameLayout.LayoutParams(
                -1, progressbar_height);
        adWebViewProgressBar = new WebViewProgressBar(this);
        // adWebViewProgressBar.setVisibility(8);

        RelativeLayout relativeLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams webViweLayoutParams = new RelativeLayout.LayoutParams(
                -1, -1);
        webViweLayoutParams.addRule(2, TOOLBAR_ID);
        RelativeLayout.LayoutParams barLayoutParams = new RelativeLayout.LayoutParams(
                -1, toolbar_height);
        barLayoutParams.addRule(12);

        adWebView = new WebView(this);
        adWebView.setVerticalScrollBarEnabled(false);

        LinearLayout barLayout = new LinearLayout(this) {
            protected void onSizeChanged(int w, int h, int ow, int oh) {
                try {
                    BitmapDrawable drawable = getBarBackground(h);
                    if (null != drawable)
                        setBackground(drawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        barLayout.setId(TOOLBAR_ID);
        barLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams btnImgLayoutParams = new LinearLayout.LayoutParams(
                -2, toolbar_height, 1.0F);
        btnImgLayoutParams.gravity = 16;

        setImgBtn(barLayout, btnImgLayoutParams);

        relativeLayout.addView(barLayout, barLayoutParams);
        relativeLayout.addView(this.adWebView, webViweLayoutParams);

        frameLayout.addView(relativeLayout, webFrameParams);
        frameLayout.addView(this.adWebViewProgressBar, porFrameParams);
        ViewGroup.LayoutParams viewGrLayoutParams = new ViewGroup.LayoutParams(
                -1, -1);
        addContentView(frameLayout, viewGrLayoutParams);

        adWebView.setWebViewClient(new webViewClient());

        adLink = getIntent().getStringExtra("adview_url");
        if (checkFilter(adLink) != -1) {
            finish();
            return;
        }

        adWebView.setDownloadListener(new AdviewDownloadListener(getIntent().getBooleanExtra("isVideo", false)));
        adWebView.setWebChromeClient(new ProWebChromeClient());

        WebSettings webSettings = this.adWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
//        webSettings.setUseWideViewPort(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webSettings.setLoadWithOverviewMode(true);

        // webSettings.setPluginsEnabled(true);
        adWebView.setClickable(true);
        adWebView.addJavascriptInterface(new AdViewJsObj(), "local_obj");
        webSettings.setSupportZoom(true);
//        String deepLink = getIntent().getStringExtra("deep_link");
        try {
//            if (AdViewUtils.openDeepLink(AdViewLandingPage.this, deepLink)) {
//                //使用deeplink打开，此时页面空白
//                return;
//            }
            if (!TextUtils.isEmpty(this.adLink))
                adWebView.loadUrl(this.adLink);
            else if (!TextUtils.isEmpty(getIntent().getStringExtra("browser_fallback_url")))
                adWebView.loadUrl(getIntent().getStringExtra("browser_fallback_url"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BitmapDrawable getBarBackground(int height) {
        try {
            Bitmap bm = AdViewUtils.getImageFromAssetsFile("webview_bar_bg.png");
            BitmapDrawable barbgBitmapDrawable = new BitmapDrawable(getResources(), bm);
            Bitmap barBg = barbgBitmapDrawable.getBitmap();
            Matrix barBgMatrix = new Matrix();
            barBgMatrix.setScale(1.0F, height * 1.0F / barBg.getHeight());
            barBg = Bitmap.createBitmap(barBg, 0, 0, barBg.getWidth(),
                    barBg.getHeight(), barBgMatrix, false);
            barbgBitmapDrawable = new BitmapDrawable(getResources(), barBg);
//            barbgBitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT,
//                    Shader.TileMode.REPEAT);
            barbgBitmapDrawable.setDither(true);
//            if (barBgStream != null) {
//                barBgStream.close();
//            }
            return barbgBitmapDrawable;
        } catch (Exception e) {
            AdViewUtils.logError("", e);
        }

        return null;
    }

    private void setImgBtn(LinearLayout linearLayout,LinearLayout.LayoutParams btnImgLayoutParams) {

        try {
            BitmapDrawable btnToPrevImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_back.png"));
            BitmapDrawable btnToPrevHoverImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_back_hover.png"));
            BitmapDrawable btnToPrevGreyImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_back_grey.png"));

            BitmapDrawable btnToNextImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_next.png"));
            BitmapDrawable btnToNextHoverImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_next_hover.png"));
            BitmapDrawable btnToNextGreyImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_next_grey.png"));

            BitmapDrawable btnDoRefreshImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_refresh.png"));
            BitmapDrawable btnDoRefreshHoverImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_refresh_hover.png"));

            BitmapDrawable btnDoStopImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_pause.png"));
            BitmapDrawable btnDoStopHoverImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_pause_hover.png"));

            BitmapDrawable btnDoShareImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_share.png"));
            BitmapDrawable btnDoShareHoverImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_share_hover.png"));

            BitmapDrawable btnDoCloseImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_close.png"));
            BitmapDrawable btnDoCloseHoverImg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_close_hover.png"));
      /*  BitmapDrawable btnToPrevImg = new BitmapDrawable(getClass().getResourceAsStream(ConstantValues.WEBVIEW_IMAGE_BASE_PATH
                                + "webview_bar_back.png"));
        BitmapDrawable btnDoCloseHoverImg = new BitmapDrawable(getClass().getResourceAsStream(ConstantValues.WEBVIEW_IMAGE_BASE_PATH
                                + "webview_bar_close_hover.png"));*/

            btnToPrev = new ImageView(this);
            btnToPrev.setId(BTN_TO_PREV);
            btnToPrev.setImageDrawable(btnToPrevGreyImg);
            btnToPrev
                    .setTag(new BtnOnTouchListener(btnToPrevHoverImg, btnToPrevImg));

            btnToNext = new ImageView(this);
            btnToNext.setId(BTN_TO_NEXT);
            btnToNext.setImageDrawable(btnToNextGreyImg);
            btnToNext
                    .setTag(new BtnOnTouchListener(btnToNextHoverImg, btnToNextImg));

            this.btnDoRefresh = new ImageView(this);
            this.btnDoRefresh.setId(BTN_DO_REFRESH);
            this.btnDoRefresh.setImageDrawable(btnDoStopImg);
            touchList = new ArrayList<BtnOnTouchListener>();
            touchList.add(new BtnOnTouchListener(btnDoRefreshHoverImg,
                    btnDoRefreshImg));
            touchList.add(new BtnOnTouchListener(btnDoStopHoverImg, btnDoStopImg));
            btnDoRefresh.setTag(touchList);

            ImageView btnDoShare = new ImageView(this);
            btnDoShare.setId(BTN_DO_SHARE);
            btnDoShare.setImageDrawable(btnDoShareImg);
            btnDoShare.setOnTouchListener(new BtnOnTouchListener(btnDoShareHoverImg, btnDoShareImg));
            ImageView btnDoClose = new ImageView(this);
            btnDoClose.setId(BTN_DO_CLOSE);
            btnDoClose.setImageDrawable(btnDoCloseImg);
            btnDoClose.setOnTouchListener(new BtnOnTouchListener(btnDoCloseHoverImg, btnDoCloseImg));

            linearLayout.addView(btnToPrev, btnImgLayoutParams);
            linearLayout.addView(btnToNext, btnImgLayoutParams);
            linearLayout.addView(btnDoRefresh, btnImgLayoutParams);
            linearLayout.addView(btnDoShare, btnImgLayoutParams);
            linearLayout.addView(btnDoClose, btnImgLayoutParams);
        }catch ( Exception e) {
            e.printStackTrace();
        }
    }

    //用于判断从服务器下发的act的url是否是特殊schem
    private int checkFilter(String url) {
        try {
            AdViewUtils.logInfo("[AdViewLandingPage] checkFilter(): url = " + url);
            if (url.contains("wtai://wp/mc;"))
                url = url.replace("wtai://wp/ap;", "tel:");
            if (url.contains("wtai://wp/mc;"))
                url = url.replace("wtai://wp/mc;", "tel:");
            if (url.contains("tel:")) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse(url));
                    startActivity(callIntent);
                } catch (Exception e) {
                    AdViewUtils.logError("", e);
                }

                return 0;
            }
            if (url.contains("market://")) { //这里如果是market，交由系统自动打开store

                Uri uri = Uri.parse(url);
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);

                List<ResolveInfo> packList = getPackageManager().queryIntentActivities(marketIntent, 0);
                if (packList.size() > 0)
                    startActivity(marketIntent);
                else {
                    //Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                    AdViewUtils.logInfo("!!!![AdViewLandingPage]checkFilter(): can't start " + url + ", no market app !!!!");
                }
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void onWebViewLoad() {
        isLoading = true;
        btnDoRefresh.setImageDrawable(new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_pause.png")));
        //不要使用getResourceAsStream()，会很影响效能
//        btnDoRefresh.setImageDrawable(new BitmapDrawable(
//        getClass().getResourceAsStream(ConstantValues.WEBVIEW_IMAGE_BASE_PATH + "webview_bar_pause.png")));
        btnDoRefresh.setId(BTN_DO_STOP);
        btnDoRefresh.setOnTouchListener((View.OnTouchListener) ((ArrayList<?>) btnDoRefresh.getTag()).get(1));
    }

    private void loadComplete() {
        isLoading = false;
        if (adWebView.canGoBack()) {
            btnToPrev.setImageDrawable(new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_back.png")));
            btnToPrev.setOnTouchListener((View.OnTouchListener) this.btnToPrev.getTag());
        } else {
            btnToPrev.setImageDrawable(new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_back_grey.png")));
            btnToPrev.setOnTouchListener(null);
        }
        if (adWebView.canGoForward()) {
            btnToNext.setImageDrawable(new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_next.png")));
            btnToNext.setOnTouchListener((View.OnTouchListener) this.btnToNext.getTag());
        } else {
            btnToNext.setImageDrawable(new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_next_grey.png")));
            btnToNext.setOnTouchListener(null);
        }

        btnDoRefresh.setImageDrawable(new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("webview_bar_refresh.png")));
        btnDoRefresh.setId(BTN_DO_REFRESH);
        btnDoRefresh.setOnTouchListener((View.OnTouchListener) ((ArrayList<?>) this.btnDoRefresh.getTag()).get(0));
    }

    private void shareConent() {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(this.adLink));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onPause() {
        super.onPause();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager lb = LocalBroadcastManager.getInstance(this);
            Intent intent = new Intent(ConstantValues.ADWEBVIEW_BROADCAST_CLOSED_STATUS);
            lb.sendBroadcast(intent);
            if (null != adWebView) {
                adWebView.stopLoading();
                //修复了WebView.destroy() called while still attached!的bug
                ViewGroup vp = (ViewGroup) adWebView.getParent();
                vp.removeView(adWebView);
                adWebView.removeAllViews();
                adWebView.loadUrl("about:blank");
                adWebView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class AdViewJsObj {
        AdViewJsObj() {
        }

        public void setWinth(int width) {
            AdViewLandingPage.this.screenWidth = (AdViewLandingPage.this.screenWidth == -1 ? AdViewLandingPage.this
                    .getWindowManager().getDefaultDisplay().getWidth()
                    : AdViewLandingPage.this.screenWidth);

            Message msg = new Message();
            msg.arg1 = (int) (AdViewLandingPage.this.screenWidth * 1.0F / width * 100.0F);
            AdViewUtils.logInfo("sedWinth, " + (width - 100));
        }
    }

    class AdviewDownloadListener implements DownloadListener {
        private boolean isVideo = false;

        public AdviewDownloadListener(boolean isVideo) {
            this.isVideo = isVideo;
        }

        @Override
        public void onDownloadStart(final String url, String userAgent,
                                    String contentDisposition, String mimetype, long contentLength) {
            try {
                if (AdViewUtils.getNetworkType(AdViewLandingPage.this).equals("WIFI")) {
                    AdViewLandingPage.this.finish();
                    Intent updateIntent = getIntent().setClass(AdViewLandingPage.this, DownloadService.class);
                    updateIntent.putExtra("adview_url", url);
                    AdViewLandingPage.this.startService(updateIntent);

                } else {
                    AdViewUtils.trafficConfirmDialog(AdViewLandingPage.this, new DownloadConfirmInterface() {
                        @Override
                        public void confirmDownload() {
                            try {
                                AdViewLandingPage.this.finish();
                                Intent updateIntent = getIntent().setClass(AdViewLandingPage.this, DownloadService.class);
                                updateIntent.putExtra("adview_url", url);
                                AdViewLandingPage.this.startService(updateIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void cancelDownload() {
                            if (isVideo)
                                AdViewLandingPage.this.finish();
                        }

                        @Override
                        public void error() {

                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                AdViewLandingPage.this.finish();
            }
        }
    }

    class BtnOnTouchListener implements View.OnTouchListener {
        Drawable downImg;
        Drawable upImg;

        public BtnOnTouchListener(Drawable downImg, Drawable upImg) {
            this.downImg = downImg;
            this.upImg = upImg;
        }

        public boolean onTouch(View view, MotionEvent event) {
            try {
                if (event.getAction() == 0) {
                    ((ImageView) view).setImageDrawable(this.downImg);
                } else if (event.getAction() == 1) {
                    ((ImageView) view).setImageDrawable(this.upImg);
                    if (adWebView != null) {
                        int viewId = view.getId();
                        if (viewId == BTN_TO_PREV) {
                            AdViewLandingPage.this.adWebView.goBack();
                        } else if (viewId == BTN_TO_NEXT) {
                            AdViewLandingPage.this.adWebView.goForward();
                        } else if (viewId == BTN_DO_SHARE) {
                            AdViewLandingPage.this.shareConent();
                        } else if (viewId == BTN_DO_REFRESH) {
                            AdViewLandingPage.this.adWebView.reload();
                        } else if (viewId == BTN_DO_STOP) {
                            AdViewLandingPage.this.adWebView.stopLoading();
                            AdViewLandingPage.this.loadComplete();
                        } else if (viewId == BTN_DO_CLOSE) {
                            setResult(RESULT_OK, null);
                            AdViewLandingPage.this.finish();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    class ProWebChromeClient extends WebChromeClient {
        ProWebChromeClient() {
        }

        public void onProgressChanged(WebView view, int newProgress) {
            try {
                if (newProgress > 0) {
                    AdViewLandingPage.this.adWebViewProgressBar
                            .setProgress(newProgress);

                    if (newProgress < 100) {
                        if (!AdViewLandingPage.this.isLoading)
                            AdViewLandingPage.this.onWebViewLoad();
                    } else {
                        AdViewLandingPage.this.loadComplete();
                    }
                }
                super.onProgressChanged(view, newProgress);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }


    class webViewClient extends WebViewClient {
        webViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Log.i("override", url + "");
                return AdViewUtils.openDeepLink(AdViewLandingPage.this, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

    }

    class WebViewProgressBar extends View {
        int width;
        int height;
        Context context;
        Bitmap sourBg;
        Bitmap barBg;
        boolean isInit = false;

        int currProgress = 0;

        Handler closeViewHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (WebViewProgressBar.this.currProgress == 100) {
                    WebViewProgressBar.this.setVisibility(View.GONE);
                    WebViewProgressBar.this.currProgress = 0;
                }
                super.handleMessage(msg);
            }
        };

        public WebViewProgressBar(Context ctx) {
            super(ctx);
            context = ctx;
        }

        private void initialize() {
            try {
                this.width = getWidth();
                this.height = getHeight();

                this.sourBg = new BitmapDrawable(getResources(), AdViewUtils.getImageFromAssetsFile("progressbarbg.png")).getBitmap();
//                this.sourBg = new BitmapDrawable(getClass().getResourceAsStream(ConstantValues.WEBVIEW_IMAGE_BASE_PATH
//                                + URLEncoder.encode("progressbar") + "bg.png")).getBitmap();

                this.sourBg = conBitmapSize(this.sourBg, this.width, this.height);

                this.isInit = true;

                if (this.currProgress > 0) {
                    setProgress(this.currProgress);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            initialize();
            super.onSizeChanged(w, h, oldw, oldh);
        }

        private Bitmap conBitmapSize(Bitmap bitmap, int width, int height) {
            Matrix matrix = new Matrix();
            matrix.setScale(1.0F * width / bitmap.getWidth(), 1.0F * height
                    / bitmap.getHeight());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);

            return bitmap;
        }

        private Bitmap cutBitmap(Bitmap bitmap, int width, int height) {
            Bitmap cutBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            return cutBitmap;
        }

        private Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            int color = -12434878;
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF rectF = new RectF(rect);
            float roundPx = pixels;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }

        //
        protected void onDraw(Canvas canvas) {
            if ((this.barBg != null) && (this.isInit)) {
                canvas.drawBitmap(this.barBg, 0.0F, 0.0F, null);
            }
            super.onDraw(canvas);
        }

        //
        public void setProgress(int progress) {
            this.currProgress = progress;

            if (!this.isInit) {
                AdViewUtils.logInfo("setProgress, not init");
                setVisibility(View.VISIBLE);
                return;
            }
            int vis = getVisibility();

            if (vis == 8) {
                setVisibility(View.VISIBLE);
            }

            int ferWidth = (int) (progress / 100.0F * this.width);

            if (this.sourBg != null) {
                this.barBg = cutBitmap(this.sourBg, ferWidth, this.height);
                this.barBg = toRoundCorner(this.barBg, 5);
                invalidate();
            } else {
                AdViewUtils.logInfo("setProgress, bg is null");
                setVisibility(View.GONE);
            }
            if (progress >= 100)
                this.closeViewHandler.sendMessageDelayed(
                        this.closeViewHandler.obtainMessage(1000), 0L);
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
