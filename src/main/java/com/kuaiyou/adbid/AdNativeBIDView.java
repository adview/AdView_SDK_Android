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
import android.widget.Advanceable;

import com.iab.omid.library.adview.adsession.AdSession;
import com.iab.omid.library.adview.adsession.ErrorType;
import com.kuaiyou.KyAdBaseView;
import com.kuaiyou.interfaces.KyNativeListener;
import com.kuaiyou.interfaces.NativeAdCallBack;
import com.kuaiyou.loader.loaderInterface.AdViewNativeListener;
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
    //private NativeAdCallBack nativeAdCallBack;
    private static int browserType = 0;
    private static String privacy_url = "";

    private String bitmapPath;
    private ApplyAdBean applyAdBean;
    //omsdk v1.2
    private AdSession adSession = null;
    private static String omsdk_url = "";
    private static String omsdk_vendor = "";
    private static String omsdk_para = "";
    //end omsdk
    private final static String confirmDialog_PositiveButton = "Confirm";
    private final static String confirmDialog_NegativeButton = "Cancel";
    private final static String confirmDialog_Title = "Title";
    private final static String confirmDialog_Message = "Show Details ?";

    private AdAdapterManager adAdapterManager;

    private String ua = "";
    public AdNativeBIDView(Context context, String appId, String posId, NativeAdCallBack nativeAdCallBack) {
        super(context);

        this.context = context;
        this.appId = appId;
        this.posId = posId;
        this.sdkType = ConstantValues.SDK_REQ_TYPE_NATIVE;
        this.nativeAdCallBack = nativeAdCallBack;

        this.ua = AdViewUtils.getUserAgent(context);
        KyAdBaseView.registerBatteryReceiver(context);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// interfaces for APP side //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void setBrowserType(int type) {
        if (type == ConstantValues.NATIVE_RESP_TYPE_INAPP || type == ConstantValues.NATIVE_RESP_TYPE_SYS)
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
        AdViewUtils.getDeviceIdFirstTime(context, this);
        //requestAd(1); wilder 2019 for gpid refresh fix
    }

    public void requestAd(int adCount) {
        if (adCount <= 0)
            adCount = 1;
        this.adCount = adCount;
        String configUrl = getConfigUrl(ConstantValues.ROUTE_ADBID_TYPE);
        applyAdBean = initRequestBean(appId, posId, ConstantValues.ROUTE_ADBID_TYPE, sdkType, adCount);

        executorService.execute(new InitAdRunable(
                makeRequestBeanString(applyAdBean).replace(" ", ""),
                configUrl,
                sdkType));

    }

    public void destroyNativeAd() {
        try {
            if (null != adAdapterManager) {
                adAdapterManager.destroyAd();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Privacy Information 展示接口， App应用接口
    public void showNativePrivacyInformation() {
        AdViewUtils.logInfo("======= showNativePrivacyInformation(): url = " + privacy_url + "======");
        if (!privacy_url.isEmpty()) {
            KyAdBaseView.showNativePrivacyInformation(context, privacy_url);
        }else {
            AdViewUtils.logInfo("=====Error=== showNativePrivacyInformation(): url is null ==");
        }
    }
    //end privacy
    @Override
    public void setAppNativeListener(NativeAdCallBack appInterface) {
        super.setAppNativeListener(appInterface);
    }
    /**
     * 汇报点击, App应用接口
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
     * 汇报展示 param adi  */
    public void reportImpression(String adi) {
        reportImpression(new View(context), adi);
    }
    /**
     * 汇报展示      *
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
                //打底所用，GDT之流
                if (null != adAdapterManager)
                    adAdapterManager.reportImpression(view, adi);
//                if (pos == 0)
//                    KyAdBaseView.reportOtherUrls(adsBean.getAgDataBean().getImpUrls());
//                else
//                    KyAdBaseView.reportOtherUrls(adsBean.getAgDataBeanList().get(pos).getImpUrls());
            }
            return;
        } catch (Exception e1) {
            AdViewUtils.logInfo("adi = " + adi);
        }
        reportImpression(adsBean);
    }

    public void reportVideoStatus(Activity context, String adi, int status) {
        try {
            //reportVideoStatus((Context) context, adi, status);
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
            reportStatus((Context)context, adsBean, status);
        } catch (Exception e) {
            e.printStackTrace();
            AdViewUtils.logInfo("reportVideoStatus error,status=" + status + ";adi=" + adi);
        }
    }

    private void reportStatus(Context activity, AdsBean adsBean, int status) {
        switch (status)
        {
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
            case NativeVideoStatus.FIRSTQUARTILE:
                break;
            case NativeVideoStatus.THIRDQUARTILE:
                break;
            case NativeVideoStatus.PAUSE:
                break;
            case NativeVideoStatus.SKIPPED:
                break;
            case NativeVideoStatus.VOLUMECHANGE:
                break;
        }
        //omsdk v1.2
        AdViewUtils.signalOMNativeVideoEvent(status);
    }

    ///////////////////////   omsdk v1.2 for native & video  ////////////////////////////////////////
    //(0)独立使用 native的session start
    public void createOMNativeSession(View v) {
        if (omsdk_url.length() > 0 && omsdk_vendor.length() > 0) {
            AdViewUtils.addOMVerificationScriptResource(omsdk_vendor, omsdk_url, omsdk_para);
            adSession = AdViewUtils.startOMAdSessionNATIVE(v);
        }
    }
    //(1) first must fill parameters
    public void addOMNativeScriptResrouce(String vendorKey, String scriptUrl, String params ) {
        AdViewUtils.addOMVerificationScriptResource(vendorKey, scriptUrl, params);

    }
    //(2)create ad session
    public void createOMNativeVideoSession(View v) {
        adSession = AdViewUtils.createOMAdSessionNATIVEVideo(v);

    }
    //(3)add ad session's obstructions
    public void addOMNativeVideoObstructions(View v) {
        if (null != adSession) {
            AdViewUtils.AddOMObstructions(v, adSession);
        }
    }
    //(4)start session
    public void startOMSession() {
        if (null != adSession) {
            AdViewUtils.startAdSession(adSession);
        }
    }
    //(5) register video loaded event
    public void regOMNativeVideoLoadedEvent(float skipOffset, boolean isAutoPlay) {
        if (null != adSession) {
            AdViewUtils.signalNativeVideoLoad(skipOffset, isAutoPlay);
        }
    }
    //(6) send playback event , see upper
    //(7) stop session
    public void stopOMSession() {
        if (null != adSession) {
            AdViewUtils.stopOMAdSession(adSession);
        }
    }

    //omsdk ends
    ////////////////////////////////////////////////end interfaces for APP /////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
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
                KyAdBaseView.reqScheduler.execute(new ClientReportRunnable("",
                                                    KyAdBaseView.replace4GDTKeys(url[i],
                                                            KyAdBaseView.getHK_Values(context, 0, 0, status == NativeVideoStatus.END,
                                                                    status == NativeVideoStatus.ERROR, bundle)),
                                            "GET"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handlerMsgs(Message msg) {
            //super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case ConstantValues.NOTIFY_REQ_GPID_FETCH_DONE:
                        requestAd(1);
                        break;
                    case ConstantValues.NOTIFY_RESP_RECEIVEAD_OK:
                        if (null != nativeAdCallBack) {
                            nativeAdCallBack.onNativeAdReceived(nativeBeanToMap((List<Object>) msg.obj));
                            //omsdk v1.2
                            if (adsBean.getAdType() == ConstantValues.RESP_ADTYPE_VIDEO) {
                                //notify app , it will call create createOMNativeVideoSession()
                            }else {
                                //omsdk v1.2
                                if (null != adSession) {
                                    AdViewUtils.stopOMAdSession(adSession);
                                    adSession = null; //重新初始化
                                }
                            }
                        }
                        break;
                    case ConstantValues.NOTIFY_RESP_RECEIVEAD_ERROR:
                        //adsBean = adsList.get(0);
                        if (null != adsBean && null != adsBean.getAgDataBean() && !TextUtils.isEmpty(adsBean.getAgDataBean().getAggsrc())) {
                            handleAd(adsBean, adsBean.getAgDataBean(), this, -1);
                            return;
                        }
                        if (null != nativeAdCallBack) {
                            nativeAdCallBack.onNativeAdReceiveFailed((String) msg.obj);
                        }
                        //omsdk v1.2
                        if (null != adSession) {
                            AdViewUtils.signalErrorEvent(adSession, ErrorType.GENERIC, (String)msg.obj);
                        }
                        break;
                    case ConstantValues.NOTIFY_NATIVE_RESP_STATUS:
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


    private static ArrayList nativeBeanToMap(List<Object> list) {
        ArrayList<HashMap<String, Object>> nativeList = new ArrayList<HashMap<String, Object>>();
        try {
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, Object> nativeMap = new HashMap<String, Object>();
                Object object = list.get(i);
                if (object instanceof NativeAdBean) {
                    NativeAdBean bean = (NativeAdBean) list.get(i);
                    nativeMap.put("adId", bean.getAdId());
                    nativeMap.put("adFlagIcon", bean.getAdIconFlag()); //wilder 2019 added
                    nativeMap.put("adFlagLogo", bean.getAdLogoFlag()); //wilder 2019 added
                    nativeMap.put("description", bean.getDesc());
                    nativeMap.put("sec_description", bean.getDesc2());
                    nativeMap.put("title", bean.getTitle());
                    nativeMap.put("adImage", bean.getImageUrl());
                    nativeMap.put("imageWidth", bean.getImageWidth());
                    nativeMap.put("imageHeight", bean.getImageHeight());
                    nativeMap.put("adIcon", bean.getIconUrl());
                    nativeMap.put("iconWidth", bean.getIconWidth());
                    nativeMap.put("iconHeight", bean.getIconHeight());
                    //omsdk v1.2 getpara
                    omsdk_url = bean.getOMUrl();
                    omsdk_vendor = bean.getOmVendor();
                    omsdk_para = bean.getOMPara();
                    //privacy information
                    nativeMap.put("privacy_image", bean.getPrivacyImageUrl());
                    nativeMap.put("privacy_click", bean.getPrivacyClickUrl());
                    privacy_url = bean.getPrivacyClickUrl();
                    //privacy end
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
                    nativeMap.put("adFlagIcon", videoBean.getAdIconFlag()); //wilder 2019 added
                    nativeMap.put("adFlagLogo", videoBean.getAdLogoFlag()); //wilder 2019 added
                    //privacy information
                    nativeMap.put("privacy_image", videoBean.getPrivacyImageUrl());
                    nativeMap.put("privacy_click", videoBean.getPrivacyClickUrl());
                    privacy_url = videoBean.getPrivacyClickUrl();
                    //privacy end
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

    private void handleClick(AdsBean adsBean) {
        if (browserType == ConstantValues.NATIVE_RESP_TYPE_INAPP) {
            if (adsBean.getAdAct() == ConstantValues.RESP_ACT_DOWNLOAD) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConstantValues.DL_DOWNLOADED_STATUS);
                intentFilter.addAction(ConstantValues.DL_DOWNLOADFAILED_STATUS);
                intentFilter.addAction(ConstantValues.DL_DOWNLOADING_STATUS);
                BroadcastReceiver brc = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(ConstantValues.DL_DOWNLOADED_STATUS)) {
                            notifyMsg(ConstantValues.NOTIFY_NATIVE_RESP_STATUS, ConstantValues.DL_DOWNLOADED_STATUS);
                        } else if (intent.getAction().equals(ConstantValues.DL_DOWNLOADING_STATUS)) {
                            notifyMsg(ConstantValues.NOTIFY_NATIVE_RESP_STATUS, ConstantValues.DL_DOWNLOADING_STATUS);
                        } else if (intent.getAction().equals(ConstantValues.DL_DOWNLOADFAILED_STATUS)) {
                            notifyMsg(ConstantValues.NOTIFY_NATIVE_RESP_STATUS, ConstantValues.DL_DOWNLOADFAILED_STATUS);
                        }
                    }
                };
                lbm.registerReceiver(brc, intentFilter);
            }
            /*KyAdBaseView.*/clickEvent(context, adsBean, adsBean.getAdLink());
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
                            public void onClick(DialogInterface dialog,int which) {
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

    ////////////  上报展示最终接口 ////////////////////////////
    private void reportImpression(AdsBean adsBean) {
        try {
            if (null == adsBean)
                return;
            if (null == executorService || executorService.isTerminated())
                executorService = Executors.newScheduledThreadPool(1);
            if (null != adsBean.getAdLogLink()) {
                executorService.execute(new ClientReportRunnable("", adsBean.getAdLogLink(), ConstantValues.GET));
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
                    if (AdViewUtils.useIMEI)
                        uuid = AdViewUtils.getImei(AdViewUtils.getActivity().getApplicationContext());
                    else {
                        uuid = AdViewUtils.getGpId(AdViewUtils.getActivity().getApplicationContext()); //wilder 2019 changed uuid -> gpid
                        if (uuid.contains("00000000-0000")) {
                            uuid = AdViewUtils.getOAId(AdViewUtils.getActivity().getApplicationContext());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < keysString.length; i++) {
                    String[] urls = rptMaps.get(keysString[i]);
                    for (int j = 0; j < urls.length; j++) {
                        if (null == urls[j] || urls[j].length() == 0)
                            continue;
                        executorService.schedule(new ClientReportRunnable("",
                                        KyAdBaseView.replaceKeys(urls[j], "0", "", "", location[0], location[1], uuid), ConstantValues.GET),
                                        Integer.valueOf(keysString[i]),
                                        TimeUnit.SECONDS);
                    }
                }
                //omsdk v1.2 send impression
//                executorService.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        AdViewUtils.signalImpressionEvent(adSession);
//                    }
//                });
                //omsdk v1.2 接口，这里启动和发展示一起
                AdViewUtils.signalImpressionEvent(adSession);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ////////////////////////   上报点击最终接口 ////////////////////////////////////
    private void reportClick(AdsBean adsBean) {
        try {
            if (null == adsBean)
                return;
            if (null == executorService || executorService.isTerminated())
                executorService = Executors.newScheduledThreadPool(1);

            if (null != adsBean && null != adsBean.getExtCRpt()) {
                HashMap<String, String[]> rptMaps = adsBean.getExtCRpt();
                Set<String> keySet = rptMaps.keySet();
                String[] keysString = new String[keySet.size()];
                keysString = keySet.toArray(keysString);

                String[] location = new String[]{"", ""};
                String uuid = "";
                try {
                    location = AdViewUtils.getLocation(AdViewUtils.getActivity().getApplicationContext());
                    if (AdViewUtils.useIMEI)
                        uuid = AdViewUtils.getImei(AdViewUtils.getActivity().getApplicationContext());
                    else
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
        adAdapterManager = AdAdapterManager.initAd(context, ConstantValues.SDK_REQ_TYPE_NATIVE, agDataBean.getAggsrc(), agDataBean.getRequestType());
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
        //omsdk v1.2
        AdViewUtils.stopOMAdSession(adSession);
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
            case ConstantValues.RESP_ADTYPE_VIDEO:
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
