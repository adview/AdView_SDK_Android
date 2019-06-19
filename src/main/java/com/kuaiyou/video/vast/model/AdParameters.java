package com.kuaiyou.video.vast.model;


import java.io.Serializable;

public class AdParameters implements Serializable {

    private String text;

    public void setText(String t) { text = t; }
    public String getText() {
        return text;
    }

}
