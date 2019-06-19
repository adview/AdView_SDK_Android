package com.kuaiyou.obj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 单条广告的属�?
 */
public class AdsBean implements Serializable {
    private static final Long serialVersionUID = 8472182013671215462L;
    private String idAd = null;
    private String adText = null;
    private String adPic = null;
    private String adLink = null;
    private String adSubTitle = null;
    private String adTitle = null;
    private String adInfo = null;
    private Integer adType = 0;
    private String adIcon = null;
    // 广告背景�?
    private String adBgColor = null;
    // 广告行为图标底色
    private String adBehaveBgColor = null;
    // 广告图标底色
    private String adIconBgColor = null;
    // 广告主标题颜�?
    private String adTitleColor = null;
    // 广告副标题颜�?
    private String adSubTitleColor = null;
    // 广告关键字颜�?
    private String adKeyWordColor = null;
    private String adBehavIcon = null;
    private String getImageUrl = null;
    private String serviceUrl = null;

    // 2013 - 9 - 26 百度添加字段
    private Integer adAct = 0;
    private String adPhoneNum = null;
    private String adLogLink = null;
    private Integer adSource = 0;
    private String dAppName = null;
    private String dAppIcon = null;
    private String dPackageName = null;
    private Integer dAppSize = 0;

    private String mon_s = null;
    private String mon_c = null;

    // 2014 - 7 - 30 Inmobi
    private String xhtml = null;
    private Integer adHeight = 50;
    private Integer adWidth = 320;

    // 2014 - 8 - 18
    private Integer appScore = -1;
    private Integer appUsers = -1;
    private String targetId = null;
    private Integer btn_render = 0;
    private String msg = null;
    private Integer resultCode = 1;
    private String dstlink = null;
    private String clickid = null;

    // 2014 - 8 - 21
    private HashMap<String, String[]> extSRpt = null;
    private HashMap<String, String[]> extCRpt = null;

    // 2014 - 9 - 18 增加开屏字�?
    private Integer ruleTime = 0;
    private Integer delayTime = 0;
    private String pointArea = "(0,0,1000,1000)";
    private Long cacheTime = 0l;
    private Integer spreadType = 2;
    private Integer vat = 0;

    private String appId = null;
    private Integer sdkType = 0;

    private Integer touchStatus = 0;
    private Integer sc = 0;

    // 2015 - 5 - 25 增加控制点击次数字段
    private Integer clickNumLimit = 2;

    private Integer action_up_x = -1;
    private Integer action_up_y = -1;
    private Integer action_down_x = -1;
    private Integer action_down_y = -1;

    private Integer realAdWidth = -1;
    private Integer realAdHeight = -1;

    private Integer specialAdWidth = -1;
    private Integer specialAdHeight = -1;

    private NativeAdBean nativeAdBean;
    private long dataTime = -1;
    private String fallback;

    private String deeplink = null;
    private String[] faUrl = null;
    private String[] saUrl = null;
    private String[] iaUrl = null;

    //  新增 广告标识字段
    private String adLogoUrl = null;
    private String adIconUrl = null;

    private int route = 0;
    private String[] videoSource = null;
    private int videoValidTime = 30;

    private String eqs = "";
    // 2017.10.7 新增字段支持gdt下载类广告
    private Integer alType = 0;

    private Integer deformationMode = 0;

    private String gdt_conversion_link;

    private String rawData;

    //2018.4.19 增加video字段
    private int xmlType;
    private VideoBean videoBean;
    private String vastXml;
    //    private String videoUrl;
//    private String iconUrl;
//    private String title;
//    private String desc;
//    private int duration;
//    private int width, height;
    private String[] spTrackers, mpTrackers, cpTrackers, playMonUrls;
    //video.ext字段
    private String preImgUrl, endHtml, endImgUrl;

    private Integer ait;

    private AgDataBean agDataBean;
    private ArrayList<AgDataBean> agDataBeanList;

    private String aptAppId;
    private String aptOrgId;
    private String aptPath;
    private Integer aptType;

