package featurea.cameraView

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.window.WindowElement
import featurea.window.WindowElementProvider
import featurea.window.provideWindowElement
import kotlinx.browser.document
import org.w3c.dom.HTMLImageElement

private const val testUrl = "https://media.sproutsocial.com/uploads/2017/02/10x-featured-social-media-image-size.png"

fun CameraElementProvider(module: Module) = module.provideWindowElement(object : WindowElementProvider<CameraView> {

    override fun Component.createElementOrNull(view: CameraView): WindowElement {
        val image: HTMLImageElement = document.createElement("img") as HTMLImageElement
        if (view.url.startsWith("http")) {
            image.src = /*view.url*/testUrl // just for now todo replace `testUrl` with `view.url`
        }
        return WindowElement(image)
    }

    override fun Component.destroyElement(element: WindowElement) {
        // no op
    }

})
