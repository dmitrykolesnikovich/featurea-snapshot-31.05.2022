package featurea.fileTransfer

import featurea.splitAndTrim
import java.io.*
import java.net.*
import java.util.*
import kotlin.math.min

/**
 * Mobile                                           Desktop
 * datagramSocket.produceFtpInfo (1)        ->      multicastSocket.consumeFtpInfo (2)
 * serverSocket.consumeFile (4)             <-      socket.produceFile (3)
 *
 * ping ftp servers and remove outdated
 *
 * Example:
 *
 * FileTransferTool mobileFileTransferComponent = FileTransferTool.createNewMobileFileTransferComponent(); // mobile side
 * FileTransferTool desktopFileTransferComponent = FileTransferTool.createNewDesktopFileTransferComponent(); // desktop side
 * Thread.sleep(3000);
 * System.out.println(desktopFileTransferComponent.getFtpServers());
 * desktopFileTransferComponent.uploadFile(ftpServer, file);
`*/
class FileTransfer : FileTransferEventListener {

    var broadcastTimeout: Long = 200
    lateinit var cacheDir: String
    lateinit var ftpServerModel: String
    lateinit var ftpServerBrand: String
    private var datagramSocket: DatagramSocket? = null
    private var multicastSocket: MulticastSocket? = null
    private lateinit var serverSocket: ServerSocket
    private val ftpServers: MutableSet<FtpServer> = LinkedHashSet()
    private lateinit var ftpInfoProducerThread: Thread
    private var isFtpInfoProducerThreadDestroyed = false
    private var isFtpInfoProducerThreadStarted = false
    private lateinit var ftpInfoConsumerThread: Thread
    private var isFtpInfoConsumerThreadDestroyed = false
    private var fileConsumerThread: Thread? = null
    private var isFileConsumerThreadDestroyed = false
    private val mobileMonitor = Object()
    private lateinit var pingFtpServersThread: Thread

    fun createBroadcast(broadcastTimeout: Long, cacheDir: String, ftpServerModel: String, ftpServerBrand: String): FileTransfer {
        this.broadcastTimeout = broadcastTimeout
        this.cacheDir = cacheDir
        this.ftpServerModel = ftpServerModel
        this.ftpServerBrand = ftpServerBrand
        createNewFileConsumer()
        createNewFtpInfoProducer()
        return this
    }

    fun destroyBroadcast() {
        stopBroadcast()
        destroyFtpInfoProducer()
        destroyFileConsumer()
    }

    fun startBroadcast() {
        isFtpInfoProducerThreadStarted = true
        synchronized(mobileMonitor) {
            mobileMonitor.notify()
        }
    }

    fun stopBroadcast() {
        isFtpInfoProducerThreadStarted = false
    }

    fun getFtpServers(): Collection<FtpServer> {
        clearOutdatedFtpServers()
        return ftpServers
    }

