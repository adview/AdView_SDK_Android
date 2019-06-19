package com.kuaiyou.adbid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.interfaces.KyNativeListener;
import com.kuaiyou.interfaces.NativeAdCallBack;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.obj.AgDataBean;
import com.kuaiyou.obj.ApplyAdBean;
import com.kuaiyou.obj.NativeAdBean;
import com.kuaiyou.obj.VideoBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ClientReportRunnable;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.NativeVideoStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AdNativeBIDView extends KyAdBaseView implements KyNativeListener {
    private int nativeWidth = -1, nativeHeight = -2;
    private int adCount = 1;

    private String appId = "";
    private String posId = "";
    private int adAct = 3;// 1+2
    private int sdkType = 0;
    private int adType = 0;
    private Context context;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    //private List<AdsBean> adsList;
    private NativeAdCallBack nativeAdCallBack;
    private static int browserType = 0;

    private String bitmapPath;
    private ApplyAdBean applyAdBean;

    private final static String confirmDialog_PositiveButton = "Confirm";
    private final static String confirmDialog_NegativeButton = "Cancel";
    private final static String confirmDialog_Title = "Title";
    private final static String confirmDialog_Message = "Show Details ?";

    private String ua = "";
    public AdNativeBIDView(Context context, String appId, String posId, NativeAdCallBack nativeAdCallBack, String gdpr) {
        super(context);

        this.context = context;
        this.appId = appId;
        this.posId = posId;
        this.sdkType = ConstantValues.NATIVEADTYPE;
        this.nativeAdCallBack = nativeAdCallBack;
        setGDPRConstent(gdpr);   //GDPR

        this.ua = AdViewUtils.getUserAgent(context);
        KyAdBaseView.registerBatteryReceiver(context);
    }

    public void setBrowserType(int type) {
        if (type == ConstantValues.INAPP || type == ConstantValues.SYS)
            browserType = type;
        else
            browserType = 0;
    }

    public void setNativeSize(int w, int h) {
        nativeWidth = w;
        nativeHeight = h;
    }

    public void setAdAct(int adAct) {
        this.adAct = adAct;
    }

    public void setAdType(int adType) {
        this.adType = adType;
    }

    public int getAdType() {
        return adType;
    }

    public void requestAd() {
        requestAd(1);
    }

    public void requestAd(int adCount) {
        if (adCount <= 0)
            adCount = 1;
        this.adCount = adCount;
        //executorService.execute(new InitAdRunnable(context, generalReqData(reqMap), AdViewUtils.adbidAddr));
        String configUrl = getConfigUrl(ConstantValues.ADBID_TYPE);
        applyAdBean = initApplyBean(appId, posId, ConstantValues.ADBID_TYPE, sdkType, adCount);

        executorService.execute(new InitAdRunable(
                getApplyInfoContent(applyAdBean).replace(" ", ""),
                configUrl,
                sdkType));

    }

    public void destroyNativeAd() {
        try {
            if (null != adAdapterManager)
                adAdapterManager.destroyAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 汇报点击
     *
     * @param adi
     */
    public void reportClick(String adi, int x, int y) {
        reportClick(new View(context), adi, x, y);
    }

    /**
     * 汇报点击
     *
     * @param adi
     */
    public void reportClick(String adi) {
        int rx = (int) (Math.random() * 50);
        int ry = (int) (Math.random() * 100);
        reportClick(new View(context), adi, rx, ry);
    }

    /**
     * 汇报点击
     *
     * @param adi
     */
    public void reportClick(View view, String adi, int x, int y) {
        try {
            AdsBean adsBean = null;
            if (null == adsBeanList) {
                AdViewUtils.logInfo("adsList is null");
                return;
            }
            for (AdsBean ads : adsBeanList) {
                if (null == ads.getIdAd())
                    continue;
                if (ads.getIdAd().equals(adi)) {
                    adsBean = ads;
                    break;
                }
            }
            try {
//                if (null == adsBean) {
                int pos = Integer.valueOf(adi);
                if (pos >= 0) {
                    if (null != adAdapterManager)
                        adAdapterManager.reportClick(view, adi);
//                    if (pos == 0)
//                        KyAdBaseView.reportOtherUrls(adsBean.getAgDataBean().getCliUrls());
//                    else
//                        KyAdBaseView.reportOtherUrls(adsBean.getAgDataBeanList().get(pos).getCliUrls());
                }
                return;
//                }
            } catch (Exception e1) {
            }
            if (null != adsBean) {
                adsBean.setAction_down_x(x);
                adsBean.setAction_down_y(y);
                adsBean.setAction_up_x(x);
                adsBean.setAction_up_y(y);
                reportClick(adsBean);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 汇报展示
     *
     * @param adi
     */
    public void reportImpression(String adi) {
        reportImpression(new View(context), adi);
    }

    /**
     * 汇报展示
     *
     * @param adi
     */
    public void reportImpression(View view, String adi) {
        AdsBean adsBean = null;
        if (null == adsBeanList) {
            AdViewUtils.logInfo("adsList is null");
            return;
        }
        for (AdsBean ads : adsBeanList) {
            if (null == ads.getIdAd())
                continue;
            if (ads.getIdAd().equals(adi)) {
                adsBean = ads;
                break;
            }
        }
        try {
            int pos = Integer.valueOf(adi);
            if (pos >= 0) {
                if (null != adAdapterManager)
                    adAdapterManager.reportImpression(view, adi);
//                if (pos == 0)
//                    KyAdBaseView.reportOtherUrls(adsBean.getAgDataBean().getImpUrls());
//                else
//                    KyAdBaseView.reportOtherUrls(adsBean.getAgDataBeanList().get(pos).getImpUrls());
            }
            return;
        } catch (Exception e1) {
        }
        reportImpression(adsBean);
    }

    public void reportVideoStatus(Activity context, String adi, int status) {
        try {
            reportVideoStatus((Context) context, adi, status);
        } catch (Exception e) {
            e.printStackTrace();
            AdViewUtils.logInfo("reportVideoStatus error,status=" + status + ";adi=" + adi);
        }
    }


    public void reportVideoStatus(Context context, String adi, int status) {
        AdsBean adsBean = null;
        if (null == adsBeanList) {
            AdViewUtils.logInfo("adsList is null");
            return;
        }
        for (AdsBean ads : adsBeanList) {
            if (ads.getIdAd().equals(adi)) {
                adsBean = ads;
                break;
            }
        }
        reportStatus(context, adsBean, status);
    }

    private void reportStatus(Context activity, AdsBean adsBean, int status) {
        switch (status) {
            case NativeVideoStatus.START:
                fireUrls(activity, adsBean, status, adsBean.getSpTrackers());
                break;
            case NativeVideoStatus.MEDIUM:
                fireUrls(activity, adsBean, status, adsBean.getMpTrackers());
                break;
            case NativeVideoStatus.END:
                fireUrls(activity, adsBean, status, adsBean.getCpTrackers());
                break;
            case NativeVideoStatus.STOP:
                fireUrls(activity, adsBean, status, adsBean.getPlayMonUrls());
                break;
            case NativeVideoStatus.RESUME:
                break;
            case NativeVideoStatus.ERROR:
                fireUrls(activity, adsBean, status, adsBean.getPlayMonUrls());
                break;
        }
    }


    private void fireUrls(Context context, AdsBean adsBean, int status, String... url) {
        if (null == url || url.length == 0)
            return;
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("desireWidth", 0);
            bundle.putInt("desireHeight", 0);
            bundle.putInt("duration", adsBean.getVideoBean().getDuration());
            bundle.putInt("lastPauseVideoTime", 0);
            bundle.putInt("currentVideoPlayTime", 0);
            for (int i = 0; i < url.length; i++) {
                KyAdBaseView.reqScheduler.execute(new ClientReportRunnable("", KyAdBaseView.replace4GDTKeys(url[i], KyAdBaseView.getHK_Values(context, 0, 0, status == NativeVideoStatus.END, status == NativeVideoStatus.ERROR, bundle)), "GET"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

/*
    private class InitAdRunnable implements Runnable {
        private Context context;
        private String url;
        private String content;
        private String result;
//        private String ua="";

        *//**
         * only for native ad
         *
         * @param context
         * @param url
         *//*
        public InitAdRunnable(Context context, String url, String content) {
            this.context = context;
            this.url = url;
            this.content = content;
        }

        @Override
        public void run() {
            Message msg = new Message();
            if (!AdViewUtils.isConnectInternet(context)) {
                msg.what = ThreadHandler.REQUEST_FAILED;
                msg.obj = "network is unavaliable";
                handler.sendMessage(msg);
                return;
            }
            // Log.i(AdViewUtils.ADVIEW, url + "?" + content);
            result = AdViewUtils.getResponse(content, url);
            if (null != result) {
                // Log.i(AdViewUtils.ADVIEW, result + "");
                if (!KyAdBaseView.isVaildAd(result)) {

                    AdsBean adsBean = new AdsBean();
                    adsList = new ArrayList<AdsBean>();
                    adsList.add(adsBean);
                    adsBean.setIdAd("0");
                    try {
                        JSONObject otherJson = new JSONObject(result);
                        if (otherJson.has("agdata")) {
                            adsBean.setAgDataBean(KyAdBaseView.parseAgdata(otherJson.optString("agdata")));
                        }
                        if (otherJson.has("agext")) {
                            if (null == adsBean)
                                adsBean = new AdsBean();
                            JSONArray jsonArray = otherJson.getJSONArray("agext");
                            ArrayList<AgDataBean> agDataBeanArrayList = new ArrayList<AgDataBean>();
                            for (int i = 0; i < jsonArray.length(); i++)
                                agDataBeanArrayList.add(KyAdBaseView.parseAgdata(jsonArray.getJSONObject(i).toString()));
                            adsBean.setAgDataBeanList(agDataBeanArrayList);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        msg.what = ThreadHandler.REQUEST_FAILED;
                        msg.obj = KyAdBaseView.getAdMsg(result);
                        handler.sendMessage(msg);
                    }
                    return;
                }
                adsList = KyAdBaseView.praseFromAds(KyAdBaseView.getAds(result), ConstantValues.NATIVEADTYPE);
                if (adsList != null && !adsList.isEmpty()) {
                    ArrayList<Object> nativeMaps = new ArrayList<Object>();
                    for (int i = 0; i < adsList.size(); i++) {
                        if (adsList.get(i).getXmlType() == 2)
                            nativeMaps.add(adsList.get(i).getVideoBean());
                        else
                            nativeMaps.add(adsList.get(i).getNativeAdBean());
                    }
                    msg.what = ThreadHandler.REQUEST_SUCCESSED;
                    msg.obj = nativeMaps;
                    handler.sendMessage(msg);
                    return;
                } else {
                    msg.what = ThreadHandler.REQUEST_FAILED;
                    msg.obj = "no_fill";
                    handler.sendMessage(msg);
                }
            } else {
                msg.what = ThreadHandler.REQUEST_FAILED;
                msg.obj = "connection_error";
                handler.sendMessage(msg);
            }

        }
    }*/

    @Override
    protected void handlerMsgs(Message msg) {
            //super.handleMessage(msg);
            try {
                //AdsBean adsBean;
                switch (msg.what) {
                    case ConstantValues.NOTIFYRECEIVEADOK:
                        if (null != nativeAdCallBack) {
                            nativeAdCallBack.onNativeAdReceived(nativeBeanToMap((List<Object>) msg.obj));
                        }
                        break;
                    case ConstantValues.NOTIFYRECEIVEADERROR:
                        //adsBean = adsList.get(0);
                        if (null != adsBean && null != adsBean.getAgDataBean() && !TextUtils.isEmpty(adsBean.getAgDataBean().getAggsrc())) {
                            handleAd(adsBean, adsBean.getAgDataBean(), this, -1);
                            return;
                        }
                        if (null != nativeAdCallBack)
                            nativeAdCallBack.onNativeAdReceiveFailed((String) msg.obj);
                        break;
                    case ConstantValues.NOTIFYSTATUS:
                        if (null != nativeAdCallBack) {
                            int status = Integer.parseInt((msg.obj).toString());
                            nativeAdCallBack.onDownloadStatusChange(status);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (null != nativeAdCallBack)
                    nativeAdCallBack.onNativeAdReceiveFailed("unknow error");
            }
    }

    private AdAdapterManager adAdapterManager;

    private static ArrayList nativeBeanToMap(List<Object> list) {
        ArrayList<HashMap<String, Object>> nativeList = new ArrayList<HashMap<String, Object>>();
        try {
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, Object> nativeMap = new HashMap<String, Object>();
                Object object = list.get(i);
                if (object instanceof NativeAdBean) {
                    NativeAdBean bean = (NativeAdBean) list.get(i);
                    nativeMap.put("adId", bean.getAdId());
                    nativeMap.put("adFlagIcon", bean.getAdIconFlag());
                    nativeMap.put("adFlagLogo", bean.getAdLogoFlag());
                    nativeMap.put("description", bean.getDesc());
                    nativeMap.put("sec_description", bean.getDesc2());
                    nativeMap.put("title", bean.getTitle());
                    nativeMap.put("adImage", bean.getImageUrl());
                    nativeMap.put("imageWidth", bean.getImageWidth());
                    nativeMap.put("imageHeight", bean.getImageHeight());
                    nativeMap.put("adIcon", bean.getIconUrl());
                    nativeMap.put("iconWidth", bean.getIconWidth());
                    nativeMap.put("iconHeight", bean.getIconHeight());
                    nativeList.add(nativeMap);
                } else if (object instanceof VideoBean) {
                    VideoBean videoBean = (VideoBean) list.get(i);
                    nativeMap.put("videoUrl", videoBean.getVideoUrl());
                    nativeMap.put("iconUrl", videoBean.getIconUrl());
                    nativeMap.put("title", videoBean.getTitle());
                    nativeMap.put("desc", videoBean.getDesc());//兼容旧版保留
                    nativeMap.put("description", videoBean.getDesc());
                    nativeMap.put("duration", videoBean.getDuration());
                    nativeMap.put("adId", videoBean.getAdId());
                    nativeMap.put("preImgUrl", videoBean.getPreImgUrl());
                    nativeMap.put("endHtml", videoBean.getEndHtml());
                    nativeMap.put("endImgUrl", videoBean.getEndImgUrl());
                    nativeList.add(nativeMap);
                } else if (object instanceof HashMap) {
                    HashMap<String, Object> map = (HashMap<String, Object>) list.get(i);
                    nativeMap.put("adId", i + "");
                    nativeMap.put("title", map.get("title"));
                    nativeMap.put("adIcon", map.get("iconUrl"));
                    nativeMap.put("description", map.get("description"));
                    nativeMap.put("adImage", map.get("imageUrl"));
                    nativeMap.put("imageList", map.get("imageList"));
                    nativeMap.put("nativeView", map.get("nativeView"));
                    nativeList.add(nativeMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nativeList;
    }


    private void reportImpression(AdsBean adsBean) {
        try {
            if (null == adsBean)
                return;
            if (null == executorService || executorService.isTerminated())
                executorService = Executors.newScheduledThreadPool(1);
            if (null != adsBean.getAdLogLink()) {
                executorService.execute(new ClientReportRunnable("", adsBean
                        .getAdLogLink(), ConstantValues.GET));
            }
            if (null != adsBean && null != adsBean.getExtSRpt()) {
                HashMap<String, String[]> rptMaps = adsBean.getExtSRpt();
                Set<String> keySet = rptMaps.keySet();
                String[] keysString = new String[keySet.size()];
                keysString = keySet.toArray(keysString);

                String[] location = new String[]{"", ""};
                String uuid = "";
                try {
                    location = AdViewUtils.getLocation(AdViewUtils.getActivity().getApplicationContext());
                    //uuid = AdViewUtils.getImei(AdViewUtils.getActivity().getApplicationContext());
                    uuid = AdViewUtils.getGpId(AdViewUtils.getActivity().getApplicationContext()); //wilder 2019 changed uuid -> gpid
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < keysString.length; i++) {
                    String[] urls = rptMaps.get(keysString[i]);
                    for (int j = 0; j < urls.length; j++) {
                        if (null == urls[j] || urls[j].length() == 0)
                            continue;
                        executorService.schedule(new ClientReportRunnable("", KyAdBaseView.replaceKeys(urls[j], "0", "", "", location[0], location[1], uuid),
                                        ConstantValues.GET),
                                Integer.valueOf(keysString[i]), TimeUnit.SECONDS);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClick(AdsBean adsBean) {
        if (browserType == ConstantValues.INAPP) {
            if (adsBean.getAdAct() == ConstantValues.ACT_DOWNLOAD) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConstantValues.DL_DOWNLOADED_STATUS);
                intentFilter.addAction(ConstantValues.DL_DOWNLOADFAILED_STATUS);
                intentFilter.addAction(ConstantValues.DL_DOWNLOADING_STATUS);
                BroadcastReceiver brc = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        ///Message msg = new Message();
                        //msg.what = ThreadHandler.STATUS_UPDATE;
                        if (intent.getAction().equals(ConstantValues.DL_DOWNLOADED_STATUS)) {
                            //msg.arg1 = ConstantValues.DL_DOWNLOADED_STATUS_INT;
                            notifyMsg(ConstantValues.NOTIFYSTATUS, ConstantValues.DL_DOWNLOADED_STATUS_INT);
                            //handler.sendMessage(msg);
                        } else if (intent.getAction().equals(ConstantValues.DL_DOWNLOADING_STATUS)) {
                            //msg.arg1 = ConstantValues.DL_DOWNLOADING_STATUS_INT;
                            //handler.sendMessage(msg);
                            notifyMsg(ConstantValues.NOTIFYSTATUS, ConstantValues.DL_DOWNLOADING_STATUS);
                        } else if (intent.getAction().equals(ConstantValues.DL_DOWNLOADFAILED_STATUS)) {
                            //msg.arg1 = ConstantValues.DL_DOWNLOADFAILED_STATUS_INT;
                            //handler.sendMessage(msg);
                            notifyMsg(ConstantValues.NOTIFYSTATUS, ConstantValues.DL_DOWNLOADFAILED_STATUS);
                        }
                    }
                };
                lbm.registerReceiver(brc, intentFilter);
            }
            KyAdBaseView.clickEvent(context, adsBean, adsBean.getAdLink());
        } else {
            try {
                Uri uri = Uri.parse(TextUtils.isEmpty(adsBean.getAdLink()) ? adsBean.getFallback() : adsBean.getAdLink());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void createConfirmDialog(Context context, final String adi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(confirmDialog_Title)
                .setMessage(confirmDialog_Message)
                .setNegativeButton(confirmDialog_NegativeButton,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        })
                .setPositiveButton(confirmDialog_PositiveButton,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                AdsBean adsBean = null;
                                if (null == adsBeanList) {
                                    AdViewUtils.logInfo("adsList is null");
                                    return;
                                }
                                for (AdsBean ads : adsBeanList) {
                                    if (ads.getIdAd().equals(adi)) {
                                        adsBean = ads;
                                        handleClick(adsBean);
                                        break;
                                    }
                                }
                                reportClick(adsBean);
                            }
                        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void reportClick(AdsBean adsBean) {
        try {
            if (null == adsBean)
                return;
            if (null == executorService || executorService.isTerminated())
                executorService = Executors.newScheduledThreadPool(1);

//            executorService.execute(new ClientReportRunnable(getContent(adsBean, reqMap),
//                    AdViewUtils.adfillAgent1, ConstantValues.GET));

            if (null != adsBean && null != adsBean.getExtCRpt()) {
                HashMap<String, String[]> rptMaps = adsBean.getExtCRpt();
                Set<String> keySet = rptMaps.keySet();
                String[] keysString = new String[keySet.size()];
                keysString = keySet.toArray(keysString);

                String[] location = new String[]{"", ""};
                String uuid = "";
                try {
                    location = AdViewUtils.getLocation(AdViewUtils.getActivity().getApplicationContext());
                    //uuid = AdViewUtils.getImei(AdViewUtils.getActivity().getApplicationContext());
                    uuid = AdViewUtils.getGpId(AdViewUtils.getActivity().getApplicationContext()); //wilder 2019 changed uuid -> gpid
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < keysString.length; i++) {
                    String[] urls = rptMaps.get(keysString[i]);
                    for (int j = 0; j < urls.length; j++) {
                        if (null == urls[j] || urls[j].length() == 0)
                            continue;
                        executorService.schedule(new ClientReportRunnable("", KyAdBaseView.replaceKeys(urls[j], "0", "", "", location[0], location[1], uuid),
                                        ConstantValues.GET),
                                Integer.valueOf(keysString[i]), TimeUnit.SECONDS);
                    }
                }
            }
            handleClick(adsBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onReady(AgDataBean agDataBean, boolean force) {

    }

    @Override
    public void onReceived(AgDataBean agDataBean, boolean force) {
    }

    @Override
    public void onAdFailed(AgDataBean agDataBean, String error, boolean force) {
        try {
            if (null != adsBeanList) {
                AdsBean adsBean = adsBeanList.get(0);
                if (null != agDataBean.getFailUrls())
                    KyAdBaseView.reportOtherUrls(agDataBean.getFailUrls());
                int times = KyAdBaseView.getAgDataBeanPosition(adsBean, agDataBean);
                if (times != -1) {
                    handleAd(adsBean, adsBean.getAgDataBeanList().get(times), this, times);
                    return;
                }
                if (null != nativeAdCallBack) {
                    nativeAdCallBack.onNativeAdReceiveFailed(error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisplay(AgDataBean agDataBean, boolean force) {

    }

    @Override
    public void onCloseBtnClicked() {

    }

    private void handleAd(AdsBean adsBean, AgDataBean agDataBean, AdNativeBIDView adViewNative, int times) {
        Bundle bundle = new Bundle();
        bundle.putString("aggsrc", agDataBean.getAggsrc());
        bundle.putString("appId", agDataBean.getResAppId());
        bundle.putString("posId", agDataBean.getResPosId());
        bundle.putSerializable("interface", adViewNative);
        bundle.putInt("type", adsBean.getAdAct());
        adAdapterManager = AdAdapterManager.initAd(context, ConstantValues.NATIVEADTYPE, agDataBean.getAggsrc(), agDataBean.getRequestType());
        adAdapterManager.setNativeCallback(adViewNative);
        adAdapterManager.handleAd(context, bundle);
        adAdapterManager.setTimeoutListener(times, agDataBean);
    }


    @Override
    public void onNativeAdReturned(AgDataBean agDataBean, List list) {
        try {
            if (null != nativeAdCallBack) {
                nativeAdCallBack.onNativeAdReceived(nativeBeanToMap(list));
            }
            if (null != adsBeanList) {
                if (null != agDataBean.getSuccUrls())
                    KyAdBaseView.reportOtherUrls(agDataBean.getSuccUrls());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onNativeStatusChange(int status) {
        if (null != nativeAdCallBack) {
            nativeAdCallBack.onDownloadStatusChange(status);
        }
        return status;
    }

    @Override
    public void onNativAdClosed(View view) {
        if (null != nativeAdCallBack)
            nativeAdCallBack.onNativeAdClosed(view);
    }

    @Override
    public void rotatedAd(Message msg) {
        try {
            Message msgCopy = Message.obtain(msg);
            AdsBean adsBean = adsBeanList.get(0);
            if (null == adsBean.getAgDataBeanList()) {
                if (null != nativeAdCallBack)
                    nativeAdCallBack.onNativeAdReceiveFailed("request failed");
                return;
            }
            if (msgCopy.arg1 < adsBean.getAgDataBeanList().size()) {
                AgDataBean agDataBean = adsBean.getAgDataBeanList().get(msgCopy.arg1);
                handleAd(adsBean, agDataBean, this, msgCopy.arg1);
            } else {
                if (null != nativeAdCallBack)
                    nativeAdCallBack.onNativeAdReceiveFailed("rotated error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (null != nativeAdCallBack)
                nativeAdCallBack.onNativeAdReceiveFailed("rotated tc error");
        }
    }

    @Override
    public int getNativeWidth() {
        return nativeWidth;
    }

    @Override
    public int getNativeHeight() {
        return nativeHeight;
    }

    @Override
    public int getAdCount() {
        return adCount;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// from KyAdBaseView.java ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    String adLogo, adIcon;
    @Override
    protected boolean initAdLogo(Object object) {
        if (!TextUtils.isEmpty(adsBean.getAdLogoUrl())) {
            adLogo = (String) AdViewUtils.getInputStreamOrPath(
                    getContext(), adsBean.getAdLogoUrl(), 1);
        }
        if (!TextUtils.isEmpty(adsBean.getAdIconUrl())) {
            adIcon = (String) AdViewUtils.getInputStreamOrPath(
                    getContext(), adsBean.getAdIconUrl(), 1);
        }
//        else
//            adIcon = "/assets/icon_ad.png";
        return true;
    }

    @Override
    protected boolean createBitmap(Object object) {
        switch (adsBean.getAdType()) {
            case ConstantValues.VIDEO:
                if (!TextUtils.isEmpty(adsBean.getAdIcon()))
                    return true;
            default:
                if (!TextUtils.isEmpty(adsBean.getAdPic()))
                    return true;
        }
        return false;
    }

    @Override
    protected void handleClick(MotionEvent e, int realX, int realY, String url) {
        //will be handled by app side
    }

    @Override
    public String getBitmapPath() {
        return bitmapPath;
    }

}
