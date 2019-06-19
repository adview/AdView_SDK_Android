package com.kuaiyou.obj;

import java.io.File;
import java.io.Serializable;

/**
 * 下载信息Bean
 */
public class DownAppInfo implements Serializable {

    private static final long serialVersionUID = -1721190114014031113L;

    private String fileName = null;
    private String packageName = null;
    private String appName = null;
    private String downUrl = null;
    private File pathDir = null;
    private File pathName = null;
    private int status = 0;
    private long date = 0l;
    private int id = 0;

    private String clickid_gdt=null;
    private int alType=0;

    private String gdtExtraUrls;
    private String[] downloadstartUrls;
    private String[] downloadedUrls;
    private String[] installUrls;

    public String getGdtExtraUrls() {
        return gdtExtraUrls;
    }

    public void setGdtExtraUrls(String gdtExtraUrls) {
        this.gdtExtraUrls = gdtExtraUrls;
    }

    public int getAlType() {
        return alType;
    }

    public void setAlType(int alType) {
        this.alType = alType;
    }

    public String getClickid_gdt() {
        return clickid_gdt;
    }

    public void setClickid_gdt(String clickid_gdt) {
        this.clickid_gdt = clickid_gdt;
    }

    public String[] getDownloadstartUrls() {
        return downloadstartUrls;
    }

    public void setDownloadstartUrls(String[] downloadstartUrls) {
        this.downloadstartUrls = downloadstartUrls;
    }

    public String[] getDownloadedUrls() {
        return downloadedUrls;
    }

    public void setDownloadedUrls(String[] downloadedUrls) {
        this.downloadedUrls = downloadedUrls;
    }

    public String[] getInstallUrls() {
        return installUrls;
    }

    public void setInstallUrls(String[] installUrls) {
        this.installUrls = installUrls;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public File getPathDir() {
        return pathDir;
    }

    public void setPathDir(File pathDir) {
        this.pathDir = pathDir;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public File getPathName() {
        return pathName;
    }

    public void setPathName(File pathName) {
        this.pathName = pathName;
    }

}
