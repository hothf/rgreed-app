package de.ka.skyfallapp.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import com.google.android.material.snackbar.Snackbar
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.events.ShowSnack

object SnackUtils {

    /**
     * Builds a new [Snackbar] snack.
     *
     * @param view the view to attach the snack
     * @param showSnackbar the event to trigger and handle the snack
     *
     */
    fun build(view: View, showSnackbar: ShowSnack, usePadding: Boolean = false): Snackbar = with(showSnackbar) {
        val text = SpannableStringBuilder(message)

        text.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(view.context, showSnackbar.type.textColorRes)),
            0,
            text.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        return Snackbar.make(view, text, length).apply {
            if (usePadding) {
                this.view.setPadding(0, 0, 0, view.context.resources.getDimensionPixelSize(R.dimen.bottomBarSize))
            } else {
                this.view.setPadding(0, 0, 0, 0)
            }

            this.view.setBackgroundColor(ContextCompat.getColor(view.context, showSnackbar.type.backgroundColorRes))
        }
    }
}