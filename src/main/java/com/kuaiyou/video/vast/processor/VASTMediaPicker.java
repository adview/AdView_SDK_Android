//
//  MediaPicker.java
//
//  Created by Harsha Herur on 12/4/13.
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.video.vast.processor;

import java.util.ArrayList;
import java.util.List;

import com.kuaiyou.video.vast.model.VASTCompanionAd;
import com.kuaiyou.video.vast.model.VASTCreative;
import com.kuaiyou.video.vast.model.VASTMediaFile;

public interface VASTMediaPicker {
	
	ArrayList<VASTCreative> pickCreative(ArrayList<VASTCreative> list);
	
	VASTMediaFile pickVideo(List<VASTMediaFile> list);

	VASTCompanionAd pickCompanion(ArrayList<VASTCompanionAd> list);
}
