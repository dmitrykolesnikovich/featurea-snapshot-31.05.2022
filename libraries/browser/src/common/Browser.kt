package featurea.browser

import featurea.runtime.Module
import featurea.runtime.Component

expect class Browser(module: Module) : Component {
    fun open(url: String)
}
