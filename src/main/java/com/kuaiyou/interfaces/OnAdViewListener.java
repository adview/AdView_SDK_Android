package com.kuaiyou.interfaces;

import android.view.View;
import android.view.ViewGroup;

/**
 * 竞价
 * 
 */
public interface OnAdViewListener {

	/**
	 * 当广告被显示时调用该函数.
	 */
	public void onAdClicked(View v);

	/**
	 * 当广告被显示时调用该函数.
	 */
	public void onAdDisplayed(View v);

	/**
	 * 当广告成功加载是调用（测试功能）
	 */
	public void onAdReady(View v);
	/**
	 * 当广告请求成功时调用该函数.
	 */
	public void onAdRecieved(View v);

	/**
	 * 当广告请求失败时调用该函数.
	 */
	public void onAdRecieveFailed(View v, String errorCode);

	/**
	 * 当广告被关闭时调用该函数.
	 */
	public void onAdClosedAd(View v);

	/**
	 * 展示时间结束将要关闭时调用.
	 */
	public void onAdSpreadPrepareClosed();

	/**
	 * 用户取消展示
	 */
	public void onAdClosedByUser();

	/**
	 * 自定义回调
	 */
	public void onAdNotifyCustomCallback(ViewGroup view, int ruleTime,
			int delayTime);
}
