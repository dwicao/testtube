package com.testtube.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.testtube.app.R;
import com.testtube.app.util.ViewUtils;

/**
 * Every dialog of the app starts here, so it gets the theme's dialog look: in the Niix theme the
 * thin pink edge of niixa.org, drawn along the dialog's rounded outline.
 */
public final class Dialogs {
	private static final float EDGE_DP = 1f;

	private Dialogs() {
	}

	@NonNull
	public static MaterialAlertDialogBuilder builder(@NonNull Context context) {
		return withEdge(new MaterialAlertDialogBuilder(context));
	}

	/**
	 * @param themeOverlay a dialog theme in place of the app's default one
	 */
	@NonNull
	public static MaterialAlertDialogBuilder builder(@NonNull Context context, @StyleRes int themeOverlay) {
		return withEdge(new MaterialAlertDialogBuilder(context, themeOverlay));
	}

	@NonNull
	private static MaterialAlertDialogBuilder withEdge(@NonNull MaterialAlertDialogBuilder builder) {
		Drawable background = builder.getBackground();
		Context themed = builder.getContext();
		// Only the Niix theme has an edge; the others leave it transparent
		int edge = ViewUtils.themeColor(themed, R.attr.ttGlassEdge);
		if (Color.alpha(edge) != 0 && background instanceof MaterialShapeDrawable shape) {
			shape.setStroke(EDGE_DP * themed.getResources().getDisplayMetrics().density, edge);
		}
		return builder;
	}
}
