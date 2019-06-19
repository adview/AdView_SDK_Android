//
//  IconClicks.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.video.vast.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompanionClicks  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -623900511825053316L;
	private String clickThrough;
	private List<String> clickTracking;
	private List<String> trackingEvent;

	public String getClickThrough() {
		return clickThrough;
	}

	public void setClickThrough(String clickThrough) {
		this.clickThrough = clickThrough;
	}

	public List<String> getClickTracking() {
		if (clickTracking == null) {
			clickTracking = new ArrayList<String>();
		}
		return this.clickTracking;
	}

	public List<String> getTrackingEvent() {
		if (trackingEvent == null) {
			trackingEvent = new ArrayList<String>();
		}
		return this.trackingEvent;
	}


//	public List<String> getCustomClick() {
//		if (customClick == null) {
//			customClick = new ArrayList<String>();
//		}
//		return this.customClick;
//	}

	@Override
	public String toString() {
		return "VideoClicks [clickThrough=" + clickThrough
				+ ", clickTracking=[" + listToString(clickTracking)
				+ "]]";
	}

	private String listToString(List<String> list) {
		StringBuffer sb = new StringBuffer();

		if (list == null) {
			return "";
		}
		for (int x = 0; x < list.size(); x++) {
			sb.append(list.get(x).toString());
		}
		return sb.toString();
	}
}
