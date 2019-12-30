//
//  TRACKING_EVENTS_TYPE.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//



package com.kuaiyou.video.vast.model;

public enum TRACKING_EVENTS_TYPE {
	creativeView,
	start,
	midpoint,
	firstQuartile,
	thirdQuartile,
	complete,
	mute,
	unmute,
	pause,
	rewind,
	resume,
	fullscreen,
	expand,
	minimize, //omsdk
	collapse,
	acceptInvitation,
	skip,
	click,		//omsdk v1.2
	invitationAccept,	//omsdk v1.2
	close
}