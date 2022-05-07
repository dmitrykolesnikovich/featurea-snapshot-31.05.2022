package featurea.utils

enum class Scope {

    INNER,
    SUPER,
    OUTER;

    fun isSuper(): Boolean = this == SUPER

    fun nest(): Scope {
        return when (this) {
            INNER -> INNER
            SUPER -> OUTER
            OUTER -> OUTER
        }
    }

}
