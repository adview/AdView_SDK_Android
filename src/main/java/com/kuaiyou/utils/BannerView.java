package com.kuaiyou.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.interfaces.AdVGListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.mraid.interfaces.MRAIDNativeFeatureListener;
import com.kuaiyou.mraid.interfaces.MRAIDViewListener;
import com.kuaiyou.obj.AdsBean;

import com.kuaiyou.video.AdVASTView;
import com.kuaiyou.interfaces.AdViewVideoInterface;
//import com.kuaiyou.video.AdVASTView2;


import java.net.URLDecoder;
import java.util.HashMap;

public class BannerView extends RelativeLayout implements View.OnTouchListener,
                                MRAIDViewListener, MRAIDNativeFeatureListener{

    // banner文字动画位移常量
    public final static int UP_OUT = 0;
    public final static int UP_IN = 1;
    public final static int DOWN_OUT = 2;
    public final static int DOWN_IN = 3;

    private int padding = 4;
    private int adWidth, adHeight;
    //private String adLogo, adIcon;

        // 动画切换默认间隔
    private static int ANIM_OFF = 30;
    private AdVGListener adVGListener;
    private String bitmapPath;
    //    boolean isSchedule;
    private boolean hasWindow = false;
    private int adAct;
    //wilder 2019 for MRec
    //private AdVASTView2 mvastView; //wilder 2020 for surface ,from AdVASTView -> AdVASTView2
    private AdVASTView mvastView;
    private AdAdapterManager adAdapterManager;
    public BannerView(Context context, Bundle bundle, AdVGListener adVGListener, AdAdapterManager adm) {
        super(context);
        this.adVGListener = adVGListener;
        int[] adSize = bundle.getIntArray("adSize");
        double density = bundle.getDouble("density");
        adAct = bundle.getInt("adAct");
        bitmapPath = bundle.getString("bitmapPath");
        this.adWidth = adSize[0];
        this.adHeight = adSize[1];
        this.padding = (int) (this.padding * density);
        this.hasWindow = true;
        setAdapterManager(adm);

//        this.isFirst=true;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                new HandlerRunable().load();
            }
        });

    }

    public void setAdapterManager(AdAdapterManager adpt) {
        adAdapterManager = adpt;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        int dipSize = 0;
        int dipHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            switch (child.getId()) {
                case ConstantValues.MIXED_UI_ICONID:
                    child.layout((padding), (padding), (adHeight - padding), (adHeight - padding));
                    break;
                case ConstantValues.MIXED_UI_BEHAVEICON_ID:
                    child.layout(((adWidth) - (adHeight) + (padding)), (padding), ((adWidth) - (padding)), (adHeight - padding));
                    break;
                case ConstantValues.MIXED_UI_TITLEID:
                    child.layout(((adHeight) + (padding)), ((2 * padding)), (adWidth - adHeight - (2 * padding)), (adHeight - 2 * padding));
                    break;
                case ConstantValues.UI_WEBVIEW_ID:
                    child.layout(0, 0, adWidth, adHeight);
                    break;
                case ConstantValues.UI_ADICON_ID:
                    dipSize = adWidth / 6;
                    dipHeight = dipSize / 5;
                    //child.layout(0, (adHeight / 4 * 3), (adHeight), (adHeight));
                    child.layout(0, (adHeight - dipHeight), (dipSize), (adHeight));
                    break;
                case ConstantValues.UI_ADLOGO_ID:
                    //child.layout((adWidth - adHeight), (adHeight / 4 * 3), (adWidth), (adHeight));
                    dipSize = adWidth / 6;
                    dipHeight = dipSize / 3;
                    child.layout((adWidth - dipSize), (adHeight - dipHeight), (adWidth), (adHeight));
                    break;
                case ConstantValues.UI_CLOSEBTN_ID:
                    //(wilder 2019) fixed action button
                    dipSize = adWidth / 17;//adHeight / 4 * 1;
                    int y = 0; //padding / 2;
                    //child.layout((adWidth - adHeight / 3 - padding), (adHeight / 3), (adWidth - padding), (adHeight / 3 * 2));
                    child.layout((adWidth - dipSize - padding), (y), (adWidth - padding), (y + dipSize));
                    break;
            }
        }
    }

    private void setCloseButton(boolean closeAble) {
        try {
            ImageView closeBtn = (ImageView) findViewById(ConstantValues.UI_CLOSEBTN_ID);
            if (closeBtn == null)
                return;
            if (!closeAble) {
                closeBtn.setVisibility(View.GONE);
            }

            //Bitmap bm = AdViewUtils.getImageFromAssetsFile("close_ad_btn.png"); wilder 2020 for non-context
            Bitmap bm = AdViewUtils.getImageFromAssetsFile2("close_ad_btn.png", getContext());
            closeBtn.setImageDrawable(new BitmapDrawable(getResources(), bm));

            closeBtn.setOnTouchListener(this);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, final MotionEvent e) {
        final AdsBean adsBean = adVGListener.getAdsBean();
        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                try {
                    adsBean.setAction_down_x((int) e.getX());
                    adsBean.setAction_down_y((int) e.getY());
                } catch (Exception exec) {
                }
                break;
            case MotionEvent.ACTION_UP:
                try {
                    switch (v.getId()) {
                        case ConstantValues.UI_CLOSEBTN_ID:
                            if (null != adVGListener)
                                adVGListener.onCloseBtnClicked();
                            return true;
                        default:
                            adsBean.setAction_up_x((int) e.getX());
                            adsBean.setAction_up_y((int) e.getY());

                            if (adVGListener.isClickableConfirm()) {
                                if (null != adVGListener)
                                    adVGListener.needConfirmDialog();
                                if (null != adVGListener)
                                    adVGListener.onViewClicked(e, null,null, e.getX(), e.getY());
                            }
                            return false;
                    }
                } catch (Exception exec) {
                    exec.printStackTrace();
                }
        }

        return true;
    }

    class HandlerRunable {
        private String titleStr = null;
        private String subTitleStr = null;
        private WebView webView = null;
        private ImageView imageView = null;
        private TextView title = null;
        private AdsBean adsBean;

        private boolean isEmbedVideo = false; //wilder 2019

        public HandlerRunable() {
        }

        public void load() {
            try {
                adsBean = adVGListener.getAdsBean();
                int rand = (int) ((Math.random() * 10) % 6);
                HashMap<String, String> colorMap = KyAdBaseView.getColorSet(rand);

                //create banner view
                initWidgetLayout(adsBean.getAdType(), colorMap);

                if (AdViewUtils.adLogoOnLine)
                    setAdIconLogoBmp();
                else
                    setAdIconLogo();

                setTouchListener();
                setCloseButton(adVGListener.getCloseble());
                setBehaveIcon();
                webView = getWebView();
                title = (TextView) findViewById(ConstantValues.MIXED_UI_TITLEID);

                switch (adsBean.getAdType()) {
                    case ConstantValues.RESP_ADTYPE_HTML:
                        if (null != webView && null != adsBean.getXhtml()) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String content = adsBean.getXhtml();
                                        if (!content.startsWith("http://") && !content.startsWith("https://")) {
                                            //wilder 2019 , scipt no layout, need center it
                                            AdViewUtils.loadWebContentExt(webView, adsBean.getXhtml());

                                        } else {
                                            webView.loadUrl(adsBean.getXhtml());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 50);

                        }
                        break;
                    case ConstantValues.RESP_ADTYPE_FULLIMAGE:
                    case ConstantValues.RESP_ADTYPE_INSTL:  //wilder 2019 for if got instl PDU
                        if (AdViewUtils.bitmapOnLine) {
                            AdViewUtils.loadWebImageURL(webView, bitmapPath, adsBean.getAdLink());
                        }else {
                            AdViewUtils.loadWebImageLocal(webView, bitmapPath, adsBean.getAdLink(), adWidth, adHeight);
                        }
                        break;
                    case ConstantValues.RESP_ADTYPE_MIXED:
                    case ConstantValues.RESP_ADTYPE_INTERLINK:
                        imageView = (ImageView) findViewById(ConstantValues.MIXED_UI_ICONID);
                        titleStr = adsBean.getAdTitle();
                        subTitleStr = adsBean.getAdSubTitle();
                        if (null != imageView && null != bitmapPath) {
                            imageView.setImageDrawable(new BitmapDrawable(getResources(), getClass().getResourceAsStream(bitmapPath)));
                        }
                        if (null != titleStr && null != title && titleStr.length() > 0) {
                            changeTextSize(title, titleStr.length());
                            title.setText(AdViewUtils.changeTextColorCateg(
                                    titleStr,
                                    ConstantValues.REGULAR_MATCH_BIGBRACKETS,
                                    null,
                                    Color.parseColor(colorMap.get(ConstantValues.MIXED_KEYWORDBACKGROUND_COLOR))));
                            if (null != subTitleStr && subTitleStr.trim().length() > 0) {
                                if (null == title.getAnimation()) {
                                    int x = 0;
                                    int y = 0;
                                    ANIM_OFF = adHeight - 1;
                                    AnimationSet animationSet = setUserAnimation(x, x, y, y
                                            - ANIM_OFF,
                                            1f,
                                            0f,
                                            300,
                                            3 * 1000);
                                    animationSet.setAnimationListener(new TranslateAnimationListener(
                                                    title,
                                                    titleStr,
                                                    subTitleStr,
                                                    Color.parseColor(colorMap.get(ConstantValues.MIXED_KEYWORDBACKGROUND_COLOR)),
                                                    UP_OUT, x, y));
                                    title.startAnimation(animationSet);
                                }
                            }
                        }
                        break;
                    case ConstantValues.RESP_ADTYPE_VIDEO_PASTER:   //wilder 2019 MREC
                    case ConstantValues.RESP_ADTYPE_VIDEO_EMBED:
                    case ConstantValues.RESP_ADTYPE_VIDEO:
                        if (!TextUtils.isEmpty(adsBean.getVastXml()) || null != adsBean.getVideoBean()) {
                            isEmbedVideo  = true;
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("adsBean", adsBean);
                            bundle.putBoolean("closeable", false);
                            bundle.putInt("vastOrientation", -1); //-1 means no rotation
                            bundle.putBoolean("trafficWarnEnable", true);
                            bundle.putString("bgColor", "#000000");
                            if (mvastView != null ) {
                                //mvastView.video_handlerAd(getContext(), adsBean, true, -1, null, bundle);
                                mvastView.processVastVideo(getContext(),bundle);
                            }
                        }
                        break;
                    default:
                        break;
                }
//                isSchedule = false;
                if ( null != adVGListener && !isEmbedVideo ) {
                    adVGListener.onReady(null, true);
                }

                if ( null != adVGListener && !isEmbedVideo ) {
                    //if embed video, should be trigged in play finished
                    adVGListener.onDisplay(null, true);

                }
            } catch (Exception e) {
                e.printStackTrace();
                if (null != adVGListener) {
                    adVGListener.onAdFailed(null, "load ad error", true);
                }
            }
        }
    }

    class TranslateAnimationListener implements Animation.AnimationListener {
        int color, type, x, y;
        private TextView title;
        private String titleStr;
        private String subTitleStr;

        public TranslateAnimationListener(TextView title, String titleStr,
                                          String subTitleStr, int color, int type, int x, int y) {
            this.title = title;
            this.titleStr = titleStr;
            this.subTitleStr = subTitleStr;
            this.x = x;
            this.y = y;
            this.type = type;
            this.color = color;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            AnimationSet animationSet = null;
            String sTitle = "";
            switch (type) {
                case UP_OUT:
                    animationSet = setUserAnimation(x, x, y + ANIM_OFF, y, 0f, 1f,
                            300, 100);
                    sTitle = subTitleStr;
                    type = UP_IN;
                    changeTextSize(title, subTitleStr.length());
                    break;
                case UP_IN:
                    animationSet = setUserAnimation(x, x, y, y + ANIM_OFF, 1f, 0f,
                            300, 3100);
                    sTitle = subTitleStr;
                    type = DOWN_OUT;
                    changeTextSize(title, subTitleStr.length());
                    break;
                case DOWN_IN:
                    animationSet = setUserAnimation(x, x, y, y - ANIM_OFF, 1f, 0f,
                            300, 3100);
                    sTitle = titleStr;
                    type = UP_OUT;
                    changeTextSize(title, titleStr.length());
                    break;
                case DOWN_OUT:
                    animationSet = setUserAnimation(x, x, y - ANIM_OFF, y, 0f, 1f,
                            300, 100);
                    sTitle = titleStr;
                    type = DOWN_IN;
                    changeTextSize(title, titleStr.length());
                    break;
            }
            title.setText(AdViewUtils.changeTextColorCateg(sTitle,
                    ConstantValues.REGULAR_MATCH_BIGBRACKETS, null, color));
            if (title.isShown() && null != animationSet) {
                animationSet.setAnimationListener(this);
                title.startAnimation(animationSet);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }

    private AnimationSet setUserAnimation(int fromX, int toX, int fromY,
                                          int toY, float fromAlpha, float toAlpha, int duration, int offSet) {
        AnimationSet animationSet = null;
        try {
            animationSet = new AnimationSet(false);
            AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha,
                    toAlpha);
            TranslateAnimation translateAnimation = new TranslateAnimation(
                    fromX, toX, fromY, toY);
            alphaAnimation.setStartOffset(offSet);
            translateAnimation.setStartOffset(offSet);
            animationSet.setDuration(duration);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(alphaAnimation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return animationSet;
    }

    private void setAdIconLogoBmp() {
        try {
            ImageView adIconView = (ImageView) findViewById(ConstantValues.UI_ADICON_ID);
            ImageView adLogoView = (ImageView) findViewById(ConstantValues.UI_ADLOGO_ID);
            //BitmapDrawable logo = null, icon = null;
            //logo
            if (null != adLogoView && null != adVGListener) {
                adLogoView.setImageBitmap(adVGListener.getAdLogoBmp());
            }
            //icon
            if (null != adIconView && null != adVGListener ) {
                adIconView.setImageBitmap(adVGListener.getAdIconBmp());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setAdIconLogo() {
        try {
            ImageView adIconView = (ImageView) findViewById(ConstantValues.UI_ADICON_ID);
            ImageView adLogoView = (ImageView) findViewById(ConstantValues.UI_ADLOGO_ID);
            BitmapDrawable logo = null, icon = null;
            String adLogo = null, adIcon = null;
            if (null != adVGListener) {
                adLogo = adVGListener.getAdLogo();
                adIcon = adVGListener.getAdIcon();
            }else
                return;
            //logo
            if (!TextUtils.isEmpty(adLogo) && adLogoView != null) {
                if (adLogo.startsWith("/assets")) {
                    //Bitmap bm = AdViewUtils.getImageFromAssetsFile(adLogo.replace("/assets/","")); wilder 2020 for non-context
                    Bitmap bm = AdViewUtils.getImageFromAssetsFile2(adLogo.replace("/assets/",""), getContext());
                    logo = new BitmapDrawable(getResources(),bm);
                }
                else {
                    //logo = new BitmapDrawable(adLogo);
                    logo = new BitmapDrawable(getResources(),adLogo);
                }
                if ( null != logo ) {
                    adLogoView.setImageDrawable(logo);
                    adLogoView.setScaleType(ImageView.ScaleType.FIT_END);
                }
            }
            //icon
            if ( !TextUtils.isEmpty(adIcon) && adIconView != null ) {
                if (adIcon.startsWith("/assets")) {
                    //Bitmap bm = AdViewUtils.getImageFromAssetsFile(adIcon.replace("/assets/","")); wilder 2020 for non-context
                    Bitmap bm = AdViewUtils.getImageFromAssetsFile2(adIcon.replace("/assets/",""), getContext());
                    icon = new BitmapDrawable(getResources(),bm);
                }
                else {
                    icon = new BitmapDrawable(getResources(), adIcon);
                }

                if ( null != icon ) {
                    adIconView.setImageDrawable(icon);
                    adIconView.setScaleType(ImageView.ScaleType.FIT_START);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTouchListener() {
        if (null != adVGListener)
            adVGListener.setClickMotion(getMraidView(), null);
    }

    private void setBehaveIcon() {
        try {
            ImageView behavIcon = (ImageView) findViewById(ConstantValues.MIXED_UI_BEHAVEICON_ID);
            if (null == behavIcon)
                return;
            behavIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            BitmapDrawable selfBehavIcon = new BitmapDrawable(getResources(),
                                    AdViewUtils.getImageFromAssetsFile2(KyAdBaseView.getActIcon(adAct),getContext())); //
//            BitmapDrawable selfBehavIcon = new BitmapDrawable(getResources(),
//                                    getClass().getResourceAsStream(KyAdBaseView.getActIcon(adAct)));
            behavIcon.setImageDrawable(selfBehavIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 根据字数修改文字大小
    private void changeTextSize(TextView textView, int strLength) {
        if (strLength < 10)
            textView.setTextSize(18);
        else if (strLength > 9 && strLength <= 14)
            textView.setTextSize(16);
        else if (strLength > 14 && strLength <= 20)
            textView.setTextSize(14);
        else
            textView.setTextSize(12);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (adWidth > 0)
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(adWidth, MeasureSpec.AT_MOST);
        if (adHeight > 0)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(adHeight, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(adWidth, adHeight);

    }

    // 当visible时请求
    // 当gone时 清空之前的线程池中的数据
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == VISIBLE) {
            this.hasWindow = true;
//            if (!isFirst)
//            if (null != adVGListener)
//                adVGListener.rotatedAd(0);
//                requestAd(reFreshTime);
        } else if (visibility == GONE) {
            this.hasWindow = false;
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
//        sendImpression();
//        if (null != adVGListener)
//            adVGListener.onVisiblityChange(visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        this.post(new Runnable() {
//            @Override
//            public void run() {
//                if (null != adVGListener)
//                    adVGListener.onReady(true);
//            }
//        });

    }


    public MRAIDView getMraidView() {
        return (MRAIDView) findViewById(ConstantValues.UI_MRAIDVIEW_ID);
    }

    public WebView getWebView() {
        return (WebView) findViewById(ConstantValues.UI_WEBVIEW_ID);
    }

    private void initWidgetLayout(int adType, HashMap<String, String> colorMap) {
        removeAllViews();
        ImageView icon_lable = new ImageView(getContext());
        ImageView logo_lable = new ImageView(getContext());
        ImageView close_btn = new ImageView(getContext());
        if (adType == ConstantValues.RESP_ADTYPE_VIDEO_PASTER ||
            adType == ConstantValues.RESP_ADTYPE_VIDEO_EMBED ||
            adType == ConstantValues.RESP_ADTYPE_VIDEO) {
            //mrec
            this.setBackgroundColor(Color.parseColor(colorMap.get(ConstantValues.MIXED_PARENTBACKGROUND_COLOR)));
            mvastView = new AdVASTView(getContext(), this.adWidth, this.adHeight, true, adAdapterManager); //wilder 2020 for surface
            //mvastView = new AdVASTView2(getContext(), this.adWidth, this.adHeight, true, adAdapterManager);
            mvastView.setVideoAppListener(new AdViewVideoInterface() {
                @Override
                public void onReceivedVideo(String vast) {
                    AdViewUtils.logInfo("====== BannerView(): onReceivedVideo() ======");
                }
                @Override
                public void onFailedReceivedVideo(String error) {
                    AdViewUtils.logInfo("====== BannerView(): onFailedReceivedVideo() ======");
                    mvastView.showErrorPage();
                    if (null != adVGListener) {
                        adVGListener.onAdFailed(null, "load video ad error", true);
                    }
                }
                @Override
                public void onVideoReady() {
                    AdViewUtils.logInfo("====== BannerView(): onVideoReady() ======");
                    //(wilder 2019) don't need it here, cause vastDownloadReady() has already
                    //send onReady(), but adaptmanager's onAdReady() will process onReady() 2 times,
                    //(1)AdBannerBIDView, (2)ADVastView, so here no needed
//                    if (null != adVGListener) {
//                        adVGListener.onReady(null, true);
//                    }
                }
                @Override
                public void onVideoStartPlayed() {
                    AdViewUtils.logInfo("====== BannerView(): onVideoStartPlayed() ======");
                }
                @Override
                public void onVideoFinished() {
                    AdViewUtils.logInfo("====== BannerView(): onVideoFinished() ======");
                    if (null != adVGListener) {
                        adVGListener.onDisplay(null, true);
                    }
                }
                @Override
                public void onVideoClosed() {
                    AdViewUtils.logInfo("====== BannerView(): onVideoClosed() ======");
                    if (null != adVGListener) {
                        adVGListener.onCloseBtnClicked();
                    }
                }
                @Override
                public void onPlayedError(String error) {
                    AdViewUtils.logInfo("====== BannerView(): onPlayedError() ======");
                    if (null != adVGListener) {
                        adVGListener.onAdFailed(null, "play video ad error", true);
                    }
                }
                @Override
                public int getOrientation() {
                    return -1;
                }
            });
            this.addView(mvastView);
            //embed mode, use video's own close
            logo_lable.setId(ConstantValues.UI_ADLOGO_ID);
            icon_lable.setId(ConstantValues.UI_ADICON_ID);
            return; //do we need logo ,icon and close ?
        }
        else if (adType == ConstantValues.RESP_ADTYPE_MIXED || adType == ConstantValues.RESP_ADTYPE_INTERLINK )
        {
            /* wilder 2019 add 主要怎对mixed等以下两种可能情况
             public final static int RESP_ADTYPE_INTERLINK = 1; //banner文字链
             public final static int RESP_ADTYPE_MIXED = 2;  //banner图文混合
             */
            TextView title = new TextView(getContext());
            ImageView behave = new ImageView(getContext());
            ImageView icon = new ImageView(getContext());
            behave.setPadding(padding, padding, padding, padding);
            title.setId(ConstantValues.MIXED_UI_TITLEID);
            behave.setId(ConstantValues.MIXED_UI_BEHAVEICON_ID);
            icon.setId(ConstantValues.MIXED_UI_ICONID);

            title.setTextColor(Color.parseColor(colorMap.get(ConstantValues.MIXED_TITLEBACKGROUND_COLOR)));
            behave.setBackgroundColor(Color.parseColor(colorMap.get(ConstantValues.MIXED_ICONBACKGROUND_COLOR)));
            this.setBackgroundColor(Color.parseColor(colorMap.get(ConstantValues.MIXED_PARENTBACKGROUND_COLOR)));

            this.addView(icon);
            this.addView(behave);
            this.addView(title);

            setOnTouchListener(this);
            title.setOnTouchListener(this);
            behave.setOnTouchListener(this);
            icon.setOnTouchListener(this);
        }
        else {
//            public final static int RESP_ADTYPE_FULLIMAGE = 0; //banner纯图片
//            public final static int RESP_ADTYPE_INSTL = 3;  //插屏
//            public final static int RESP_ADTYPE_HTML = 4;    //html
            final MRAIDView mraidView = new MRAIDView(getContext(), this, this, false);
            final WebView webView = mraidView.getMraidWebView();
            mraidView.setId(ConstantValues.UI_MRAIDVIEW_ID);
            webView.setId(ConstantValues.UI_WEBVIEW_ID);
            webView.setClickable(true);
            this.addView(mraidView);
        }

        close_btn.setId(ConstantValues.UI_CLOSEBTN_ID);
        logo_lable.setId(ConstantValues.UI_ADLOGO_ID);
        icon_lable.setId(ConstantValues.UI_ADICON_ID);

        this.addView(logo_lable);
        this.addView(icon_lable);
        this.addView(close_btn);

    }

    ///------------------ MRAIDNativeFeatureListener ------------------------------
    @Override
    public void mraidNativeFeatureDownload(String url) {
        if (null != adVGListener)
            adVGListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureCallTel(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL); // android.intent.action.DIAL
        intent.setData(Uri.parse(url));
        getContext().startActivity(intent);
    }

    @Override
    public void mraidNativeFeatureOpenDeeplink(String url) {
        if (url.startsWith("mraid")) {
            try {
                url = URLDecoder.decode(url.replace("mraid://openDeeplink?url=", ""), "UTF-8");
                if (null != adVGListener)
                    adVGListener.getAdsBean().setDeeplink(url);
                if (null != adVGListener)
                    adVGListener.checkClick(url);
//                clickCheck(url, adsBean, applyAdBean, retAdBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mraidNativeFeatureOpenBrowser(String url) {
        if (null != adVGListener)
            adVGListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureSendSms(String url) {
        AdViewUtils.sendSms(getContext(), url);
    }

    @Override
    public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {
    }

    @Override
    public void mraidNativeFeatureStorePicture(String url) {
    }

    /*---------    MRAID VIEW STATUS INTERFACE  ---------------------*/
    @Override
    public void mraidViewOMJSInjected( MRAIDView mraidView ) {
        //OMSDk v1.2 , add friendly obstructions
        ImageView adIconView = (ImageView) findViewById(ConstantValues.UI_ADICON_ID);
        ImageView adLogoView = (ImageView) findViewById(ConstantValues.UI_ADLOGO_ID);
        ImageView closeBtn = (ImageView) findViewById(ConstantValues.UI_CLOSEBTN_ID);

        mraidView.AddOMObstructions(adLogoView);
        mraidView.AddOMObstructions(adIconView);
        mraidView.AddOMObstructions(closeBtn);
        //start omsession, after add obstructions
        mraidView.startOMSession();
    }

    @Override
    public WebResourceResponse onShouldIntercept(String url) {
        if (null != adVGListener)
            return adVGListener.shouldInterceptRequest(url);
        return null;
    }

    @Override
    public void loadDataError(int errorType) {
        if (null != adVGListener)
            adVGListener.onAdFailed(null,"CustomError://" + errorType, true);
    }

    @Override
    public void onShouldOverride(String url) {
        // 至少触摸过才可以点击跳转
        try {
            if (adVGListener.getAdsBean().getTouchStatus() > MRAIDView.ACTION_DEFAULT) {
                if (null != adVGListener)
                    adVGListener.checkClick(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

}
