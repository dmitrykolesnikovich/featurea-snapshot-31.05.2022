package featurea.modbus.transaction

import featurea.modbus.config.Channel

const val MODBUS_TCP_PROTOCOL_ID: Short = 0
const val MODBUS_TCP_SLAVE_ID: Byte = 1
const val MODBUS_COIL_ENABLE_VALUE: Short = 0xFF00.toShort()
const val MODBUS_MAX_READ_REGISTER_COUNT: Int = 125
const val MODBUS_MAX_WRITE_REGISTER_COUNT: Int = 120

class Transaction(val request: Request, val channel: Channel?, val channelGroup: ChannelGroup?) {

    lateinit var response: Response
    var startTime: Long = -1L
    var finishTime: Long = -1L
    val deltaTime: Long get() = finishTime - startTime
    val id: Short get() = request.id

    constructor(request: Request, channel: Channel) : this(request, channel, null)

    constructor(request: Request, channelGroup: ChannelGroup) : this(request, null, channelGroup)

}

val Transaction.isReadRequest: Boolean get() = channelGroup != null
val Transaction.isWriteRequest: Boolean get() = channel != null
