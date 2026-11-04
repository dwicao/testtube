package com.testtube.app.player.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;

import com.testtube.app.player.engine.Engine;
import com.testtube.app.util.StringUtils;

import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UnstableApi
public final class PlayerUtils {
	private static final Pattern FPS_SUFFIX = Pattern.compile("p(\\d+)$", Pattern.CASE_INSENSITIVE);

	public static boolean isPortrait(@NonNull Engine engine) {
		int videoWidth = engine.getVideoSize().width;
		int videoHeight = engine.getVideoSize().height;
		return videoWidth > 0 && videoHeight > 0 && videoHeight > videoWidth;
	}

	@NonNull
	public static List<VideoStream> filterBestStreams(@Nullable List<VideoStream> streams) {
		if (streams == null || streams.isEmpty()) return new ArrayList<>();

		Map<String, VideoStream> best = new HashMap<>();

		for (VideoStream stream : streams) {
			String res = stream.getResolution();
			String key = res + "#" + stream.getFps();
			VideoStream prev = best.get(key);

			if (prev == null || isBetterStream(stream, prev)) best.put(key, stream);
		}

		List<VideoStream> result = new ArrayList<>(best.values());
		result.sort((s1, s2) -> {
			int h1 = streamHeight(s1);
			int h2 = streamHeight(s2);
			if (h1 != h2) return Integer.compare(h2, h1);
			return Integer.compare(s2.getFps(), s1.getFps());
		});
		return result;
	}

	@NonNull
	public static List<String> sortResolutionLabels(@NonNull List<String> resolutions) {
		List<String> sorted = new ArrayList<>(resolutions);
		sorted.sort((left, right) -> {
			int height = Integer.compare(StringUtils.parseHeight(right), StringUtils.parseHeight(left));
			if (height != 0) return height;
			int fps = Integer.compare(parseFps(right), parseFps(left));
			if (fps != 0) return fps;
			return left.compareTo(right);
		});
		return sorted;
	}

	/** Height in pixels, read from the label ("1080p60") when the stream doesn't say. */
	public static int streamHeight(@NonNull VideoStream stream) {
		int height = stream.getHeight();
		return height > 0 ? height : StringUtils.parseHeight(stream.getResolution());
	}

	private static int parseFps(@Nullable String resolution) {
		if (resolution == null) return 0;
		Matcher matcher = FPS_SUFFIX.matcher(resolution);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
	}

	public static boolean isBetterStream(@NonNull VideoStream s1, @NonNull VideoStream s2) {
		int p1 = getCodecPriority(s1.getCodec());
		int p2 = getCodecPriority(s2.getCodec());
		if (p1 != p2) return p1 > p2;

		if (s1.getFps() != s2.getFps()) return s1.getFps() > s2.getFps();

		return s1.getBitrate() > s2.getBitrate();
	}

	public static int getCodecPriority(@Nullable String codec) {
		if (codec == null) return 0;
		// HDR renders blue on Samsung/Exynos firmware without proper tone mapping.
		// Always lose to any SDR codec so SDR is picked whenever it exists.
		if (isLikelyHdr(codec)) return -1;
		String lower = codec.toLowerCase(Locale.ROOT);
		if (lower.startsWith("avc") || lower.startsWith("h264")) return 4;
		if (lower.contains("vp9") || lower.contains("vp8")) return 3;
		if (lower.contains("h265")) return 2;
		if (lower.contains("av01")) return 1;

		return 0;
	}

	/**
	 * VP9 Profile 2 (vp09.02) is HDR10; AV1 10-bit (av01.*.10) is HDR. SDR is vp09.00 / av01.*.08.
	 * H.264 has no YouTube HDR. When unsure, assume SDR so nothing gets wrongly excluded.
	 */
	public static boolean isLikelyHdr(@Nullable String codec) {
		if (codec == null) return false;
		String lower = codec.toLowerCase(Locale.ROOT);
		if (lower.contains("vp09.02")) return true;
		if (lower.contains("av01") && (lower.endsWith(".10") || lower.contains(".10"))) return true;
		return false;
	}

	@Nullable
	public static VideoStream selectVideoStream(@Nullable List<VideoStream> streams, @Nullable String targetRes) {
		if (streams == null || streams.isEmpty()) return null;
		if (targetRes == null) return bestOf(streams);

		List<VideoStream> exact = new ArrayList<>();
		for (VideoStream s : streams) if (targetRes.equals(s.getResolution())) exact.add(s);
		if (!exact.isEmpty()) return bestOf(exact);

		int targetHeight = StringUtils.parseHeight(targetRes);
		List<VideoStream> fitting = new ArrayList<>();
		for (VideoStream s : streams) {
			int h = s.getHeight() > 0 ? s.getHeight() : StringUtils.parseHeight(s.getResolution());
			if (h > 0 && h <= targetHeight) fitting.add(s);
		}
		if (!fitting.isEmpty()) return bestOf(fitting);

		return bestOf(streams);
	}

	@Nullable
	private static VideoStream bestOf(@NonNull List<VideoStream> streams) {
		VideoStream best = null;
		for (VideoStream s : streams) {
			if (best == null || isBetterStream(s, best)) best = s;
		}
		return best;
	}

	/** The first audio track, which is the one YouTube plays by default. */
	@Nullable
	public static AudioStream defaultAudioStream(@Nullable List<AudioStream> streams) {
		return streams == null || streams.isEmpty() ? null : streams.get(0);
	}
}
