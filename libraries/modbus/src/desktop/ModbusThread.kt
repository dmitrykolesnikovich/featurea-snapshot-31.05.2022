package featurea.modbus

import featurea.log

class ModbusThread(modbusClient: ModbusClient) {

    private val requestTransactionFromQueueThread = ModbusClientThread(modbusClient) {
        modbusClient.requestTransactionFromQueue()
    }

    private val updateQueuesThread = ModbusClientThread(modbusClient) { elapsedTime: Float ->
        modbusClient.updateQueues(elapsedTime)
    }

    fun start() {
        requestTransactionFromQueueThread.start()
        updateQueuesThread.start()
    }

    fun update() {
        requestTransactionFromQueueThread.update()
        updateQueuesThread.update()
    }

}

/*internals*/

private class ModbusClientThread(val modbusClient: ModbusClient, val action: (elapsedTime: Float) -> Unit) : Thread() {

    private var now: Long = 0
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lockObject: Object = Object()

    override fun run() {
        try {
            while (!modbusClient.isDestroy) {
                val nanoTime = System.nanoTime()
                if (now == 0L) {
                    now = nanoTime
                }
                val elapsedTime: Float = (nanoTime - now) / 1_000_000f
                now = nanoTime
                if (modbusClient.isConnect) {
                    action(elapsedTime)
                    waitWhileShouldRequest()
                } else {
                    waitUntilConnected()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun update() {
        synchronized(lockObject) {
            lockObject.notify()
        }
    }

    /*internals*/

    private fun waitWhileShouldRequest() {
        synchronized(lockObject) {
            try {
                lockObject.wait(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun waitUntilConnected() {
        try {
            while (!modbusClient.isConnect && !modbusClient.isDestroy) {
                synchronized(lockObject) {
                    try {
                        lockObject.wait()
                    } catch (e: InterruptedException) {
                        log(e.localizedMessage)
                    }
                }
            }
        } finally {
            now = System.nanoTime()
        }
    }

}
