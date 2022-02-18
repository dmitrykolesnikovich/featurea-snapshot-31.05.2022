package featurea.modbus.support

import featurea.modbus.master.Master

typealias MasterNotifierListener = MasterNotifier.(notification: String) -> Unit

class MasterNotifier(val master: Master) {

    private var listeners = mutableListOf<MasterNotifierListener>()
    val isNotEmpty: Boolean get() = listeners.isNotEmpty()

    private val notificationBuilder: StringBuilder = StringBuilder()
    private val listenersToIterate = mutableListOf<MasterNotifierListener>()
    private val listenersToUnregister = mutableListOf<MasterNotifierListener>()
    private var currentListener: MasterNotifierListener? = null

    fun registerListener(listener: MasterNotifierListener) {
        listeners.add(listener)
    }

    fun notify(block: (notificationBuilder: StringBuilder) -> Unit) {
        block(notificationBuilder)
        val notification: String = notificationBuilder.toString()
        listenersToIterate.addAll(listeners)
        for (listener in listenersToIterate) {
            currentListener = listener
            listener(notification)
        }
        listeners.removeAll(listenersToUnregister)
        listenersToUnregister.clear()
        notificationBuilder.clear()
        listenersToIterate.clear()
    }

    fun unregisterListener() {
        listenersToUnregister.add(checkNotNull(currentListener))
    }

}
