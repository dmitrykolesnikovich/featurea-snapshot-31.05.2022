package featurea.webSocket

import featurea.StringBlock
import featurea.runtime.Module
import featurea.runtime.Component

expect class WebSocket(module: Module) : Component {

    var retriesCount: Int
    var responseTimeout: Int
    var retryTimeout: Int

    fun initCookieWithLocalStorage(key: String, days: Int) // quickfix todo improve
    fun init(url: String)
    fun connect(onConnect: (() -> Unit)? = null)
    fun send(message: String)
    fun disconnect()

    fun onConnect(callback: () -> Unit)
    fun onSuccess(callback: StringBlock)
    fun onFailure(callback: StringBlock)
    fun onReconnect(callback: () -> Unit)
    fun onDestroy(callback: () -> Unit)
    fun onDisconnect(callback: () -> Unit)

}
