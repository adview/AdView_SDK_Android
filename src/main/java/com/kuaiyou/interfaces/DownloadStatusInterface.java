package com.kuaiyou.interfaces;

/**
 * Created by Administrator on 2017/2/13.
 */
public interface DownloadStatusInterface {
    void onDownloadFinished(int pos, int creativePos, String path);

    void onDownloadFailed(int pos, int creativePos, int error);

    void onShouldPlayOnline(int pos, int creativePos);

    void downloadCanceled(int pos, int creativePos);

    int getDownloadStatus(String url, String name, long size);

    boolean checkCacheSize(long size);

    boolean getDownloadPath(String url, String name);

}
