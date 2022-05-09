package featurea.utils

sealed class Scope {

    abstract val nest: Scope
    abstract val isSuper: Boolean

    object Inner : Scope() {
        override val nest = Inner
        override val isSuper = false
    }

    object Super : Scope() {
        override val nest = Outer
        override val isSuper = true
    }

    object Outer : Scope() {
        override val nest = Outer
        override val isSuper = false
    }

}
