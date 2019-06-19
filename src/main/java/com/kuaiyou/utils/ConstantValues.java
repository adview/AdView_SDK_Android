package com.kuaiyou.utils;

import android.os.Environment;

public class ConstantValues {

    //请求广告类型
    public static final int BANNERTYPE = 0;
    public static final int INSTLTYPE = 1;
    public static final int SPREADTYPE = 4;
    public static final int VIDEOTYPE = 5;
    public static final int NATIVEADTYPE = 6;
    public static final int MRECTYPE = 11;      //wilder 2019 for mrec

    //浏览器类型
    public final static int INAPP = 0;
    public final static int SYS = 1;

    // 响应广告类型
    public final static int FULLIMAGE = 0; //banner纯图片
    public final static int INTERLINK = 1; //banner文字链
    public final static int MIXED = 2;  //banner图文混合
    public final static int INSTL = 3;  //插屏
    public final static int HTML = 4;    //html
    public final static int SPREAD = 5; //开屏纯图片
    public final static int VIDEO = 6; //激励视频
    public final static int VIDEO_PASTER = 7 ;//贴片视频
    public final static int NATIVE = 8; //原生广告
    public final static int VIDEO_EMBED = 11; //MREC 嵌入视频

    // SSP渠道号
    public final static int ADS_ADVIEW = 1;
    public final static int ADS_BAIDU = 2;
    public final static int ADS_INMOBI = 4;
    public final static int ADS_MIAOZHEN = 5;
    public final static int ADS_GDT = 6;
    public final static int ADS_ALI = 8;
    public final static int ADS_PINYOU = 12;

    // 广告行为
    public final static int ACT_OPENWEB = 1;
    public final static int ACT_DOWNLOAD = 2;
    public final static int ACT_OPENMAP = 4;
    public final static int ACT_SENDMSG = 8;
    public final static int ACT_SENDEMAIL = 16;
    public final static int ACT_CALL = 32;
    public final static int ACT_PALYVIDEO = 64;
    public final static int ACT_WECHATAPP = 128;

    // 联网方式
    public final static String POST = "POST";
    public final static String GET = "GET";

    // spread logo
    public final static int HASLOGO = 1;
    public final static int NOLOGO = 2;

    // banner大小
    public final static int BANNER_AUTO_FILL = 0;
    public final static int BANNER_MREC = 1;        //320x250
    public final static int BANNER_480X75 = 2;
    public final static int BANNER_728X90 = 3;
    public final static int BANNER_SMART = 5;

    //INSTL 插屏广告大小
    public final static int INSTL_SIZE = 4; //300 x 300
    public final static int INSTL_320X480 = 8;
    public final static int INSTL_600X500 = 7;
    public final static int INSTL_300X250 = 6;

    // handler信息通知
    public final static int NOTIFYTIMEOUTCHECK = -1;
    public final static int NOTIFYRECEIVEADOK = 0;
    public final static int NOTIFYRECEIVEADERROR = 1;
    public final static int NOTIFYCONNCETFAIL = 2;
    public final static int NOTIFYROTATEAD = 3;
    public final static int NOTIFYSTATUS= 4; //native used

    /*------------------spread handler status--------------------*/
    public static final int FAILED = -1;
    public static final int INITSUCCESS = 0;
    public static final int BITMAPRECIEVED = 1;
    public static final int WEBVIEWRECIEVED = 4;
    public static final int DELAY = 2;
    public static final int STRICT = 3;
    public static final int COUNTDOWN = 5;
    public static final int USERCANCEL = 6;
    public static final int IMPRESSION = 7;
    public static final int UIDELAYUPDATE = 8;
    public static final int CLOSEDSTATUSCHECK = 9;

    /*------------------spread handler status--------------------*/

    public final static int ADICONID = 90001;
    public final static int ADLOGOID = 90002;
    public final static int MRAIDVIEWID = 90003;
    public final static int CLOSEBTNID = 90004;

