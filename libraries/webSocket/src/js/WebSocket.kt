package featurea.webSocket

import featurea.StringBlock
import featurea.System
import featurea.js.*
import featurea.log
import featurea.name
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.browser.localStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.CloseEvent
import org.w3c.dom.get
import kotlinx.browser.window as jsWindow
import org.w3c.dom.WebSocket as JsWebSocket

actual class WebSocket actual constructor(override val module: Module) : Component {

    private val system: System = import()

    actual var retryTimeout: Int = 3000
    actual var responseTimeout: Int = 10_000
    actual var retriesCount: Int = 3

    private var connectCallback: (() -> Unit)? = null
    private var successCallback: StringBlock? = null
    private var failureCallback: StringBlock? = null
    private var reconnectCallback: (() -> Unit)? = null
    private var disconnectCallback: (() -> Unit)? = null
    private var destroyCallback: (() -> Unit)? = null

    private val token = SessionToken(expirationTimeout = responseTimeout)
    private lateinit var socket: JsWebSocket
    private lateinit var url: String
    private var reconnectCounter: Int = 0
    private var isDestroy: Boolean = false
    private var isDisconnect: Boolean = false

    actual fun initCookieWithLocalStorage(key: String, days: Int) {
        val value: String? = localStorage.getItem(key)
        setCookie(key, value, days)
    }

    actual fun init(url: String) {
        log("WebSocketJs.init: $url (${system.workingDir})")
        this.url = url
        this.token.onExpire = {
            log("token.onExpire: $url (${system.workingDir})")
            disconnect()
            connect()
        }
        // >> quickfix todo find better place
        onDestroy {
            jsWindow.alert("Connection lost: ${url.removeSuffix("/wss")}")
            val buildPath: String? = system.workingDir
            if (buildPath != null) {
                removeMainWindow(buildPath.name) // quickfix todo improve
            }
        }
        // <<
    }

    actual fun connect(onConnect: (() -> Unit)?) {
        log("WebSocketJs.connect: $url (${system.workingDir})")
        if (onConnect != null) {
            onConnect(onConnect)
        }
        socket = JsWebSocket("$url?token=${localStorage["token"]}") // quickfix todo revert
        socket.onopen(timeout = responseTimeout) {
            reset()
            connectCallback?.invoke()
        }
        socket.onmessage = { event ->
            val message: String = event.data.toString()
            success(message)
        }
        socket.onerror = { event ->
            log("socket.onerror: $url (${system.workingDir})")
            failure(message = event.type)
        }
        socket.onclose = { event ->
            event as CloseEvent
            log("socket.onclose: ${event.type}, $url (${system.workingDir})")
            when (event.code.toInt()) {
                1003/*io.ktor.http.cio.websocket.CloseReason.Codes.CANNOT_ACCEPT*/ -> destroy()
                else -> reconnect()
            }
        }
    }

    actual fun send(message: String) {
        if (isDisconnect) return
        if (isDestroy) return
        if (socket.readyState == JsWebSocket.OPEN) {
            socket.send(message)
        }
    }

    actual fun disconnect() {
        if (isDisconnect) return
        isDisconnect = true
        log("WebSocketJs.disconnect: $url (${system.workingDir})")
        socket.closeWithoutEvents()
        invalidate()
        disconnectCallback?.invoke()
    }

    actual fun onConnect(callback: () -> Unit) {
        this.connectCallback = callback
    }

    actual fun onSuccess(callback: StringBlock) {
        this.successCallback = callback
    }

    actual fun onFailure(callback: StringBlock) {
        this.failureCallback = callback
    }

    actual fun onReconnect(callback: () -> Unit) {
        this.reconnectCallback = callback
    }

    actual fun onDestroy(callback: () -> Unit) {
        this.destroyCallback = callback
    }

    actual fun onDisconnect(callback: () -> Unit) {
        this.disconnectCallback = callback
    }

    /*internals*/

    private fun reset() {
        log("WebSocketJs.reset: $url (${system.workingDir})")
        token.validate()
        reconnectCounter = 0
        isDestroy = false
        isDisconnect = false
    }

    private fun success(message: String) {
        token.validate()
        successCallback?.invoke(message)
    }

    // https://stackoverflow.com/a/40084550/909169
    private fun failure(message: String) {
        log("WebSocketJs.failure: $message, $url (${system.workingDir})")
        invalidate()
        failureCallback?.invoke(message)
    }

    private fun reconnect() {
        log("WebSocketJs.reconnect: $reconnectCounter, $retriesCount, $url (${system.workingDir})")
        reconnectCallback?.invoke()
        reconnectCounter++
        if (reconnectCounter > retriesCount) {
            destroy()
            return
        }
        if (reconnectCounter == 1) {
            connect()
        } else {
            jsWindow.setTimeout({
                connect()
            }, retryTimeout)
        }
    }

    private fun invalidate() {
        log("WebSocketJs.invalidate: $url (${system.workingDir})")
        token.invalidate()
    }

    private fun destroy() {
        if (!isDestroy) {
            isDestroy = true
            destroyCallback?.invoke()
        }
        disconnect()
    }

}
