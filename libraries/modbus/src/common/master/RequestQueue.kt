package featurea.modbus.master

import featurea.*
import featurea.modbus.config.Channel
import featurea.modbus.transaction.*
import featurea.socket.SocketConnection

@OptIn(ExperimentalStdlibApi::class)
class RequestQueue(private val master: Master) {

    private val readTransactionQueue: ArrayDeque<Transaction> = ArrayDeque()
    private val writeTransactionQueue: ArrayDeque<Transaction> = ArrayDeque()
    private var currentTransaction: Transaction? = null
    private var currentTransactionId: AtomicByte = AtomicByte()
    private val channelGroups: MutableList<ChannelGroup> = ArrayList()

    fun completeCurrentTransaction(transactionId: Short): Transaction {
        // log("[RequestQueue] response: ${master.connection} ($transactionId)")
        val currentTransaction: Transaction = currentTransaction ?: error("currentTransaction not defined")
        check(currentTransaction.id == transactionId) { "transaction id not matched" }
        val result: Transaction = currentTransaction
        resetCurrentTransaction()
        return result
    }

    fun produceReadRequests(elapsedTime: Float) {
        master.connection.updateChannelGroups(channelGroups, elapsedTime, master.quota.channelLimit)
        for (channelGroup in channelGroups) {
            produceReadRequest(channelGroup)
        }
        channelGroups.clear()
    }

    fun produceReadRequest(channelGroup: ChannelGroup) = master.modbusClient.config.indexScope {
        if (channelGroup.isVirtual) {
            master.notifier.notify { notificationBuilder ->
                with(channelGroup) {
                    applyResponseVirtual(master.logger, notificationBuilder)
                }
            }
        } else {
            val request: Request = channelGroup.createReadRequest(nextTransactionId().toShort())
            val transaction: Transaction = Transaction(request, channelGroup)
            readTransactionQueue.add(transaction)
        }
    }

    fun produceWriteRequest(channel: Channel, text: ShortArray) {
        val request: Request = channel.createWriteRequest(nextTransactionId().toShort(), text)
        val transaction: Transaction = Transaction(request, channel)
        writeTransactionQueue.add(transaction)
    }

    fun produceWriteRequest(channel: Channel, writeValueWithWriteFormula: Double) {
        channel.writeValueWithWriteFormula = writeValueWithWriteFormula
        val request: Request = channel.createWriteRequest(nextTransactionId().toShort(), writeValueWithWriteFormula)
        val transaction: Transaction = Transaction(request, channel)
        writeTransactionQueue.add(transaction)
    }

    fun reset() {
        currentTransactionId.setValue(0)
        readTransactionQueue.clear()
        writeTransactionQueue.clear()
        master.connection.hasRequest = false
        resetCurrentTransaction() // the line I spent 4 hours on
    }

    private fun request(transaction: Transaction) {
        val socketConnection = master.socketConnection ?: return
        val bytes: ByteArray = transaction.request.message
        transaction.startTime = getTimeMillis().toLong()

        val bytesText: String = bytes.toList().toString()
        master.logger.log("send $bytesText")

        // log("[RequestQueue] request: ${master.connection} (${transaction.id})")
        if (socketConnection.write(bytes)) {
            master.connection.hasRequest = true
            master.shouldRequest = false
            when {
                transaction.isWriteRequest -> master.writeRequestCount++
                transaction.isReadRequest -> master.readRequestCount++
            }
            // log("[${master}] [${nowString()}] request: complete")
        }
    }

    private fun nextTransactionId(): Int = currentTransactionId.incrementAndGet().toInt()

    fun requestTransactionFromQueue() {
        val socketConnection: SocketConnection = master.socketConnection ?: return
        if (!socketConnection.isConnected()) return
        val currentTransaction: Transaction? = currentTransaction
        if (currentTransaction != null) {
            // log("[RequestQueue] cancel: ${master.connection} (${currentTransaction.id})")
            return
        }
        if (writeTransactionQueue.isNotEmpty()) {
            requestWriteTransactionFromQueue()
        } else {
            requestReadTransactionFromQueue()
        }
    }

    private fun requestReadTransactionFromQueue() {
        resetCurrentTransaction()
        currentTransaction = readTransactionQueue.popOrNull()
        val currentTransaction = currentTransaction
        if (currentTransaction != null) {
            request(currentTransaction)
        }
    }

    private fun requestWriteTransactionFromQueue() {
        resetCurrentTransaction()
        currentTransaction = writeTransactionQueue.popOrNull()
        val currentTransaction = currentTransaction
        if (currentTransaction != null) {
            request(currentTransaction)
        }
    }

    private fun resetCurrentTransaction() {
        currentTransaction = null
    }

}
