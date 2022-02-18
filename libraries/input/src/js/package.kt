package featurea.input

import featurea.js.isUserAgentMobile
import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    InputPlugin {
        if (isUserAgentMobile) {
            "TouchEventProducer" to ::TouchEventProducer
        } else {
            "MouseEventProducer" to ::MouseEventProducer
        }
    }
}
