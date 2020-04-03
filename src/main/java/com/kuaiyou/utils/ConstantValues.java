package com.kuaiyou.utils;

import android.os.Environment;

public class ConstantValues {

    //代码中的动态使用的类名,主要用于download功能和video
    public final static String ADACTIVITY_CLASS      = "com.kuaiyou.utils.AdActivity";
    public final static String DOWNLOADSERVICE_CLASS = "com.kuaiyou.utils.DownloadService";
    public final static String ADVIEW_LANDINGPAGE_CLASS   = "com.kuaiyou.utils.AdViewLandingPage";
    public final static String VIDEOACTIVITY_CLASS   = "com.kuaiyou.video.AdViewVideoActivity";

    //请求广告类型,对应RTB接口协议中的 st (sdkType) ，返回值里也是没有st的，详见sdk接口v3.3,
    public static final int SDK_REQ_TYPE_BANNER = 0;
    public static final int SDK_REQ_TYPE_INSTL = 1;
    public static final int SDK_REQ_TYPE_SPREAD = 4;
    public static final int SDK_REQ_TYPE_VIDEO = 5;
    public static final int SDK_REQ_TYPE_NATIVE = 6;
    public static final int SDK_REQ_TYPE_MREC = 11;      //wilder 2019 for mrec

    //请求广告的route类型
    public final static int SDK_REQ_ROUTE_ADFILL = 0; // 补余
    public final static int SDK_REQ_ROUTE_SSP = 1; // 聚合
    public final static int SDK_REQ_ROUTE_RTB = 2;// 竞价

    // 响应广告类型,对应RTB接口协议中的 at (adType) ，请求里是没有at的，BID->DSP的请求中的at是BID从开发者后台取得的，详见sdk接口v3.3
    //其中 0-4 都是采用mraidview 的webview来加载
    public final static int RESP_ADTYPE_FULLIMAGE = 0; //banner纯图片
    public final static int RESP_ADTYPE_INTERLINK = 1; //banner文字链
    public final static int RESP_ADTYPE_MIXED = 2;  //banner图文混合
    public final static int RESP_ADTYPE_INSTL = 3;  //插屏纯图片
    public final static int RESP_ADTYPE_HTML = 4;    //html
    public final static int RESP_ADTYPE_SPREAD = 5; //开屏纯图片
    public final static int RESP_ADTYPE_VIDEO = 6; //激励视频
    public final static int RESP_ADTYPE_VIDEO_PASTER = 7 ;//贴片视频
    public final static int RESP_ADTYPE_NATIVE = 8; //原生广告
    public final static int RESP_ADTYPE_VIDEO_EMBED = 11; //MREC 嵌入视频

    // 广告行为,对应resp包的act字段， 1- 广告落地页，2-下载， 3- 打开小程序（海外版不支持）
    public final static int RESP_ACT_OPENWEB = 1;
    public final static int RESP_ACT_DOWNLOAD = 2;
    public final static int RESP_ACT_OPENMAP = 4;
    public final static int RESP_ACT_SENDMSG = 8;
    public final static int RESP_ACT_SENDEMAIL = 16;
    public final static int RESP_ACT_CALL = 32;
    public final static int RESP_ACT_PLAYVIDEO = 64;
    public final static int RESP_ACT_WECHATAPP = 128;

    // 服务器下发的代发字段
    public static final int RESP_SERVICEAGENT = 1;
    public static final int RESP_SDKAGENT = 0;

    //通用广告的ui,logo & icon,closebutton等
    public final static int     UI_ADICON_ID = 90001;
    public final static int     UI_ADLOGO_ID = 90002;
    public final static int     UI_CLOSEBTN_ID = 90003;
    public final static int     UI_MRAIDVIEW_ID = 90004;
    public final static int     UI_WEBVIEW_ID = 90005;
    public final static String  UI_VIDEOICON_BG_COLOR = "#5FA0A0A0"; //背景色

