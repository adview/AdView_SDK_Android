package com.kuaiyou.adbid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.adbid.banner.adapter.AdBIDBannerAdapter;
import com.kuaiyou.adbid.banner.adapter.AdBaiduBannerAdapter;
import com.kuaiyou.adbid.banner.adapter.AdGDTBannerAdapter;
import com.kuaiyou.adbid.banner.adapter.AdTTAdBannerAdapter;
import com.kuaiyou.adbid.instl.adapter.AdBIDInstlAdapter;
import com.kuaiyou.adbid.instl.adapter.AdBaiduInstlAdapter;
import com.kuaiyou.adbid.instl.adapter.AdGDTInstlAdapter;
import com.kuaiyou.adbid.instl.adapter.AdTTAdInstlAdapter;
import com.kuaiyou.adbid.nativee.adapter.AdBIDNativeAdapter;
import com.kuaiyou.adbid.nativee.adapter.AdBaiduNativeAdapter;
import com.kuaiyou.adbid.nativee.adapter.AdGDTNativeAdapter;
import com.kuaiyou.adbid.nativee.adapter.AdGDTNativeExpressAdapter;
import com.kuaiyou.adbid.spread.adapter.AdBIDSpreadAdapter;
import com.kuaiyou.adbid.spread.adapter.AdBaiduSpreadAdapter;
import com.kuaiyou.adbid.spread.adapter.AdGDTSpreadAdapter;
import com.kuaiyou.adbid.spread.adapter.AdTTAdSpreadAdapter;
import com.kuaiyou.adbid.video.adapter.AdBIDVideoAdapter;
import com.kuaiyou.interfaces.KyNativeListener;
import com.kuaiyou.interfaces.KyVideoListener;
import com.kuaiyou.interfaces.KyViewListener;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.adbid.video.adapter.AdTTVideoAdapter;
import com.kuaiyou.video.vast.VASTPlayer;
import com.kuaiyou.video.vast.VASTPlayerListener;

import java.lang.reflect.Constructor;
import java.util.List;

public abstract class AdAdapterManager implements VASTPlayerListener {
    public final static String GDT_TYPE = "1006";
    public final static String BAIDU_TYPE = "1007";
    public final static String TOUTIAO_TYPE = "1008";
    public final static String BID_TYPE = "9999";

    private boolean hasResult = false;

    public final static int CHECKTIMEOUT = 1;
    public final static int CHECKRESULT = 2;


    protected AgDataBean agDataBean;

    private KyViewListener onAdListener;
    private KyNativeListener nativeListener;
    private KyVideoListener kyVideoListener;

    private TimeoutHandler timeoutHandler;
    protected VASTPlayer newPlayer;

    public AdAdapterManager() {

        timeoutHandler = new TimeoutHandler();
    }


    public void setNativeCallback(KyNativeListener nativeListener) {
        this.nativeListener = nativeListener;
    }

    public void setCallback(KyViewListener onAdListener) {
        this.onAdListener = onAdListener;
    }

    public void setVideoCallback(KyVideoListener kyVideoListener) {
        this.kyVideoListener = kyVideoListener;
    }

    public static AdAdapterManager initAd(Context context, int sdkType, String adType) {
        return initAd(context, sdkType, adType, 0);
    }

