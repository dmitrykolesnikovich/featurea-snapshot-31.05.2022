package featurea.studio

import featurea.Application
import kotlinx.coroutines.runBlocking
import javax.swing.SwingUtilities

fun Application.runOnUpdateOnJfxThread(block: suspend () -> Unit) {
    if (SwingUtilities.isEventDispatchThread()) {
        runOnUpdate {
            runBlocking {
                block()
            }
        }
    } else {
        SwingUtilities.invokeLater {
            runOnUpdate {
                runBlocking {
                    block()
                }
            }
        }
    }
}