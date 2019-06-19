package com.kuaiyou.obj;

import java.io.Serializable;

public class AgDataBean implements Serializable {

    //    2018.6.12
    private String aggsrc = "9999";
    private String resAppId;
    private String resPosId;
    private String impUrls;
    private String cliUrls;
    private String failUrls;
    private String succUrls;
    private int requestType = 0;

    public String getFailUrls() {
        return failUrls;
    }

    public void setFailUrls(String failUrls) {
        this.failUrls = failUrls;
    }

    public String getSuccUrls() {
        return succUrls;
    }

    public void setSuccUrls(String succUrls) {
        this.succUrls = succUrls;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public String getAggsrc() {
        return aggsrc;
    }

    public void setAggsrc(String aggsrc) {
        this.aggsrc = aggsrc;
    }

    public String getResAppId() {
        return resAppId;
    }

    public void setResAppId(String resAppId) {
        this.resAppId = resAppId;
    }

    public String getResPosId() {
        return resPosId;
    }

    public void setResPosId(String resPosId) {
        this.resPosId = resPosId;
    }

    public String getImpUrls() {
        return impUrls;
    }

    public void setImpUrls(String impUrls) {
        this.impUrls = impUrls;
    }

    public String getCliUrls() {
        return cliUrls;
    }

    public void setCliUrls(String cliUrls) {
        this.cliUrls = cliUrls;
    }
}
