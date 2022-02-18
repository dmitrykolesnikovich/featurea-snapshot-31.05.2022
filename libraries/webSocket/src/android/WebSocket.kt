package featurea.webSocket

import featurea.StringBlock
import featurea.runtime.Component
import featurea.runtime.Module

actual class WebSocket actual constructor(override val module: Module) : Component {

    actual var retriesCount: Int = 3
    actual var retryTimeout: Int = 3000
    actual var responseTimeout: Int = 3000

    actual fun initCookieWithLocalStorage(key: String, days: Int): Unit = TODO()
    actual fun init(url: String): Unit = TODO()
    actual fun connect(onConnect: (() -> Unit)?): Unit = TODO()
    actual fun disconnect(): Unit = TODO()
    actual fun send(message: String): Unit = TODO()

    actual fun onConnect(callback: () -> Unit): Unit = TODO()
    actual fun onSuccess(callback: StringBlock): Unit = TODO()
    actual fun onFailure(callback: StringBlock): Unit = TODO()
    actual fun onReconnect(callback: () -> Unit): Unit = TODO()
    actual fun onDestroy(callback: () -> Unit): Unit = TODO()
    actual fun onDisconnect(callback: () -> Unit): Unit = TODO()

}
