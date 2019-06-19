package com.kuaiyou.interfaces;

public interface KyInstalListener extends KyViewListener {

    int getDisplayMode();

    void onVisiblityChange(int visible);
}
