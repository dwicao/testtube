package com.testtube.app.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.testtube.app.AppSettings;
import com.testtube.app.extractor.LocalSubscriptions;
import com.testtube.app.filter.BlockedChannel;
import com.testtube.app.filter.ContentFilters;
import com.testtube.app.player.common.PlayerLoopMode;
import com.testtube.app.player.common.PlayerPreferences;
import com.testtube.app.player.controller.gesture.GestureAction;
import com.testtube.app.update.UpdateManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Full settings backup as one JSON file: every Settings toggle, subscriptions, blocked
 * channels, SponsorBlock categories and player/update preferences. History, queue, downloads
 * and sign-in state are intentionally left out (history/queue have their own flows, downloads
 * are files, and tokens must never leave the device).
 *
 * <p>Restore writes everything back and needs an app restart (cached values are read once).
 */
public final class SettingsBackup {
	private static final int FORMAT = 1;

	private SettingsBackup() {
	}

	@NonNull
	public static String exportJson(@NonNull PlayerPreferences prefs, @NonNull ContentFilters filters,
	                                @NonNull com.testtube.app.filter.AiChannelList aiChannels,
	                                @NonNull UpdateManager updates) throws Exception {
		JSONObject settings = new JSONObject();
		settings.put("appTheme", AppSettings.theme().name());
		settings.put("showShorts", AppSettings.showShorts());
		settings.put("fullChannelPages", AppSettings.fullChannelPages());
		settings.put("floatingPlayer", AppSettings.floatingPlayer());
		settings.put("gesturesEnabled", AppSettings.gesturesEnabled());
		settings.put("gestureLeft", AppSettings.savedGestureLeft().name());
		settings.put("gestureRight", AppSettings.savedGestureRight().name());
		settings.put("gestureHorizontal", AppSettings.savedGestureHorizontal().name());
		settings.put("gestureDoubleTap", AppSettings.savedGestureDoubleTap().name());
		settings.put("commenting", AppSettings.commentingEnabled());
		settings.put("videoCache", AppSettings.videoCache().name());
		settings.put("showSubscriptions", AppSettings.showSubscriptions());
		settings.put("greyWatched", AppSettings.greyOutWatched());
		settings.put("watchedDim", AppSettings.watchedDimPercent());
		settings.put("clearCacheOnExit", AppSettings.clearCacheOnExit());
		settings.put("searchOnlyHome", AppSettings.searchOnlyHome());
		settings.put("personalizeHome", AppSettings.personalizeHome());
		settings.put("sponsorblockEnabled", AppSettings.sponsorblockEnabled());
		settings.put("preloadOnMobile", AppSettings.preloadOnMobile());
		settings.put("preferAvc", AppSettings.preferAvc());
		settings.put("allowHdr", AppSettings.allowHdr());
		settings.put("downloadsWifiOnly", AppSettings.downloadsWifiOnly());
		settings.put("downloadsNightly", AppSettings.downloadsNightly());
		settings.put("dataSaver", AppSettings.dataSaver());
		settings.put("qualityWifi", prefs.getQuality(false));
		settings.put("qualityMobile", prefs.getQuality(true));
		settings.put("loopMode", prefs.getLoopMode().persistedValue());
		settings.put("subtitlesEnabled", prefs.isSubtitleEnabled());
		if (prefs.getSubtitleLanguage() != null) settings.put("subtitleLanguage", prefs.getSubtitleLanguage());
		settings.put("playbackSpeed", (double) prefs.getPlaybackSpeed());
		settings.put("watchedThreshold", filters.watchedThreshold());
		settings.put("aiFilter", aiChannels.isEnabled());
		settings.put("updateAutoCheck", updates.autoCheckEnabled());
		settings.put("updatePrereleases", updates.prereleasesEnabled());
		JSONArray cats = new JSONArray();
		for (String cat : prefs.getSponsorBlockCategories()) cats.put(cat);
		settings.put("sponsorCats", cats);

		JSONObject root = new JSONObject();
		root.put("format", FORMAT);
		root.put("app", "testtube");
		root.put("settings", settings);
		root.put("subscriptionsCsv", LocalSubscriptions.get().exportCsv());
		JSONArray blocked = new JSONArray();
		for (BlockedChannel channel : filters.blockedChannels()) {
			JSONObject entry = new JSONObject();
			entry.put("name", channel.name());
			if (channel.path() != null) entry.put("path", channel.path());
			if (channel.altPath() != null) entry.put("altPath", channel.altPath());
			blocked.put(entry);
		}
		root.put("blockedChannels", blocked);
		return root.toString(2);
	}

