package com.kuaiyou.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.kuaiyou.interfaces.DownloadConfirmInterface;
import com.kuaiyou.interfaces.DownloadStatusInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2017/2/13.
 */
public class DownloadRunnable implements Runnable {
    private int pos, creativePos;
    private boolean isHtml;
    private boolean needConfirm;
    private String url;
    private Context context;
    private DownloadStatusInterface downloadStatusInterface;
    private CustomHandler customHandler;

    public final static int ERROR_URLEXCEPTION = 1;
    public final static int ERROR_NETWORKEXCEPTION = 2;
    public final static int ERROR_STROGEEXCEPTION = 3;
    public final static int ERROR_WORKINPROGRESSEXCEPTION = 4;

    public final static int DOWNLOAD_STATUS_ERROR = -1;
    public final static int DOWNLOAD_STATUS_READY = 0;
    public final static int DOWNLOAD_STATUS_EXIST = 1;
    public final static int DOWNLOAD_STATUS_PROGRESS = 2;
    public final static int DOWNLOAD_STATUS_DONE = 3;

    public DownloadRunnable(Context context, String url, boolean isHtml, boolean needConfirm, int pos, DownloadStatusInterface dInterface) {
        this.pos = pos;
        this.url = url;
        this.context = context;
        this.needConfirm = needConfirm;
        this.isHtml = isHtml;
        this.downloadStatusInterface = dInterface;
        this.customHandler = new CustomHandler();
    }

    public DownloadRunnable(Context context, String url, boolean isHtml, boolean needConfirm, int adPos, int creativePos, DownloadStatusInterface dInterface) {
        this.pos = adPos;
        this.creativePos = creativePos;
        this.url = url;
        this.needConfirm = needConfirm;
        this.isHtml = isHtml;
        this.context = context;
        this.downloadStatusInterface = dInterface;
        this.customHandler = new CustomHandler();
    }

    @Override
    public void run() {

        preCheck();
    }

