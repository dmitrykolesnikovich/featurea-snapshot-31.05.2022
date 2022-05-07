package featurea.modbus.transaction

import featurea.encodeToShortArray
import featurea.modbus.ModbusClient
import featurea.modbus.config.Channel
import featurea.modbus.config.Region
import featurea.modbus.master.Master
import featurea.modbus.support.toFloat
import featurea.utils.encodeToShortArray

@OptIn(ExperimentalStdlibApi::class)
fun ModbusClient.writeChannel(channelName: String, value: Double): Double = config.indexScope {
    if (isDestroy) return@indexScope value
    val channel: Channel = config.findChannelOrNull(channelName) ?: return@indexScope value
    val writeValueWithWriteFormula: Double = channel.formulaService.applyWriteValueWithoutWriteFormula(value)

    if (channel.script != null) { // todo conceptualize
        channel.writeValueWithWriteFormula = writeValueWithWriteFormula
    } else {
        val master = findMasterOrNull(channel.connection) ?: return@indexScope value
        if (channel.formulaService.hasWriteFormulaError) logger.logWriteFormulaEvaluationError(channel.writeFormula!!.value)
        master.beginTransactionWriteChannelValue(channel, writeValueWithWriteFormula)
    }

    return@indexScope writeValueWithWriteFormula
}

fun ModbusClient.writeText(channelName: String, text: String) {
    writeText(channelName, text.encodeToShortArray((text.length + 1) / 2))
}

fun ModbusClient.writeText(channelName: String, text: ShortArray) {
    if (isDestroy) return
    val channel: Channel = config.findChannelOrNull(channelName) ?: return
    val master: Master = findMasterOrNull(channel.connection) ?: return
    master.beginTransactionWriteChannelValue(channel, text)
}

fun Channel.createWriteRequest(transactionId: Short, value: Double): Request {
    return when (region) {
        Region.Coils -> Request.writeCoil(transactionId, address, type, value == 1.0)
        Region.Discretes -> error("region: $region")
        Region.Inputs -> error("region: $region")
        Region.Holdings -> Request.writeHolding(transactionId, address, type, value.toFloat())
        else -> error("region: $region")
    }
}

fun Channel.createWriteRequest(transactionId: Short, textRegisters: ShortArray): Request {
    check(region == Region.Holdings)
    val request: Request = Request.writeHoldings(transactionId, address, textRegisters)
    return request
}