    // 广告布局控件ID
    public final static int ICONID = 10001;
    public final static int TITLEID = 10002;
    public final static int SUBTITLEID = 10003;
    public final static int BEHAVICONID = 10004;
    public final static int DESCRIPTTEXTID = 10005;
    public final static int WEBVIEWID = 10006;

    public final static int SPREADTEXTID = 10007;
    public final static int SPREADMIXLAYOUTID = 10008;

    public static final int SPREADADFRAMEID = 70003;
    public final static int SPREADNOTIFYLAYOUT = 10009;
    public final static int SPREADADCOUNTER = 10010;

    // spread
//    public static final int SPREADADIMAGEID = 70001;
    public static final int SPREADLOGOIMAGEID = 70002;
//    public static final int SPREADADFRAMEID = 70003;
//    public static final int SPREADLOGOFRAMEID = 70004;
//    public static final int SPREADADTEXTID = 70005;
//    public static final int SPREADTEXTFRAMEID = 70006;
//    public static final int SPREADADWEBVIEWID = 70007;
//    public static final int SPREADADNOTIFYID = 70008;
//    public static final int SPREADADNOTIFYLAYOUTID = 70009;
//    public static final int SPREADADICONID = 70010;

    // 开屏布局分部常量
    public final static int EXTRA1 = -1;
    public final static int EXTRA2 = -2;
    public final static int EXTRA3 = -3;
    public final static int NULL = 0;
    public final static int TOP = 1;
    public final static int CENTER = 2;
    public final static int BOTTOM = 3;
    public final static int ALL_CENTER = 4;

    public final static int SCALE_NOHTML = 1;
    public final static int SCALE_INCLUDEHTML = 2;
    public final static int NOSCALED = 0;

    // 广告条颜色类名称
    public static final String ICONBACKGROUNDCOLOR = "icon";
    public static final String BEHAVEBACKGROUNDCOLOR = "behave";
    public static final String PARENTBACKGROUNDCOLOR = "parent";
    public static final String TITLEBACKGROUNDCOLOR = "title";
    public static final String SUBTITLEBACKGROUNDCOLOR = "subtitle";
    public static final String KEYWORDBACKGROUNDCOLOR = "keyword";

    // 插屏点击广播
    public final static String CLICKBROADCAST = "clickadview";

    // 插屏布局名称常量
    public final static String SCREENWIDTH = "screenWidth";
    public final static String SCREENHEIGHT = "screenHeight";
    public final static String FRAMEWIDTH = "frameWidth";
    public final static String FRAMEHEIGHT = "frameHeight";
    public final static String INSTLWIDTH = "instlWidth";
    public final static String INSTLHEIGHT = "instlHeight";

    // sharedPreference files name
    public final static String SP_BITMAPMAPPING = "sp_bitmap_local";
    public final static String SP_LASTMODIFY = "sp_imagemodifysince";
    public final static String SP_LASTVISIT = "sp_lastvisittime";
    public final static String SP_ADVINFO = "sp_dev_info";
    public final static String SP_SPREADINFO = "sp_spread_info";

    public final static String SP_INSTLINFO = "sp_instl_info";
    public final static String SP_BANNERINFO = "sp_banner_info";
    public final static String SP_VIDEO_NAME = "local_dw_video";

    public final static String SP_DOWNLOAD_INFO = "sp_download_info";

    // banner文字动画位移常量
    public final static int UP_OUT = 0;
    public final static int UP_IN = 1;
    public final static int DOWN_OUT = 2;
    public final static int DOWN_IN = 3;

    // for district spp or adfill
    public final static String ADFILL_ANDROID = "x791zcfub19w2vioo7rpnadkgne03wwo";
    public final static String SSP_ANDROID = "rfkghh59eyryzx7wntlgry0mff0yx7z1";
    public final static String RTB_ANDROID = "nzg884l0iqykvsi5eu3i022cjq3qhvff";
    public final static String UPDATE_ANDROID = "f0hDgR7qfpOL8L9gPEpB1nILo2ttTJug";


