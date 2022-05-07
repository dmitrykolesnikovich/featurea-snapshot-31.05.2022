package featurea.modbus.support

import featurea.utils.Encoding.BIG_ENDIAN
import featurea.utils.encodeToFloat
import featurea.utils.encodeToInt
import featurea.utils.encodeToTwoShorts
import kotlin.math.roundToInt

fun ShortArray.toFloat(type: DataType): Float {
    val shortArray: ShortArray = this
    when (type) {
        DataType.Int16 -> {
            check(shortArray.size == 1)
            return Int16(initialValue = shortArray[0].toFloat()).normalizedValue
        }
        DataType.UInt16 -> {
            check(shortArray.size == 1)
            return UInt16(initialValue = shortArray[0].toFloat()).normalizedValue
        }
        DataType.Int32 -> {
            check(shortArray.size == 2)
            val initialValue = shortArray.encodeToInt(encoding = BIG_ENDIAN).toFloat()
            return Int32(initialValue).normalizedValue
        }
        DataType.UInt32 -> {
            check(shortArray.size == 2)
            val initialValue = shortArray.encodeToInt(encoding = BIG_ENDIAN).toFloat()
            return UInt32(initialValue).normalizedValue
        }
        DataType.Float32 -> {
            check(shortArray.size == 2)
            val initialValue = shortArray.encodeToFloat(encoding = BIG_ENDIAN)
            return Float32(initialValue).normalizedValue
        }
    }
}

fun Float.toShortArray(type: DataType): ShortArray {
    val initialValue: Float = this
    val normalizedValue: Float = type.normalize(initialValue)
    return when (type) {
        DataType.Int16 -> shortArrayOf(normalizedValue.toShort())
        DataType.UInt16 -> shortArrayOf(normalizedValue.toShort())
        DataType.Int32 -> normalizedValue.roundToInt().encodeToTwoShorts(encoding = BIG_ENDIAN)
        DataType.UInt32 -> normalizedValue.roundToInt().encodeToTwoShorts(encoding = BIG_ENDIAN)
        DataType.Float32 -> normalizedValue.encodeToTwoShorts(encoding = BIG_ENDIAN)
    }
}
