package de.ka.skyfallapp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.view.ViewAnimationUtils
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Rect
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.events.ShowSnack

/**
 * Represents a snacker. This is some kind of snack bar, but much cooler!
 */
class Snacker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    enum class SnackType(@ColorRes val textColorRes: Int, @DrawableRes val backgroundRes: Int) {
        DEFAULT(R.color.snackDefaultTextColor, R.drawable.bg_snacker_default),
        WARNING(R.color.snackWarningTextColor, R.drawable.bg_snacker_warning),
        ERROR(R.color.snackErrorTextColor, R.drawable.bg_snacker_error)
    }

    private val snackHandler = Handler()

    private var isHidingStopped = false
    private var snackText: TextView
    private var container: View

    init {
        inflate(context, R.layout.layout_snacker, this)

        container = findViewById(R.id.snacker)
        snackText = findViewById(R.id.snackText)
        visibility = View.INVISIBLE

        post {
            createCenteredHide(container)
            createCenteredReveal(container)
        }
    }

    /**
     * Reveals the snacker. Will auto dismiss itself. A snacker can be revealed several times.
     */
    fun reveal(showSnack: ShowSnack) {
        snackHandler.removeCallbacksAndMessages(null)
        isHidingStopped = true

        container.setBackgroundResource(showSnack.type.backgroundRes)
        snackText.setTextColor(ContextCompat.getColor(context, showSnack.type.textColorRes))
        snackText.text = showSnack.message

        visibility = View.VISIBLE
        createCenteredReveal(container)

        snackHandler.postDelayed({
            isHidingStopped = false
            createCenteredHide(container)
        }, HIDE_TIME_MS)
    }

    // caution: these are one shot animations, they can not be reused / stopped / paused!
    private fun createCenteredReveal(view: View) {
        val bounds = Rect()
        view.getDrawingRect(bounds)
        val finalRadius = Math.max(bounds.width(), bounds.height())
        ViewAnimationUtils.createCircularReveal(view, bounds.centerX(), bounds.centerY(), 0f, finalRadius.toFloat())
            .start()
    }

    private fun createCenteredHide(view: View) {
        val bounds = Rect()
        view.getDrawingRect(bounds)
        val initialRadius = view.width / 2

        ViewAnimationUtils.createCircularReveal(
            view,
            bounds.centerX(),
            bounds.centerY(),
            initialRadius.toFloat(),
            0f
        ).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!isHidingStopped) {
                        visibility = View.INVISIBLE
                    }
                }
            })
            start()
        }
    }

    companion object {
        const val HIDE_TIME_MS = 2_500.toLong()
    }
}