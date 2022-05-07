package featurea.modbus.transaction

import featurea.utils.ByteQueue
import featurea.utils.Encoding.BIG_ENDIAN
import featurea.utils.encodeToByteArray
import featurea.modbus.support.DataType
import featurea.modbus.support.toShortArray
import featurea.modbus.transaction.RequestFunction.*

class Request private constructor(val id: Short, val message: ByteArray) {

    companion object {

        fun writeCoil(id: Short, address: Short, type: DataType, value: Boolean): Request =
            Request(id, request(id, WRITE_COIL, address, coilValue = value, type = type))

        fun writeHolding(id: Short, address: Short, type: DataType, value: Float): Request =
            Request(id, request(id, WRITE_HOLDING_REGISTER, address, value = value, type = type))

        fun writeHoldings(id: Short, address: Short, values: ShortArray): Request =
            Request(id, request(id, WRITE_MULTIPLE_HOLDING_REGISTERS, address, values = values))

        fun readCoils(id: Short, address: Short, count: Short): Request =
            Request(id, request(id, READ_COILS, address, count))

        fun readDiscretes(id: Short, address: Short, count: Short): Request =
            Request(id, request(id, READ_DISCRETE_INPUTS, address, count))

        fun readHoldings(id: Short, address: Short, count: Short): Request =
            Request(id, request(id, READ_HOLDING_REGISTERS, address, count))

        fun readInputs(id: Short, address: Short, count: Short): Request =
            Request(id, request(id, READ_INPUT_REGISTERS, address, count))

        /*internals*/

        private fun request(
            transactionId: Short,
            function: RequestFunction,
            offset: Short,
            count: Short = -1,
            coilValue: Boolean = false,
            value: Float = -1f,
            values: ShortArray? = null,
            type: DataType? = null
        ): ByteArray {
            val result: ByteQueue = ByteQueue()
            result.pushShort(transactionId)
            result.pushShort(MODBUS_TCP_PROTOCOL_ID)
            val payload: ByteQueue = ByteQueue()
            payload.pushByte(MODBUS_TCP_SLAVE_ID)
            payload.pushByte(function.code)
            payload.pushShort(offset)
            when (function) {
                READ_COILS, READ_DISCRETE_INPUTS, READ_HOLDING_REGISTERS, READ_INPUT_REGISTERS -> {
                    payload.pushShort(count)
                }
                WRITE_COIL -> {
                    payload.pushShort(if (coilValue) MODBUS_COIL_ENABLE_VALUE else 0)
                }
                WRITE_HOLDING_REGISTER -> {
                    val shortArray: ShortArray = value.toShortArray(checkNotNull(type))
                    val bytes = shortArray.encodeToByteArray(encoding = BIG_ENDIAN)
                    payload.pushAll(bytes)
                }
                WRITE_MULTIPLE_HOLDING_REGISTERS -> {
                    checkNotNull(values)
                    payload.pushShort(values.size.toShort())
                    val bytes: ByteArray = values.encodeToByteArray(encoding = BIG_ENDIAN)
                    payload.pushByte(bytes.size.toByte())
                    payload.pushAll(bytes)
                }
            }
            result.pushShort(payload.size.toShort())
            result.pushAll(payload)
            return result.toByteArray()
        }

    }
}
