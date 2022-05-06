package featurea.utils

import featurea.Bundle
import featurea.Properties
import featurea.System
import kotlin.reflect.KProperty

class PropertyDelegate<T : Any>(val key: String, val defaultValue: () -> T) {

    inline operator fun <reified T : Any> getValue(properties: Properties, property: KProperty<*>): T {
        return properties[key] ?: defaultValue() as T
    }

    inline operator fun <reified T : Any> setValue(properties: Properties, property: KProperty<*>, value: T) {
        properties[key] = value
    }

}

class PropertyDelegateOrNull(val key: String) {

    inline operator fun <reified T : Any> getValue(properties: Properties, property: KProperty<*>): T? {
        return properties[key]
    }

    inline operator fun <reified T : Any> setValue(properties: Properties, property: KProperty<*>, value: T?) {
        properties[key] = value
    }

}

class BundlePropertyDelegate<T : Any>(val key: String, val defaultValue: () -> T) {

    inline operator fun <reified T : Any> getValue(bundle: Bundle, property: KProperty<*>): T {
        return bundle.manifest[key] ?: defaultValue() as T
    }

    inline operator fun <reified T : Any> setValue(bundle: Bundle, property: KProperty<*>, value: T) {
        bundle.manifest[key] = value
    }

}

class SystemPropertyDelegate<T : Any>(val key: String, val defaultValue: () -> T) {

    inline operator fun <reified T : Any> getValue(system: System, property: KProperty<*>): T {
        return system.properties[key] ?: defaultValue() as T
    }

    inline operator fun <reified T : Any> setValue(system: System, property: KProperty<*>, value: T) {
        system.properties[key] = value
    }

}

class SystemPropertyDelegateOrNull(val key: String) {

    inline operator fun <reified T : Any> getValue(system: System, property: KProperty<*>): T? {
        return system.properties[key]
    }

    inline operator fun <reified T : Any> setValue(system: System, property: KProperty<*>, value: T?) {
        system.properties[key] = value
    }

}
