package featurea.cameraView

import com.pedro.vlc.VlcListener

fun String.parseUsernameAndPassword(): Pair<String?, String?> {
    var username: String? = null
    var password: String? = null
    var string = replace("http://", "")
    string = string.substring(0, string.indexOf('/'))
    if (string.contains("@")) {
        val tokens = string.split("@").toTypedArray()
        val usernameAndPassword = tokens[0].split(":").toTypedArray()
        username = usernameAndPassword[0]
        if (usernameAndPassword.size > 1) {
            password = usernameAndPassword[1]
        }
    }
    return username to password
}

open class VlcAdapter : VlcListener {
    override fun onComplete() {}
    override fun onError() {}
}
