package featurea.ktor

import com.auth0.jwt.interfaces.DecodedJWT

fun JwtService.findPayloadOrNullQuickfix(jwtToken: String, key: String): String? {
    try {
        val decoder: DecodedJWT = verifier.verify(jwtToken)
        val decoderType: Class<DecodedJWT> = decoder::javaClass.get()
        val payloadField = decoderType.getDeclaredField("payload")
        payloadField.isAccessible = true
        val payload = payloadField.get(decoder)
        val payloadType = payload::javaClass.get()
        val treeField = payloadType.getDeclaredField("tree")
        treeField.isAccessible = true
        val tree: Map<String, Any> = treeField.get(payload) as Map<String, String>
        val emailTextNode = tree[key]!!
        val emailTextNodeType = emailTextNode::javaClass.get()
        val emailField = emailTextNodeType.getDeclaredField("_value")
        emailField.isAccessible = true
        val email = emailField.get(emailTextNode) as String
        return email
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
