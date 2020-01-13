package com.kuaiyou.adbid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.spread.adapter.AdBIDSpreadAdapter;
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
import java.io.FileNotFoundException;


public class AdSpreadBIDView extends KyAdBaseView implements KySpreadListener {

    private ApplyAdBean applyAdBean = null;
    private int routeType = ConstantValues.ROUTE_ADBID_TYPE;
    private int logoRes;
    private String logoUriStr;
    private String adLogo;
    private String adIcon;
    private String appID;

    private String bitmapPath;
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

    private AdAdapterManager adAdapterManager = null;

    public AdSpreadBIDView(Context context, String key, ViewGroup view) {
        this(context, key, view, ConstantValues.ROUTE_ADBID_TYPE);
    }
    /**
     * @param context () Context
     * @param appID     Adview SDK KEY
     * @param view    attached view
     */
    public AdSpreadBIDView(Context context, String appID, ViewGroup view, int routeType) {
        super(context);
        setWillNotDraw(false);

        // 初始化参数
        //use instl 320x480 size
        super.adSize = ConstantValues.INSTL_REQ_SIZE_320X480;

        calcAdSize();
        this.routeType = routeType;
        density = AdViewUtils.getDensity(getContext());
        this.appID = appID;
        //applyAdBean = initRequestBean(appID, null, routeType, ConstantValues.SDK_REQ_TYPE_SPREAD, 1);

        // 初始化 起始界面
        spreadView = new SpreadView(getContext());
        spreadView.setSpreadViewListener(this);
        spreadView.init();
        //add view
        view.addView(spreadView, new ViewGroup.LayoutParams(-1, -1));

        if (!selfTestMode_Spread) {
            //wilder 2019 , if you want to debug spread ad, comment this line
            new TimeoutHandler(this).sendEmptyMessageDelayed(ConstantValues.SPREAD_REQ_INIT_SUCCESS, timeOutTime);
        }
        // 设置缓存大小
        try {
            if (Class.forName("android.support.v4.util.LruCache") != null
                    && Class.forName("android.support.v4.app.NotificationCompat") != null)
                ;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //Toast.makeText(getContext(), "请添加最新版的android-support-v4 或 v13.jar", Toast.LENGTH_SHORT).show();
            AdViewUtils.logInfo("!!! [AdSpreadBIDView] err: pls import newest version of android-support-v4.jar !!!");
            return;
        }
        //got gpid
        AdViewUtils.getDeviceIdFirstTime(context, this);

    }

