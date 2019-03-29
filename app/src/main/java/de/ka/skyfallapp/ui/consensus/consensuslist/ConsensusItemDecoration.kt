package de.ka.skyfallapp.ui.consensus.consensuslist

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A item decoration fo consensuses.
 */
class ConsensusItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        // no position, leave it alone
        if (itemPosition == RecyclerView.NO_POSITION) {
            return
        }

        if (itemPosition == 0) {    // first item
            outRect.set(view.paddingLeft, spacing, view.paddingRight, spacing)
        } else // every other item
            outRect.set(view.paddingLeft, view.paddingTop, view.paddingRight, spacing)
        }
}