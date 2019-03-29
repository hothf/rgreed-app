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
import androidx.annotation.ColorRes
import de.ka.skyfallapp.R


/**
 * Represents a snacker. This is some kind of snack bar, but much cooler!
 */
class Snacker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    enum class SnackType(@ColorRes val textColorRes: Int, @ColorRes val backgroundColorRes: Int) {
        DEFAULT(R.color.snackDefaultTextColor, R.color.snackDefaultBackground),
        WARNING(R.color.snackWarningTextColor, R.color.snackWarningBackground),
        ERROR(R.color.snackErrorTextColor, R.color.snackErrorBackground)
    }

    private val snackHandler = Handler()

    private var isHidingStopped = false
    private var container: View

    init {
        inflate(context, R.layout.layout_snacker, this)

        container = findViewById(R.id.snacker)
        visibility = View.INVISIBLE


        post {
            createCenteredHide(container)
            createCenteredReveal(container)
        }
    }

    fun reveal() {
        snackHandler.removeCallbacksAndMessages(null)
        isHidingStopped = true

        visibility = View.VISIBLE
        createCenteredReveal(container)

        snackHandler.postDelayed({
            isHidingStopped = false
            createCenteredHide(container)
        }, 1_000)
    }

    // one shot animations, they can not be reused!
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


    }
}