    private void preCheck() {
        URL downloadUrl;
        HttpURLConnection conn = null;
        boolean isFirst = true;
        String tempUrl = null;
        String fileName;
        boolean pathCreated = false;
//        long totalSize = 0L;
        final String downloadPath = ConstantValues.DOWNLOAD_VIDEO_PATH;
        long updateTotalSize = 0;
        int status = 0;
        try {
            Message msg = new Message();
            if (needConfirm) {
                if (!AdViewUtils.getNetworkType(context).equals("WIFI")) {
                    msg.what = 1;
                    msg.arg1 = pos;
                    msg.arg2 = creativePos;
                    msg.obj = putExtra(conn, downloadPath, url, updateTotalSize);
                    customHandler.sendMessage(msg);
                    return;
                }
                if (isHtml) {
                    if (null != downloadStatusInterface)
                        downloadStatusInterface.onDownloadFinished(pos, creativePos, url);
                    return;
                }
            } else if (isHtml) {
                if (null != downloadStatusInterface)
                    downloadStatusInterface.onDownloadFinished(pos, creativePos, url);
                return;
            }
            int responseCode = 0;
            downloadUrl = new URL(url);
            while (isFirst || 302 == responseCode) {
                isFirst = false;
                if (downloadUrl.getProtocol().startsWith("https"))
                    conn = (HttpsURLConnection) downloadUrl
                            .openConnection();
                else
                    conn = (HttpURLConnection) downloadUrl
                            .openConnection();
                conn.setConnectTimeout(10 * 1000);
                conn.setReadTimeout(10 * 1000);

                responseCode = conn.getResponseCode();
                tempUrl = conn.getHeaderField("Location");
                if (!TextUtils.isEmpty(tempUrl))
                    downloadUrl = new URL(tempUrl);

            }

            if (responseCode >= 400) {
                if (null != downloadStatusInterface)
                    downloadStatusInterface.onDownloadFailed(pos, creativePos, ERROR_URLEXCEPTION);
                return;
            }

            updateTotalSize = conn.getContentLength();
            URL absUrl = conn.getURL();// 获得真实Url
            fileName = absUrl.getPath();
            if (fileName.contains("/")) {
                fileName = fileName.substring((fileName.lastIndexOf("/") + 1), fileName.length());
            }
            fileName = fileName.hashCode() + "";
            //检测存储是否可用并且创建下载路径
            if (null != downloadStatusInterface)
                pathCreated = downloadStatusInterface.getDownloadPath(url, fileName);

            if (!pathCreated) {
                if (null != downloadStatusInterface)
                    downloadStatusInterface.onDownloadFailed(pos, creativePos, ERROR_STROGEEXCEPTION);
                return;
            }

            //获取下载许可状态：
            //1.是否已下载
            //2.是否在下载中
            if (null != downloadStatusInterface)
                status = downloadStatusInterface.getDownloadStatus(url, fileName, updateTotalSize);

            if (status == DOWNLOAD_STATUS_PROGRESS) {
                AdViewUtils.logInfo("status=DOWNLOAD_STATUS_PROGRESS");
                if (null != downloadStatusInterface)
                    downloadStatusInterface.onDownloadFailed(pos, creativePos, ERROR_WORKINPROGRESSEXCEPTION);
                return;
            } else if (status == DOWNLOAD_STATUS_EXIST) {
                AdViewUtils.logInfo("status=DOWNLOAD_STATUS_EXIST");
                if (null != downloadStatusInterface)
                    downloadStatusInterface.onDownloadFinished(pos, creativePos, downloadPath + fileName);
                return;
            } else if (status == DOWNLOAD_STATUS_ERROR) {
                AdViewUtils.logInfo("status=DOWNLOAD_STATUS_ERROR");
                if (null != downloadStatusInterface)
                    downloadStatusInterface.onDownloadFailed(pos, creativePos, ERROR_STROGEEXCEPTION);
                return;
            }

            if (null != downloadStatusInterface)
                if (!downloadStatusInterface.checkCacheSize(updateTotalSize)) {
                    AdViewUtils.logInfo("status=retry too times or del failed");
                    if (null != downloadStatusInterface)
                        downloadStatusInterface.onDownloadFailed(pos, creativePos, ERROR_STROGEEXCEPTION);
                    return;
                }
            // TODO: 2017/2/17  check network
            if (needConfirm) {
                if (!AdViewUtils.getNetworkType(context).equals("WIFI")) {
                    msg.what = 1;
                    msg.arg1 = pos;
                    msg.arg2 = creativePos;
                    msg.obj = putExtra(conn, downloadPath, fileName, updateTotalSize);
                    customHandler.sendMessage(msg);
//                AdViewUtils.trafficConfirmDialog(context, new CustomDownloadConfirmInterface(conn, is, fos, downloadPath, fileName, updateTotalSize));
                } else {
                    msg.what = 2;
                    msg.obj = putExtra(conn, downloadPath, fileName, updateTotalSize);
                    customHandler.sendMessage(msg);
                }
            } else {
                msg.what = 2;
                msg.obj = putExtra(conn, downloadPath, fileName, updateTotalSize);
                customHandler.sendMessage(msg);
            }
//                download(conn, is, fos, downloadPath, fileName, updateTotalSize);

        } catch (Exception e) {
            e.printStackTrace();
//            hasException = true;
            if (null != downloadStatusInterface)
                downloadStatusInterface.onDownloadFailed(pos, creativePos, ERROR_NETWORKEXCEPTION);
        }

    }

    private ThreadBean putExtra(HttpURLConnection conn, String downloadPath, String fileName, long updateTotalSize) {
        ThreadBean bean = new ThreadBean();
        bean.setConn(conn);
        bean.setDownloadPath(downloadPath);
        bean.setFileName(fileName);
//        bean.setFos(fos);
//        bean.setIs(is);
        bean.setUpdateTotalSize(updateTotalSize);
        return bean;

    }

