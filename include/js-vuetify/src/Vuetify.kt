package featurea.js.vuetify

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import kotlinx.browser.window as jsWindow

// projects/lcontrol/web/web-site/public/js/vuetify.js

fun fixApplicationBackground() {
    val div = document.querySelector(".theme--light.v-application")
    if (div is HTMLDivElement) {
        div.style.background = "transparent"
    }
}

fun fixApplicationContentMinHeight() {
    val div = document.querySelector(".v-application--wrap") as HTMLDivElement
    div.style.minHeight = "${jsWindow.innerHeight}px"
    jsWindow.addEventListener("resize", {
        div.style.minHeight = "${jsWindow.innerHeight}px"
    }, false)
}
