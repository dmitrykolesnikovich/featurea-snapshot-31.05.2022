@file:Suppress("MemberVisibilityCanBePrivate")

package featurea.app

import featurea.runtime.*
import featurea.utils.BufferedList
import featurea.utils.Property
import featurea.utils.forEachEvent
import featurea.utils.updateTasksWithDelay

class Application(override val module: Module) : Component {

    val controllers = mutableListOf<ApplicationController>()
    val delegateProperty = Property<ApplicationDelegate?>(null)
    var delegate: ApplicationDelegate? by delegateProperty
    var elapsedTime: Float = 0f
    var isEnable: Boolean = true
    val listeners = mutableListOf<ApplicationListener>()
    val tasksToRepeatOnUpdateApplication = mutableListOf<(elapsedTime: Float) -> Unit>()
    val tasksToRunOnUpdate = BufferedList<() -> Unit>()
    val tasksWithPeriodToRepeatOnUpdate = mutableMapOf<() -> Unit, Float>()
    val tasksWithPeriodToRepeatOnUpdateInitial = mutableMapOf<() -> Unit, Float>()
    val tasksWithDelayToRunOnUpdate = mutableMapOf<() -> Unit, Float>()
    val tasksToRunOnCompleteLoading = BufferedList<Task>()
    val tasksToRepeatOnBuildApplication = mutableListOf<Task>()
    val tasksToRepeatOnStartLoading = mutableListOf<Task>()
    var frameCount: Long = 0
        private set

    init {
        delegateProperty.watch {
            frameCount = 0L
        }
    }

    suspend fun updateControllers() {
        val controllers = ArrayList(controllers) // quickfix todo improve
        for (controller in controllers) {
            controller.update()
        }
    }

    fun updateDelegate() {
        frameCount++
        delegate?.update(elapsedTime)
        for (task in tasksToRepeatOnUpdateApplication) {
            task(elapsedTime)
        }
    }

    fun updateTasks() {
        tasksWithPeriodToRepeatOnUpdate.updateTasksWithDelay(elapsedTime, tasksWithPeriodToRepeatOnUpdateInitial)
        tasksToRunOnUpdate.poll { oldTasks, newTasks ->
            for (oldTask in oldTasks) {
                oldTask()
            }
            for (newTask in newTasks) {
                newTask()
            }
        }
        tasksWithDelayToRunOnUpdate.updateTasksWithDelay(elapsedTime)
    }

    fun onCreate() {
        delegate?.create()
        listeners.forEachEvent { it.onCreateApplication() }
    }

    fun onStart() {
        delegate?.start()
        listeners.forEachEvent { it.onStartApplication() }
    }

    fun onResume() {
        delegate?.resume()
        listeners.forEachEvent { it.onResumeApplication() }
    }

    fun onPause() {
        delegate?.pause()
        listeners.forEachEvent { it.onPauseApplication() }
    }

    fun onStop() {
        delegate?.stop()
        listeners.forEachEvent { it.onStopApplication() }
    }

    fun onDestroy() {
        delegate?.destroy()
        listeners.forEachEvent { it.onDestroyApplication() }
    }

    fun repeatOnStartLoading(task: Task) {
        tasksToRepeatOnStartLoading.add(task)
    }

    fun runOnCompleteLoading(task: Task) {
        tasksToRunOnCompleteLoading.add(task)
    }

    fun repeatOnBuildApplication(task: Task) {
        tasksToRepeatOnBuildApplication.add(task)
    }

    fun runOnUpdate(task: () -> Unit) {
        tasksToRunOnUpdate.add(task)
    }

    fun runOnUpdate(delay: Float, task: () -> Unit) {
        tasksWithDelayToRunOnUpdate[task] = delay
    }

    fun repeatOnUpdate(task: (elapsedTime: Float) -> Unit) {
        tasksToRepeatOnUpdateApplication.add(task)
    }

    fun repeatOnUpdate(period: Float, task: () -> Unit) {
        tasksWithPeriodToRepeatOnUpdate[task] = period
        tasksWithPeriodToRepeatOnUpdateInitial[task] = period
    }

}
