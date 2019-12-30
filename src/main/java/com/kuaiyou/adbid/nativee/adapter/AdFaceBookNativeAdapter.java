package com.kuaiyou.adbid.nativee.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.AdVG.ad.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.MediaView;
import com.facebook.ads.MediaViewListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.kuaiyou.adbid.AdAdapterManager;
import com.kuaiyou.utils.AdViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdFaceBookNativeAdapter extends AdAdapterManager {
    private Context mContext;
    protected static final String TAG = AdFaceBookNativeAdapter.class.getSimpleName();
    private NativeAd nativeAd;

    private @Nullable    NativeAdLayout nativeAdLayout;
    private @Nullable    AdOptionsView adOptionsView;
    private MediaView nativeAdMedia;

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    protected void initAdapter(Context context) {
        AdViewUtils.logInfo("initAdapter AdFaceBookNativeAdapter");
        this.mContext = context;
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(mContext);
    }
    @Override
    public void handleAd(Context context, Bundle bundle) {
        String key1, key2;
        key1 = bundle.getString("appId");
        key2 = bundle.getString("posId");
//            AdView.setAppSid(context, key1);
        loadNativeAd(key2);
        //refreshAd(key1, key2);
    }

    private void loadNativeAd(String posid) {
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).



        nativeAd = new NativeAd(mContext, posid);

        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                if (nativeAd == null || nativeAd != ad) {
                    // Race condition, load() called again before last ad was displayed
                    return;
                }

                //nativeAdLayout = view.findViewById(R.id.native_ad_unit);
                //取得facebook内置的layout格式
                nativeAdLayout = (NativeAdLayout)((Activity)mContext).getLayoutInflater().inflate(R.layout.native_ad_unit, null);
                inflateAd(nativeAd, nativeAdLayout);
                // Registering a touch listener to log which ad component receives the touch event.
                // We always return false from onTouch so that we don't swallow the touch event (which
                // would prevent click events from reaching the NativeAd control).
                // The touch listener could be used to do animations.
                nativeAd.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                            int i = view.getId();
//                            if (i == R.id.native_ad_call_to_action) {
//                                Log.d(TAG, "Call to action button clicked");
//                            } else if (i == R.id.native_ad_media) {
//                                Log.d(TAG, "Main image clicked");
//                            }
//                            else
                            {
                                Log.d(TAG, "Other ad component clicked");
                            }
                        }
                        return false;
                    }
                });
                // Unregister last ad
                nativeAd.unregisterView();

//                if (adChoicesContainer != null) {
//                    adOptionsView = new AdOptionsView((Activity)mContext, nativeAd, nativeAdLayout);
//                    adChoicesContainer.removeAllViews();
//                    adChoicesContainer.addView(adOptionsView, 0);
//                }

                onAdReturned(toNativeInfoMap());

//                nativeAd.registerViewForInteraction(
//                        nativeAdLayout,
//                        nativeAdMedia,
//                        nativeAdIcon,
//                        clickableViews);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });

        // Request an ad
        nativeAd.loadAd();
    }

    private void inflateAd(NativeAd nativeAd, View adView) {
        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        nativeAdMedia.setListener(getMediaViewListener());

        // Setting the Text
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        sponsoredLabel.setText(R.string.sponsored);

        // You can use the following to specify the clickable areas.
        List<View> clickableViews = new ArrayList<View>();
        clickableViews.add(nativeAdIcon);
        clickableViews.add(nativeAdMedia);
        clickableViews.add(nativeAdCallToAction);
        nativeAd.registerViewForInteraction(
                nativeAdLayout,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);

        // Optional: tag views
        NativeAdBase.NativeComponentTag.tagView(nativeAdIcon, NativeAdBase.NativeComponentTag.AD_ICON);
        NativeAdBase.NativeComponentTag.tagView(nativeAdTitle, NativeAdBase.NativeComponentTag.AD_TITLE);
        NativeAdBase.NativeComponentTag.tagView(nativeAdBody, NativeAdBase.NativeComponentTag.AD_BODY);
        NativeAdBase.NativeComponentTag.tagView(nativeAdSocialContext, NativeAdBase.NativeComponentTag.AD_SOCIAL_CONTEXT);
        NativeAdBase.NativeComponentTag.tagView(nativeAdCallToAction, NativeAdBase.NativeComponentTag.AD_CALL_TO_ACTION);
    }

    private static MediaViewListener getMediaViewListener() {
        return new MediaViewListener() {
            @Override
            public void onVolumeChange(MediaView mediaView, float volume) {
                Log.i(TAG, "MediaViewEvent: Volume " + volume);
            }

            @Override
            public void onPause(MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: Paused");
            }

            @Override
            public void onPlay(MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: Play");
            }

            @Override
            public void onFullscreenBackground(MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: FullscreenBackground");
            }

            @Override
            public void onFullscreenForeground(MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: FullscreenForeground");
            }

            @Override
            public void onExitFullscreen(MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: ExitFullscreen");
            }

            @Override
            public void onEnterFullscreen(MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: EnterFullscreen");
            }

            @Override
            public void onComplete(MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: Completed");
            }
        };
    }

    @Override
    public void reportClick(Object... o) {
        try {
            super.reportClick(o);
            int count = Integer.valueOf(o[1] + "");
            //adData.get(count).handleClick((View) o[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportImpression(Object... o) {
        try {
            super.reportImpression(o);
            int count = Integer.valueOf(o[1] + "");
            //adData.get(count).recordImpression((View) o[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<HashMap<String, Object>> toNativeInfoMap() {
        ArrayList<HashMap<String, Object>> nativeAdInfos = new ArrayList<HashMap<String, Object>>();
        try {
//            for (int i = 0; i < list.size(); i++) {
//                NativeADDataRef adItem = list.get(i);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("title", nativeAd.getAdHeadline());
                item.put("iconUrl", nativeAd.getAdIcon());
                item.put("description", nativeAd.getAdBodyText());
                item.put("imageUrl", nativeAd.getAdCoverImage());
                item.put("facebook_adview",nativeAdLayout); //将facebook的native item的layout传给app
//                if (null != adItem.getImgList())
//                    item.put("imageList", adItem.getImgList());
                //item.put("adItem", adItem);
//                nativeAdInfos.add(item);
//                adData.add(adItem);
//            }
            return nativeAdInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void showNativeAdWithDelay() {
        /**
         * Here is an example for displaying the ad with delay;
         * Please do not copy the Handler into your project
         */
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Check if nativeAd has been loaded successfully
                if(nativeAd == null || !nativeAd.isAdLoaded()) {
                    return;
                }
                // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
                if(nativeAd.isAdInvalidated()) {
                    return;
                }
                //inflateAd(nativeAd); // Inflate Native Ad into Container same as previous code example
            }
        }, 1000 * 60 * 15); // Show the ad after 15 minutes
    }
}
