package com.kuaiyou.mraid.utils;

import com.kuaiyou.utils.AdViewUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MRAIDParser {

	private final static String TAG = "MRAIDParser";

	public Map<String, String> parseCommandUrl(String commandUrl) {
		// The command is a URL string that looks like this:
		//
		// mraid://command?param1=val1&param2=val2&...
		//
		// We need to parse out the command and create a map containing it and
		// its the parameters and their associated values.

		AdViewUtils.logInfo( "parseCommandUrl " + commandUrl);

		// Remove mraid:// prefix.
		String s = commandUrl.substring(8);

		String command;
		Map<String, String> params = new HashMap<String, String>();

		// Check for parameters, parse them if found
		int idx = s.indexOf('?');
		if (idx != -1) {
			command = s.substring(0, idx);
			String paramStr = s.substring(idx + 1);
			String[] paramArray = paramStr.split("&");
			for (String param : paramArray) {
				idx = param.indexOf('=');
				String key = param.substring(0, idx);
				String val = param.substring(idx + 1);
				params.put(key, val);
			}
		} else {
			command = s;
		}

		// Check for valid command.
		if (!isValidCommand(command)) {
			AdViewUtils.logInfo("command " + command + " is unknown");
			return null;
		}

		// Check for valid parameters for the given command.
		if (!checkParamsForCommand(command, params)) {
			AdViewUtils.logInfo("command URL " + commandUrl + " is missing parameters");
			return null;
		}

		Map<String, String> commandMap = new HashMap<String, String>();
		commandMap.put("command", command);
		commandMap.putAll(params);
		return commandMap;
	}

	private boolean isValidCommand(String command) {
		final String[] commands = { "close", "createCalendarEvent", "expand",
				"open", "playVideo", "volumeON", "volumeOFF", "onPlayStarted",
				"onPlayEnded", "resize", "setOrientationProperties",
				"setResizeProperties", "storePicture", "useCustomClose",
				"action-web", "action-app", "action-call", "action-sms","action-play","action-replay",
				"action-undefined" };
		return (Arrays.asList(commands).contains(command));
	}

	private boolean checkParamsForCommand(String command,
			Map<String, String> params) {
		if (command.equals("createCalendarEvent")) {
			return params.containsKey("eventJSON");
		} else if (command.equals("open") || command.equals("playVideo")
				|| command.equals("storePicture")) {
			return params.containsKey("url");
		} else if (command.equals("setOrientationProperties")) {
			return params.containsKey("allowOrientationChange")
					&& params.containsKey("forceOrientation");
		} else if (command.equals("setResizeProperties")) {
			return params.containsKey("width") && params.containsKey("height")
					&& params.containsKey("offsetX")
					&& params.containsKey("offsetY")
					&& params.containsKey("customClosePosition")
					&& params.containsKey("allowOffscreen");
		} else if (command.equals("useCustomClose")) {
			return params.containsKey("useCustomClose");
		}
		return true;
	}

}
