package com.kuaiyou.mraid.interfaces;

import android.webkit.WebResourceResponse;

/******************************************************************************
 * A listener for MRAIDView/MRAIDInterstitial to listen for notifications when
 * the following native features are requested from a creative:
 * 
 * * make a phone call * add a calendar entry * play a video (external) * open a
 * web page in a browser * store a picture * send an SMS
 * 
 * If you don't implement this interface, the default for supporting these
 * features in the creative will be false.
 ******************************************************************************/

public interface MRAIDNativeFeatureListener {

	void mraidNativeFeatureDownload(String url);

	void mraidNativeFeatureCallTel(String url);

	void mraidNativeFeatureCreateCalendarEvent(String eventJSON);

	void mraidNativeFeatureOpenDeeplink(String url);

	void mraidNativeFeatureOpenBrowser(String url);

	void mraidNativeFeatureStorePicture(String url);

	void mraidNativeFeatureSendSms(String url);

}
