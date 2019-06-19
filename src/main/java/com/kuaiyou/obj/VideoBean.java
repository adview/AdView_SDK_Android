package com.kuaiyou.obj;

public class VideoBean {

    private String iconButtonText;
    private String videoUrl;
    private String iconUrl;
    private String title;
    private String desc;
    private String[] playMonUrls;
    private Integer duration;
    private Integer width, height;
    private String adId = null;

    private Integer iconStartTime = -1, iconEndTime = -1;

    private String endIconUrl, endDesc, endTitle, endButtonText, endButtonUrl;
    private Integer endComments, endRating;

    //    private String[] spTrackers;
//    private String[] mpTrackers;
//    private String[] cpTrackers;
    private String preImgUrl, endHtml, endImgUrl;


    public boolean isValidBean() {
        return true;
    }

    public String getIconButtonText() {
        return iconButtonText;
    }

    public void setIconButtonText(String iconButtonText) {
        this.iconButtonText = iconButtonText;
    }

    public Integer getIconStartTime() {
        return iconStartTime;
    }

    public void setIconStartTime(Integer iconStartTime) {
        this.iconStartTime = iconStartTime;
    }

    public Integer getIconEndTime() {
        return iconEndTime;
    }

    public void setIconEndTime(Integer iconEndTime) {
        this.iconEndTime = iconEndTime;
    }

    public String getEndIconUrl() {
        return endIconUrl;
    }

    public void setEndIconUrl(String endIconUrl) {
        this.endIconUrl = endIconUrl;
    }

    public String getEndDesc() {
        return endDesc;
    }

    public void setEndDesc(String endDesc) {
        this.endDesc = endDesc;
    }

    public String getEndTitle() {
        return endTitle;
    }

    public void setEndTitle(String endTitle) {
        this.endTitle = endTitle;
    }

    public String getEndButtonText() {
        return endButtonText;
    }

    public void setEndButtonText(String endButtonText) {
        this.endButtonText = endButtonText;
    }

    public String getEndButtonUrl() {
        return endButtonUrl;
    }

    public void setEndButtonUrl(String endButtonUrl) {
        this.endButtonUrl = endButtonUrl;
    }

    public Integer getEndComments() {
        return endComments;
    }

    public void setEndComments(Integer endComments) {
        this.endComments = endComments;
    }

    public Integer getEndRating() {
        return endRating;
    }

    public void setEndRating(Integer endRating) {
        this.endRating = endRating;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String[] getPlayMonUrls() {
        return playMonUrls;
    }

    public void setPlayMonUrls(String[] playMonUrls) {
        this.playMonUrls = playMonUrls;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

//    public String[] getSpTrackers() {
//        return spTrackers;
//    }
//
//    public void setSpTrackers(String[] spTrackers) {
//        this.spTrackers = spTrackers;
//    }
//
//    public String[] getMpTrackers() {
//        return mpTrackers;
//    }
//
//    public void setMpTrackers(String[] mpTrackers) {
//        this.mpTrackers = mpTrackers;
//    }
//
//    public String[] getCpTrackers() {
//        return cpTrackers;
//    }
//
//    public void setCpTrackers(String[] cpTrackers) {
//        this.cpTrackers = cpTrackers;
//    }
}
