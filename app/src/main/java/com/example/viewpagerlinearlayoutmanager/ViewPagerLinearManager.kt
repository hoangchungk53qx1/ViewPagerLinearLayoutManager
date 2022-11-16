package com.example.viewpagerlinearlayoutmanager

import android.content.Context
import android.graphics.drawable.GradientDrawable.Orientation
import android.graphics.pdf.PdfDocument.Page
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by ChungHA , idea from Tiktok decode normal
 * The video control has three states: initialization, running, and destruction (INIT, SELECTED, RELEASE), corresponding to three interface methods
 * 1 Initialization/Destruction: When the child view enters/leaves the parent array layout ->
 * recyclerview.addOnChildAttachStateChangeListener{onChildViewAttachedToWindow{INIT}; onChildViewDetachedFromWindow{RELEASE}}
 * 2 run: when the scroll state is idle ->
 * linearLayoutManager.onScrollStateChanged(state: Int){*.IDLE -> SELECTED}
 */

class ViewPagerLinearManager(context: Context, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    private var deltaY: Int = 0
    private lateinit var onViewPagerListener: OnViewPagerListener
    private var recyclerView: RecyclerView? = null
    private var pagerSnapHelper: PagerSnapHelper = PagerSnapHelper()

    constructor(context: Context) : this(context, VERTICAL, false)

    init {
        pagerSnapHelper = PagerSnapHelper()
    }

    // When the layout manager is attached to the recyclerview
    override fun onAttachedToWindow(view: RecyclerView?) {
        pagerSnapHelper.attachToRecyclerView(view)
        this.recyclerView = view

        // Subview attach
        recyclerView?.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (childCount == 1) {
                    onViewPagerListener.onInitCompleted()
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                when (deltaY > 0) {
                    true -> {
                        onViewPagerListener.onPageRelease(true, getPosition(view))
                    }
                    false -> {
                        onViewPagerListener.onPageRelease(false, getPosition(view))
                    }
                }
            }
        })
        super.onAttachedToWindow(view)
    }

    // The process callback function when the RecyclerView sliding state changes
    override fun onScrollStateChanged(state: Int) {
        when (state) {
            RecyclerView.SCROLL_STATE_IDLE -> { // Normal playback state = non-drag state = idle state = when there is no hand movement
                val viewIdle = pagerSnapHelper.findSnapView(this) ?: return
                val posIdle = getPosition(viewIdle)
                if (childCount == 1) {
                    onViewPagerListener.onPageSelected(posIdle, posIdle == itemCount - 1)
                }
            }
        }
    }
    //The process callback when sliding in the vertical direction, dy > 0: the view is moved up and slid
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        this.deltaY = dy
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    fun setOnViewPagerListener(onViewPagerListener: OnViewPagerListener){
        this.onViewPagerListener = onViewPagerListener
    }

    interface OnViewPagerListener {
        fun onInitCompleted()
        fun onPageRelease(isNext: Boolean, position: Int)
        fun onPageSelected(position: Int, isBottom: Boolean)
    }
}