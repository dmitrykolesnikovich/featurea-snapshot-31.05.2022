package featurea.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.routing.*
import java.util.*

class JwtService(val issuer: String, secret: String) {

    private val algorithm = Algorithm.HMAC512(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(issuer).build()

    fun generateToken(email: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000 * 24 * 20)) // quickfix todo improve
        .sign(algorithm)

}

fun Route.jwtAuthenticate(init: Route.() -> Unit) {
    authenticate("jwt-auth") {
        init()
    }
}

fun Application.jwtAuthenticatedRouting(path: String, init: Route.() -> Unit) {
    routing {
        jwtAuthenticate {
            route(path) {
                init()
            }
        }
    }
}


fun Application.installJwtAuthentication(jwtService: JwtService) {
    authentication {
        jwt("jwt-auth") {
            verifier(jwtService.verifier)
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                UserIdPrincipal(email)
            }
        }
    }
}