    // 联网方式，用于kybaseview的网络发包
    public final static String POST = "POST";
    public final static String GET = "GET";

    //native resp 原生广告：浏览器类型
    public final static int NATIVE_RESP_TYPE_INAPP = 0;
    public final static int NATIVE_RESP_TYPE_SYS = 1;

    // banner 请求的size大小
    public final static int BANNER_REQ_SIZE_AUTO_FILL = 0;
    public final static int BANNER_REQ_SIZE_MREC = 1;        //320x250
    public final static int BANNER_REQ_SIZE_480X75 = 2;
    public final static int BANNER_REQ_SIZE_728X90 = 3;
    public final static int BANNER_REQ_SIZE_SMART = 5;

    //INSTL 请求的插屏广告大小
    public final static int INSTL_REQ_SIZE = 4; //300 x 300
    public final static int INSTL_REQ_SIZE_320X480 = 8;
    public final static int INSTL_REQ_SIZE_600X500 = 7;
    public final static int INSTL_REQ_SIZE_300X250 = 6;

    // handler信息通知
    public final static int NOTIFY_RESP_TIMEOUT_CHECK = -1;
    public final static int NOTIFY_RESP_RECEIVEAD_OK = 0;
    public final static int NOTIFY_RESP_RECEIVEAD_ERROR = 1;
    public final static int NOTIFY_RESP_CONNCET_FAIL = 2;
    public final static int NOTIFY_RESP_ROTATE_AD = 3;
    public final static int NOTIFY_NATIVE_RESP_STATUS= 4; //native used
    public final static int NOTIFY_REQ_GPID_FETCH_DONE = 8; //gpid got done,发生在reqad()之前

    /*------------------spread 相关： handler status--------------------*/
    public static final int SPREAD_RESP_FAILED = -1;
    public static final int SPREAD_REQ_INIT_SUCCESS = 0;
    public static final int SPREAD_RESP_BITMAP_RECEIVED = 1;
    public static final int SPREAD_RESP_HTML_RECEIVED = 4;
    public static final int SPREAD_RESP_DELAY = 2;
    public static final int SPREAD_RESP_TIMEUP_STRICT = 3;  //开屏展示的限定时间，一般为3s，到时间会发送建议关闭事件
    public static final int SPREAD_RESP_COUNTDOWN = 5;
    public static final int SPREAD_RESP_USERCANCEL = 6;
    public static final int SPREAD_RESP_IMPRESSION = 7;
    public static final int SPREAD_RESP_UIDELAY_UPDATE = 8;
    public static final int SPREAD_RESP_LANDINGPAGE_CLOSEDSTATUS_CHECK = 9;

    // spread logo
    public final static int SPREAD_RESP_HAS_LOGO = 1;
    public final static int SPREAD_RESP_NO_LOGO = 2;

    /*------------------------- spread  end      --------------------*/


    /***** ---------------------- Mixed 广告类型使用 ------------------***/
    // MIXED 广告布局控件ID
    public final static int MIXED_UI_ICONID = 10001;
    public final static int MIXED_UI_TITLEID = 10002;
    public final static int MIXED_UI_SUBTITLE_ID = 10003;
    public final static int MIXED_UI_BEHAVEICON_ID = 10004;
    public final static int MIXED_UI_DESCRIPTTEXT_ID = 10005;

    // spread UI使用
    public final static int SPREAD_UI_TEXTID = 10007;
    public final static int SPREAD_UI_MIXLAYOUTID = 10008;  //mixed 使用
    public static final int SPREAD_UI_FRAMEID = 10009;
    public final static int SPREAD_UI_NOTIFYLAYOUTID = 10010;
    public final static int SPREAD_UI_COUNTERID = 10011;
    public static final int SPREAD_UI_LOGOIMAGEID = 10013;

