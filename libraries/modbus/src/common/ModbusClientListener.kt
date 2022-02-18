package featurea.modbus

import featurea.StringBlock
import featurea.modbus.config.Channel
import featurea.modbus.support.MasterNotifier
import featurea.runtime.Constructor

interface ModbusClientListener {
    fun onInit() {}
    fun onCreate() {}
    fun onConnect() {}
    fun onDisconnect() {}
    fun onDestroy() {}
    fun onTick(elapsedTime: Float) {}
    fun onTickChannel(channel: Channel, elapsedTime: Float, notifier: MasterNotifier) {}
}

@Constructor
fun ModbusClientListener(block: (elapsedTime: Float) -> Unit) = object : ModbusClientListener {
    override fun onTick(elapsedTime: Float) = block(elapsedTime)
}
