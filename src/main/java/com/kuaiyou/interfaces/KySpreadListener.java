package com.kuaiyou.interfaces;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import com.kuaiyou.utils.SpreadView;

public interface KySpreadListener extends KyViewListener {

    void onAdNotifyCustomCallback(ViewGroup view, int ruleTime,
                                  int delayTime);

    SpreadView getSpreadView();

    SpreadView getSkipView();

    Drawable getSpreadLogo();

    int getNotifyType();

    String getBehaveIcon();
}
