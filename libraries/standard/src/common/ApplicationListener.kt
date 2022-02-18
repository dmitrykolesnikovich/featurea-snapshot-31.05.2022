package featurea

typealias ApplicationTask = ApplicationListener.() -> Unit

interface ApplicationListener : EventListener {
    fun onCreateApplication() {}
    fun onStartApplication() {}
    fun onResumeApplication() {}
    fun onPauseApplication() {}
    fun onStopApplication() {}
    fun onDestroyApplication() {}
}

fun CreateApplicationListener(task: ApplicationTask): ApplicationListener = object : ApplicationListener {
    override fun onCreateApplication() = task()
}

fun PauseApplicationListener(task: ApplicationTask): ApplicationListener = object : ApplicationListener {
    override fun onPauseApplication() = task()
}

fun StopApplicationListener(task: ApplicationTask): ApplicationListener = object : ApplicationListener {
    override fun onStopApplication() = task()
}

fun DestroyApplicationListener(task: ApplicationTask): ApplicationListener = object : ApplicationListener {
    override fun onDestroyApplication() = task()
}
