package com.testtube.app.extractor;

import androidx.annotation.NonNull;

import com.testtube.app.net.Failure;

import java.util.HashSet;
import java.util.Set;

/**
 * Watches extraction health across videos to spot a YouTube-side breakage early (the Litube
 * pattern: extraction silently starts failing everywhere after a YouTube change).
 *
 * <p>Only unexpected extraction breakage counts ({@link Failure#OTHER}): removed, private,
 * region-locked or age-restricted videos are per-video facts, and offline/timeout/bot-check
 * are the network having a moment. Three <em>different</em> videos failing with OTHER in a row
 * means YouTube probably changed something, and the app should suggest checking for updates
 * instead of showing the same dead-end error again.
 */
public final class UpstreamHealth {
	private static final int STREAK_TO_SUGGEST = 3;
	private static final long SUGGEST_COOLDOWN_MS = 24L * 60 * 60 * 1000;

	@NonNull
	private static final Object LOCK = new Object();
	@NonNull
	private static final Set<String> streakVideos = new HashSet<>();
	private static long lastSuggestMs;

	private UpstreamHealth() {
	}

	/** A video extracted (and usually started) fine: the streak is over. */
	public static void recordSuccess() {
		synchronized (LOCK) {
			streakVideos.clear();
		}
	}

	/**
	 * An extraction finished with an error. Only {@link Failure#OTHER} extends the streak;
	 * anything else (network, per-video restrictions, bot check) leaves it alone.
	 */
	public static void recordFailure(@NonNull Failure failure, @NonNull String videoId) {
		if (failure != Failure.OTHER) return;
		synchronized (LOCK) {
			streakVideos.add(videoId);
		}
	}

	/** True when an update suggestion is due (streak reached, cooldown passed). */
	public static boolean shouldSuggestUpdate() {
		synchronized (LOCK) {
			return streakVideos.size() >= STREAK_TO_SUGGEST
							&& System.currentTimeMillis() - lastSuggestMs >= SUGGEST_COOLDOWN_MS;
		}
	}

	public static void markSuggested() {
		synchronized (LOCK) {
			lastSuggestMs = System.currentTimeMillis();
		}
	}
}
