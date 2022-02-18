package featurea.modbus.test

import featurea.modbus.ModbusClient
import featurea.modbus.ModbusConfig
import featurea.modbus.config.Channel
import featurea.modbus.test.Resources.Home23Mdb
import featurea.rml.buildResource

fun Test1() = TestRuntime {
    val modbusClient: ModbusClient = module.importComponent()

    val modbusConfig = ModbusConfig()
    modbusConfig.directory = buildResource("$Home23Mdb/:config")
    modbusConfig.build()
    modbusClient.init(modbusConfig)
    modbusClient.create()
    modbusClient.connect()
    modbusConfig.readChannels = { channels: List<Channel> ->
        // breakpoint()
    }
}