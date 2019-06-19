package com.kuaiyou.loader.loaderInterface;

/**
 * Created by Administrator on 2017/7/25.
 */

public interface InitSDKInterface {
    void initSuccessed();

    void initFailed(String msg);

    void updateStatus(int status);

}