	public static void importJson(@NonNull String json, @NonNull PlayerPreferences prefs,
	                              @NonNull ContentFilters filters,
	                              @NonNull com.testtube.app.filter.AiChannelList aiChannels,
	                              @NonNull UpdateManager updates) throws Exception {
		JSONObject root = new JSONObject(json);
		if (root.optInt("format", 0) != FORMAT) throw new IllegalArgumentException("unknown backup");
		JSONObject settings = root.getJSONObject("settings");
		AppSettings.setTheme(enumValue(AppSettings.Theme.class, settings, "appTheme", AppSettings.Theme.SYSTEM));
		AppSettings.setShowShorts(settings.optBoolean("showShorts", AppSettings.showShorts()));
		AppSettings.setFullChannelPages(settings.optBoolean("fullChannelPages", true));
		AppSettings.setFloatingPlayer(settings.optBoolean("floatingPlayer", false));
		boolean gestures = settings.optBoolean("gesturesEnabled", false);
		AppSettings.setGestures(GestureAction.parse(GestureAction.Vertical.class,
								settings.optString("gestureLeft", null), GestureAction.Vertical.BRIGHTNESS),
						GestureAction.parse(GestureAction.Vertical.class,
								settings.optString("gestureRight", null), GestureAction.Vertical.VOLUME),
						GestureAction.parse(GestureAction.Horizontal.class,
								settings.optString("gestureHorizontal", null), GestureAction.Horizontal.SEEK),
						GestureAction.parse(GestureAction.DoubleTap.class,
								settings.optString("gestureDoubleTap", null), GestureAction.DoubleTap.SKIP));
		AppSettings.setGesturesEnabled(gestures);
		AppSettings.setCommentingEnabled(settings.optBoolean("commenting", false));
		AppSettings.VideoCache cache = videoCacheOrDefault(settings.optString("videoCache", ""));
		AppSettings.setVideoCache(cache);
		AppSettings.setShowSubscriptions(settings.optBoolean("showSubscriptions", true));
		AppSettings.setGreyOutWatched(settings.optBoolean("greyWatched", true));
		try {
			AppSettings.setWatchedDimPercent(settings.optInt("watchedDim", AppSettings.watchedDimPercent()));
		} catch (RuntimeException ignored) {
		}
		AppSettings.setClearCacheOnExit(settings.optBoolean("clearCacheOnExit", false));
		AppSettings.setSearchOnlyHome(settings.optBoolean("searchOnlyHome", false));
		AppSettings.setPersonalizeHome(settings.optBoolean("personalizeHome", true));
		AppSettings.setSponsorblockEnabled(settings.optBoolean("sponsorblockEnabled", true));
		AppSettings.setPreloadOnMobile(settings.optBoolean("preloadOnMobile", false));
		AppSettings.setPreferAvc(settings.optBoolean("preferAvc", true));
		AppSettings.setAllowHdr(settings.optBoolean("allowHdr", false));
		AppSettings.setDownloadsWifiOnly(settings.optBoolean("downloadsWifiOnly", false));
		AppSettings.setDownloadsNightly(settings.optBoolean("downloadsNightly", false));
		AppSettings.setDataSaver(settings.optBoolean("dataSaver", false));
		String wifi = settings.optString("qualityWifi", null);
		if (isKnownQuality(wifi)) prefs.setQuality(false, wifi);
		String mobile = settings.optString("qualityMobile", null);
		if (isKnownQuality(mobile)) prefs.setQuality(true, mobile);
		prefs.setLoopMode(PlayerLoopMode.fromPersistedValue(settings.optInt("loopMode",
						prefs.getLoopMode().persistedValue())));
		prefs.setSubtitleEnabled(settings.optBoolean("subtitleEnabled", false));
		String subtitleLang = settings.optString("subtitleLanguage", null);
		if (subtitleLang != null && !subtitleLang.isBlank()) prefs.setSubtitleLanguage(subtitleLang);
		double speed = settings.optDouble("playbackSpeed", Double.NaN);
		if (!Double.isNaN(speed) && speed >= 0.25 && speed <= 3.0) prefs.setPlaybackSpeed((float) speed);
		int threshold = settings.optInt("watchedThreshold", -1);
		if (threshold >= ContentFilters.MIN_WATCHED_THRESHOLD && threshold <= ContentFilters.MAX_WATCHED_THRESHOLD) {
			filters.setWatchedThreshold(threshold);
		}
		aiChannels.setEnabled(settings.optBoolean("aiFilter", false));
		updates.setAutoCheck(settings.optBoolean("updateAutoCheck", true));
		updates.setPrereleases(settings.optBoolean("updatePrereleases", false));
		Set<String> cats = new HashSet<>();
		JSONArray catsJson = settings.optJSONArray("sponsorCats");
		if (catsJson != null) {
			for (int i = 0; i < catsJson.length(); i++) cats.add(catsJson.optString(i));
		}
		if (!cats.isEmpty()) prefs.setSponsorBlockCategories(cats);
		List<BlockedChannel> blocked = new ArrayList<>();
		JSONArray blockedJson = root.optJSONArray("blockedChannels");
		if (blockedJson != null) {
			for (int i = 0; i < blockedJson.length(); i++) {
				JSONObject entry = blockedJson.optJSONObject(i);
				if (entry == null) continue;
				String name = entry.optString("name", "");
				if (name.isBlank()) continue;
				blocked.add(new BlockedChannel(name, entry.optString("path", null),
								entry.optString("altPath", null)));
			}
		}
		filters.importBlocked(blocked);
		String csv = root.optString("subscriptionsCsv", "");
		if (!csv.isBlank()) {
			LocalSubscriptions.get().addAll(LocalSubscriptions.parseImport(csv));
		}
	}

