package com.kuaiyou.adbid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.interfaces.KySpreadListener;
import com.kuaiyou.interfaces.OnAdViewListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ApplyAdBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.SharedPreferencesUtils;
import com.kuaiyou.utils.SpreadView;
import com.kuaiyou.utils.SpreadViewTVHongBao;
import java.io.FileNotFoundException;


public class AdSpreadBIDView extends KyAdBaseView implements KySpreadListener {

    private ApplyAdBean applyAdBean = null;
    private int routeType = ConstantValues.ADBID_TYPE;
    private int logoRes;
    private String logoUriStr;
    private String adLogo;
    private String adIcon;

    private String bitmapPath;
    //    private Rect touchRect = null;
    private SharedPreferences preferences = null;
    private SpreadView spreadView;
    private boolean gotData;

    private int timeOutTime = 10 * 1000; //wilder 2019 , seconds

    // 顶部倒计时/跳过 功能类型
    public final static int NOTIFY_COUNTER_NULL = 0;
    public final static int NOTIFY_COUNTER_NUM = 1;
    public final static int NOTIFY_COUNTER_TEXT = 2;
    public final static int NOTIFY_COUNTER_CUSTOM = 3;

    private boolean isDisplayed = false;
    private boolean isClickFinished = true;
    private int notifyType = NOTIFY_COUNTER_NUM;

    private AdAdapterManager adAdapterManager;

