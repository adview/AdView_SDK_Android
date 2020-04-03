package com.kuaiyou.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.SparseArray;

import com.kuaiyou.obj.DownAppInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import static com.kuaiyou.utils.ConstantValues.ADACTIVITY_CLASS;

/**
 * 下载
 */
public class DownloadService extends Service {

    private int smallIcon = 17301633;
    private Bitmap largeIcon = null;
    private NotificationManager updateNotificationManager = null;// 状态栏通知的管理类
    // private Notification updateNotification = null;
    //downloadservice 使用的判断
    private final static int ADLINK_NORMAL = 0;
    private final static int ADLINK_GDT = 1;

    //    private NotificationCompat notificationCompat;
    private Notification.Builder notificationBuilder;
    //    private Builder notificationBuilder = null;
    private Intent updateIntent = null;
    private PendingIntent updatePendingIntent = null;

    private final static String LOADING = "Loading"; //"正在加载";
    private final static String PREPAREDOWNLOAD = "Preparing Loading ...";//"正在准备下载...";
    private final static String STARTDOWNLOAD = "Start Downloading";//"开始下载";
    private final static String DOWNLOADING = "Downloading";//"正在下载";
    private final static String FINISHINGDOWNLOAD = "Download finished";//"下载完成";

    private final static String FAILEDDOWNLOAD1 = "Download init fail";//"下载初始化错误";
    private final static String FAILEDDOWNLOAD2 = "Download status bar creation fail";//"下载通知栏创建失败";
    private final static String FAILEDDOWNLOAD3 = "Download create file fail";//"创建文件失败";
    private final static String FAILEDDOWNLOAD4 = "Download network fail 001"; //"网络异常_001";
    private final static String FAILEDDOWNLOAD5 = "Download network fail 002"; //"网络异常_002";
    private final static String FAILEDDOWNLOAD6 = "Download no network";//"没有网络连接";
    private final static String FAILEDDOWNLOAD7 = "Download unknown error"; //"下载未知错误";

    private final static int DOWNLOADINIT_STATUS = -3;
    private final static int DOWNLOADFAILED_STATUS = 1;
    private final static int DOWNLOADSUCCESSED_STATUS = 0;
    private final static int DOWNLOADPROGRESSING_STATUS = -1;
    private final static int DOWNLOADFILEEXIST_STATUS = -2;


    private final static int ACTION_ID_DOWNLOAD_START = 5;
    private final static int ACTION_ID_DOWNLOAD_FINISHED = 7;

    private final static String FAILED = "failed";
    private final static String PATH = "path";

    private SparseArray<DownAppInfo> notifyPath; // 用SparseArray(稀疏数组)来代替HashMap


