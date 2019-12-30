//
//  VastPlayer.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.video.vast;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.DownloadRunnable;
import com.kuaiyou.interfaces.DownloadStatusInterface;
import com.kuaiyou.utils.SharedPreferencesUtils;
import com.kuaiyou.utils.DefaultMediaPicker;
import com.kuaiyou.video.vast.model.VASTModel;
import com.kuaiyou.video.vast.processor.VASTMediaPicker;
import com.kuaiyou.video.vast.processor.VASTProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VASTPlayer {

    //wilder 2019 test function
    private boolean selfTestMode_VastPlayer = false; //test vast format PDU

    // errors that can be returned in the vastError callback method of the
    // VASTPlayerListener
    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_NETWORK = 1;
    public static final int ERROR_XML_OPEN_OR_READ = 2;
    public static final int ERROR_XML_PARSE = 3;
    public static final int ERROR_SCHEMA_VALIDATION = 4; // not used in SDK, only in sourcekit
    public static final int ERROR_POST_VALIDATION = 5;
    public static final int ERROR_EXCEEDED_WRAPPER_LIMIT = 6;
    public static final int ERROR_VIDEO_PLAYBACK = 7;

    public static ExecutorService executorService;

    private Context context;
    private VASTPlayerListener vastPlayerListener;   //wilder 2019

    private ArrayList<VASTModel> vastModel = new ArrayList<VASTModel>();
    private ArrayList<VASTModel> wrapperModel = new ArrayList<VASTModel>();
    private Bundle bundle;

    public VASTPlayer(Context context, Bundle bundle, VASTPlayerListener callback) {
        this.context = context;
        this.bundle = bundle;
        this.vastPlayerListener = callback;
    }

    public ArrayList<VASTModel> getVastModel() {
        return vastModel;
    }

    public ArrayList<VASTModel> getWrapperModel() {
        return wrapperModel;
    }

    public void loadVideoWithUrl(final String urlString) {
        AdViewUtils.logInfo("loadVideoWithUrl " + urlString);
        if (AdViewUtils.isConnectInternet(context)) {
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader in = null;
                    StringBuffer sb;
                    try {
                        URL url = new URL(urlString);
                        in = new BufferedReader(new InputStreamReader(url.openStream()));
                        sb = new StringBuffer();
                        String line;
                        while ((line = in.readLine()) != null) {
                            sb.append(line).append(System.getProperty("line.separator"));
                        }
                    } catch (Exception e) {
                        sendError(ERROR_XML_OPEN_OR_READ);
                        e.printStackTrace();
                        return;
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    loadVideoWithData(sb.toString());
                }
            })).start();
        } else {
            sendError(ERROR_NO_NETWORK);
        }
    }

    public boolean isPlayOnline() {
        return AdViewUtils.playOnLine;
    }

    public void loadVideoWithData(final String xmlData) {
        AdViewUtils.logInfo("====== VastPlayer:: loadVideoWithData  ======"/* + xmlData*/);
        if (AdViewUtils.isConnectInternet(context)) {
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    VASTMediaPicker mediaPicker = new DefaultMediaPicker(context);
                    VASTProcessor processor = new VASTProcessor(mediaPicker);
                    //wilder 2019 for test
                    try {
                        int error;
                        if (selfTestMode_VastPlayer) {
                            //String testDat = test_loadVASTXML("/assets/test/VAST-test1.xml");//VAST-test1.xml,VPAID-video-sample-1.xml
                            //vpaid-xfinity-0001.xml, [omsdk]vast-video-00001.txt, [omsdk]VPAID-innovid-tv-https.xml (要vpn)
                            //[omsdk]VPAID-video-adview-sample-001.xml
                            //String testDat = AdViewUtils.loadAssetsFileByContext("test/[omsdk]VPAID-video-adview-sample-001.xml",context );
                            String testDat = AdViewUtils.loadAssetsFile("test/VAST-0830-vast_tag.xml"); //[omsdk-ok]omsdk-videoData.xml
                            error = processor.process(testDat);
                        } else {
                            //end wilder 2019
                            error = processor.process(xmlData);
                        }

                        if (error == ERROR_NONE) {
                            vastModel = processor.getModel();
                            wrapperModel = processor.getWrapperModel();
                            sendParseReady(); //(wilder 2019)send broadcast VASTPlayer.ACTION_VASTPARSEDONE -> AdBIDVideoAdaptor,and start download
                        } else {
                            sendError(error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            })).start();
        } else {
            sendError(ERROR_NO_NETWORK);
        }
    }

    public class FirstVideoDownloadInterface implements DownloadStatusInterface {

        @Override
        public void onDownloadFinished(int pos, int creativePos, String path) {

            AdViewUtils.logInfo("++++ FirstVideoDownloadInterface() : onDownloadFinished: " + path + ";" + "creativePos: " + creativePos + "++++++");
            vastModel.get(pos).getCreativeList().get(creativePos).setPickedVideoUrl(path);
            vastModel.get(pos).getCreativeList().get(creativePos).setReady(true);
            sendDownloadReady();

        }

        @Override
        public void onDownloadFailed(int pos, int creativePos, int error) {
            vastModel.get(pos).getCreativeList().get(creativePos).setReady(false);
            vastModel.get(pos).getCreativeList().get(creativePos).setFailed(true);
            int tmpPos = pos;
            int tmpCreativePos = creativePos;
            try {
                if (creativePos + 1 < vastModel.get(pos).getCreativeList().size()) {
                    tmpCreativePos = creativePos + 1;
                } else {
                    tmpPos = pos + 1;
                    tmpCreativePos = 0;
                }
                if (tmpPos < vastModel.size()) {
                    String pickedUrl = vastModel.get(tmpPos).getCreativeList().get(tmpCreativePos).getPickedVideoUrl();
                    String pickType = vastModel.get(tmpPos).getCreativeList().get(tmpCreativePos).getPickedVideoType();
                    executorService.execute(new DownloadRunnable(context, pickedUrl,
                                        pickType.matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX),
                            true, tmpPos, tmpCreativePos, this));
                } else
                    sendError(error);
            } catch (Exception e) {
                sendError(error);
                e.printStackTrace();
            }
        }

        @Override
        public void onShouldPlayOnline(int pos, int creativePos) {
            vastModel.get(pos).getCreativeList().get(creativePos).setReady(true);
            sendDownloadReady();
        }

        @Override
        public void downloadCanceled(int pos, int creativePos) {
            sendDownloadCanceled();
        }

        @Override
        public int getDownloadStatus(String url, String name, long size) {
            return VASTPlayer.getDownloadStatus(context, url, name, size);
        }

        @Override
        public boolean checkCacheSize(long size) {
            return checkFileSize(context, size);
        }

        @Override
        public boolean getDownloadPath(String url, String name) {
            File downloadPath = new File(ConstantValues.DOWNLOAD_VIDEO_PATH);
            if (!downloadPath.exists())
                return downloadPath.mkdirs();
            return true;
        }
    }

    public void downloadMedia(Context context, int adCount, int creativeCount, DownloadStatusInterface downloadStatusInterface) {
        boolean isHtmlorJS = false;
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(1);
        }
        if (!TextUtils.isEmpty(vastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl())) {
//            vastModel.get(0).getCreativeList().get(0).setPickedVideoUrl("http://i.l.i2nmobicdn.cn/adtools/videoads/prod/1701d05de0294e57be0bb1e009e34566/videos/54c5473f-9ce1-4720-8de5-a83feae2e0fa/video.720p.mp4");
            String videoUrl = vastModel.get(adCount).getCreativeList().get(creativeCount).getPickedVideoUrl();
            //wilder 2019 changes for vpaid's jscript
            //String vpaidURL = vastModel.get(0).getCreativeList().get(0).getVPAIDurl();
            if (isPlayOnline()) {
                isHtmlorJS = true; //wilder 2019 for test
            }else {
                isHtmlorJS = vastModel.get(adCount).getCreativeList().get(creativeCount).
                        getPickedVideoType().matches(DefaultMediaPicker.SUPPORTED_HTML_TYPE_REGEX)
                        || vastModel.get(adCount).getCreativeList().get(creativeCount).
                        getPickedVideoType().matches(DefaultMediaPicker.SUPPORTED_JAVASCRIPT_TYPE_REGEX);
            }

            executorService.execute(new DownloadRunnable(context,
                                    videoUrl,
                                    isHtmlorJS,     //(wilder 2019) if is html , it will not trigger download process
                                    true,
                                    adCount, creativeCount,
                                    downloadStatusInterface));
        }
    }

    public static int getDownloadStatus(Context context, String url, String name, long size) {
        try {
            String time[] = null;
            File filePath = new File(ConstantValues.DOWNLOAD_VIDEO_PATH + name);
            String valueStr = (String) SharedPreferencesUtils.getSharedPreferenceValue(context,
                    ConstantValues.SP_VIDEO_NAME_FILE, name);
            if (null == valueStr)
                return DownloadRunnable.DOWNLOAD_STATUS_READY;
            time = valueStr.split("_");
            if (filePath.exists() && filePath.length() == size) {
                if (null != time && !TextUtils.isEmpty(time[0]) && System.currentTimeMillis() - Long.valueOf(time[0]) > 30 * 60 * 1000) {
                    filePath.delete();
                    SharedPreferencesUtils.deleteSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, url);
                    return DownloadRunnable.DOWNLOAD_STATUS_READY;
                }
                return DownloadRunnable.DOWNLOAD_STATUS_EXIST;
            } else if (filePath.exists() && filePath.length() != size) {
                if (null != time && !TextUtils.isEmpty(time[2]) && Integer.valueOf(time[2]) == DownloadRunnable.DOWNLOAD_STATUS_PROGRESS)
                    return DownloadRunnable.DOWNLOAD_STATUS_PROGRESS;
                else {
                    if (filePath.delete()) {
                        SharedPreferencesUtils.deleteSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, url);
                        SharedPreferencesUtils.minusSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, "total_size", size);
                        return DownloadRunnable.DOWNLOAD_STATUS_READY;
                    } else
                        return DownloadRunnable.DOWNLOAD_STATUS_ERROR;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DownloadRunnable.DOWNLOAD_STATUS_ERROR;
        }
        return DownloadRunnable.DOWNLOAD_STATUS_READY;
    }

    public static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }

    public static boolean checkFileSize(Context context, long size) {
        long totalSize;
        int count = 0;
        File cachePath = new File(ConstantValues.DOWNLOAD_VIDEO_PATH);
        try {
            totalSize = getFileSizes(cachePath);
            AdViewUtils.logInfo("totalSize=" + totalSize);
            if (size >= ConstantValues.VIDEO_CACHE_SIZE) {
                AdViewUtils.logInfo("video size too large =" + size);
                return false;
            }
            while (totalSize + size > ConstantValues.VIDEO_CACHE_SIZE) {
                if (count > 50)
                    break;
                File[] cacheFiles = cachePath.listFiles();
                if (null == cacheFiles || cacheFiles.length == 0)
                    break;
                File tempFiles;
                for (int i = 0; i < cacheFiles.length; i++) {
                    for (int j = i + 1; j < cacheFiles.length; j++) {
                        if (cacheFiles[i].lastModified() > cacheFiles[j].lastModified()) {
                            tempFiles = cacheFiles[i];
                            cacheFiles[i] = cacheFiles[j];
                            cacheFiles[j] = tempFiles;
                        }
                    }
                }
                try {
                    int pos = 0;
                    String currentName = cacheFiles[pos].getAbsolutePath();
                    boolean isDeleted = cacheFiles[pos].delete();
                    AdViewUtils.logInfo("delete file:" + currentName + " :" + isDeleted);
                    while (!isDeleted) {
                        AdViewUtils.logInfo("retry delete file:" + cacheFiles[pos] + " :" + isDeleted);
                        if (cacheFiles.length > pos)
                            isDeleted = cacheFiles[++pos].delete();
                        else break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
                totalSize = getFileSizes(cachePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        if (count > 50)
            return false;
        return true;
    }

    public void downloadVideo(int adCount, int creativeCount) {
        if (vastModel != null) {
            if (AdViewUtils.isConnectInternet(context)) {
                downloadMedia(context, adCount, creativeCount, new FirstVideoDownloadInterface());
            } else {
                sendError(ERROR_NO_NETWORK);
            }
        } else {
            AdViewUtils.logInfo("vastModel is null; nothing to download");
        }
    }


    public void sendPlayReady(Context context) {
        AdViewUtils.logInfo("play");
        if (vastModel != null) {
            if (AdViewUtils.isConnectInternet(context)) {
                //add for embed vast view
                //sendLocalBroadcast(context, ACTION_DOWNLOADCANCEL, null);
                if (vastPlayerListener != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vastPlayerListener.vastPlayReady(bundle);
                        }
                    });
                }
            } else {
                sendError(ERROR_NO_NETWORK);
            }
        } else {
            AdViewUtils.logInfo("vastModel is null; nothing to play");
        }
    }

    private void sendDownloadCanceled() {
        AdViewUtils.logInfo("sendDownloadCanceled");
        if (vastPlayerListener != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastPlayerListener.vastDownloadCancel();
                }
            });
        }
    }

    private VASTPlayer getPlayer() {
        return this;
    }

    private void sendParseReady() {
        AdViewUtils.logInfo("sendReady");
        //sendLocalBroadcast(context, ACTION_VASTPARSEDONE, null);
        if (vastPlayerListener != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
               public void run() {
                    vastPlayerListener.vastParseDone(getPlayer());
                }
            });
        }

    }

    private void sendDownloadReady() {
        AdViewUtils.logInfo("sendDownloadReady");
        //sendLocalBroadcast(context, ACTION_VASTDOWNLOADREADY, null);
        if (vastPlayerListener != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastPlayerListener.vastDownloadReady();
                }
            });
        }
    }

    private void sendError(final int error) {
        AdViewUtils.logInfo("{{{{{{{{{{{{{{{{ sendError:" + error + "}}}}}}}}}}}}}}}}");
        Bundle bundle = new Bundle();
        bundle.putInt("error", error);
        //sendLocalBroadcast(context, ACTION_VASTERROR, bundle);
        if (vastPlayerListener != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastPlayerListener.vastError(error);
                }
            });
        }
    }

    public void sendClick() {
        if (vastPlayerListener != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastPlayerListener.vastClick();
                }
            });
        }
    }

    public void sendComplete() {
        if (vastPlayerListener != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastPlayerListener.vastComplete();
                }
            });
        }
    }

    public void sendDismiss() {
        if (vastPlayerListener != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastPlayerListener.vastDismiss();
                }
            });
        }
    }

}
 