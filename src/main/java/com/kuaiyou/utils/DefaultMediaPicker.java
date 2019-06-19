//
//  DefaultMediaPicker.java
//
//  Created by Harsha Herur on 12/4/13.
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.video.vast.model.VASTCompanionAd;
import com.kuaiyou.video.vast.model.VASTCreative;
import com.kuaiyou.video.vast.model.VASTMediaFile;
import com.kuaiyou.video.vast.processor.VASTMediaPicker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DefaultMediaPicker implements VASTMediaPicker {

    private static final int maxPixels = 5000;
    private static final int MAX_VIDEO_BITRATE = 1000;
    // These are the Android supported MIME types, see
    // http://developer.android.com/guide/appendix/media-formats.html#core (as
    // of API 18)
    public final static String SUPPORTED_HTML_TYPE_REGEX = "text/.*(?i)(html)";
    public final static String SUPPORTED_IMAGE_TYPE_REGEX = "image/.*(?i)(\\*|gif|jpeg|jpg|png)";
    public final static String SUPPORTED_VIDEO_TYPE_REGEX = "video/.*(?i)(\\*|mp4|webm)";
    public final static String SUPPORTED_JAVASCRIPT_TYPE_REGEX = "application/.*(?i)(javascript)"; //wilder 2019 for VPAID
    //public final static String SUPPORTED_VIDEO_TYPE_REGEX = "video/.*(?i)(mp4)";//|mp2t|matroska|x-flv)";

    private int deviceWidth;
    private int deviceHeight;
    private int deviceArea;
    private Context context;

    public DefaultMediaPicker(Context context) {
        this.context = context;
        setDeviceWidthHeight();
    }

    public DefaultMediaPicker(int width, int height) {
        setDeviceWidthHeight(width, height);
    }

    @Override
    public VASTCompanionAd pickCompanion(ArrayList<VASTCompanionAd> list) {
        // TODO Auto-generated method stub
        AdViewUtils.logInfo("pickCompanion");
        if (list == null || prefilterCompanions(list) == 0) {
            return null;
        }
        Collections.sort(list, new CompanionComparator());
        VASTCompanionAd companionAd = getBestCompanionMatch(list);
        return companionAd;
    }

    @Override
    // given a list of MediaFiles, select the most appropriate one.
    public VASTMediaFile pickVideo(List<VASTMediaFile> mediaFiles) {
        AdViewUtils.logInfo("pickVideo");
        // make sure that the list of media files contains the correct
        // attributes
        if (mediaFiles == null || prefilterMediaFiles(mediaFiles) == 0) {
            return null;
        }
        Collections.sort(mediaFiles, new AreaComparator());
        VASTMediaFile mediaFile = getBestMediaFileMatch(mediaFiles);
        return mediaFile;
    }

    @Override
    public ArrayList<VASTCreative> pickCreative(ArrayList<VASTCreative> list) {
        AdViewUtils.logInfo("pickCreative1");
        // TODO Auto-generated method stub
        if (list == null || prefilterCreatives(list) == 0) {
            return null;
        }
        AdViewUtils.logInfo("pickCreative1 middle");
        Collections.sort(list, new CreativeComparator());
        AdViewUtils.logInfo("pickCreative1 done");
        return list;
    }

    /*
     * This method filters the list of mediafiles and return the count. Validate
     * that the media file objects contain the required attributes for the
     * Default Media Picker processing.
     *
     * Required attributes: 1. type 2. height 3. width 4. url
     */

    private int prefilterMediaFiles(List<VASTMediaFile> mediaFiles) {

        Iterator<VASTMediaFile> iter = mediaFiles.iterator();

        while (iter.hasNext()) {

            VASTMediaFile mediaFile = iter.next();

            // type attribute
            String type = mediaFile.getType();
            if (TextUtils.isEmpty(type)) {
                AdViewUtils.logInfo("Validator error: mediaFile type empty");
                iter.remove();
                continue;
            }

            // Height attribute
            BigInteger height = mediaFile.getHeight();
            if (null == height)
                mediaFile.setHeight(BigInteger.valueOf(0));
//            if (null == height) {
//                AdViewUtils.logInfo("Validator error: mediaFile height null");
//                iter.remove();
//                continue;
//            } else {
//                int videoHeight = height.intValue();
//                if (!(0 < videoHeight && videoHeight < maxPixels)) {
//                    AdViewUtils.logInfo(
//                            "Validator error: mediaFile height invalid: "
//                                    + videoHeight);
//                    iter.remove();
//                    continue;
//                }
//            }

            // width attribute
            BigInteger width = mediaFile.getWidth();
            if (null == width)
                mediaFile.setWidth(BigInteger.valueOf(0));
//            if (null == width) {
//                AdViewUtils.logInfo("Validator error: mediaFile width null");
//                iter.remove();
//                continue;
//            } else {
//                int videoWidth = width.intValue();
//                if (!(0 < videoWidth && videoWidth < maxPixels)) {
//                    AdViewUtils.logInfo("Validator error: mediaFile width invalid: "
//                            + videoWidth);
//                    iter.remove();
//                    continue;
//                }
//            }

            // mediaFile url
            String url = mediaFile.getValue();
            if (TextUtils.isEmpty(url)) {
                AdViewUtils.logInfo("Validator error: mediaFile url empty");
                iter.remove();
                continue;
            }
        }

        return mediaFiles.size();
    }

    /*
     * This method filters the list of companions and return the count. Validate
     * that the media file objects contain the required attributes for the
     * Default Media Picker processing.
     *
     * Required attributes: 1. height 2. width
     */

    private int prefilterCompanions(List<VASTCompanionAd> companionAds) {

        Iterator<VASTCompanionAd> iter = companionAds.iterator();

        while (iter.hasNext()) {

            VASTCompanionAd companionAd = iter.next();

            // Height attribute
            BigInteger height = companionAd.getHeight();

            if (null == height) {
                AdViewUtils.logInfo("!!!!! Validator error: mediaFile height null!!!!! ");
                iter.remove();
                continue;
            } else {
                int videoHeight = height.intValue();
                if (!(0 < videoHeight && videoHeight < maxPixels)) {
                    AdViewUtils.logInfo(
                            "!!!!! Validator error: mediaFile height invalid: !!!!! "
                                    + videoHeight);
                    iter.remove();
                    continue;
                }
            }

            // width attribute
            BigInteger width = companionAd.getWidth();
            if (null == width) {
                AdViewUtils.logInfo("!!!!! Validator error: mediaFile width null!!!!! ");
                iter.remove();
                continue;
            } else {
                int videoWidth = width.intValue();
                if (!(0 < videoWidth && videoWidth < maxPixels)) {
                    AdViewUtils.logInfo("!!!!! Validator error: mediaFile width invalid: !!!!! "
                            + videoWidth);
                    iter.remove();
                    continue;
                }
            }

            // mediaFile url
            String htmlValue = companionAd.getHtmlValue();
            String htmlUrl = companionAd.getiFrameValue();
            String staticValue = companionAd.getStaticValue();
            if (TextUtils.isEmpty(htmlUrl) && TextUtils.isEmpty(staticValue)
                    && TextUtils.isEmpty(htmlValue)) {
                AdViewUtils.logInfo("!!!!!Validator error: mediaFile url empty!!!!!!");
                iter.remove();
                continue;
            } else if (TextUtils.isEmpty(htmlUrl)
                    && !TextUtils.isEmpty(staticValue)
                    && TextUtils.isEmpty(htmlValue)) {
                // // type attribute
                String type = companionAd.getValueType();
                if (TextUtils.isEmpty(type)) {
                    AdViewUtils.logInfo("!!!!!Validator error: mediaFile type empty!!!!");
                    iter.remove();
                    continue;
                } else {

                }
            }
        }

        return companionAds.size();
    }

    /*
     * This method filters the list of companions and return the count. Validate
     * that the media file objects contain the required attributes for the
     * Default Media Picker processing.
     *
     * Required attributes: 1. height 2. width
     */

    private int prefilterCreatives(List<VASTCreative> creatives) {
        AdViewUtils.logInfo("prefilterCreatives");
        Iterator<VASTCreative> iter = creatives.iterator();
        AdViewUtils.logInfo("prefilterCreatives itors  ");
        while (iter.hasNext()) {

            VASTCreative creative = iter.next();
            AdViewUtils.logInfo("VASTCreative next  " + creative.getMediaFiles());
            if (null == creative.getMediaFiles()) {
                //(wilder 2019) it means if no media in creative, the creative will be lost
                continue;
            }
            int result = prefilterMediaFiles(creative.getMediaFiles());
            if (result > 0) {
                return result;
            }

        }

        return 0;
    }

    private void setDeviceWidthHeight() {

        // get the device width and height of the device using the context
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        deviceWidth = metrics.widthPixels;
        deviceHeight = metrics.heightPixels;
        deviceArea = deviceWidth * deviceHeight;
    }

    private void setDeviceWidthHeight(int width, int height) {

        this.deviceWidth = width;
        this.deviceHeight = height;
        deviceArea = deviceWidth * deviceHeight;

    }

    private class CreativeComparator implements Comparator<VASTCreative> {

        @Override
        public int compare(VASTCreative obj1, VASTCreative obj2) {
            // get area of the video of the two MediaFiles
            int obj1Area = obj1.getSequence();
            int obj2Area = obj2.getSequence();

            // get the difference between the area of the MediaFile and the area
            // of the screen
//            int obj1Diff = Math.abs(obj1Area - deviceArea);
//            int obj2Diff = Math.abs(obj2Area - deviceArea);
//            AdViewUtils.logInfo("AreaComparator: obj1:" + obj1Diff + " obj2:"
//                    + obj2Diff);

            // choose the MediaFile which has the lower difference in area
            if (obj1Area < obj2Area) {
                return 1;
            } else if (obj1Area > obj2Area) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private class AreaComparator implements Comparator<VASTMediaFile> {

        @Override
        public int compare(VASTMediaFile obj1, VASTMediaFile obj2) {
            // get area of the video of the two MediaFiles
            int obj1Area = obj1.getWidth().intValue()
                    * obj1.getHeight().intValue();
            int obj2Area = obj2.getWidth().intValue()
                    * obj2.getHeight().intValue();
            // choose the MediaFile which has the lower difference in area
            if (obj1Area < obj2Area) {
                return 1;
            } else if (obj1Area > obj2Area) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private class CompanionComparator implements Comparator<VASTCompanionAd> {

        @Override
        public int compare(VASTCompanionAd obj1, VASTCompanionAd obj2) {
            // get area of the video of the two MediaFiles
            try {
                float obj1Area = obj1.getWidth().floatValue()
                        / obj1.getHeight().floatValue();
                float obj2Area = obj2.getWidth().floatValue()
                        / obj2.getHeight().floatValue();
                // get the difference between the area of the MediaFile and the area
                // of the screen
//            int obj1Diff = Math.abs(obj1Area - deviceArea);
//            int obj2Diff = Math.abs(obj2Area - deviceArea);
//            AdViewUtils.logInfo("AreaComparator: obj1:" + obj1Diff + " obj2:"
//                    + obj2Diff);

                // choose the MediaFile which has the lower difference in area
                if (obj1Area < obj2Area) {
                    return 1;
                } else if (obj1Area > obj2Area) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

    }

    private boolean isMediaFileCompatible(VASTMediaFile media) {

        // check if the MediaFile is compatible with the device.
        // further checks can be added here
        return media.getType().matches(SUPPORTED_VIDEO_TYPE_REGEX) ||
                media.getType().matches(SUPPORTED_JAVASCRIPT_TYPE_REGEX) || //wilder 2019 for vpaid,1st must be video mode, if video no, check vpaid
                media.getType().matches(SUPPORTED_IMAGE_TYPE_REGEX) ||
                media.getType().matches(SUPPORTED_HTML_TYPE_REGEX) ;
    }

    //wilder 2019 for get matched video based on bitrate
    private VASTMediaFile getBestMediaFileMatch(List<VASTMediaFile> lists) {
        AdViewUtils.logInfo("getBestMediaFileMatch()");
        // Iterate through the sorted list and return the first compatible media.
        // If none of the media file is compatible, return null
        VASTMediaFile firstMeetFile = null, maxBitrateFile = null;
        Iterator<VASTMediaFile> iterator = lists.iterator();
        try {
            while (iterator.hasNext()) {
                VASTMediaFile media = iterator.next();
                if (isMediaFileCompatible(media)) {
                    if (firstMeetFile == null) {
                        firstMeetFile = media;
                    } else {
                        int bitrate = 0;
                        if (media.getBitrate() != null) {
                            bitrate = media.getBitrate().intValue();
                        }
                        AdViewUtils.logInfo("++++++ Video BitRate : [ " + bitrate + " ]+++++++");
                        if (bitrate <= MAX_VIDEO_BITRATE) {
                            //search max bitrate one , but if too high bitrate may cause play slow
                            if (maxBitrateFile == null) {
                                maxBitrateFile = media;
                            } else if (bitrate > maxBitrateFile.getBitrate().intValue()) {
                                maxBitrateFile = media;
                            }
                        }
                    }
                }
            }
            //filter done, got the max one to first media
            if ((maxBitrateFile != null)) {
                if (firstMeetFile.getBitrate().intValue() >= MAX_VIDEO_BITRATE) {
                    firstMeetFile = maxBitrateFile;
                } else {
                    firstMeetFile = (firstMeetFile.getBitrate().intValue() > maxBitrateFile.getBitrate().intValue()) ? firstMeetFile : maxBitrateFile;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return firstMeetFile;
    }

    private boolean isCompanionCompatible(VASTCompanionAd media) {
        // check if the MediaFile is compatible with the device.
        // further checks can be added here
        if (media.getValueType() ==  null) {
            return false;
        }
        return media.getValueType().matches(SUPPORTED_IMAGE_TYPE_REGEX);
    }

    private VASTCompanionAd getBestCompanionMatch(List<VASTCompanionAd> list) {
        AdViewUtils.logInfo("getBestCompanionMatch()");
        // Iterate through the sorted list and return the first compatible media.
        // If none of the media file is compatible, return null
        Iterator<VASTCompanionAd> iterator = list.iterator();

        while (iterator.hasNext()) {
            VASTCompanionAd media = iterator.next();
            if (isCompanionCompatible(media)) {
                return media;
            }
        }
        return null;
    }

}
