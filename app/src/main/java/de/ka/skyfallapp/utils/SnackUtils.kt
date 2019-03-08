package de.ka.skyfallapp.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import de.ka.skyfallapp.base.events.ShowSnack

object SnackUtils {

    /**
     * Builds a new [Snackbar] snack.
     *
     * @param view the view to attach the snack
     * @param showSnackbar the event to trigger and handle the snack
     *
     */
    fun build(view: View, showSnackbar: ShowSnack): Snackbar = with(showSnackbar) {
        val text = SpannableStringBuilder(message)

        text.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(view.context, showSnackbar.type.textColorRes)),
            0,
            text.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        return Snackbar.make(view, text, length).apply {
            this.view.setBackgroundColor(ContextCompat.getColor(view.context, showSnackbar.type.backgroundColorRes))
        }
    }
}