	private static <T extends Enum<T>> T enumValue(@NonNull Class<T> type, @NonNull JSONObject settings,
	                                               @NonNull String key, @NonNull T fallback) {
		try {
			String name = settings.optString(key, null);
			if (name == null || name.isBlank()) return fallback;
			return Enum.valueOf(type, name);
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	/** VideoCache.parse is package-private; replicate it including the legacy names. */
	@NonNull
	private static AppSettings.VideoCache videoCacheOrDefault(@NonNull String stored) {
		switch (stored) {
			case "FAST":
				return AppSettings.VideoCache.LARGE;
			case "BALANCED":
				return AppSettings.VideoCache.MEDIUM;
			case "SAVER":
				return AppSettings.VideoCache.SMALL;
			default:
				break;
		}
		try {
			return AppSettings.VideoCache.valueOf(stored);
		} catch (IllegalArgumentException | NullPointerException e) {
			return AppSettings.VideoCache.LARGE;
		}
	}

	private static boolean isKnownQuality(@NonNull String quality) {
		for (String offered : PlayerPreferences.QUALITIES) {
			if (offered.equals(quality)) return true;
		}
		return false;
	}

	/** Human-readable one-liner for toasts and the file picker. */
	@NonNull
	public static String fileName() {
		return "testtube-backup.json";
	}

	/** Restart the app so the restored values take effect everywhere. */
	public static void restart(@NonNull Context context) {
		try {
			android.content.Intent launch = context.getPackageManager()
							.getLaunchIntentForPackage(context.getPackageName());
			if (launch != null) {
				launch.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
								| android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
				context.startActivity(launch);
			}
		} catch (RuntimeException ignored) {
		}
		Runtime.getRuntime().exit(0);
	}
}
