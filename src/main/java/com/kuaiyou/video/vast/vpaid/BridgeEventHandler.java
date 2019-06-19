package com.kuaiyou.video.vast.vpaid;

public interface BridgeEventHandler {

    void runOnUiThread(Runnable runnable);

    void callJsMethod(final String url);

    void onPrepared();

    void onAdSkipped();

    void onAdStopped();

    void onGetSkippableState(boolean skippable);

    void onGetDurationResult(int result);

    void onGetVolumeResult(float vol);

    void openUrl(String url);

    void trackError(String message);

    void postEvent(String eventType);

    void postEvent(String eventType, String value);

    void onDurationChanged();

    void onAdLinearChange();

    void onAdVolumeChange();

    void onAdImpression();

    void onSelfClosed(); //wilder 2019
}
