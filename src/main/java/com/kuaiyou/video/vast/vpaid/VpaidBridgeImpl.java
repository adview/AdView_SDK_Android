package com.kuaiyou.video.vast.vpaid;

import android.support.annotation.Keep;
import android.webkit.JavascriptInterface;

import com.kuaiyou.utils.AdViewUtils;


@Keep
@SuppressWarnings("unused")
public class VpaidBridgeImpl implements VpaidBridge {

    private static final String LOG_TAG = VpaidBridgeImpl.class.getSimpleName();

    private final BridgeEventHandler mBridge;
    private final CreativeParams mCreativeParams;
    private final static String WrapperInstance = "adVPAIDWrapperInstance";
    public VpaidBridgeImpl(BridgeEventHandler eventHandler, CreativeParams creativeParams) {
        mBridge = eventHandler;
        mCreativeParams = creativeParams;
    }

    //region VpaidBridge methods
    @Override
    public void prepare() {
        AdViewUtils.logInfo( "call initVpaidWrapper()");
        callJsMethod("initVpaidWrapper()");
    }

    @Override
    public void startAd() {
        AdViewUtils.logInfo( "-------- call startAd() --------");
        callWrapper("startAd()");
    }

    @Override
    public void stopAd() {
        AdViewUtils.logInfo( "-------- call stopAd() -----------");
        callWrapper("stopAd()");
    }


    @Override
    public void pauseAd() {
        AdViewUtils.logInfo( "call pauseAd()");
        callWrapper("pauseAd()");
    }

    @Override
    public void resumeAd() {
        AdViewUtils.logInfo( "call resumeAd()");
        callWrapper("resumeAd()");
    }

    @Override
    public void skipAd() {
        AdViewUtils.logInfo( "call skipAd()");
        callWrapper("skipAd()");
    }

    @Override
    public void getAdSkippableState() {
        AdViewUtils.logInfo( "call getAdSkippableState()");
        callWrapper("getAdSkippableState()");
    }

    @Override
    public void setAdVolume(float vol) {
        AdViewUtils.logInfo( "call setAdVolume()");

        String requestParam = "setAdVolume(" + "%1$f)";
        String requestFinal = String.format(requestParam, vol);
        callWrapper(requestFinal);
    }

    @Override
    public void getAdVolume() {
        //it will cause getAdVolumeResult()
        AdViewUtils.logInfo( "call getAdVolume()");
        callWrapper("getAdVolume()");
    }
    //endregion

    //region Helpers
    private void runOnUiThread(Runnable runnable) {

        mBridge.runOnUiThread(runnable);
    }

    private void callJsMethod(final String url) {

        mBridge.callJsMethod(url);
    }

    private void callWrapper(String method) {

        callJsMethod(WrapperInstance + "." + method);
    }
    //endregion

    //region JsCallbacks
    @JavascriptInterface
    public void wrapperReady() {

        initAd();
    }

    private void initAd() {
        AdViewUtils.logInfo( "JS: call initAd()");
        String requestTemplate = "initAd(" +
                "%1$d," + // width
                "%2$d," + // height
                "%3$s," + // viewMode
                "%4$s," + // desiredBitrate
                "%5$s," + // creativeData
                "%6$s)"; // environmentVars
        String requestFinal = String.format(requestTemplate,
                mCreativeParams.getWidth(),
                mCreativeParams.getHeight(),
                mCreativeParams.getViewMode(),
                mCreativeParams.getDesiredBitrate(),
                mCreativeParams.getCreativeData(),
                mCreativeParams.getEnvironmentVars()
        );
        callWrapper(requestFinal);
    }

    @JavascriptInterface
    public String handshakeVersionResult(String result) {
        AdViewUtils.logInfo( "JS: handshakeVersion()");
        return result;
    }

    @JavascriptInterface
    public void vpaidAdLoaded() {
        AdViewUtils.logInfo( "JS: vpaidAdLoaded");
        mBridge.onPrepared();
    }

