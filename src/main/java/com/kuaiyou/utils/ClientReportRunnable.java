package com.kuaiyou.utils;

import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 汇报点击/展示 Runnable
 *
 * @author zhangchen
 */
public class ClientReportRunnable implements Runnable {

    private String url;
    private String content;
    private String method = "GET";
    private boolean isExchange = false;
    private int curTryNum = 0;

    // only for AdVG
    private ClientReportRunnable(String content, String url,
                                 boolean isExchange, int curTryNum) {
        this.url = url;
        this.content = content;
        this.isExchange = isExchange;
        this.curTryNum = curTryNum;

    }

    public ClientReportRunnable(String content, String url, boolean isExchange) {
        this(content, url, isExchange, 0);
    }

    public ClientReportRunnable(String content, String url, String method) {
        this(content, url, method, 0);
    }

    private ClientReportRunnable(String content, String url, String method, int curTryNum) {
        this.url = url;
        this.content = content;
        this.method = method;
        this.curTryNum = curTryNum;
    }

    @Override
    public void run() {
        String result = null;
        try {
            if (TextUtils.isEmpty(url)) {
                AdViewUtils.logInfo("### ClientReportRunnable(): warn:  url is Null ###");
                return;
            }
            if (null == content) {
                AdViewUtils.logInfo("### ClientReportRunnable(): ClientReport  content is Null ###");
                return;
            }
            if (!isExchange) {
                if (method.equals(ConstantValues.GET))
                    result = AdViewUtils.getResponse(url, content, false, ConstantValues.REQUEST_CONNECT_TIMEOUT);
                else {
//                         AdViewUtils.logInfo(url + "?" + content);
                    result = AdViewUtils.postResponse(url, content, false);
//                         AdViewUtils.logInfo(result + "");
                }
            } else {
                result = AdViewUtils.kyPostResponse(url, content);
            }
            if (result == null && curTryNum < 2) {
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        new ClientReportRunnable(content, url, method,curTryNum + 1);
                    }
                };
                timer.schedule(timerTask, 30 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

