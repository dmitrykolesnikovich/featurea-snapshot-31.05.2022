package featurea.jvm

import java.lang.reflect.Field
import java.lang.reflect.Method

fun type(canonicalName: String): Class<*> = typeOrNull(canonicalName)!!

fun typeOrNull(canonicalName: String): Class<*>? {
    try {
        return Class.forName(canonicalName)
    } catch (e: ClassNotFoundException) {
        return null
    }
}

fun Any?.field(fieldName: String): Field = fieldOrNull(fieldName)!!

fun Any?.fieldOrNull(fieldName: String): Field? {
    if (this == null) return null
    val canonicalName = this::class.java.canonicalName ?: return null
    return typeOrNull(canonicalName).field(fieldName)
}

fun field(canonicalClassName: String, fieldName: String): Field = typeOrNull(canonicalClassName).field(fieldName)

fun Class<*>?.field(name: String): Field = fieldOrNull(name)!!

fun <T> Field.staticValue() = get(null) as T

fun Class<*>?.fieldOrNull(name: String): Field? {
    if (this == null) return null
    return getDeclaredField(name)
        .apply { isAccessible = true }
}

fun Class<*>?.method(name: String): Method = methodOrNull(name)!!

fun Class<*>?.methodOrNull(name: String): Method? {
    if (this == null) return null
    return methods.find { it.name == name }
}

fun Class<*>?.methodReturnType(name: String): Class<*>? = methodReturnTypeOrNull(name)!!

fun Class<*>?.methodReturnTypeOrNull(name: String): Class<*>? {
    return methodOrNull(name)?.returnType
}

fun Class<*>?.enumConstant(name: String): Any? = enumConstantOrNull(name)

fun Class<*>?.enumConstantOrNull(name: String): Any? {
    if (this == null) return null
    val enumConstants = enumConstants ?: return null
    return enumConstants.find { it.toString() == name }
}
