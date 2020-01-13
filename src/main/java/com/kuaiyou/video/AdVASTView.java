package com.kuaiyou.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.http.SslError;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iab.omid.library.adview.adsession.AdSession;
import com.iab.omid.library.adview.adsession.ErrorType;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.adbid.AdVideoBIDView;
import com.kuaiyou.interfaces.AdViewVideoInterface;
import com.kuaiyou.interfaces.DownloadStatusInterface;
import com.kuaiyou.interfaces.KyVideoListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ExtensionBean;
import com.kuaiyou.obj.ExtensionOMSDKBean;
import com.kuaiyou.utils.AdViewUtils;
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


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    //first frame, 是否提前获取第一帧
    private boolean  useFirstFrameCache = true;

    private float density = 0;
    boolean isGotTTime = false;

    //private boolean autoCloseAble = false;
    private boolean fullVideoOrientation = false;
    //private boolean trafficWarnEnable = true;
    private String bgColor = "#000000";//default is black
    // This is the contents of mraid.js. We keep it around in case we need to
    // inject it
    // into webViewPart2 (2nd part of 2-part expanded ad).
    //////////////////////////////////////////////
    private FullscreenHolder mRootLayout;
    private ProgressBar mProgressBar;
    private CustomWebview contentWebView;
    private WebClientHandler handler = null;
    //wilder 2019 for vpaid
    private AdViewControllerVpaid  mACVpaidCtrl;
    private static String mAdParams;
    private boolean mIsWaitingForWebView;
    //private boolean hasFinalPage = false;
    private int finalPageCompNum = 0;
    //omsdk v1.2
    private AdSession adSession = null;
    private boolean isImpressionTrigged = false;
    private boolean isOMLoaded = false;
    private boolean isOMSDKSupport = false;

    private boolean isReady = false;
    private boolean isPlaying = false;
    private boolean isProcessing = false;

    private AdsBean adsBean;

    public final static int STATUS_PLAY_VIDEO_MESSAGE = 1;
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
    public final static int STATUS_PREPARE_LOAD_VIDEO_MESSAGE = 14; //add by wilder
    public final static int STATUS_OMSDK_INITED = 15;       //wilder 2019 for omsdk
    public final static int STATUS_OMSDK_LOADED = 16; //   omsdk
    public final static int STATUS_OMSDK_SEND_IMPRESSION = 17;
    public final static int STATUS_OMSDK_PLAYBACK_EVENT = 18;
    public final static int STATUS_GET_FIRST_FRAME = 19; //取得第一帧
    //vpaid events
    public final static int STATUS_VPAID_ADSTART_VIEW = 20;
    public final static int STATUS_VPAID_SKIPBUTTON_SHOW = 21;
    public final static int STATUS_VPAID_VOLUME_STATUS = 22;

    private final static int CLICKTRACKING_WRAPPER_EVENT = 1;
    private final static int ERROR_WRAPPER_EVENT = 2;
    private final static int IMPRESSION_WRAPPER_EVENT = 3;
    private final static int TRACKING_WRAPPER_EVENT = 4;

    private Timer mTrackingEventTimer = null;
    //private Timer mOMSDKTimer = null;
    //UI used
    private static final int LT_POSITION = 1, RT_POSITION = 2, LB_POSITION = 3, RB_POSITION = 4, SKIP_POSITION = 5, CENTER_POSITION = 6, FULLSCREEN_POSITION = 7;

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
    private float currentVPAIDVolume = 0;     //wilder 2019 for vpaid，vpaid支持音量大小调节，0-1
    private float currentVastVolume = 1;        //normal vast player ,目前普通video音量只支持开和关

    private final static int WAITTIMEOUT = 20 * 1000;
    private static final long QUARTILE_TIMER_INTERVAL = 250;
    ////////////////// action view ////////////////////
    private static final int ROOT_LAYOUT_ID = 999;
    private static final int ACTION_VIEW_BASE_ID = 10000; //无实际意义，为相对坐标
    private static final int CLOSE_VIEW_ID = 10001;
    private static final int SKIP_TEXT_ID = 10002;
    private static final int COUNTDOWN_VIEW_ID = 10003;
    private static final int VOLUME_VIEW_ID = 10004;
    private static final int REPLAY_VIEW_ID = 10005;
    private static final int ICONBANNER_VIEW_ID = 10006;
    private static final int FINALPAGE_VIEW_ID = 10007;     //最后的尾帧
    //private static final int FIRST_FRAME_VIEW_ID = 10008;   //wilder 2019 ,第一帧显示
    private static final int PLAYER_VIEW_ID   = 10009;      //view上的播放按钮
    private static final int PAUSE_VIEW_ID = 10010;         //暂停
    private static final int FULLSCREEN_VIEW_ID = 10011; //expand to fullscreen

    private static final int ACTION_VIEW_END_ID = 10012; //无实际意义，为终止坐标

    private final static int FINALPAGE_COMPANIONS_ID = 10100;

    ///////////////////////////end action view ///////////////////////////
    private static final String END_LABEL_TEXT = "Skip";
    private Bitmap preVideoImg = null;

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

    private boolean useExpand = true; //1025 test

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

        isOMSDKSupport = false; //omsdk v1.2
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
        createContentView(); //生成content的webview
        if (!isEmbed) {
            getScreenSize(false); //for fullscreen size used
            calcCornerSize();
        }else {
            //wilder 20191024,手动播放模式下，action控件布局修正
            calcCornerSize();
        }
        createProgressBar();
        showProgressBar();
    }

    public void setVideoAppListener(AdViewVideoInterface adInterface) {
        this.adAppInterface = adInterface;
    }

