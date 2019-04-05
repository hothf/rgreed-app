package de.ka.rgreed.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.DateFormat
import java.util.*


fun Long.toDate(): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(this))

fun Long.toTime(): String = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(this))

fun Long.toDateTime(): String = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(this))


/**
 * If the soft keyboard is currently open because of this view, it will be closed.
 */
fun View?.closeAttachedKeyboard() {
    this?.let { view ->
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

/**
 * If the soft keyboard is currently not shown because of this view, it will be shown.
 */
fun View?.showAttachedKeyboard() {
    this?.let { view ->
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}