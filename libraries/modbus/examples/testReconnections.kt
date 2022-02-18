package featurea.modbus.test

import featurea.featureaDir
import java.io.File

fun testReconnections() {
    testStation(File("$featureaDir/engine/libraries/modbus/test/res/reconnections/Alarm.mdb"), isLog = false)
    testStation(File("$featureaDir/engine/libraries/modbus/test/res/reconnections/brigad_R-converted.mdb"), isLog = false)
    testStation(File("$featureaDir/engine/libraries/modbus/test/res/reconnections/Kadorr.mdb"), isLog = false)
    testStation(File("$featureaDir/engine/libraries/modbus/test/res/reconnections/Layan2_0.mdb"), isLog = false)
    testStation(File("$featureaDir/engine/libraries/modbus/test/res/reconnections/Prestige.mdb"), isLog = false)
    testStation(File("$featureaDir/engine/libraries/modbus/test/res/reconnections/Spirid_Cloud2_0.mdb"), isLog = false)
    testStation(File("$featureaDir/engine/libraries/modbus/test/res/reconnections/TestSub2.mdb"), isLog = false)
}
