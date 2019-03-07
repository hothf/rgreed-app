package de.ka.skyfallapp.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import de.ka.skyfallapp.R


/**
 * A custom layout for showing a toolbar.
 */
class AppToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(context, attrs, defStyleAttr) {

    /**
     * Lists all possible app toolbar states.
     */
    enum class AppToolbarState {

        /**
         * Shows the app action on the right, replaced  with a loading indicator
         */
        LOADING,

        /**
         * Shows the app action on the right
         */
        ACTION_VISIBLE,

        /**
         * Does not display the action on the right
         */
        NO_ACTION
    }

    private var navText: TextView
    private var navButton: ImageView
    private var navActionButton: Button
    private var navActionProgress: ProgressBar

    init {
        inflate(context, R.layout.layout_apptoolbar, this)

        navButton = findViewById(R.id.navImage)
        navText = findViewById(R.id.navText)
        navActionButton = findViewById(R.id.navActionButton)
        navActionProgress = findViewById(R.id.navActionProgress)

        hideAction()
    }

    /**
     * Sets a click listener for the navigation button on the left.
     */
    fun setNavButtonClickListener(listener: () -> Unit) {
        navButton.setOnClickListener { view ->
            view.closeAttachedKeyboard()
            listener()
        }
    }

    /**
     * Sets a text for the navigation bar on the top.
     */
    fun setNavText(text: String) {
        navText.text = text
    }

    /**
     * Sets the text of the action button.
     */
    fun setActionButtonText(text: String) {
        navActionButton.text = text
    }

    /**
     * Sets the click listener  of the action button.
     */
    fun setActionButtonClickListener(listener: () -> Unit) {
        navActionButton.setOnClickListener { view ->
            view.closeAttachedKeyboard()
            listener()
        }
    }

    /**
     * Updates the state of the toolbar. Mainly focuses on displaying / hiding / updating the action on the right.
     */
    fun updateState(state: AppToolbarState) {
        when (state) {
            AppToolbarState.LOADING -> showLoading()
            AppToolbarState.ACTION_VISIBLE -> showAction()
            AppToolbarState.NO_ACTION -> hideAction()

        }
    }

    private fun hideAction() {
        navActionButton.visibility = View.GONE
        navActionProgress.visibility = View.GONE
    }

    private fun showAction() {
        navActionButton.visibility = View.VISIBLE
        navActionProgress.visibility = View.GONE
    }

    private fun showLoading() {
        navActionButton.visibility = View.GONE
        navActionProgress.visibility = View.VISIBLE
    }


}