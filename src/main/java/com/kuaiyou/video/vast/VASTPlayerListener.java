package com.kuaiyou.video.vast;

import android.os.Bundle;

public interface VASTPlayerListener {

    void vastDownloadReady();

    void vastDownloadCancel();

    void vastParseDone(VASTPlayer mp);

    void vastError(int error);

    void vastClick();

    void vastComplete();

    void vastDismiss();

    int vastOrientationChange();

    boolean vastAutoCloseEnableChange();

    int getCachePriod();

    void vastPlayReady(Bundle bundle);
}
