package featurea

fun MutableMap<() -> Unit, Float>.updateTasksWithDelay(elapsedTime: Float, initial: Map<() -> Unit, Float>? = null) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val (task, delay) = iterator.next()
        val newDelay = delay - elapsedTime
        if (newDelay > 0) {
            this[task] = newDelay
        } else {
            task()
            if (initial == null) {
                iterator.remove()
            } else {
                this[task] = newDelay + initial[task]!!
            }
        }
    }
}
