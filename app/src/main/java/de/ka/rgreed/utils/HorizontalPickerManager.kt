package de.ka.rgreed.utils

import android.content.Context

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager


/**
 * A layout manager for picking a horizontal value.
 *
 * Created by adityagohad on 06/06/17. Modified and converted by Thomas Hofmann on 03/12/2019
 *
 * Taken from https://github.com/adityagohad/HorizontalPicker/blob/master/horizontalpickerlib/src/main/java/travel/ithaka/android/horizontalpickerlib/PickerLayoutManager.java
 *
 * Converted to kotlin.
 */
class HorizontalPickerManager(context: Context, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    var scaleDownBy = 0.66f
    var scaleDownDistance = 0.9f
    var isChangeAlpha = true
    var itemSizeResId = android.R.dimen.app_icon_size

    var onSelected: ((Int) -> Unit)? = null

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        scaleDownView()
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val orientation = orientation
        return if (orientation == LinearLayoutManager.HORIZONTAL) {
            val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
            scaleDownView()
            scrolled
        } else 0
    }

    private fun scaleDownView() {
        val mid = width / 2.0f
        val unitScaleDownDist = scaleDownDistance * mid
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childMid = (getDecoratedLeft(child!!) + getDecoratedRight(child)) / 2.0f
            val scale =
                1.0f + -1 * scaleDownBy * Math.min(unitScaleDownDist, Math.abs(mid - childMid)) / unitScaleDownDist
            child.scaleX = scale
            child.scaleY = scale
            if (isChangeAlpha) {
                child.alpha = scale
            }
        }
    }

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        val displayMetrics = view!!.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val padding = (screenWidth - view.resources.getDimensionPixelSize(itemSizeResId)) / 2
        view.setPadding(padding, 0, padding, 0)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == 0) {
            var selected = 0
            var lastHeight = 0f
            for (i in 0 until childCount) {
                if (lastHeight < getChildAt(i)!!.scaleY) {
                    lastHeight = getChildAt(i)!!.scaleY
                    selected = i
                }
            }
            getChildAt(selected)?.let {
                onSelected?.invoke(getPosition(it))
            }
        }
    }
}