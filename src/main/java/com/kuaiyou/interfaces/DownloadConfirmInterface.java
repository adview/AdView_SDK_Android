package com.kuaiyou.interfaces;

import java.io.IOException;

/**
 * Created by Administrator on 2016/11/11.
 */
public interface DownloadConfirmInterface {
    void confirmDownload();
    void cancelDownload();
    void error();
}
