package com.kuaiyou.interfaces;


import java.io.Serializable;

public interface AdViewVideoInterface extends Serializable{

    /**
     * 成功收到广告时调用
     */
    void onReceivedVideo(String vast);

    /**
     * 失败收到广告时调用
     */
    void onFailedReceivedVideo(String error);

    /**
     * 视频下载就绪时
     */
    void onVideoReady();

    /**
     * 视屏广告开始播放时调用
     */
    void onVideoStartPlayed();

    /**
     * 视屏广告播放结束时调用
     */
    void onVideoFinished();

    /**
     * 视屏广告被关闭时调用
     */
    void onVideoClosed();

    /**
     * 视屏广告被关闭时调用
     */
    void onPlayedError(String error);

    int getOrientation();
}
