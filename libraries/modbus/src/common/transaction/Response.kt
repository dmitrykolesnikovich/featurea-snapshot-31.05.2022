package featurea.modbus.transaction

import featurea.*
import featurea.Encoding.BIG_ENDIAN
import featurea.modbus.transaction.RequestFunction.*

class Response internal constructor(
    val transactionId: Short,
    val bytes: ByteArray?,
    val error: ResponseError? = null,
    val allBytes: List<Byte>
) {
    val isError: Boolean = error != null
    val values: ShortArray? = bytes?.encodeToShortArray(encoding = BIG_ENDIAN)
    val value: Short get() = values!![0]

    companion object {

        fun readOrNull(queue: ByteQueue): Response? {
            val responseBytes = mutableListOf<Byte>()

            queue.mark()
            if (queue.size < 6) {
                queue.reset()
                return null
            }

            val transactionId: Short = queue.popShort()
            val protocolId: Short = queue.popShort()
            check(protocolId == MODBUS_TCP_PROTOCOL_ID)
            val count: Short = queue.popShort()
            check(count > 0)

            if (queue.size < count) {
                queue.reset()
                return null
            }

            val slaveId: Byte = queue.popByte()
            check(slaveId == MODBUS_TCP_SLAVE_ID)
            val functionCode: Byte = queue.popByte()
            val function: RequestFunction = functionCode.toFunction()

            responseBytes.addAll(transactionId.encodeToTwoBytes(Encoding.BIG_ENDIAN).toList())
            responseBytes.addAll(protocolId.encodeToTwoBytes(Encoding.BIG_ENDIAN).toList())
            responseBytes.addAll(count.encodeToTwoBytes(Encoding.BIG_ENDIAN).toList())
            responseBytes.add(slaveId)
            responseBytes.add(functionCode)

            if (function.isError) {
                val errorCode: Byte = queue.popByte()
                responseBytes.add(errorCode)
                val error: ResponseError = errorCode.toError()
                return Response(transactionId, bytes = null, error = error, allBytes = responseBytes)
            } else {
                return when (function) {
                    READ_COILS, READ_DISCRETE_INPUTS, READ_HOLDING_REGISTERS, READ_INPUT_REGISTERS -> {
                        val countByte = queue.popByte()
                        val count: Int = countByte.toUByte().toInt()
                        check(queue.size >= count)
                        val bytes = ByteArray(count)
                        queue.pop(bytes)
                        responseBytes.add(countByte)
                        responseBytes.addAll(bytes.toList())
                        Response(transactionId, bytes, allBytes = responseBytes)
                    }
                    WRITE_COIL, WRITE_HOLDING_REGISTER -> {
                        val offset: Short = queue.popShort()
                        val value: Short = queue.popShort()
                        val twoBytes: ByteArray = value.encodeToTwoBytes(encoding = BIG_ENDIAN)
                        responseBytes.addAll(offset.encodeToTwoBytes(Encoding.BIG_ENDIAN).toList())
                        responseBytes.addAll(twoBytes.toList())
                        Response(transactionId, twoBytes, allBytes = responseBytes)
                    }
                    WRITE_MULTIPLE_HOLDING_REGISTERS -> {
                        val offset: Short = queue.popShort()
                        val sizeOfWrittenRegisters: Short = queue.popShort()
                        emptyResponse(transactionId) // todo make use of `offset` and `sizeOfWrittenRegisters`
                    }
                    else -> error(function)
                }
            }
        }

    }

}

fun emptyResponse(transactionId: Short): Response = Response(transactionId, ByteArray(0), allBytes = emptyList())