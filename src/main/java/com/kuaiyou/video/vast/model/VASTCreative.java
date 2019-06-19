package com.kuaiyou.video.vast.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VASTCreative implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -3584705574391445686L;
    private String id;
    private Integer sequence = 999;
    private String adID;
    private String apiFramework;
    private Integer skipoffset = -1;
    private Integer duration = 0;
    private boolean isReady = false;
    private boolean isFailed = false;

    private String pickedVideoUrl;
    private int pickedVideoWidth;
    private int pickedVideoHeight;

    private String pickedVideoType;


    //wilder 2019
    private String VPAIDurl;
    private AdParameters AdPar;
    private int nonLinearWidth;
    private int nonLinearHeight;

    //end wilder
    private ArrayList<VASTIcon> vastIcons;
    private ArrayList<VASTCompanionAd> vastCompanionAds;
    private ArrayList<VASTNonLinear> vastNonLinears;

    private HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings;
    private VideoClicks videoClicks = new VideoClicks();
    private ArrayList<VASTMediaFile> mediaFiles;

    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public int getPickedVideoWidth() {
        return pickedVideoWidth;
    }

    public void setPickedVideoWidth(int pickedVideoWidth) {
        this.pickedVideoWidth = pickedVideoWidth;
    }

    public int getPickedVideoHeight() {
        return pickedVideoHeight;
    }

    public void setPickedVideoHeight(int pickedVideoHeight) {
        this.pickedVideoHeight = pickedVideoHeight;
    }

    public String getPickedVideoType() {
        return pickedVideoType;
    }

    public void setPickedVideoType(String type) {
        this.pickedVideoType = type;
    }

    public String getPickedVideoUrl() {
        return pickedVideoUrl;
    }

    public void setPickedVideoUrl(String pickedVideoUrl) {
        this.pickedVideoUrl = pickedVideoUrl;
    }

    public ArrayList<VASTIcon> getVastIcons() {
        if (null != vastIcons)
            return vastIcons;
        return new ArrayList<VASTIcon>();
    }

    public void setVastIcons(ArrayList<VASTIcon> vastIcons) {
        this.vastIcons = vastIcons;
    }

    public ArrayList<VASTCompanionAd> getVastCompanionAds() {
        return vastCompanionAds;
    }

    public void setVastCompanionAds(ArrayList<VASTCompanionAd> vastCompanionAds) {
        this.vastCompanionAds = vastCompanionAds;
    }

    public void setVastNonlinears(ArrayList<VASTNonLinear> vastNonLinears) {
        this.vastNonLinears = vastNonLinears;
    }

    public ArrayList<VASTNonLinear> getVastNonLinears() {
        return vastNonLinears;
    }

    public HashMap<TRACKING_EVENTS_TYPE, List<String>> getTrackings() {
        return trackings;
    }

    public void setTrackings(HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings) {
        this.trackings = trackings;
    }

    public VideoClicks getVideoClicks() {
        return videoClicks;
    }

    public void setVideoClicks(VideoClicks videoClicks) {
        this.videoClicks = videoClicks;
    }

    public ArrayList<VASTMediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(ArrayList<VASTMediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    //wilder 2019 for VPAID
    public void setVPAIDurl(String url) {
        this.VPAIDurl = url;
    }
    public String getVPAIDurl() {
        return VPAIDurl;
    }

    public void setAdParameters(AdParameters para) {
        this.AdPar = para;
    }
    public String getAdParameters() {
        if (AdPar == null) {
            return "";
        }
        return  AdPar.getText();
    }
    public void setNonLinearWidth(int width) {
        this.nonLinearWidth = width;
    }
    public void setNonLinearHeight(int height) {
        this.nonLinearHeight = height;
    }
    public int getNonLinearWidth() { return nonLinearWidth;}
    public int getNonLinearHeight() { return nonLinearHeight; }
    //end wilder 2019

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getAdID() {
        return adID;
    }

    public void setAdID(String adID) {
        this.adID = adID;
    }

    public String getApiFramework() {
        return apiFramework;
    }

    public void setApiFramework(String apiFramework) {
        this.apiFramework = apiFramework;
    }

    public Integer getSkipoffset() {
        return skipoffset;
    }

    public void setSkipoffset(Integer skipoffset) {
        this.skipoffset = skipoffset;
    }



}
