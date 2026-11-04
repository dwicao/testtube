package com.testtube.app.history;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates {@link WatchHistory} entries into the numbers shown on the Library's Stats tab.
 * Entries recorded before durations were stored count as videos but not as watch time.
 */
public final class WatchStats {
	private WatchStats() {
	}

	public record Channel(@NonNull String name, int videos) {
	}

	public record Summary(int videos, long secondsKnown, int trackedVideos, int days,
	                      @NonNull List<Channel> topChannels) {
	}

	@NonNull
	public static Summary summarize(@NonNull List<WatchHistory.Entry> entries) {
		Map<String, Integer> perChannel = new HashMap<>();
		Map<LocalDate, Boolean> days = new HashMap<>();
		ZoneId zone = ZoneId.systemDefault();
		int tracked = 0;
		long seconds = 0L;
		for (WatchHistory.Entry entry : entries) {
			if (entry == null) continue;
			if (entry.durationSec() > 0) {
				tracked++;
				seconds += entry.durationSec();
			}
			String author = entry.author();
			if (author != null && !author.isBlank()) {
				perChannel.put(author, perChannel.getOrDefault(author, 0) + 1);
			}
			try {
				days.put(Instant.ofEpochMilli(entry.watchedAt()).atZone(zone).toLocalDate(), Boolean.TRUE);
			} catch (RuntimeException ignored) {
			}
		}
		List<Channel> channels = new ArrayList<>();
		for (Map.Entry<String, Integer> e : perChannel.entrySet()) {
			channels.add(new Channel(e.getKey(), e.getValue()));
		}
		channels.sort((a, b) -> Integer.compare(b.videos(), a.videos()));
		while (channels.size() > 5) channels.remove(channels.size() - 1);
		return new Summary(entries.size(), seconds, tracked, days.size(), channels);
	}
}