    public static AdsBean copyAdsBean(AdsBean adsBean) {
        AdsBean newAdsBean = new AdsBean();
        newAdsBean.setAdAct(adsBean.getAdAct());
        newAdsBean.setAdBehaveBgColor(adsBean.getAdBehaveBgColor());
        newAdsBean.setAdBehavIcon(adsBean.getAdBehavIcon());
        newAdsBean.setAdBgColor(adsBean.getAdBgColor());
        newAdsBean.setAdHeight(adsBean.getAdHeight());
        newAdsBean.setAdIcon(adsBean.getAdIcon());
        newAdsBean.setAdIconBgColor(adsBean.getAdIconBgColor());
        newAdsBean.setAdInfo(adsBean.getAdInfo());
        newAdsBean.setAdKeyWordColor(adsBean.getAdKeyWordColor());
        newAdsBean.setAdLink(adsBean.getAdLink());
        newAdsBean.setAdLogLink(adsBean.getAdLogLink());
        newAdsBean.setAdPhoneNum(adsBean.getAdPhoneNum());
        newAdsBean.setAdPic(adsBean.getAdPic());
        newAdsBean.setAdSource(adsBean.getAdSource());
        newAdsBean.setAdSubTitle(adsBean.getAdSubTitle());
        newAdsBean.setAdSubTitleColor(adsBean.getAdSubTitleColor());
        newAdsBean.setAdText(adsBean.getAdText());
        newAdsBean.setAdTitleColor(adsBean.getAdTitleColor());
        newAdsBean.setAdType(adsBean.getAdType());
        newAdsBean.setAdWidth(adsBean.getAdWidth());
        newAdsBean.setAppScore(adsBean.getAppScore());
        newAdsBean.setAppUsers(adsBean.getAppUsers());
        newAdsBean.setBtn_render(adsBean.getBtn_render());
        newAdsBean.setCacheTime(adsBean.getCacheTime());
        newAdsBean.setClickid(adsBean.getClickid());
        newAdsBean.setdAppIcon(adsBean.getdAppIcon());
        newAdsBean.setdAppName(adsBean.getdAppName());
        newAdsBean.setdAppSize(adsBean.getdAppSize());
        newAdsBean.setDelayTime(adsBean.getDelayTime());
        newAdsBean.setdPackageName(adsBean.getdPackageName());
        newAdsBean.setDstlink(adsBean.getDstlink());
        newAdsBean.setExtCRpt(adsBean.getExtCRpt());
        newAdsBean.setExtSRpt(adsBean.getExtSRpt());
        newAdsBean.setGetImageUrl(adsBean.getGetImageUrl());
        newAdsBean.setIdAd(adsBean.getIdAd());
        newAdsBean.setMon_c(adsBean.getMon_c());
        newAdsBean.setMon_s(adsBean.getMon_s());
        newAdsBean.setMsg(adsBean.getMsg());
        newAdsBean.setPointArea(adsBean.getPointArea());
        newAdsBean.setResultCode(adsBean.getResultCode());
        newAdsBean.setRuleTime(adsBean.getRuleTime());
        newAdsBean.setServicesUrl(adsBean.getServicesUrl());
        newAdsBean.setServiceUrl(adsBean.getServiceUrl());
        newAdsBean.setTargetId(adsBean.getTargetId());
        newAdsBean.setVat(adsBean.getVat());
        newAdsBean.setXhtml(adsBean.getXhtml());
        newAdsBean.setTouchStatus(adsBean.getTouchStatus());
        newAdsBean.setAppId(adsBean.getAppId());
        newAdsBean.setSdkType(adsBean.getSdkType());
        newAdsBean.setSc(adsBean.getSc());
        newAdsBean.setClickNumLimit(adsBean.getClickNumLimit());
        newAdsBean.setRealAdHeight(adsBean.getRealAdHeight());
        newAdsBean.setRealAdWidth(adsBean.getRealAdWidth());
        newAdsBean.setAction_down_x(adsBean.getAction_down_x());
        newAdsBean.setAction_down_y(adsBean.getAction_down_y());
        newAdsBean.setAction_up_x(adsBean.getAction_up_x());
        newAdsBean.setAction_up_y(adsBean.getAction_up_y());

        newAdsBean.setSpecialAdHeight(adsBean.getSpecialAdHeight());
        newAdsBean.setSpecialAdWidth(adsBean.getSpecialAdWidth());
        newAdsBean.setNativeAdBean(adsBean.getNativeAdBean());
        newAdsBean.setDataTime(adsBean.getDataTime());
        newAdsBean.setFallback(adsBean.getFallback());

        newAdsBean.setDeeplink(adsBean.getDeeplink());
        newAdsBean.setFaUrl(adsBean.getFaUrl());
        newAdsBean.setSaUrl(adsBean.getSaUrl());
        newAdsBean.setIaUrl(adsBean.getIaUrl());
        newAdsBean.setAdLogoUrl(adsBean.getAdLogoUrl());
        newAdsBean.setAdIconUrl(adsBean.getAdIconUrl());
        newAdsBean.setRoute(adsBean.getRoute());
        newAdsBean.setVideoSource(adsBean.getVideoSource());
        newAdsBean.setVideoValidTime(adsBean.getVideoValidTime());
        newAdsBean.setEqs(adsBean.getEqs());
        newAdsBean.setAlType(adsBean.getAlType());
        newAdsBean.setGdtConversionLink(adsBean.getGdtConversionLink());
        newAdsBean.setDeformationMode(adsBean.getDeformationMode());
        newAdsBean.setRawData(adsBean.getRawData());

        newAdsBean.setXmlType(adsBean.getXmlType());

        newAdsBean.setXmlType(adsBean.getXmlType());
        newAdsBean.setVideoBean(adsBean.getVideoBean());
        newAdsBean.setVastXml(adsBean.getVastXml());
//        newAdsBean.setVideoUrl(adsBean.getVideoUrl());
//        newAdsBean.setIconUrl(adsBean.getIconUrl());
//        newAdsBean.setTitle(adsBean.getTitle());
//        newAdsBean.setDesc(adsBean.getDesc());
//
//        newAdsBean.setDuration(adsBean.getDuration());
        newAdsBean.setSpTrackers(adsBean.getSpTrackers());
        newAdsBean.setMpTrackers(adsBean.getMpTrackers());
        newAdsBean.setCpTrackers(adsBean.getCpTrackers());
        newAdsBean.setPreImgUrl(adsBean.getPreImgUrl());

        newAdsBean.setEndHtml(adsBean.getEndHtml());
        newAdsBean.setEndImgUrl(adsBean.getEndImgUrl());
//        newAdsBean.setWidth(adsBean.getWidth());
//        newAdsBean.setHeight(adsBean.getHeight());
        newAdsBean.setPlayMonUrls(adsBean.getPlayMonUrls());

        newAdsBean.setAptAppId(adsBean.getAptAppId());
        newAdsBean.setAptOrgId(adsBean.getAptOrgId());
        newAdsBean.setAptPath(adsBean.getAptPath());
        newAdsBean.setAptType(adsBean.getAptType());

        newAdsBean.setAgDataBean(adsBean.getAgDataBean());

        return newAdsBean;
    }


