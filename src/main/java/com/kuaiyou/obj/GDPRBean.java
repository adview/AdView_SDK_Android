package com.kuaiyou.obj;

import java.io.Serializable;

public class GDPRBean implements Serializable {

    /*
    * SubjectToGDPR,ConsentString,ParsedPurposeConsents,ParsedVendorConsents
    * */
    //初始默认值
    private boolean iabCMPPresent = false;
    private String iabSubjectToGDPR = "";
    private String iabConsentString = "";
    private String iabParsedPurposeConsents = "";
    private String iabParsedVendorConsents = "";

    public boolean getIabCMPPresent() {
        return iabCMPPresent;
    }
    public void setIabCMPPresent(boolean iabCMPPresent) {

        this.iabCMPPresent = iabCMPPresent;
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
