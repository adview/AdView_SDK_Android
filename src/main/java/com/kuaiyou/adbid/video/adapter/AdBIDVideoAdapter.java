package com.kuaiyou.adbid.video.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.video.AdViewVideoActivity;


//#wilder

public class AdBIDVideoAdapter extends AdAdapterManager {


    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {

    }

    @Override
    public void handleAd(Context context, Bundle bundle) {
        Intent vastPlayerIntent = new Intent(context, AdViewVideoActivity.class);

//        Bundle bdle = new Bundle();
//        bdle.putBoolean("closeable", autoCloseAble);
//        bdle.putInt("vastOrientation", videoOrientation);
//        bdle.putBoolean("trafficWarnEnable", trafficWarnEnable);
//        bdle.putString("bgColor", bgColor.equals("#undefine") ? "#000000" : bgColor);

        vastPlayerIntent.putExtras(bundle);
        //vastPlayerIntent.putExtra("adsBean", (Serializable)adsBean);
        context.startActivity(vastPlayerIntent);
        //((Activity)context).startActivityForResult(vastPlayerIntent, SUBACTIVITY1);
        return ;
    }

    @Override
    public boolean playVideo(Context context) {
        try {
            if (null != newPlayer)
                newPlayer.sendPlayReady(context); //这里仅通知AdVastView准备播放，具体是否立刻播放请参见advastView
            else
                AdViewUtils.logInfo("video occurred error");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


}