    // spread UI 开屏布局分部常量
    public final static int SPREAD_UI_EXTRA1 = -1;
    public final static int SPREAD_UI_EXTRA2 = -2;
    public final static int SPREAD_UI_EXTRA3 = -3;
    public final static int SPREAD_UI_NULL = 0;
    public final static int SPREAD_UI_TOP = 1;
    public final static int SPREAD_UI_CENTER = 2;
    public final static int SPREAD_UI_BOTTOM = 3;
    public final static int SPREAD_UI_ALLCENTER = 4;
    public final static int SPREAD_UI_SCALE_NOHTML = 5;
    public final static int SPREAD_UI_SCALE_INCLUDEHTML = 6;
    public final static int SPREAD_UI_NOSCALED = 7;

    // 混合Mixed类型所使用广告条颜色类名称
    public static final String MIXED_ICONBACKGROUND_COLOR = "icon";
    public static final String MIXED_BEHAVEBACKGROUND_COLOR = "behave";
    public static final String MIXED_PARENTBACKGROUND_COLOR = "parent";
    public static final String MIXED_TITLEBACKGROUND_COLOR = "title";
    public static final String MIXED_SUBTITLEBACKGROUND_COLOR = "subtitle";
    public static final String MIXED_KEYWORDBACKGROUND_COLOR = "keyword";

    // 插屏点击广播
    public final static String INSTL_CLICKBROADCAST = "clickadview";

    public final static String INSTL_POPWINDOW_WEBVIEW_BACKGOUNDCOLOR = "#aa212121";
    // 插屏布局名称常量
    public final static String INSTL_WIDTH_KEY = "instlWidth";
    public final static String INSTL_HEIGHT_KEY = "instlHeight";

    // sharedPreference files name
    public final static String INSTL_SP_BITMAPMAPPING_FILE = "sp_bitmap_local";
    public final static String SP_LASTMODIFY_FILE = "sp_imagemodifysince";
    public final static String SP_LASTVISIT_FILE = "sp_lastvisittime";
    public final static String SP_ADVINFO_FILE = "sp_dev_info";
    public final static String SP_SPREADINFO_FILE = "sp_spread_info";

    public final static String SP_INSTLINFO_FILE = "sp_instl_info";
    public final static String SP_BANNERINFO_FILE = "sp_banner_info";
    public final static String SP_VIDEO_NAME_FILE = "local_dw_video";
    public final static String SP_DOWNLOAD_INFO = "sp_download_info";
    // for district spp or adfill
    public final static String ROUTE_ADFILL_ANDROID_MD5KEY = "x791zcfub19w2vioo7rpnadkgne03wwo";
    public final static String ROUTE_SSP_ANDROID_MD5KEY = "rfkghh59eyryzx7wntlgry0mff0yx7z1";
    public final static String ROUTE_RTB_ANDROID_MD5KEY = "nzg884l0iqykvsi5eu3i022cjq3qhvff";
    //这个UPDATE_ANDROID一定不能删除，在InitSDKManager中有getDeclaredField(CONSTANT_CLASS_NAME, "UPDATE_ANDROID")
    public final static String UPDATE_ANDROID_MD5KEY = "f0hDgR7qfpOL8L9gPEpB1nILo2ttTJug";

    // 广告请求的route类型，竞价 & 补余 & 服务器聚合，用于内部开发
    public final static int ROUTE_ADFILL_TYPE = 997;
    public final static int ROUTE_ADBID_TYPE = 998;
    public final static int ROUTE_ADRTB_TYPE = 996;

    // 网络连接超时时间
    public final static int REQUEST_CONNECT_TIMEOUT = 15 * 1000;
    // 广告缓存时间
    public static final int DEFAULT_CACHE_PEROID = 5; //seconds

    public final static long AD_EXPIRE_TIME = 20 * 60 * 1000;//广告有效期 20分钟 过期时间
    public final static int VIDEO_CACHE_SIZE = 200 * 1024 * 1024;//视频缓存大小200M
    // 误点击常量
    public final static int CLICK_ERROR = 1;
    public final static int CLICK_NORMAL = 0;
    public final static int CLICK_NONE = -1;

