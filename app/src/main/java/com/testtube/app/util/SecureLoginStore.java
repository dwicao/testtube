package com.testtube.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Encrypted store for the login flag. Cookies themselves live in WebView's CookieManager
 * (OS-controlled SQLite) and can't be encrypted by the app — root can still read them,
 * which is documented. This at least keeps the app's own "signed in" bit out of plaintext MMKV.
 */
public final class SecureLoginStore {
	private static final String FILE = "testtube_login_secure";

	private SecureLoginStore() {
	}

	@NonNull
	public static SharedPreferences get(@NonNull Context context) {
		try {
			MasterKey key = new MasterKey.Builder(context)
							.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
							.build();
			return EncryptedSharedPreferences.create(
							context,
							FILE,
							key,
							EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
							EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
		} catch (Exception e) {
			// KeyStore broken (no lock screen, etc.): fall back to private prefs, still not MMKV.
			return context.getSharedPreferences(FILE + "_fallback", Context.MODE_PRIVATE);
		}
	}
}