    public ArrayList<AgDataBean> getAgDataBeanList() {
        return agDataBeanList;
    }

    public void setAgDataBeanList(ArrayList<AgDataBean> agDataBeanList) {
        this.agDataBeanList = agDataBeanList;
    }

    public AgDataBean getAgDataBean() {
        return agDataBean;
    }

    public void setAgDataBean(AgDataBean agDataBean) {
        this.agDataBean = agDataBean;
    }

    public Integer getAit() {
        return ait;
    }

    public void setAit(Integer ait) {
        this.ait = ait;
    }

    public String[] getPlayMonUrls() {
        return playMonUrls;
    }

    public void setPlayMonUrls(String[] playMonUrls) {
        this.playMonUrls = playMonUrls;
    }

    public VideoBean getVideoBean() {
        return videoBean;
    }

    public void setVideoBean(VideoBean videoBean) {
        this.videoBean = videoBean;
    }

    public int getXmlType() {
        return xmlType;
    }

    public void setXmlType(int xmlType) {
        this.xmlType = xmlType;
    }

    public String getVastXml() {
        return vastXml;
    }

    public void setVastXml(String vastXml) {
        this.vastXml = vastXml;
    }

//    public String getVideoUrl() {
//        return videoUrl;
//    }
//
//    public void setVideoUrl(String videoUrl) {
//        this.videoUrl = videoUrl;
//    }
//
//    public String getIconUrl() {
//        return iconUrl;
//    }
//
//    public void setIconUrl(String iconUrl) {
//        this.iconUrl = iconUrl;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDesc() {
//        return desc;
//    }
//
//    public void setDesc(String desc) {
//        this.desc = desc;
//    }
//
//    public int getDuration() {
//        return duration;
//    }
//
//    public void setDuration(int duration) {
//        this.duration = duration;
//    }
//
//    public int getWidth() {
//        return width;
//    }
//
//    public void setWidth(int width) {
//        this.width = width;
//    }
//
//    public int getHeight() {
//        return height;
//    }
//
//    public void setHeight(int height) {
//        this.height = height;
//    }