    // 文字匹配正则表达式
    public final static String REGULAR_MATCH_NUM = "([0-9]|%|\\.){1}"; // 匹配数字
    public final static String REGULAR_MATCH_BIGBRACKETS = "\\{([^\\}]*)\\}"; // 匹配{}

    // webview 和使用的图片地址，都放到assets中
    public static final String WEBVIEW_IMAGE_BASE_PATH = "/assets/";

    public final static String DL_DOWNLOADING_STATUS    = "download_status_downloading";
    public final static String DL_DOWNLOADED_STATUS     = "download_status_downloaded";
    public final static String DL_DOWNLOADFAILED_STATUS = "download_status_failed";
    //关闭落地页会广播closed事件，在spread中会有条件接受
    public final static String ADWEBVIEW_BROADCAST_LANDINGPAGE_CLOSED_STATUS = "adwebview_landingpage_closed_status";

    //请求和report的thread的并发数量
    public final static int REPORT_THREADPOOL_NUM = 4;
    public final static int REQUEST_THREADPOOL_NUM = 1;

    ////////////////////// 该权限需要WRITE_EXTERNAL_STORAGE，目前同步用于downloadAPP（海外版已取消该功能）////////////////
    // ////////////        downloadVideo(海外版用的是在线video)         /////////////////////////////////////
    public final static String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Adview/";
    public final static String DOWNLOAD_APP_PATH = BASE_PATH + "download/apps/";
    public final static String DOWNLOAD_VIDEO_PATH = BASE_PATH + "download/video/";
    //public final static String CACHE_AD_PATH = BASE_PATH + "ad/"; 该目录用于存放adicon和adlogo，海外版会不下发这2个字段，如下发则使用应用内存储
    ////////////////// 权限 end//////////////////////////////////////////////////////////////////////////////////////////

    public final static int MRAID_LOADERROR_BLANK = 2;  //mraid 白屏
    public final static int MRAID_LOADERROR_AUTORUN = 1;    //mraid 自动运行

    public final static String WEBVIEW_BASEURL = "http://www.adview.com"; //wilder 2019, for some base url should be baseurl, else may cause some error
    public final static String VAST_OMSDK_ASSETS_URL = "file:///android_asset"; //for OMSDK used for native js file for vast video

    //混合视频广告 Mixed vast video used
    public final static int MIXED_VAST_START_TYPE = 1;
    public final static int MIXED_VAST_MIDDLE_TYPE = 2;
    public final static int MIXED_VAST_END_TYPE = 3;
    public final static int MIXED_VAST_DURATION_TYPE = 4;
    public final static int MIXED_VAST_MEDIA_FILE = 5;
    public final static int MIXED_VAST_IMPRESSION = 6;
    public final static int MIXED_VAST_CLICKTHROUGH = 7;
    public final static int MIXED_VAST_CLICKTRACKING = 8;
    public final static int MIXED_VAST_EXTENSION = 9;

    public static String MIXED_VAST_STARTEVENT_STR = "__START_EVENT__";
    public static String MIXED_VAST_MIDDLEEVENT_STR = "__MIDDLE_EVENT__";
    public static String MIXED_VAST_ENDEVENT_STR = "__END_EVENT__";
    public static String MIXED_VAST_DURATION_STR = "__DURATION__";
    public static String MIXED_VAST_MEDIAFILE_STR = "__MEDIAFILE__";
    public static String MIXED_VAST_IMPRESSION_STR = "__IMPRESSION__";
    public static String MIXED_VAST_CLICKTHROUGHT_STR = "__CLICKTHROUGHT__";
    public static String MIXED_VAST_CLICKTRACKING_STR = "__CLICKTRACKING__";
    public static String MIXED_VAST_EXTENSION_STR = "__EXTENSION__";

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

    //webView 里内容自适应屏幕
    public static String h5_style = "<style>" +
            "img{max-width:100%; height:auto}" +
            "video{max-width:100%;height:auto}" +
            "</style>";

