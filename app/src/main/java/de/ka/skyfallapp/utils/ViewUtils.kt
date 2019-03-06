package de.ka.skyfallapp.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView

/**
 * Provides useful view utility classes and methods.
 */
object ViewUtils {

    class TextDoneListener(val block: () -> Unit = {}) : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.closeAttachedKeyboard()
                block()
                return true
            }
            return false
        }
    }

    /**
     * A text watcher only interested in text changes.
     *
     * @param textToUpdate the text to update on text changes
     */
    class TextChangeListener(val update: (String) -> Unit) : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            // not needed
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // not needed
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            update(text.toString())
        }
    }
}