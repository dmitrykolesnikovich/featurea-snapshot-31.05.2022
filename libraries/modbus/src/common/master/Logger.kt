package featurea.modbus.master

import featurea.modbus.config.Channel
import featurea.modbus.transaction.ChannelGroup
import featurea.modbus.transaction.Transaction
import featurea.nowString

class Logger {

    fun interface Source {
        fun log(string: String?)
    }

    var isEnable: Boolean = false
    val sources = mutableListOf<Source>()

    fun logThreadStart(ip: String, port: Int) = log("Start thread $ip:$port")
    fun logThreadStop(ip: String, port: Int) = log("Stop thread $ip:$port")
    fun logConnect(ip: String, port: Int) = log("Connect $ip:$port")
    fun logConnectionFailed(ip: String, port: Int, e: Throwable) =
        log("Error. Connection failed $ip:$port", e)

    fun logDisconnect(ip: String, port: Int) = log("Disconnect $ip:$port")

    fun onReconnectSuccessSocketConnection(ip: String, port: Int, responseTimeout: Int, e: Throwable) =
        log("Error. Reconnect $ip:$port, responseTimeout: $responseTimeout", e)

    fun onReconnectFailedSocketConnection(ip: String, port: Int, responseTimeout: Int, e: Throwable) =
        log("Error. Reconnect failed $ip:$port, responseTimeout: $responseTimeout", e)

    fun logWriteSucceed(transaction: Transaction) {
        val channel = transaction.channel
        val value = channel!!.readValueWithReadFormula
        log("""[${nowString()}] Write ${channel.debugString(transaction.id)}: $value (${channel.updateInterval}ms)""")
    }

    fun logWriteFormulaEvaluationError(formula: String) {
        error("[${nowString()}] Error. Write formula evaluation $formula")
    }

    fun logReadSuccess(transaction: Transaction) {
        val channelGroup = transaction.channelGroup!!
        log("Read succeed channels " + channelGroup.debugString(transaction.id))
        val channels: Collection<Channel> = channelGroup.channels
        for (channel in channels) log("Read ${channel.name}: ${channel.readValueWithReadFormula} (${channel.updateInterval}ms)")
    }

    fun logReadFailed(transaction: Transaction, exceptionCode: Byte, exceptionMessage: String) {
        val channelGroup = transaction.channelGroup!!
        log("Error. Read failed ${channelGroup.debugString(transaction.id)}, exceptionCode: $exceptionCode,  exceptionMessage: $exceptionMessage")
    }

    fun logReadFormulaEvaluationError(formula: String) {
        error("[${nowString()}] Error. Read formula evaluation $formula")
    }

    fun logDeviceNotValidError(ip: String, port: Int) = log("Error. Device not valid $ip:$port")

    fun log(message: String) {
        /*
        if (targetOs == TargetOs.DESKTOP && message.contains("Error.")) {
            featurea.log(message) // quickfix todo improve
        }
        */
        if (!isEnable) return
        for (source in sources) {
            source.log(message)
        }
    }

    /*internals*/

    private fun log(message: String, e: Throwable) {
        log(message)
    }

}

fun Channel.debugString(transactionId: Short): String {
    val ip = connection.ip ?: "undefined"
    val port = connection.port
    return "$ip:$port/$transactionId/$region/$address"
}

fun ChannelGroup.debugString(transactionId: Short): String {
    val ip: String = connection.ip ?: "undefined"
    val port: Int = connection.port
    return "$ip:$port/$transactionId/$region/$startAddress..$finishAddress"
}