    // 服务器代发
    public static final int SERVICEAGENT = 1;
    public static final int SDKAGENT = 0;

    // 竞价 & 补余 & 服务器聚合
    public final static int ADFILL_TYPE = 997;
    public final static int ADBID_TYPE = 998;
    public final static int ADRTB_TYPE = 996;

    public final static int ADFILL_ROUTE = 0; // 补余
    public final static int SSP_ROUTE = 1; // 聚合
    public final static int ADRTB_ROUTE = 2;// 竞价

    // 网络连接超时时间
    public final static int REQUESTTIMEOUT = 15 * 1000;
    // 广告缓存时间
    public static final int DEFAULTCACHEPEROID = 5;// seconds

    public final static long AD_EXPIRE_TIME = 20 * 60 * 1000;//广告有效期 20分钟 过期时间
    public final static int VIDEOCACHESIZE = 200 * 1024 * 1024;//视频缓存大小200M
    // 误点击常量
    public final static int CLICKERROR = 1;
    public final static int CLICKNORMAL = 0;
    public final static int CLICKNONE = -1;

    // 文字匹配正则表达式
    public final static String REGULAR_MATCHNUM = "([0-9]|%|\\.){1}"; // 匹配数字
    public final static String REGULAR_MATCHBIGBRACKETS = "\\{([^\\}]*)\\}"; // 匹配{}

    // webview 图片地址
    public static final String WEBVIEW_IMAGE_BASE_PATH = "/assets/";
    // webview 控件id
    public static final int BTN_TO_PREV = 1;
    public static final int BTN_TO_NEXT = 2;
    public static final int BTN_DO_REFRESH = 3;
    public static final int BTN_DO_SHARE = 4;
    public static final int BTN_DO_CLOSE = 5;
    public static final int BTN_DO_STOP = 6;
    public static final int TOOLBAR_ID = 88;

    public final static String DL_DOWNLOADING_STATUS    = "download_status_downloading";
    public final static String DL_DOWNLOADED_STATUS     = "download_status_downloaded";
    public final static String DL_DOWNLOADFAILED_STATUS = "download_status_status";

    public final static String ADWEBVIEW_CLOSED_STATUS = "adwebview_closed_status";

    public final static int DL_DOWNLOADING_STATUS_INT = 4;
    public final static int DL_DOWNLOADED_STATUS_INT = 8;
    public final static int DL_DOWNLOADFAILED_STATUS_INT = 16;

    public final static String HK_CLICKAREA = "{CLICKAREA}";
    public final static String HK_RELATIVE_COORD = "{RELATIVE_COORD}";
    public final static String HK_ABSOLUTE_COORD = "{ABSOLUTE_COORD}";
    public final static String HK_LONGITUDE = "{LONGITUDE}";
    public final static String HK_LATITUDE = "{LATITUDE}";
    public final static String HK_UUID = "{UUID}";

    public final static String HK_GDT_DOWN_X = "__DOWN_X__";
    public final static String HK_GDT_DOWN_Y = "__DOWN_Y__";
    public final static String HK_GDT_UP_X = "__UP_X__";
    public final static String HK_GDT_UP_Y = "__UP_Y__";

    public final static String HK_DURATION = "__DURATION__";
    public final static String HK_BEGINTIME = "__BEGINTIME__";
    public final static String HK_ENDTIME = "__ENDTIME__";
    public final static String HK_FIRST_FRAME = "__FIRST_FRAME__";
    public final static String HK_LAST_FRAME = "__LAST_FRAME__";
    public final static String HK_SCENE = "__SCENE__";
    public final static String HK_TYPE = "__TYPE__";
    public final static String HK_BEHAVIOR = "__BEHAVIOR__";
    public final static String HK_STATUS = "__STATUS__";

    public final static int ADLINK_NORMAL = 0;
    public final static int ADLINK_GDT = 1;

    public final static int ACTION_ID_DOWNLOAD_START = 5;
    public final static int ACTION_ID_INSTALLED = 6;
    public final static int ACTION_ID_DOWNLOAD_FINISHED = 7;


