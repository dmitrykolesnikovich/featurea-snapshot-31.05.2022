package featurea

actual fun getTimeMillis(): Double = js("Date.now()").unsafeCast<Double>()

actual fun nowString(): String = TODO()