package com.kuaiyou.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.adbid.AdVideoBIDView;
import com.kuaiyou.adbid.video.adapter.AdBIDVideoAdapter;
import com.kuaiyou.interfaces.DownloadStatusInterface;
import com.kuaiyou.interfaces.KyVideoListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ExtensionBean;
import com.kuaiyou.obj.VideoBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.Assets;
import com.kuaiyou.utils.ClientReportRunnable;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.CountDownView;
import com.kuaiyou.utils.CustomWebview;
import com.kuaiyou.utils.DefaultMediaPicker;
import com.kuaiyou.utils.DownloadRunnable;
import com.kuaiyou.utils.VideoFinalPage;
import com.kuaiyou.utils.VideoLableView;
import com.kuaiyou.video.vast.VASTPlayer;
import com.kuaiyou.video.vast.VASTPlayerListener;
import com.kuaiyou.video.vast.model.CompanionClicks;
import com.kuaiyou.video.vast.model.IconClicks;
import com.kuaiyou.video.vast.model.TRACKING_EVENTS_TYPE;
import com.kuaiyou.video.vast.model.VASTCompanionAd;
import com.kuaiyou.video.vast.model.VASTCreative;
import com.kuaiyou.video.vast.model.VASTIcon;
import com.kuaiyou.video.vast.model.VASTModel;
import com.kuaiyou.video.vast.model.VideoClicks;
import com.kuaiyou.video.vast.vpaid.EventConstants;
import com.kuaiyou.utils.AdViewLandingPage;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

