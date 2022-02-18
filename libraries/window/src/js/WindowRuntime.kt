package featurea.window

import featurea.isInstrumentationEnabled
import featurea.js.*
import featurea.js.dialog.onChangeCssProperty
import featurea.js.dialog.setupFullScreenSize
import featurea.log
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.Provide
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import kotlinx.browser.window as jsWindow

actual fun WindowContainer() = Container {
    onCreate {
        log("isInstrumentationEnable: $isInstrumentationEnabled")
    }
}

@Provide(CloseButtonProxy::class)
@Provide(HTMLCanvasElementProxy::class)
@Provide(LoaderLabelProxy::class)
@Provide(RootElementProxy::class)
@Provide(SplashImageProxy::class)
@Provide(TitlebarProxy::class)
actual fun WindowModule() = Module {
    onInit { appModule ->
        await(HtmlElementProxy::class)

        val rootElement: HTMLElement = appModule.components["_rootElement"]
        appModule.provideComponent(RootElementProxy(rootElement))

        val mainCanvas = rootElement.querySelector("#mainCanvas") as HTMLCanvasElement
        appModule.provideComponent(HTMLCanvasElementProxy(mainCanvas))

        val loaderLabel = rootElement.querySelector("#loaderLabel") as HTMLElement?
        loaderLabel?.also { appModule.provideComponent(LoaderLabelProxy(loaderLabel)) }

        val titlebar = rootElement.querySelector(".titlebar") as HTMLElement?
        titlebar?.also { appModule.provideComponent(TitlebarProxy(titlebar)) }

        val closeButton = rootElement.querySelector("#closeButton") as HTMLElement?
        closeButton?.also { appModule.provideComponent(CloseButtonProxy(closeButton)) }

        val splashImage = rootElement.querySelector("#splashImage") as HTMLImageElement?
        splashImage?.also { appModule.provideComponent(SplashImageProxy(splashImage)) }

        val dialogContent = rootElement.querySelector(".content") as HTMLElement?
        dialogContent?.also { appModule.components.inject("_dialogContent", dialogContent) }

        /*
        val hiddenInput = rootElement.querySelector("#hiddenInput") as HTMLElement?
        hiddenInput?.also { appModule.components.inject("_hiddenInput", hiddenInput) }
        */

        if (isUserAgentMobile) {
            jsWindow.addEventListener("resize", { rootElement.setupFullScreenSize() }, false)
            rootElement.onChangeCssProperty("display") { value -> if (value == "block") rootElement.setupFullScreenSize() }
            rootElement.setupFullScreenSize()
        }

        appModule.importComponent<Window>()
    }
}
