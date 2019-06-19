package com.kuaiyou.video;

import android.support.annotation.Nullable;

public interface AdControllerInterface {

    void runOnUiThread(Runnable runnable);

    void vpaid_onPrepared();

    void vpaid_openUrl(@Nullable String url);

    void vpaid_setVolume(int vol);

    void vpaid_dismiss(); //when closed in js itself

    void vpaid_fireEvent(String type, String value);

    void vpaid_setSkippableState(boolean skippable);

    void vpaid_setDurationTime(int result);
}
