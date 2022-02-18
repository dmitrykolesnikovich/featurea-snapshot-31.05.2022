package featurea.android.simulator

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

// https://gist.github.com/nwellis/bfc09c92de28147ffcd04747f9706f06

/**
 * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
 * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
 * background will be visible. That is rarely an desired effect.
 */

fun setupItemTouchHelper(activity: Activity, mRecyclerView: RecyclerView) {

    val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        // we want to cache these and not allocate anything repeatedly in the onChildDraw method
        lateinit var background: Drawable
        var xMark: Drawable? = null
        var xMarkMargin: Int = 0
        var initiated: Boolean = false

        private fun init() {
            background = ColorDrawable(Color.RED)
            xMark = ContextCompat.getDrawable(activity, R.drawable.ic_clear_24dp)
            xMark!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            xMarkMargin = activity.resources.getDimension(R.dimen.ic_clear_margin).toInt()
            initiated = true
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val swipedPosition = viewHolder.adapterPosition
            val adapter = mRecyclerView.adapter as FilesAdapter
            adapter.remove(swipedPosition)
        }

        override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val itemView = viewHolder.itemView

            // not sure why, but this method get's called for viewholder that are already swiped away
            if (viewHolder.adapterPosition == -1) return
            if (!initiated) init()

            // draw red background
            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
            background.draw(canvas)

            // draw x mark
            val itemHeight = itemView.bottom - itemView.top
            val intrinsicWidth = xMark!!.intrinsicWidth
            val intrinsicHeight = xMark!!.intrinsicWidth

            val xMarkLeft = itemView.right - xMarkMargin - intrinsicWidth
            val xMarkRight = itemView.right - xMarkMargin
            val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val xMarkBottom = xMarkTop + intrinsicHeight
            xMark!!.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)

            xMark!!.draw(canvas)

            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }
    val mItemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
    mItemTouchHelper.attachToRecyclerView(mRecyclerView)
}

/**
 * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
 * after an item is removed.
 */
fun setUpAnimationDecoratorHelper(mRecyclerView: RecyclerView) {
    mRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {

        // we want to cache this and not allocate anything repeatedly in the onDraw method
        lateinit var background: Drawable
        var initiated: Boolean = false

        private fun init() {
            background = ColorDrawable(Color.RED)
            initiated = true
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            if (!initiated) init()
            if (parent.itemAnimator!!.isRunning) {
                var lastViewComingDown: View? = null
                var firstViewComingUp: View? = null

                // this is fixed
                val left = 0
                val right = parent.width

                // this we need to find out
                var top = 0
                var bottom = 0

                // find relevant translating views
                val childCount = parent.layoutManager!!.childCount
                for (i in 0 until childCount) {
                    val child = parent.layoutManager!!.getChildAt(i)
                    if (child!!.translationY < 0) {
                        // view is coming down
                        lastViewComingDown = child
                    } else if (child.translationY > 0) {
                        // view is coming up
                        if (firstViewComingUp == null) {
                            firstViewComingUp = child
                        }
                    }
                }

                if (lastViewComingDown != null && firstViewComingUp != null) {
                    // views are coming down AND going up to fill the void
                    top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                    bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                } else if (lastViewComingDown != null) {
                    // views are going down to fill the void
                    top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                    bottom = lastViewComingDown.bottom
                } else if (firstViewComingUp != null) {
                    // views are coming up to fill the void
                    top = firstViewComingUp.top
                    bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                }

                background.setBounds(left, top, right, bottom)
                background.draw(c)

            }
            super.onDraw(c, parent, state)
        }

    })
}