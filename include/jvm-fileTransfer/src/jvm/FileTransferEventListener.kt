package featurea.fileTransfer

import java.io.File

interface FileTransferEventListener {
    fun onProduceFtpInfo(ftpServer: FtpServer) {}
    fun onConsumeFtpInfo(ftpServer: FtpServer) {}
    fun onUploadFile(ftpServer: FtpServer, file: File) {}
    fun onUploadFileProgress(ftpServer: FtpServer?, file: File?, progress: Double /*0..1*/) {}
    fun onConsumeFile(file: File) {}
    fun onUpdateFtpServers(ftpServers: Set<FtpServer>) {}
}

fun ConsumeFileListener(consume: (file: File) -> Unit): FileTransferEventListener {
    return object : FileTransferEventListener {
        override fun onConsumeFile(file: File) = consume(file)
    }
}