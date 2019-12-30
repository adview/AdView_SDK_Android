package com.kuaiyou.interfaces;

public interface KyInstalListener extends AdVGListener {

    int getDisplayMode();

    void onVisiblityChange(int visible);
}
