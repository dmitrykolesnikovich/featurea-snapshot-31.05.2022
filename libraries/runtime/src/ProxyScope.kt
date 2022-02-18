package featurea.runtime

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private var runtimeScope: ProxyScope? = null

interface ProxyScope {
    fun provide(proxy: Proxy<*>)
}

fun defaultProxyScope(receiver: Proxy<*> = UnitProxy, block: Runtime.() -> Unit = {}) {
    proxyScope(receiver) {
        Runtime().apply(block)
    }
}

fun proxyScope(receiver: Proxy<*> = UnitProxy, init: RuntimeBuilder.() -> Runtime) {
    val proxyScope: ProxyScope? = runtimeScope
    if (proxyScope != null) {
        error("inside proxy scope")
    }
    val runtimeBuilder: RuntimeBuilder = RuntimeBuilder(receiver, init)
    runtimeScope = runtimeBuilder
    runtimeBuilder.build {
        runtimeScope = null
    }
}

fun provide(proxy: Proxy<*>) {
    val proxyScope: ProxyScope? = runtimeScope
    if (proxyScope == null) {
        error("outside proxy scope")
    }
    proxyScope.provide(proxy)
}
