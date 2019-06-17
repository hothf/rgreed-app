package de.ka.rgreed.ui.consensus.consensuslist

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A item decoration for consensuses.
 */
class ConsensusItemDecoration(private val spacingTop: Int, private val spacingLeftAndRight: Int = 0) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        // no position, leave it alone
        if (itemPosition == RecyclerView.NO_POSITION) {
            return
        }

        var spacingLeft = view.paddingLeft
        var spacingRight = view.paddingRight

        if (spacingLeftAndRight > 0) {
            spacingLeft = spacingLeftAndRight
            spacingRight = spacingLeftAndRight
        }

        if (itemPosition == 0) {    // first item
            outRect.set(spacingLeft, spacingTop/4, spacingRight, spacingTop)
        } else // every other item
            outRect.set(spacingLeft, view.paddingTop, spacingRight, spacingTop)
    }
}