package de.ka.rgreed.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.os.Handler
import android.widget.TextView
import de.ka.rgreed.R


/**
 * Represents a time aware text view. This is basically a [TextView] but with special handling for time format.
 */
class TimeAwareTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    private val animChecker = Handler()
    private val check = Runnable { setTimeAwareText(currentUpdate) }

    private var currentUpdate: TimeAwareUpdate? = null
    private var animator: ObjectAnimator? = null

    /**
     * Sets a text, aware of the time with the millis since the epoch.
     *
     * @param update the update containing the millis since epoch to be aware of and a formatting resource
     *
     */
    fun setTimeAwareText(update: TimeAwareUpdate?) {
        currentUpdate = update
        if (update == null) {
            return
        }
        val timeLeftSeconds = ((update.millisSinceEpoch - System.currentTimeMillis()) / 1_000)
        if (update.isInvalid || timeLeftSeconds <= 0 || timeLeftSeconds > ANIM_WHOLE_TRIGGER_THRESHOLD_SECONDS) {
            endAnimations()
            return
        }
        animChecker.postDelayed(check, 1_000)
        if (timeLeftSeconds <= ANIM_BLINKING_TRIGGER_THRESHOLD_SECONDS && (animator == null || !animator!!.isRunning)) {
            animator = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f).apply {
                duration = 1_000
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                start()
            }
        }
        text = String.format(
            context.getString(update.formatResId, getFineGrainedTimeText(timeLeftSeconds))
        )
    }

    private fun getFineGrainedTimeText(timeLeftSeconds: Long): String {
        return if (timeLeftSeconds >= 60 * 60) { //> 1 hour
            val hours = (timeLeftSeconds / (60 * 60))
            val minutes = timeLeftSeconds / 60 - (hours * 60)
            String.format(context.getString(R.string.date_time_and_hours), hours.toInt(), minutes.toInt())
        } else if (timeLeftSeconds < 60 * 60 && timeLeftSeconds > 120) {
            val minutes = timeLeftSeconds / 60
            String.format(context.getString(R.string.date_minutes), minutes.toInt())
        } else {
            String.format(context.getString(R.string.date_seconds), timeLeftSeconds)
        }
    }

    private fun endAnimations() {
        currentUpdate?.let {
            text = if (it.isInvalid) {
                String.format(context.getString(it.formatResId, context.getString(R.string.invalid_char)))
            } else {
                String.format(context.getString(it.formatResId, it.millisSinceEpoch.toDateTime()))
            }
        }
        animator?.end()
        animator = null
        animChecker.removeCallbacksAndMessages(null)
    }

    companion object {
        const val ANIM_WHOLE_TRIGGER_THRESHOLD_SECONDS = 2 * 60 * 60 // 2 hours
        const val ANIM_BLINKING_TRIGGER_THRESHOLD_SECONDS = 15 * 60 // 15 minutes
    }
}

/**
 * Responsible for updating the state of a [TimeAwareTextView].
 *
 * @param formatResId the res id of a formatted string for displaying the time aware text.
 * This has to offer one string argument
 * @param millisSinceEpoch the time to make the text aware of
 * @param isInvalid set to true to override everything to display the invalid character, defaults to false
 */
data class TimeAwareUpdate(val formatResId: Int, val millisSinceEpoch: Long, val isInvalid: Boolean = false)