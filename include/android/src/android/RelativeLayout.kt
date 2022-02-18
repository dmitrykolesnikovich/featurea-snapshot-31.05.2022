package featurea.android

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

fun RelativeLayout.appendView(view: View, index: Int, x: Int, y: Int, width: Int, height: Int) {
    val relativeLayout = this
    val layoutParams = RelativeLayout.LayoutParams(width, height).apply {
        leftMargin = x
        topMargin = y
    }
    view.layoutParams = layoutParams
    val parent = view.parent as ViewGroup?
    if (parent == relativeLayout) {
        relativeLayout.updateViewLayout(view, layoutParams)
    } else {
        parent?.removeView(view)
        relativeLayout.addView(view, index)
    }
    view.invalidate()
    relativeLayout.requestLayout()
}

fun RelativeLayout.appendView(view: View, x: Int, y: Int, width: Int, height: Int) {
    val relativeLayout = this
    try {
        if (view.parent == null) {
            view.layoutParams = RelativeLayout.LayoutParams(width, height).apply {
                this.leftMargin = x
                this.topMargin = y
            }
            relativeLayout.addView(view)
        } else {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.apply {
                this.leftMargin = x
                this.topMargin = y
                this.width = width
                this.height = height
            }
            relativeLayout.requestLayout()
        }
        relativeLayout.invalidate()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}
