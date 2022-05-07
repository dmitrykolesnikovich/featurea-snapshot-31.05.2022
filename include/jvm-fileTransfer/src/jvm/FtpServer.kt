package featurea.fileTransfer

import featurea.utils.splitAndTrim
import java.lang.NumberFormatException
import java.util.*

class FtpServer(val ip: String, val port: Int, val model: String, val brand: String) {

    val payload: String
        get() = ip + PAYLOAD_DELIMITER + port + PAYLOAD_DELIMITER + model + PAYLOAD_DELIMITER + brand

    override fun toString(): String = "FtpServer{ip='$ip', port=$port, model='$model', brand='$brand'}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val ftpServer = other as FtpServer
        return port == ftpServer.port && ip == ftpServer.ip && model == ftpServer.model && brand == ftpServer.brand
    }

    override fun hashCode(): Int = Objects.hash(ip, port, model, brand)

    companion object {
        private const val PAYLOAD_DELIMITER = "\n"
        fun valueOf(payload: String): FtpServer? {
            val tokens: List<String> = payload.splitAndTrim(PAYLOAD_DELIMITER, 0)
            return if (tokens.size != 4) {
                null
            } else try {
                FtpServer(tokens[0], tokens[1].toInt(), tokens[2], tokens[3])
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                null
            }
        }
    }

}