package featurea.modbus.test

import featurea.log
import featurea.modbus.ModbusClient
import featurea.modbus.ModbusConfig
import featurea.modbus.config.Channel
import featurea.modbus.test.Resources.testMdb
import featurea.rml.buildResource

fun Test2() = TestRuntime {
    val modbusClient: ModbusClient = module.importComponent()

    val modbusConfig = ModbusConfig()
    modbusConfig.directory = buildResource("$testMdb/:config")
    modbusConfig.build()
    modbusClient.init(modbusConfig)
    modbusClient.create()
    modbusClient.connect()
    modbusConfig.readChannels = { channels: List<Channel> ->
        for (channel in channels) {
            log("${channel.name}: ${channel.readValueWithReadFormula}")
        }
    }
}
