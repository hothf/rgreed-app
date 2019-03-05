package de.ka.skyfallapp.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * If the soft keyboard is currently open because of this view, it will be closed.
 */
fun View?.closeAttachedKeyboard() {
    this?.let { view ->
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}