package com.testtube.app.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.testtube.app.R;

public final class ViewUtils {
	private static final float ALPHA_VISIBLE = 1.0f;
	private static final float ALPHA_INVISIBLE = 0.0f;
	private static final long FADE_IN_MS = 160L;
	/** 38%, Material's opacity for disabled content */
	private static final int DISABLED_ALPHA = 97;

	private ViewUtils() {
	}

	/** A colour of the current theme, such as {@code R.attr.colorPrimary}. */
	@ColorInt
	public static int themeColor(@NonNull Context context, @AttrRes int attr) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(attr, value, true);
		return value.data;
	}

	/**
	 * Colours an on/off option button (Video, Audio only, Subtitles…): filled in the accent with
	 * its text in the colour on the accent when on, a quiet grey with normal text when off, faded
	 * when disabled. Text and fill change together, so the label stays readable in both themes.
	 */
	public static void paintToggle(@NonNull TextView button, boolean on) {
		Context context = button.getContext();
		int fill = themeColor(context, on
						? androidx.appcompat.R.attr.colorPrimary
						: com.google.android.material.R.attr.colorSurfaceContainerHighest);
		int text = themeColor(context, on
						? com.google.android.material.R.attr.colorOnPrimary
						: com.google.android.material.R.attr.colorOnSurface);
		button.setBackgroundTintList(fadedWhenDisabled(fill));
		button.setTextColor(fadedWhenDisabled(text));
	}

	@NonNull
	private static ColorStateList fadedWhenDisabled(int color) {
		return new ColorStateList(
						new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}},
						new int[]{ColorUtils.setAlphaComponent(color, DISABLED_ALPHA), color});
	}

	/**
	 * The theme's typeface (ttFontRegular: the system font, or JetBrains Mono in the NiiX theme)
	 * in the given style, such as {@link Typeface#BOLD}. For text styled in code, because
	 * {@code setTypeface(null, style)} quietly falls back to the system font. Pain in the ass.
	 */
	@NonNull
	public static Typeface appTypeface(@NonNull Context context, int style) {
		Typeface family = null;
		TypedValue value = new TypedValue();
		if (context.getTheme().resolveAttribute(R.attr.ttFontRegular, value, true)) {
			if (value.resourceId != 0) {
				try {
					family = ResourcesCompat.getFont(context, value.resourceId);
				} catch (Resources.NotFoundException e) {
					// A bundled font that can't be read: the system font below
				}
			} else if (value.string != null) {
				family = Typeface.create(value.string.toString(), Typeface.NORMAL);
			}
		}
		return Typeface.create(family != null ? family : Typeface.DEFAULT, style);
	}

	/** Gives a TextView's icons (drawableStart, drawableTop…) one square size. */
	public static void sizeCompoundDrawables(@NonNull TextView view, float sizeDp) {
		int size = dpToPx(view.getContext(), sizeDp);
		Drawable[] drawables = view.getCompoundDrawablesRelative();
		for (Drawable drawable : drawables) {
			if (drawable != null) drawable.setBounds(0, 0, size, size);
		}
		view.setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawables[3]);
	}

	/** Shows a view that was just swapped in with a short fade, so screens don't pop. */
	public static void fadeIn(@NonNull View view) {
		view.animate().cancel();
		view.setAlpha(ALPHA_INVISIBLE);
		view.animate().alpha(ALPHA_VISIBLE).setDuration(FADE_IN_MS).start();
	}

	public static int dpToPx(@NonNull Context context, float dp) {
		return (int) (dp * context.getResources().getDisplayMetrics().density);
	}

	public static int getScreenWidth(@NonNull Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static void animateViewAlpha(@NonNull View v, float alpha, int visibilityIfGone) {
		if (Float.compare(alpha, ALPHA_VISIBLE) == 0) {
			v.animate().cancel();
			v.setAlpha(ALPHA_VISIBLE);
			v.setVisibility(View.VISIBLE);
		} else if (v.getVisibility() != View.VISIBLE) {
			v.setAlpha(ALPHA_INVISIBLE);
			v.setVisibility(visibilityIfGone);
		} else {
			v.setVisibility(View.VISIBLE);
			v.animate().alpha(alpha).setDuration(100L).withEndAction(() -> {
				if (Float.compare(alpha, ALPHA_INVISIBLE) == 0) {
					v.setVisibility(visibilityIfGone);
				}
			}).start();
		}
	}

	public static void setFullscreen(@NonNull View view, boolean fullscreen) {
		if (fullscreen) {
			view.setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_LOW_PROFILE
											| View.SYSTEM_UI_FLAG_FULLSCREEN
											| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
											| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
											| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
											| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			);
		} else {
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}
	}
}
