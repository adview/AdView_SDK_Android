package com.kuaiyou.mraid.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.kuaiyou.utils.AdViewUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class MRAIDNativeFeatureManager {

    private final static String TAG = "MRAIDNativeFeatureManager";

    private Context context;
    private ArrayList<String> supportedNativeFeatures;
    private final static String[] fullSupportedNativeFeatures = {
            MRAIDNativeFeature.CALENDAR,
            MRAIDNativeFeature.INLINE_VIDEO, MRAIDNativeFeature.SMS,
            MRAIDNativeFeature.STORE_PICTURE, MRAIDNativeFeature.TEL,};

    public MRAIDNativeFeatureManager(Context context) {
        this.context = context;
        this.supportedNativeFeatures = new ArrayList<String>(Arrays.asList(fullSupportedNativeFeatures));
    }

    public boolean isCalendarSupported() {
        boolean retval =
                supportedNativeFeatures.contains(MRAIDNativeFeature.CALENDAR) &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
                        PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.WRITE_CALENDAR);
        AdViewUtils.logInfo("isCalendarSupported " + retval);
        return retval;
    }

    public boolean isInlineVideoSupported() {
        // all Android 2.2+ devices should serve HTML5 video
        boolean retval = supportedNativeFeatures.contains(MRAIDNativeFeature.INLINE_VIDEO);
        AdViewUtils.logInfo("isInlineVideoSupported " + retval);
        return retval;
    }

    public boolean isSmsSupported() {
        boolean retval =
                supportedNativeFeatures.contains(MRAIDNativeFeature.SMS) &&
                        PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.SEND_SMS);
        AdViewUtils.logInfo("isSmsSupported " + retval);
        return retval;
    }

    public boolean isStorePictureSupported() {
        boolean retval = supportedNativeFeatures.contains(MRAIDNativeFeature.STORE_PICTURE);
        AdViewUtils.logInfo("isStorePictureSupported " + retval);
        return retval;
    }

    public boolean isTelSupported() {
        boolean retval =
                supportedNativeFeatures.contains(MRAIDNativeFeature.TEL) &&
                        PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE);
        AdViewUtils.logInfo("isTelSupported " + retval);
        return retval;
    }

    public ArrayList<String> getSupportedNativeFeatures() {
        return supportedNativeFeatures;
    }
}