    @JavascriptInterface
    public void vpaidAdStarted() {

        AdViewUtils.logInfo( "+++ JS: vpaidAdStarted +++");
    }

    @JavascriptInterface
    public void initAdResult() {

        AdViewUtils.logInfo( "++++ JS: Init ad done +++");
    }

    @JavascriptInterface
    public void vpaidAdError(String message) {
        AdViewUtils.logInfo( "+++++ JS: vpaidAdError = " + message + " ++++++");
        mBridge.trackError(message);
    }

    @JavascriptInterface
    public void vpaidAdLog(String message) {

        AdViewUtils.logInfo( "========= JS: vpaidAdLog = " + message + "=======");
    }

    @JavascriptInterface
    public void vpaidAdUserAcceptInvitation() {
        AdViewUtils.logInfo( "JS: vpaidAdUserAcceptInvitation");
    }

    @JavascriptInterface
    public void vpaidAdUserMinimize() {
        //最小化, wilder 2019 for omsdk
        mBridge.postEvent(EventConstants.MINIMIZE);
        AdViewUtils.logInfo( "JS: vpaidAdUserMinimize");
    }

    @JavascriptInterface
    public void vpaidAdUserClose() {

        AdViewUtils.logInfo( "+++ JS: vpaidAdUserClose +++");

        mBridge.onSelfClosed();
    }

    @JavascriptInterface
    public void vpaidAdSkippableStateChange() {
        //not use, it should be use getskippableState ,see js
        AdViewUtils.logInfo( "++++ JS: vpaidAdSkippableStateChange ++++");
    }

    @JavascriptInterface
    public void vpaidAdExpandedChange() {
        //扩展开广告, wilder 2019 for omsdk
        mBridge.postEvent(EventConstants.EXPANDED_CHANGE);
        AdViewUtils.logInfo( "+++ JS: vpaidAdExpandedChange +++");
    }

    @JavascriptInterface
    public void getAdExpandedResult(String result) {
        AdViewUtils.logInfo( "+++ JS: getAdExpandedResult: " + result +"+++");
    }

    @JavascriptInterface
    public void vpaidAdSizeChange() {

        AdViewUtils.logInfo( "++++ JS: vpaidAdSizeChange +++++++");
    }

    @JavascriptInterface
    public void vpaidAdDurationChange() {
        AdViewUtils.logInfo( "JS: vpaidAdDurationChange");
        callWrapper("getAdDurationResult");
        mBridge.onDurationChanged();
    }

    @JavascriptInterface
    public void vpaidAdRemainingTimeChange() {
        AdViewUtils.logInfo( "JS: vpaidAdRemainingTimeChange");
        callWrapper("getAdRemainingTime()");
    }

    @JavascriptInterface
    public void vpaidAdLinearChange() {
        AdViewUtils.logInfo( "JS: vpaidAdLinearChange");
        mBridge.onAdLinearChange();
    }

    @JavascriptInterface
    public void vpaidAdPaused() {
        AdViewUtils.logInfo( "JS: vpaidAdPaused");
        mBridge.postEvent(EventConstants.PAUSE);
    }

    @JavascriptInterface
    public void vpaidAdVideoStart() {
        AdViewUtils.logInfo( "JS: vpaidAdVideoStart");
        mBridge.postEvent(EventConstants.START);
    }

    @JavascriptInterface
    public void vpaidAdPlaying() {
        AdViewUtils.logInfo( "JS: vpaidAdPlaying");
        mBridge.postEvent(EventConstants.RESUME);
    }

    @JavascriptInterface
    public void vpaidAdClickThruIdPlayerHandles(String url, String id, boolean playerHandles) {

        /*        • If event.data.playerHandles is true and e.data.url is:
                a. Not defined: then the video player must use the VAST element,
                VideoClicks/ClickThrough.
                b. Defined: then the video player must use event.data.url.
                • If event.data.playerHandles is false, then the video player doesn’t open
                the landing page URL. The ad unit is responsible for opening the landing page URL in
                a new window in this case.
         */
        if (playerHandles) {
            AdViewUtils.logInfo("+++++++ JS: vpaidAdClickThruIdPlayerHandles():url=" + url + ",id=" + id + ",handlers=" + playerHandles + "+++++++");

            if (url.length() > 0) {
                mBridge.openUrl(url);
            }else {
                //send vast video click /clickthrough
                mBridge.postEvent(EventConstants.CLICK_THR);
            }

        }


    }

