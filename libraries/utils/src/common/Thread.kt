package featurea.utils

expect fun currentThreadSpecifier(): String

expect fun currentThread(): Any

expect fun <T> runBlocking(block: suspend () -> T): T

expect fun runOnApplicationThread(block: suspend () -> Unit)

expect fun runOnEditorThread(block: suspend () -> Unit)
