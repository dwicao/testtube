package com.testtube.app.player.engine;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.ExoPlayer;

import com.testtube.app.R;
import com.testtube.app.extractor.PlaybackDetails;
import com.testtube.app.extractor.PlaybackPlan;
import com.testtube.app.extractor.PlaybackPlanner;
import com.testtube.app.util.StringUtils;

import java.util.function.Supplier;

/**
 * The player of the Shorts tab. Separate from the main player so a Short never touches its queue,
 * history or mini player, but it builds the streams the same way.
 * <p>
 * A single player plus a cache prefetch: while one Short plays, the next one's first seconds are
 * written into the player cache, so a swipe starts at once without keeping a second
 * {@link ExoPlayer} (and its decoders, buffers and radio use) alive.
 */
@UnstableApi
public final class ShortsEngine {
	/** Shorts don't change quality while playing; more than this mostly costs time to start. */
	private static final int MAX_HEIGHT = 720;

	/** Called on the main thread, for the player that's on screen only. */
	public interface Listener {
		void onFirstFrame();

		void onPlayingChanged(boolean playing);

		void onVideoSize(@NonNull VideoSize size);

		void onError(@NonNull PlaybackException error);
	}

	@NonNull
	private final PlayerDataSource sources;
	@NonNull
	private final String autoSubtitleFormat;
	@NonNull
	private final Supplier<String> quality;
	@NonNull
	private final java.util.function.BooleanSupplier isMobile;
	@NonNull
	private ExoPlayer player;
	@Nullable
	private Listener listener;
	@NonNull
	private final StreamPreloader preloader;

	public ShortsEngine(@NonNull Context context, @Nullable SimpleCache cache, @NonNull Supplier<String> quality,
			@NonNull java.util.function.BooleanSupplier isMobile) {
		this.sources = new PlayerDataSource(cache);
		this.autoSubtitleFormat = context.getString(R.string.subtitle_auto_generated);
		this.quality = quality;
		this.isMobile = isMobile;
		this.preloader = new StreamPreloader(sources.preloadSource());
		this.player = build(context);
	}

	public ShortsEngine(@NonNull Context context, @Nullable SimpleCache cache, @NonNull Supplier<String> quality) {
		this(context, cache, quality, () -> false);
	}

	@NonNull
	private ExoPlayer build(@NonNull Context context) {
		ExoPlayer exo = new ExoPlayer.Builder(context)
						.setLoadControl(PlayerLoadControl.create())
						.setAudioAttributes(new AudioAttributes.Builder()
										.setUsage(C.USAGE_MEDIA)
										.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
										.build(), true)
						.setHandleAudioBecomingNoisy(true)
						.setUsePlatformDiagnostics(false)
						.build();
		// Shorts loop until the next one is swiped in
		exo.setRepeatMode(Player.REPEAT_MODE_ONE);
		// No captions over Shorts (the extraction comes with every subtitle track)
		exo.setTrackSelectionParameters(exo.getTrackSelectionParameters().buildUpon()
						.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
						.build());
		exo.addListener(new Player.Listener() {
			@Override
			public void onRenderedFirstFrame() {
				if (exo == player && listener != null) listener.onFirstFrame();
			}

			@Override
			public void onIsPlayingChanged(boolean isPlaying) {
				if (exo == player && listener != null) listener.onPlayingChanged(isPlaying);
			}

			@Override
			public void onVideoSizeChanged(@NonNull VideoSize size) {
				if (exo == player && listener != null) listener.onVideoSize(size);
			}

			@Override
			public void onPlayerError(@NonNull PlaybackException error) {
				if (listener != null) listener.onError(error);
			}
		});
		return exo;
	}

	/** The player on screen; a single instance for the whole tab. */
	@NonNull
	public Player player() {
		return player;
	}

	public void setListener(@Nullable Listener listener) {
		this.listener = listener;
	}

	/**
	 * Starts a Short from the beginning. The next Short's start is usually already in the player
	 * cache (see {@link #preload}), so this starts at once.
	 *
	 * @throws IllegalArgumentException when the extraction has nothing playable
	 */
	public void play(@NonNull PlaybackDetails details) {
		preloader.cancel();
		player.setMediaSource(PlaybackSourceFactory.create(sources, autoSubtitleFormat, details, plan(details)));
		player.prepare();
		player.setPlayWhenReady(true);
	}

	/** Caches the start of a Short in the background so swiping to it starts at once. */
	public void preload(@NonNull PlaybackDetails details) {
		String id = details.video().getId();
		if (id == null) return;
		// On mobile data a full preload costs battery + data; the swipe still plays from
		// extraction, just without the head start — unless the user opted into mobile preload.
		try {
			if (isMobile.getAsBoolean() && !com.testtube.app.AppSettings.preloadOnMobile()) return;
		} catch (RuntimeException ignored) {
		}
		// Prefetch with the same capped plan playback uses, so the cached URLs match.
		try {
			preloader.preload(new PlaybackDetails(details.video(), details.catalog(),
							details.deliveries(), plan(details), details.segments(), details.subtitles()));
		} catch (RuntimeException ignored) {
			// A preload must never break playback; play() extracts again anyway
		}
	}

	@NonNull
	private PlaybackPlan plan(@NonNull PlaybackDetails details) {
		String preferred = quality.get();
		int height = StringUtils.parseHeight(preferred);
		String capped = height > 0 && height <= MAX_HEIGHT ? preferred : MAX_HEIGHT + "p";
		return PlaybackPlanner.plan(details.deliveries(), capped);
	}

	public void resume() {
		if (player.getPlaybackState() == Player.STATE_IDLE && player.getMediaItemCount() > 0) player.prepare();
		player.setPlayWhenReady(true);
	}

	public void pause() {
		player.setPlayWhenReady(false);
	}

	public boolean isPlaying() {
		return player.getPlayWhenReady();
	}

	public void stop() {
		player.stop();
		player.clearMediaItems();
	}

	/** Played share of the Short, 0 to 1. */
	public float progress() {
		long duration = player.getDuration();
		if (duration == C.TIME_UNSET || duration <= 0) return 0f;
		return Math.max(0f, Math.min(1f, player.getCurrentPosition() / (float) duration));
	}

	public void release() {
		listener = null;
		try {
			preloader.release();
		} catch (RuntimeException ignored) {
		}
		player.release();
	}
}
