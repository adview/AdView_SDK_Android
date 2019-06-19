package com.kuaiyou.video.vast.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VASTIcon implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4636151628446222555L;
    private String staticValue;
    private String htmlValue;
    private String iFrameValue;
    private String valueType;
    private String program;
    private BigInteger width;
    private BigInteger height;
    private String xPosition;
    private String yPosition;
    private String duration;
    private String offset="0";
    private String apiFramework;

    private IconClicks iconClicks;

    public IconClicks getIconClicks() {
        return iconClicks;
    }

    public void setIconClicks(IconClicks iconClicks) {
        this.iconClicks = iconClicks;
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

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
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

    public String getXPosition() {
        return xPosition;
    }

    public void setXPosition(String xPosition) {
        this.xPosition = xPosition;
    }

    public String getYPosition() {
        return yPosition;
    }

    public void setYPosition(String yPosition) {
        this.yPosition = yPosition;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        if (!TextUtils.isEmpty(duration)) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                Date date = null;
                try {
                    date = format.parse(duration);
                    this.duration=date.getSeconds()+"";
                } catch (ParseException e) {
                    e.printStackTrace();
                }
        }
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        if (!TextUtils.isEmpty(offset)) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            Date date = null;
            try {
                date = format.parse(offset);
                this.offset=date.getSeconds()+"";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getApiFramework() {
        return apiFramework;
    }

    public void setApiFramework(String apiFramework) {
        this.apiFramework = apiFramework;
    }


}
