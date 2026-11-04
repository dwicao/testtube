package com.testtube.app.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Keeps the error that closed the app, so the next start can show it and people can paste it
 * into a bug report instead of "it crashed". Stays on the phone; nothing is sent anywhere.
 */
public final class CrashLog {
	private static final String FILE = "last_crash.txt";

	private CrashLog() {
	}

	public static void install(@NonNull Context context) {
		Context app = context.getApplicationContext();
		File file = new File(app.getFilesDir(), FILE);
		Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
			try {
				StringWriter trace = new StringWriter();
				error.printStackTrace(new PrintWriter(trace));
				String version = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
				String report = "TestTube " + version + ", Android " + Build.VERSION.RELEASE
								+ " (" + Build.VERSION.SDK_INT + "), " + Build.MANUFACTURER + " " + Build.MODEL
								+ "\nThread: " + thread.getName() + "\n\n" + trace;
				Files.write(file.toPath(), report.getBytes(StandardCharsets.UTF_8));
			} catch (Throwable ignored) {
				// Writing the report must never hide the real error
			}
			if (previous != null) previous.uncaughtException(thread, error);
		});
	}

	/** The report of the last crash, once; null when the app closed normally. */
	@Nullable
	public static String take(@NonNull Context context) {
		File file = new File(context.getFilesDir(), FILE);
		if (!file.exists()) return null;
		try {
			return new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			return null;
		} finally {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}
	}

	/**
	 * Copies a crash report into Downloads/TestTube/ so it survives even when the app can't
	 * show its own dialog (e.g. it crashes again on the next start). Best effort, never throws.
	 *
	 * @return true when the copy landed somewhere readable
	 */
	public static boolean archiveToDownloads(@NonNull Context context, @NonNull String report) {
		try {
			String name = "TestTube-crash-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss",
							java.util.Locale.US).format(new java.util.Date()) + ".txt";
			byte[] bytes = report.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
				android.content.ContentValues values = new android.content.ContentValues();
				values.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, name);
				values.put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/plain");
				values.put(android.provider.MediaStore.Downloads.RELATIVE_PATH, "Download/TestTube/");
				android.net.Uri uri = context.getContentResolver().insert(
								android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
				if (uri == null) return false;
				try (java.io.OutputStream out = context.getContentResolver().openOutputStream(uri)) {
					if (out == null) return false;
					out.write(bytes);
				}
				return true;
			}
			java.io.File dir = new java.io.File(
							android.os.Environment.getExternalStoragePublicDirectory(
											android.os.Environment.DIRECTORY_DOWNLOADS), "TestTube");
			if (!dir.isDirectory() && !dir.mkdirs()) return false;
			java.io.File out = new java.io.File(dir, name);
			try (java.io.FileOutputStream stream = new java.io.FileOutputStream(out)) {
				stream.write(bytes);
			}
			return true;
		} catch (Throwable ignored) {
			return false;
		}
	}
}
