package com.kuaiyou.interfaces;

import android.os.Message;

import com.kuaiyou.obj.AgDataBean;

import java.io.Serializable;

public interface KyBaseListener extends Serializable{

    void onReady(AgDataBean agDataBean, boolean force);

    void onReceived(AgDataBean agDataBean, boolean force);

    void onAdFailed(AgDataBean agDataBean, String error, boolean force);

    void onDisplay(AgDataBean agDataBean, boolean force);

    void onCloseBtnClicked();

    void rotatedAd(Message msg);
}
