package com.kuaiyou.interfaces;

import android.graphics.Bitmap;

public interface KyInstalListener extends AdVGListener {

    int getDisplayMode();

    void onVisiblityChange(int visible);


}
