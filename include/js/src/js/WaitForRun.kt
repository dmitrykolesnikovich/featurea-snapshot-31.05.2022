package featurea.js

import kotlinx.browser.window as jsWindow

class WaitForRun {

    internal lateinit var wait: () -> Boolean
    internal lateinit var run: () -> Unit

    fun wait(wait: () -> Boolean) {
        this.wait = wait
    }

    fun run(run: () -> Unit) {
        this.run = run
    }
}

fun waitForRun(init: WaitForRun.() -> Unit) {
    val waitForRun: WaitForRun = WaitForRun()
    waitForRun.init()
    var intervalId: Int = -1
    val runner = {
        val success: Boolean = waitForRun.wait()
        if (success) {
            waitForRun.run()
            if (intervalId != -1) {
                jsWindow.clearInterval(intervalId)
                intervalId = -1
            }
        }
    }
    intervalId = jsWindow.setInterval(runner, 300)
}