    fun createUploader(): FileTransfer {
        if (::ftpInfoConsumerThread.isInitialized) return this

        try {
            multicastSocket = MulticastSocket(4444)
            val networkInterface = retrieveNetworkInterfaceByIpPrefix("192.168.")
            multicastSocket!!.joinGroup(InetSocketAddress(multicastIp, 0), networkInterface)
            ftpInfoConsumerThread = object : Thread() {
                private val buffer = ByteArray(100)
                override fun run() {
                    while (!isFtpInfoConsumerThreadDestroyed) {
                        try {
                            val datagramPacket = DatagramPacket(buffer, buffer.size)
                            /*System.out.println("createUploader #1");*/multicastSocket!!.receive(datagramPacket)
                            /*System.out.println("createUploader #2");*/
                            val data = datagramPacket.data
                            val message = String(data, 0, datagramPacket.length)
                            /*System.out.println("createUploader #3 message: " + message);*/
                            val headerAndPayload: List<String> = message.splitAndTrim(
                                MESSAGE_PREFIX, 2
                            )
                            val header = headerAndPayload[0]
                            /*System.out.println("createUploader #4 header: " + header);*/
                            val payload = headerAndPayload[1]
                            when (header) {
                                FTP_INFO_HEADER -> {
                                    val ftpServer = FtpServer.valueOf(payload)
                                    consumeFtpInfo(ftpServer)
                                }
                            }
                        } catch (skip: SocketException) {
                            // no op
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            ftpInfoConsumerThread.start()
            pingFtpServersThread = object : Thread() {
                override fun run() {
                    while (!isFtpInfoConsumerThreadDestroyed) {
                        clearOutdatedFtpServers()
                        try {
                            sleep(2000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            pingFtpServersThread.start() // todo uncomment
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return this
    }

    fun destroyUploader() {
        try {
            multicastSocket!!.close()
        } finally {
            isFtpInfoConsumerThreadDestroyed = true
        }
    }

    private fun createNewFtpInfoProducer() {
        if (::ftpInfoProducerThread.isInitialized) return

        try {
            datagramSocket = DatagramSocket()
            ftpInfoProducerThread = object : Thread() {
                override fun run() {
                    while (!isFtpInfoProducerThreadDestroyed) {
                        while (!isFtpInfoProducerThreadStarted) {
                            synchronized(mobileMonitor) {
                                try {
                                    mobileMonitor.wait()
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        produceFtpInfo()
                        try {
                            sleep(broadcastTimeout)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            ftpInfoProducerThread.start()
        } catch (e: SocketException) {
            e.printStackTrace()
        }
    }

    private fun destroyFtpInfoProducer() {
        try {
            datagramSocket!!.close()
        } finally {
            isFtpInfoProducerThreadDestroyed = true
        }
    }

    private fun produceFtpInfo(): FtpServer? {
        return try {
            val port = serverSocket!!.localPort
            val ftpServer = FtpServer(iPAddress, port, ftpServerModel, ftpServerBrand)
            val buffer = (FTP_INFO_HEADER + MESSAGE_PREFIX + ftpServer.payload).toByteArray()
            val packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(multicastIp), multicastPort)
            datagramSocket!!.send(packet)
            onProduceFtpInfo(ftpServer)
            ftpServer
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun clearOutdatedFtpServers() {
        synchronized(ftpServers) {
            val startSize = ftpServers.size
            val iterator = ftpServers.iterator()
            while (iterator.hasNext()) {
                val ftpServer = iterator.next()
                try {
                    send(ftpServer, PING_HEADER, null)
                } catch (skip: Throwable) {
                    // no op
                    iterator.remove()
                }
            }
            val finishSize = ftpServers.size
            if (startSize != finishSize) {
                onUpdateFtpServers(ftpServers)
            }
        }
    }

    private fun consumeFtpInfo(ftpServer: FtpServer?) {
        if (ftpServer == null) {
            return
        }
        synchronized(ftpServers) {
            if (ftpServers.add(ftpServer)) {
                onConsumeFtpInfo(ftpServer)
                onUpdateFtpServers(ftpServers)
            }
        }
    }

    fun uploadFile(ftpServer: FtpServer, file: File) {
        try {
            send(ftpServer, UPLOAD_FILE_HEADER, file)
            onUploadFile(ftpServer, file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun send(ftpServer: FtpServer, header: String, payload: Any?) {
        var bufferedOutputStream: BufferedOutputStream? = null
        var dataOutputStream: DataOutputStream? = null
        try {
            val socket = Socket(ftpServer.ip, ftpServer.port)
            val outputStream = socket.getOutputStream()
            bufferedOutputStream = BufferedOutputStream(outputStream)
            dataOutputStream = DataOutputStream(bufferedOutputStream)
            dataOutputStream.writeUTF(header)
            if (payload is File) {
                val file = payload
                dataOutputStream.writeUTF(file.name)
                // >>
                // 1)
                /*dataOutputStream.write(getBytes(file));*/
                // 2)
                val inputStream = FileInputStream(file)
                val bytes = ByteArray(4096)
                val total = file.length()
                var current: Long = 0
                var length: Int
                while (inputStream.read(bytes).also { length = it } != -1) {
                    dataOutputStream.write(bytes, 0, length)
                    current += length.toLong()
                    val progress = min(0.99, current.toDouble() / total.toDouble())
                    /*println("progress: $progress")*/
                    onUploadFileProgress(ftpServer, file, progress)
                }
                onUploadFileProgress(ftpServer, file, 1.0)
                // <<
            }
            dataOutputStream.flush()
        } finally {
            try {
                dataOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                bufferedOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createNewFileConsumer() {
        if (fileConsumerThread != null) {
            return
        }
        try {
            serverSocket = ServerSocket(0)
            fileConsumerThread = object : Thread() {
                override fun run() {
                    while (!isFileConsumerThreadDestroyed) {
                        try {
                            val clientSocket = serverSocket!!.accept()
                            val inputStream = clientSocket.getInputStream()
                            var bufferedInputStream: BufferedInputStream? = null
                            var dataInputStream: DataInputStream? = null
                            try {
                                bufferedInputStream = BufferedInputStream(inputStream)
                                dataInputStream = DataInputStream(bufferedInputStream)
                                val header = dataInputStream.readUTF()
                                when (header) {
                                    UPLOAD_FILE_HEADER -> {
                                        val fileName = dataInputStream.readUTF()
                                        val to = newFile("$cacheDir/$fileName")
                                        consumeFile(to, dataInputStream)
                                    }
                                    PING_HEADER -> {
                                    }
                                }
                            } catch (skip: SocketException) {
                                /*breakpoint()*/
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            } finally {
                                try {
                                    dataInputStream?.close()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                                try {
                                    bufferedInputStream?.close()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (skip: SocketException) {
                            /*breakpoint()*/
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        fileConsumerThread!!.start()
    }

    private fun destroyFileConsumer() {
        try {
            serverSocket!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            isFileConsumerThreadDestroyed = true
        }
    }

    private fun consumeFile(to: File, from: InputStream) {
        copy(from, to)
        onConsumeFile(to)
    }

    /*Adapter API*/
    private val listeners: MutableList<FileTransferEventListener> = ArrayList()
    fun addFileTransferComponentListener(listener: FileTransferEventListener) {
        listeners.add(listener)
    }

    fun removeFileTransferComponentListener(listener: FileTransferEventListener) {
        listeners.add(listener)
    }

    override fun onProduceFtpInfo(ftpServer: FtpServer) {
        for (listener in listeners) {
            listener.onProduceFtpInfo(ftpServer)
        }
    }

    override fun onConsumeFtpInfo(ftpServer: FtpServer) {
        for (listener in listeners) {
            listener.onConsumeFtpInfo(ftpServer)
        }
    }

    override fun onUploadFile(ftpServer: FtpServer, file: File) {
        for (listener in listeners) {
            listener.onUploadFile(ftpServer, file)
        }
    }

    override fun onUploadFileProgress(ftpServer: FtpServer?, file: File?, progress: Double) {
        for (listener in listeners) {
            listener.onUploadFileProgress(ftpServer, file, progress)
        }
    }

    override fun onConsumeFile(file: File) {
        for (listener in listeners) {
            listener.onConsumeFile(file)
        }
    }

    override fun onUpdateFtpServers(ftpServers: Set<FtpServer>) {
        for (listener in listeners) {
            listener.onUpdateFtpServers(ftpServers)
        }
    }

    companion object {

        private const val MESSAGE_PREFIX = "\n\n"
        private const val FTP_INFO_HEADER = "FTP_INFO_HEADER"
        private const val UPLOAD_FILE_HEADER = "UPLOAD_FILE_HEADER"
        private const val PING_HEADER = "PING_HEADER"
        private const val multicastIp = "230.0.0.1"
        private const val multicastPort = 4444

        private fun retrieveNetworkInterfaceByIpPrefix(ipPrefix: String): NetworkInterface? {
            try {
                for (networkInterface in Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    for (inetAddress in Collections.list(networkInterface.inetAddresses)) {
                        if (inetAddress is Inet4Address) {
                            if (inetAddress.hostAddress.startsWith(ipPrefix)) {
                                return networkInterface
                            }
                        }
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
            return null
        }

    }
}