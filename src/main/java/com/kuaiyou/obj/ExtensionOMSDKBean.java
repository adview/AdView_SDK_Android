package com.kuaiyou.obj;

import java.io.Serializable;

public class ExtensionOMSDKBean implements Serializable {
    private String omsdkVendor = "";// = "iabtechlab.com-omid";
    private String omsdkUrl = "";// = "https://s3-us-west-2.amazonaws.com/omsdk-files/compliance-js/omid-validation-verification-script-v1.js";
    private String omsdkParameters = "";// = "iabtechlab-Cjnet";
    private String omsdkTrackingURL;
    private String omsdkTrackingType;


    public String getOmsdkVendor() {
        return omsdkVendor;
    }
    public void setOmsdkVendor(String omsdkVendor) {
        this.omsdkVendor = omsdkVendor;
    }

    public String getOmsdkUrl() {
        return omsdkUrl;
    }
    public void setOmsdkUrl(String omsdkUrl) {
        this.omsdkUrl = omsdkUrl;
    }

    public String getOmsdkParameters() {
        return omsdkParameters;
    }
    public void setOmsdkParameters(String omsdkParameters) {
        this.omsdkParameters = omsdkParameters;
    }

    public String getOmsdkTrackingURL() {
        return omsdkTrackingURL;
    }
    public void setOmsdkTrackingURL(String omsdkTrackingURL) {
        this.omsdkTrackingURL = omsdkTrackingURL;
    }

    public String getOmsdkTrackingType() {
        return omsdkTrackingType;
    }
    public void setOmsdkTrackingType(String omsdkTrackingType) {
        this.omsdkTrackingType = omsdkTrackingType;
    }

}
