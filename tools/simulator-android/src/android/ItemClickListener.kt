package featurea.android.simulator

@FunctionalInterface
interface ItemClickListener<T> {
    fun onClickItem(item: T)
}