    public AdSpreadBIDView(Context context, String key, ViewGroup view, String gdpr) {
        this(context, key, view, ConstantValues.ADBID_TYPE, gdpr);
    }
    /**
     * @param context () Context
     * @param appID     Adview SDK KEY
     * @param view    attached view
     */
    public AdSpreadBIDView(Context context, String appID, ViewGroup view, int routeType, String gdpr) {
        super(context);
        setWillNotDraw(false);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            try {
//                WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
//                lp.layoutInDisplayCutoutMode =
//                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//                ((Activity) context).getWindow().setAttributes(lp);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        // 初始化参数
        String configUrl;
        long cacheTime;
        //use instl 320x480 size
        super.adSize = ConstantValues.INSTL_320X480;

        setGDPRConstent(gdpr);   //GDPR
        calcAdSize();

        this.routeType = routeType;
        density = AdViewUtils.getDensity(getContext());
        configUrl = getConfigUrl(routeType);

        applyAdBean = initApplyBean(appID, null, routeType, ConstantValues.SPREADTYPE, 1);

        // 初始化 起始界面
        if (getContext().getPackageName().equals("com.tvmining.yao8")) {
            spreadSettleType = SpreadSettleType.CPM;
            spreadView = new SpreadViewTVHongBao(getContext());
        } else {
            spreadView = new SpreadView(getContext());
        }
        spreadView.setSpreadViewListener(this);
        spreadView.init();
        //add view
        view.addView(spreadView, new ViewGroup.LayoutParams(-1, -1));

        if (!selfTestMode_Spread) {
            //wilder 2019 , if you want to debug spread ad, comment this line
            new TimeoutHandler(this).sendEmptyMessageDelayed(ConstantValues.INITSUCCESS, timeOutTime);
        }
        // 设置缓存大小
        try {
            if (Class.forName("android.support.v4.util.LruCache") != null
                    && Class.forName("android.support.v4.app.NotificationCompat") != null)
                ;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "请添加最新版的android-support-v4 或 v13.jar",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        preferences = SharedPreferencesUtils.getSharedPreferences(getContext(),ConstantValues.SP_SPREADINFO);
        // 广告缓存时间 暂时不支持
        cacheTime = preferences.getLong("sp_cacheTime", 0);
        // 判断是否属于缓存时间内
        if (System.currentTimeMillis() / 1000 - cacheTime > ConstantValues.DEFAULTCACHEPEROID) {
            reqScheduler.execute(
                    new InitAdRunable( getApplyInfoContent(applyAdBean),
                                        configUrl,
                                        ConstantValues.SPREADTYPE));
        } else {
            reqScheduler.execute(
                    new InitAdRunable( preferences.getString("sp_cacheData", null),
                                        ConstantValues.SPREADTYPE));
        }
    }

    class TimeoutHandler extends Handler {
        private AdSpreadBIDView adSpreadBIDView;

        public TimeoutHandler(AdSpreadBIDView adSpreadBIDView) {
            super(Looper.getMainLooper());
            this.adSpreadBIDView = adSpreadBIDView;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!adSpreadBIDView.gotData) {
                if (null != onAdSpreadListener) {
                    onAdSpreadListener.onAdRecieveFailed(adSpreadBIDView, "ad timeout");
                }
                onAdSpreadListener = null;
            }
        }
    }

    public void setSpreadNotifyType(int type) {
        notifyType = type;
    }

    public void setBackgroundColor(int color) {
        setBackground(new ColorDrawable(color));
    }

    public void setLogo(int id) {
        if (0 == id)
            return;
        logoRes = id;
        spreadView.updateLogo();
//        spreadView.init();
    }
    /**
     * for rtb
     */
    public void setLogo(String image) {
        if (null == image)
            return;
        logoUriStr = image;
//        spreadView.init();
    }
    /**
     * 获取logoView
     *
     * @return ImageView
     */
    public ImageView getLogoView() {
        return (ImageView) spreadView.findViewById(ConstantValues.SPREADLOGOIMAGEID);
    }
    /**
     * cancel spread ad
     */
    public void cancelAd() {
        try {
            adAdapterManager.cancelSpreadAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            spreadView.removeAllViews();
            spreadView = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSpreadSettleType(SpreadSettleType st) {
        spreadSettleType = st;
    }

    //    public RelativeLayout getParentLayout() {
//        try {
//            return spreadView.findViewById(ConstantValues.SPREADNOTIFYLAYOUT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public View getParentLayout() {
        try {
            return (View) spreadView.getParent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////// ================== KyAdBaseView =====================================///////////////////////

    @Override
    protected void handlerMsgs(Message msg) {
        gotData = true;
        switch (msg.what) {
            case ConstantValues.NOTIFYRECEIVEADOK:
                switch (adsBean.getAdType()) {
                    case ConstantValues.HTML:
                    case ConstantValues.MIXED:
                    case ConstantValues.SPREAD:
                        adAdapterManager = handlerAd(true, -1, ConstantValues.SPREADTYPE, null, this);
                        break;
                    default:
                        if (null != onAdSpreadListener) {
                            onAdSpreadListener.onAdRecieveFailed(this, "spread type error");
                        }
                        break;
                }
                break;
            case ConstantValues.NOTIFYRECEIVEADERROR:
                try {
                    if (null != adsBean && null != adsBean.getAgDataBean() && !TextUtils.isEmpty(adsBean.getAgDataBean().getAggsrc())) {
                        adAdapterManager = handlerAd(false, -1, ConstantValues.SPREADTYPE, adsBean.getAgDataBean(), this);
                        return;
                    }
                    //send to app side
                    if (null != onAdSpreadListener) {
                        onAdSpreadListener.onAdRecieveFailed(this, (String) msg.obj);
                    }
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean initAdLogo(Object object) {
        AdsBean adsBean = (AdsBean) object;
        if (null == adsBean)
            return false;
        if (!TextUtils.isEmpty(adsBean.getAdLogoUrl())) {
            adLogo = (String) AdViewUtils.getInputStreamOrPath(getContext(), adsBean.getAdLogoUrl(), 1);
        }
        if (!TextUtils.isEmpty(adsBean.getAdIconUrl())) {
            adIcon = (String) AdViewUtils.getInputStreamOrPath(getContext(), adsBean.getAdIconUrl(), 1);
        }
        return true;
    }

    @Override
    protected boolean createBitmap(Object object) {
        switch (adsBean.getAdType()) {
            case ConstantValues.MIXED:
                if (!TextUtils.isEmpty(adsBean.getAdIcon()))
                    return true;
            default:
                if (!TextUtils.isEmpty(adsBean.getAdPic()))
                    return true;
        }
        return false;
    }

    @Override
    protected void handleClick(MotionEvent event, int realX, int realY, String url) {
        try {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        isClickFinished = true;
                        adAdapterManager.sendMessage(ConstantValues.CLOSEDSTATUSCHECK);
//                        handler.sendEmptyMessage(ConstantValues.CLOSEDSTATUSCHECK);
                        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new IntentFilter(ConstantValues.ADWEBVIEW_CLOSED_STATUS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (!isDisplayed) {
                //目前只准对电视红包
                if (spreadSettleType == SpreadSettleType.CPM) {
                    if (null != onAdSpreadListener) {
                        onAdSpreadListener.onAdDisplayed(this);
                    }
                    reportImpression(adsBean,retAdBean, applyAdBean, true);
                    isDisplayed = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        reportClick(event, realX, realY, applyAdBean, adsBean, retAdBean);

        if (null != onAdSpreadListener) {
            onAdSpreadListener.onAdClicked(AdSpreadBIDView.this);
        }
        if (adsBean.getAdAct() != ConstantValues.ACT_DOWNLOAD) {
            adAdapterManager.removeMessage(ConstantValues.INITSUCCESS);
            adAdapterManager.removeMessage(ConstantValues.BITMAPRECIEVED);
            isClickFinished = false;
        }
        isClickFinished = clickEvent(getContext(), adsBean, url);
    }

    @Override
    public String getBitmapPath() {
        return bitmapPath;
    }

    @Override
    public void setOnAdSpreadListener(OnAdViewListener onAdViewListener) {
        super.setOnAdSpreadListener(onAdViewListener);
    }

    @Override
    public void onCloseBtnClicked() {
        if (null != onAdSpreadListener) {
            onAdSpreadListener.onAdSpreadPrepareClosed();
        }
        onAdSpreadListener = null;
    }

    @Override
    public void onViewClicked(MotionEvent e, AgDataBean agDataBean, String url, float downX, float downY) {
        if (downX == 888 && downY == 888) {
            if (null != agDataBean)
                reportOtherUrls(agDataBean.getCliUrls());

            if (null != onAdSpreadListener)
                onAdSpreadListener.onAdClicked(AdSpreadBIDView.this);
            return;
        }
        long detlaTime = 50;
        long downTime = System.currentTimeMillis();
        MotionEvent motionDown = MotionEvent.obtain(downTime + detlaTime, downTime + detlaTime, MotionEvent.ACTION_DOWN, 1, 1, 0);//adHeight-((adHeight*140)/(600*2))
        MotionEvent motionUp = MotionEvent.obtain(downTime + detlaTime, downTime + detlaTime + 50, MotionEvent.ACTION_UP, 1, 1, 0);
        spreadView.findViewById(ConstantValues.WEBVIEWID).dispatchTouchEvent(motionDown);
        spreadView.findViewById(ConstantValues.WEBVIEWID).dispatchTouchEvent(motionUp);
        motionDown.recycle();
        motionUp.recycle();
    }

    @Override
    public boolean isClickableConfirm() {
        return isClickFinished;
    }

    @Override
    public void setClickMotion(MRAIDView view, Rect touchRect) {
        setClickMotion(view, adsBean, touchRect);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(String url) {
        return null;
    }

    @Override
    public boolean needConfirmDialog() {
        if (retAdBean.getSc() == 1)
            createConfirmDialog(getContext(), adsBean, null, true, null, null);
        return false;
    }

    @Override
    public void checkClick(String url) {
        clickCheck(url, adsBean, applyAdBean, retAdBean);
    }

    @Override
    public void onReady(AgDataBean agDataBean, boolean force) {
        if (null != onAdSpreadListener)
            onAdSpreadListener.onAdReady(this);
    }

    @Override
    public void onReceived(AgDataBean agDataBean, boolean force) {
        try {
            if (null != onAdSpreadListener)
                onAdSpreadListener.onAdRecieved(this);
            if (null != agDataBean && null != agDataBean.getSuccUrls())
                reportOtherUrls(agDataBean.getSuccUrls());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdFailed(AgDataBean agDataBean, String error, boolean force) {
        try {

            if (error.startsWith("CustomError://"))
                reportLoadError(adsBean, applyAdBean.getAppId(), Integer.valueOf(error.replace("CustomError://", "")));
            else {
                if (null != agDataBean && null != agDataBean.getFailUrls())
                    reportOtherUrls(agDataBean.getFailUrls());

                int times = getAgDataBeanPosition(adsBean, agDataBean);
                if (times != -1) {
                    adAdapterManager = handlerAd(false, times, ConstantValues.SPREADTYPE, adsBean.getAgDataBeanList().get(times), this);
                    return;
                }
                if (null != onAdSpreadListener) {
                    onAdSpreadListener.onAdRecieveFailed(this, error);
                }
                onAdSpreadListener = null;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisplay(AgDataBean agDataBean, boolean force) {
        if (null != agDataBean && null != agDataBean.getImpUrls()) {
            reportOtherUrls(agDataBean.getImpUrls());
        }

        reportImpression(adsBean, retAdBean, applyAdBean, true);
        if (null != onAdSpreadListener) {
            onAdSpreadListener.onAdDisplayed(this);
        }
    }
    @Override
    public boolean getCloseble() {
        return false;
    }
    @Override
    public String getAdLogo() {
        return adLogo;
    }
    @Override
    public String getAdIcon() {
        return adIcon;
    }

    @Override
    public AdsBean getAdsBean() {
        return adsBean;
    }
    @Override
    public void rotatedAd(Message msg) {
        try {
            Message msgCopy = Message.obtain(msg);
            if (null == adsBean.getAgDataBeanList()) {
                if (null != onAdSpreadListener) {
                    onAdSpreadListener.onAdRecieveFailed(this, "backup list is null");
                }
                onAdSpreadListener = null;
                return;
            }
            if (msgCopy.arg1 < adsBean.getAgDataBeanList().size()) {
                AgDataBean agDataBean = adsBean.getAgDataBeanList().get(msgCopy.arg1);
                adAdapterManager = handlerAd(false, msgCopy.arg1, ConstantValues.SPREADTYPE, agDataBean, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (null != onAdSpreadListener) {
                onAdSpreadListener.onAdRecieveFailed(this, "backup list is null");
            }
            onAdSpreadListener = null;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////================================ KySpreadListener =============================================/////
    @Override
    public String getBehaveIcon() {
        return getActIcon(adsBean.getAdAct());
    }
    @Override
    public int getNotifyType() {
        return notifyType;
    }
    @Override
    public Drawable getSpreadLogo() {
        Drawable d = null;
        try {
            if (0 != logoRes)
                d = getContext().getResources().getDrawable(logoRes);
            if (null == d && !TextUtils.isEmpty(logoUriStr))
                d = Drawable.createFromStream(getContext().getContentResolver().openInputStream(Uri.parse(logoUriStr)), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return d;
    }
    @Override
    public SpreadView getSpreadView() {
        return spreadView;
    }
    @Override
    public SpreadView getSkipView() {
        return null;
    }
    @Override
    public void onAdNotifyCustomCallback(ViewGroup view, int ruleTime, int delayTime) {
        if (null != onAdSpreadListener)
            onAdSpreadListener.onAdNotifyCustomCallback(view, ruleTime, delayTime);
    }
    /////////////   ========  end KySpreadListener ============== //////////////////////////////////

}
