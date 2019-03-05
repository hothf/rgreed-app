package de.ka.skyfallapp.utils

import android.animation.Animator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar

import android.widget.RelativeLayout

import com.google.android.material.textfield.TextInputEditText
import de.ka.skyfallapp.R

import android.view.inputmethod.EditorInfo
import android.widget.TextView

import android.view.KeyEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * A custom layout for showing a locked view.
 *
 * Can be animated to show the lock and to dismiss it. The states are changed with [updateState].
 *
 * Remember to put this at the end of your xml file, to be placed on top of other layout items.
 */
class LockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    /**
     * Listens for un unlock event requests.
     */
    interface UnlockListener {

        /**
         * Called on an unlock request.
         *
         * @param password the password to use to request an unlock
         */
        fun onUnlockRequested(password: String)
    }

    private var upperTileView: View
    private var lowerTileView: View
    private var currentPassword = ""
    private var lockImage: ImageView
    private var lockProgress: ProgressBar
    private var lockInput: TextInputEditText
    private var lockButton: FloatingActionButton
    private var listener: UnlockListener? = null

    /**
     * Lists all possible states of the layout.
     */
    enum class LockedViewState {
        HIDDEN,
        SHOW,
        HIDE,
        LOAD,
        ERROR
    }

    init {
        inflate(context, R.layout.layout_lock_view, this)

        this.setOnClickListener { /*do nothing, this will block the unwanted view interaction*/ }

        upperTileView = findViewById(R.id.upperLockTile)
        lowerTileView = findViewById(R.id.lowerLockTile)
        lockButton = findViewById(R.id.lockButton)
        lockImage = findViewById(R.id.lockImage)
        lockProgress = findViewById(R.id.lockProgress)
        lockInput = findViewById(R.id.lockInput)

        lockButton.setOnClickListener { listener?.onUnlockRequested(currentPassword) }
        lockInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                currentPassword = text.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                // do nothing
            }
        })

        lockInput.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    listener?.onUnlockRequested(currentPassword)
                    return true
                }
                return false
            }
        })

        updateState(LockedViewState.HIDDEN)
    }

    /**
     * Sets a listener for the only event of this view: a click on the button
     */
    fun setListener(listener: UnlockListener) {
        this.listener = listener
    }

    /**
     * Changes the state of the view to the new specified in [newState].
     */
    fun updateState(newState: LockedViewState) {
        when (newState) {
            LockedViewState.HIDDEN -> hideCompletely()
            LockedViewState.HIDE -> hideAnimated()
            LockedViewState.SHOW -> showAnimated()
            LockedViewState.LOAD -> showLoading()
            LockedViewState.ERROR -> showError()
        }
    }

    private fun hideCompletely() {
        this.visibility = View.INVISIBLE
        lockButton.visibility = View.VISIBLE
        lockProgress.visibility = View.GONE
    }


    private fun showAnimated() {
        this.visibility = View.VISIBLE

        upperTileView.translationY = -upperTileView.height.toFloat()
        lowerTileView.translationY = lowerTileView.height.toFloat()
        lockImage.scaleX = 0.0f
        lockImage.scaleY = 0.0f

        upperTileView.animate()
            .setDuration(DURATION_ANIMS_MS)
            .translationY(0.0f)
            .setInterpolator(DecelerateInterpolator())
            .setStartDelay(START_DELAY_MS)

        lowerTileView.animate()
            .setDuration(DURATION_ANIMS_MS)
            .translationY(0.0f)
            .setInterpolator(DecelerateInterpolator())
            .setStartDelay(START_DELAY_MS)

        lockImage.animate()
            .setDuration(DURATION_ANIMS_MS)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setInterpolator(OvershootInterpolator())
            .setStartDelay(START_DELAY_MS)
    }

    private fun hideAnimated() {
        if (visibility == View.INVISIBLE) {
            return
        }

        upperTileView.animate()
            .setDuration(DURATION_ANIMS_MS)
            .translationY(-lowerTileView.height.toFloat())
            .setInterpolator(DecelerateInterpolator())
        lowerTileView.animate()
            .setDuration(DURATION_ANIMS_MS)
            .translationY(lowerTileView.height.toFloat())
            .setInterpolator(DecelerateInterpolator())


        lockImage.animate()
            .scaleX(0.0f)
            .scaleY(0.0f)
            .setDuration(DURATION_ANIMS_MS * 2)
            .setListener(object :
                Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                    // not needed
                }

                override fun onAnimationCancel(p0: Animator?) {
                    // not needed
                }

                override fun onAnimationRepeat(p0: Animator?) {
                    // not needed
                }

                override fun onAnimationEnd(p0: Animator?) {
                    updateState(LockedViewState.HIDDEN)
                }
            })
    }

    private fun showLoading() {
        lockButton.visibility = View.GONE
        lockProgress.visibility = View.VISIBLE

        lockInput.closeAttachedKeyboard()
    }

    private fun showError() {
        lockButton.visibility = View.VISIBLE
        lockProgress.visibility = View.GONE
    }

    companion object {
        const val START_DELAY_MS = 300L
        const val DURATION_ANIMS_MS = 250L
    }

}