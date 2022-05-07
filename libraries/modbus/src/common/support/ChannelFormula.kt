package featurea.modbus.support

import featurea.utils.calibrateByFractionSizeToString
import featurea.modbus.config.Channel

@OptIn(ExperimentalStdlibApi::class)
class ChannelFormula(val channel: Channel, val indexScope: IndexScope) {

    var hasReadFormulaError: Boolean = false
        private set
    var hasWriteFormulaError: Boolean = false
        private set

    fun applyWriteValueWithoutWriteFormula(writeValueWithoutWriteFormula: Double): Double = indexScope {
        var writeValueWithWriteFormula: Double = writeValueWithoutWriteFormula
        val writeFormula = channel.writeFormula
        var result = writeValueWithoutWriteFormula
        if (writeFormula != null) {
            try {
                writeFormula.setVariable("\${value}", result)
                result = writeFormula.calculate()
                hasWriteFormulaError = false
            } catch (e: Throwable) {
                hasWriteFormulaError = true
            }
        }
        if (!hasWriteFormulaError) {
            writeValueWithWriteFormula = result
        } else {
            /*onWriteFormulaError()*/
        }
        return@indexScope writeValueWithWriteFormula
    }

    fun applyReadValueWithoutReadFormula(readValueWithoutReadFormula: Double) = indexScope {
        val readFormula = channel.readFormula
        var result: Double = readValueWithoutReadFormula
        if (readFormula != null) {
            try {
                readFormula.setVariable("\${value}", readValueWithoutReadFormula)
                result = readFormula.calculate()
                hasReadFormulaError = false
            } catch (e: Throwable) {
                hasReadFormulaError = true
            }
        }
        if (!hasReadFormulaError) {
            result = result.calibrateByFractionSizeToString(channel.fractionSize).toDouble()
            channel.readValueWithReadFormula = result
        } else {
            /*onReadFormulaError()*/
        }
    }
}
