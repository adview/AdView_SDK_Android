//
//  Tracking.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.video.vast.model;

import java.io.Serializable;

public class Tracking  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6299566407557931472L;
	private String value;
	private TRACKING_EVENTS_TYPE event;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public TRACKING_EVENTS_TYPE getEvent() {
		return event;
	}

	public void setEvent(TRACKING_EVENTS_TYPE event) {
		this.event = event;
	}

	@Override
	public String toString() {
		return "Tracking [event=" + event + ", value=" + value + "]";
	}

}
