package featurea.utils

typealias Specifier = String

interface Specified {
    val specifier: Specifier
}

inline fun <reified T : Enum<T>> enumValueBySpecifierOrNull(specifier: Specifier?): T? {
    val values = enumValues<T>()
    for (value in values) if (value is Specified) if (value.specifier == specifier) return value
    return null
}

inline fun <reified T : Enum<T>> enumValueBySpecifier(specifier: Specifier): T =
    enumValueBySpecifierOrNull<T>(specifier) ?: throw IllegalArgumentException("specifier: $specifier")

inline fun <reified T : Enum<T>> Specifier.toEnumValue(): T =
    enumValueBySpecifier(this)

inline fun <reified T : Enum<T>> Specifier.toEnumValueOrNull(): T? =
    enumValueBySpecifierOrNull<T>(this)
