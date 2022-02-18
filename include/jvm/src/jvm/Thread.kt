package featurea.jvm

import kotlinx.coroutines.runBlocking

fun runLoop(period: Int, block: suspend (elapsedTime: Float) -> Unit) {
    var now: Long = 0
    while (true) {
        val nanoTime = System.nanoTime()
        if (now == 0L) now = nanoTime
        val elapsedTime = (nanoTime - now) / 1000000f
        now = nanoTime
        try {
            runBlocking {
                block(elapsedTime)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            val sleepTime = period - elapsedTime
            if (sleepTime > 0) {
                Thread.sleep(sleepTime.toLong())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sleepInMinutes(minutes: Int) {
    val millis = minutes * 60 * 1000
    Thread.sleep(millis.toLong())
}

fun sleepInSeconds(seconds: Int) {
    val millis = seconds * 1000
    Thread.sleep(millis.toLong())
}
