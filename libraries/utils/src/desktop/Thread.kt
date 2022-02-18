@file:JvmName("Thread")

package featurea.utils

import javafx.application.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.swing.SwingUtilities

actual fun currentThreadSpecifier(): String = Thread.currentThread().toString()

actual fun currentThread(): Any = Thread.currentThread()

actual fun <T> runBlocking(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking {
        block()
    }
}

actual fun runOnApplicationThread(block: suspend () -> Unit) {
    SwingUtilities.invokeLater {
        runBlocking {
            block()
        }
    }
}

actual fun runOnEditorThread(block: suspend () -> Unit) {
    GlobalScope.launch(Dispatchers.JavaFx) {
        block()
    }
}

fun <T> Property<T>.watchOnApplicationThread(watcher: suspend () -> Unit) = watch {
    runOnApplicationThread {
        watcher()
    }
}

fun <T> Property<T>.watchOnEditorThread(watcher: suspend () -> Unit) = watch {
    runOnEditorThread {
        watcher()
    }
}
