package com.kuaiyou.obj;

import java.io.Serializable;

public class RetAdBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3427103170627091128L;
	// private String idApp = "";
	// private String idAd = "";
	// private int adShowType;
	// private String adShowText = "";
	// private String adShowPic = "";
	// private int adLinkType;
	// private String adLink = "";
	private int count = 0;
	private String lastAd = "";
	private String msg = "";
	private int result = 0;
	private String ads = null;
	private int adSource = 0;
	private int serverAgent = 0;
	private int agt = 0;
	private int sc = 0;

	private int adType = 0;

	public int getSc() {
		return sc;
	}

	public void setSc(int sc) {
		this.sc = sc;
	}

	public int getAdType() {
		return adType;
	}

	public void setAdType(int adType) {
		this.adType = adType;
	}

	public int getAgt() {
		return agt;
	}

	public void setAgt(int agt) {
		this.agt = agt;
	}

	public int getServerAgent() {
		return serverAgent;
	}

	public void setServerAgent(int serverAgent) {
		this.serverAgent = serverAgent;
	}

	public int getAdSource() {
		return adSource;
	}

	public void setAdSource(int adSource) {
		this.adSource = adSource;
	}

	public String getAds() {
		return ads;
	}

	public void setAds(String ads) {
		this.ads = ads;
	}

	// public String getIdApp() {
	// return idApp;
	// }
	//
	// public void setIdApp(String idApp) {
	// this.idApp = idApp;
	// }
	//
	// public String getIdAd() {
	// return idAd;
	// }
	//
	// public void setIdAd(String idAd) {
	// this.idAd = idAd;
	// }
	//
	// public int getAdShowType() {
	// return adShowType;
	// }
	//
	// public void setAdShowType(int adShowType) {
	// this.adShowType = adShowType;
	// }
	//
	// public String getAdShowText() {
	// return adShowText;
	// }
	//
	// public void setAdShowText(String adShowText) {
	// this.adShowText = adShowText;
	// }
	//
	// public String getAdShowPic() {
	// return adShowPic;
	// }
	//
	// public void setAdShowPic(String adShowPic) {
	// this.adShowPic = adShowPic;
	// }
	//
	// public int getAdLinkType() {
	// return adLinkType;
	// }
	//
	// public void setAdLinkType(int adLinkType) {
	// this.adLinkType = adLinkType;
	// }
	//
	// public String getAdLink() {
	// return adLink;
	// }
	//
	// public void setAdLink(String adLink) {
	// this.adLink = adLink;
	// }
	//
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	//
	public String getLastAd() {
		return lastAd;
	}

	public void setLastAd(String lastAd) {
		this.lastAd = lastAd;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

}
