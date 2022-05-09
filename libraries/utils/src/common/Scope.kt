package featurea.utils

sealed class Scope(val nest: () -> Scope) {
    object Inner : Scope({ Inner })
    object Super : Scope({ Outer })
    object Outer : Scope({ Outer })
}