@SuppressLint("SetJavaScriptEnabled")
public class AdVASTView extends RelativeLayout implements AdControllerInterface,
                            View.OnClickListener, CustomWebview.CustomInterface,
                            CustomWebview.CustomClickInterface,
                            KyVideoListener {

    // used for setting positions and sizes (all in pixels, not dpi)
    private DisplayMetrics displayMetrics;
    //private int contentViewTop;
    private Rect currentPosition;
    private int originalRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private float density = 0;

    private boolean autoCloseAble = false;
    private int videoOrientation = -1;
    private boolean trafficWarnEnable = true;
    private String bgColor = "#000000";//default is black
    // This is the contents of mraid.js. We keep it around in case we need to
    // inject it
    // into webViewPart2 (2nd part of 2-part expanded ad).
    //private Handler handler;
    //////////////////////////////////////////////
    private RelativeLayout mRootLayout;
    private ProgressBar mProgressBar;
    private CustomWebview contentWebView;
    private WebClientHandler handler;
    //wilder 2019 for vpaid
    private AdViewControllerVpaid  mACVpaidCtrl;
    private static String mAdParams;
    private boolean mIsWaitingForWebView;

    private AdsBean adsBean;
    private Bundle passBundle;

    public final static int STATUS_START_MESSAGE = 1;
    public final static int STATUS_END_MESSAGE = 2;
    public final static int STATUS_ERROR_MESSAGE = 3;
    public final static int STATUS_SKIP_MESSAGE = 4;
    public final static int STATUS_MUTE_MESSAGE = 5;
    public final static int STATUS_TIME_MESSAGE = 6;
    public final static int STATUS_TOTALTIME_MESSAGE = 7;
    public final static int STATUS_CLICK_MESSAGE = 8;
    public final static int STATUS_SIZE_CHANGE_MESSAGE = 9;
    public final static int STATUS_VISIBLE_CHANGE_MESSAGE = 10;
    public final static int STATUS_UNMUTE_MESSAGE = 11;
    public final static int STATUS_ICON_BANNER_MESSAGE = 12;
    public final static int STATUS_FINAL_PAGE_MESSAGE = 13;
    public final static int STATUS_PLAY_VIDEO_MESSAGE = 14; //add by wilder
    //vpaid events
    public final static int STATUS_VPAID_ADSTART_VIEW = 20;
    public final static int STATUS_VPAID_SKIPBUTTON_SHOW = 21;
    public final static int STATUS_VPAID_VOLUME_STATUS = 22;

    private final static int CLICKTRACKING_WRAPPER_EVENT = 1;
    private final static int ERROR_WRAPPER_EVENT = 2;
    private final static int IMPRESSION_WRAPPER_EVENT = 3;
    private final static int TRACKING_WRAPPER_EVENT = 4;

    private Timer mTrackingEventTimer;

    private static final int LT_POSITION = 1, RT_POSITION = 2, LB_POSITION = 3, RB_POSITION = 4, SKIP_POSITION = 5;

    private ArrayList<ArrayList<HashMap<TRACKING_EVENTS_TYPE, List<String>>>> mTrackingEventMap = new ArrayList<ArrayList<HashMap<TRACKING_EVENTS_TYPE, List<String>>>>();
    private ArrayList<ArrayList<VASTCompanionAd>> companionAdsList = new ArrayList<ArrayList<VASTCompanionAd>>();
    private ArrayList<ArrayList<ArrayList<VASTIcon>>> iconAdsList = new ArrayList<ArrayList<ArrayList<VASTIcon>>>();
    private ArrayList<ArrayList<VideoClicks>> videoClicks = new ArrayList<ArrayList<VideoClicks>>();
    private ArrayList<VASTModel> mVastModel = new ArrayList<VASTModel>();
    private ArrayList<VASTModel> wrapperModel = new ArrayList<VASTModel>();

    private boolean isSkippShown = false;
    private int adCount = 0, creativeCount = 0;
    private int mQuartile = 0;
    private boolean isPaused = false;

    private BitmapDrawable volumeON, volumeOFF;
    private int lastPauseVideoTime = 0;
    private int cornerSize;

    private boolean isSkipped = false;
    private boolean isWaittingDownload = false;
    private boolean isScaled = false;
    //private float density;
    private int screenWidth;
    private int screenHeight;
    //behaved view
    private int behavedWidth;
    private int behavedHeight;
    private int allBehavedCounts = 0;
    private int beHavedNum = 0;

    private boolean mIsProcessedImpressions = false;

    private String currentTotalTime = "0";
    private int currentVideoPlayTime = 0;
    private float currentVPAIDVolume = 0;     //wilder 2019 for vpaid

    private final static int WAITTIMEOUT = 20 * 1000;

    private static final long QUARTILE_TIMER_INTERVAL = 250;
    private static final int CLOSE_VIEW_ID = 10001;
    private static final int SKIP_TEXT_ID = 10002;
    private static final int COUNTDOWN_VIEW_ID = 10003;
    private static final int VOLUME_VIEW_ID = 10004;
    private static final int REPLAY_VIEW_ID = 10005;

    private static final int ICONBANNER_VIEW_ID = 10006;
    private static final int FINALPAGE_VIEW_ID = 10007;

    private static final String END_LABEL_TEXT = "跳过视频";

    private boolean isEmbed = false;
    private int holdOnTime;
    private boolean hasError;

    private int desiredWidth, desiredHeight;
//    private boolean isPortrait = false;
    private ArrayList<ExtensionBean> extensionBeanList;
    private Context mContext;
    private VASTPlayer mPlayer;

    private AdAdapterManager adVideoAdapterManager;
    private AdViewVideoInterface adAppInterface;

    public AdVASTView(Context ctx, int w, int h, boolean isEmbed, AdAdapterManager adm ) {
        super(ctx);
        this.isEmbed = isEmbed;
        this.mContext = ctx;

        screenWidth = w;
        screenHeight = h;

        this.adVideoAdapterManager = adm;
        adVideoAdapterManager.setVideoCallback(this);

        onCreate(w,h);
    }

    public AdVASTView(Context ctx) {
        super(ctx);
        this.mContext = ctx;
    }

    protected void onCreate(int w, int h) {

        displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        currentPosition = new Rect();

        if (mContext instanceof Activity) {
            originalRequestedOrientation = ((Activity) mContext).getRequestedOrientation();
        } else {
            originalRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
        //AdViewUtils.logInfo("originalRequestedOrientation " + getOrientationString(originalRequestedOrientation));
        handler = new WebClientHandler();
        createUiView();
        if (!isEmbed) {
            getScreenSize(false); //for fullscreen size used
            calcCornerSize();
        }
        createProgressBar();
        showProgressBar();
        // 在主线程中执行
        //handler = new Handler(Looper.getMainLooper());
    }

    public void setVideoAppListener(AdViewVideoInterface adInterface) {
        this.adAppInterface = adInterface;
    }

    public void setAdVideoAdapterManager(AdAdapterManager adpt) {
        this.adVideoAdapterManager = adpt;
    }

    /*(wilder 2019) make vast view in webview */
    public void startVastShow(VASTPlayer player) {
        if(player == null) {
            return;
        }
        this.mPlayer = player;
        this.mVastModel = player.getVastModel();
        this.wrapperModel = player.getWrapperModel();

        if (null != mVastModel && !mVastModel.isEmpty()) {
            extensionBeanList = new ArrayList<ExtensionBean>();
            for (int i = 0; i < mVastModel.size(); i++) {
                ArrayList<VideoClicks> tempVideoClicks = new ArrayList<VideoClicks>();
                ArrayList<ArrayList<VASTIcon>> tempIconAdsList = new ArrayList<ArrayList<VASTIcon>>();
                ArrayList<HashMap<TRACKING_EVENTS_TYPE, List<String>>> tempmTrackingEventMap = new ArrayList<HashMap<TRACKING_EVENTS_TYPE, List<String>>>();

                for (int j = 0; j < mVastModel.get(i).getCreativeList().size(); j++) {
                    tempIconAdsList.add(mVastModel.get(i).getCreativeList().get(j).getVastIcons());
                    tempmTrackingEventMap.add(mVastModel.get(i).getCreativeList().get(j).getTrackings());
                    tempVideoClicks.add(mVastModel.get(i).getCreativeList().get(j).getVideoClicks());
                }
                //extensions & companions
                extensionBeanList.add(mVastModel.get(i).getExtensionBean());
                companionAdsList.add(mVastModel.get(i).getCompanionAdList());//adcount
                //widler 2019 for companion
                /*
                for (int j = 0; j < mVastModel.get(i).getCreativeList().size(); j++) {
                    //extensionBeanList.add(mVastModel.get(i).getCreativeList().get(j).get);
                    companionAdsList.add(mVastModel.get(i).getCreativeList().get(j).getVastCompanionAds());
                }
                */
                //end wilder
                if (null != tempIconAdsList) {
                    iconAdsList.add(tempIconAdsList);
                }
                if (null != tempmTrackingEventMap) {
                    mTrackingEventMap.add(tempmTrackingEventMap);
                }
                if (null != tempVideoClicks) {
                    videoClicks.add(tempVideoClicks);
                }
            }

            initVPAID();  //wilder 2019, VPAID interface should be outside each creative, so it can handle all of events

            setNextMediaFile();
            //when start show, screen may be rotated, so it must be recalc corner size
            calcCornerSize();
            processImpressions();
        } else {
            AdViewUtils.logInfo("数据格式异常");
            finishActivity();
        }
    }

    public void showErrorPage() {
        if ( AdViewUtils.htmlUseBlankErrorPage ) {
            //view.destroyDrawingCache();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //load url must handled in main loop thread
                    if (contentWebView != null) {
                        contentWebView.loadUrl(AdViewUtils.htmlErrorPage);
                        //view.loadUrl("about:blank");
                    }
                }
            });
        }
    }

    private void calcCornerSize() {

        cornerSize = screenWidth > screenHeight ? screenHeight / 14 : screenWidth / 14;
        cornerSize = cornerSize < 50 ? 50 : cornerSize;
        cornerSize = cornerSize > 100 ? 100 : cornerSize;
    }

    //this was used by full screen activity ,
    // always triggered by after rotated screen , must recalc size
    public void getScreenSize(boolean isOriented) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        density = displayMetrics.density;

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        if (isOriented && screenWidth < screenHeight ) {
            //wilder 2019 for rotated screen
            removeCompanions();
            removeIcons();

            String url = screenWidth + "x" + screenHeight;
            Message message = new Message();
            message.obj = url;
            message.what = STATUS_SIZE_CHANGE_MESSAGE;
            handler.sendMessage(message);
            //end wilder 2019
        }

    }

    private void getBehavedSize() {
        Rect visibleRect = new Rect();
        this.getLocalVisibleRect(visibleRect);   //relative to parent's position

        behavedWidth = visibleRect.width() / 6;
        behavedHeight = visibleRect.height() / 5;

        //calc all behaved view size
        allBehavedCounts +=  mVastModel.get(adCount).getCompanionAdList().size();
        allBehavedCounts += mVastModel.get(adCount).getCreativeList().get(creativeCount).getVastIcons().size();
        //Wrapper 's behave view size
        for (int i = 0; i < wrapperModel.size(); i++) {
            ArrayList<VASTCompanionAd> companionAdArrayList = wrapperModel.get(i).getCompanionAdList();
            allBehavedCounts += companionAdArrayList.size();
            for (int j = 0; j < wrapperModel.get(i).getCreativeList().size(); j++) {
                ArrayList<VASTIcon> vastIconArrayList = wrapperModel.get(i).getCreativeList().get(j).getVastIcons();
                allBehavedCounts += vastIconArrayList.size();
            }
        }
        //end
    }

    private void fixLayoutSize(int w, int h, boolean hasBehaved) {
         if (w != -1 && h != -1){
            getDesiredSize(w, h);
            contentWebView.loadUrl("javascript:fixSize(" + desiredWidth / density + "," + desiredHeight / density + ")");

        } else {
            desiredWidth = screenWidth;
            desiredHeight = screenHeight;
        }
        Log.i("fixLayoutSize", "fixSize(" + desiredWidth / density + "," + desiredHeight / density + ")");
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) contentWebView.getLayoutParams();

        if (hasBehaved) {
            //LayoutParams  layoutParmars2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            layoutParams.width = desiredWidth /4 * 3;
            layoutParams.height = desiredHeight;

            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, -1);    //remove it first

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mRootLayout.setGravity(Gravity.CENTER_VERTICAL);
            //layoutParams.setGravity(Gravity.CENTER_HORIZONTAL);
            //layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        }else {
            layoutParams.width = desiredWidth;
            layoutParams.height = desiredHeight;
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        }

        contentWebView.setLayoutParams(layoutParams);

    }



    public void onResume() {

        AdViewUtils.logInfo("entered on onResume --(life cycle event)");
        try {
//            showProgressBar();
            if (isPaused) {
                isPaused = false;
                if (isFinished) {
                    return;
                }
                processEvent(TRACKING_EVENTS_TYPE.resume);
                //String mediaType = getCurrentMediaType();
                if (isVideoTypeRelated()) {
                    contentWebView.loadUrl("javascript:playVideo()");
                }
                //getScreenSize();
                startQuartileTimer(isVideoTypeRelated(), isFinalMedia());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        //super.onPause();
        AdViewUtils.logInfo("entered on onPause --(life cycle event)");
        try {
            isPaused = true;
            lastPauseVideoTime = currentVideoPlayTime;
            processEvent(TRACKING_EVENTS_TYPE.pause);
            //String mediaType = getCurrentMediaType();
            if (isVideoTypeRelated()) {
                contentWebView.loadUrl("javascript:pauseVideo()");
//                startQuartileTimer();
            }
            cleanActivityUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentMediaType() {
        try {
            String mediaType = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoType();
            String url = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();
            String vpaidURL = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVPAIDurl();   //wilder 2019

            if (!TextUtils.isEmpty(mediaType)) {
                return mediaType;
            } else {
                if(TextUtils.isEmpty(url))
                    return "";
                if (url.endsWith("mp4"))
                    mediaType = "video/mp4";
                else if (url.endsWith("png") | url.endsWith("jpg") | url.endsWith("gif"))
                    mediaType = "image/*";

                return mediaType;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
    private void removeView(View view) {
        if (null != view) {
            ((FrameLayout) findViewById(view.getId()).getParent()).removeView(view);
        }
    }
    */
    private void createActionView(int viewId, int position, boolean needBgColor) {
        createActionView(viewId, position, needBgColor, false);
    }

    private void createActionView(int viewId, int position, boolean needBgColor, boolean skipPosition) {
        if (null != findViewById(viewId)) {
            //wilder 2019, when rotated, it will be re-created
            mRootLayout.removeView(findViewById(viewId));
            //return;
        }
        Rect visibleRect = new Rect();
        //contentWebView.getGlobalVisibleRect(visibleRect); //only can be used in fullscreen video view
        this.getLocalVisibleRect(visibleRect);   //relative to parent's position

        Log.i("createActionView", "size = " + visibleRect);
        if (visibleRect.width() == 0 || visibleRect.height() == 0)
            return;
        FrameLayout.LayoutParams viewLayoutParams = new FrameLayout.LayoutParams(cornerSize, cornerSize);
        View view = null;
        Bitmap bm = null;

        switch (viewId) {
            case COUNTDOWN_VIEW_ID:
                view = new CountDownView(getContext());
                ((CountDownView) view).setPaintColor(Color.WHITE);
                ((CountDownView) view).setTextSize(cornerSize / 2);
                break;
            case SKIP_TEXT_ID:
                view = new TextView(getContext());
                ((TextView) view).setText(END_LABEL_TEXT);
                ((TextView) view).setTextSize(cornerSize / 6);
                ((TextView) view).setTextColor(Color.WHITE);
                ((TextView) view).setGravity(Gravity.CENTER);
                ((TextView) view).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                break;
            case CLOSE_VIEW_ID:
                view = new ImageView(getContext());
                bm = AdViewUtils.getImageFromAssetsFile("close_video.png");
                ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(), bm));
//                ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(),getClass().getResourceAsStream(
//                                        ConstantValues.WEBVIEW_IMAGE_BASE_PATH + "close_video.png")));
                break;
            case REPLAY_VIEW_ID:
                view = new ImageView(getContext());
                bm = AdViewUtils.getImageFromAssetsFile("replay.png");
                ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(), bm));
//                ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(), getClass()
//                        .getResourceAsStream( ConstantValues.WEBVIEW_IMAGE_BASE_PATH + "replay.png")));
                break;
            case VOLUME_VIEW_ID:
                view = new ImageView(getContext());
                if (null == volumeON) {
                    bm = AdViewUtils.getImageFromAssetsFile("unmute.png");
                    volumeON = new BitmapDrawable(getResources(), bm);
//                    volumeON = new BitmapDrawable(getResources(), getClass().getResourceAsStream(
//                                    ConstantValues.WEBVIEW_IMAGE_BASE_PATH + "unmute.png"));
                }
                if (null == volumeOFF) {
                    bm = AdViewUtils.getImageFromAssetsFile("mute.png");
                    volumeOFF = new BitmapDrawable(getResources(), bm);
//                    volumeOFF = new BitmapDrawable(getResources(), getClass().getResourceAsStream(
//                            ConstantValues.WEBVIEW_IMAGE_BASE_PATH + "mute.png"));
                }
                ((ImageView) view).setImageDrawable(AdViewUtils.isMediaMuted(getContext()) ? volumeOFF : volumeON);
                break;
        }
        view.setId(viewId);
        switch (position) {
            case LT_POSITION:
                if (skipPosition) {
                    viewLayoutParams.leftMargin = cornerSize / 8;
                    viewLayoutParams.topMargin = cornerSize / 8;
                } else {
                    viewLayoutParams.leftMargin = visibleRect.left + cornerSize / 8;
                    viewLayoutParams.topMargin = visibleRect.top + cornerSize / 8;
                }
                break;
            case RT_POSITION:
                if (skipPosition) {
                    viewLayoutParams.leftMargin = visibleRect.right - cornerSize - cornerSize / 8;
                    viewLayoutParams.topMargin = cornerSize / 8;
                } else {
                    viewLayoutParams.leftMargin = visibleRect.right - cornerSize - cornerSize / 8;
                    viewLayoutParams.topMargin = visibleRect.top + cornerSize / 8;
                }
                break;
            case LB_POSITION:
                viewLayoutParams.leftMargin = visibleRect.left + cornerSize / 8;
                viewLayoutParams.topMargin = visibleRect.bottom - cornerSize - cornerSize / 8;
                break;
            case RB_POSITION:
                viewLayoutParams.leftMargin = visibleRect.right - cornerSize - cornerSize / 8;
                viewLayoutParams.topMargin = visibleRect.bottom - cornerSize - cornerSize / 8;
                break;
            case SKIP_POSITION:
                viewLayoutParams.width = cornerSize * 3;
                viewLayoutParams.height = (int) (cornerSize);
                viewLayoutParams.leftMargin = visibleRect.right - cornerSize * 3 - cornerSize / 8;
                viewLayoutParams.topMargin = visibleRect.top + cornerSize / 8;
                view.setBackgroundColor(Color.parseColor(ConstantValues.VIDEO_ICON_BG_COLOR));
                view.setVisibility(View.GONE);
                break;
        }
        if (needBgColor && viewId != SKIP_TEXT_ID)
            view.setBackground(AdViewUtils.getColorDrawable(getContext(), ConstantValues.VIDEO_ICON_BG_COLOR, cornerSize));

        view.setOnClickListener(this);
        mRootLayout.addView(view,viewLayoutParams);  //wilder 2019 changed

    }

    private String lastVideoPlayTime = "";
    private boolean isHoldOn = false;

    private void updateCountDown(String content) {
        try {
            CountDownView countDownView = (CountDownView)findViewById(COUNTDOWN_VIEW_ID);
            if (!content.equals("undefined")) {
                if (lastVideoPlayTime.equals(content))
                    isHoldOn = true;
                else
                    isHoldOn = false;
                if (TextUtils.isEmpty(content) || content.equals("null") || TextUtils.isEmpty(currentTotalTime) || "null".equals(currentTotalTime))
                    return;

                if(isVPAID()) {
                    Float remain = Float.valueOf(content);
                    Float total = Float.valueOf(currentTotalTime);
                    countDownView.updateProgress((int)((total - remain) / total * 360));

                    countDownView.updateContent(String.valueOf(remain.intValue()));

                    lastVideoPlayTime = String.valueOf((int)(total - remain));
                }else {
                    currentVideoPlayTime = (int) (Float.valueOf(content) * 1000);
                    countDownView.updateProgress((int) (Float.valueOf(content) / Float.valueOf(currentTotalTime) * 360));
                    int tempInt = Float.valueOf(currentTotalTime).intValue() - Float.valueOf(content).intValue();
                    countDownView.updateContent(tempInt + "");

                    lastVideoPlayTime = content;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanActivityUp() {
        AdViewUtils.logInfo("cleanActivityUp stopQuartileTimer ");
        this.stopQuartileTimer();
        isSkippShown = false;
        mRootLayout.removeView(findViewById(COUNTDOWN_VIEW_ID));
        mRootLayout.removeView(findViewById(SKIP_TEXT_ID));
        mRootLayout.removeView(findViewById(VOLUME_VIEW_ID));
    }

    private void cleanUpMediaPlayer() {
        AdViewUtils.logInfo("entered cleanUpMediaPlayer ");
        if (null != contentWebView) {
            contentWebView.stopLoading();
            //contentWebView.loadUrl("about:blank");
            contentWebView.destroy();
            contentWebView = null;
        }
    }

    private boolean getDesiredSize(int w, int h) {
        float videoWidth = w == 0 ? (float) mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoWidth() : (float) w;
        float videoHeight = h == 0 ? (float) mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoHeight() : (float) h;

        Activity activity = (Activity) mContext;

        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            if ( w < h  && !isEmbed) {
                //wilder 2019 , add if w > h, it will means portrait mode, must rotated
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
                desiredWidth = screenWidth;
                desiredHeight = (int) (((float) screenWidth / videoWidth) * videoHeight);
                return false;
            }
            //based on dx or dy
            float dx = videoWidth / screenWidth;
            float dy = videoHeight / screenHeight;
            if (dx > dy) {
                desiredWidth = screenWidth;
                desiredHeight = (int) (1 / dx * videoHeight);
            } else {
                desiredWidth = (int) (1 / dy * videoWidth);
                desiredHeight = screenHeight;
            }

        } else if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
            desiredWidth = screenWidth;
            desiredHeight = (int) (((float) screenWidth / videoWidth) * videoHeight);
        }else {
            //AdViewUtils.logInfo(" need fix width & height");
            float dx = videoWidth / screenWidth;
            float dy = videoHeight / screenHeight;
            if (dx > dy) {
                desiredWidth = screenWidth;
                desiredHeight = (int) (1 / dx * videoHeight);
            } else {
                desiredWidth = (int) (1 / dy * videoWidth);
                desiredHeight = screenHeight;
            }

        }

        return true;
    }

    private void initVPAID() {
        //wilder 2019 VPAID
        Activity activity = (Activity) mContext;
        mAdParams = mVastModel.get(adCount).getCreativeList().get(creativeCount).getAdParameters();
        mACVpaidCtrl = new AdViewControllerVpaid( activity, mAdParams);
        mACVpaidCtrl.setListener(this);
        contentWebView.addJavascriptInterface(mACVpaidCtrl.getBridge(), "android");
    }

    private void createUiView() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        density = displayMetrics.density;

        mRootLayout = new RelativeLayout(mContext);
        mRootLayout.setBackgroundColor(Color.parseColor(bgColor));

        //(wilder 2019) it must be MATCH_PARENT, so view can be fitted  with parent view
        //mRootLayout.setGravity(Gravity.CENTER_VERTICAL); //vertial can make webview center in parent
        this.addView(mRootLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //content webview layout
        LayoutParams  layoutParmars = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParmars.addRule(RelativeLayout.CENTER_IN_PARENT);

        createWebView();

        contentWebView.setLayoutParams(layoutParmars);

        mRootLayout.addView(contentWebView, layoutParmars);
    }

    private void createWebView() {
        contentWebView = new CustomWebview(mContext);

        contentWebView.setWebViewClient(new VideoWebClient());
        contentWebView.setWebChromeClient(new VideoChromeClient());
        contentWebView.setCustomInterface(this);
    }

    private boolean isFinalMedia() {
        //last ad 's last creative
        if (adCount == (mVastModel.size() - 1)) {
            if (creativeCount == mVastModel.get(adCount).getCreativeList().size() - 1) {
                return true;
            }
            else
                return false;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case VOLUME_VIEW_ID:
                if(isVPAID()) {
                    if(currentVPAIDVolume > 1.0f) {
                        //maybe use percent
                        currentVPAIDVolume += 10.0f;
                        if (currentVPAIDVolume > 100.0f) {
                            currentVPAIDVolume = 0.0f;
                        }
                    }else if (currentVPAIDVolume > 0.0f && currentVPAIDVolume < 1.0f ){
                        //html video's volume only can be 0 - 1
                        currentVPAIDVolume += 0.1f;
                        if (currentVPAIDVolume > 1.0f) {
                            currentVPAIDVolume = 0.0f;
                        }
                    }
                    mACVpaidCtrl.setVolume(currentVPAIDVolume);
                }else {
                    if (AdViewUtils.isMediaMuted(mContext)) {
                        ((ImageView) v).setImageDrawable(volumeON);
                        handler.sendEmptyMessage(STATUS_UNMUTE_MESSAGE);
                    } else {
                        ((ImageView) v).setImageDrawable(volumeOFF);
                        handler.sendEmptyMessage(STATUS_MUTE_MESSAGE);
                    }
                    AdViewUtils.setCurrentVolume(mContext, !AdViewUtils.isMediaMuted(mContext));
                }
                break;
            case SKIP_TEXT_ID:
                currentVideoPlayTime = 0;
                isSkipped = true;
                if(isVPAID()) {
                    mACVpaidCtrl.skipVideo();
                }else {
                    contentWebView.loadUrl("javascript:skipVideo()");
                }
                switchPlay(false);
                break;
            case CLOSE_VIEW_ID:
                finishActivity();
                break;
            case REPLAY_VIEW_ID:
                try {
                    mRootLayout.removeView(findViewById(CLOSE_VIEW_ID));
                    mRootLayout.removeView(findViewById(REPLAY_VIEW_ID));
                    VideoLableView v1 = (VideoLableView)findViewById(ICONBANNER_VIEW_ID);
                    VideoFinalPage v2 = (VideoFinalPage)findViewById(FINALPAGE_VIEW_ID);
                    mRootLayout.removeView(v1);
                    mRootLayout.removeView(v2);
                    if (v1 != null) {
                        v1.destoryView();
                    }
                    if (v2 != null ) {
                        v2.destoryView();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isFinished = false;
                adCount = 0;
                creativeCount = 0;

                setNextMediaFile();
                break;
            case FINALPAGE_VIEW_ID:
            case ICONBANNER_VIEW_ID:
                infoClicked(mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickThrough(),
                        mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(),
                        0, 0);
                break;
        }
    }

    @Override
    public void onWebviewClicked(int type, int tag) {
        if (type == ICON_TYPE) {
            IconClicks iconClicks = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVastIcons().get(tag).getIconClicks();
            infoClicked(iconClicks.getClickThrough(), iconClicks.getClickTracking(), 0, 0);
        } else if (type == COMPANION_TYPE) {
            CompanionClicks companionClicks = mVastModel.get(adCount).getCompanionAdList().get(tag).getCompanionClicks();
            infoClicked(companionClicks.getClickThrough(), companionClicks.getClickTracking(), 0, 0);
        } else if (type == WRAPPER_TYPE + ICON_TYPE) {
            int tmpAdCount = tag / 1000000;
            int tmpCreativeCount = 0;
            int tmpNum = 0;
            tmpCreativeCount = (tag - tmpAdCount * 1000000) / 10000;
            tmpNum = (tag - tmpAdCount * 1000000 - tmpCreativeCount * 10000) % 100;

            IconClicks iconClicks = mVastModel.get(tmpAdCount).getCreativeList().get(tmpCreativeCount).getVastIcons().get(tmpNum).getIconClicks();
            infoClicked(iconClicks.getClickThrough(), iconClicks.getClickTracking(), 0, 0);
        } else if (type == WRAPPER_TYPE + COMPANION_TYPE) {
            int tmpCreativeCount = 0;
            int tmpNum = 0;
            tmpNum = (tag - tmpCreativeCount * 10000) % 100;

            CompanionClicks companionClicks = wrapperModel.get(tmpCreativeCount).getCompanionAdList().get(tmpNum).getCompanionClicks();
            infoClicked(companionClicks.getClickThrough(), companionClicks.getClickTracking(), 0, 0);
        }
    }

    @Override
    public void onWebviewLayout(boolean changed, int left, int top, int right, int bottom)
    {
        //after webview video resized or changed position, this will be trigger
        currentPosition.left = left;
        currentPosition.top = top;
        currentPosition.right = right;
        currentPosition.bottom = bottom;

    }

    @Override
    public void runOnUiThread(Runnable runnable) {
       //
        ((Activity) mContext).runOnUiThread(runnable);
    }

    class WebClientHandler extends Handler {

        public WebClientHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != STATUS_TIME_MESSAGE) {
                AdViewUtils.logInfo("AdVASTView():  handlerMsg=" + msg.what);
            }
            switch (msg.what) {
                case  STATUS_VPAID_SKIPBUTTON_SHOW:
                    try {
                        if (null != msg.obj) {
                            boolean show = Boolean.parseBoolean(msg.obj.toString());
                            if (show) {
                                createActionView(SKIP_TEXT_ID, SKIP_POSITION, true);
                            }else {
                                mRootLayout.removeView(findViewById(SKIP_TEXT_ID));
                            }
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case STATUS_VPAID_VOLUME_STATUS:
                    try {
                        if (null != msg.obj) {
                            float v = Float.valueOf(msg.obj.toString());
                            currentVPAIDVolume = v;
                            ImageView vw = findViewById(VOLUME_VIEW_ID);
                            if(v > 0 ) {
                                //AdViewUtils.setCurrentVolumeValue(this, v);
                                //send
                                if(vw != null) {
                                    vw.setImageDrawable(volumeON);
                                }
                                handler.sendEmptyMessage(STATUS_UNMUTE_MESSAGE);
                            }else {
                                if(vw != null) {
                                    vw.setImageDrawable(volumeOFF);
                                }
                                handler.sendEmptyMessage(STATUS_MUTE_MESSAGE);
                            }
                        }
                    }catch ( Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case STATUS_SIZE_CHANGE_MESSAGE:
                case STATUS_VPAID_ADSTART_VIEW:     //vpaid
                    try {
                        if (null != msg.obj) {
                            if (!TextUtils.isEmpty(msg.obj.toString())) {
                                String[] size = msg.obj.toString().split("&");
                                if (size.length == 2) {
                                    fixLayoutSize(
                                            Integer.valueOf(size[0].replace("w=", "")),
                                            Integer.valueOf(size[1].replace("h=", "")),
                                            false);
                                    postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isVPAID()) {
                                                hideProgressBar();
                                            }
                                            if (isFinalMedia() && !isVideoTypeRelated()) {
                                                createActionView(REPLAY_VIEW_ID, LT_POSITION, true);
                                                createActionView(CLOSE_VIEW_ID, RT_POSITION, true);
                                            } else {
                                                if (isVideoTypeRelated() || isVPAID()) {
                                                    //创建Inline 的Companion Icon
                                                    getBehavedSize(); //wilder 2019, this will calc all behaved list size for layout behaved view's pos
                                                    createBehavedView(mVastModel.get(adCount).getCompanionAdList(), COMPANION_TYPE, 0);
                                                    createBehavedView(mVastModel.get(adCount).getCreativeList().get(creativeCount).getVastIcons(), ICON_TYPE, 0);
                                                    //创建Wrapper 的Companion Icon
                                                    for (int i = 0; i < wrapperModel.size(); i++) {
                                                        ArrayList<VASTCompanionAd> companionAdArrayList = wrapperModel.get(i).getCompanionAdList();
                                                        if (!companionAdArrayList.isEmpty()) {
                                                            createBehavedView(companionAdArrayList, WRAPPER_TYPE + COMPANION_TYPE, i * 10000);
                                                        }
                                                        for (int j = 0; j < wrapperModel.get(i).getCreativeList().size(); j++) {
                                                            ArrayList<VASTIcon> vastIconArrayList = wrapperModel.get(i).getCreativeList().get(j).getVastIcons();
                                                            createBehavedView(vastIconArrayList, WRAPPER_TYPE + ICON_TYPE, i * 1000000 + j * 10000);
                                                        }
                                                    }
                                                    createActionView(VOLUME_VIEW_ID, RB_POSITION, true);
                                                }
                                                createActionView(REPLAY_VIEW_ID, LT_POSITION, true);  //wilder 2019 for view
                                                createActionView(CLOSE_VIEW_ID, RT_POSITION, true);    //wilder 2019 for view

                                                createActionView(COUNTDOWN_VIEW_ID, LB_POSITION, true);
                                                createActionView(SKIP_TEXT_ID, SKIP_POSITION, true);
                                            }
                                            isScaled = false;
                                        }
                                    }, 200);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATUS_MUTE_MESSAGE:
                    processEvent(TRACKING_EVENTS_TYPE.mute);
                    break;

                case STATUS_UNMUTE_MESSAGE:
                    processEvent(TRACKING_EVENTS_TYPE.unmute);
                    break;

                case STATUS_END_MESSAGE:
                    processEvent(TRACKING_EVENTS_TYPE.complete);
                    switchPlay(false);
                    break;

                case STATUS_START_MESSAGE:
                    hideProgressBar();
                    if (!isSkipped) {
                        /*  wilder 2019 covered
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            contentWebView.evaluateJavascript("javascript:getTotalTime()", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    Message msg = new Message();
                                    msg.what = STATUS_TOTALTIME_MESSAGE;
                                    msg.obj = value;
                                    handler.sendMessage(msg);
                                }
                            });
                        } else {
                            contentWebView.loadUrl("javascript:alert(getTotalTime())");
                        }  */
                        try {
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //contentWebView.loadUrl("javascript:pauseVideo()"); wilder 2019 comment
                                        postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    contentWebView.loadUrl("javascript:playVideo()");
                                                } catch (Exception e) {
                                                }
                                            }
                                        }, 200);
                                    } catch (Exception e) {
                                    }
                                }
                            }, 200);
                        } catch (Exception e) {
                        }
                    }
                    break;

                case STATUS_TIME_MESSAGE:
                    if (null != findViewById(COUNTDOWN_VIEW_ID)) {
                        if (!isGotTTime & !isVPAID()) {
                            isGotTTime = true;
                            sendEmptyMessage(STATUS_START_MESSAGE);
                        }
                        if (null != msg.obj) {

                            updateCountDown((String) msg.obj);

                        }
                    }
                    if (null != findViewById(SKIP_TEXT_ID)) {
                        if (!isSkippShown)
                            showSkipText((String) msg.obj);
                    }
                    break;

                case STATUS_TOTALTIME_MESSAGE:
                    currentTotalTime = (String) msg.obj;
                    try {
                        if (!TextUtils.isEmpty(currentTotalTime) && !"null".equals(currentTotalTime)) {
                            Float totalTime = Float.valueOf(currentTotalTime);
                            mVastModel.get(adCount).getCreativeList().get(creativeCount).setDuration(totalTime.intValue());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATUS_CLICK_MESSAGE:
                    try {
                        String result = msg.obj.toString();
                        HashMap<String, String> resultMap = parseJsReact(result);
                        if (resultMap.containsKey("x") && resultMap.containsKey("y")) {
                            infoClicked(mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickThrough(),
                                    mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(),
                                    Float.valueOf(resultMap.get("x")).intValue(),
                                    Float.valueOf(resultMap.get("y")).intValue());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATUS_ERROR_MESSAGE:
                    processErrorEvent();
                    stopQuartileTimer();
                    switchPlay(false);
                    break;

                case STATUS_VISIBLE_CHANGE_MESSAGE:
                    findViewById(msg.arg2).setVisibility(msg.arg1);
                    break;

                case STATUS_ICON_BANNER_MESSAGE:
                    try {
                        VideoLableView videoLableView = new VideoLableView(mContext);
                        videoLableView.setId(ICONBANNER_VIEW_ID);
                        videoLableView.setOnClickListener(AdVASTView.this);
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(desiredWidth - 5 * cornerSize, (int) (desiredHeight / 4.3));
                        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                        lp.bottomMargin = (int) (cornerSize / 8);
                        mRootLayout.addView(videoLableView, lp);
                        videoLableView.setData(msg.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATUS_FINAL_PAGE_MESSAGE:
                    if (null != findViewById(FINALPAGE_VIEW_ID))
                        ((VideoFinalPage) findViewById(FINALPAGE_VIEW_ID)).setData(msg.getData());
                    break;

                case STATUS_PLAY_VIDEO_MESSAGE:
                    playVideo(getContext());
                    break;
                default:
                    break;
            }
        }
    }

    boolean isGotTTime = false;

    private HashMap<String, String> parseJsReact(String jsResult) {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        String[] tempArray;
        if (!TextUtils.isEmpty(jsResult)) {
            tempArray = jsResult.split("&");
            if (tempArray.length > 0) {
                for (int i = 0; i < tempArray.length; i++) {
                    String[] valueArray = tempArray[i].split("=");
                    if (valueArray.length == 2) {
                        resultMap.put(valueArray[0], valueArray[1]);
                    }
                }
            }
        }
        return resultMap;
    }

    class VideoChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            if (!message.equals("undefined")) {
                Message msg = new Message();
                msg.what = STATUS_TOTALTIME_MESSAGE;
                msg.obj = message;
                handler.sendMessage(msg);
                result.confirm();
                return true;
            } else
                return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            AdViewUtils.logDebug(consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    class VideoWebClient extends WebViewClient {
        //wilder 2019
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
            //fix https can not access
            //super.onReceivedSslError(view, handler, error); wilder 2019 must remove this line.
            //handler.proceed();
            AdViewUtils.logInfo("************ onReceivedSslError : " + error.toString());
            if(error.getPrimaryError() == android.net.http.SslError.SSL_INVALID ){
                // 校验过程遇到了bug
                handler.proceed();
            }else{
                handler.cancel();
            }
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // android 5.0以上默认不支持Mixed Content
                view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            } */
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            AdViewUtils.logInfo("++++ onPageFinished : url = " + url + "+++++++");
            if (mIsWaitingForWebView) {
                mACVpaidCtrl.initBridgeWrapper();
                mIsWaitingForWebView = false;
            }
            view.invalidate();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                //if (null != listener)
                //    return listener.onShouldIntercept(url);
            }
            if(url.contains(".xml") && url.contains(".js")) {
                AdViewUtils.logInfo("++++ shouldInterceptRequest : " + url + "++++");
            }

            return super.shouldInterceptRequest(view, url);
        }
        //end wilder
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!url.contains("time")) {
                AdViewUtils.logInfo("shouldOverrideUrlLoading : " + url);
            }
            if (url.startsWith("mraid://")) {
                Message message = new Message();
                if (url.contains("play"))
                    message.what = STATUS_START_MESSAGE;
                else if (url.contains("end"))
                    message.what = STATUS_END_MESSAGE;
                else if (url.contains("skip"))
                    message.what = STATUS_SKIP_MESSAGE;
                else if (url.contains("error"))
                    message.what = STATUS_ERROR_MESSAGE;
                else if (url.contains("click")) {
                    if (((CustomWebview) view).isClicked()) {
                        message.what = STATUS_CLICK_MESSAGE;
                        message.obj = url.replace("mraid://click?", "");
                        ((CustomWebview) view).setClicked(false);
                    }
                } else if (url.contains("time")) {
                    message.what = STATUS_TIME_MESSAGE;
                    message.obj = url.replace("mraid://time?", "");

                } else if (url.contains("totalduration")) {
                    //(widler 2019) for get totaltime
                    message.what = STATUS_TOTALTIME_MESSAGE;
                    message.obj = url.replace("mraid://totalduration?", "");

                } else if (url.contains("size")) {
                    message.obj = url.replace("mraid://size?", "");
                    message.what = STATUS_SIZE_CHANGE_MESSAGE;
                    final ExtensionBean eb = mVastModel.get(adCount).getExtensionBean();
                    if (null != eb) {
                        if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated()) {
                            AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
                        }
                        AdViewUtils.repScheduler.execute(new AdVASTView.ResourceDownloadRunnable(eb, STATUS_ICON_BANNER_MESSAGE));
                    }
                }
                handler.sendMessage(message);
            } else if (url.startsWith("http")||url.startsWith("https")) {
                infoClicked(url, mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(), 0, 0);
            }
            return false;
        }
    }

    class ResourceDownloadRunnable implements Runnable {
        private ExtensionBean eb;
        private int status;

        public ResourceDownloadRunnable(ExtensionBean eb, int status) {
            this.eb = eb;
            this.status = status;
        }

        @Override
        public void run() {
            String iconPath = (String) AdViewUtils.getInputStreamOrPath(mContext, eb.getEndPageIconUrl(), 1);
            String bgPath = (String) AdViewUtils.getInputStreamOrPath(mContext, eb.getEndPageImage(), 1);
            if (null != bgPath || null != iconPath) {
                Bundle bundle = new Bundle();
                bundle.putString("bgPath", bgPath);
                bundle.putString("iconPath", iconPath);
                bundle.putString("title", eb.getEndPageTitle());
                bundle.putString("subTitle", eb.getEndPageDesc());
                bundle.putString("buttonText", eb.getEndPageText());

                Message msg2 = new Message();
                msg2.what = status;
                msg2.setData(bundle);
                handler.sendMessage(msg2);
            }
        }
    }

    private void createProgressBar() {
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        mProgressBar = new ProgressBar(mContext);
        mProgressBar.setLayoutParams(params);

        mRootLayout.addView(mProgressBar);
        mProgressBar.setVisibility(View.GONE);
    }

    /*
    private void hideTitleStatusBars() {
        // hide title bar of application
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // hide status bar of Android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }
    */
    private boolean isVPAID() {
        String vpURL = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVPAIDurl();
        if (!TextUtils.isEmpty(vpURL)) {
            return true;
        }
        return false;
    }

    private boolean isPlayOnline() {

        if (null != this.mPlayer) {
            return mPlayer.isPlayOnline();
        }
        return false;
    }

    private boolean isVideoTypeRelated() {
        String mediaType = getCurrentMediaType();
        if (mediaType.contains("video") || isPlayOnline()) {
            return true;
        }

        return false;
    }

    private void setNextMediaFile() {
        AdViewUtils.logInfo("setNextMediaFile");
        String mediaType = getCurrentMediaType();
        String url = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();

        startQuartileTimer(isVideoTypeRelated(), isFinalMedia());

        if (mVastModel.get(adCount).getCreativeList().get(creativeCount).isFailed() ||
                !mVastModel.get(adCount).getCreativeList().get(creativeCount).isReady()) {

            AdViewUtils.logInfo("==== media not ready ====");
            contentWebView.loadUrl("<body bgcolor=" + bgColor + ">");
            if (mVastModel.get(adCount).getCreativeList().get(creativeCount).isFailed()) {
                switchPlay(false);
            }else {
                //wait for download
                isWaittingDownload = true;
            }
        } else {
            String html = generalHtml(url, mediaType, bgColor);
            if (!TextUtils.isEmpty(html)) {
                if (mediaType.matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX)) {
                    if (url.startsWith("http")||url.startsWith("https"))
                        contentWebView.loadUrl(url);
                    else {
                        contentWebView.loadDataWithBaseURL("", url, "text/html", "utf-8", null);
//                        contentWebView.loadUrl("javascript:changeBackgroundColor(#ff0000)");
                    }
                }
                else if (mediaType.matches(DefaultMediaPicker.SUPPORTED_JAVASCRIPT_TYPE_REGEX)) {
                    //wilder 2019 for VPAID
                    String vpURL = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVPAIDurl();
                    if (!TextUtils.isEmpty(vpURL)) {
                        AdViewUtils.logInfo("+++ setNextMediaFile(): vpURL = " + vpURL + "++++");
                        mIsWaitingForWebView = true;
                        //load creative URL
                        mACVpaidCtrl.loadVPAIDURL(contentWebView, vpURL);
                    }
                } //end wilder
                else {
                    //video case : include file:// or http: or https 's video file
                    contentWebView.loadDataWithBaseURL(
                            (url.startsWith("http")||url.startsWith("https")) ? "" :"file://" + url.substring(0,url.lastIndexOf("/") + 1),
                            html,
                            "text/html",
                            "utf-8", null);
                }
            }
        }
        /* (wilder 2019) for more ad or more creatives can be download, will trigger the following */
        //if (!isFinalMedia())
        if (isWaittingDownload) {
            if (null == VASTPlayer.executorService || VASTPlayer.executorService.isTerminated()) {
                VASTPlayer.executorService = Executors.newFixedThreadPool(1);
            }

            Boolean isHtmlorJS = mVastModel.get(adCount).getCreativeList().get(creativeCount).
                    getPickedVideoType().matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX)
                    || mVastModel.get(adCount).getCreativeList().get(creativeCount).
                    getPickedVideoType().matches(DefaultMediaPicker.SUPPORTED_JAVASCRIPT_TYPE_REGEX);

            VASTPlayer.executorService.execute(new DownloadRunnable(mContext,
                    mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl(),
                    isHtmlorJS,
                    false,
                    adCount, creativeCount,
                    new AdVASTView.DownloadNextMediaInterface()));

        }
    }

    class DownloadNextMediaInterface implements DownloadStatusInterface {

        @Override
        public void onDownloadFinished(final int pos, final int creativePos, String path) {
            AdViewUtils.logInfo("++++ DownloadNextMediaInterface() : onDownloadFinished " + path + ";" + "creativePos " + creativePos + "++++++");
            mVastModel.get(pos).getCreativeList().get(creativePos).setReady(true);
            mVastModel.get(pos).getCreativeList().get(creativePos).setPickedVideoUrl(path);
            if (isWaittingDownload) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isWaittingDownload = false;
                        setNextMediaFile();
                    }
                });
            }
        }

        @Override
        public void onDownloadFailed(int pos, int creativePos, int error) {
            AdViewUtils.logInfo("onDownloadFailed " + error + "   creativePos " + creativePos);
            mVastModel.get(pos).getCreativeList().get(creativePos).setFailed(true);
            processErrorEvent();
            if (isWaittingDownload) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isWaittingDownload = false;
                        setNextMediaFile();
                    }
                });

            }
        }

        @Override
        public void onShouldPlayOnline(int pos, int creativePos) {
            mVastModel.get(pos).getCreativeList().get(creativePos).setReady(true);
        }

        @Override
        public void downloadCanceled(int pos, int creativePos) {
        }

        @Override
        public int getDownloadStatus(String url, String name, long size) {
            return VASTPlayer.getDownloadStatus(mContext, url, name, size);
        }

        @Override
        public boolean checkCacheSize(long size) {
            return VASTPlayer.checkFileSize(mContext, size);
        }

        @Override
        public boolean getDownloadPath(String url, String name) {
            File downloadPath = new File(ConstantValues.DOWNLOAD_VIDEO_PATH);
            if (!downloadPath.exists())
                return downloadPath.mkdirs();
            return true;
        }

    }

    private void showSkipText(String content) {
        try {
            int skipDuration = mVastModel.get(adCount).getCreativeList().get(creativeCount).getSkipoffset();
            if (skipDuration == -1)
                return;
            TextView skipText = (TextView)findViewById(SKIP_TEXT_ID);
            if (Float.valueOf(content) >= skipDuration) {
                isSkippShown = true;
                skipText.setVisibility(View.VISIBLE);
                skipText.setOnClickListener(AdVASTView.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generalHtml(String url, String type, String color) {
        String asstesName = "";
        if (type.matches(DefaultMediaPicker.SUPPORTED_IMAGE_TYPE_REGEX)) {
            //asstesName = Assets.IMAGEJS;
            asstesName = AdViewUtils.loadAssetsFile("VAST_Image_JS.html");
        } else if (type.matches(DefaultMediaPicker.SUPPORTED_VIDEO_TYPE_REGEX)) {
            //"/assets/VAST_Video_JS.html"
            asstesName = AdViewUtils.loadAssetsFile("VAST_Video_JS.html");
            //asstesName = Assets.VIDEOJS;
        } else if (type.matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX)) {
            return url;
        } else if (type.matches(DefaultMediaPicker.SUPPORTED_JAVASCRIPT_TYPE_REGEX)) {
            return url;
        }

        final String tempStr = asstesName; //Assets.getJsFromBase64(asstesName);

        if (null != tempStr) {
            if (url.startsWith("http") || url.startsWith("https")) {
                return tempStr.replace("__COLOR__", color).
                        replace("VIDEO_FILE", url).
                        replace("WIDTH", desiredWidth / density + "").
                        replace("HEIGHT", desiredHeight / density + "");
            }
            else {
                return tempStr.replace("__COLOR__", color).
                        replace("VIDEO_FILE", url.substring(url.lastIndexOf("/") + 1, url.length())).
                        replace("WIDTH", desiredWidth / density + "").
                        replace("HEIGHT", desiredHeight / density + "");
            }
        }
        return null;
    }

    private void processImpressions() {
        AdViewUtils.logInfo("entered processImpressions");
        try {
            if (!mIsProcessedImpressions) {
                mIsProcessedImpressions = true;
                List<String> impressions = mVastModel.get(adCount).getImpressions();
                fireUrls(impressions, KyAdBaseView.getHK_Values(mContext, -1, -1, false, false, getVideoSnap()));
                reportWrapperEvents(IMPRESSION_WRAPPER_EVENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportWrapperEvents(int wrapperEvent) {
        reportWrapperEvents(wrapperEvent, null, -1, -1);
    }

    private void reportWrapperEvents(int wrapperEvent, TRACKING_EVENTS_TYPE event) {
        reportWrapperEvents(wrapperEvent, event, -1, -1);
    }

    private void reportWrapperEvents(int wrapperEvent, int x, int y) {
        reportWrapperEvents(wrapperEvent, null, x, y);
    }

    private void reportWrapperEvents(int wrapperEvent, TRACKING_EVENTS_TYPE event, int x, int y) {
        if (wrapperModel == null || wrapperModel.size() == 0)
            return;
        switch (wrapperEvent) {
            case CLICKTRACKING_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    ArrayList<VASTCreative> creativeList = wrapperModel.get(i).getCreativeList();
                    for (int j = 0; j < creativeList.size(); j++) {
                        VideoClicks videoClicks = creativeList.get(j).getVideoClicks();
                        fireUrls(videoClicks.getClickTracking(),
                                KyAdBaseView.getHK_Values(mContext, x, y, false, false, getVideoSnap()));
                    }
                }
                break;
            case ERROR_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    fireUrls(wrapperModel.get(i).getErrorUrl(),
                            KyAdBaseView.getHK_Values(mContext, -1, -1, false, true, getVideoSnap()));
                }
                break;
            case IMPRESSION_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    fireUrls(wrapperModel.get(i).getImpressions(),
                            KyAdBaseView.getHK_Values(mContext, -1, -1, false, false, getVideoSnap()));
                }
                break;
            case TRACKING_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    ArrayList<VASTCreative> creativeList = wrapperModel.get(i).getCreativeList();
                    for (int j = 0; j < creativeList.size(); j++) {
                        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings = creativeList.get(j).getTrackings();
                        if (null != trackings && null != trackings.get(event))
                            fireUrls(trackings.get(event),
                                    KyAdBaseView.getHK_Values(mContext, -1, -1,
                                            event.equals(TRACKING_EVENTS_TYPE.complete), false, getVideoSnap()));
                    }
                }
                break;
        }
    }

    private void infoClicked(String jumpUrl, List<String> fireUrls, int x, int y) {
        AdViewUtils.logInfo("entered infoClicked:");
        try {
            if (!TextUtils.isEmpty(jumpUrl))
                processClickThroughEvent(jumpUrl, fireUrls, x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processClickThroughEvent(String jumpUrl, List<String> fireUrls, int x, int y) {
        AdViewUtils.logInfo("entered processClickThroughEvent:");
        try {
            //VASTPlayer.sendLocalBroadcast(mContext, VASTPlayer.ACTION_VASTCLICK, null);
            mPlayer.sendClick();

            fireUrls(fireUrls, KyAdBaseView.getHK_Values(mContext, x, y, false, false, getVideoSnap()));
            reportWrapperEvents(CLICKTRACKING_WRAPPER_EVENT, x, y);
            // Navigate to the click through url
            Intent i = new Intent();
            i.putExtra("adview_url", jumpUrl);//"http://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk");//jumpUrl);
            i.putExtra("isVideo", true);
            i.setClass(mContext, AdViewLandingPage.class);
            ((Activity)mContext).startActivity(i);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //wilder 2019
    private void showFinalPage() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean skipPosition = false;
                    VideoFinalPage videoFinalPage = null;
                    FrameLayout.LayoutParams lp = null;
                    final ExtensionBean eb = mVastModel.get(adCount).getExtensionBean();
                    if (null != eb) {
                        skipPosition = true;
                        videoFinalPage = new VideoFinalPage(mContext);
                        videoFinalPage.setOnClickListener(AdVASTView.this);
                        videoFinalPage.setId(FINALPAGE_VIEW_ID);
                        lp = new FrameLayout.LayoutParams(-1, -1);
                        //LayoutParams  layoutParmars = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        //layoutParmars.addRule(RelativeLayout.CENTER_IN_PARENT);
                        if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                            AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOLNUM);
                        AdViewUtils.repScheduler.execute(new ResourceDownloadRunnable(eb, STATUS_FINAL_PAGE_MESSAGE));
                    }
                    if (null != videoFinalPage) {
                        //addContentView(videoFinalPage, lp);
                        mRootLayout.addView(videoFinalPage, lp); //wilder 2019 changed to rootlayer
                    }
                    createActionView(CLOSE_VIEW_ID, RT_POSITION, true, skipPosition);
                    createActionView(REPLAY_VIEW_ID, LT_POSITION, true, skipPosition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    private boolean isFinished = false;

    private void switchPlay(boolean isManual) {
        try {
            stopQuartileTimer();
            hasError = false;
            isGotTTime = false;
            currentVideoPlayTime = 0;
            removeIcons();
            removeCompanions();


            if (adCount <= mVastModel.size() - 1) {
                //(wilder 2019) not last ad
                if (!isManual && creativeCount < mVastModel.get(adCount).getCreativeList().size() - 1) {
                    //not last creative
                    creativeCount++;
                } else {
                    //last creative
                    if (adCount < mVastModel.size() - 1) {
                        //still more ad
                        adCount++;      //next ad
                        creativeCount = 0;  //reset creative
                    }else {
                        //last ad
                        if (null != findViewById(SKIP_TEXT_ID)) {
                            isSkippShown = false;
                        }
                        mRootLayout.removeView(findViewById(COUNTDOWN_VIEW_ID));
                        mRootLayout.removeView(findViewById(VOLUME_VIEW_ID));
                        mRootLayout.removeView(findViewById(SKIP_TEXT_ID));
                        if (isFinalMedia()) {
                            showFinalPage();
                        }
                        mTrackingEventTimer = null;
                        isFinished = true;
                        //VASTPlayer.sendLocalBroadcast(mContext, VASTPlayer.ACTION_VASTCOMPLETE, null);
                        mPlayer.sendComplete();
                        return;
                    }

                }

                showProgressBar();
                isSkipped = false;
                if (null != findViewById(SKIP_TEXT_ID)) {
                    isSkippShown = false;
                }
                mRootLayout.removeView(findViewById(COUNTDOWN_VIEW_ID));
                mRootLayout.removeView(findViewById(VOLUME_VIEW_ID));
                mRootLayout.removeView(findViewById(SKIP_TEXT_ID));
                setNextMediaFile();
                mIsProcessedImpressions = false;
            }
        } catch (Exception e) {
            try {
                finishActivity();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void startQuartileTimer(final boolean isVideo, final boolean noSkip) {
        try {
            if (null == mTrackingEventTimer) {
                mTrackingEventTimer = new Timer();
            }
            mTrackingEventTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (hasError)
                        return;
                    int duration = mVastModel.get(adCount).getCreativeList().get(creativeCount).getDuration() * 1000;
                    final CountDownView countDownView = (CountDownView)findViewById(COUNTDOWN_VIEW_ID);
                    final ImageView closeView = (ImageView)findViewById(CLOSE_VIEW_ID);
                    if (!isHoldOn) {
                        if (0 != currentVideoPlayTime && 0 != duration) {
                            int percentage = currentVideoPlayTime * 100 / duration;
                            if (percentage >= 25 * mQuartile) {
                                if (mQuartile == 0) {
                                    AdViewUtils.logInfo("Video at start: (" + percentage + "%)");
                                    processEvent(TRACKING_EVENTS_TYPE.start);
                                } else if (mQuartile == 1) {
                                    AdViewUtils.logInfo("Video at first quartile: ("
                                            + percentage + "%)");
                                    processEvent(TRACKING_EVENTS_TYPE.firstQuartile);
                                } else if (mQuartile == 2) {
                                    AdViewUtils.logInfo("Video at midpoint: (" + percentage
                                            + "%)");
                                    processEvent(TRACKING_EVENTS_TYPE.midpoint);
                                } else if (mQuartile == 3) {
                                    AdViewUtils.logInfo("Video at third quartile: ("
                                            + percentage + "%)");
                                    processEvent(TRACKING_EVENTS_TYPE.thirdQuartile);
//                        stopQuartileTimer();
                                }
                                mQuartile++;
                            }
                        }
                    }
                    ArrayList<VASTIcon> vastIcons = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVastIcons();
                    if (null != vastIcons && vastIcons.size() > 0) {
                        for (int i = 0; i < vastIcons.size(); i++) {
                            VASTIcon vastIcon = vastIcons.get(i);

                            if (!TextUtils.isEmpty(vastIcon.getDuration())) {
                                if (currentVideoPlayTime > ((Integer.valueOf(vastIcon.getDuration()) + Integer.valueOf(vastIcon.getOffset())) * 1000)) {
                                    if (null != findViewById(ICONS_ID_HEADER + i) && findViewById(ICONS_ID_HEADER + i).isShown()) {
                                        Message msg = new Message();
                                        msg.what = STATUS_VISIBLE_CHANGE_MESSAGE;
                                        msg.arg2 = ICONS_ID_HEADER + i;
                                        msg.arg1 = View.GONE;
                                        handler.sendMessage(msg);
                                        continue;
                                    }
                                }
                                if (!TextUtils.isEmpty(vastIcon.getOffset())) {
                                    if (currentVideoPlayTime > (Integer.valueOf(vastIcon.getOffset()) * 1000) &&
                                        currentVideoPlayTime < ((Integer.valueOf(vastIcon.getDuration()) + Integer.valueOf(vastIcon.getOffset())) * 1000)) {
                                        if (null != findViewById(ICONS_ID_HEADER + i) && !findViewById(ICONS_ID_HEADER + i).isShown()) {
                                            Message msg = new Message();
                                            msg.what = STATUS_VISIBLE_CHANGE_MESSAGE;
                                            msg.arg2 = ICONS_ID_HEADER + i;
                                            msg.arg1 = View.VISIBLE;
                                            handler.sendMessage(msg);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!isVideo) {
                        if (null == closeView) {
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (noSkip && !isVPAID()) {
                                        hideProgressBar();
                                        processEvent(TRACKING_EVENTS_TYPE.complete);
                                        switchPlay(true);
                                    }
                                }
                            });
                        }
                        if (currentVideoPlayTime > duration) {
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(!isVPAID()) {
                                        currentVideoPlayTime = 0;
                                        holdOnTime = 0;
                                        processEvent(TRACKING_EVENTS_TYPE.complete);
                                        switchPlay(false);
                                    }
                                }

                            });
                        }
                        if (noSkip)
                            return;

                        if (mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoType().
                                matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX) ){
                            if (!isScaled && null == findViewById(COUNTDOWN_VIEW_ID)) {

                                int w = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoWidth();
                                int h = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoHeight();
                                if (w == 0 || h == 0) {
                                    desiredWidth = screenWidth;
                                    desiredHeight = screenHeight;
                                }
                                isScaled = true;
                                Message msg = new Message();
                                msg.what = STATUS_SIZE_CHANGE_MESSAGE;
                                msg.obj = "w=-1&h=-1";
                                handler.sendMessage(msg);
                            }
                        }
                        if (null != countDownView) {
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int duration = mVastModel.get(adCount).getCreativeList().get(creativeCount).getDuration() * 1000;
                                    hideProgressBar();
                                    if (null != findViewById(SKIP_TEXT_ID) && !findViewById(SKIP_TEXT_ID).isShown())
                                        showSkipText(((float) currentVideoPlayTime / 1000) + "");
                                    countDownView.updateContent((duration - currentVideoPlayTime) / 1000 + "");
                                    countDownView.updateProgress((int) (currentVideoPlayTime / (float) duration * 360));
                                }
                            });
                        }
                    } else {
                        if (isHoldOn || TextUtils.isEmpty(lastVideoPlayTime)) {
                            holdOnTime += QUARTILE_TIMER_INTERVAL;
                            if (holdOnTime > WAITTIMEOUT) {
                                holdOnTime = 0;
                                hasError = true;
                                currentVideoPlayTime = 0;
                                handler.sendEmptyMessage(STATUS_ERROR_MESSAGE);
                                return;
                            }
                        } else {
                            holdOnTime = 0;
                        }
                    }
                    currentVideoPlayTime += QUARTILE_TIMER_INTERVAL;

                }
            }, 0, QUARTILE_TIMER_INTERVAL);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void stopQuartileTimer() {
        try {
            if (mTrackingEventTimer != null) {
                mTrackingEventTimer.cancel();
                mTrackingEventTimer = null;
                mQuartile = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processErrorEvent() {
        AdViewUtils.logInfo("entered processErrorEvent");
        try {
            List<String> errorUrls = mVastModel.get(adCount).getErrorUrl();
            fireUrls(errorUrls, KyAdBaseView.getHK_Values(mContext, -1, -1, false, true, getVideoSnap()));
            reportWrapperEvents(ERROR_WRAPPER_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processEvent(TRACKING_EVENTS_TYPE eventName) {
        AdViewUtils.logInfo("entered Processing Event: " + eventName);
        try {
            if (null != mTrackingEventMap && null != mTrackingEventMap.get(adCount)) {
                if (adCount >= mTrackingEventMap.size()
                        || creativeCount >= mTrackingEventMap.get(adCount).size()
                        || null == mTrackingEventMap.get(adCount).get(creativeCount)
                        || null == mTrackingEventMap.get(adCount).get(creativeCount).get(eventName)) {
                    AdViewUtils.logInfo("entered Processing Event: " + eventName + " has no address,returned[" + adCount + "," + creativeCount + "]");
                    return;
                }
                List<String> urls = mTrackingEventMap.get(adCount).get(creativeCount).get(eventName);
                //fire vast events
                fireUrls(urls,
                        KyAdBaseView.getHK_Values(mContext, -1, -1,eventName.equals(TRACKING_EVENTS_TYPE.complete),false, getVideoSnap()));
                //report adview events
                reportWrapperEvents(TRACKING_WRAPPER_EVENT, eventName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bundle getVideoSnap() {
        Bundle bundle = new Bundle();
        bundle.putInt("desireWidth", desiredWidth);
        bundle.putInt("desireHeight", desiredHeight);
        bundle.putInt("duration", mVastModel.get(adCount).getCreativeList().get(creativeCount).getDuration());
        bundle.putInt("lastPauseVideoTime", lastPauseVideoTime);
        bundle.putInt("currentVideoPlayTime", currentVideoPlayTime);
        return bundle;
    }

    private void fireUrls(List<String> urls, HashMap<String, String> map) {
        AdViewUtils.logInfo("entered fireUrls");
        if (urls != null) {
            for (String url : urls) {
                Log.i("fireUrls", "url:" + url);
                if (null != url) {
                    url = KyAdBaseView.replace4GDTKeys(url, map);
                    if (null == KyAdBaseView.reqScheduler || KyAdBaseView.reqScheduler.isTerminated()) {
                        KyAdBaseView.reqScheduler = Executors.newScheduledThreadPool(ConstantValues.REQUEST_THREADPOOLNUM);
                    }
                    KyAdBaseView.reqScheduler.execute(new ClientReportRunnable("", url, "GET"));
                }
            }
        } else {
            AdViewUtils.logInfo("url list is null");
        }
    }

//    public JSONObject getClickArea(int x, int y, boolean isRelative) {
//        if (x == -1 || y == -1)
//            return new JSONObject();
//        JSONObject jsonObject = new JSONObject();
//        int width = desiredWidth;
//        int height = desiredHeight;
//        try {
//            jsonObject.put("down_x", "" + (x > width ? -999 : (isRelative ? (x * 1000 / width) : x)));
//            jsonObject.put("down_y", "" + (y > height ? -999 : (isRelative ? (y * 1000 / height) : y)));
//            jsonObject.put("up_x", "" + (x > width ? -999 : (isRelative ? (x * 1000 / width) : x)));
//            jsonObject.put("up_y", "" + (y > height ? -999 : (isRelative ? (y * 1000 / height) : y)));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new JSONObject();
//        }
//        return jsonObject;
//    }


    private void  finishActivity() {
        AdViewUtils.logInfo("=========  AdVASTView(): finishActivity()  ======");
        if(mACVpaidCtrl != null) {
            mACVpaidCtrl.dismiss();
        }
        //VASTPlayer.sendLocalBroadcast(mContext, VASTPlayer.ACTION_VASTDISMISS, null);
        if( mPlayer != null) {
            mPlayer.sendDismiss();
            mIsWaitingForWebView = false;
        }

        if(!isEmbed) {
            ((Activity)mContext).finish();
        }
        //else {
            //view mode
        onCloseBtnClicked();
        //}
    }

    /*(wilder 2019) this means parse xml and build a DOM tree for xml */
    /*
    public AdAdapterManager video_handlerAd(Context context, AdsBean adsBean, boolean isBid, int times, AgDataBean agDataBean, Bundle bdle) {

        Bundle bundle = new Bundle();
        this.adsBean = adsBean;
        this.passBundle = bdle;

        if( bdle != null) {
            this.autoCloseAble = bdle.getBoolean("closeable");
            this.videoOrientation = bdle.getInt("vastOrientation");
            this.trafficWarnEnable = bdle.getBoolean("trafficWarnEnable");
            this.bgColor = bdle.getString("bgColor");
        }

//        if (isBid) {
//            bundle.putString("aggsrc", "9999");
//            bundle.putSerializable("adsBean", adsBean);
//            bundle.putBoolean("isPaster", false);
//        } else {
//            bundle.putString("aggsrc", agDataBean.getAggsrc());
//            bundle.putString("appId", agDataBean.getResAppId());
//            bundle.putString("posId", agDataBean.getResPosId());
//        }

        bundle.putBoolean("closeable", autoCloseAble);
        bundle.putInt("vastOrientation", videoOrientation);
        bundle.putBoolean("trafficWarnEnable", trafficWarnEnable);
        bundle.putString("bgColor", bgColor.equals("#undefine") ? "#000000" : bgColor);

        adVideoAdapterManager = AdAdapterManager.initAd(context, ConstantValues.VIDEOTYPE, bundle.getString("aggsrc"));
        adVideoAdapterManager.setVideoCallback(this);
        adVideoAdapterManager.handleAd(context, bundle);
        if (!isBid) {
            adVideoAdapterManager.setTimeoutListener(times, agDataBean);
        }

        return adVideoAdapterManager;
    }
*/

    private void showProgressBar() {
//        isTouchable = false;
        if (null != mProgressBar && !mProgressBar.isShown())
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
//        isTouchable = true;
        if (null != mProgressBar && mProgressBar.isShown())
            mProgressBar.setVisibility(View.GONE);
    }

    private final static int COMPANION_TYPE = 1;
    private final static int ICON_TYPE = 2;
    private final static int WRAPPER_TYPE = 10;
    private final static int ICONS_ID_HEADER = 610001;
    private final static int COMPANIONS_ID_HEADER = 710001;
    private final static int X = 1;
    private final static int Y = 2;

    private void createBehavedView(ArrayList list, int type, int count) {

        for (int i = 0; i < list.size(); i++) {
            int tag = i;
            String url = "";
            //int behaveWidth = 0, behaveHeight = 0;
            String xOffset = "0", yOffest = "0";
            int viewId = 0;
            boolean isVisible = true;
            Object object = null;
            if (beHavedNum > 6) {
                //max item only can be 6
                break;
            }
            switch (type) {
                case WRAPPER_TYPE + COMPANION_TYPE:
                    tag = count + i;
                case COMPANION_TYPE:
                    viewId = COMPANIONS_ID_HEADER + i;
                    VASTCompanionAd vastCompanionAd = (VASTCompanionAd) list.get(i);
                    if (null == vastCompanionAd || vastCompanionAd.getHeight() == null || vastCompanionAd.getWidth() == null) {
                        allBehavedCounts -= 1;  //reduce all counts, for layout pos
                        continue;
                    }
                    object = vastCompanionAd;
                    break;
                case WRAPPER_TYPE + ICON_TYPE:
                    tag = count + i;
                case ICON_TYPE:
                    VASTIcon vastIcon = (VASTIcon) list.get(i);
                    if (null == vastIcon || vastIcon.getHeight() == null
                            || vastIcon.getWidth() == null
                            || vastIcon.getXPosition() == null
                            || vastIcon.getYPosition() == null) {

                        allBehavedCounts -= 1;  //reduce all counts, for layout pos
                        continue;
                    }

                    viewId = ICONS_ID_HEADER + i;
                    object = vastIcon;

                    if (!TextUtils.isEmpty(vastIcon.getOffset())) {
                        if (Integer.valueOf(vastIcon.getOffset()) > 0) {
                            isVisible = false;
                        }
                    }
                    break;
            }

            if (null != findViewById(viewId)) {
                //mRootLayout.removeView(findViewById(viewId));
                //(wilder 2019) already created
                break;
            }

            CustomWebview behavedWebview = new CustomWebview(mContext);
            behavedWebview.setTouchEventEnable(true);
            behavedWebview.setId(viewId);
            behavedWebview.setCustomClickInterface(this);
            behavedWebview.setTag(tag);
            behavedWebview.setType(type);
            behavedWebview.setBackgroundColor(Color.TRANSPARENT); //wilder 2019
            behavedWebview.setWebViewClient(new VideoWebClient());

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(behavedWidth, behavedHeight);
            layoutParams.leftMargin = convertBehavedXPostion(beHavedNum, allBehavedCounts); //convertBehavedPostion(xOffset + "", +behaveWidth, 1);
            layoutParams.topMargin = convertBehavedYPostion(beHavedNum);//convertBehavedPostion(yOffest + "", +behaveHeight, 2);

            getResourceValue(object, behavedWebview);

            if (!isVisible) {
                behavedWebview.setVisibility(View.INVISIBLE);
            }

            mRootLayout.addView(behavedWebview, layoutParams); //wilder 2019 changed
            beHavedNum++;
        }
    }

    private void getResourceValue(Object object, WebView webView) {
        if (null == webView || null == object)
            return;
        if (object instanceof VASTIcon) {
            if (!TextUtils.isEmpty(((VASTIcon) object).getStaticValue())) {
                if (!TextUtils.isEmpty(((VASTIcon) object).getValueType())) {
                    if (((VASTIcon) object).getValueType().contains("javascript")) {
                        //webView.loadData("<script>" + ((VASTIcon) object).getStaticValue() + "</script>", "text/html", "utf-8");
                        KyAdBaseView.loadWebScript(webView, "<script>" + ((VASTIcon) object).getStaticValue() + "</script>");
                    }
                    else {
                        KyAdBaseView.loadWebContentURL(webView, ((VASTIcon) object).getStaticValue(), "");
                    }
                } else {
                    KyAdBaseView.loadWebContentURL(webView, ((VASTIcon) object).getStaticValue(), "");
                }

            } else if (!TextUtils.isEmpty(((VASTIcon) object).getHtmlValue())) {
                if (((VASTIcon) object).getHtmlValue().startsWith("http")||((VASTIcon) object).getHtmlValue().startsWith("https")) {
                    webView.loadUrl(((VASTIcon) object).getHtmlValue());
                }
                else {
                    webView.loadData(((VASTIcon) object).getHtmlValue(), "text/html", "utf-8");
                }
            } else if (!TextUtils.isEmpty(((VASTIcon) object).getiFrameValue())) {
                String iFrame = ((VASTIcon) object).getiFrameValue();
                if (iFrame.startsWith("http") || iFrame.startsWith("https")) {
                    webView.loadUrl(((VASTIcon) object).getiFrameValue());
                }
                else {
                    webView.loadData(((VASTIcon) object).getiFrameValue(), "text/html", "utf-8");
                }
            }
        } else if (object instanceof VASTCompanionAd) {    //companion ads
            if (!TextUtils.isEmpty(((VASTCompanionAd) object).getStaticValue())) {
                //static resource
                if (!TextUtils.isEmpty(((VASTCompanionAd) object).getValueType())) {
                    if (((VASTCompanionAd) object).getValueType().contains("javascript")) {
                        KyAdBaseView.loadWebScript(webView, "<script>" + ((VASTCompanionAd) object).getStaticValue()  + "</script>");
                    }
                    else {
                        KyAdBaseView.loadWebContentURL(webView, ((VASTCompanionAd) object).getStaticValue(), "");
                    }
                } else {
                    KyAdBaseView.loadWebContentURL(webView, ((VASTCompanionAd) object).getStaticValue(), "");
                }

            } else if (!TextUtils.isEmpty(((VASTCompanionAd) object).getHtmlValue())) {
                //html resource
                String htmlValue = ((VASTCompanionAd) object).getHtmlValue();
                if (htmlValue.startsWith("http") || htmlValue.startsWith("https")) {
                    webView.loadUrl(((VASTCompanionAd) object).getHtmlValue());
                }
                else {
                    webView.loadData(((VASTCompanionAd) object).getHtmlValue(), "text/html", "utf-8");
                }
            } else if (!TextUtils.isEmpty(((VASTCompanionAd) object).getiFrameValue())) {
                //iFrameResource
                String iFrame = ((VASTCompanionAd) object).getiFrameValue();
                if (iFrame.startsWith("http") || iFrame.startsWith("https")){
                    webView.loadUrl(((VASTCompanionAd) object).getiFrameValue());
                }
                else {
                    webView.loadData(((VASTCompanionAd) object).getiFrameValue(), "text/html", "utf-8");
                }
            }
        }
    }

    private void removeIcons() {
        int count = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVastIcons().size();
        for (int i = 0; i < count; i++) {
            View tempView = findViewById(ICONS_ID_HEADER + i);
            if (null == tempView)
                continue;
            Log.i("removeIcons", "removeIcons");
            mRootLayout.removeView(tempView);
        }
        beHavedNum = 0;
        allBehavedCounts = 0;
    }

    private void removeCompanions() {
        int count = mVastModel.get(adCount).getCompanionAdList().size();
        for (int i = 0; i < count; i++) {
            View tempView = findViewById(COMPANIONS_ID_HEADER + i);
            if (null == tempView)
                continue;
            Log.i("removeCompanions", "removeCompanions");
            mRootLayout.removeView(tempView);
        }
        beHavedNum = 0;
        allBehavedCounts = 0;
    }

    private int convertBehavedXPostion(int num, int allNum) {
        int x_offset = 5; //every item view's caps
        Rect visibleRect = new Rect();
        this.getLocalVisibleRect(visibleRect);   //relative to parent's position
        int x = 0;
        //got start x pos
        for (int i = 0; i < allNum; i++) {
            int total_w = allNum * behavedWidth;
            x = (visibleRect.width() - total_w) /2 ;
            if (x < 0) {
                x = 0;
            }
            break;
        }
        //got detailed x pos for which num
        x = x + num * behavedWidth + x_offset;
        return x;
    }

    private int convertBehavedYPostion(int num) {
        Rect visibleRect = new Rect();
       // mRootLayout.getGlobalVisibleRect(visibleRect); //wilder 2019 changed to root relative
        this.getLocalVisibleRect(visibleRect);
        int behavedHeight = visibleRect.height() / 5;

        return visibleRect.bottom - behavedHeight;
    }

    public void onDestroy() {
        // TODO Auto-generated method stub
        //super.onDestroy();
        try {
            this.stopQuartileTimer();
            cleanUpMediaPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }

    private boolean isReady = false;
    private boolean isPlaying, isProcessing;

    public void playVideo(Context context) {
        AdViewUtils.logInfo("isReady=" + isReady + ";isProcessing=" + isProcessing);
        if (isPlaying) {
            AdViewUtils.logInfo("video is playing");
            return;
        }
        if (!isReady) {
            AdViewUtils.logInfo("video is not ready");
            return;
        }
        if (null != adVideoAdapterManager) {
            adVideoAdapterManager.playVideo(context);
        }
//        isPlaying = true;
    }

    //real start play video or contents
    public void startVastShow() {
        if (adVideoAdapterManager != null) {
            //VASTPlayer mPlayer = ((AdBIDVideoAdapter) adVideoAdapterManager).getVastPlayer();
            this.startVastShow(mPlayer);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////VPAID  by wilder 2019/////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    private void getVPAIDad(){
        //String vpaidjs = test_loadVPAIDXML("assets/vpaid-js.js");
        //injectJavaScript("mraid.fireReadyEvent();");
        //String getvpaidJS = "getVPAIDAd()";
        //callJS({'AdParameters':'{"videos":[{"url":"http://techslides.com/demos/sample-videos/small.mp4","mimetype":"video/mp4"}],"overlay":"http://ryanthompson591.github.io/vpaidExamples/img/ClickMe.jpg"}'});
        String getvpaidJS = "callJS(" + "{'AdParameters':'" + mAdParams + "'}" + ")";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //String url = "javascript:" + methodName + "(" + jsonParams + ");
            //contentWebView.loadDataWithBaseURL(null, "<html></html>", "text/html", "UTF-8", null);
            contentWebView.evaluateJavascript(getvpaidJS, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                    Object o = value;
                    //Class c = value.getClass();
                    //String na = c.getName();

                    Log.i(AdViewUtils.ADVIEW, value.getClass().getName());
                }
            });
        } else {
            contentWebView.loadUrl("javascript:" + getvpaidJS);
        }
    }

    @Override
    public void vpaid_onPrepared(){
        //got vpaidAdLoaded
        AdViewUtils.logInfo("++++AdVASTView: onPrepare() ++++++");
        if(mACVpaidCtrl != null) {
            //(wilder 2019) remember :
            //UI action
            // can not
            // be in callback with JS
            int w = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoWidth();
            int h = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoHeight();
            if (w == 0 || h == 0) {
                desiredWidth = screenWidth;
                desiredHeight = screenHeight;
            }

            Message msg = new Message();
            msg.what = STATUS_VPAID_ADSTART_VIEW;
            msg.obj = "w=-1&h=-1";
            handler.sendMessage(msg);

            //hideProgressBar();
            //mACVpaidCtrl.setVolume(50);  //set volume normal
            mACVpaidCtrl.playAd();
        }
    }

    @Override
    public void vpaid_openUrl(@Nullable String url){
        if(!TextUtils.isEmpty(url)) {
            //if has url ,it should be opened in landing page
            infoClicked(url,
                    mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(), 0, 0);
        }else {
            //fire clickthrough Event or clickTracking events
            infoClicked(mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickThrough(),
                    mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(), 0, 0);
        }
    }

    @Override
    public void vpaid_setVolume(int vol){
        if(mACVpaidCtrl != null) {
            mACVpaidCtrl.setVolume(vol);
        }
    }

    @Override
    public void vpaid_dismiss() {

    }

    @Override
    public void vpaid_setSkippableState(boolean skippable){
        //got skippable state result
        //show UI
        Message msg = new Message();
        msg.what = STATUS_VPAID_SKIPBUTTON_SHOW;
        msg.obj = skippable;
        handler.sendMessage(msg);
    }

    @Override
    public void vpaid_setDurationTime(int result) {
        AdViewUtils.logInfo("===== total time is :  " + result + "====");
        Message msg = new Message();
        msg.what = STATUS_TOTALTIME_MESSAGE;
        msg.obj = String.valueOf(result);
        handler.sendMessage(msg);
    }

    @Override
    public void vpaid_fireEvent(String type, String value){
        AdViewUtils.logInfo( "+++++ AdVASTView:fireEvent():type = " + type + ",value = " + value + "+++++");

        if (type.equalsIgnoreCase(EventConstants.START)) {
            processEvent(TRACKING_EVENTS_TYPE.start);

        }else if (type.equalsIgnoreCase(EventConstants.PROGRESS)){
            AdViewUtils.logInfo("=== play progress : " + value + " ==== ");
            Message msg = new Message();
            msg.obj = value;
            msg.what = STATUS_TIME_MESSAGE;
            handler.sendMessage(msg);

        }else if ((type.equalsIgnoreCase(EventConstants.COMPLETE))) {
            Message msg = new Message();
            msg.obj = value;
            msg.what = STATUS_END_MESSAGE;
            handler.sendMessage(msg);

        }else if ((type.equalsIgnoreCase(EventConstants.CLOSE))) {
            processEvent(TRACKING_EVENTS_TYPE.close);
        }else if ((type.equalsIgnoreCase(EventConstants.FIRST_QUARTILE))) {
            processEvent(TRACKING_EVENTS_TYPE.firstQuartile);
        }else if ((type.equalsIgnoreCase(EventConstants.MIDPOINT))){
            processEvent(TRACKING_EVENTS_TYPE.midpoint);
        }else if ((type.equalsIgnoreCase(EventConstants.THIRD_QUARTILE))){
            processEvent(TRACKING_EVENTS_TYPE.thirdQuartile);
        }else if ((type.equalsIgnoreCase(EventConstants.MUTE))) {
            //processEvent(TRACKING_EVENTS_TYPE.mute);
            handler.sendEmptyMessage(STATUS_MUTE_MESSAGE);
        }else if ((type.equalsIgnoreCase(EventConstants.UNMUTE))){
            //processEvent(TRACKING_EVENTS_TYPE.unmute);
            handler.sendEmptyMessage(STATUS_UNMUTE_MESSAGE);
        }else if ((type.equalsIgnoreCase((EventConstants.SKIP)))) {

            processEvent(TRACKING_EVENTS_TYPE.skip);

        }else if ((type.equalsIgnoreCase((EventConstants.VOLUME)))) {
            //js set volume callback
            AdViewUtils.logInfo("===== onGetVolume() : " + value + " ===== ");
            Message msg = new Message();
            msg.obj = value;
            msg.what = STATUS_VPAID_VOLUME_STATUS;
            handler.sendMessage(msg);

        }else if ((type.equalsIgnoreCase((EventConstants.ERROR)))) {
            if (handler != null) {
                handler.sendEmptyMessage(STATUS_ERROR_MESSAGE);
            }
        }else if ((type.equalsIgnoreCase((EventConstants.SELF_CLOSE)))) {
            finishActivity();
        }else {


        }

    }
    ///end wilder for VPAID ///////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //--------------------------------------------------------------------------------------------
    //-------------------------------------- KyVideoListener --callbed by video adapter-----------
    //--------------------------------------------------------------------------------------------
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onVideoClicked(AgDataBean agDataBean) {
        try {
            if (null != agDataBean && null != agDataBean.getCliUrls())
                KyAdBaseView.reportOtherUrls(agDataBean.getCliUrls());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoPlayFinished(AgDataBean agDataBean) {
        isPlaying = false;
        isProcessing = false;

        hideProgressBar();
        try {
            if (null != adAppInterface) {
                adAppInterface.onVideoFinished();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoPlayStarted(AgDataBean agDataBean) {
        isPlaying = true;
        try {
            if (null != agDataBean && null != agDataBean.getImpUrls())
                KyAdBaseView.reportOtherUrls(agDataBean.getImpUrls());
            if (null != adAppInterface) {
                adAppInterface.onVideoStartPlayed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoReceived(String vast) {
        AdViewUtils.logInfo("===AdVASTView== onVideoReceived()  ===== ");
        if (null != adAppInterface) {
            adAppInterface.onReceivedVideo(vast);
        }
    }

    @Override
    public void onDownloadCancel() {
        isProcessing = false;
        isReady = false;
    }

    @Override
    public void onVideoPlayReady (Bundle bundle) {
        //wilder 2019 , start vast ad play
        startVastShow();
    }

    //------------------------- end for KyVideoListener -----------------------
    /////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    //------------------ KyBaseListener  -----------------------------------------
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onReady(AgDataBean agDataBean, boolean force) {
        isProcessing = false;
        isReady = true;
        AdViewUtils.logInfo("isReady=" + isReady + ";isProcessing=" + isProcessing);

        //notify caller view , banner or video activity
        if (null != adAppInterface) {
            adAppInterface.onVideoReady();
        }

        //wilder 2019 play it directly, maybe should play by user?
        if (AdViewUtils.VideoAutoPlay) {
            Message message = new Message();
            message.what = STATUS_PLAY_VIDEO_MESSAGE;
            handler.sendMessage(message);
        }
    }

    @Override
    public void onReceived(AgDataBean agDataBean, boolean force) {
        //使用onVideoReceived（String vast）
        AdViewUtils.logInfo("AdVastView::onReceived()");
    }

    @Override
    public void onAdFailed(AgDataBean agDataBean, String error, boolean force) {
        try {
            if (null != agDataBean && null != agDataBean.getFailUrls())
                KyAdBaseView.reportOtherUrls(agDataBean.getFailUrls());
            int times = KyAdBaseView.getAgDataBeanPosition(adsBean, agDataBean);
            if (times != -1) {
                //adVideoAdapterManager = handlerAd(getContext(), adsBean, false, times, agDataBean, this.passBundle);
                return;
            } else {
                isProcessing = false;
                isReady = false;

                if (null != adAppInterface) {
                    adAppInterface.onFailedReceivedVideo( "____video load ad error____");
                }
            }
        } catch (Exception e) {
            isProcessing = false;
            isReady = false;
            e.printStackTrace();
        }
        //if still no float UI, should show it ,then user can close it
        hideProgressBar();
        createActionView(REPLAY_VIEW_ID, LT_POSITION, true);  //wilder 2019 for view
        createActionView(CLOSE_VIEW_ID, RT_POSITION, true);    //wilder 2019 for view
    }

    @Override
    public void onDisplay(AgDataBean agDataBean, boolean force) {
        if (null != adAppInterface) {
            adAppInterface.onVideoStartPlayed();
        }
    }

    @Override
    public void onCloseBtnClicked() {
        isPlaying = false;
        isReady = false;
        if (null != adAppInterface) {
            adAppInterface.onVideoClosed();
        }
        if (!isEmbed) {
            AdVideoBIDView.getInstance(mContext).resetState();
        }
    }
    @Override
    public void rotatedAd(Message msg) {
        try {
            Message msgCopy = Message.obtain(msg);
            if (null == adsBean.getAgDataBeanList()) {
                if (null != adAppInterface)
                    adAppInterface.onFailedReceivedVideo("video request failed");
                return;
            }
            if (msgCopy.arg1 < adsBean.getAgDataBeanList().size()) {
                AgDataBean agDataBean = adsBean.getAgDataBeanList().get(msgCopy.arg1);

            } else {
                if (null != adAppInterface)
                    adAppInterface.onFailedReceivedVideo("video rotated error");

            }
        } catch (Exception e) {
            e.printStackTrace();
            if (null != adAppInterface)
                adAppInterface.onFailedReceivedVideo("video rotated tc error");
        }
    }
    //------------------------end -- KyBaseListener  ---------------
    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////end wilder 2019 for MREC ////////////////////////////////////////////////

    ////////////////////////////vast video part //////////////////////////////////////////////
    private final int START_EVENT_TYPE = 1;
    private final int MIDDLE_EVENT_TYPE = 2;
    private final int END_EVENT_TYPE = 3;
    private final int DURATION_TYPE = 4;
    private final int MEDIA_FILE_TYPE = 5;
    private final int IMPRESSION_TYPE = 6;
    private final int CLICK_THROUGHT_TYPE = 7;
    private final int CLICK_TRACKING_TYPE = 8;
    private final int EXTENSION_TYPE = 9;


    private String START_EVENT_STR = "__START_EVENT__";
    private String MIDDLE_EVENT_STR = "__MIDDLE_EVENT__";
    private String END_EVENT_STR = "__END_EVENT__";
    private String DURATION_STR = "__DURATION__";
    private String MEDIA_FILE_STR = "__MEDIAFILE__";
    private String IMPRESSION_STR = "__IMPRESSION__";
    private String CLICK_THROUGHT_STR = "__CLICKTHROUGHT__";
    private String CLICK_TRACKING_STR = "__CLICKTRACKING__";
    private String EXTENSION_STR = "__EXTENSION__";


    public void processVastVideo(Context context, Bundle bundle) {
        AdsBean adsBean = (AdsBean) bundle.getSerializable("adsBean");
        try {
            String vast = null;
            try {
                //kvVideoListener = (AdAdapterManager) bundle.getSerializable("interface"); //wilder 2019
                if (adsBean.getXmlType() == 2) {
                    if (bundle.getString("bgColor").equals("#undefine") && !TextUtils.isEmpty(adsBean.getAdBgColor()))
                        bundle.putString("bgColor", adsBean.getAdBgColor());
                    if (adsBean.getVideoBean().isValidBean()) {
                        vast = generalVast(adsBean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(vast)) {
                vast = adsBean.getVastXml();
            }

            if (!TextUtils.isEmpty(vast)) {
                parseVastXml(context, bundle, vast);
            } else {
                //adVideoAdapterManager.onAdFailed("EMPTY BODY");
                onAdFailed(null, "EMPTY BODY", true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public VASTPlayer getVastPlayer() {
//        return newPlayer;
//    }

    private void parseVastXml(Context context, Bundle bundle, String vastStr) {
        Bundle extra = new Bundle();
        extra.putInt("cacheTime", 30);//fix
        extra.putInt("vastOrientation", bundle.getInt("vastOrientation"));
        extra.putString("bgColor", bundle.getString("bgColor"));
        extra.putBoolean("closeable", bundle.getBoolean(""));

        mPlayer = new VASTPlayer(context, extra , (VASTPlayerListener)adVideoAdapterManager);
        mPlayer.loadVideoWithData(vastStr);
    }

    private String generalVast(AdsBean adsBean) {
        VideoBean videoBean = adsBean.getVideoBean();
        String videoVast = Assets.getJsFromBase64(Assets.NATIVEVIDEOVAST);

        videoVast = videoVast.replace(DURATION_STR, vastLinkPart(DURATION_TYPE, videoBean.getDuration() + ""));

        videoVast = videoVast.replace(IMPRESSION_STR, vastEsEc(IMPRESSION_TYPE, adsBean.getExtSRpt()));

        videoVast = videoVast.replace(START_EVENT_STR, vastLinkPart(START_EVENT_TYPE, adsBean.getSpTrackers()));
        videoVast = videoVast.replace(MIDDLE_EVENT_STR, vastLinkPart(MIDDLE_EVENT_TYPE, adsBean.getMpTrackers()));
        videoVast = videoVast.replace(END_EVENT_STR, vastLinkPart(END_EVENT_TYPE, adsBean.getCpTrackers()));

        videoVast = videoVast.replace(CLICK_THROUGHT_STR, vastLinkPart(CLICK_THROUGHT_TYPE, adsBean.getAdLink()));
        videoVast = videoVast.replace(CLICK_TRACKING_STR, vastEsEc(CLICK_TRACKING_TYPE, adsBean.getExtCRpt()));

        videoVast = videoVast.replace(MEDIA_FILE_STR, vastLinkPart(MEDIA_FILE_TYPE, videoBean.getVideoUrl(), videoBean.getHeight() + "", videoBean.getWidth() + ""));


        String[] extensionLinks = new String[7];
        extensionLinks[0] = videoBean.getEndHtml();
        extensionLinks[1] = videoBean.getEndImgUrl();
        extensionLinks[2] = videoBean.getEndIconUrl();
        extensionLinks[3] = videoBean.getEndDesc();
        extensionLinks[4] = videoBean.getEndTitle();
        extensionLinks[5] = videoBean.getEndButtonText();
        extensionLinks[6] = videoBean.getEndButtonUrl();

        videoVast = videoVast.replace(EXTENSION_STR, vastLinkPart(EXTENSION_TYPE, extensionLinks));

        return videoVast;
    }


    private String vastEsEc(int type, HashMap<String, String[]> maps) {
        String temp = "";
        Iterator iterator = maps.keySet().iterator();
        while (iterator.hasNext()) {
            String[] links = maps.get(iterator.next());
            temp = temp + vastLinkPart(type, links);
        }
        return temp;
    }


    private String vastLinkPart(int type, String... links) {
        String temp = "";
        for (String link : links) {
            switch (type) {
                case START_EVENT_TYPE:
                    temp = temp + "<Tracking event=\"start\">\n" +
                            "   <![CDATA[" + link + "]]>" +
                            "</Tracking>";
                    break;
                case MIDDLE_EVENT_TYPE:
                    temp = temp + "<Tracking event=\"midpoint\">\n" +
                            "   <![CDATA[" + link + "]]>" +
                            "</Tracking>";
                    break;
                case END_EVENT_TYPE:
                    temp = temp + "<Tracking event=\"complete\">\n" +
                            "   <![CDATA[" + link + "]]>" +
                            "</Tracking>";
                    break;
                case IMPRESSION_TYPE:
                    temp = temp + "<Impression>\n" +
                            "   <![CDATA[" + link + "]]>\n" +
                            "</Impression>";
                    break;
                case CLICK_THROUGHT_TYPE:
                    temp = temp + "<ClickThrough>\n" +
                            "   <![CDATA[" + link + "]]>\n" +
                            " </ClickThrough>";
                    break;
                case CLICK_TRACKING_TYPE:
                    temp = temp + "<ClickTracking>\n" +
                            "   <![CDATA[" + link + "]]>\n" +
                            "</ClickTracking>";
                    break;
            }
        }

        switch (type) {
            case DURATION_TYPE:
                int duration = Integer.valueOf(links[0]);
                int seconds = duration % 60;
                int minutes = duration / 60;
                int hours = minutes / 24;
                minutes = minutes % 60;
                temp = "<Duration>" + hours + ":" + minutes + ":" + seconds + "</Duration>";
                break;
            case MEDIA_FILE_TYPE:
                temp = "<MediaFile type=\"video/mp4\" width=\"" + links[2] + "\" height=\"" + links[1] + "\">\n" +
                        "        <![CDATA[" + links[0] + "]]>\n" +
                        " </MediaFile>";
                break;
            case EXTENSION_TYPE:
                temp = (TextUtils.isEmpty(links[0]) ? "" : ("<Ky_EndHtml><![CDATA[" + links[0] + "]]></Ky_EndHtml>\n")) +
                        (TextUtils.isEmpty(links[1]) ? "" : "<Ky_EndImage><![CDATA[" + links[1] + "]]></Ky_EndImage>\n") +
                        (TextUtils.isEmpty(links[2]) ? "" : "<Ky_EndIconUrl><![CDATA[" + links[2] + "]]></Ky_EndIconUrl>\n") +
                        (TextUtils.isEmpty(links[3]) ? "" : "<Ky_EndDesc><![CDATA[" + links[3] + "]]></Ky_EndDesc>\n") +
                        (TextUtils.isEmpty(links[4]) ? "" : "<Ky_EndTitle><![CDATA[" + links[4] + "]]></Ky_EndTitle>\n") +
                        (TextUtils.isEmpty(links[5]) ? "" : "<Ky_EndText><![CDATA[" + links[5] + "]]></Ky_EndText>\n") +
                        (TextUtils.isEmpty(links[6]) ? "" : "<Ky_EndLink><![CDATA[" + links[6] + "]]></Ky_EndLink>");
                break;
        }
        return temp;
    }

    ///////////////////////////end vast video ////////////////////////////////////////////////
}
