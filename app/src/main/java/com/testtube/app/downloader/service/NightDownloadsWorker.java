package com.testtube.app.downloader.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.testtube.app.AppSettings;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Resumes paused downloads once a night on unmetered networks. Scheduled while
 * Settings → Playback → "Night downloads" is on; the 02:00 time keeps the radio
 * and the battery bill out of the way.
 */
public final class NightDownloadsWorker extends Worker {
	private static final String WORK_NAME = "night-downloads";
	/** Local time the nightly run fires. */
	private static final int RUN_HOUR = 2;

	public NightDownloadsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
		super(context, params);
	}

	@NonNull
	@Override
	public Result doWork() {
		if (!AppSettings.downloadsNightly()) return Result.success();
		try {
			Intent resume = new Intent(getApplicationContext(), DownloadService.class)
							.setAction(DownloadService.ACTION_RESUME_ALL);
			getApplicationContext().startService(resume);
		} catch (RuntimeException ignored) {
			return Result.retry();
		}
		return Result.success();
	}

	/** (Re)schedules the 02:00 run, or cancels it when switched off. Idempotent. */
	public static void apply(@NonNull Context context) {
		WorkManager work = WorkManager.getInstance(context.getApplicationContext());
		if (!AppSettings.downloadsNightly()) {
			work.cancelUniqueWork(WORK_NAME);
			return;
		}
		Constraints constraints = new Constraints.Builder()
						.setRequiredNetworkType(NetworkType.UNMETERED)
						.setRequiresBatteryNotLow(true)
						.build();
		PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
						NightDownloadsWorker.class, 24L, TimeUnit.HOURS)
						.setConstraints(constraints)
						.setInitialDelay(delayUntilNextRun(), TimeUnit.MILLISECONDS)
						.addTag(WORK_NAME)
						.build();
		work.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
	}

	private static long delayUntilNextRun() {
		Calendar next = Calendar.getInstance();
		next.set(Calendar.HOUR_OF_DAY, RUN_HOUR);
		next.set(Calendar.MINUTE, 0);
		next.set(Calendar.SECOND, 0);
		next.set(Calendar.MILLISECOND, 0);
		if (next.getTimeInMillis() <= System.currentTimeMillis()) {
			next.add(Calendar.DAY_OF_YEAR, 1);
		}
		return Math.max(0L, next.getTimeInMillis() - System.currentTimeMillis());
	}
}