//    public void setAdVideoAdapterManager(AdAdapterManager adpt) {
//        this.adVideoAdapterManager = adpt;
//    }

    //真正去加载视频的html, 至于是否立刻播放，后一个动作要参见 afterOMParametersFromJS()
    private void loadNextVideoContainer() {
        AdViewUtils.logInfo("============== AdVastView::loadNextVideoContainer () ==== real play  & start playing timer ===========");
        currentTotalTime = "0"; //replay的时候还会根据totaltime来判断，所以重新加载页面时要重置
        String mediaType = getCurrentMediaType();
        //这里正经取的video的url，在这之前都没有办法得到vast的数据
        String url = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();
        //video timer start from now
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
            String html = generalContainerHtml(url, mediaType, bgColor);
            if (!TextUtils.isEmpty(html)) {
                if (mediaType.matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX)) {
                    if (url.startsWith("http")||url.startsWith("https"))
                        contentWebView.loadUrl(url);
                    else {
                        //self construction data mode
                        AdViewUtils.loadWebContentExt(contentWebView, url);
//                        contentWebView.loadUrl("javascript:changeBackgroundColor(#ff0000)");
                    }
                }
                else if (mediaType.matches(DefaultMediaPicker.SUPPORTED_JAVASCRIPT_TYPE_REGEX)) {
                    //wilder 2019 for VPAID
                    String vpURL = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVPAIDurl();
                    if (!TextUtils.isEmpty(vpURL)) {
                        AdViewUtils.logInfo("+++ loadNextVideoContainer(): vpURL = " + vpURL + "++++");
                        mIsWaitingForWebView = true;
                        //load creative URL
                        mACVpaidCtrl.setWebView(contentWebView);
                        AdViewUtils.loadVPAIDURL(contentWebView, vpURL);
                    }
                } //end wilder
                else {
                    //video case : include file:// or http: or https 's video file
                    AdViewUtils.loadWebVideoURL(contentWebView, html);
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

        //when start show, screen may be rotated, so it must be recalc corner size
        calcCornerSize();
    }

    /*(wilder 2019) make vast view in webview ，在启动 startLoadingContent()之前是无法使用mVastModel， mplayer等数据的*/
    public void startLoadingContent(VASTPlayer player) {
        if(player == null) {
            return;
        }
        this.mPlayer = player;
        this.mVastModel = player.getVastModel();
        this.wrapperModel = player.getWrapperModel();

        //更新各种extension 和companion,click等各种vast所需要的数据结构
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
//            //wilder 20191101 ,添加第一帧
            if (useFirstFrameCache && !AdViewUtils.videoAutoPlay) {
                Message message = new Message();
                message.what = STATUS_GET_FIRST_FRAME;
                handler.sendMessage(message);
            }else {
                loadNextVideoContainer();
            }
            //reportImpressions(); 20190830改为在start事件中发送展示汇报
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
        if (fullVideoOrientation) {
            DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
            int scrWidth = displayMetrics.widthPixels;
            int scrHeight = displayMetrics.heightPixels;
            cornerSize = scrWidth > scrHeight ? scrHeight / 14 : scrWidth / 14;
        }else {
            cornerSize = screenWidth > screenHeight ? screenHeight / 14 : screenWidth / 14;
        }
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

    //只在播放时会调用这个函数，在JS的onmetadataloaded()或者重新播放时也会调用
    private void fixLayoutSize(int w, int h, boolean hasBehaved) {
        //率先考虑全屏扩展
        if (fullVideoOrientation) {
            DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
            desiredWidth = displayMetrics.widthPixels;
            desiredHeight = displayMetrics.heightPixels;
            //contentWebView.loadUrl("javascript:fixSize(" + desiredWidth  + "," + desiredHeight  + ")");
        }else if (w != -1 && h != -1){
            getDesiredSize(w, h);
            //contentWebView.loadUrl("javascript:fixSize(" + desiredWidth / density + "," + desiredHeight / density + ")");
        }else {
            desiredWidth = screenWidth;
            desiredHeight = screenHeight;
        }
        //这里需要实际去改变video的大小
        contentWebView.loadUrl("javascript:fixSize(" + desiredWidth / density + "," + desiredHeight / density + ")");

        Log.i("[fixLayoutSize]", "fixSize(" + desiredWidth / density + " x " + desiredHeight / density + ")");
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
        //调整webview的大小
        contentWebView.setLayoutParams(layoutParams);
    }

    public void onResume() {
        AdViewUtils.logInfo("======= AdVastView::onResume() --(life cycle event) ======");
        try {
//            showProgressBar();
            if (isPaused) {
                isPaused = false;
                if (isFinished) {
                    return;
                }
                currentVideoPlayTime = lastPauseVideoTime; //wilder 2019 for resume timer
                reportTrackingEvent(TRACKING_EVENTS_TYPE.resume);
                if (isVPAID()) {
                    mACVpaidCtrl.resume();
                }else if (isVideoTypeRelated()) {
                    //启动video的js播放，同时启动counter,这2步缺一不可
                    handler.sendEmptyMessage(STATUS_PLAY_VIDEO_MESSAGE);
                    startVASTQuartileTimer(isVideoTypeRelated(), isFinalMedia());
                }
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
            reportTrackingEvent(TRACKING_EVENTS_TYPE.pause);
            //String mediaType = getCurrentMediaType();
            if (isVPAID()) {
                mACVpaidCtrl.pause();
            }else if (isVideoTypeRelated()) {
                contentWebView.loadUrl("javascript:pauseVideo()");
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

    //加载视频第一帧
    public static Bitmap getNetVideoBitmap(String videoUrl) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUrl, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////// UI 部分 ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //生成包含video的webview,底是rootLayout
    private void createContentView() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        density = displayMetrics.density;

        mRootLayout = new FullscreenHolder(mContext);//new RelativeLayout(mContext);
        mRootLayout.setBackgroundColor(Color.parseColor(bgColor));
        mRootLayout.setId(ROOT_LAYOUT_ID);
        //(wilder 2019) it must be MATCH_PARENT, so view can be fitted  with parent view
        //mRootLayout.setGravity(Gravity.CENTER_VERTICAL); //vertial can make webview center in parent
        //this.addView(mRootLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        this.addView(mRootLayout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        //webview
        contentWebView = new CustomWebview(mContext, true);
        //content webview layout
        LayoutParams  layoutParmars = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParmars.addRule(RelativeLayout.CENTER_IN_PARENT); //居中
        contentWebView.setWebViewClient(new VideoWebClient());
        contentWebView.setWebChromeClient(new VideoChromeClient());
        contentWebView.setCustomInterface(this);

        contentWebView.setLayoutParams(layoutParmars);

        mRootLayout.addView(contentWebView, layoutParmars);
    }

    //开始播放时的UI
    private void createPlayingUI() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isVPAID()) {
                    hideProgressBar();
                }
                if (isFinalMedia() && !isVideoTypeRelated()) {
                    createFinishView(false);
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

                    //createActionView(REPLAY_VIEW_ID, LT_POSITION, true);  //播放的时候不能replay
                    createActionView(PAUSE_VIEW_ID, LT_POSITION, true); //添加pause
                    //createActionView(CLOSE_VIEW_ID, RT_POSITION, true);    //wilder 20191223 根据需求去掉close
                    createActionView(COUNTDOWN_VIEW_ID, LB_POSITION, true);
                    createActionView(SKIP_TEXT_ID, SKIP_POSITION, true);
                }
                isScaled = false;
            }
        }, 100);
    }

    //播放结束时的UI
    private void createFinishView(boolean skip) {
        mRootLayout.removeView(mRootLayout.findViewById(PAUSE_VIEW_ID));
        //createActionView(REPLAY_VIEW_ID, LT_POSITION, true, skip);
        createActionView(CLOSE_VIEW_ID, RT_POSITION, true, skip);
        //加入播放按钮
        //if (!isVPAID())
        if (finalPageCompNum == 0) {
            //endcard 尾帧显示时不显示replay按钮
            createActionView(PLAYER_VIEW_ID, CENTER_POSITION, true, skip);
        }
    }

    /* 另一种生成视频首帧的view的方式，暂时弃用
    private void createFirstFrameView() {
        if (null != mRootLayout.findViewById(FIRST_FRAME_VIEW_ID)) {
            //wilder 2019, when rotated, it will be re-created
            mRootLayout.removeView(mRootLayout.findViewById(FIRST_FRAME_VIEW_ID));
            //return;
        }
        if (null != preVideoImg) {
            Rect visibleRect = new Rect();
            //contentWebView.getGlobalVisibleRect(visibleRect); //only can be used in fullscreen video view
            this.getLocalVisibleRect(visibleRect);   //relative to parent's position

            Log.i("createFirstFrameView", "size = " + visibleRect);
            if (visibleRect.width() == 0 || visibleRect.height() == 0)
                return;
            FrameLayout.LayoutParams viewLayoutParams = new FrameLayout.LayoutParams(cornerSize, cornerSize);
            View view = new ImageView(getContext());;
            //Bitmap bm = null;
            viewLayoutParams.width = visibleRect.width();
            viewLayoutParams.height = visibleRect.height();
            viewLayoutParams.leftMargin = 0;
            viewLayoutParams.topMargin = 0;

            view.setId(FIRST_FRAME_VIEW_ID);
            ((ImageView) view).setImageBitmap(preVideoImg);
            view.setBackground(AdViewUtils.getColorDrawable(getContext(), ConstantValues.UI_VIDEOICON_BG_COLOR, cornerSize));

            view.setOnClickListener(this);
            mRootLayout.addView(view, viewLayoutParams);  //wilder 2019 changed

            View v = mRootLayout.findViewById(PLAYER_VIEW_ID);
            if (null != v) {
                v.bringToFront();
            }
            v = mRootLayout.findViewById(CLOSE_VIEW_ID);
            if (null != v) {
                v.bringToFront();
            }
            //omsdk v1.2
            AddOMObstructions(view);
        }

    }*/

    //建立互动UI层
    private void createActionView(int viewId, int position, boolean needBgColor, boolean skipPosition) {
        if (null != mRootLayout.findViewById(viewId)) {
            //wilder 2019, when rotated, it will be re-created
            mRootLayout.removeView(mRootLayout.findViewById(viewId));
        }
        Rect visibleRect = new Rect();
        //contentWebView.getGlobalVisibleRect(visibleRect); //only can be used in fullscreen video view
        mRootLayout.getLocalVisibleRect(visibleRect);   //relative to parent's position

        Log.i("createActionView", "size = " + visibleRect);
        if (visibleRect.width() == 0 || visibleRect.height() == 0)
            return;
//        FrameLayout.LayoutParams viewLayoutParams = new FrameLayout.LayoutParams(cornerSize, cornerSize);  wilder 20191209
        LayoutParams viewLayoutParams = new LayoutParams(cornerSize, cornerSize);
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
                break;
            case REPLAY_VIEW_ID:
                view = new ImageView(getContext());
                bm = AdViewUtils.getImageFromAssetsFile("replay.png");
                ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(), bm));
                break;
            case VOLUME_VIEW_ID:
                view = new ImageView(getContext());
                if (null == volumeON) {
                    bm = AdViewUtils.getImageFromAssetsFile("unmute.png");
                    volumeON = new BitmapDrawable(getResources(), bm);
                }
                if (null == volumeOFF) {
                    bm = AdViewUtils.getImageFromAssetsFile("mute.png");
                    volumeOFF = new BitmapDrawable(getResources(), bm);
                }
                //((ImageView) view).setImageDrawable(AdViewUtils.isMediaMuted(getContext()) ? volumeOFF : volumeON);
                ((ImageView) view).setImageDrawable(currentVastVolume == 0 ? volumeOFF : volumeON);
                break;
/*            case FIRST_FRAME_VIEW_ID:
                //view = new ImageView(getContext());
                //this.mPlayer = player;
                if (null != mPlayer) {
                    this.mVastModel = mPlayer.getVastModel();
                    //this.wrapperModel = player.getWrapperModel();
                    //String url = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            String url = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();
                            preVideoImg = getNetVideoBitmap(url);
                            //((ImageView) view).setImageBitmap(preVideoImg);
                            createFirstFrameView();
                        }
                    });
                }
                return;*/
            case PLAYER_VIEW_ID:
                view = new ImageView(getContext());
                bm = AdViewUtils.getImageFromAssetsFile("icon_video.png");
                ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(), bm));

                break;
            case PAUSE_VIEW_ID:
                view = new ImageView(getContext());
                bm = AdViewUtils.getImageFromAssetsFile("webview_bar_pause.png");
                ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(), bm));

                break;
            case FULLSCREEN_VIEW_ID:
                if (AdViewUtils.useVideoFullScreen) {
                    view = new ImageView(getContext());
                    bm = AdViewUtils.getImageFromAssetsFile("replay.png");
                    ((ImageView) view).setImageDrawable(new BitmapDrawable(getResources(), bm));
                }
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
                view.setBackgroundColor(Color.parseColor(ConstantValues.UI_VIDEOICON_BG_COLOR));
                view.setVisibility(View.GONE);
                break;
            case CENTER_POSITION:
//                if (viewId == FIRST_FRAME_VIEW_ID) {
//                    viewLayoutParams.width = visibleRect.width();
//                    viewLayoutParams.height = visibleRect.height();
//                    viewLayoutParams.leftMargin = 0;
//                    viewLayoutParams.topMargin = 0;
//                }else
                {
                    viewLayoutParams.width = cornerSize * 4;
                    viewLayoutParams.height = cornerSize * 4;
                    viewLayoutParams.leftMargin = (visibleRect.width() - cornerSize * 4) / 2;
                    viewLayoutParams.topMargin = (visibleRect.height() - cornerSize * 4) / 2;
                }
                break;
            case FULLSCREEN_POSITION:
                viewLayoutParams.leftMargin = visibleRect.right - cornerSize - cornerSize / 8 - 2 * cornerSize;
