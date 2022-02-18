package featurea.modbus.transaction;

enum class ResponseError(val code: Byte, val message: String) {
    ILLEGAL_FUNCTION(0x1.toByte(), "Illegal function"),
    ILLEGAL_DATA_ADDRESS(0x2.toByte(), "Illegal data address"),
    ILLEGAL_DATA_VALUE(0x3.toByte(), "Illegal data value"),
    SLAVE_DEVICE_FAILURE(0x4.toByte(), "Slave device failure"),
    ACKNOWLEDGE(0x5.toByte(), "Acknowledge"),
    SLAVE_DEVICE_BUSY(0x6.toByte(), "Slave device busy"),
    MEMORY_PARITY_ERROR(0x8.toByte(), "Memory parity error"),
    GATEWAY_PATH_UNAVAILABLE(0xa.toByte(), "Gateway path unavailable"),
    GATEWAY_TARGET_DEVICE_FAILED_TO_RESPOND(0xb.toByte(), "Gateway target device failed to respond");
}

fun Byte.toError(): ResponseError = ResponseError.values().find { it.code == this } ?: error("code: $this")