    public String getAptAppId() {
        return aptAppId;
    }

    public void setAptAppId(String aptAppId) {
        this.aptAppId = aptAppId;
    }

    public String getAptOrgId() {
        return aptOrgId;
    }

    public void setAptOrgId(String aptOrgId) {
        this.aptOrgId = aptOrgId;
    }

    public String getAptPath() {
        return aptPath;
    }

    public void setAptPath(String aptPath) {
        this.aptPath = aptPath;
    }

    public Integer getAptType() {
        return aptType;
    }

    public void setAptType(Integer aptType) {
        this.aptType = aptType;
    }

    public String[] getSpTrackers() {
        return spTrackers;
    }

    public void setSpTrackers(String[] spTrackers) {
        this.spTrackers = spTrackers;
    }

    public String[] getMpTrackers() {
        return mpTrackers;
    }

    public void setMpTrackers(String[] mpTrackers) {
        this.mpTrackers = mpTrackers;
    }

    public String[] getCpTrackers() {
        return cpTrackers;
    }

    public void setCpTrackers(String[] cpTrackers) {
        this.cpTrackers = cpTrackers;
    }

    public String getPreImgUrl() {
        return preImgUrl;
    }

    public void setPreImgUrl(String preImgUrl) {
        this.preImgUrl = preImgUrl;
    }

    public String getEndHtml() {
        return endHtml;
    }

    public void setEndHtml(String endHtml) {
        this.endHtml = endHtml;
    }

    public String getEndImgUrl() {
        return endImgUrl;
    }

