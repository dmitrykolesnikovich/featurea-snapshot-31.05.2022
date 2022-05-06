package featurea.utils

import featurea.System

class Logger(val tag: String) {

    var isEnable: Boolean = false

    fun log(message: String) {
        if (isEnable) {
            featurea.utils.log("[$tag] $message")
        }
    }

}

fun failure(message: Any?, exit: String? = null) {
    log(message, isFailure = true)
    if (exit != null) {
        error(exit)
    }
}

expect fun log(message: Any?, isFailure: Boolean = false)

fun log(message: Any?, included: SystemTarget) {
    if (System.target == included) {
        log(message)
    }
}

inline fun <T> logElapsedTime(tag: String, min: Double = 0.0, block: () -> T): T {
    val startTime = getTimeMillis()
    val result = block()
    val finishTime = getTimeMillis()
    val elapsedTime = finishTime - startTime
    if (elapsedTime >= min) {
        log("[${tag}] elapsedTime: $elapsedTime")
    }
    return result
}

private var lastMessage: String? = null

fun log(message: String, allowDuplicates: Boolean) {
    if (allowDuplicates) {
        log(message)
    } else if (lastMessage != message) {
        lastMessage = message
        log(message)
    }
}
