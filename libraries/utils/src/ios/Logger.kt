package featurea.utils

actual fun log(message: Any?, isFailure: Boolean) {
    println(message.toString()) // IMPORTANT platform.Foundation.NSLog does not work for me
}
