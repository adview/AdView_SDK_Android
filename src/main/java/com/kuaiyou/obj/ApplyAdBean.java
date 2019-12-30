package com.kuaiyou.obj;

import com.kuaiyou.utils.AdViewUtils;

import java.io.Serializable;

/**
 * 设备和应用程序
 */
public class ApplyAdBean implements Serializable {

    private static final long serialVersionUID = 1442076703502458179L;

    private String appId = null;   //key
    private Integer system;
    private Integer isTestMode = 0;
    private Integer adCount = 0;
    private Integer sdkType = 0;
    /**
     * 定义产品的类�?0=>广告�?1=>插屏2=>应用�?
     */
    private String adSize = null;
    private String appName = null;
    private Integer sdkVer = 0;
    private Integer configVer = 0;
    private String bundleId = null;   //包名
    /**
     * 包名 id
     */
    private String uuid = null; // 设备唯一标识。iOS使用IDFA， Android使用IMEI
    private String service = null;   //运营商
    /**
     * 手机运营商代�?46000�?6002=>中国移动
     */
    private String time = null;
    private String token = null;
    private Integer route = -1;
    /**
     * 业务类型 2：服务器聚合（媒介API�?
     */
    private String resolution = null;
    private Integer devUse = 0;
    private String latitude = null;
    private String longitude = null;
    private String netType = null;
    private String devBrand = null;
    private String devType = null;
    private Integer adSource = 1;
    private String osVer = null;
    private String appVer = null;

    public String getOsVer() {
        return osVer;
    }

    public void setOsVer(String osVer) {
        this.osVer = osVer;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    // 增加 inmobi
    private String gpId = null;     //Android, Google play advertise id
    private String ua = null;       //Web useragent
    private String android_ID = null;  //ANDROID_ID作为唯一设备标识号（刷机、重置系统后可变）

    //有关华为 OAID
    private String oaID = null;

    // 适配gdt广告
    private Integer adHeight = 0;
    private Integer adWidth = 0;

    private Integer screenWidth = 0;
    private Integer screenHeight = 0;

    private String adPosId = "";

    private Integer html5 = 0;
    private Double deny = 1.5;

    //GDPR
    private Integer gdpr = 0;
    private String consent = "";

    // 适配交换广告。直投广告
    private Integer serviceId = 996;
    private Integer sex = 0;
    private String appVersion = "0";
    private String osVersion = "0";
    private String cid = "";
    private String blac = "";
    private String bsss_loc = "";

    private String bssid_wifi = "";
    private String ssid_wifi = "";

    private String mac = "";

    private int adType = 0;

    private String packageNames;

    private int batteryLevel = 100;

    private int orientation = 0;

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int bty) {
        this.batteryLevel = bty;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(String packageNames) {
        this.packageNames = packageNames;
    }

    public int getAdType() {
        return adType;
    }

    public void setAdType(int adType) {
        this.adType = adType;
    }

    public Integer getIsTestMode() {
        return isTestMode;
    }

    public void setIsTestMode(Integer isTestMode) {
        this.isTestMode = isTestMode;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getBlac() {
        return blac;
    }

    public void setBlac(String blac) {
        this.blac = blac;
    }

    public String getBsss_loc() {
        return bsss_loc;
    }

    public void setBsss_loc(String bsss_loc) {
        this.bsss_loc = bsss_loc;
    }

    public String getBssid_wifi() {
        return bssid_wifi;
    }

    public void setBssid_wifi(String bssid_wifi) {
        this.bssid_wifi = bssid_wifi;
    }

    public String getSsid_wifi() {
        return ssid_wifi;
    }

    public void setSsid_wifi(String ssid_loc) {
        this.ssid_wifi = ssid_loc;
    }

    public String getMacAddress() {
        return mac;
    }

    public void setMacAddress(String mac) {
        this.mac = mac;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Double getDeny() {
        return deny;
    }

    public void setDeny(Double deny) {
        this.deny = deny;
    }

    public Integer getHtml5() {
        return html5;
    }

    public void setHtml5(Integer html5) {
        this.html5 = html5;
    }

    public String getAdPosId() {
        if (adPosId != null) {
            return adPosId;
        }else {
            return "0";
        }
    }

    public void setAdPosId(String adPosId) {
        this.adPosId = adPosId;
    }

    public Integer getAdHeight() {
        return adHeight;
    }

    public void setAdHeight(Integer adHeight) {
        this.adHeight = adHeight;
    }

    public Integer getAdWidth() {
        return adWidth;
    }

    public void setAdWidth(Integer adWidth) {
        this.adWidth = adWidth;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public String getAndroid_ID() {
        return android_ID;
    }

    public void setAndroid_ID(String android_ID) {
        this.android_ID = android_ID;
    }

    public String getGpId() {
        return gpId;
    }

    public void setGpId(String gpId) {
        this.gpId = gpId;
    }
    //OAid
    public String getOAId() {
        return oaID;
    }

    public void setOAId(String oaid) {
        this.oaID = oaid;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public Integer getDevUse() {
        return devUse;
    }

    public void setDevUse(Integer devUse) {
        this.devUse = devUse;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getNetType() {
        return netType;
    }

    public void setNetType(String netType) {
        this.netType = netType;
    }

    public String getDevBrand() {
        return devBrand;
    }

    public void setDevBrand(String devBrand) {
        this.devBrand = devBrand;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public Integer getAdSource() {
        return adSource;
    }

    public void setAdSource(Integer adSource) {
        this.adSource = adSource;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Integer getRoute() {
        return route;
    }

    public void setRoute(Integer route) {
        this.route = route;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getBundleId() {
        //wilder 2019 for test custom package
        if (AdViewUtils.test_UserPackage.length() > 0) {
            return AdViewUtils.test_UserPackage;
        }else {
            return bundleId;
        }
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
        //this.uuid = "00000000";  //wilder 2019 for oversea changes
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getSdkVer() {
        return sdkVer;
    }

    public void setSdkVer(Integer sdkVer) {
        this.sdkVer = sdkVer;
    }

    public Integer getConfigVer() {
        return configVer;
    }

    public void setConfigVer(Integer configVer) {
        this.configVer = configVer;
    }

    public Integer getAdCount() {
        return adCount;
    }

    public void setAdCount(Integer adCount) {
        this.adCount = adCount;
    }

    public Integer getSdkType() {
        return sdkType;
    }

    public void setSdkType(Integer sdkType) {
        this.sdkType = sdkType;
    }

    public String getAdSize() {
        return adSize;
    }

    public void setAdSize(String adSize) {
        this.adSize = adSize;
    }

    public ApplyAdBean() {
        setSystem(1);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String str) {
        appId = new String(str);
    }

    public Integer getTestMode() {
        return isTestMode;
    }

    public void setTestMode(Integer val) {
        isTestMode = val;
    }

    public Integer getSystem() {
        return system;
    }

    public void setSystem(Integer val) {
        system = val;
    }

}
