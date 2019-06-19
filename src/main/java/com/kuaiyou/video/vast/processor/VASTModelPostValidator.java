//
//  VASTModelPostValidator.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.video.vast.processor;

import java.util.ArrayList;
import java.util.List;

import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.video.vast.model.VASTCreative;
import com.kuaiyou.video.vast.model.VASTMediaFile;
import com.kuaiyou.video.vast.model.VASTModel;

import android.text.TextUtils;

public class VASTModelPostValidator {

	// This method tries to make sure that there is at least 1 Media file to
	// be used for VASTActivity. Also, if the boolean validateModel is true, it
	// will
	// do additional validations which includes "at least 1 impression tracking
	// url's is required'
	// If any of the above fails, it returns false. The false indicates that you
	// can stop proceeding
	// further to display this on the MediaPlayer.

	public static boolean validate(VASTModel model, VASTMediaPicker mediaPicker) {
		AdViewUtils.logInfo( "validate");
		int count = 0;
		if (!validateModel(model)) {
			AdViewUtils.logInfo( "Validator returns: not valid (invalid model)");
			return false;
		}
		// boolean isValid = false;
		// Must have a MediaPicker to choose one of the MediaFile element from
		// XML
		ArrayList<VASTCreative> creativesList = model.getCreativeList();
		if (mediaPicker != null) {
			if (null != creativesList && !creativesList.isEmpty()) {
			    //(wilder 2019) here only get video or media type  creatives
				ArrayList<VASTCreative> creatives = mediaPicker.pickCreative(creativesList);
				//(wilder 2019) also, companion also must have "type" value of nodes
				mediaPicker.pickCompanion(model.getCompanionAdList());
				model.setAppropriateCreative(creatives);
				if (null != creatives && !creatives.isEmpty()) {
					AdViewUtils.logInfo("pick is not empty");
					// List<VASTMediaFile> mediaFiles =
					// model.get(i).getCreativeMediaList();
					for (int i = 0; i < creatives.size(); i++) {
						VASTMediaFile mediaFile = mediaPicker.pickVideo(creatives.get(i).getMediaFiles());
						if (mediaFile != null) {
							String url = mediaFile.getValue();
							if (!TextUtils.isEmpty(url)) {
								AdViewUtils.logInfo("picked url =" + url);
								creatives.get(i).setPickedVideoType(mediaFile.getType());
								creatives.get(i).setPickedVideoUrl(url);
								creatives.get(i).setPickedVideoWidth(mediaFile.getWidth().intValue());
								creatives.get(i).setPickedVideoHeight(mediaFile.getHeight().intValue());
								//wilder 2019 for VPAID
								String apiFrame = mediaFile.getApiFramework();
								if (!TextUtils.isEmpty(apiFrame) && apiFrame.equalsIgnoreCase("VPAID")) {
									creatives.get(i).setVPAIDurl(url);
								}
								AdViewUtils.logInfo("+++ mediaPicker selected mediaFile with URL: " + url + "+++");

							} else
								count++;
						}else {
							/* wilder 2019, also , if media type not support by picker, it will trigger here
							for (int j = 0; j< creatives.get(i).getMediaFiles().size();j++) {
								mediaFile = creatives.get(i).getMediaFiles().get(j);
								String apiFrame = mediaFile.getApiFramework();
							}
							*/
							//end wilder
                        }
					}
				}
			}

		} else {
			AdViewUtils.logInfo(
					"mediaPicker: We don't have a compatible media file to play.");
		}
		// AdViewUtils.logInfo( "Validator returns: "
		// + (isValid ? "valid" : "not valid (no media file)"));
		if (count == creativesList.size())
			return false;
		else
			return true;
	}

	private static boolean validateModel(VASTModel model) {
		AdViewUtils.logInfo( "validateModel");
		boolean isValid = true;
		int count = 0;
		// There should be at least one impression.
		//fix
//		List<String> impressions = model.getImpressions();
//		if (impressions == null || impressions.size() == 0) {
//			isValid = false;
//		}

		// There must be at least one VASTMediaFile object
		List<VASTCreative> creative = model.getCreativeList();
		for (int j = 0; j < creative.size(); j++) {
			ArrayList<VASTMediaFile> mediaFiles = creative.get(j)
					.getMediaFiles();
			if (mediaFiles == null || mediaFiles.size() == 0) {
				AdViewUtils.logInfo( "!!!! Validator error: mediaFile list invalid !!!!");
				isValid = false;
			}
			if (!isValid) {
				model.setVaild(false);
				count++;
			}
		}

		if (count == creative.size())
			return false;
		else
			return true;
	}

}
