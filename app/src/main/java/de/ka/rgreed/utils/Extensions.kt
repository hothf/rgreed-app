package de.ka.rgreed.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import de.ka.rgreed.repo.api.models.ConsensusResponse
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

/**
 * Adds consensuses to a list when the ids of the new consensus entry list does not have a duplicate.
 */
fun MutableList<ConsensusResponse>.addAllUniqueIds(newList: List<ConsensusResponse>) {
    newList.distinctBy { it.id }.forEach { newListEntry ->
        val duplicate = this.find { it.id == newListEntry.id }

        if (duplicate != null) {
            this.remove(duplicate)
        }

        this.add(newListEntry)
    }
}