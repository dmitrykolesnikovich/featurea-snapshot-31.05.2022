@file:Suppress("FoldInitializerAndIfToElvis")

package featurea.runtime

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private var runtimeScope: ProxyScope? = null

interface ProxyScope {
    fun provide(proxy: Proxy<*>)
}

fun defaultProxyScope(block: Runtime.() -> Unit = {}) {
    buildRuntime {
        Runtime().apply(block)
    }
}

fun buildRuntime(init: RuntimeBuilder.() -> Runtime) { // todo return Runtime
    val proxyScope: ProxyScope? = runtimeScope
    if (proxyScope != null) {
        error("inside proxy scope")
    }
    val runtimeBuilder: RuntimeBuilder = RuntimeBuilder(init)
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
