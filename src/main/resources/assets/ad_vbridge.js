////////////////////   VPAID Bridge Wrapper   ////////////////////////////////
adVPAIDWrapper=function(){
	this._creative=getVPAIDAd();
	//this._ui=getUI();
	this.timer=null;
	adVPAIDWrapper.prototype.setVpaidClient=function(vpaidClient){
		this._vpaidClient=vpaidClient;
	}
	adVPAIDWrapper.prototype.handshakeVersion=function(version){	
		var result=this._creative.handshakeVersion(version);
		android.handshakeVersionResult(result);
		return result;
	}
	adVPAIDWrapper.prototype.initAd=function(width,height,viewMode,desiredBitrate,creativeData,environmentVars){		
		this._creative.initAd(width,height,viewMode,desiredBitrate,creativeData,environmentVars);
		android.initAdResult();
	};
	adVPAIDWrapper.prototype.onAdPaused=function(){
			console.log("onAdPaused");
			this._vpaidClient.vpaidAdPaused();
	};
	adVPAIDWrapper.prototype.onAdPlaying=function(){
			console.log("onAdPlaying");
			this._vpaidClient.vpaidAdPlaying();
	};
	adVPAIDWrapper.prototype.onAdError=function(message){
		console.log("onAdError: "+message);
		this._vpaidClient.vpaidAdError(message);
	};
	adVPAIDWrapper.prototype.onAdLog=function(message){
		console.log("onAdLog: "+message);
		this._vpaidClient.vpaidAdLog(message);
	};
	adVPAIDWrapper.prototype.onAdUserAcceptInvitation=function(){
		console.log("onAdUserAcceptInvitation");
		this._vpaidClient.vpaidAdUserAcceptInvitation();	
	};
	adVPAIDWrapper.prototype.onAdUserMinimize=function(){
		console.log("onAdUserMinimize");
		this._vpaidClient.vpaidAdUserMinimize();
	};
	adVPAIDWrapper.prototype.onAdUserClose=function(){
		console.log("onAdUserClose");
		this._vpaidClient.vpaidAdUserClose();
	};
	adVPAIDWrapper.prototype.onAdSkippableStateChange=function(){
		console.log("Ad Skippable State Changed to: "+this._creative.getAdSkippableState());
		//this._ui.showSkipButton(this.getAdSkippableState());
		this.getAdSkippableState(); //wilder 2019 , pass skip state to app through getstateresult()
		this._vpaidClient.vpaidAdSkippableStateChange();
	};
	adVPAIDWrapper.prototype.onAdExpandedChange=function(){
		console.log("Ad Expanded Changed to: "+this._creative.getAdExpanded());
		this._vpaidClient.vpaidAdExpandedChange();
	};
	adVPAIDWrapper.prototype.getAdExpanded=function(){
		console.log("getAdExpanded");
		var result=this._creative.getAdExpanded();
		android.getAdExpandedResult(result);
	};
	adVPAIDWrapper.prototype.getAdSkippableState=function(){
		console.log("getAdSkippableState");
		if (this._creative.getAdSkippableState && typeof this._creative.getAdSkippableState == "function") {
		    var result=this._creative.getAdSkippableState();
		    android.getAdSkippableStateResult(result);
		}else
		    return false;
	};
	adVPAIDWrapper.prototype.onAdSizeChange=function(){
		console.log("Ad size changed to: w="+this._creative.getAdWidth()+" h="+this._creative.getAdHeight());
		this._vpaidClient.vpaidAdSizeChange();
	};
	adVPAIDWrapper.prototype.onAdDurationChange=function(){
		if(this.handshakeVersion()>=2){
			//this._ui.moveProgress(this._creative.getAdRemainingTime(),this._creative.getAdDuration());
		}
		this._vpaidClient.vpaidAdDurationChange();
	};
	adVPAIDWrapper.prototype.onAdRemainingTimeChange=function(){
		if(this.handshakeVersion()<2){
			//this._ui.moveProgress(this._creative.getAdRemainingTime(),this._creative.getAdDuration());
		}
		this._vpaidClient.vpaidAdRemainingTimeChange();
	};
	adVPAIDWrapper.prototype.getAdRemainingTime=function(){
		console.log("getAdRemainingTime");
		var result=this._creative.getAdRemainingTime();
		android.getAdRemainingTimeResult(result);
	};
	adVPAIDWrapper.prototype.onAdImpression=function(){
			console.log("Ad Impression");
			this._vpaidClient.vpaidAdImpression();
	};
	adVPAIDWrapper.prototype.onAdClickThru=function(url,id,playerHandles){
			console.log("Clickthrough portion of the ad was clicked");
			var adjustedUrl=url;
			if(adjustedUrl==undefined)
				adjustedUrl=""
			this._vpaidClient.vpaidAdClickThruIdPlayerHandles(adjustedUrl,id,playerHandles);
	};
	adVPAIDWrapper.prototype.onAdInteraction=function(id){
		console.log("----- A non-clickthrough event has occured: vpaidAdInteraction() ----------");
		this._vpaidClient.vpaidAdInteraction(id);
	};
	adVPAIDWrapper.prototype.onAdVideoStart=function(){
		console.log("Video 0% completed");
		this._vpaidClient.vpaidAdVideoStart();
		document.getElementById("black-screen")&&(document.getElementById("black-screen").style.display="none");
		};
	adVPAIDWrapper.prototype.onAdVideoFirstQuartile=function(){
		console.log("Video 25% completed");
		this._vpaidClient.vpaidAdVideoFirstQuartile();
	};
	adVPAIDWrapper.prototype.onAdVideoMidpoint=function(){
		console.log("Video 50% completed");
		this._vpaidClient.vpaidAdVideoMidpoint();
	};
	adVPAIDWrapper.prototype.onAdVideoThirdQuartile=function(){
		console.log("Video 75% completed");
		this._vpaidClient.vpaidAdVideoThirdQuartile();
	};
	adVPAIDWrapper.prototype.onAdVideoComplete=function(){
		console.log("Video 100% completed");
		this._vpaidClient.vpaidAdVideoComplete();
	};
	adVPAIDWrapper.prototype.onAdLinearChange=function(){
		console.log("Ad linear has changed: "+this._creative.getAdLinear());
		this._vpaidClient.vpaidAdLinearChange();
	};
	adVPAIDWrapper.prototype.getAdLinear=function(){
		console.log("getAdLinear");
		var result=this._creative.getAdLinear();
		android.getAdLinearResult(result);
	};
	adVPAIDWrapper.prototype.getAdDuration=function(){
		console.log("getAdDuration");
		var result=this._creative.getAdDuration();
		android.getAdDurationResult(result);
	};
	adVPAIDWrapper.prototype.onAdLoaded=function(){
		console.log("ad has been loaded");
		//this._ui.createSkipButton();
		//this._ui.showSkipButton(this.getAdSkippableState());
        this.getAdSkippableState(); //wilder 2019
        this.getAdVolume();     //wilder 2019
		this._vpaidClient.vpaidAdLoaded();
	};
	adVPAIDWrapper.prototype.onAdStarted=function(){
		console.log("Ad has started");
		//var res=this._creative.getAdDuration();
        //android.getAdDurationResult(res);
		this.timer=setInterval(function(){
			//this._ui.moveProgress(this._creative.getAdRemainingTime(),this._creative.getAdDuration());
			var res=this._creative.getAdDuration();
            android.getAdDurationResult(res);
			var result=this._creative.getAdRemainingTime();
			this._vpaidClient.getAdRemainingTimeResult(result);

			}.bind(this),500);

		this._vpaidClient.vpaidAdStarted();
	};
	adVPAIDWrapper.prototype.onAdStopped=function(){
		console.log("Ad has stopped");
		clearInterval(this.timer);
		this._vpaidClient.vpaidAdStopped();
	};
	adVPAIDWrapper.prototype.onAdSkipped=function(){
		console.log("Ad was skipped");
		this._creative.stopAd();
		this._vpaidClient.vpaidAdSkipped();
	};
	adVPAIDWrapper.prototype.setAdVolume=function(val){
	this._creative.setAdVolume(val);
	};
	adVPAIDWrapper.prototype.getAdVolume=function(){
	var result=this._creative.getAdVolume();
	android.getAdVolumeResult(result);
	};
	adVPAIDWrapper.prototype.onAdVolumeChange=function(){
	console.log("Ad Volume has changed to - "+this._creative.getAdVolume());
	this._vpaidClient.vpaidAdVolumeChanged();
	};
	adVPAIDWrapper.prototype.startAd=function(){
	//this._ui.createProgressBar();
	this._creative.startAd();
	};
	adVPAIDWrapper.prototype.skipAd=function(){
	this._creative.skipAd();
	};
	adVPAIDWrapper.prototype.stopAd=function(){
	this._creative.stopAd();
	};
	adVPAIDWrapper.prototype.resizeAd=function(width,height,viewMode){
	this._creative.resizeAd(width,height,viewMode);
	};
	adVPAIDWrapper.prototype.pauseAd=function(){
	this._creative.pauseAd();
	};
	adVPAIDWrapper.prototype.resumeAd=function(){
	this._creative.resumeAd();
	};
	adVPAIDWrapper.prototype.expandAd=function(){
	this._creative.expandAd();
	};
	adVPAIDWrapper.prototype.collapseAd=function(){
	this._creative.collapseAd();
	};
	adVPAIDWrapper.prototype.setCallbacksForCreative=function(){
			var callbacks=
			{
			'AdStarted':this.onAdStarted,
			'AdStopped':this.onAdStopped,
			'AdSkipped':this.onAdSkipped,
			'AdLoaded':this.onAdLoaded,
			'AdLinearChange':this.onAdLinearChange,
			'AdSizeChange':this.onAdSizeChange,
			'AdExpandedChange':this.onAdExpandedChange,
			'AdSkippableStateChange':this.onAdSkippableStateChange,
			'AdDurationChange':this.onAdDurationChange,
			'AdRemainingTimeChange':this.onAdRemainingTimeChange,
			'AdVolumeChange':this.onAdVolumeChange,
			'AdImpression':this.onAdImpression,
			'AdClickThru':this.onAdClickThru,
			'AdInteraction':this.onAdInteraction,
			'AdVideoStart':this.onAdVideoStart,
			'AdVideoFirstQuartile':this.onAdVideoFirstQuartile,
			'AdVideoMidpoint':this.onAdVideoMidpoint,
			'AdVideoThirdQuartile':this.onAdVideoThirdQuartile,
			'AdVideoComplete':this.onAdVideoComplete,
			'AdUserAcceptInvitation':this.onAdUserAcceptInvitation,
			'AdUserMinimize':this.onAdUserMinimize,
			'AdUserClose':this.onAdUserClose,
			'AdPaused':this.onAdPaused,
			'AdPlaying':this.onAdPlaying,
			'AdError':this.onAdError,
			'AdLog':this.onAdLog
			};
			for(var eventName in callbacks){
				this._creative.subscribe(callbacks[eventName],eventName,this);
			}
	};
	adVPAIDWrapper.prototype.onAdSkipPress=function(){
		this._creative.skipAd();
	}
	adVPAIDWrapper.prototype.setCallbacksForUI=function(){
	    /*
		var callbacks={'AdSkipped':this.onAdSkipPress,};
		for(var eventName in callbacks){
			this._ui.subscribe(callbacks[eventName],eventName,this);
		}
		*/
	};
}

initVpaidWrapper=function(){
	adVPAIDWrapperInstance=new adVPAIDWrapper();
	adVPAIDWrapperInstance.setCallbacksForCreative();
	adVPAIDWrapperInstance.setVpaidClient(android);
	adVPAIDWrapperInstance.setCallbacksForUI()
	android.wrapperReady();
}