    private Handler updateHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            try {
                LocalBroadcastManager lbm = null;
                Intent lbmIntent = null;
                Intent installIntent = null;
                Uri uri = null;
                int notifyId = msg.getData().getInt("notifyId");
                switch (msg.what) {
                    case DOWNLOADSUCCESSED_STATUS:
                    case DOWNLOADFILEEXIST_STATUS:
                        String pathName = msg.getData().getString(PATH);
                        if (null != pathName) {
                            File temp = new File(pathName);
                            uri = Uri.fromFile(temp);
                            if (!AdViewUtils.checkClickPermission(DownloadService.this, ADACTIVITY_CLASS, PackageManager.GET_ACTIVITIES)) {
                                installIntent = new Intent();
                                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                installIntent.setAction("android.intent.action.VIEW");
                                installIntent.setDataAndType(uri,"application/vnd.android.package-archive");
                                DownloadService.this.updatePendingIntent = PendingIntent.getActivity(DownloadService.this, 0,
                                                installIntent, 0);
                                DownloadService.this.startActivity(installIntent);
                            } else {
                                //wilder 2020 downloadservice 暂时关闭
//                                Intent intent = new Intent(DownloadService.this, AdActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra("path", Uri.fromFile(temp));
//                                intent.putExtra("install_report", notifyPath.get(notifyId).getInstallUrls());
//                                intent.putExtra("gdt_conversion_link", notifyPath.get(notifyId).getGdtExtraUrls());
//                                intent.putExtra("click_id_gdt", notifyPath.get(notifyId).getClickid_gdt());
//                                DownloadService.this.startActivity(intent);
                            }
                            notificationBuilder.setTicker(FINISHINGDOWNLOAD);
                            updateNotificationManager.notify(
                                    notifyId, notificationBuilder.build());
                            updateNotificationManager
                                    .cancel(notifyId);
                            lbm = LocalBroadcastManager.getInstance(DownloadService.this);
                            lbmIntent = new Intent(ConstantValues.DL_DOWNLOADED_STATUS);
                            lbm.sendBroadcast(lbmIntent);
                            // 汇报下载完成效果数
                            AdViewUtils.reportEffect(notifyPath.get(notifyId).getDownloadedUrls());
                            if (!TextUtils.isEmpty(notifyPath.get(notifyId).getGdtExtraUrls()))
                                AdViewUtils.reportEffect(new String[]{
                                        AdViewUtils.getGdtActionLink(notifyPath.get(notifyId).getGdtExtraUrls(),
                                        notifyPath.get(notifyId).getClickid_gdt(),
                                        ACTION_ID_DOWNLOAD_FINISHED)});
                        }
                        if (null != notifyPath.get(notifyId)) {
                            notifyPath.delete(notifyId);
                        }
                        DownloadService.this.stopService(DownloadService.this.updateIntent);
                        break;
                    case DOWNLOADFAILED_STATUS:
                        String description = msg.getData().getString(FAILED);
                        //Toast.makeText(DownloadService.this, description, Toast.LENGTH_LONG).show();
                        AdViewUtils.logInfo("[DownloadService] status : " + description );
                        if (null != notificationBuilder) {
                            notificationBuilder.setOngoing(false);
                            notificationBuilder.setAutoCancel(true);
                            notificationBuilder.setTicker(description);
                            notificationBuilder.setContentTitle(description)
                                    .setProgress(0, 0, false)
                                    .setContentIntent(updatePendingIntent);
                            DownloadService.this.updateNotificationManager.notify(
                                    notifyId, notificationBuilder.build());
                            DownloadService.this
                                    .stopService(DownloadService.this.updateIntent);
                        }

                        if (null != notifyPath && null != notifyPath.get(notifyId)) {
                            if (null != notifyPath.get(notifyId).getPathName())
                                notifyPath.get(notifyId).getPathName().delete();
                            notifyPath.delete(notifyId);
                        }
                        if (null != updateNotificationManager)
                            updateNotificationManager.cancel(notifyId);

                        lbm = LocalBroadcastManager.getInstance(DownloadService.this);
                        lbmIntent = new Intent(ConstantValues.DL_DOWNLOADFAILED_STATUS);
                        lbm.sendBroadcast(lbmIntent);

//                        if (null != downloadStatusListener)
//                            downloadStatusListener.onDownloadStatusChange(ConstantValues.DL_DOWNLOADFAILED_STATUS);
                        break;
                    case DOWNLOADINIT_STATUS:
                        //Toast.makeText(DownloadService.this, PREPAREDOWNLOAD,Toast.LENGTH_SHORT).show();
                        AdViewUtils.logInfo("[DownloadService] status : " + PREPAREDOWNLOAD);
                        break;
                    case DOWNLOADPROGRESSING_STATUS:
                        //Toast.makeText(DownloadService.this, DOWNLOADING, Toast.LENGTH_SHORT).show();
                        AdViewUtils.logInfo("[DownloadService] status : " + DOWNLOADING );
                        break;
                    default:
                        if (null != DownloadService.this.updateIntent)
                            DownloadService.this.stopService(DownloadService.this.updateIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };


    // /**
    // * 二次确认
    // *
    // * @return
    // */
    // public static boolean isSecConfirm() {
    // return DownloadService.secConfirm;
    // }
    //
    // public static void setSecConfirm(boolean secConfirm) {
    // DownloadService.secConfirm = secConfirm;
    // }


    /**
     * IBinder:进行远程操作对象的一个基接口。定义了为在提供进程间和跨进程间的调用时提供高性能的轻量级远程调用的核心部分
     */
    public IBinder onBind(Intent intent) {
        return new InterfaceBinder();
    }


    public class InterfaceBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // 取消通知栏 信息
        try {
            if (null != updateNotificationManager)
                updateNotificationManager.cancelAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            int notifyId = (int) System.currentTimeMillis();
            // if (isSecConfirm())
            // createConfirmDialog(this, intent, notifyId);
            // else {
            addToNotifyPath(intent, notifyId);
            new Thread(new DownloadRunnable(notifyId)).start();
            // 正在准备下载
            Message message = new Message();
            Bundle bundle = new Bundle();
            message.what = DOWNLOADINIT_STATUS;
            message.setData(bundle);
            DownloadService.this.updateHandler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
            Message message = new Message();
            Bundle bundle = new Bundle();
            message.what = DOWNLOADFAILED_STATUS;
            bundle.putString(FAILED, FAILEDDOWNLOAD1);
            message.setData(bundle);
            DownloadService.this.updateHandler.sendMessage(message);
            return super.onStartCommand(intent, flags, startId);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    // /**
    // * 开始下载
    // */
    // public void onStart(Intent intent, int startId) {
    //
    // }

    private String channelName = "adview_ad";
    private String channelId = "14";

    /**
     * 创建通知栏
     *
     * @param notifyId 通知的id号
     */
    private void createNotication(int notifyId) {
        String appName = null;
        try {
            if (null != notifyPath)
                appName = notifyPath.get(notifyId).getAppName();

            updateNotificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                updateNotificationManager.createNotificationChannel(channel);
                notificationBuilder = new Notification.Builder(this, channelId);
            } else
                notificationBuilder = new Notification.Builder(this);
            // 设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
            updateIntent = new Intent(this, this.getClass());
            notificationBuilder.setOnlyAlertOnce(true);
            updatePendingIntent = PendingIntent.getActivity(this, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setOngoing(true);
            notificationBuilder
                    .setContentTitle(null == appName ? LOADING : appName)// 标题
                    .setLargeIcon(largeIcon).setSmallIcon(smallIcon)// 大小图标
                    .setTicker(STARTDOWNLOAD + null == appName ? "" : appName)// /通知首次出现在通知栏，带上升动画效果的
                    .setContentIntent(updatePendingIntent) // 设置通知栏点击Intent
                    .setProgress(0, 0, true);// max:进度条最大数值
            // 、progress:当前进度、indeterminate:表示进度是否不确定，true为不确定，false为确定
            updateNotificationManager.notify(notifyId,
                    notificationBuilder.build());

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Message message = new Message();
            Bundle bundle = new Bundle();
            message.what = DOWNLOADFAILED_STATUS;
            bundle.putString(FAILED, FAILEDDOWNLOAD2);
            message.setData(bundle);
            DownloadService.this.updateHandler.sendMessage(message);
        }
    }

    private boolean gdtResponse(String url, int notifyId) {
        String result = AdViewUtils.getResponse(url, "");
        if (!TextUtils.isEmpty(result)) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                JSONObject innerObject = new JSONObject(jsonObject.getString("data"));
                notifyPath.get(notifyId).setClickid_gdt(innerObject.optString("clickid"));
                notifyPath.get(notifyId).setDownUrl(innerObject.optString("dstlink"));
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 下载文件
     *
     * @param notifyId
     * @return
     * @throws Exception
     */
    public long downloadFile(int notifyId) throws Exception {
        long currentMillis = System.currentTimeMillis();
        int currentSize = 0;
        long totalSize = 0L;
        int updateTotalSize = 0;
        boolean isFirst = true;
        String fileName;
        String tempUrl;
        HttpURLConnection httpConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;
        String appName = notifyPath.get(notifyId).getAppName();
        Message message = DownloadService.this.updateHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt("notifyId", notifyId);
        try {
            createNotication(notifyId);

            if (notifyPath.get(notifyId).getAlType() == ADLINK_GDT)
                gdtResponse(notifyPath.get(notifyId).getDownUrl(), notifyId);
            // 汇报开始下载效果数
            AdViewUtils.reportEffect(notifyPath.get(notifyId).getDownloadstartUrls());
            if (!TextUtils.isEmpty(notifyPath.get(notifyId).getGdtExtraUrls()))
                AdViewUtils.reportEffect(new String[]{AdViewUtils.getGdtActionLink(notifyPath.get(notifyId).getGdtExtraUrls(),
                                                notifyPath.get(notifyId).getClickid_gdt(),
                                                ACTION_ID_DOWNLOAD_START)});

            AdViewUtils.logInfo("[DownloadService] Download File: " + notifyPath.get(notifyId).getDownUrl());

            URL url = new URL(notifyPath.get(notifyId).getDownUrl());
            int responseCode = -1;
            while (isFirst || 302 == responseCode) {
                isFirst = false;
                if (url.getProtocol().startsWith("https"))
                    httpConnection = (HttpsURLConnection) url.openConnection();
                else
                    httpConnection = (HttpURLConnection) url.openConnection();
                // httpConnection.setRequestProperty("Accept-Encoding", "identity");
                // httpConnection
                // .setRequestProperty("User-Agent", "PacificHttpClient");
                if (currentSize > 0) {
                    httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
                }
                httpConnection.setConnectTimeout(10000);
                httpConnection.setReadTimeout(20000);
                responseCode = httpConnection.getResponseCode();
                tempUrl = httpConnection.getHeaderField("Location");
                if (!TextUtils.isEmpty(tempUrl))
                    url = new URL(tempUrl);
            }
            updateTotalSize = httpConnection.getContentLength();
            URL absUrl = httpConnection.getURL();// 获得真实Url
            fileName = absUrl.getFile();// 获取下载文件名称
            if (fileName.contains("/"))
                fileName = fileName.substring(fileName.lastIndexOf("/"));
            // String fileType = fileName.substring(fileName.lastIndexOf("."),
            // fileName.length());
            // // for gdt //no use
            // if (fileName.contains("qz_gdt"))
            // fileName = fileName.substring(0, fileName.indexOf("qz_gdt"));
            fileName = fileName.hashCode() + ".apk";

            // 已下载
            if (hasDownloaded(ConstantValues.DOWNLOAD_APP_PATH + fileName, updateTotalSize)) {
                message.what = DOWNLOADFILEEXIST_STATUS;
                bundle.putString(PATH, ConstantValues.DOWNLOAD_APP_PATH + fileName);
                message.setData(bundle);
                DownloadService.this.updateHandler.sendMessage(message);
                return 0;
            }
            if (isDownloadingWithAbsUrl(absUrl.getPath())
                    || isDownloadingWithFileName(fileName)) {
                message.what = DOWNLOADPROGRESSING_STATUS;
                bundle.putString(FAILED, DOWNLOADING);
                message.setData(bundle);
                DownloadService.this.updateHandler.sendMessage(message);

                return -1;
            }
            // 创建文件夹失败
            if (!createFilePath(fileName)) {
                message.what = DOWNLOADFAILED_STATUS;
                bundle.putString(FAILED, FAILEDDOWNLOAD3);
                message.setData(bundle);
                DownloadService.this.updateHandler.sendMessage(message);
                return 0;
            }

            if (httpConnection.getResponseCode() == 404) {
                message.what = DOWNLOADFAILED_STATUS;
                bundle.putString(FAILED, FAILEDDOWNLOAD4);
                message.setData(bundle);
                DownloadService.this.updateHandler.sendMessage(message);
                return 0;
            }
            // 存储信息
            notifyPath.get(notifyId).setDownUrl(absUrl.getPath());
            notifyPath.get(notifyId).setFileName(fileName);
            notifyPath.get(notifyId).setPathName(new File(ConstantValues.DOWNLOAD_APP_PATH, fileName));
            notifyPath.get(notifyId).setPathDir(new File(ConstantValues.DOWNLOAD_APP_PATH));

            is = httpConnection.getInputStream();
            StringBuilder sb = new StringBuilder();
            if (updateTotalSize > 0 && updateTotalSize < 10 * 1024) {
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(is));
                sb.append(bufferedReader.readLine());
                Pattern p = Pattern.compile("url\\s?=\\s?http:([^\"])+");
                Matcher matcher = p.matcher(sb.toString());
                while (matcher.find()) {
                    notifyPath.get(notifyId).setDownUrl(matcher.group().replaceFirst("url\\s?=\\s?", ""));
                    downloadFile(notifyId);
                    return 1;
                }
            }

            /**
             * 下载过程，修改状态栏进度条
             */
            fos = new FileOutputStream(ConstantValues.DOWNLOAD_APP_PATH + fileName, false);
            byte[] buffer = new byte[4096];
            int readsize = 0;
            while ((readsize = is.read(buffer)) > 0) {
                fos.write(buffer, 0, readsize);
                totalSize += readsize;
                if (-1 == updateTotalSize) {
                    if (System.currentTimeMillis() - currentMillis > 1500) {
                        notificationBuilder
                                .setContentTitle(
                                        null == appName ? DOWNLOADING : appName)
                                // 大小图标
                                .setTicker(
                                        DOWNLOADING + null == appName ? ""
                                                : appName)// /通知首次出现在通知栏，带上升动画效果的
                                .setContentIntent(updatePendingIntent) // 设置通知栏点击Intent
                                .setProgress(0, 0, true);// max:进度条最大数值
                        this.updateNotificationManager.notify(notifyId,
                                notificationBuilder.build());
                        currentMillis = System.currentTimeMillis();
                    }
                } else if (totalSize != updateTotalSize) {
                    if (System.currentTimeMillis() - currentMillis > 1000) {
                        int percent = (int) (totalSize * 100 / updateTotalSize);
                        notificationBuilder
                                .setContentTitle(
                                        null == appName ? DOWNLOADING : appName)
                                .setProgress(
                                        100,
                                        (int) (totalSize * 100 / updateTotalSize),
                                        false).setContentText(percent + "%");
                        this.updateNotificationManager.notify(notifyId,
                                notificationBuilder.build());
                        currentMillis = System.currentTimeMillis();
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception

            e.printStackTrace();
            message.what = DOWNLOADFAILED_STATUS;
            bundle.putString(FAILED, FAILEDDOWNLOAD5);
            message.setData(bundle);
            DownloadService.this.updateHandler.sendMessage(message);

        } finally {
            if (fos != null) {
                fos.flush(); // 把缓冲区的数据强行输出,(注意不要和frush()刷新混淆了)
            }
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return totalSize;
    }


    /**
     * 当前URL是否正在下载 check whether current absUrl is Downloading
     *
     * @param absUrl
     * @return true is downloading,false otherwise
     */
    private boolean isDownloadingWithAbsUrl(String absUrl) {
        boolean isDownloading = false;
        if (null == notifyPath)
            return isDownloading;
        if (null != absUrl && absUrl.length() > 0) {
            for (int i = 0; i < notifyPath.size(); i++) {
                if (null != notifyPath.valueAt(i).getDownUrl()
                        && notifyPath.valueAt(i).getDownUrl().equals(absUrl)) {
                    isDownloading = true;
                    break;
                }
            }
        }
        return isDownloading;
    }

    /**
     * 当前文件名是否正在下载 check whether current notifyID is Downloading
     *
     * @param fileName
     * @return true is downloading,false otherwise
     */
    private boolean isDownloadingWithFileName(String fileName) {
        boolean isDownloading = false;
        if (null == notifyPath)
            return isDownloading;
        if (null != fileName && fileName.length() > 0) {
            for (int i = 0; i < notifyPath.size(); i++) {
                if (null != notifyPath.valueAt(i).getFileName()
                        && notifyPath.valueAt(i).getFileName().equals(fileName)) {
                    isDownloading = true;
                    break;
                }
            }
        }
        return isDownloading;
    }

    /**
     * 当前文件夹是否正在下载 check whether current notifyID is Downloading
     *
     * @param packageName
     * @return true is downloading,false otherwise
     */
    private boolean isDownloadingWithPackageName(String packageName) {
        boolean isDownloading = false;
        if (null == notifyPath)
            return isDownloading;
        if (null != packageName && packageName.length() > 0) {
            for (int i = 0; i < notifyPath.size(); i++) {
                if (null != notifyPath.valueAt(i).getPackageName()
                        && notifyPath.valueAt(i).getPackageName()
                        .equals(packageName)) {
                    isDownloading = true;
                    break;
                }
            }
        }
        return isDownloading;
    }

    /**
     * 检查是否已下载 check whether current path has been Downloaded
     *
     * @param path
     * @param totalSize
     * @return true has been Downloaded,false otherwise
     */
    private boolean hasDownloaded(String path, int totalSize) {
        try {
            File filePath = new File(path);
            if (filePath.exists() && filePath.length() == totalSize) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否存在文件路径 create download path & dir
     *
     * @param fileName
     * @return true is create succesed
     */
    private boolean createFilePath(String fileName) {
        if (null == fileName)
            return false;
        if (null == ConstantValues.DOWNLOAD_APP_PATH)
            return false;
        File fileDir = new File(ConstantValues.DOWNLOAD_APP_PATH);
        File filePath = new File(ConstantValues.DOWNLOAD_APP_PATH, fileName);
        if (!fileDir.exists())
            if (!fileDir.mkdirs())
                return false;

        if (!filePath.exists())
            try {
                if (!filePath.createNewFile())
                    return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        return true;
    }

    /**
     * 下载列表-添加到通知栏
     *
     * @param intent
     * @param notifyId
     */
    public void addToNotifyPath(Intent intent, int notifyId) {
        String path = intent.getStringExtra("adview_url");
        String packageName = intent.getStringExtra("package");
        // String appIcon = intent.getStringExtra("appicon");
        String appName = intent.getStringExtra("appname");
        if (isDownloadingWithPackageName(packageName)) {
            Message message = DownloadService.this.updateHandler
                    .obtainMessage();
            Bundle bundle = new Bundle();
            message.what = DOWNLOADPROGRESSING_STATUS;
            bundle.putString(FAILED, DOWNLOADING);
            message.setData(bundle);
            DownloadService.this.updateHandler.sendMessage(message);
            return;
        }
        // 下载的小图标为app的icon
        if (AdViewUtils.useSelfIcon) {
            PackageManager pm = getPackageManager();
            try {
                PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
                smallIcon = packageInfo.applicationInfo.icon;
                if (smallIcon == 0)
                    smallIcon = 17301633;
            } catch (NameNotFoundException e) {
                smallIcon = 17301633;
                e.printStackTrace();
            }
        }
        // 大图标与小图标一致
        largeIcon = BitmapFactory.decodeResource(getResources(), 17301633);
        if (null != appName && appName.length() == 0)
            appName = null;
        if (null == notifyPath)
            notifyPath = new SparseArray<DownAppInfo>();
        DownAppInfo downAppInfo = new DownAppInfo();
        downAppInfo.setDownUrl(path);
        // downAppInfo.setPathName(updateFile);
        // downAppInfo.setPathDir(updateDir);
        downAppInfo.setGdtExtraUrls(intent.getStringExtra("gdt_conversion_link"));
        downAppInfo.setAlType(intent.getIntExtra("altype", 0));
        downAppInfo.setAppName(appName);
        downAppInfo.setPackageName(packageName);
        downAppInfo.setDownloadstartUrls(intent.getStringArrayExtra("downloadstart_report"));
        downAppInfo.setDownloadedUrls(intent.getStringArrayExtra("downloaded_report"));
        downAppInfo.setInstallUrls(intent.getStringArrayExtra("install_report"));
        notifyPath.put(notifyId, downAppInfo);
    }

    class DownloadRunnable implements Runnable {
        Message message = DownloadService.this.updateHandler.obtainMessage();
        private int notifyId = 0;
        private Bundle bundle = null;

        DownloadRunnable(int notifyId) {
            this.notifyId = notifyId;
            bundle = new Bundle();
            bundle.putInt("notifyId", notifyId);
        }

        public void run() {
            try {
                this.message.what = DOWNLOADSUCCESSED_STATUS;
                // 无网络连接
                if (!AdViewUtils.isConnectInternet(DownloadService.this)) {
                    this.message.what = DOWNLOADFAILED_STATUS;
                    bundle.putString(FAILED, FAILEDDOWNLOAD6);
                    this.message.setData(bundle);
                    DownloadService.this.updateHandler
                            .sendMessage(this.message);
                    return;
                }
                if (null != notifyPath && null != notifyPath.get(notifyId))
                    if (TextUtils.isEmpty(notifyPath.get(notifyId).getDownUrl()))
                        return;
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(DownloadService.this);
                Intent intent = new Intent(ConstantValues.DL_DOWNLOADING_STATUS);
                lbm.sendBroadcast(intent);

                long downloadSize = DownloadService.this.downloadFile(this.notifyId);
                if (downloadSize > 0L) {
                    bundle.putString(PATH, notifyPath.get(notifyId).getPathName().getAbsolutePath());
                    this.message.setData(bundle);
                    DownloadService.this.updateHandler
                            .sendMessage(this.message);
                } else if (downloadSize == -1) {
                } else {
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.message.what = DOWNLOADFAILED_STATUS;
                bundle.putString(FAILED, FAILEDDOWNLOAD7);
                this.message.setData(bundle);
                DownloadService.this.updateHandler.sendMessage(this.message);
                return;
            }
        }
    }
}
