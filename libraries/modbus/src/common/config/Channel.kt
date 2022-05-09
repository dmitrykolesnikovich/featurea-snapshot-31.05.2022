package featurea.modbus.config

import featurea.content.ResourceTag
import featurea.formula.Formula
import featurea.utils.getTimeMillis
import featurea.modbus.support.DataType
import featurea.modbus.support.ScriptType
import featurea.runtime.Module
import featurea.runtime.create

@OptIn(ExperimentalStdlibApi::class)
class Channel(module: Module) : Directory(module) {

    var isLocal: Boolean = false
    val isRemote: Boolean get() = !isLocal
    var address: Short = -1
    var diapason: Short = -1
    var region: Region? = null
    lateinit var type: DataType
    var updateInterval: Float = 1000f // quickfix todo revert to 0
    var fractionSize: Int = 0
    var readFormula: Formula<Double>? = null
    var writeFormula: Formula<Double>? = null
    var journalDelta: Double = 0.0
    var journalTimeout: Double = 0.0
    var script: String? = null
    var scriptType: ScriptType? = null
    var scriptTimeout: Double = 0.0
    var scriptTimeoutProgress: Double = 0.0
    var scriptSource: String? = null

    internal var index: Int = -1
    override fun toString(): String = "Channel(name='$name', address='$address:$type')"

    val startAddress: Short get() = address
    val finishAddress: Short get() = (address + registerCount - 1).toShort()
    val registerCount: Short get() = if (hasDiapason) diapason else type.size
    val hasDiapason: Boolean get() = diapason != (-1).toShort()
    val connection: Connection get() = config.connectionOf(this) // todo precalculate
    val isVirtual: Boolean get() = address.toInt() == -1
    var isEnable: Boolean = false

    var readValueWithReadFormula: Double = 0.0
        set(value) {
            field = value
            lastReadValueTime = getTimeMillis()
        }
    var lastReadValueTime: Double = -1.0
    var isReadValueWithReadFormulaValid: Boolean = false // IMPORTANT client set this to true
    var writeValueWithWriteFormula: Double = 0.0
        set(value) {
            field = value
            for (writeListener in writeNotifiers) {
                writeListener(value)
            }
        }
    private val writeNotifiers = mutableListOf<(value: Double) -> Unit>()
    var readValueInDiapason: ShortArray? = null

    fun registerWriteNotifier(listener: (value: Double) -> Unit) {
        writeNotifiers.add(listener)
    }

    private var updateProgress: Float = 0f
    var shouldRead: Boolean = false
    fun updateProgress(elapsedTime: Float) {
        updateProgress += elapsedTime
        shouldRead = updateProgress >= updateInterval
        if (shouldRead) {
            updateProgress = 0f
        }
    }

    /*dangerSound*/

    var dangerFormula: Formula<Boolean>? = null
    var dangerSound: String? = null
    var checkDangerPeriod: Double = 0.0
    private var isMarkedForDangerSound: Boolean = false

    fun markForDangerSound() {
        isMarkedForDangerSound = true
    }

    fun consumeMarkForDangerSound(): Boolean {
        val result: Boolean = isMarkedForDangerSound
        isMarkedForDangerSound = false
        return result
    }

}

// constructor
fun Directory.Channel(init: Channel.() -> Unit = {}): Channel = create(init).also { append(it) }

// constructor
fun Connection.Channel(init: Channel.() -> Unit): Channel = create(init).also { append(it) }

/*convenience*/

fun Channel.removeAttribute(key: String) {
    when (key) {
        "address" -> address = (-1).toShort()
    }
}

val ResourceTag.channelSpecifier: ChannelSpecifier
    get() {
        val address = attributes["address"]
        val isLocal = attributes["isLocal"]
        if (address != null) return ChannelSpecifier.Physical
        if (isLocal == "true") return ChannelSpecifier.Local
        return ChannelSpecifier.Virtual
    }

val Channel.shouldJournal: Boolean get() = journalDelta != 0.0 || journalTimeout != 0.0
