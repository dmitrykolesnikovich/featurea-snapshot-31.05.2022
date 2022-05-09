package featurea.runtime

import kotlin.reflect.KClass

@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Provide(val proxy: KClass<out Proxy<*>>)

interface Proxy<T> {
    val delegate: T
}

open class Delegate<T>(val proxyType: KClass<out Proxy<T>>)
