package com.kuaiyou.video.vast.model;

import java.io.Serializable;
import java.math.BigInteger;

public class VASTCompanionAd  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1957834515896092464L;
	private String staticValue;
	private String htmlValue;
	private String iFrameValue;
	private String valueType;
	private String id;
	private BigInteger width;
	private BigInteger height;
	private BigInteger assetWidth;
	private BigInteger assetHeight;
	private BigInteger expandedWidth;
	private BigInteger expandedHeight;
	private String apiFramework;
	private String adSlotID;
	
	private CompanionClicks companionClicks;
	
	public CompanionClicks getCompanionClicks() {
		return companionClicks;
	}
	public void setCompanionClicks(CompanionClicks companionClicks) {
		this.companionClicks = companionClicks;
	}
	public String getValueType() {
		return valueType;
	}
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	public String getStaticValue() {
		return staticValue;
	}
	public void setStaticValue(String staticValue) {
		this.staticValue = staticValue;
	}
	public String getHtmlValue() {
		return htmlValue;
	}
	public void setHtmlValue(String htmlValue) {
		this.htmlValue = htmlValue;
	}
	public String getiFrameValue() {
		return iFrameValue;
	}
	public void setiFrameValue(String iFrameValue) {
		this.iFrameValue = iFrameValue;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public BigInteger getWidth() {
		return width;
	}
	public void setWidth(BigInteger width) {
		this.width = width;
	}
	public BigInteger getHeight() {
		return height;
	}
	public void setHeight(BigInteger height) {
		this.height = height;
	}
	public BigInteger getAssetWidth() {
		return assetWidth;
	}
	public void setAssetWidth(BigInteger assetWidth) {
		this.assetWidth = assetWidth;
	}
	public BigInteger getAssetHeight() {
		return assetHeight;
	}
	public void setAssetHeight(BigInteger assetHeight) {
		this.assetHeight = assetHeight;
	}
	public BigInteger getExpandedWidth() {
		return expandedWidth;
	}
	public void setExpandedWidth(BigInteger expandedWidth) {
		this.expandedWidth = expandedWidth;
	}
	public BigInteger getExpandedHeight() {
		return expandedHeight;
	}
	public void setExpandedHeight(BigInteger expandedHeight) {
		this.expandedHeight = expandedHeight;
	}
	public String getApiFramework() {
		return apiFramework;
	}
	public void setApiFramework(String apiFramework) {
		this.apiFramework = apiFramework;
	}
	public String getAdSlotID() {
		return adSlotID;
	}
	public void setAdSlotID(String adSlotID) {
		this.adSlotID = adSlotID;
	}
	
	
}