    public static AdAdapterManager initAd(Context context, int sdkType, String adType, int express) {
        AdAdapterManager adViewAdapter = null;
        try {
            Constructor<? extends AdAdapterManager> constructor = getAdapterClass(sdkType, adType, express)
                    .getConstructor();
            adViewAdapter = constructor.newInstance(); // 构造函数
            adViewAdapter.initAdapter(context);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return adViewAdapter;
    }


    public int getSufId() {
        return 0;
    }

    public void closeInstl() {
    }

    public void setTimeoutListener(int times, AgDataBean agDataBean) {
        AdViewUtils.logInfo("src= " + agDataBean.getAggsrc() + ";" + times);
        this.agDataBean = agDataBean;
        if (!timeoutHandler.hasMessages(CHECKTIMEOUT) && !timeoutHandler.hasMessages(CHECKTIMEOUT)) {
            Message msg = new Message();
            msg.what = CHECKTIMEOUT;
            msg.arg1 = ++times;
            msg.obj = agDataBean;
            timeoutHandler.sendMessage(msg);
        }

    }

    class TimeoutHandler extends Handler {
        public TimeoutHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHECKRESULT:
                    if (!hasResult) {
                        if (null != agDataBean)
                            KyAdBaseView.reportOtherUrls(agDataBean.getFailUrls());
                        if (null != onAdListener)
                            onAdListener.rotatedAd(msg);
                        if (null != nativeListener)
                            nativeListener.rotatedAd(msg);
                        onAdListener = null;
                        nativeListener = null;
                    }
                    break;
                case CHECKTIMEOUT:
                    Message msgCopy = Message.obtain(msg);
                    msgCopy.what = CHECKRESULT;
                    sendMessageDelayed(msgCopy, 5 * 1000);
                    break;
            }

        }
    }

    public void cancelTimeoutCheck() {
        if (null != timeoutHandler) {
            timeoutHandler.removeMessages(CHECKRESULT);
            timeoutHandler.removeMessages(CHECKTIMEOUT);
        }
    }


    public boolean playVideo(Context context) {

        return true;
    }

    public boolean showInstl(Activity activity) {

        return false;
    }

    public int getInstlWidth() {
        return 0;
    }

    public int getInstlHeight() {
        return 0;
    }

    public View getInstlView() {
        return null;
    }

    public View getDialogView() {
        return null;
    }

    private static Class<? extends AdAdapterManager> getAdapterClass(int sdkType, String type, int express) {
        switch (sdkType) {
            case ConstantValues.BANNERTYPE:
                if (type.equals(BID_TYPE))
                    return AdBIDBannerAdapter.class;//return AdBIDBannerAdapter.class;
                else if (type.equals(GDT_TYPE))
                    return AdGDTBannerAdapter.class;//return AdGDTBannerAdapter.class;
                else if (type.equals(BAIDU_TYPE))
                    return AdBaiduBannerAdapter.class;//return AdBIDBannerAdapter.class;
                else if (type.equals(TOUTIAO_TYPE))
                    return AdTTAdBannerAdapter.class;//return AdBIDBannerAdapter.class;
                break;
            case ConstantValues.INSTLTYPE:
                if (type.equals(BID_TYPE))
                    return AdBIDInstlAdapter.class;//return AdBIDBannerAdapter.class;
                else if (type.equals(GDT_TYPE))
                    return AdGDTInstlAdapter.class;//return AdGDTBannerAdapter.class;
                else if (type.equals(BAIDU_TYPE))
                    return AdBaiduInstlAdapter.class;//return AdGDTBannerAdapter.class;
                else if (type.equals(TOUTIAO_TYPE))
                    return AdTTAdInstlAdapter.class;//return AdGDTBannerAdapter.class;
                break;
            case ConstantValues.SPREADTYPE:
                if (type.equals(BID_TYPE))
                    return AdBIDSpreadAdapter.class;
                else if (type.equals(GDT_TYPE))
                    return AdGDTSpreadAdapter.class;
                else if (type.equals(BAIDU_TYPE))
                    return AdBaiduSpreadAdapter.class;
                else if (type.equals(TOUTIAO_TYPE))
                    return AdTTAdSpreadAdapter.class;
                break;
            case ConstantValues.NATIVEADTYPE:
                if (type.equals(BID_TYPE))
                    return AdBIDNativeAdapter.class;
                else if (type.equals(GDT_TYPE)) {
                    if (express == 1)
                        return AdGDTNativeExpressAdapter.class;
                    return AdGDTNativeAdapter.class;
                } else if (type.equals(BAIDU_TYPE))
                    return AdBaiduNativeAdapter.class;
                break;
            case ConstantValues.VIDEOTYPE:
                if (type.equals(BID_TYPE))
                    //return AdTTVideoAdapter.class;
                    return AdBIDVideoAdapter.class;
                else if (type.equals(TOUTIAO_TYPE))
                    return AdTTVideoAdapter.class;
                break;
        }
        return null;
    }

    public abstract View getAdView();

    public void cancelSpreadAd() {
    }

    public void sendMessage(int msg) {
    }

    public void removeMessage(int msg) {
    }

    public void reportClick(Object... o) {
        if (null != agDataBean)
            KyAdBaseView.reportOtherUrls(agDataBean.getCliUrls());
    }

    public void reportImpression(Object... o) {
        if (null != agDataBean)
            KyAdBaseView.reportOtherUrls(agDataBean.getImpUrls());
    }

    protected abstract void initAdapter(Context context);

    public abstract void handleAd(Context context, Bundle bundle);

    public boolean isCloseble() {
        return true;
    }

    protected void onAdDisplay(boolean force) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("+++ AdadapterManager：onAdDisplay(): src= " + agDataBean.getAggsrc());
            if (null != onAdListener)
                onAdListener.onDisplay(agDataBean, force);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onAdDisplay() {
        onAdDisplay(false);
    }


    protected void onAdFailed(String msg, boolean force) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onAdFailed src=" + agDataBean.getAggsrc() + " ;msg=" + msg);
            hasResult = true;
            cancelTimeoutCheck();
            if (null != onAdListener)
                onAdListener.onAdFailed(agDataBean, msg, force);
            if (null != kyVideoListener)
                kyVideoListener.onAdFailed(agDataBean, msg, force);
            if (null != nativeListener)
                nativeListener.onAdFailed(agDataBean, msg, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onAdFailed(String msg) {
        hasResult = true;
        cancelTimeoutCheck();
        onAdFailed(msg, false);
    }

    protected void onAdRecieved(boolean force) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onAdRecieved src=" + agDataBean.getAggsrc());
            hasResult = true;
            cancelTimeoutCheck();
            if (null != onAdListener)
                onAdListener.onReceived(agDataBean, force);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onAdRecieved() {
        hasResult = true;
        cancelTimeoutCheck();
        onAdRecieved(false);
    }


    public void onAdClick(MotionEvent e, String url, float downX, float downY) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onAdClick src=" + agDataBean.getAggsrc());
            if (null != onAdListener)
                onAdListener.onViewClicked(e, agDataBean, url, downX, downY);
            if (null != kyVideoListener)
                kyVideoListener.onVideoClicked(agDataBean);
            //暂时缺少原生的点击接口
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    protected void onAdClosed() {
        onAdClosed(null);
    }

    protected void onAdClosed(View view) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onAdClosed src=" + agDataBean.getAggsrc());
            if (null != onAdListener)
                onAdListener.onCloseBtnClicked();
            if (null != nativeListener)
                nativeListener.onNativAdClosed(view);
            if (null != kyVideoListener)
                kyVideoListener.onVideoPlayFinished(agDataBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onAdReady(boolean force) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onAdReady src=" + agDataBean.getAggsrc());
            if (null != onAdListener)
                onAdListener.onReady(agDataBean, force);
            if (null != nativeListener)
                nativeListener.onReady(agDataBean, force);
            if (null != kyVideoListener)
                kyVideoListener.onReady(agDataBean, force);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onAdReady() {
        try {
            onAdReady(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void onAdReturned(List list) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onAdReturned src=" + agDataBean.getAggsrc());
            hasResult = true;
            cancelTimeoutCheck();
            if (null != nativeListener)
                nativeListener.onNativeAdReturned(agDataBean, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onAdStatusChanged(int status) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onAdStatusChanged src=" + agDataBean.getAggsrc() + " ;status=" + status);
            if (null != nativeListener)
                nativeListener.onNativeStatusChange(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void onVideoFinished() {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onVideoFinished src=" + agDataBean.getAggsrc());
            if (null != kyVideoListener)
                kyVideoListener.onVideoPlayFinished(agDataBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onVideoStartPlay() {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onVideoStartPlay src=" + agDataBean.getAggsrc());
            if (null != kyVideoListener)
                kyVideoListener.onVideoPlayStarted(agDataBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onVideoReceieved(String vast) {
        try {
            if (null != agDataBean)
                AdViewUtils.logInfo("onVideoReceieved src=" + agDataBean.getAggsrc());
            if (null != kyVideoListener) {
                kyVideoListener.onVideoReceived(vast);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onVideoDownloadCancel() {
        if (null != kyVideoListener)
            kyVideoListener.onDownloadCancel();
    }

    //wilder 2019 for mrec
    protected  void onAdPlayReady(Bundle bundle) {
        if (null != kyVideoListener) {
            kyVideoListener.onVideoPlayReady(bundle);
        }
    }

    public void destroyAd() {
    }
    ////////////////////////////// add video interface //////////////////////////////////////////

    /////////////////////////////////(wilder 2019) ///////////////////////////////////////////
    ///////////////////////////////VAST Player Listener /////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void vastDownloadReady(){
        this.onAdReady(); //wilder 2019 ,notify ready event to App
        //wilder 2019 for test mrec
    }

    @Override
    public void vastDownloadCancel() {
        AdViewUtils.logInfo("download canceled");
        this.onVideoDownloadCancel();
    }
    @Override
    public void vastParseDone(VASTPlayer mp){
        /*
        if ( adsBean.getAdType() == ConstantValues.VIDEO_PASTER) {
            onVideoReceieved(adsBean.getVast());
        }
        */
        onVideoReceieved("");
        if (mp != null) {
            newPlayer = mp;
            this.newPlayer.downloadVideo(0, 0);  //wilder 2019 start download 1st video
        }
    }

    @Override
    public void vastError(int error){
        AdViewUtils.logInfo("vastError");
        this.onAdFailed("error: " + error);
    }
    @Override
    public void vastClick(){
        //onAdClick(null,null, 0,0); //wilder 2019
        //AdViewUtils.logInfo("vastClick");
    }
    @Override
    public void vastComplete(){
        AdViewUtils.logInfo("vastComplete");
        this.onVideoFinished();
    }
    @Override
    public void vastDismiss(){
        AdViewUtils.logInfo("vastDismiss");
        this.onAdClosed();

    }
    @Override
    public int vastOrientationChange(){
        return 0;
    }
    @Override
    public boolean vastAutoCloseEnableChange(){
        return false;
    }
    @Override
    public int getCachePriod(){
        return 0;
    }
    @Override
    public void vastPlayReady(Bundle bundle) {

        this.onAdPlayReady(bundle); //through kyvideolistener pass to caller, such as bunnerView etc.

        this.onVideoStartPlay(); //start play event
    }
    ///////////////////end VAST listener ////////////////////////////////////

}
