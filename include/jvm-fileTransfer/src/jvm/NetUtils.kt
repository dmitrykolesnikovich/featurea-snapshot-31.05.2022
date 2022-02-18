package featurea.fileTransfer

import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

val iPAddress: String
    get() = getIPAddress(true)

fun getIPAddress(useVersion4: Boolean): String {
    try {
        val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (anInterface in interfaces) {
            val inetAddresses: List<InetAddress> = Collections.list(anInterface.inetAddresses)
            for (inetAddress in inetAddresses) {
                if (!inetAddress.isLoopbackAddress) {
                    val sAddr = inetAddress.hostAddress
                    val isIPv4 = sAddr.indexOf(':') < 0
                    if (useVersion4) {
                        if (isIPv4) {
                            return sAddr
                        }
                    } else {
                        if (!isIPv4) {
                            return dropIP6ZoneSuffix(sAddr)
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    error("ip address not found")
}

private fun dropIP6ZoneSuffix(ip: String): String {
    val delimiter = ip.indexOf('%')
    return if (delimiter < 0) ip.toUpperCase(Locale.getDefault()) else ip.substring(0, delimiter).toUpperCase(Locale.getDefault())
}