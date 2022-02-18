package featurea.layout

class Layout {

    val views: MutableList<View> = mutableListOf()

    fun addView(view: View) {
        if (views.contains(view)) error("duplicate view: $view")
        views.add(view)
    }

    fun removeView(view: View) {
        views.remove(view)
    }

}
