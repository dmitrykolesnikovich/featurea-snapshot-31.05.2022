package featurea.android.simulator

import android.os.FileObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import featurea.android.MainActivityProxy
import featurea.hasExtension
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import java.io.File
import java.util.*

// quickfix todo replace this file to separate component `fileManager-android`
private const val MASK = FileObserver.CREATE or FileObserver.DELETE or FileObserver.DELETE_SELF or
        FileObserver.MODIFY or FileObserver.MOVED_FROM or FileObserver.MOVED_TO or FileObserver.MOVE_SELF

typealias FileWatcherClient = (files: Set<File>) -> Unit

class FileWatcherThread(override val module: Module) : Component {

    private val mainActivity = import(MainActivityProxy)
    private lateinit var directory: File
    private lateinit var extension: String
    private lateinit var client: FileWatcherClient
    private lateinit var observer: FileObserver

    override fun onCreateComponent() {
        mainActivity.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onActivityResume() = start()

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onActivityPause() = stop()
        })
    }

    fun init(directory: File, extension: String, client: FileWatcherClient) {
        this.directory = directory
        this.extension = extension
        this.client = client
        observer = object : FileObserver(directory.absolutePath, MASK) {
            override fun onEvent(event: Int, path: String?) = update()
        }
        start()
    }

    fun start() {
        if (::observer.isInitialized) {
            observer.startWatching()
            update()
        }
    }

    fun stop() {
        if (::observer.isInitialized) {
            observer.stopWatching()
        }
    }

    @Synchronized
    private fun update() {
        val result: MutableSet<File> = HashSet()
        val listFiles = directory.listFiles() ?: return
        for (file in listFiles) {
            val name = file.name
            if (name.hasExtension(extension)) {
                result.add(object : File(file.absolutePath) {
                    override fun toString() = nameWithoutExtension
                })
            }
        }
        mainActivity.runOnUiThread { client(result) }
    }

}