package featurea.modbus

import featurea.featureaDir
import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.config.Directory
import featurea.modbus.support.ChannelDangerSoundTask
import featurea.modbus.support.ConnectionProvider
import featurea.modbus.support.JournalSystem
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.modbus") {
    includeContentRootWithConfig { "$featureaDir/libraries/modbus/res" }
    include(featurea.audio.artifact)

    "Channel" to ::Channel
    "Connection" to ::Connection
    "ConnectionProvider" to ::ConnectionProvider
    "Directory" to ::Directory
    "DirectoryDocket" to ::DirectoryDocket
    "JournalSystem" to ::JournalSystem
    "ModbusClient" to ::ModbusClient
    "ModbusClientConfig" to ::ModbusConfig

    ModbusClientPlugin {
        "ChannelDangerSoundTask" to ::ChannelDangerSoundTask
    }
}
