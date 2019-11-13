package com.streamliner.base_architecture

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Extends [SwipeRefreshLayout] to support non-direct descendant scrolling views.
 *
 * [SwipeRefreshLayout] works as expected
 */
class ScrollChildSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
    ) : SwipeRefreshLayout (context, attrs){

    var scrollUpChild: View? = null

    override fun canChildScrollUp(): Boolean = scrollUpChild?.canScrollVertically(-1) ?: super.canChildScrollUp()
}