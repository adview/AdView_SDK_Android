package com.kuaiyou.interfaces;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;

import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.utils.SpreadView;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhangchen on 2017/12/21.
 */

public interface AdVGListener extends KyBaseListener {

    void onViewClicked(MotionEvent e, AgDataBean agDataBean, String url, float downX, float downY);


    void checkClick(String url);

    boolean isClickableConfirm();

    void setClickMotion(MRAIDView view, Rect touchRect);

    boolean needConfirmDialog();

    boolean getCloseble();

    WebResourceResponse shouldInterceptRequest(String url);

    String getAdLogo();
    String getAdIcon();

    //wilder 20200228
    Bitmap getAdLogoBmp();
    Bitmap getAdIconBmp();

    AdsBean getAdsBean();

}
