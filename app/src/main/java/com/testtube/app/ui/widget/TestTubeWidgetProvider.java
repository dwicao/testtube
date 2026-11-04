package com.testtube.app.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.media3.common.util.UnstableApi;

import com.testtube.app.R;
import com.testtube.app.ui.MainActivity;

/**
 * Homescreen mini player: title, play/pause and next. Buttons reuse the same media-button
 * intents as the notification, so they work while backgrounded without a new exported API.
 */
public final class TestTubeWidgetProvider extends AppWidgetProvider {
	@UnstableApi
	@Override
	public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager manager, @NonNull int[] ids) {
		update(context, manager, ids, false, null, null);
	}

	/** Refreshes every placed widget; call from the playback service on play/pause/track change. */
	@UnstableApi
	public static void update(@NonNull Context context, @NonNull AppWidgetManager manager,
	                          @NonNull int[] ids, boolean isPlaying, @Nullable String title,
	                          @Nullable String artist) {
		if (ids.length == 0) return;
		for (int id : ids) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_player);
			views.setTextViewText(R.id.widget_title,
							title != null && !title.isBlank() ? title : context.getString(R.string.app_name));
			views.setTextViewText(R.id.widget_artist, artist != null ? artist : "");
			views.setImageViewResource(R.id.widget_play_pause,
							isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
			views.setContentDescription(R.id.widget_play_pause, context.getString(
							isPlaying ? R.string.action_pause : R.string.action_play));
			views.setOnClickPendingIntent(R.id.widget_play_pause,
							MediaButtonReceiver.buildMediaButtonPendingIntent(context,
											PlaybackStateCompat.ACTION_PLAY_PAUSE));
			views.setOnClickPendingIntent(R.id.widget_next,
							MediaButtonReceiver.buildMediaButtonPendingIntent(context,
											PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
			try {
				Intent launch = new Intent(context, MainActivity.class)
								.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(
								context, 2001, launch, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
			} catch (RuntimeException ignored) {
			}
			try {
				manager.updateAppWidget(id, views);
			} catch (RuntimeException ignored) {
			}
		}
	}

	@NonNull
	public static int[] placedIds(@NonNull Context context) {
		try {
			return AppWidgetManager.getInstance(context)
							.getAppWidgetIds(new ComponentName(context, TestTubeWidgetProvider.class));
		} catch (RuntimeException e) {
			return new int[0];
		}
	}
}
