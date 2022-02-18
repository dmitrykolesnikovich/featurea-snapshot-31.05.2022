package featurea.modbus.support

import featurea.System
import featurea.audio.Audio
import featurea.modbus.ModbusClient
import featurea.modbus.ModbusClientListener
import featurea.modbus.config.Channel
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

@OptIn(ExperimentalStdlibApi::class)
class DangerService(val channel: Channel) {
    var checkDangerProgress: Double = 0.0
    val hasDangerFeature: Boolean get() = channel.dangerFormula != null && channel.dangerSound != null
    var hasDangerSound: Boolean = true
}

class ChannelDangerSoundTask(override val module: Module) : Component {
    val modbusClient: ModbusClient = import()

    override fun onCreateComponent() {
        modbusClient.listeners.add(object : ModbusClientListener {
            override fun onTickChannel(channel: Channel, elapsedTime: Float, notifier: MasterNotifier) {
                modbusClient.checkDanger(channel, elapsedTime)
            }
        })
    }
}

/*internals*/

@OptIn(ExperimentalStdlibApi::class)
private fun ModbusClient.checkDanger(channel: Channel, dt: Float) = config.indexScope {
    val dangerService: DangerService = channel.dangerService
    if (!dangerService.hasDangerFeature) return@indexScope

    dangerService.checkDangerProgress += dt
    val checkDangerPeriod = channel.checkDangerPeriod
    if (dangerService.checkDangerProgress >= checkDangerPeriod) {
        if (checkDangerPeriod == 0.0) {
            dangerService.checkDangerProgress = 0.0
        } else {
            dangerService.checkDangerProgress %= checkDangerPeriod
        }

        val dangerFormula = channel.dangerFormula ?: return@indexScope
        val dangerSound = channel.dangerSound ?: return@indexScope

        dangerFormula.setVariable("\${value}", channel.readValueWithReadFormula)
        val shouldPlaySound = dangerFormula.calculate()
        if (shouldPlaySound) {
            if (channel.checkDangerPeriod != 0.0 || !dangerService.hasDangerSound) {
                val system: System = channel.import()
                val isHeadless: Boolean = system.properties["headless"] ?: false
                if (isHeadless) {
                    channel.markForDangerSound()
                } else {
                    val audio: Audio = channel.import()
                    audio.playSound(dangerSound)
                }
            }
        }
        dangerService.hasDangerSound = shouldPlaySound
    }
}
