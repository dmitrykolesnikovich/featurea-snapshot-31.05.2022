package featurea.modbus.test

import featurea.featureaDir
import featurea.log
import featurea.modbus.ModbusRuntime
import featurea.modbus.config.Channel
import featurea.runtime.launch
import java.io.File

fun test4() {
    launch {
        val mdbSource = File("$featureaDir/engine/libraries/modbus/test/res/test.mdb").readText()
        ModbusRuntime(featurea.modbus.test.artifact, mdbSource, -1, -1) { modbusClient, _ ->
            modbusClient.config.readChannels = { channels: List<Channel> ->
                for (channel in channels) {
                    log("${channel.name}: ${channel.readValueWithReadFormula}")
                }
            }
        }
    }
}