//                viewLayoutParams.topMargin = cornerSize / 8;
                viewLayoutParams.topMargin = visibleRect.bottom - cornerSize - cornerSize / 8;
                break;
        }
        //添加到rootlayout
        try {
            if (null == view)  //可能不存在
                return;

            if (needBgColor && viewId != SKIP_TEXT_ID) {
                view.setBackground(AdViewUtils.getColorDrawable(getContext(), ConstantValues.UI_VIDEOICON_BG_COLOR, cornerSize));
            }
            view.setOnClickListener(this);
            //view.bringToFront();
            mRootLayout.addView(view, viewLayoutParams);  //wilder 2019 changed
            //omsdk v1.2
            AddOMObstructions(view);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createActionView(int viewId, int position, boolean needBgColor) {
        createActionView(viewId, position, needBgColor, false);
    }

    //更新action view层
    private void updateAllActionView(int width, int height) {

        fixLayoutSize(-1,-1,false); //wilder 20191030
        //要重新计算一下corner的尺寸,全屏和embed模式下尺寸不同
        calcCornerSize();
        //刷新ui
        for (int i = 1; i < ACTION_VIEW_END_ID; i++) {
            int id = ACTION_VIEW_BASE_ID + i;
            View tempView = mRootLayout.findViewById(id);
            if (null == tempView)
                continue;

            int pos = RT_POSITION;
            if (id == CLOSE_VIEW_ID)
                pos = RT_POSITION;
            else if (id == COUNTDOWN_VIEW_ID)
                pos = LB_POSITION;
            else if (id == SKIP_TEXT_ID)
                pos =  SKIP_POSITION;
            else if (id == VOLUME_VIEW_ID)
                pos = RB_POSITION;
            else if (id == REPLAY_VIEW_ID)
                pos = LT_POSITION;
            else if (id == PLAYER_VIEW_ID)
                pos = CENTER_POSITION;
            else if (id == PAUSE_VIEW_ID)
                pos = LT_POSITION;
            else if (id == FULLSCREEN_VIEW_ID)
                pos = FULLSCREEN_POSITION;

            createActionView(id, pos, true); //添加pause
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    private String lastVideoPlayTime = "";
    private boolean isHoldOn = false;

    private void updateCountDown(String content) {
        try {
            CountDownView countDownView = (CountDownView)mRootLayout.findViewById(COUNTDOWN_VIEW_ID);
            //AdViewUtils.logInfo("<<<<<<<<<<<< updateCountDown(): content = " + content + ">>>>>>>>>>>>>");
            if (!content.equals("undefined")) {
                if (lastVideoPlayTime.equals(content))
                    isHoldOn = true;
                else
                    isHoldOn = false;
                if (TextUtils.isEmpty(content) || content.equals("null") || TextUtils.isEmpty(currentTotalTime) || "null".equals(currentTotalTime))
                    return;

                if(isVPAID()) {
                    //对于VPAID而言，content是剩下的时间
                    Float remain = Float.valueOf(content);
                    Float total = Float.valueOf(currentTotalTime);
                    countDownView.updateProgress((int)((total - remain) / total * 360));

                    countDownView.updateContent(String.valueOf(remain.intValue()));

                    lastVideoPlayTime = String.valueOf((int)(total - remain));
                }else {
                    currentVideoPlayTime = (int) (Float.valueOf(content) * 1000);
                    countDownView.updateProgress((int) (Float.valueOf(content) / Float.valueOf(currentTotalTime) * 360));
                    int tempInt = 0;
                    //如果totaltime是0,那就用当前时间递增来取代
                    if (0.0f == Float.valueOf(currentTotalTime).intValue()) {
                        tempInt = Float.valueOf(content).intValue();
                    }else {
                        tempInt = Float.valueOf(currentTotalTime).intValue() - Float.valueOf(content).intValue();
                    }

                    countDownView.updateContent(tempInt + "");
                    lastVideoPlayTime = content;
                    //AdViewUtils.logInfo("<<<<<<<<<<<< updateCountDown(): " + currentVideoPlayTime + ">>>>>>>>>>>>>");
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

        //wilder 2019,暂停时保持原来画面，用于pause
/*        mRootLayout.removeView(findViewById(COUNTDOWN_VIEW_ID));
        mRootLayout.removeView(findViewById(SKIP_TEXT_ID));
        mRootLayout.removeView(findViewById(VOLUME_VIEW_ID));*/
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

    private void doReloadPlay() {
        try {
            mRootLayout.removeView(mRootLayout.findViewById(CLOSE_VIEW_ID));
            mRootLayout.removeView(mRootLayout.findViewById(REPLAY_VIEW_ID));
            VideoLableView v1 = (VideoLableView)mRootLayout.findViewById(ICONBANNER_VIEW_ID);
            VideoFinalPage v2 = (VideoFinalPage)mRootLayout.findViewById(FINALPAGE_VIEW_ID);
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

        loadNextVideoContainer();
    }

    //这里处理的是所有非webview的外挂的view的点击事件，webview页面的点击，见onWebviewClicked()
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
                    if (/*AdViewUtils.isMediaMuted(mContext)*/currentVastVolume == 0) {
                        ((ImageView) v).setImageDrawable(volumeON);
                        handler.sendEmptyMessage(STATUS_UNMUTE_MESSAGE);
                        currentVastVolume = 1;
                        contentWebView.loadUrl("javascript:setVolume(0.5)");  //wilder 2019 change volume
                    } else {
                        ((ImageView) v).setImageDrawable(volumeOFF);
                        handler.sendEmptyMessage(STATUS_MUTE_MESSAGE);
                        currentVastVolume = 0;
                        contentWebView.loadUrl("javascript:setVolume(0)"); //wilder 2019 change volume
                    }
                    //AdViewUtils.setCurrentVolume(mContext, !AdViewUtils.isMediaMuted(mContext)); //wilder 2019 change volume
                }
                //omsdk v1.2 volume change
                if (isOMSDKSupport) {
                    contentWebView.loadUrl("javascript:signalVolumeEvent()");
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
                //omsdk v1.2
                if (null != adSession && isOMSDKSupport) {
                    AdViewUtils.stopOMAdSession(adSession);
                }
                //for omsdk , stop session need  time to trigger, so webview should be destroyed for wait for a while
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishActivity();
                    }
                },100);

                break;
            case PLAYER_VIEW_ID:
                if (isPaused) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //load url must handled in main loop thread
                            try {
                                if (contentWebView != null) {
                                    mRootLayout.removeView(mRootLayout.findViewById(PLAYER_VIEW_ID));
                                    createActionView(PAUSE_VIEW_ID, LT_POSITION, true);
                                    onResume();
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else {
                    //开始播放
                    removePreUI();
                    isFinished = false; //该变量用于在onreume时如果已经停止了，就不用恢复播放
                    if (isVPAID()) {
                        //vpaid直接播放，不需要启动QuartileTimer
                        Float totalTime = Float.valueOf(currentTotalTime);
                        if (totalTime > 0) {
                            //重播，对于vpaid而言因为是js,所以必须重新加载页面开始，否则会认为只是2次start
                            doReloadPlay();
                        }else {
                            //首次播放
                            if (null != mACVpaidCtrl) {
                                Message msg = new Message();
                                msg.what = STATUS_VPAID_ADSTART_VIEW;
                                msg.obj = "w=-1&h=-1";
                                handler.sendMessage(msg);

                                mACVpaidCtrl.playAd();
                            }
                        }
                    }else {
                        handler.sendEmptyMessage(STATUS_PLAY_VIDEO_MESSAGE);
                        startVASTQuartileTimer(isVideoTypeRelated(), isFinalMedia());
                    }
                }
                break;
            case PAUSE_VIEW_ID:
                if (!isPaused) {
//                    mRootLayout.removeView(findViewById(PAUSE_VIEW_ID));
//                    createActionView(PLAYER_VIEW_ID, LT_POSITION, true);
//                    onPause();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //load url must handled in main loop thread
                            try {
                                if (contentWebView != null) {
                                    mRootLayout.removeView(mRootLayout.findViewById(PAUSE_VIEW_ID));
                                    createActionView(PLAYER_VIEW_ID, CENTER_POSITION, true);
                                    onPause();

                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                break;
            case FULLSCREEN_VIEW_ID:
                expandFullScreen();
                break;
            case REPLAY_VIEW_ID:
                doReloadPlay();
                break;

            case FINALPAGE_VIEW_ID:
            case ICONBANNER_VIEW_ID: {
                String clickThru = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickThrough();
                if (!TextUtils.isEmpty(clickThru)) {
                    reportClickEvents(clickThru,
                                            mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(),
                                            0, 0);
                }
                break;

            }
            default:
            break;
        }
    }

    //webview内部的点击事件，含behaved view,但是content view的video的点击不走这里，那是从html页面的click 事件来的，
    //详见shouldOverrideUrlLoading()中的 click事件处理部分
    @Override
    public void onWebviewClicked(int type, int tag) {
        List<String> urlsTrackingList = null;
        String urlThru = "";
        try {
            if (type == ICON_TYPE) {
                //注意tag的作用，这里定位了在companion list中的位置
                IconClicks iconClicks = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVastIcons().get(tag).getIconClicks();
                urlsTrackingList = iconClicks.getClickTracking();
                urlThru = iconClicks.getClickThrough();

            } else if (type == COMPANION_TYPE) {
                //注意tag的作用，这里定位了在companion list中的位置
                CompanionClicks companionClicks = mVastModel.get(adCount).getCompanionAdList().get(tag).getCompanionClicks();
                urlsTrackingList = companionClicks.getClickTracking();
                urlThru = companionClicks.getClickThrough();

            } else if (type == WRAPPER_TYPE + ICON_TYPE) {
                int tmpAdCount = tag / 1000000;
                int tmpCreativeCount = 0;
                int tmpNum = 0;
                tmpCreativeCount = (tag - tmpAdCount * 1000000) / 10000;
                tmpNum = (tag - tmpAdCount * 1000000 - tmpCreativeCount * 10000) % 100;

                IconClicks iconClicks = wrapperModel.get(tmpAdCount).getCreativeList().get(tmpCreativeCount).getVastIcons().get(tmpNum).getIconClicks();
                urlsTrackingList = iconClicks.getClickTracking();
                urlThru = iconClicks.getClickThrough();
            } else if (type == WRAPPER_TYPE + COMPANION_TYPE) {
                int tmpCreativeCount = 0;
                int tmpNum = 0;
                tmpNum = (tag - tmpCreativeCount * 10000) % 100;

                CompanionClicks companionClicks = wrapperModel.get(tmpCreativeCount).getCompanionAdList().get(tmpNum).getCompanionClicks();
                urlsTrackingList = companionClicks.getClickTracking();
                urlThru = companionClicks.getClickThrough();
            } else if (type == FINALPAGE_TYPE + WRAPPER_TYPE) {
                //用于endcard的companion的wrapper模式
                //注意tag的作用，这里定位了在companion list中的位置
                CompanionClicks companionClicks = wrapperModel.get(adCount).getCompanionAdList().get(tag).getCompanionClicks();
                urlsTrackingList = companionClicks.getClickTracking();
                urlThru = companionClicks.getClickThrough();
            }
        }catch ( Exception e) {
            e.printStackTrace();
        }
        //process events
        if (!TextUtils.isEmpty(urlThru)) {
            reportClickEvents(urlThru, urlsTrackingList, 0, 0);
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
                                mRootLayout.removeView(mRootLayout.findViewById(SKIP_TEXT_ID));
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
                            ImageView vw = mRootLayout.findViewById(VOLUME_VIEW_ID);
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
                                removePreUI(); //此时要移除PreUI，例如播放按钮等
                                String[] size = msg.obj.toString().split("&");
                                if (size.length == 2) {
                                    fixLayoutSize(Integer.valueOf(size[0].replace("w=", "")), Integer.valueOf(size[1].replace("h=", "")), false);
                                    createPlayingUI();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATUS_MUTE_MESSAGE:
                    reportTrackingEvent(TRACKING_EVENTS_TYPE.mute);
                    break;

                case STATUS_UNMUTE_MESSAGE:
                    reportTrackingEvent(TRACKING_EVENTS_TYPE.unmute);
                    break;

                case STATUS_END_MESSAGE:
                    reportTrackingEvent(TRACKING_EVENTS_TYPE.complete);
                    switchPlay(false);
                    break;

                case STATUS_PLAY_VIDEO_MESSAGE:  //正式调用js:playvideo()
                    hideProgressBar();
                    if (!isSkipped) {
                        try {
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                               try {
                                     Float totalTime = Float.valueOf(currentTotalTime);
                                     if (totalTime >= 0.0f) {
                                       //replay，重播时因为没有 onloadedmetadata()事件，因此需要重刷size
                                         createPlayingUI();
                                     }
                                     contentWebView.loadUrl("javascript:playVideo()");
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                                }
                            }, 200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case STATUS_TIME_MESSAGE:
                    //正常流程是先启动js的playvideo(),js会在 onloadedmetadata() 之后发送totaltime,同时会定时发送timer事件上来
                    //用于更新counter,但要注意对于android 6.0可能返回的time = 0
                    if (null != mRootLayout.findViewById(COUNTDOWN_VIEW_ID)) {
                        if (null != msg.obj) {
                            updateCountDown((String) msg.obj);
                        }
                    }
                    if (null != mRootLayout.findViewById(SKIP_TEXT_ID)) {
                        if (!isSkippShown)
                            showSkipText((String) msg.obj);
                    }
                    break;

                case STATUS_TOTALTIME_MESSAGE:
                    currentTotalTime = (String) msg.obj;
                    try {
                        if (!TextUtils.isEmpty(currentTotalTime) && !"null".equals(currentTotalTime)) {
                            Float totalTime = Float.valueOf(currentTotalTime);
                            int toTime = 0;
                            if (totalTime == 0.0f) {
                                //android 6.0 有可能返回0，如果用vast里面的duration代替这个totaltime，会不准确，因此在
                                //updateCountDown()里面处理一下显示即可
                                //toTime = mVastModel.get(adCount).getCreativeList().get(creativeCount).getDuration();
                                //currentTotalTime = String.valueOf(toTime);
                                AdViewUtils.logInfo("======= total time is 0,set totaltime to : " + toTime +  "========");
                            }else {
                                toTime = totalTime.intValue();
                            }
                            mVastModel.get(adCount).getCreativeList().get(creativeCount).setDuration(toTime);
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
                            //从<clickthrought>获取点击的落地页landingpage url
                            String clickUri = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickThrough();
                            if (!TextUtils.isEmpty(clickUri)) {
                                reportClickEvents(
                                        clickUri,
                                        mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(),
                                        Float.valueOf(resultMap.get("x")).intValue(),
                                        Float.valueOf(resultMap.get("y")).intValue());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATUS_ERROR_MESSAGE:
                    reportErrorEvent();
                    stopQuartileTimer();
                    switchPlay(false);
                    break;

                case STATUS_VISIBLE_CHANGE_MESSAGE:
                    mRootLayout.findViewById(msg.arg2).setVisibility(msg.arg1);
                    break;

                case STATUS_ICON_BANNER_MESSAGE:
                    try {
                        VideoLableView videoLableView = new VideoLableView(mContext);
                        videoLableView.setId(ICONBANNER_VIEW_ID);
                        videoLableView.setOnClickListener(AdVASTView.this);

                        //FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(desiredWidth - 5 * cornerSize, (int) (desiredHeight / 4.3)); wilder 20191209
                        //lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

                        LayoutParams lp = new LayoutParams(desiredWidth - 5 * cornerSize, (int) (desiredHeight / 4.3));
                        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); //wilder 20191209
                        lp.bottomMargin = (int) (cornerSize / 8);
                        mRootLayout.addView(videoLableView, lp);
                        //omsdk v1.2
                        AddOMObstructions(videoLableView);
                        videoLableView.setData(msg.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATUS_FINAL_PAGE_MESSAGE:
                    if (null != mRootLayout.findViewById(FINALPAGE_VIEW_ID))
                        ((VideoFinalPage) mRootLayout.findViewById(FINALPAGE_VIEW_ID)).setData(msg.getData());
                    break;

                case STATUS_PREPARE_LOAD_VIDEO_MESSAGE:
                    prepareLoadVideo(getContext());
                    break;
                case STATUS_GET_FIRST_FRAME:
                    try {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (!isVPAID())
                                    {
                                        String url = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();
                                        preVideoImg = getNetVideoBitmap(url);
                                    }
                                    //play
                                    loadNextVideoContainer();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    loadNextVideoContainer();
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                    break;
                case STATUS_OMSDK_INITED:
                    //omsdk v1.2isOMLoaded
                    getOMParametersFromJS();
                    break;
                case STATUS_OMSDK_LOADED:
                    setOmsdkLoaded();
                    break;
                case STATUS_OMSDK_SEND_IMPRESSION:
                    contentWebView.loadUrl("javascript:signalImpressionEvent()");
                    break;
                case STATUS_OMSDK_PLAYBACK_EVENT:
                    if (null != msg.obj) {
                        //omsdk v1.2
                        TRACKING_EVENTS_TYPE type = (TRACKING_EVENTS_TYPE)msg.obj;
                        reportOMSDKEvents(type);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * *************** OMSDK v1.2.15****************************
     */

    private void AddOMObstructions(View v) {
        if (null != adSession && isOMSDKSupport) {
            AdViewUtils.AddOMObstructions(v, adSession);
        }
    }

    private void sendOMSDKErr(String info) {
        if (null != adSession) {
            AdViewUtils.signalErrorEvent(adSession, ErrorType.VIDEO, info);
        }
    }
    ////////////////////omsdk end //////////////////////////////////
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

    private void setFullScreen(boolean fullScreen) {
        WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
        if (fullScreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            ((Activity) mContext).getWindow().setAttributes(attrs);
            ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            fullVideoOrientation = true;
        } else {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((Activity) mContext).getWindow().setAttributes(attrs);
            ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            fullVideoOrientation = false;
        }

    }

    private void expandFullScreen() {
        //使用全屏功能，必须在app的manifest.xml中将android:configChanges="keyboardHidden|orientation|screenSize"
        //contentWebView.loadUrl("javascript:launchFullScreen()");//此种方法通过html执行，调用的是video的controls控件
        if (!AdViewUtils.useVideoFullScreen)
            return;
        AdViewUtils.logInfo("===========expandFullScreen()==============");
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("[expandFullScreen]", "横屏");
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            showCustomView2(true);
        }else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("[expandFullScreen]", "竖屏");
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            showCustomView2(false);
        }
    }

    private void showCustomView2(final boolean changeFull) {
            //new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    float density = displayMetrics.density;
                    int scrWidth = displayMetrics.widthPixels;
                    int scrHeight = displayMetrics.heightPixels;
                    FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
                            scrWidth,
                            scrHeight
                            //ViewGroup.LayoutParams.MATCH_PARENT,
                            //ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    FrameLayout decor = (FrameLayout) ((Activity) mContext).getWindow().getDecorView();
                    if (changeFull) {
                        //先移除旧的root layout
                        removeView(mRootLayout);
                        decor.addView(mRootLayout, COVER_SCREEN_PARAMS);
                        setFullScreen(true);
                    } else {
                        decor.removeView(mRootLayout);
                        addView(mRootLayout, new FrameLayout.LayoutParams(
                                            LayoutParams.MATCH_PARENT,
                                            LayoutParams.MATCH_PARENT ));
                        setFullScreen(false);
                    }

                }//end run()
            });
           // }, 500);
    }

    class VideoChromeClient extends WebChromeClient {
        WebChromeClient.CustomViewCallback mCallback;
        View customView;
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

        @Override
        public View getVideoLoadingProgressView() {
            Log.i("ToVmp","--------- getVideoLoadingProgressView ------------ ");
//            if (null != mProgressBar)
//                return mProgressBar;
//            else
                return null;
        }

        @Override
        public Bitmap getDefaultVideoPoster() {  //缺省的post screen
            if (AdViewUtils.videoAutoPlay) {
                return null;
            }else {
                if (!useFirstFrameCache) { //提前取得img视乎效果好些
                    try {
                        String url = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();
                        preVideoImg = getNetVideoBitmap(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //此时将progress销毁
                hideProgressBar();
                return preVideoImg;
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Log.i("ToVmp","--------- onShowCustomView ------------ ");
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            Log.i("ToVmp","--------- onHideCustomView ---------------");
            super.onHideCustomView();
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
            AdViewUtils.logInfo("++++++++++++++ (AdVastView) onPageFinished : url = " + url + "++++++++++++++++");
            if (mIsWaitingForWebView) {
                mACVpaidCtrl.initBridgeWrapper();
                mIsWaitingForWebView = false;
            }
//            //omsdk v1.2
//            adSession = AdViewUtils.startOMAdSessionJS(view);
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
                AdViewUtils.logInfo("===== AdVastView::shouldOverrideUrlLoading(),url =  " + url + "=======");
            }
            if (url.startsWith("mraid://")) {
                Message message = new Message();
                if (url.contains("play")) {
                    //发生在js:playvideo()之后，除非暂停播放，否则无法停止
                    message.what = STATUS_PLAY_VIDEO_MESSAGE;
                }
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
                            AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
                        }
                        AdViewUtils.repScheduler.execute(new AdVASTView.ResourceDownloadRunnable(eb, STATUS_ICON_BANNER_MESSAGE));
                    }
                }else if (url.contains("ominited")) {
                    //omsdk v1.2
                    message.what = STATUS_OMSDK_INITED;
                } else if (url.contains("omLoaded")) {
                    //omsdk v1.2
                    message.what = STATUS_OMSDK_LOADED;
                }

                handler.sendMessage(message);

            } else if (url.startsWith("http")||url.startsWith("https")) {
                reportClickEvents(
                        url,
                        mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(),
                        0, 0);
            }
            return false;
        }
    }

    //用于下载尾帧的数据
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
        //omsdk v1.2,可用来测试obstructions
        AddOMObstructions(mProgressBar);

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
                        loadNextVideoContainer();
                    }
                });
            }
        }

        @Override
        public void onDownloadFailed(int pos, int creativePos, int error) {
            AdViewUtils.logInfo("onDownloadFailed " + error + "   creativePos " + creativePos);
            mVastModel.get(pos).getCreativeList().get(creativePos).setFailed(true);
            reportErrorEvent();
            if (isWaittingDownload) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isWaittingDownload = false;
                        loadNextVideoContainer();
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
            TextView skipText = (TextView)mRootLayout.findViewById(SKIP_TEXT_ID);
            if (Float.valueOf(content) >= skipDuration) {
                isSkippShown = true;
                skipText.setVisibility(View.VISIBLE);
                skipText.setOnClickListener(AdVASTView.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generalContainerHtml(String url, String type, String color) {
        String asstesName = "";
        if (type.matches(DefaultMediaPicker.SUPPORTED_IMAGE_TYPE_REGEX)) {
            asstesName = AdViewUtils.loadAssetsFileByContext("VAST_Image_JS.html", mContext);
        } else if (type.matches(DefaultMediaPicker.SUPPORTED_VIDEO_TYPE_REGEX)) {
            //if (AdViewUtils.canUseOMSDK())
                asstesName = AdViewUtils.loadAssetsFileByContext("VAST_Video_JS.html", mContext);
//            else
//                asstesName = AdViewUtils.loadAssetsFileByContext("VAST_Video_JS-non-omsdk.html", mContext); //这是去除omsdk的html模板
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

    //wilder 2019
    private void removeFinalPageCompanions() {
        int count = finalPageCompNum;
        for (int i = 0; i < count; i++) {
            View tempView = findViewById(FINALPAGE_VIEW_ID  + i);
            if (null == tempView)
                continue;
            //Log.i("removeCompanions", "removeCompanions");
            mRootLayout.removeView(tempView);
        }
        finalPageCompNum = 0;
    }

    private void createFinalPageCompanions(int type, ArrayList<VASTCompanionAd> list) {
        if (!list.isEmpty()) {
            for (int j = 0; j < list.size(); j++) {
                VASTCompanionAd vastCompanionAd = (VASTCompanionAd) list.get(j);
                if (null == vastCompanionAd || vastCompanionAd.getHeight() == null || vastCompanionAd.getWidth() == null) {
                    //allBehavedCounts -= 1;  //reduce all counts, for layout pos
                    continue;
                }else {
                    int width = vastCompanionAd.getWidth().intValue();
                    int height = vastCompanionAd.getHeight().intValue();
                    //if (width >= adsBean.getAdWidth() && height >= adsBean.getAdHeight()) 如果vast的尺寸大于as的尺寸，则可以展示
                    if (width >= 300 && height >= 200) {
                        //int tag = Integer.parseInt(vastCompanionAd.getId());
                        int tag = j; //这个决定了companion在list中的位置，点击时发送clickthrough靠它定位
                        //扩展的behaved view
                        CustomWebview finalPageWebview = new CustomWebview(mContext , false);
                        finalPageWebview.setTouchEventEnable(true);
                        finalPageWebview.setId(FINALPAGE_COMPANIONS_ID + finalPageCompNum);
                        finalPageWebview.setCustomClickInterface(this);
                        finalPageWebview.setTag(tag); //设置id ,用于标识不同的view，这里和companion的id相同
                        finalPageWebview.setType(type); //这个type决定了webview click的动作见 onWebviewClicked()
                        finalPageWebview.setBackgroundColor(Color.TRANSPARENT); //wilder 2019
                        finalPageWebview.setWebViewClient(new VideoWebClient());
                        //注意布局
                        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        //LayoutParams layoutParams = new LayoutParams(width, height);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                            layoutParams.leftMargin = convertBehavedXPostion(beHavedNum, allBehavedCounts); //convertBehavedPostion(xOffset + "", +behaveWidth, 1);
//                            layoutParams.topMargin = convertBehavedYPostion(beHavedNum);//convertBehavedPostion(yOffest + "", +behaveHeight, 2);
                        finalPageWebview.setLayoutParams(layoutParams);
                        //加载companion或icon的内容，一般是一个链接
                        loadBehavedResource(vastCompanionAd, finalPageWebview);
                        finalPageCompNum++; //这里进行计数，用于销毁时的动作
                        mRootLayout.addView(finalPageWebview); //wilder 2019 changed
                    }
                }

            }
        }

    }
    //点击事件
    private void clickEventFinalCompanions(String tag, ArrayList<VASTCompanionAd> list) {
        for (int j = 0; j < list.size(); j++) {
            VASTCompanionAd vastCompanionAd = (VASTCompanionAd) list.get(j);
            //String tag = v.getTag().toString();
            if (vastCompanionAd.getId().equals(tag)) {
                String clickThru = vastCompanionAd.getCompanionClicks().getClickThrough();
                if (!TextUtils.isEmpty(clickThru)) {
                    reportClickEvents(clickThru,
                            vastCompanionAd.getCompanionClicks().getClickTracking(),
                            0, 0);
                    break;
                }
            }
        }
    }
    //end card
    private void showFinalPageCompanion() {
//        if (isEmbed)  //mrec 格式没有endcard
//            return;
        if (AdViewUtils.useVastFinalPage) {
            for (int i = 0; i < mVastModel.size(); i++) {
                //tag += i;
                ArrayList<VASTCompanionAd> companionAdArrayList = mVastModel.get(i).getCompanionAdList();
                createFinalPageCompanions(COMPANION_TYPE, companionAdArrayList);
            }
            //wrapper
            for (int j = 0; j < wrapperModel.size(); j++) {
                ArrayList<VASTCompanionAd> companionAdArrayListWrapper = wrapperModel.get(j).getCompanionAdList();
                createFinalPageCompanions(WRAPPER_TYPE + FINALPAGE_TYPE, companionAdArrayListWrapper);
            }
        }
    }


    private void showFinalPage() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean skipPosition = false;
                    VideoFinalPage videoFinalPage = null;
                    //FrameLayout.LayoutParams layoutParm = null; wilder 20191209
                    LayoutParams layoutParm = null;
                    finalPageCompNum = 0; //默认设为false
                    final ExtensionBean eb = mVastModel.get(adCount).getExtensionBean();
                    if (null != eb) {
                        //普通adview协议的finalpage 是从BID服务器的扩展字段取得相关信息
                        skipPosition = true;
                        videoFinalPage = new VideoFinalPage(mContext);
                        videoFinalPage.setOnClickListener(AdVASTView.this);
                        videoFinalPage.setId(FINALPAGE_VIEW_ID);
                        layoutParm = new LayoutParams(-1, -1);
                        //LayoutParams  layoutParmars = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        //layoutParmars.addRule(RelativeLayout.CENTER_IN_PARENT);
                        if (null == AdViewUtils.repScheduler || AdViewUtils.repScheduler.isTerminated())
                            AdViewUtils.repScheduler = Executors.newScheduledThreadPool(ConstantValues.REPORT_THREADPOOL_NUM);
                        AdViewUtils.repScheduler.execute(new ResourceDownloadRunnable(eb, STATUS_FINAL_PAGE_MESSAGE));
                    }else {
                        //采用companion来作为finalpage
                        showFinalPageCompanion();
                    }
                    if (null != videoFinalPage) {
                        //addContentView(videoFinalPage, layoutParm);
                        mRootLayout.addView(videoFinalPage, layoutParm); //wilder 2019 changed to rootlayer
                        //omsdk v1.2
                        //AddOMObstructions(videoFinalPage);
                    }
                    //移除播放状态，添加重播等按钮
                    createFinishView(skipPosition);

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
                        if (null != mRootLayout.findViewById(SKIP_TEXT_ID)) {
                            isSkippShown = false;
                        }
                        mRootLayout.removeView(mRootLayout.findViewById(COUNTDOWN_VIEW_ID));
                        mRootLayout.removeView(mRootLayout.findViewById(VOLUME_VIEW_ID));
                        mRootLayout.removeView(mRootLayout.findViewById(SKIP_TEXT_ID));

                        if (isFinalMedia()) {
                            showFinalPage();
                        }
                        mTrackingEventTimer = null;
                        isFinished = true;
                        mPlayer.sendComplete();

                        return;
                    }
                }

                showProgressBar();
                isSkipped = false;
                if (null != mRootLayout.findViewById(SKIP_TEXT_ID)) {
                    isSkippShown = false;
                }
                mRootLayout.removeView(mRootLayout.findViewById(COUNTDOWN_VIEW_ID));
                mRootLayout.removeView(mRootLayout.findViewById(VOLUME_VIEW_ID));
                mRootLayout.removeView(mRootLayout.findViewById(SKIP_TEXT_ID));
                loadNextVideoContainer();
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

    //对于非video的内容的定时器的处理逻辑
    private void processHtmlContent(final boolean noSkip) {
            int duration = mVastModel.get(adCount).getCreativeList().get(creativeCount).getDuration() * 1000;
            final CountDownView countDownView = (CountDownView)mRootLayout.findViewById(COUNTDOWN_VIEW_ID);
            //close view
            final ImageView closeView = (ImageView)mRootLayout.findViewById(CLOSE_VIEW_ID);
            if (null == closeView) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (noSkip && !isVPAID()) {
                            hideProgressBar();
                            reportTrackingEvent(TRACKING_EVENTS_TYPE.complete);
                            switchPlay(true);
                        }
                    }
                });
            }
            //超时处理
            if (currentVideoPlayTime > duration) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!isVPAID()) {
                            currentVideoPlayTime = 0;
                            holdOnTime = 0;
                            reportTrackingEvent(TRACKING_EVENTS_TYPE.complete);
                            switchPlay(false);
                        }
                    }

                });
            }

            if (noSkip)
                return;

            //如果内容是html格式，则需要调整大小
            if (mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoType().matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX) ){
                if (!isScaled && null == mRootLayout.findViewById(COUNTDOWN_VIEW_ID)) {
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
            //倒计时countdown的处理
            if (null != countDownView) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int duration = mVastModel.get(adCount).getCreativeList().get(creativeCount).getDuration() * 1000;
                        hideProgressBar();
                        if (null != mRootLayout.findViewById(SKIP_TEXT_ID) && !mRootLayout.findViewById(SKIP_TEXT_ID).isShown())
                            showSkipText(((float) currentVideoPlayTime / 1000) + "");
                        countDownView.updateContent((duration - currentVideoPlayTime) / 1000 + "");
                        countDownView.updateProgress((int) (currentVideoPlayTime / (float) duration * 360));
                    }
                });
            }
    }

    //vast icons的处理
    private void processVastIcons() {
        ArrayList<VASTIcon> vastIcons = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVastIcons();
        if (null != vastIcons && vastIcons.size() > 0) {
            for (int i = 0; i < vastIcons.size(); i++) {
                VASTIcon vastIcon = vastIcons.get(i);

                if (!TextUtils.isEmpty(vastIcon.getDuration())) {
                    if (currentVideoPlayTime > ((Integer.valueOf(vastIcon.getDuration()) + Integer.valueOf(vastIcon.getOffset())) * 1000)) {
                        if (null != mRootLayout.findViewById(ICONS_ID_HEADER + i) && mRootLayout.findViewById(ICONS_ID_HEADER + i).isShown()) {
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
                            if (null != mRootLayout.findViewById(ICONS_ID_HEADER + i) && !mRootLayout.findViewById(ICONS_ID_HEADER + i).isShown()) {
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
    }

    //对于vast video而言，该函数用于更新倒计时图标，更新进度条，同时发送播放百分比的事件，
    //vpaid的事件通过js回调上来不用在此处理
    public void startVASTQuartileTimer(final boolean isVideo, final boolean noSkip) {
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
//                    final CountDownView countDownView = (CountDownView)mRootLayout.findViewById(COUNTDOWN_VIEW_ID);
//                    final ImageView closeView = (ImageView)mRootLayout.findViewById(CLOSE_VIEW_ID);

                    //处理Quartile事件
                    if (!isHoldOn) {
                        if (0 != currentVideoPlayTime && 0 != duration) {
                            int percentage = currentVideoPlayTime * 100 / duration;
                            if (percentage >= 25 * mQuartile) {
                                if (mQuartile == 0) {
                                    AdViewUtils.logInfo("====== AdVastView::Video at start: (" + percentage + "%) ======");
                                    reportTrackingEvent(TRACKING_EVENTS_TYPE.start);
                                } else if (mQuartile == 1) {
                                    AdViewUtils.logInfo("====== AdVastView::Video at first quartile: (" + percentage + "%) ======");
                                    reportTrackingEvent(TRACKING_EVENTS_TYPE.firstQuartile);
                                } else if (mQuartile == 2) {
                                    AdViewUtils.logInfo("====== AdVastView::Video at midpoint: (" + percentage + "%) ======");
                                    reportTrackingEvent(TRACKING_EVENTS_TYPE.midpoint);
                                } else if (mQuartile == 3) {
                                    AdViewUtils.logInfo("====== AdVastView::Video at third quartile: (" + percentage + "%) ======");
                                    reportTrackingEvent(TRACKING_EVENTS_TYPE.thirdQuartile);
//                        stopQuartileTimer();
                                }
                                mQuartile++;
                            }
                        }
                    }
                    //vast icons的处理
                    processVastIcons();
                    //处理非video等逻辑
                    if (isVideo) {
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
                    } else {
                        //又可能返回的不是vast而是html
                        processHtmlContent(noSkip);
                        if (noSkip)
                            return;
                    }
                    //定时更新
                    currentVideoPlayTime += QUARTILE_TIMER_INTERVAL;
                }//end run()
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
                if (!isPaused) {
                    mQuartile = 0; //wilder 20190829
                }
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

    private void  finishActivity() {
        AdViewUtils.logInfo("=========  AdVASTView(): finishActivity()  ======");
        if(mACVpaidCtrl != null) {
            mACVpaidCtrl.dismiss();
        }
        if( mPlayer != null) {
            mPlayer.sendDismiss();
            mIsWaitingForWebView = false;
        }

        if(!isEmbed) {
            ((Activity)mContext).finish();
        }

        onCloseBtnClicked();
    }

    private void showProgressBar() {
        if (null != mProgressBar && !mProgressBar.isShown())
            mProgressBar.setVisibility(View.VISIBLE);

        //omsdk v1.2,可用来测试obstructions
        AddOMObstructions(mProgressBar);

    }

    private void hideProgressBar() {
        if (null != mProgressBar && mProgressBar.isShown())
            mProgressBar.setVisibility(View.GONE);
    }

    private final static int COMPANION_TYPE = 1;
    private final static int ICON_TYPE = 2;
    private final static int WRAPPER_TYPE = 10;
    private final static int FINALPAGE_TYPE = 8; //wilder 2019
    private final static int ICONS_ID_HEADER = 610001;
    private final static int COMPANIONS_ID_HEADER = 710001;
    private final static int X = 1;
    private final static int Y = 2;

    /*生成behaved view ,含 companions ,icons 等*/
    private void createBehavedView(ArrayList list, int type, int count) {
        //仅在设定enable时才显示
        if (!AdViewUtils.useVastBehavedView)
            return;
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
                    //判断companions的尺寸来决定是否把它作为finalpage
                    //companionToFinalPage(vastCompanionAd);
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

            if (null != mRootLayout.findViewById(viewId)) {
                //mRootLayout.removeView(findViewById(viewId));
                //(wilder 2019) already created
                break;
            }


            //扩展的behaved view
            CustomWebview behavedWebview = new CustomWebview(mContext , false);
            behavedWebview.setTouchEventEnable(true);
            behavedWebview.setId(viewId);
            behavedWebview.setCustomClickInterface(this);
            behavedWebview.setTag(tag);
            behavedWebview.setType(type);
            behavedWebview.setBackgroundColor(Color.TRANSPARENT); //wilder 2019
            behavedWebview.setWebViewClient(new VideoWebClient());
            //注意布局
            LayoutParams layoutParams = new LayoutParams(behavedWidth, behavedHeight);
            layoutParams.leftMargin = convertBehavedXPostion(beHavedNum, allBehavedCounts); //convertBehavedPostion(xOffset + "", +behaveWidth, 1);
            layoutParams.topMargin = convertBehavedYPostion(beHavedNum);//convertBehavedPostion(yOffest + "", +behaveHeight, 2);
            behavedWebview.setLayoutParams(layoutParams);
            //加载companion或icon的内容，一般是一个链接
            loadBehavedResource(object, behavedWebview);

            if (!isVisible) {
                behavedWebview.setVisibility(View.INVISIBLE);
            }

            mRootLayout.addView(behavedWebview); //wilder 2019 changed
            beHavedNum++;
            //omsdk v1.2
            AddOMObstructions(behavedWebview);
        }
    }

    /*加载behaved view的内容，含companions , icons等*/
    private void loadBehavedResource(Object object, WebView webView) {
        if (null == webView || null == object)
            return;
        if (object instanceof VASTIcon) {
            if (!TextUtils.isEmpty(((VASTIcon) object).getStaticValue())) {
                if (!TextUtils.isEmpty(((VASTIcon) object).getValueType())) {
                    if (((VASTIcon) object).getValueType().contains("javascript")) {
                        //webView.loadData("<script>" + ((VASTIcon) object).getStaticValue() + "</script>", "text/html", "utf-8");
                        AdViewUtils.loadWebContentExt(webView, "<script>" + ((VASTIcon) object).getStaticValue() + "</script>");
                    }
                    else {
                        AdViewUtils.loadWebImageURL(webView, ((VASTIcon) object).getStaticValue(), "");
                    }
                } else {
                    AdViewUtils.loadWebImageURL(webView, ((VASTIcon) object).getStaticValue(), "");
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
                        AdViewUtils.loadWebContentExt(webView, "<script>" + ((VASTCompanionAd) object).getStaticValue()  + "</script>");
                    }
                    else {
                        AdViewUtils.loadWebImageURL(webView, ((VASTCompanionAd) object).getStaticValue(), "");
                    }
                } else {
                    AdViewUtils.loadWebImageURL(webView, ((VASTCompanionAd) object).getStaticValue(), "");
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
            //Log.i("removeIcons", "removeIcons");
            mRootLayout.removeView(tempView);
        }
        beHavedNum = 0;
        allBehavedCounts = 0;
    }

    private void createPreUI() {
        createActionView(PLAYER_VIEW_ID, CENTER_POSITION, true);
        createActionView(CLOSE_VIEW_ID, RT_POSITION, true);
        if (isEmbed) {
            createActionView(FULLSCREEN_VIEW_ID, FULLSCREEN_POSITION, true);
        }
    }
    private void removePreUI() {
        //正式播放视频时候，移除action UI
        mRootLayout.removeView(mRootLayout.findViewById(PLAYER_VIEW_ID));
        mRootLayout.removeView(mRootLayout.findViewById(CLOSE_VIEW_ID));
        //mRootLayout.removeView(mRootLayout.findViewById(FIRST_FRAME_VIEW_ID));
    }

    private void removeCompanions() {
        int count = mVastModel.get(adCount).getCompanionAdList().size();
        for (int i = 0; i < count; i++) {
            View tempView = findViewById(COMPANIONS_ID_HEADER + i);
            if (null == tempView)
                continue;
            //Log.i("removeCompanions", "removeCompanions");
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

    public void prepareLoadVideo(Context context) {
        AdViewUtils.logInfo("============= AdVastView::PlayVideo() : isReady=" + isReady + ";isProcessing=" + isProcessing + "===============");
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////VPAID  by wilder 2019/////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void vpaid_onPrepared(){
        //got vpaidAdLoaded
        AdViewUtils.logInfo("++++AdVASTView: onPrepare() ++++++");
        if(mACVpaidCtrl != null) {
            //(wilder 2019) remember :
            //ui动作不在js里面处理
            int w = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoWidth();
            int h = mVastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoHeight();
            if (w == 0 || h == 0) {
                desiredWidth = screenWidth;
                desiredHeight = screenHeight;
            }
            //mACVpaidCtrl.setVolume(50);  //set volume normal
            if (AdViewUtils.videoAutoPlay) {
                Message msg = new Message();
                msg.what = STATUS_VPAID_ADSTART_VIEW;
                msg.obj = "w=-1&h=-1";
                handler.sendMessage(msg);

                mACVpaidCtrl.playAd();
            }
        }
    }

    @Override
    public void vpaid_openUrl(@Nullable String url){

        if(!TextUtils.isEmpty(url)) {
            //if has url ,it should be opened in landing page
            reportClickEvents(
                    url,
                    mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(),
                    0, 0);
        }else {
            //fire clickthrough Event or clickTracking events
            String urlThr = mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickThrough();
            if (!TextUtils.isEmpty(urlThr)) {
                reportClickEvents(
                        urlThr,
                        mVastModel.get(adCount).getCreativeList().get(creativeCount).getVideoClicks().getClickTracking(),
                        0, 0);
            }
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
        if (!isPaused) {
            AdViewUtils.logInfo("+++++++++++[VPAID]+ AdVASTView:Vpaid_fireEvent():type = " + type + ",value = " + value + "+++++++++++");
        }
        if (type.equalsIgnoreCase(EventConstants.START)) {
            reportTrackingEvent(TRACKING_EVENTS_TYPE.start);
            //omsdk v1.2 send impression
            //sendOMImpression();
        }else if (type.equalsIgnoreCase(EventConstants.PROGRESS)){
            //该事件定时产生，用于更新进度条等
            if (!isPaused) {
                AdViewUtils.logInfo("=== play progress : " + value + " ==== ");
                Message msg = new Message();
                msg.obj = value;
                msg.what = STATUS_TIME_MESSAGE;
                handler.sendMessage(msg);
            }

        }else if ((type.equalsIgnoreCase(EventConstants.COMPLETE))) {
            Message msg = new Message();
            msg.obj = value;
            msg.what = STATUS_END_MESSAGE;
            handler.sendMessage(msg);
        }else if ((type.equalsIgnoreCase(EventConstants.CLOSE))) {
            reportTrackingEvent(TRACKING_EVENTS_TYPE.close);
        }else if ((type.equalsIgnoreCase(EventConstants.FIRST_QUARTILE))) {
            reportTrackingEvent(TRACKING_EVENTS_TYPE.firstQuartile);
        }else if ((type.equalsIgnoreCase(EventConstants.MIDPOINT))){
            reportTrackingEvent(TRACKING_EVENTS_TYPE.midpoint);
        }else if ((type.equalsIgnoreCase(EventConstants.THIRD_QUARTILE))){
            reportTrackingEvent(TRACKING_EVENTS_TYPE.thirdQuartile);
        }else if ((type.equalsIgnoreCase(EventConstants.MUTE))) {
            //processTrackingEvent(TRACKING_EVENTS_TYPE.mute);
            handler.sendEmptyMessage(STATUS_MUTE_MESSAGE);
        }else if ((type.equalsIgnoreCase(EventConstants.UNMUTE))){
            //processTrackingEvent(TRACKING_EVENTS_TYPE.unmute);
            handler.sendEmptyMessage(STATUS_UNMUTE_MESSAGE);
        }else if ((type.equalsIgnoreCase((EventConstants.SKIP)))) {
            reportTrackingEvent(TRACKING_EVENTS_TYPE.skip);
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
        }else if ((type.equalsIgnoreCase((EventConstants.CLICK_THR))))  {
            //callNative("click?x="+x+"&y="+y);
            Message msg = new Message();
            msg.what = STATUS_CLICK_MESSAGE;
            msg.obj = "x=1&y=1";
            ((CustomWebview) contentWebView).setClicked(false);
            handler.sendMessage(msg);
        } else if ((type.equalsIgnoreCase(EventConstants.PAUSE))) {
            AdViewUtils.logInfo("===== [VPAID] got Paused()  ===== ");

        }else if ((type.equalsIgnoreCase(EventConstants.RESUME))) {
            AdViewUtils.logInfo("===== [VPAID] got Resumed()  ===== ");
        }else if ((type.equalsIgnoreCase(EventConstants.MINIMIZE))) {
            AdViewUtils.logInfo("===== [VPAID] got Minimize event  ===== ");

            sendOMPlaybackMessage(TRACKING_EVENTS_TYPE.minimize);
        }else if ((type.equalsIgnoreCase(EventConstants.EXPANDED_CHANGE))) {
            AdViewUtils.logInfo("===== [VPAID] got Expanded changed event  ===== ");

            sendOMPlaybackMessage(TRACKING_EVENTS_TYPE.expand);
        }
        else{
            AdViewUtils.logInfo("===== vpaid_fireEvent(): other events, type: " + type + " ===== ");
        }

    }
    ///end wilder for VPAID ///////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////
    ///////////////////end wilder 2019 for MREC ////////////////////////////////////////////////

    public void processVastVideo(Context context, Bundle bundle) {
        adsBean = (AdsBean) bundle.getSerializable("adsBean");
        try {
            String vast = null;
            try {
                //kvVideoListener = (AdAdapterManager) bundle.getSerializable("interface"); //wilder 2019
                if (adsBean.getXmlType() == 2) {
//                    1=> VAST XML
//                    2=>元素组合
                    if (bundle.getString("bgColor").equals("#undefine") && !TextUtils.isEmpty(adsBean.getAdBgColor()))
                        bundle.putString("bgColor", adsBean.getAdBgColor());
                    if (adsBean.getVideoBean().isValidBean()) {
                        vast = AdViewUtils.generalMixedVast(adsBean);
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
                onAdFailed(null, "EMPTY BODY", true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseVastXml(Context context, Bundle bundle, String vastStr) {
        Bundle extra = new Bundle();
        extra.putInt("cacheTime", 30);//fix
        extra.putInt("vastOrientation", bundle.getInt("vastOrientation"));
        extra.putString("bgColor", bundle.getString("bgColor"));
        extra.putBoolean("closeable", bundle.getBoolean(""));
        //create vastplayer
        mPlayer = new VASTPlayer(context, extra , (VASTPlayerListener)adVideoAdapterManager);
        mPlayer.loadVideoWithData(vastStr);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// 各种上报事件 /////////////////////////////////////////////////////////
    //所有汇报在此汇集 ====》
    private void reportWrapperEvents(int wrapperEvent, TRACKING_EVENTS_TYPE event, int x, int y) {
        if (wrapperModel == null || wrapperModel.size() == 0)
            return;
        switch (wrapperEvent) {
            case CLICKTRACKING_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    ArrayList<VASTCreative> creativeList = wrapperModel.get(i).getCreativeList();
                    for (int j = 0; j < creativeList.size(); j++) {
                        VideoClicks videoClicks = creativeList.get(j).getVideoClicks();
                        KyAdBaseView.vast_reportUrls(videoClicks.getClickTracking(),
                                KyAdBaseView.getHK_Values(mContext, x, y, false, false, getVideoSnap()));
                    }
                }
                break;
            case ERROR_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    KyAdBaseView.vast_reportUrls(wrapperModel.get(i).getErrorUrl(),
                            KyAdBaseView.getHK_Values(mContext, -1, -1, false, true, getVideoSnap()));
                }
                break;
            case IMPRESSION_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    KyAdBaseView.vast_reportUrls(wrapperModel.get(i).getImpressions(),
                            KyAdBaseView.getHK_Values(mContext, -1, -1, false, false, getVideoSnap()));
                }
                break;
            case TRACKING_WRAPPER_EVENT:
                for (int i = 0; i < wrapperModel.size(); i++) {
                    ArrayList<VASTCreative> creativeList = wrapperModel.get(i).getCreativeList();
                    for (int j = 0; j < creativeList.size(); j++) {
                        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings = creativeList.get(j).getTrackings();
                        if (null != trackings && null != trackings.get(event))
                            KyAdBaseView.vast_reportUrls(trackings.get(event),
                                    KyAdBaseView.getHK_Values(mContext, -1, -1,
                                            event.equals(TRACKING_EVENTS_TYPE.complete), false, getVideoSnap()));
                    }
                }
                break;
        }
    }

    private void reportImpressions() {
        AdViewUtils.logInfo("========= AdVastView::reportImpressions() =======");
        try {
            if (!mIsProcessedImpressions) {
                mIsProcessedImpressions = true;
                List<String> impressions = mVastModel.get(adCount).getImpressions();
                KyAdBaseView.vast_reportUrls(impressions, KyAdBaseView.getHK_Values(mContext, -1, -1, false, false, getVideoSnap()));
                reportWrapperEvents(IMPRESSION_WRAPPER_EVENT, null, -1, -1);
                //omsdk v1.2, (1)for vast: impression will be sent by js side, see onloadedmetadata() in js sendOMImpression();
                // (2)for vpaid: impression will be send in start event, cause onloadedmeta will not be used
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //汇报点击click事件
    private void reportClickEvents(String jumpUrl, List<String> urls, int x, int y) {
        AdViewUtils.logInfo("======== AdVastView::reportClickEvents() =========" );
        try {
            //(1)先给adive及app发送点击事件
            mPlayer.sendClick();
            //(2)发送点击事件给vast内容中的相关dsp,这些url来自vast内容
            KyAdBaseView.vast_reportUrls(urls, KyAdBaseView.getHK_Values(mContext, x, y, false, false, getVideoSnap()));
            reportWrapperEvents(CLICKTRACKING_WRAPPER_EVENT, null, x, y);
            // Navigate to the click through url, 打开落地页
//            Intent i = new Intent();
//            i.putExtra("adview_url", jumpUrl);
//            i.putExtra("isVideo", true);
//            i.setClass(mContext, AdViewLandingPage.class);
//            ((Activity)mContext).startActivity(i);
            //(3)这里打开落地页
            AdViewUtils.openLandingPage(mContext, jumpUrl, AdViewUtils.useCustomTab);
            //(4)omsdk v1.2 must send click event
            if (jumpUrl.contains("http://")||jumpUrl.contains("https://")) {
                reportOMSDKEvents(TRACKING_EVENTS_TYPE.click);
            }else {
                reportOMSDKEvents(TRACKING_EVENTS_TYPE.invitationAccept);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void reportErrorEvent() {
        AdViewUtils.logInfo(" ======= AdVastView::reportErrorEvent ()  ======");
        try {
            List<String> errorUrls = mVastModel.get(adCount).getErrorUrl();
            KyAdBaseView.vast_reportUrls(errorUrls, KyAdBaseView.getHK_Values(mContext, -1, -1, false, true, getVideoSnap()));
            reportWrapperEvents(ERROR_WRAPPER_EVENT, null, -1, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportTrackingEvent(TRACKING_EVENTS_TYPE eventName) {
        AdViewUtils.logInfo("========AdVastView::reportTrackingEvent(): " + eventName + "==========");
        if (eventName == TRACKING_EVENTS_TYPE.start) {
            reportImpressions(); //这里发送展示汇报
            //omsdk v1.2 send impression
            sendOMImpression();
        }
        try {
            if (null != mTrackingEventMap && null != mTrackingEventMap.get(adCount)) {
                if (adCount >= mTrackingEventMap.size()
                        || creativeCount >= mTrackingEventMap.get(adCount).size()
                        || null == mTrackingEventMap.get(adCount).get(creativeCount)
                        || null == mTrackingEventMap.get(adCount).get(creativeCount).get(eventName)) {
                    AdViewUtils.logInfo(" reportTrackingEvent(): " + eventName + " has no address,returned[" + adCount + "," + creativeCount + "]");
                    //omsdk v1.2
                    sendOMPlaybackMessage(eventName);
                    return;
                }
                List<String> urls = mTrackingEventMap.get(adCount).get(creativeCount).get(eventName);
                //fire vast events
                KyAdBaseView.vast_reportUrls(urls,
                        KyAdBaseView.getHK_Values(mContext, -1, -1,eventName.equals(TRACKING_EVENTS_TYPE.complete),false, getVideoSnap()));
                //report adview events
                reportWrapperEvents(TRACKING_WRAPPER_EVENT, eventName, -1, -1);
                sendOMPlaybackMessage(eventName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //omsdk v1.2 report
    private void reportOMSDKEvents(TRACKING_EVENTS_TYPE eventName) {
        AdViewUtils.logInfo("= ******* ====AdVastView::reportOMSDKEvents():" + eventName + "======******");
        if (!isOMSDKSupport)
            return;
        if (eventName == TRACKING_EVENTS_TYPE.start) {
            AdViewUtils.logInfo("========AdVastView::reportOMSDKEvents(): start ,isOMloaded : " + isOMLoaded + "==============");
            if (isOMLoaded) {
                contentWebView.loadUrl("javascript:signalPlaybackEvent('start')");
                if (isEmbed) {
                    contentWebView.loadUrl("javascript:signalStateChange('normal')");
                } else {
                    contentWebView.loadUrl("javascript:signalStateChange('fullscreen')");
                }
            }else {
                AdViewUtils.logInfo("========AdVastView::reportOMSDKEvents(): " + eventName + " failed, wait omloaded ==========");
            }
        }else if (eventName == TRACKING_EVENTS_TYPE.click) {
            //omsdk click report
            contentWebView.loadUrl("javascript:signalPlaybackEvent('click')");
        }else if (eventName == TRACKING_EVENTS_TYPE.invitationAccept) {
            //omsdk click report
            contentWebView.loadUrl("javascript:signalPlaybackEvent('invitationAccept')");
        }else if (eventName == TRACKING_EVENTS_TYPE.firstQuartile) {
            contentWebView.loadUrl("javascript:signalPlaybackEvent('firstQuartile')");
        }else if (eventName == TRACKING_EVENTS_TYPE.midpoint) {
            contentWebView.loadUrl("javascript:signalPlaybackEvent('midpoint')");
        }else if (eventName == TRACKING_EVENTS_TYPE.thirdQuartile) {
            contentWebView.loadUrl("javascript:signalPlaybackEvent('thirdQuartile')");
        }else if (eventName == TRACKING_EVENTS_TYPE.complete) {
            contentWebView.loadUrl("javascript:signalPlaybackEvent('complete')");
        }else if (eventName == TRACKING_EVENTS_TYPE.pause) {
            contentWebView.loadUrl("javascript:signalPlaybackEvent('pause')");
        }else if (eventName == TRACKING_EVENTS_TYPE.resume) {
            contentWebView.loadUrl("javascript:signalPlaybackEvent('resume')");
        }else if (eventName == TRACKING_EVENTS_TYPE.skip) {
            contentWebView.loadUrl("javascript:signalSkipVideo()");
        }else if (eventName == TRACKING_EVENTS_TYPE.fullscreen) {
            /*
            VideoPlayerState:{MINIMIZED:"minimized", COLLAPSED:"collapsed", NORMAL:"normal", EXPANDED:"expanded", FULLSCREEN:"fullscreen"}
             */
            contentWebView.loadUrl("javascript:signalStateChange('fullscreen')");
        }else if (eventName == TRACKING_EVENTS_TYPE.minimize) {
            contentWebView.loadUrl("javascript:signalStateChange('minimize')");
        }else if (eventName == TRACKING_EVENTS_TYPE.expand) {
            contentWebView.loadUrl("javascript:signalStateChange('expanded')");
        }
    }
    //////////////////////////////////////上报事件处理 end ///////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //        (create)     AdVastView --> new VastPlayer
    //        AdvastView ( KyVideoListener) <--  video adapter <-- VastPlayer (event)
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onVideoClicked(AgDataBean agDataBean) {
        AdViewUtils.logInfo("==== AdVastView::onVideoClicked()  ==== ");
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
        AdViewUtils.logInfo("===AdVASTView== onVideoPlayFinished()  ===== ");
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
        AdViewUtils.logInfo("===AdVASTView== onVideoPlayStarted()  ===== ");
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
        if (adVideoAdapterManager == null)
            return;

        if(mPlayer == null) {
            return;
        }
        //this.mPlayer = player;
        this.mVastModel = mPlayer.getVastModel();
        this.wrapperModel = mPlayer.getWrapperModel();

        //更新各种extension 和companion,click等各种vast所需要的数据结构
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
//            //wilder 20191101 ,添加第一帧
            if (useFirstFrameCache && !AdViewUtils.videoAutoPlay) {
                Message message = new Message();
                message.what = STATUS_GET_FIRST_FRAME;
                handler.sendMessage(message);
            }else {
                //加载video的container
                loadNextVideoContainer();
            }
            //reportImpressions(); 20190830改为在start事件中发送展示汇报
        } else {
            AdViewUtils.logInfo("数据格式异常");
            finishActivity();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    //------------------ KyBaseListener  ------------------------------------
    ////////////////////////////////////////////////////////////////////////////
    /*this will be called*/
    @Override
    public void onReady(AgDataBean agDataBean, boolean force) {
        isProcessing = false;
        isReady = true;
        //notify caller view , banner or video activity
        if (null != adAppInterface) {
            adAppInterface.onVideoReady();
        }
        //wilder 2019 play it directly, maybe should play by user?
        if (AdViewUtils.videoAutoPlay) {
            Message message = new Message();
            message.what = STATUS_PREPARE_LOAD_VIDEO_MESSAGE;
            handler.sendMessage(message);
        }else {
            //if not auto play, should hide progress and support click play
            Message message = new Message();
            message.what = STATUS_PREPARE_LOAD_VIDEO_MESSAGE;
            handler.sendMessage(message);
        }
    }

    @Override
    public void onReceived(AgDataBean agDataBean, boolean force) {
        //使用onVideoReceived（String vast）
        AdViewUtils.logInfo("======= AdVastView::onReceived() =========");
    }

    @Override
    public void onAdFailed(AgDataBean agDataBean, String error, boolean force) {
        try {
            if (null != agDataBean && null != agDataBean.getFailUrls()) {
                KyAdBaseView.reportOtherUrls(agDataBean.getFailUrls());
            }
            int times = KyAdBaseView.getAgDataBeanPosition(adsBean, agDataBean);
            if (times != -1) {
                //adVideoAdapterManager = handlerAd(getContext(), adsBean, false, times, agDataBean, this.passBundle);
                return;
            } else {
                isProcessing = false;
                isReady = false;

                if (null != adAppInterface) {
                    String info = "____video load ad error____";
                    adAppInterface.onFailedReceivedVideo( info);
                    //omsdk v1.2 report error
                    sendOMSDKErr(info);
                }
            }
        } catch (Exception e) {
            isProcessing = false;
            isReady = false;
            e.printStackTrace();
        }
        //if still no float UI, should show it ,then user can close it
        hideProgressBar();

        createFinishView(false);
        //omsdk v1.2 report error
        sendOMSDKErr(error);
    }

    @Override
    public void onDisplay(AgDataBean agDataBean, boolean force) {
        AdViewUtils.logInfo("======= AdVastView::onDisplay() =========");
        if (null != adAppInterface) {
            adAppInterface.onVideoStartPlayed();
        }
    }
    /*this will be called */
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
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////    omsdk v1.2   //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //引入omsdk后,vast流程如下:
    //(1)xxx_video_js.html页面加载是触发initOMSDK()(js函数）,
    //(2)initOMSDK()中调用native: ominited ->STATUS_OMSDK_INITED 事件
    //(3) STATUS_OMSDK_INITED -> getOMParametersFromJS()从vast的extension中取得omsdk的3个参数，
    //(4)调用js函数setOMParameters()将参数传入js,同时传入是否自动播放参数
    //(5)在js中启动startOMSession()，如果自动播放参数是true则立刻在js播放，否则回头由player按键触发
    private void getOMParametersFromJS() {
        String vendor = ""; // = "iabtechlab.com-omid";
        String para = ""; // = "iabtechlab-Cjnet";
        String url = ""; // = "https://s3-us-west-2.amazonaws.com/omsdk-files/compliance-js/omid-validation-verification-script-v1.js";
        String skip = "";
        String auto = "true";
        final ExtensionOMSDKBean omeb = mVastModel.get(adCount).getExtOMSDKBean();
        //get skipable
        int skipDuration = mVastModel.get(adCount).getCreativeList().get(creativeCount).getSkipoffset();
        if (skipDuration != -1) {
            skip = String.valueOf(skipDuration);
        }
        if (null != omeb) {
            vendor = omeb.getOmsdkVendor();
            para = omeb.getOmsdkParameters();
            url = omeb.getOmsdkUrl();
            //auto = AdViewUtils.videoAutoPlay ? "true" : "false";
            if (vendor.length() > 0 && url.length() > 0) {
                isOMSDKSupport = true;
                contentWebView.loadUrl("javascript:setOMParameters(\""
                                                    + vendor    + "\",\""
                                                    + para      + "\",\""
                                                    + url       + "\",\""
                                                    + skip      + "\","
                                                    + AdViewUtils.videoAutoPlay
                                                    + ")");
                //start session
                adSession = AdViewUtils.startOMAdSessionJS(contentWebView);
                afterOMParametersFromJS();
                return;
            }
        }
        //normal vast , just play
        AdViewUtils.logInfo("===!!!! AdVastView::getOMSessionParameters() !!! not omsdk  !!!!!========");
        //anyway nomal vast also can be play directly
        contentWebView.loadUrl("javascript:setOMParameters(\""
                                            + vendor    + "\",\""
                                            + para      + "\",\""
                                            + url       + "\",\""
                                            + skip      + "\","
                                            + AdViewUtils.videoAutoPlay
                                            + ")");
        afterOMParametersFromJS();
    }

    //前一个动作：loadNextVideoContainer(), 该事件由js加载html产生，发生在加载完video和omsdk之后，正式播放视频之前,详见js
    private void afterOMParametersFromJS() {
        if (!isVPAID()) {
            if (AdViewUtils.videoAutoPlay) {
                startVASTQuartileTimer(isVideoTypeRelated(), isFinalMedia());  //wilder 20190815 move here
            } else {
                //对于自动播放模式，
                createPreUI();
            }
        }else {
            //对于vpaid模式，自动播放， vpaid 不需要startVASTQuartileTimer
            if (AdViewUtils.videoAutoPlay) {
                //startVASTQuartileTimer(isVideoTypeRelated(), isFinalMedia());  //wilder 20190815 move here
            } else {
                createPreUI();
            }
        }
    }

    //when omsdk loaded
    private void setOmsdkLoaded() {
        isOMLoaded = true;
        if (isEmbed) {
            contentWebView.loadUrl("javascript:setFullScreen(false)");
        } else {
            contentWebView.loadUrl("javascript:setFullScreen(true)");
        }
    }

    public void sendOMImpression() {
        //cause inject OMJS may not be completed, so must waiting
        if (!isOMSDKSupport || !AdViewUtils.canUseOMSDK())
            return;

        Message message = new Message();
        message.what = STATUS_OMSDK_SEND_IMPRESSION;
        handler.sendMessage(message);

//        mOMSDKTimer = new Timer();
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                if (isOMLoaded && !isImpressionTrigged) {
//                    //only can be triggered once, and webview must be called in main loop
//                    //AdViewUtils.signalImpressionEvent(adSession);
//                    //contentWebView.loadUrl("javascript:signalImpressionEvent()");
//                    Message message = new Message();
//                    message.what = STATUS_OMSDK_SEND_IMPRESSION;
//                    handler.sendMessage(message);
//
//                    isImpressionTrigged = true;
//                    mOMSDKTimer.cancel();
//                    mOMSDKTimer = null;
//                }else {
//                    AdViewUtils.logInfo("************  timer up    **************");
//                }
//            }
//        };
//        mOMSDKTimer.schedule(timerTask,  1000, 800);
    }


    private void sendOMPlaybackMessage(TRACKING_EVENTS_TYPE eventName) {
        //omsdk v1.2, 有关start事件，发送时有可能omsdk还未加载成功，因此建议在js端等待视频加载完毕播放时发送video.start消息
        if (!isOMSDKSupport || !AdViewUtils.canUseOMSDK())
            return;
        //reportOMSDKEvents(eventName);
        //AdViewUtils.logInfo("------------- sendOMPlaybackMessage: " + eventName + "------------");
        Message message = new Message();
        message.what = STATUS_OMSDK_PLAYBACK_EVENT;
        message.obj = eventName;
        handler.sendMessage(message);
    }
    ///////////////////////////end omsdk v.1.2.15 ////////////////////////////////////////////////

    /** 全屏容器界面 */
    class FullscreenHolder extends RelativeLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (!changed)
                return;
            //RelativeLayout root = (RelativeLayout)getChildAt(0);
            FullscreenHolder root = (FullscreenHolder)findViewById(ROOT_LAYOUT_ID);
            if (null != root) {
                int cnt = root.getChildCount();
                if (cnt > 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            updateAllActionView(getWidth(), getHeight());
                        }
                    });

                }
            }
        }
    }

}
