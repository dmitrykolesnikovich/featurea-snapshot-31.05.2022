package featurea

enum class Scope {

    INNER,
    SUPER,
    OUTER;

    fun isSuper(): Boolean = this == Scope.SUPER

    fun nest(): Scope {
        return when (this) {
            INNER -> INNER
            SUPER -> OUTER
            OUTER -> OUTER
        }
    }

}
