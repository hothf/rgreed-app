package de.ka.skyfallapp.utils

import android.content.Context
import android.util.AttributeSet
import de.ka.skyfallapp.R

class RefreshSwipeView(context: Context, attrs: AttributeSet?) :
    androidx.swiperefreshlayout.widget.SwipeRefreshLayout(context, attrs) {

    init {
        setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)
    }
}