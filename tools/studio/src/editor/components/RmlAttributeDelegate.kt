package featurea.studio.editor.components

import featurea.utils.Color
import featurea.utils.toColor
import featurea.utils.toResource
import featurea.content.ResourceTag
import featurea.utils.splitAndTrim
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ResourceAttributeDelegate<T : Any>(val rmlTag: ResourceTag, val key: String, val defaultValue: T, val type: KClass<T>) {

    operator fun getValue(component: Any, property: KProperty<*>): T {
        val value: String? = rmlTag.attributes[key]
        val result: Any? = when (type) {
            Int::class -> value?.toInt()
            Array<Int>::class -> value?.splitAndTrim(",")?.map { it.toInt() }?.toTypedArray()
            Color::class -> value?.toColor()
            String::class -> value
            else -> error("type: $type")
        }
        return result as T? ?: defaultValue
    }

    operator fun setValue(component: Any, property: KProperty<*>, value: T) {
        rmlTag.attributes[key] = when (value) {
            is Array<*> -> value.joinToString()
            is Color -> value.toResource()
            else -> value.toString()
        }
    }

}

inline fun <reified T : Any> ResourceTag.attributeDelegate(key: String, defaultValue: T): ResourceAttributeDelegate<T> =
    ResourceAttributeDelegate(this, key, defaultValue, T::class)
