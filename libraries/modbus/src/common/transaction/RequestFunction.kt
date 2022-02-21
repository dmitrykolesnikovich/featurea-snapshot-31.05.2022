package featurea.modbus.transaction;

private const val MODBUS_ERROR_DELTA: Byte = 0x80.toByte()

enum class RequestFunction(val code: Byte, val isError: Boolean = code < 0) {
    READ_COILS(1.toByte()),
    READ_COILS_ERROR((1 + MODBUS_ERROR_DELTA).toByte()),
    READ_DISCRETE_INPUTS(2.toByte()),
    READ_DISCRETE_INPUTS_ERROR((2 + MODBUS_ERROR_DELTA).toByte()),
    READ_HOLDING_REGISTERS(3.toByte()),
    READ_HOLDING_REGISTERS_ERROR((3 + MODBUS_ERROR_DELTA).toByte()),
    READ_INPUT_REGISTERS(4.toByte()),
    READ_INPUT_REGISTERS_ERROR((4 + MODBUS_ERROR_DELTA).toByte()),
    WRITE_COIL(5.toByte()),
    WRITE_COIL_ERROR((5 + MODBUS_ERROR_DELTA).toByte()),
    WRITE_HOLDING_REGISTER(6.toByte()),
    WRITE_HOLDING_REGISTER_ERROR((6 + MODBUS_ERROR_DELTA).toByte()),
    WRITE_MULTIPLE_HOLDING_REGISTERS(16.toByte()),
    WRITE_MULTIPLE_HOLDING_REGISTERS_ERROR((16 + MODBUS_ERROR_DELTA).toByte());
}

fun Byte.toFunction(): RequestFunction = RequestFunction.values().find { it.code == this } ?: error("code: $this")