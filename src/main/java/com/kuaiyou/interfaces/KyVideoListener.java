package com.kuaiyou.interfaces;

import android.os.Bundle;

import com.kuaiyou.obj.AgDataBean;

public interface KyVideoListener extends KyBaseListener {

    void onVideoClicked(AgDataBean agDataBean);

    void onVideoPlayFinished(AgDataBean agDataBean);

    void onVideoPlayStarted(AgDataBean agDataBean);

    void onVideoReceived(String vast);

    void onDownloadCancel();

    void onVideoPlayReady(Bundle bundle);

}
