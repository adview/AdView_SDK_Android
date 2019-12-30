package com.kuaiyou.mraid.interfaces;

import android.webkit.WebResourceResponse;

import com.kuaiyou.mraid.MRAIDView;

public interface MRAIDViewListener {

	/******************************************************************************
	 * A listener for basic MRAIDView banner ad functionality.
	 ******************************************************************************/

	void mraidViewLoaded(MRAIDView mraidView);

	void mraidViewExpand(MRAIDView mraidView);

	void mraidViewClose(MRAIDView mraidView);

	boolean mraidViewResize(MRAIDView mraidView, int width, int height,
								   int offsetX, int offsetY);

	void onShouldOverride(String data);

	WebResourceResponse onShouldIntercept(String data);

	void loadDataError(int errorType);

	void mraidViewOMJSInjected( MRAIDView mraidView ); //OMSDK v1.2 used
//	public void mraidViewVideoPlayStarted(MRAIDView mraidView);
//
//	public void mraidViewVideoPlayFinished(MRAIDView mraidView);

}
