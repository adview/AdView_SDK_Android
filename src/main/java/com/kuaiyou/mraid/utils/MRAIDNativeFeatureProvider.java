package com.kuaiyou.mraid.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;

import com.kuaiyou.utils.AdViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MRAIDNativeFeatureProvider {

	private static final String TAG = "MRAIDNativeFeatureProvider";

	private final Context context;
	private final MRAIDNativeFeatureManager nativeFeatureManager;

	public MRAIDNativeFeatureProvider(Context context,
									  MRAIDNativeFeatureManager nativeFeatureManager) {
		this.context = context;
		this.nativeFeatureManager = nativeFeatureManager;
	}

	final public void callTel(String url) {
		if (nativeFeatureManager.isTelSupported()) {
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			context.startActivity(intent);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressLint("SimpleDateFormat")
	public void createCalendarEvent(String eventJSON) {
		if (!nativeFeatureManager.isCalendarSupported()) {
			return;
		}
		try {
			// Need to fix some of the encoded string from JS
			eventJSON = eventJSON.replace("\\", "").replace("\"{", "{")
					.replace("}\"", "}");
			JSONObject jsonObject = new JSONObject(eventJSON);

			String description = jsonObject
					.optString("description", "Untitled");
			String location = jsonObject.optString("location", "unknown");
			String summary = jsonObject.optString("summary");

			/*
			 * NOTE: The Java SimpleDateFormat class will not work as is with
			 * the W3C spec for calendar entries. The problem is that the W3C
			 * spec has time zones (UTC offsets) containing a colon like this:
			 * "2014-12-21T12:34-05:00" The SimpleDateFormat parser will choke
			 * on the colon. It wants something like this:
			 * "2014-12-21T12:34-0500"
			 * 
			 * Also, the W3C spec indicates that seconds are optional, so we
			 * have to use two patterns to be able to parse both this:
			 * "2014-12-21T12:34-0500" and this: "2014-12-21T12:34:56-0500"
			 */

			String[] patterns = { "yyyy-MM-dd'T'HH:mmZ",
					"yyyy-MM-dd'T'HH:mm:ssZ", };

			String[] dateStrings = new String[2];
			dateStrings[0] = jsonObject.getString("start");
			dateStrings[1] = jsonObject.optString("end");

			long startTime = 0;
			long endTime = 0;

			for (int i = 0; i < dateStrings.length; i++) {
				if (TextUtils.isEmpty(dateStrings[i])) {
					continue;
				}
				// remove the colon in the timezone
				dateStrings[i] = dateStrings[i].replaceAll(
						"([+-]\\d\\d):(\\d\\d)$", "$1$2");
				for (String pattern : patterns) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat(pattern);
						if (i == 0) {
							startTime = sdf.parse(dateStrings[i]).getTime();
						} else {
							endTime = sdf.parse(dateStrings[i]).getTime();
						}
						break;
					} catch (ParseException e) {
						continue;
					}
				}
			}

			/*
			 * boolean wholeDay = false; if (jObject.getJSONObject("recurrence")
			 * != null) { JSONObject recurrence =
			 * jObject.getJSONObject("recurrence"); if
			 * (recurrence.getString("frequency") != null) { wholeDay =
			 * recurrence.getString("frequency").toLowerCase().equals("daily");
			 * } }
			 */

			Intent intent = new Intent(Intent.ACTION_INSERT)
					.setType("vnd.android.cursor.item/event");
			intent.putExtra(Events.TITLE, description);
			intent.putExtra(Events.DESCRIPTION, summary);
			intent.putExtra(Events.EVENT_LOCATION, location);

			if (startTime > 0) {
				intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
						startTime);
			}

			if (endTime > 0) {
				intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
			}

			/*
			 * if (wholeDay) {
			 * intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, wholeDay);
			 * }
			 */

			context.startActivity(intent);
		} catch (JSONException e) {
			AdViewUtils.logInfo("Error parsing JSON: " + e.getLocalizedMessage());
		}
	}

	public void playVideo(String url) {
		//Uri u = Uri.parse(url);
		try {
			String str = URLDecoder.decode(url, "UTF-8");
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
			context.startActivity(intent);
		}catch (Exception e) {
			e.printStackTrace();
		}
		//AdViewUtils.openLandingPage(context,url, true);
	}

	// public void openBrowser(String url) {
	// if (url.startsWith("market:")) {
	// Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	// context.startActivity(intent);
	// } else if (url.startsWith("http:") || url.startsWith("https:")) {
	// Intent intent = new Intent(context, AdviewWebView.class);
	// intent.putExtra(MRAIDBrowser.URL_EXTRA, url);
	// intent.putExtra(MRAIDBrowser.MANAGER_EXTRA,
	// nativeFeatureManager.getSupportedNativeFeatures());
	// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// context.startActivity(intent);
	// }
	// }

	public void storePicture(final String url) {
		if (nativeFeatureManager.isStorePictureSupported()) {
			// Spawn a new thread to download and save the image
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						storePictureInGallery(url);
					} catch (Exception e) {
						AdViewUtils.logInfo( e.getLocalizedMessage());
					}
				}
			}).start();
		}
	}

	public void sendSms(String url) {
		if (nativeFeatureManager.isSmsSupported()) {
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
			context.startActivity(intent);
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void storePictureInGallery(String url) {
		// Setting up file to write the image to.
		SimpleDateFormat gmtDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd-HHmmss");
		String s = getAlbumDir() + "/img" + gmtDateFormat.format(new Date())
				+ ".png";
		AdViewUtils.logInfo("Saving image into: " + s);
		File f = new File(s);
		// Open InputStream to download the image.
		InputStream is;
		try {
			is = new URL(url).openStream();
			// Set up OutputStream to write data into image file.
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			MediaScannerConnection.scanFile(context,
					new String[] { f.getAbsolutePath() }, null,
					new OnScanCompletedListener() {

						@Override
						public void onScanCompleted(String path, Uri uri) {
							AdViewUtils.logInfo("File saves successfully to " + path);
						}
					});
			AdViewUtils.logInfo("Saved image successfully");
		} catch (MalformedURLException e) {
			AdViewUtils.logInfo(
					"Not able to save image due to invalid URL: "
							+ e.getLocalizedMessage());
		} catch (IOException e) {
			AdViewUtils.logInfo( "Unable to save image: " + e.getLocalizedMessage());
		}
	}

	private void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1) {
					break;
				}
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			AdViewUtils.logInfo("Error saving picture: " + ex.getLocalizedMessage());
		}
	}

	private File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"NexageAd");
			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						AdViewUtils.logInfo("Failed to create camera directory");
						return null;
					}
				}
			}
		} else {
			AdViewUtils.logInfo("External storage is not mounted READ/WRITE.");
		}
		return storageDir;
	}

}
