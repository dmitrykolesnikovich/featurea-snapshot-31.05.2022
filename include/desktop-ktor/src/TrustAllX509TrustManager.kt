package featurea.ktor

import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

// https://stackoverflow.com/a/66490930/909169
@Suppress("TrustAllX509TrustManager")
class TrustAllX509TrustManager : X509TrustManager {

    override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)

    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {
        // log("[TrustAllX509TrustManager] checkClientTrusted - certs: $certs, authType: $authType")
    }

    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {
        // log("[TrustAllX509TrustManager] checkServerTrusted - certs: $certs, authType: $authType")
    }

}

fun getDefaultJavaTrustManager(): X509TrustManager {
    val factory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    factory.init(null as KeyStore?)
    var customTransactionManager: X509TrustManager? = null
    for (trustManager in factory.trustManagers) {
        if (trustManager is X509TrustManager) {
            customTransactionManager = trustManager
            break
        }
    }
    customTransactionManager = customTransactionManager ?: error("")
    val customTm: X509TrustManager = object : X509TrustManager {

        override fun getAcceptedIssuers(): Array<X509Certificate> = customTransactionManager.acceptedIssuers

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            customTransactionManager.checkServerTrusted(chain, authType)
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            customTransactionManager.checkClientTrusted(chain, authType)
        }

    }

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf(customTm), null)
    SSLContext.setDefault(sslContext)

    return customTransactionManager
}
