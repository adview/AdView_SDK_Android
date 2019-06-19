package com.kuaiyou.obj;

import java.io.Serializable;

public class ExtensionBean implements Serializable{

    private String endPageHtml;
    private String endPageImage;
    private String endPageIconUrl;
    private String endPageDesc;
    private String endPageTitle;
    private String endPageText;
    private String EndPageLink;

    public String getEndPageHtml() {
        return endPageHtml;
    }

    public void setEndPageHtml(String endPageHtml) {
        this.endPageHtml = endPageHtml;
    }

    public String getEndPageImage() {
        return endPageImage;
    }

    public void setEndPageImage(String endPageImage) {
        this.endPageImage = endPageImage;
    }

    public String getEndPageIconUrl() {
        return endPageIconUrl;
    }

    public void setEndPageIconUrl(String endPageIconUrl) {
        this.endPageIconUrl = endPageIconUrl;
    }

    public String getEndPageDesc() {
        return endPageDesc;
    }

    public void setEndPageDesc(String endPageDesc) {
        this.endPageDesc = endPageDesc;
    }

    public String getEndPageTitle() {
        return endPageTitle;
    }

    public void setEndPageTitle(String endPageTitle) {
        this.endPageTitle = endPageTitle;
    }

    public String getEndPageText() {
        return endPageText;
    }

    public void setEndPageText(String endPageText) {
        this.endPageText = endPageText;
    }

    public String getEndPageLink() {
        return EndPageLink;
    }

    public void setEndPageLink(String endPageLink) {
        EndPageLink = endPageLink;
    }
}
