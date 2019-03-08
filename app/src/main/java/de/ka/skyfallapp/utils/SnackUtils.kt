package de.ka.skyfallapp.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.events.ShowSnack

object SnackUtils {

    /**
     * Builds a new [Snackbar] snack.
     *
     * @param view the view to attach the snack
     * @param showSnackbarEvent the event to trigger and handle the snack
     *
     */
    fun build(view: View, showSnackbarEvent: ShowSnack): Snackbar = with(showSnackbarEvent) {
        val whiteSpan = ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.colorPrimary))
        val snackbarText = SpannableStringBuilder(message)
        snackbarText.setSpan(whiteSpan, 0, snackbarText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val snackbar = Snackbar.make(view, snackbarText, length)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(view.context, colorRes))

        snackbar.show()

        return snackbar
    }
}