    public final static String ADACTIVITY_DECLARATIONS      = "com.kuaiyou.utils.AdActivity";
    public final static String DOWNLOADSERVICE_DECLARATIONS = "com.kuaiyou.utils.DownloadService";
    public final static String ADVIEWWEBVIEW_DECLARATIONS   = "com.kuaiyou.utils.AdViewLandingPage";
    public final static String VIDEOACTIVITY_DECLARATIONS   = "com.kuaiyou.video.AdViewVideoActivity";

    public final static int ACTIVITY_REQUEST_CODE = 8888;
    public final static int REPORT_THREADPOOLNUM = 4;
    public final static int REQUEST_THREADPOOLNUM = 1;

    public final static String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Adview/";

    public final static String DOWNLOAD_APP_PATH = BASE_PATH + "download/apps/";
    public final static String CACHE_AD_PATH = BASE_PATH + "ad/";
    public final static String DOWNLOAD_VIDEO_PATH = BASE_PATH + "download/video/";

    public final static int LOADERROR_BLANK = 2;
    public final static int LOADERROR_AUTORUN = 1;

    public static String VIDEO_ICON_BG_COLOR = "#5FA0A0A0";

    public final static String WEBVIEW_BASEURL = "http://www.adview.com"; //wilder 2019

//    public static String MRAID_BITMAP_HTMLSTYLE = "<meta charset='utf-8'><style type='text/css'>html,body{}* { padding: 0px; margin: 0px;}a:link { text-decoration: none;}p { white-space:nowrap; overflow:hidden; text-overflow:ellipsis; vertical-align: middle;}</style>"
//            + "<body><a href=\"AD_LINK\"><img src=\"IMAGE_PATH\" alt=\"\" width=\"BITMAP_WIDTH\" height=\"BITMAP_HEIGHT\" ></a>"
//            + " </body>";

    //(wilder 2019) picture center in view
    public static String MRAID_BITMAP_HTMLSTYLE = "<HTML><Div align=\"center\"  margin=\"0px\">"
            + "<body><a href=\"AD_LINK\"><img src=\"IMAGE_PATH\" alt=\"\" width=\"BITMAP_WIDTH\" height=\"BITMAP_HEIGHT\" ></a>"
            + " </body>"
            + "</Div></HTML>";

//    public static String MRAID_SCRIPT_HTMLSTYLE = "<HTML><Div align=\"center\"  margin=\"0px\">"
//            + "<body>__SCRIPT__"
//            + " </body>"
//            + "</Div></HTML>";


//    public static String MRAID_SCRIPT_HTMLSTYLE = "<Div align=\"center\" margin=\"0px\">"
//            + "__SCRIPT__"
//            + "</Div>";
    public static String MRAID_SCRIPT_HTMLSTYLE = "<Div align=\"center\" style=\"margin: 0 auto; text-align: center\">"
            + "__SCRIPT__"
            + "</Div>";
//    public static String MRAID_SCRIPT_HTMLSTYLE = "<meta charset='utf-8'><style type='text/css'>html,body{}* { padding: 0px; margin: 0 auto;}a:link { text-decoration: none;}p { white-space:nowrap; overflow:hidden; text-overflow:ellipsis; vertical-align: middle;}</style>"
//        + "<body>__SCRIPT__"
//        + " </body>";

//    public static String MRAID_BITMAP_HTMLSTYLE = "<html>\n" +
//            "<body bgcolor=\"white\">\n" +
//            "    <table width=\"BITMAP_WIDTH\" height=\"BITMAP_HEIGHT\">\n" +
//            "        <tr>\n" +
//            "            <td align=\"center\" valign=\"center\">\n" +
//            "                <a href=\"AD_LINK\"><img src=\"IMAGE_PATH\">\n" +
//            "            </td>\n" +
//            "        </tr>\n" +
//            "    </table>\n" +
//            "</body>";
}
