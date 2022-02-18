package featurea.modbus

import featurea.rml.buildResourceWithoutCache
import featurea.runtime.Dependency
import featurea.runtime.Runtime
import kotlinx.coroutines.runBlocking

typealias ModbusRuntimeInit = suspend (ModbusClient, ModbusThread) -> Unit

fun ModbusRuntime(id: String, mdbSource: String, connectionLimit: Int, channelLimit: Int, init: ModbusRuntimeInit): Runtime {
    return ModbusRuntime(id, featurea.modbus.artifact, mdbSource, connectionLimit, channelLimit, init)
}

fun ModbusRuntime(id: String, artifact: Dependency, mdbSource: String, connectionLimit: Int, channelLimit: Int, init: ModbusRuntimeInit): Runtime {
    return Runtime {
        exportComponents(artifact)
        injectDefaultContainer()
        injectDefaultModule()
        complete { module ->
            runBlocking {
                val modbusClient: ModbusClient = module.importComponent()
                val modbusConfig: ModbusConfig = modbusClient.ModbusConfig()
                modbusConfig.directory = modbusClient.buildResourceWithoutCache("/config", mdbSource)
                modbusConfig.directory.id = id // just for now todo delete this
                modbusConfig.build()
                modbusClient.connectionProvider.channelLimit = channelLimit
                modbusClient.connectionProvider.connectionLimit = connectionLimit
                modbusClient.init(modbusConfig)
                modbusClient.create()
                modbusClient.connect {
                    val thread: ModbusThread = ModbusThread(modbusClient)
                    thread.start()
                    init(modbusClient, thread)
                }
            }
        }
    }
}
