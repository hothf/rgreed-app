package de.ka.skyfallapp.ui.consensus.consensuslist

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A item decoration fo consensuses.
 */
class ConsensusItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.top = spacing
        
        if (parent.adapter != null && parent.getChildAdapterPosition(view) == (parent.adapter!!.itemCount - 1)) {
            outRect.bottom = spacing
        }
    }
}