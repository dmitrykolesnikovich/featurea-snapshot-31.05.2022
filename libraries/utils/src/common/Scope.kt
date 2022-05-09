package featurea.utils

sealed class Scope {

    abstract val nest: Scope

    object Inner : Scope() {
        override val nest = Inner
    }

    object Super : Scope() {
        override val nest = Outer
    }

    object Outer : Scope() {
        override val nest = Outer
    }

}
