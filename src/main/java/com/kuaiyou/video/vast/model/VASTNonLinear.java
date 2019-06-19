package com.kuaiyou.video.vast.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class VASTNonLinear implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4636151628447799582L;

    private String staticValue;
    private String htmlValue;
    private String iFrameValue;
    private String valueType;
    private String id;
    private BigInteger width;
    private BigInteger height;

    private BigInteger expandedWidth;
    private BigInteger expandedHeight;
    private String apiFramework;
    private boolean scalable;
    private boolean maintainAspectRatio;
    private String minSuggestedDuration;

    private CompanionClicks nonLinearClicks;

    public boolean getScalable() { return scalable;}
    public void setScalable(boolean scalable) {
        this.scalable = scalable;
    }

    public boolean getMaintainAspectRatio() { return maintainAspectRatio; }
    public void setMaintainAspectRatio(boolean ratio) {
        this.maintainAspectRatio = ratio;
    }

    public String getMinSuggestedDuration() {
        return minSuggestedDuration;
    }
    public void setMinSuggestedDuration(String duration) {
        if (!TextUtils.isEmpty(duration)) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            Date date = null;
            try {
                date = format.parse(duration);
                this.minSuggestedDuration=date.getSeconds()+"";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public CompanionClicks getNonLinearClicks() {
        return nonLinearClicks;
    }
    public void setNonLinearClicks(CompanionClicks companionClicks) {
        this.nonLinearClicks = companionClicks;
    }
    public String getValueType() {
        return valueType;
    }
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    public String getStaticValue() {
        return staticValue;
    }
    public void setStaticValue(String staticValue) {
        this.staticValue = staticValue;
    }
    public String getHtmlValue() {
        return htmlValue;
    }
    public void setHtmlValue(String htmlValue) {
        this.htmlValue = htmlValue;
    }
    public String getiFrameValue() {
        return iFrameValue;
    }
    public void setiFrameValue(String iFrameValue) {
        this.iFrameValue = iFrameValue;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public BigInteger getWidth() {
        return width;
    }
    public void setWidth(BigInteger width) {
        this.width = width;
    }
    public BigInteger getHeight() {
        return height;
    }
    public void setHeight(BigInteger height) {
        this.height = height;
    }

    public BigInteger getExpandedWidth() {
        return expandedWidth;
    }
    public void setExpandedWidth(BigInteger expandedWidth) {
        this.expandedWidth = expandedWidth;
    }
    public BigInteger getExpandedHeight() {
        return expandedHeight;
    }
    public void setExpandedHeight(BigInteger expandedHeight) {
        this.expandedHeight = expandedHeight;
    }

    public String getApiFramework() {
        return apiFramework;
    }
    public void setApiFramework(String apiFramework) {
        this.apiFramework = apiFramework;
    }



}
