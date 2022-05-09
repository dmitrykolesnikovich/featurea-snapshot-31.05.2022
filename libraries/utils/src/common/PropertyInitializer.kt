@file:Suppress("UNUSED_PARAMETER")

package featurea.utils

/*
```
class A {
    val x: File by property { lateinit }
}
```

compiled to

```
class A {
    val xProperty: Property<File> = Property()
    val x: File by xProperty
}
```
*/

interface PropertyInitializerContext<T> {
    val lateinit: T
}

fun <T> property(initializer: PropertyInitializerContext<T>.() -> T): Property<T> = error("stub")
