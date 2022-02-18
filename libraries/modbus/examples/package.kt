package featurea.modbus.test

import featurea.featureaDir
import featurea.isInstrumentationEnable
import featurea.log
import featurea.modbus.ModbusRuntime
import featurea.modbus.config.Channel
import featurea.modbus.master.Logger
import featurea.runtime.Artifact
import featurea.runtime.Component
import featurea.runtime.Runtime
import featurea.runtime.launch
import kotlinx.coroutines.runBlocking
import java.io.File

/*content*/

object Resources {
    val Home23Mdb = "Home23.mdb"
    val testMdb = "test.mdb"
}

/*dependencies*/

val artifact = Artifact("featurea.modbus.test") {
    includeContentRoot { "$featureaDir/engine/libraries/modbus/test/res" }
    include(featurea.modbus.artifact)
}

/*runtime*/

fun TestRuntime(setup: Task) = Runtime {
    exportComponents(featurea.modbus.test.artifact)
    injectContainer("featurea.runtime.DefaultContainer")
    injectModule("featurea.runtime.DefaultModule")
    build {
        runBlocking {
            setup(it.importComponent())
        }
    }
}

fun testStation(file: File, isLog: Boolean = true) {
    log("station: ${file.path}")
    launch {
        val mdbSource = file.readText()
        ModbusRuntime(artifact, mdbSource, -1, -1) { modbusClient, _ ->
            modbusClient.logger.isEnable = true
            modbusClient.logger.sources.add(Logger.Source { message ->
                println(message)
            })
            if (isLog) {
                modbusClient.config.readChannels = { channels: List<Channel> ->
                    for (channel in channels) {
                        log("${channel.name}: ${channel.readValueWithReadFormula}")
                    }
                }
            }
        }
    }
    Thread.sleep(3_000)
}