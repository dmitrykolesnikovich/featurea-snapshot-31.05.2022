package featurea.js

import featurea.js.vuetify.fixApplicationBackground
import featurea.js.vuetify.fixApplicationContentMinHeight
import featurea.runtime.Artifact
import featurea.runtime.Delegate
import featurea.runtime.Proxy
import featurea.runtime.Runtime
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement

/*dependencies*/

val artifact = Artifact("featurea.js") {
    "HtmlElementProxy" to HtmlElementProxy::class
    "HTMLCanvasElementProxy" to HTMLCanvasElementProxy::class
    "LoaderLabelProxy" to LoaderLabelProxy::class
    "TitlebarProxy" to TitlebarProxy::class
    "CloseButtonProxy" to CloseButtonProxy::class
    "RootElementProxy" to RootElementProxy::class
    "SplashImageProxy" to SplashImageProxy::class
}

class HtmlElementProxy(override val delegate: HTMLElement) : Proxy<HTMLElement> {
    companion object : Delegate<HTMLElement>(HtmlElementProxy::class)
}

class HTMLCanvasElementProxy(override val delegate: HTMLCanvasElement) : Proxy<HTMLCanvasElement> {
    companion object : Delegate<HTMLCanvasElement>(HTMLCanvasElementProxy::class)
}

class LoaderLabelProxy(override val delegate: HTMLElement) : Proxy<HTMLElement> {
    companion object : Delegate<HTMLElement>(LoaderLabelProxy::class)
}

class TitlebarProxy(override val delegate: HTMLElement) : Proxy<HTMLElement> {
    companion object : Delegate<HTMLElement>(TitlebarProxy::class)
}

class CloseButtonProxy(override val delegate: HTMLElement) : Proxy<HTMLElement> {
    companion object : Delegate<HTMLElement>(CloseButtonProxy::class)
}

class SplashImageProxy(override val delegate: HTMLImageElement) : Proxy<HTMLImageElement> {
    companion object : Delegate<HTMLImageElement>(SplashImageProxy::class)
}

class RootElementProxy(override val delegate: HTMLElement) : Proxy<HTMLElement> {
    companion object : Delegate<HTMLElement>(RootElementProxy::class)
}

/*runtime*/

fun exportLaunchers(onLaunch: (args: Array<String>) -> Runtime) {
    // includes
    export("fixApplicationBackground", ::fixApplicationBackground)
    export("fixApplicationContentMinHeight", ::fixApplicationContentMinHeight)
    export("loadSplashScreen", ::loadSplashScreen)
    export("removeAllMainWindows", ::removeAllMainWindows)
    export("removeMainWindow", ::removeMainWindow)

    // launchers
    export("appendMainCanvas") { args: Array<String> -> appendMainCanvasOnLaunchWithArgs(onLaunch, args) }
    export("appendMainWindow") { args: Array<String> -> appendMainWindowOnLaunchWithArgs(onLaunch, args) }
}
