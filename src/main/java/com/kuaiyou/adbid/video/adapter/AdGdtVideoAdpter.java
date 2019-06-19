package com.kuaiyou.adbid.video.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.kuaiyou.KyAdBaseView;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.util.AdError;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

import java.util.Date;
import java.util.Locale;

public class AdGdtVideoAdpter extends AdAdapterManager implements RewardVideoADListener{

    private static final String TAG = "AdGdtVideoAdpter";
    private RewardVideoAD rewardVideoAD;
    private int width, height;

    private boolean adLoaded;//广告加载成功标志
    private boolean videoCached;//视频素材文件下载完成标志

    //1101152570
//        条：9079537218417626401
//        插屏：8575134060152130849
//        开屏：8863364436303842593
//        原生：5010320697302671
    //  激励视频：2090845242931421;//支持竖版出横版视频
    //  "5040942242835423";//不支持竖版出横版视频
    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdTTAdBannerAdapter");
        int[] sw = AdViewUtils.getWidthAndHeight(context, true);
        width = sw[0];
        height = sw[1];
    }



    @Override
    public void handleAd(Context context, Bundle bundle) {

        if (!KyAdBaseView.checkClass("com.qq.e.ads.rewardvideo.RewardVideoAD")) {
            super.onAdFailed("com.qq.e.ads.rewardvideo.RewardVideoAD not found");
            return;
        }

        String appID = bundle.getString("appId");
        String posID = bundle.getString("posId");
        //wilder 2019 test keys
        appID = "1101152570";
        posID = "2090845242931421";
        // 1. 初始化激励视频广告
        rewardVideoAD = new RewardVideoAD((Activity) context, appID, posID, this);
        adLoaded = false;
        videoCached = false;
        // 2. 加载激励视频广告
        rewardVideoAD.loadAD();
    }


    @Override
    public boolean playVideo(Context context) {
        try {
            //if (null != ttFullScreenVideoAd)
            //    ttFullScreenVideoAd.showFullScreenVideoAd((Activity) context);
            // 3. 展示激励视频广告
            if (adLoaded && rewardVideoAD != null)
            {//广告展示检查1：广告成功加载，此处也可以使用videoCached来实现视频预加载完成后再展示激励视频广告的逻辑
                if (!rewardVideoAD.hasShown()) {//广告展示检查2：当前广告数据还没有展示过
                    long delta = 1000;//建议给广告过期时间加个buffer，单位ms，这里demo采用1000ms的buffer
                    //广告展示检查3：展示广告前判断广告数据未过期
                    if (SystemClock.elapsedRealtime() < (rewardVideoAD.getExpireTimestamp() - delta)) {
                        rewardVideoAD.showAD();
                    } else {
                        //Toast.makeText(this, "激励视频广告已过期，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
                    }
                } else {
                    //Toast.makeText(this, "此条广告已经展示过，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
                }
            } else {
                //Toast.makeText(this, "成功加载广告后再进行广告展示！", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 广告加载成功，可在此回调后进行广告展示
     **/
    @Override
    public void onADLoad() {
        adLoaded = true;
        String msg = "load ad success ! expireTime = " + new Date(System.currentTimeMillis() +
                rewardVideoAD.getExpireTimestamp() - SystemClock.elapsedRealtime());
        Log.i(TAG, msg);
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 视频素材缓存成功，可在此回调后进行广告展示
     */
    @Override
    public void onVideoCached() {
        videoCached = true;
        Log.i(TAG, "onVideoCached");
    }

    /**
     * 激励视频广告页面展示
     */
    @Override
    public void onADShow() {

        Log.i(TAG, "onADShow");
    }

    /**
     * 激励视频广告曝光
     */
    @Override
    public void onADExpose() {

        Log.i(TAG, "onADExpose");
        this.onAdDisplay(true);
    }

    /**
     * 激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
     */
    @Override
    public void onReward() {

        Log.i(TAG, "onReward");
    }

    /**
     * 激励视频广告被点击
     */
    @Override
    public void onADClick() {

        Log.i(TAG, "onADClick");
        AdGdtVideoAdpter.this.onAdClick(null, null, 888, 888);
    }

    /**
     * 激励视频播放完毕
     */
    @Override
    public void onVideoComplete() {

        Log.i(TAG, "onVideoComplete");
    }

    /**
     * 激励视频广告被关闭
     */
    @Override
    public void onADClose() {

        Log.i(TAG, "onADClose");
    }

    /**
     * 广告流程出错
     */
    @Override
    public void onError(AdError adError) {
        String msg = String.format(Locale.getDefault(), "onError, error code: %d, error msg: %s",
                adError.getErrorCode(), adError.getErrorMsg());
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


}
