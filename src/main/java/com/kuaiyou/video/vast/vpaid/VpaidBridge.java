package com.kuaiyou.video.vast.vpaid;

public interface VpaidBridge {

    void prepare();

    void startAd();

    void stopAd();

    void pauseAd();

    void resumeAd();

    void skipAd();

    void getAdSkippableState(); //wilder 2019

    void setAdVolume(float vol); //wilder 2019

    void getAdVolume();


}
