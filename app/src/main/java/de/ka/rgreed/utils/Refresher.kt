package de.ka.rgreed.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.animation.Animator
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import de.ka.rgreed.R

/**
 * Represents a refrehser. Basically a default button but animated in and out much cooler!
 */
class Refresher @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var isInistialized = false
    private var refresherButton: Button
    private var animatedTranslationY = 0.0f
    private var originTranslationY = 0.0f

    init {
        inflate(context, R.layout.layout_refresher, this)

        refresherButton = findViewById(R.id.refresherButton)

        post {
            animatedTranslationY =
                refresherButton.height.toFloat() + resources.getDimensionPixelSize(R.dimen.default_32)
            originTranslationY = 0.0f
            translationY = animatedTranslationY
            isInistialized = true
        }

    }

    fun setClickListener(clickListener: () -> Unit) {
        refresherButton.setOnClickListener { clickListener() }
    }

    /**
     * Responsible for showing or hinding the refresher.
     *
     * @param show set o true to show, false to hide the refresher
     */
    fun toggleRefresher(show: Boolean) {
        if (!isInistialized) {
            return
        }
        if (show) {
            animate().translationY(originTranslationY).setDuration(ANIM_TIME_MS)
                .setInterpolator(OvershootInterpolator())
        } else {
            animate().translationY(animatedTranslationY).setDuration(ANIM_TIME_MS)
                .setInterpolator(OvershootInterpolator())
        }
    }

    companion object {
        const val ANIM_TIME_MS = 300.toLong()
    }
}