package featurea.modbus.master

import featurea.*
import featurea.modbus.ModbusClient
import featurea.modbus.transaction.*
import featurea.runtime.import
import featurea.socket.SocketConnectionResponseListener

/*
graphics thread:  produce, request and consume
network thread:   response
*/
class ResponseQueue(private val master: Master) : SocketConnectionResponseListener() {

    private val transactionsMap: BufferedMap<Short, Transaction> = BufferedMap()
    private val modbusClient = master.import<ModbusClient>()

    override fun onResponse(byteQueue: ByteQueue) {
        if (byteQueue.isEmpty) {
            log("[${master}] [${nowString()}] byteQueue is empty")
        }
        var response: Response?
        while (true) {
            response = Response.readOrNull(byteQueue)
            if (response == null) {
                break
            }
            val transaction: Transaction = master.commitTransaction(response.transactionId)
            transaction.response = response
            transaction.finishTime = getTimeMillis().toLong()
            if (transaction.isReadRequest) {
                transactionsMap.put(transaction.id, transaction)
                val responseText: String = response.allBytes.toString()
                master.logger.log("recv (${master.connection.ip}:${master.connection.port}) $responseText")
            }
            master.connection.hasResponse = true
            master.shouldRequest = true
        }
    }

    fun consumeResponses() = modbusClient.config.indexScope {
        val usedRespondedTransactions: MutableMap<Short, Transaction> = transactionsMap.swapBuffers()
        for (transaction in usedRespondedTransactions.values) {
            val channelGroup: ChannelGroup? = transaction.channelGroup
            if (channelGroup != null) {
                with(channelGroup) {
                    master.logger.log("[Response Time] [${nowString()}] ${channelGroup.debugString(transaction.id)}: ${transaction.deltaTime}ms")
                    if (!transaction.response.isError) {
                        if (master.notifier.isNotEmpty) {
                            master.notifier.notify { notificationBuilder ->
                                applyResponse(master.logger, transaction, notificationBuilder)
                            }
                        } else {
                            applyResponse(master.logger, transaction, null)
                        }
                        master.logger.logReadSuccess(transaction)
                    } else {
                        val error: ResponseError? = transaction.response.error
                        if (error != null) {
                            master.logger.logReadFailed(transaction, error.code, error.message)
                        }
                    }
                }
            }
            if (transaction.channel != null && !transaction.response.isError) {
                master.logger.logWriteSucceed(transaction)
            }
        }
        usedRespondedTransactions.clear()
    }

    fun reset() {
        transactionsMap.clear()
        master.connection.hasResponse = false
    }

}