    @JavascriptInterface
    public void vpaidAdVideoFirstQuartile() {

        mBridge.postEvent(EventConstants.FIRST_QUARTILE);
    }

    @JavascriptInterface
    public void vpaidAdVideoMidpoint() {
        AdViewUtils.logInfo( "JS: vpaidAdVideoMidpoint");
        mBridge.postEvent(EventConstants.MIDPOINT);
    }

    @JavascriptInterface
    public void vpaidAdVideoThirdQuartile() {
        AdViewUtils.logInfo( "JS: vpaidAdVideoThirdQuartile");
        mBridge.postEvent(EventConstants.THIRD_QUARTILE);
    }

    @JavascriptInterface
    public void vpaidAdVideoComplete() {

        AdViewUtils.logInfo( "JS: vpaidAdVideoComplete");
        mBridge.postEvent(EventConstants.COMPLETE); //wilder 20190816 vpaid自己有消息发送，不需要timer去判
    }

    @JavascriptInterface
    public void getAdSkippableStateResult(boolean value) {
        AdViewUtils.logInfo( "JS: SkippableState: " + value);
        mBridge.onGetSkippableState(value);
    }

    @JavascriptInterface
    public void getAdRemainingTimeResult(int value) {
        //本回调从js端(ad_vbridge.js)返回，是通过timer，500ms在adstart之后触发，一直到adstop会定制，主要用于更新进度条等
        //播放的5个状态不要在次判断，有单独的事件发过来处理
        AdViewUtils.logInfo( "JS: getAdRemainingTimeResult: " + value);
        if (value == 0) {
            mBridge.postEvent(EventConstants.PROGRESS, String.valueOf(value));
            //mBridge.postEvent(EventConstants.COMPLETE); wilder 20190816 ,不要在次发送complete
        } else {
            mBridge.postEvent(EventConstants.PROGRESS, String.valueOf(value));
        }
    }

    @JavascriptInterface
    public void getAdDurationResult(int value) {
        //means already got total ad time
        mBridge.onGetDurationResult(value);

        AdViewUtils.logInfo( "+++ JS: getAdDurationResult: " + value + "+++");
    }

    @JavascriptInterface
    public void getAdLinearResult(boolean value) {
        AdViewUtils.logInfo( "getAdLinearResult: " + value);
    }

    @JavascriptInterface
    public void vpaidAdSkipped() {
        AdViewUtils.logInfo( "JS: vpaidAdSkipped");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBridge.onAdSkipped();
            }
        });
    }

    @JavascriptInterface
    public void vpaidAdStopped() {
        AdViewUtils.logInfo( "JS: vpaidAdStopped");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBridge.onAdStopped();
            }
        });
    }

    @JavascriptInterface
    public void vpaidAdImpression() {
        AdViewUtils.logInfo( "JS: vpaidAdImpression");
        mBridge.onAdImpression();
    }

    @JavascriptInterface
    public void vpaidAdInteraction() {

        AdViewUtils.logInfo( "+++++++++ JS: vpaidAdInteraction ++++++++++");
    }

    @JavascriptInterface
    public void vpaidAdVolumeChanged() {
        AdViewUtils.logInfo( "JS: vpaidAdVolumeChanged");
        mBridge.onAdVolumeChange();
    }

    @JavascriptInterface
    public void getAdVolumeResult(float vol) {

        AdViewUtils.logInfo( "+++ JS: getAdVolumeResult(): volume = " + vol+ "++++" );

        mBridge.onGetVolumeResult(vol);
    }


}