    public static String MRAID_SCRIPT_HTMLSTYLE =
            "<Div align=\"center\" style=\"margin: 0 auto; text-align: center\">"
            // + "<style>" + "img{max-width:100%; height:auto}" + "video{max-width:100%;height:auto}" + "</style>" //can make auto fit ?
            + "<script src=\"file:///android_asset/MRAID.js\" type=\"text/javascript\"></script>"
            + "<script src=\"file:///android_asset/omsdk-v1.js\" type=\"text/javascript\"></script>"
            // + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=2,user-scalable=yes\" />"
            //        + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no\" />"
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


    /////////////////////////////////////////////////VPAID used ///////////////////////////////////////////////////////////////
    public static final String ENVIRONMENT_VARS = "{ " +
            "slot: document.getElementById('adview-slot'), " +
            "videoSlot: document.getElementById('adview-videoslot'), " +
            "videoSlotCanAutoPlay: true }";

    public static final String VPAID_CREATIVE_URL_STRING = "[VPAID_CREATIVE_URL]";
    public static final String VPAID_BRIDGE_JS_STRING = "[VPAID_BRIDGE_JS]";
/*    public static final String VPAID_HTML =
            "<html>\n" +
                    "    <header>\n" +
                    "    </header>\n" +
                    "    <meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>" +
                    "    <body style=\"margin:0\">\n" +
                    "         <script type=\"text/javascript\">\n" +
                    "            [VPAID_BRIDGE_JS]\n" +
                    "         </script>\n" +
                    "        <script src=\"[VPAID_CREATIVE_URL]\" type=\"text/javascript\"></script>\n" +
                    "        <video id=\"adview-videoslot\" style=\"position:absolute; width:100%; height:100%; z-index:1; -webkit-transform: translate3d(0, 0, 0);\" ></video>\n" +
                    "        <div id=\"adview-slot\" style=\"width:100%; height:100%; z-index: 3; position:absolute; -webkit-user-select: none;\" ></div>\n" +
                    "    </body>\n" +
                    "</html>";*/

     /*
  "        <script src=\"[VPAID_BRIDGE_JS]\" type=\"text/javascript\"></script>\n" +
          "        <script src=\"[VPAID_UI_JS]\" type=\"text/javascript\"></script>\n" +
          "        <script src=\"[VPAID_CREATIVE_URL]\" type=\"text/javascript\"></script>\n" +
   */
    /*
    "        <video style=\"position:absolute; width:100%; height:100%; z-index:1;\" id=\"adview-videoslot\"></video>\n" +

    object-fit:contain;
    object-fit:fill;
    "        <video id=\"adview-videoslot\" width=\"100%\" height=\"100%\" preload=\"none\" style=\"-webkit-transform: translate3d(0, 0, 0)\">" +
     "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\" />\n" +
    * */

    /*
    private static final String VPAID_HTML =
            "<html>\n" +
                    "  <head>\n" +
                    "  </head>\n" +
                    "\n" +
                    "  <body>\n" +
                    "    <div id=\"mainContainer\" style = \"width:100%; height:100%; position:relative;\">\n" +
                    "      <div id=\"content\" style = \"width:100%; height:100%; position:absolute;\">\n" +
                    "        <video id=\"contentElement\" style = \"width:100%; height:100%; overflow: hidden;\">\n" +
                    "        </video>\n" +
                    "      </div>\n" +
                    "      <div id=\"adContainer\" style = \"width:100%; height:100%; position:absolute;\"></div>\n" +
                    "    </div>\n" +
                    "         <script type=\"text/javascript\">\n" +
                    "            [VPAID_BRIDGE_JS]\n" +
                    "         </script>\n" +
                    "        <script src=\"[VPAID_CREATIVE_URL]\" type=\"text/javascript\"></script>\n" +
                    "  </body>\n" +
            "</html>";
    */
}