    /**
     * new interface for request ad
     */
    private void requestAd() {
        long cacheTime;
        String configUrl;
        //2020 RequestBean的动作必须在fetch GPID之后
        applyAdBean = initRequestBean(appID, null, routeType, ConstantValues.SDK_REQ_TYPE_SPREAD, 1);

        configUrl = getConfigUrl(routeType);
        preferences = SharedPreferencesUtils.getSharedPreferences(getContext(),ConstantValues.SP_SPREADINFO_FILE);
        // 广告缓存时间 暂时不支持
        cacheTime = preferences.getLong("sp_cacheTime", 0);

        // 判断是否属于缓存时间内
        if (System.currentTimeMillis() / 1000 - cacheTime > ConstantValues.DEFAULT_CACHE_PEROID) {
            reqScheduler.execute(
                    new InitAdRunable( makeRequestBeanString(applyAdBean),
                            configUrl,
                            ConstantValues.SDK_REQ_TYPE_SPREAD));
        } else {
            reqScheduler.execute(
                    new InitAdRunable( preferences.getString("sp_cacheData", null),
                            ConstantValues.SDK_REQ_TYPE_SPREAD));
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
    }
    /**
     * for rtb
     */
    public void setLogo(String image) {
        if (null == image)
            return;
        logoUriStr = image;
    }
    /**
     * 获取logoView
     *
     * @return ImageView
     */
    public ImageView getLogoView() {
        return (ImageView) spreadView.findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID);
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
            case ConstantValues.NOTIFY_REQ_GPID_FETCH_DONE:
                requestAd();
                break;
            case ConstantValues.NOTIFY_RESP_RECEIVEAD_OK:
                switch (adsBean.getAdType()) {
                    case ConstantValues.RESP_ADTYPE_HTML:
                    case ConstantValues.RESP_ADTYPE_MIXED:
                    case ConstantValues.RESP_ADTYPE_SPREAD:
                        adAdapterManager = handlerAd(true, -1, ConstantValues.SDK_REQ_TYPE_SPREAD, null, this);
                        break;
                    default:
                        if (null != onAdSpreadListener) {
                            onAdSpreadListener.onAdRecieveFailed(this, "spread type error");
                        }
                        break;
                }
                break;
            case ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR:
                try {
                    if (null != adsBean && null != adsBean.getAgDataBean() && !TextUtils.isEmpty(adsBean.getAgDataBean().getAggsrc())) {
                        adAdapterManager = handlerAd(false, -1, ConstantValues.SDK_REQ_TYPE_SPREAD, adsBean.getAgDataBean(), this);
                        return;
                    }
                    //send to app side
                    if (null != onAdSpreadListener) {
                        onAdSpreadListener.onAdRecieveFailed(this, (String) msg.obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            case ConstantValues.RESP_ADTYPE_MIXED:
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
        /*
        try {
            //如果打开落地页，则当关闭落地页之后，需要延迟发送关闭消息给app
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        isClickFinished = true;
                        adAdapterManager.sendMessage(ConstantValues.SPREAD_RESP_LANDINGPAGE_CLOSEDSTATUS_CHECK);
                        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new IntentFilter(ConstantValues.ADWEBVIEW_BROADCAST_LANDINGPAGE_CLOSED_STATUS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        try {
            if (!isDisplayed) {
                //目前只准对电视红包
                if (spreadSettleType == SpreadSettleType.CPM) {
                    if (null != onAdSpreadListener) {
                        onAdSpreadListener.onAdDisplayed(this);
                    }
                    reportImpression(adsBean,respAdBean, applyAdBean, true);
                    isDisplayed = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //点击汇报
        reportClick(event, realX, realY, applyAdBean, adsBean, respAdBean);

        if (null != onAdSpreadListener) {
            //向app发送click消息
            onAdSpreadListener.onAdClicked(AdSpreadBIDView.this);
        }
        if (adsBean.getAdAct() != ConstantValues.RESP_ACT_DOWNLOAD) {
            adAdapterManager.removeMessage(ConstantValues.SPREAD_REQ_INIT_SUCCESS);
            adAdapterManager.removeMessage(ConstantValues.SPREAD_RESP_BITMAP_RECEIVED);
            isClickFinished = false;
        }
        //打开落地页
        isClickFinished = clickEvent(getContext(), adsBean, url);
        //wilder 2020 ,发送延迟可关闭的消息给app,因为目前采用的方式是custom tab，还不能获取关闭事件
        //因此在这里就延迟一下，通知app可以关闭spread了
        isClickFinished = true;
        adAdapterManager.sendMessage(ConstantValues.SPREAD_RESP_LANDINGPAGE_CLOSEDSTATUS_CHECK);
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
        //OMSDK v1.2 close session
        if (null != adAdapterManager) {
            ((AdBIDSpreadAdapter)adAdapterManager).getMRaidView().stopOMAdSession();
        }
        //omsdk ends
        //for omsdk , stop session need  some time, so webview should be destroyed wait for a while
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null != onAdSpreadListener) {
                    onAdSpreadListener.onAdSpreadPrepareClosed();
                }
                onAdSpreadListener = null;
            }
        },100);

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
        spreadView.findViewById(ConstantValues.UI_WEBVIEW_ID).dispatchTouchEvent(motionDown);
        spreadView.findViewById(ConstantValues.UI_WEBVIEW_ID).dispatchTouchEvent(motionUp);
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
        if (respAdBean.getSc() == 1)
            createConfirmDialog(getContext(), adsBean, null, true, null, null);
        return false;
    }

    @Override
    public void checkClick(String url) {
        clickCheck(url, adsBean, applyAdBean, respAdBean);
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
                    adAdapterManager = handlerAd(false, times, ConstantValues.SDK_REQ_TYPE_SPREAD, adsBean.getAgDataBeanList().get(times), this);
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

        reportImpression(adsBean, respAdBean, applyAdBean, true);
        if (null != onAdSpreadListener) {
            onAdSpreadListener.onAdDisplayed(this);
        }

        //OMSDK v1.2, this must be last called, cause muti-called will cause error
        if (null != adAdapterManager) {
            ((AdBIDSpreadAdapter)adAdapterManager).getMRaidView().sendOMImpression();
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
                adAdapterManager = handlerAd(false, msgCopy.arg1, ConstantValues.SDK_REQ_TYPE_SPREAD, agDataBean, this);
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
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    @Override
    public void mraidViewHasLoaded() {

        if (null != adAdapterManager) {
            ((AdBIDSpreadAdapter)adAdapterManager).mraidViewHasLoaded();
        }
    }
    /////////////   ========  end KySpreadListener ============== //////////////////////////////////

}