    public void setEndImgUrl(String endImgUrl) {
        this.endImgUrl = endImgUrl;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public Integer getDeformationMode() {
        return deformationMode;
    }

    public void setDeformationMode(Integer deformationMode) {
        this.deformationMode = deformationMode;
    }

    public String getGdtConversionLink() {
        return gdt_conversion_link;
    }

    public void setGdtConversionLink(String gdt_conversion_link) {
        this.gdt_conversion_link = gdt_conversion_link;
    }

    public Integer getAlType() {
        return alType;
    }

    public void setAlType(Integer alType) {
        this.alType = alType;
    }

    public String getEqs() {
        return eqs;
    }

    public void setEqs(String eqs) {
        this.eqs = eqs;
    }

    public int getVideoValidTime() {
        return videoValidTime;
    }

    public void setVideoValidTime(int videoValidTime) {
        this.videoValidTime = videoValidTime;
    }

    public int getRoute() {
        return route;
    }


    public void setRoute(int route) {
        this.route = route;
    }


    public String[] getVideoSource() {
        return videoSource;
    }

    public void setVideoSource(String[] videoSource) {
        this.videoSource = videoSource;
    }

    public String getAdIconUrl() {
        return adIconUrl;
    }

    public void setAdIconUrl(String adIconUrl) {
        this.adIconUrl = adIconUrl;
    }

    public String getAdLogoUrl() {
        return adLogoUrl;
    }

    public void setAdLogoUrl(String adLogoUrl) {
        this.adLogoUrl = adLogoUrl;
    }

    public String[] getIaUrl() {
        return iaUrl;
    }

    public void setIaUrl(String[] iaUrl) {
        this.iaUrl = iaUrl;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String[] getFaUrl() {
        return faUrl;
    }

    public void setFaUrl(String[] fa) {
        this.faUrl = fa;
    }

    public String[] getSaUrl() {
        return saUrl;
    }

    public void setSaUrl(String[] sa) {
        this.saUrl = sa;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public long getDataTime() {
        return dataTime;
    }

    public void setDataTime(long dataTime) {
        this.dataTime = dataTime;
    }

    public NativeAdBean getNativeAdBean() {
        return nativeAdBean;
    }

    public void setNativeAdBean(NativeAdBean nativeAdBean) {
        this.nativeAdBean = nativeAdBean;
    }

    public Integer getSpecialAdWidth() {
        return specialAdWidth;
    }

    public void setSpecialAdWidth(Integer specialAdWidth) {
        this.specialAdWidth = specialAdWidth;
    }

    public Integer getSpecialAdHeight() {
        return specialAdHeight;
    }

    public void setSpecialAdHeight(Integer specialAdHeight) {
        this.specialAdHeight = specialAdHeight;
    }

    public Integer getRealAdWidth() {
        return realAdWidth;
    }

    public void setRealAdWidth(Integer realAdWidth) {
        this.realAdWidth = realAdWidth;
    }

    public Integer getRealAdHeight() {
        return realAdHeight;
    }

    public void setRealAdHeight(Integer realAdHeight) {
        this.realAdHeight = realAdHeight;
    }

    public Integer getAction_up_x() {
        return action_up_x;
    }

    public void setAction_up_x(Integer action_up_x) {
        this.action_up_x = action_up_x;
    }

    public Integer getAction_up_y() {
        return action_up_y;
    }

    public void setAction_up_y(Integer action_up_y) {
        this.action_up_y = action_up_y;
    }

    public Integer getAction_down_x() {
        return action_down_x;
    }

    public void setAction_down_x(Integer action_down_x) {
        this.action_down_x = action_down_x;
    }

    public Integer getAction_down_y() {
        return action_down_y;
    }

    public void setAction_down_y(Integer action_down_y) {
        this.action_down_y = action_down_y;
    }

    public Integer getClickNumLimit() {
        return clickNumLimit;
    }

    public void setClickNumLimit(Integer clickNumLimit) {
        this.clickNumLimit = clickNumLimit;
    }

    public Integer getSc() {
        return sc;
    }

    public void setSc(Integer sc) {
        this.sc = sc;
    }

    public Integer getSdkType() {
        return sdkType;
    }

    public void setSdkType(Integer sdkType) {
        this.sdkType = sdkType;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Integer getTouchStatus() {
        return touchStatus;
    }

    public void setTouchStatus(Integer touchStatus) {
        this.touchStatus = touchStatus;
    }

    public Integer getVat() {
        return vat;
    }

    public void setVat(Integer vat) {
        this.vat = vat;
    }

    public Integer getRuleTime() {
        return ruleTime;
    }

    public void setRuleTime(Integer ruleTime) {
        this.ruleTime = ruleTime;
    }

    public Integer getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Integer delayTime) {
        this.delayTime = delayTime;
    }

    public String getPointArea() {
        return pointArea;
    }

    public void setPointArea(String pointArea) {
        this.pointArea = pointArea;
    }

    public Long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(Long cacheTime) {
        this.cacheTime = cacheTime;
    }

    public Integer getSpreadType() {
        return spreadType;
    }

    public void setSpreadType(Integer spreadType) {
        this.spreadType = spreadType;
    }

    public HashMap<String, String[]> getExtSRpt() {
        return extSRpt;
    }

    public void setExtSRpt(HashMap<String, String[]> extSRpt) {
        this.extSRpt = extSRpt;
    }

    public HashMap<String, String[]> getExtCRpt() {
        return extCRpt;
    }

    public void setExtCRpt(HashMap<String, String[]> extCRpt) {
        this.extCRpt = extCRpt;
    }

    public String getDstlink() {
        return dstlink;
    }

    public void setDstlink(String dstlink) {
        this.dstlink = dstlink;
    }

    public String getClickid() {
        return clickid;
    }

    public void setClickid(String clickid) {
        this.clickid = clickid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public Integer getBtn_render() {
        return btn_render;
    }

    public void setBtn_render(Integer btn_render) {
        this.btn_render = btn_render;
    }

    public Integer getAppScore() {
        return appScore;
    }

    public void setAppScore(Integer appScore) {
        this.appScore = appScore;
    }

    public Integer getAppUsers() {
        return appUsers;
    }

    public void setAppUsers(Integer appUsers) {
        this.appUsers = appUsers;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Integer getAdHeight() {
        return adHeight;
    }

    public void setAdHeight(Integer adHeight) {
        this.adHeight = adHeight;
    }

    public Integer getAdWidth() {
        return adWidth;
    }

    public void setAdWidth(Integer adWidth) {
        this.adWidth = adWidth;
    }

    public String getXhtml() {
        return xhtml;
    }

    public void setXhtml(String xhtml) {
        this.xhtml = xhtml;
    }

    public String getMon_s() {
        return mon_s;
    }

    public void setMon_s(String mon_s) {
        this.mon_s = mon_s;
    }

    public String getMon_c() {
        return mon_c;
    }

    public void setMon_c(String mon_c) {
        this.mon_c = mon_c;
    }

    public Integer getAdAct() {
        return adAct;
    }

    public void setAdAct(Integer adAct) {
        this.adAct = adAct;
    }

    public String getAdPhoneNum() {
        return adPhoneNum;
    }

    public void setAdPhoneNum(String adPhoneNum) {
        this.adPhoneNum = adPhoneNum;
    }

    public String getAdLogLink() {
        return adLogLink;
    }

    public void setAdLogLink(String adLogLink) {
        this.adLogLink = adLogLink;
    }

    public Integer getAdSource() {
        return adSource;
    }

    public void setAdSource(Integer adSource) {
        this.adSource = adSource;
    }

    public String getdAppName() {
        return dAppName;
    }

    public void setdAppName(String dAppName) {
        this.dAppName = dAppName;
    }

    public String getdAppIcon() {
        return dAppIcon;
    }

    public void setdAppIcon(String dAppIcon) {
        this.dAppIcon = dAppIcon;
    }

    public String getdPackageName() {
        return dPackageName;
    }

    public void setdPackageName(String dPackageName) {
        this.dPackageName = dPackageName;
    }

    public Integer getdAppSize() {
        return dAppSize;
    }

    public void setdAppSize(Integer dAppSize) {
        this.dAppSize = dAppSize;
    }

    public String getServicesUrl() {
        return serviceUrl;
    }

    public void setServicesUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getIdAd() {
        return idAd;
    }

    public void setIdAd(String idAd) {
        this.idAd = idAd;
    }

    public String getAdText() {
        return adText;
    }

    public void setAdText(String adText) {
        this.adText = adText;
    }

    public String getAdPic() {
        return adPic;
    }

    public void setAdPic(String adPic) {
        this.adPic = adPic;
    }

    public String getAdLink() {
        return adLink;
    }

    public void setAdLink(String adLink) {
        this.adLink = adLink;
    }

    public String getAdSubTitle() {
        return adSubTitle;
    }

    public void setAdSubTitle(String adSubTitle) {
        this.adSubTitle = adSubTitle;
    }

    public String getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(String adTitle) {
        this.adTitle = adTitle;
    }

    public String getAdInfo() {
        return adInfo;
    }

    public void setAdInfo(String adInfo) {
        this.adInfo = adInfo;
    }

    public Integer getAdType() {
        return adType;
    }

    public void setAdType(Integer adType) {
        this.adType = adType;
    }

    public String getAdIcon() {
        return adIcon;
    }

    public void setAdIcon(String adIcon) {
        this.adIcon = adIcon;
    }

    public String getAdBgColor() {
        return adBgColor;
    }

    public void setAdBgColor(String adBgColor) {
        this.adBgColor = adBgColor;
    }

    public String getAdBehaveBgColor() {
        return adBehaveBgColor;
    }

    public void setAdBehaveBgColor(String adBehaveBgColor) {
        this.adBehaveBgColor = adBehaveBgColor;
    }

    public String getAdIconBgColor() {
        return adIconBgColor;
    }

    public void setAdIconBgColor(String adIconBgColor) {
        this.adIconBgColor = adIconBgColor;
    }

    public String getAdTitleColor() {
        return adTitleColor;
    }

    public void setAdTitleColor(String adTitleColor) {
        this.adTitleColor = adTitleColor;
    }

    public String getAdSubTitleColor() {
        return adSubTitleColor;
    }

    public void setAdSubTitleColor(String adSubTitleColor) {
        this.adSubTitleColor = adSubTitleColor;
    }

    public String getAdKeyWordColor() {
        return adKeyWordColor;
    }

    public void setAdKeyWordColor(String adKeyWordColor) {
        this.adKeyWordColor = adKeyWordColor;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getAdBehavIcon() {
        return adBehavIcon;
    }

    public void setAdBehavIcon(String adBehavIcon) {
        this.adBehavIcon = adBehavIcon;
    }

    public String getGetImageUrl() {
        return getImageUrl;
    }

    public void setGetImageUrl(String getImageUrl) {
        this.getImageUrl = getImageUrl;
    }
}
