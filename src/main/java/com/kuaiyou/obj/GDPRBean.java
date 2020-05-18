package com.kuaiyou.obj;

import java.io.Serializable;

public class GDPRBean implements Serializable {

    /*
    * SubjectToGDPR,ConsentString,ParsedPurposeConsents,ParsedVendorConsents
    * */
    //初始默认值
    private String iabCMPPresent = ""; //means uncertain
    private String iabSubjectToGDPR = "";
    private String iabConsentString = "";
    private String iabParsedPurposeConsents = "";
    private String iabParsedVendorConsents = "";

    public String getIabCMPPresent() {
        return iabCMPPresent;
    }
    public void setIabCMPPresent(int iabCMPPresent) {
        if (iabCMPPresent == 0 || iabCMPPresent == 1) {
            this.iabCMPPresent = String.valueOf(iabCMPPresent);
        }else {
            this.iabCMPPresent = ""; //不合法的参数都设成未定义
        }
    }
    public String getIabSubjectToGDPR() {
        return iabSubjectToGDPR;
    }
    public void setIabSubjectToGDPR(String iabSubjectToGDPR) {
        this.iabSubjectToGDPR = iabSubjectToGDPR;
    }
    public String getIabConsentString() {
        return iabConsentString;
    }
    public void setIabConsentString(String iabConsentString) {
        this.iabConsentString = iabConsentString;
    }
    public String getIabParsedPurposeConsents() {
        return iabParsedPurposeConsents;
    }
    public void setIabParsedPurposeConsents(String iabParsedPurposeConsents) {
        this.iabParsedPurposeConsents = iabParsedPurposeConsents;
    }
    public String getIabParsedVendorConsents() {
        return iabParsedVendorConsents;
    }
    public void setIabParsedVendorConsents(String iabParsedVendorConsents) {
        this.iabParsedVendorConsents = iabParsedVendorConsents;
    }

}