    private void download(ThreadBean bean) {
        AdViewUtils.logInfo("download start");
        InputStream is = null;
        FileOutputStream fos = null;
        boolean hasException = false;
        HttpURLConnection conn = bean.getConn();
        String downloadPath = bean.getDownloadPath();
        String fileName = bean.getFileName();
        long updateTotalSize = bean.getUpdateTotalSize();
        try {
            // 以当前时间为value，保存该地址的下载信息
            SharedPreferencesUtils.commitSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, fileName, System.currentTimeMillis() + "_" + updateTotalSize + "_" + DOWNLOAD_STATUS_PROGRESS);
            is = conn.getInputStream();
            fos = new FileOutputStream(downloadPath + fileName, false);
            byte[] buffer = new byte[4096];
            int readsize = 0;
            int totalSize = 0;
            long time = System.currentTimeMillis();
            while ((readsize = is.read(buffer)) > 0) {
                fos.write(buffer, 0, readsize);
                totalSize += readsize;
                if (System.currentTimeMillis() - time > 2 * 1000) {
                    AdViewUtils.logInfo("download =" + ((float) totalSize * 100 / updateTotalSize) + "%");
                    time = System.currentTimeMillis();
                }
            }
            if (null != downloadStatusInterface)
                downloadStatusInterface.onDownloadFinished(pos, creativePos, downloadPath + fileName);
            SharedPreferencesUtils.commitSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, fileName, System.currentTimeMillis() + "_" + updateTotalSize + "_" + DOWNLOAD_STATUS_DONE);
            SharedPreferencesUtils.addSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, "total_size", updateTotalSize);
        } catch (Exception e) {
            e.printStackTrace();
            hasException = true;
            if (null != downloadStatusInterface)
                downloadStatusInterface.onDownloadFailed(pos, creativePos, ERROR_NETWORKEXCEPTION);
        } finally {
            try {
                if (fos != null) {
                    fos.flush(); // 把缓冲区的数据强行输出,(注意不要和frush()刷新混淆了)
                }
                if (conn != null) {
                    conn.disconnect();
                }
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (hasException) {
                    try {
                        if (TextUtils.isEmpty(fileName))
                            return;
                        File failedFile = new File(downloadPath + fileName);
                        if (null != failedFile && failedFile.exists())
                            if (failedFile.delete()) {
                                SharedPreferencesUtils.deleteSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, fileName);
                                SharedPreferencesUtils.minusSharedPreferencesValue(context, ConstantValues.SP_VIDEO_NAME_FILE, "total_size", updateTotalSize);
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class CustomHandler extends Handler {

        public CustomHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            final Message copy = Message.obtain();
            copy.copyFrom(msg);
            switch (copy.what) {
                case 1:
                    AdViewUtils.trafficConfirmDialog(context, new DownloadConfirmInterface() {
                        @Override
                        public void confirmDownload() {
                            copy.what = 3;
                            customHandler.sendMessage(copy);
                        }

                        @Override
                        public void cancelDownload() {
                            if (null != downloadStatusInterface)
                                downloadStatusInterface.downloadCanceled(copy.arg1, copy.arg2);
                        }

                        @Override
                        public void error() {
                            if (null != downloadStatusInterface)
                                downloadStatusInterface.downloadCanceled(copy.arg1, copy.arg2);
                        }
                    });
                    break;
                case 2:
//                    copy.copyFrom(msg);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            download((ThreadBean) copy.obj);
                        }
                    }).start();
                    break;
                case 3:
                    if (null != downloadStatusInterface)
                        downloadStatusInterface.onShouldPlayOnline(msg.arg1, msg.arg2);
                    break;
            }
        }
    }

    class ThreadBean {
        private HttpURLConnection conn;
        private InputStream is;
        private FileOutputStream fos;
        private String downloadPath;
        private String fileName;
        private long updateTotalSize;

        public HttpURLConnection getConn() {
            return conn;
        }

        public void setConn(HttpURLConnection conn) {
            this.conn = conn;
        }

        public InputStream getIs() {
            return is;
        }

        public void setIs(InputStream is) {
            this.is = is;
        }

        public FileOutputStream getFos() {
            return fos;
        }

        public void setFos(FileOutputStream fos) {
            this.fos = fos;
        }

        public String getDownloadPath() {
            return downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getUpdateTotalSize() {
            return updateTotalSize;
        }

        public void setUpdateTotalSize(long updateTotalSize) {
            this.updateTotalSize = updateTotalSize;
        }
    }
}
