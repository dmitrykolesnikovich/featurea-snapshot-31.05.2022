package featurea.modbus

import featurea.breakpoint
import featurea.firstStringOrNull
import featurea.formula.toFormulaOrNull
import featurea.modbus.config.*
import featurea.modbus.support.ScriptType
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.script.Script
import featurea.toEnumValue
import featurea.toEnumValueOrNull

class DirectoryDocket(override val module: Module) : Component, Script {

    lateinit var directory: Directory

    override suspend fun executeAction(action: String, args: List<Any?>, isSuper: Boolean): Directory {
        val argument: String? = args.firstStringOrNull()
        if (action == "create") {
            directory = when (argument) {
                "Directory" -> Directory()
                "Channel" -> Channel(module)
                "Connection" -> Connection()
                else -> error("argument: $argument")
            }
            directory.withDocket = true
        } else if (action == "build") {
            // no op
        } else {
            with(directory) {
                when (action) {
                    "id" -> id = argument ?: "undefined"
                    "name" -> name = argument ?: "undefined"
                    "append" -> append(directory = args[0] as Directory)
                    "remove" -> remove(directory = args[0] as Directory)
                    "insert" -> insert(index = args[0] as Int, directory = args[1] as Directory)
                    else -> {
                        if (this is Connection) {
                            if (argument == null) {
                                breakpoint()
                            }
                            val value: String = checkNotNull(argument)
                            when (action) {
                                "ip" -> ip = value
                                "port" -> port = value.toInt()
                                "responseTimeout" -> responseTimeout = value.toInt()
                                "retriesCount" -> retriesCount = value.toInt()
                                "retryTimeout" -> retryTimeout = value.toInt()
                                "registerCount" -> registerCount = value.toInt()
                            }
                        }
                        if (this is Channel) {
                            if (argument == null) {
                                breakpoint()
                            }
                            val value: String = checkNotNull(argument)
                            when (action) {
                                "isLocal" -> isLocal = value.toBoolean()
                                "address" -> address = value.toShort()
                                "diapason" -> diapason = value.toShort()
                                "region" -> region = value.toEnumValueOrNull<Region>()
                                "type" -> type = value.toEnumValue()
                                "enable" -> isEnable = value.toBoolean()
                                "updateInterval" -> updateInterval = value.toFloat()
                                "readFormula" -> readFormula = value.toFormulaOrNull()
                                "writeFormula" -> writeFormula = value.toFormulaOrNull()
                                "fractionSize" -> fractionSize = value.toInt()
                                "journalDelta" -> journalDelta = value.toDouble()
                                "journalTimeout" -> journalTimeout = value.toDouble()
                                "script" -> script = value
                                "scriptType" -> scriptType = value.toEnumValue<ScriptType>()
                                "scriptTimeout" -> scriptTimeout = value.toDouble()
                                "dangerFormula" -> dangerFormula = value.toFormulaOrNull()
                                "dangerSound" -> dangerSound = value
                                "checkDangerPeriod" -> checkDangerPeriod = value.toDouble()
                                "removeAttribute" -> removeAttribute(key = value)
                            }
                        }
                    }
                }
            }
        }
        return directory
    }

}
