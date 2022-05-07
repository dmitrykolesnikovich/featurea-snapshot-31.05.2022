package featurea.ktor

import featurea.utils.log
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

class Post(val call: ApplicationCall, val principal: UserIdPrincipal, val parameters: Parameters)
class Get(val call: ApplicationCall, val principal: UserIdPrincipal, val parameters: Parameters)
class Delete(val call: ApplicationCall, val principal: UserIdPrincipal)

fun <T> Route.POST(path: String, block: suspend Post.() -> T) {
    post(path) {
        log("POST: $path")
        findPrincipalOrRespondForbidden { principal ->
            try {
                val parameters = call.receiveParameters()
                val post = Post(call, principal, parameters)
                val result = post.block()
                call.respondResult(result)
            } catch (e: Throwable) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Failure")
            }
        }
    }
}

fun <T> Route.GET(path: String, block: suspend Get.() -> T) {
    get(path) {
        findPrincipalOrRespondForbidden { principal ->
            val parameters = call.request.queryParameters
            val get = Get(call, principal, parameters)
            val result: T = get.block()
            call.respondResult(result)
        }
    }
}

fun Route.DELETE(path: String, block: suspend Delete.() -> Unit) {
    delete(path) {
        findPrincipalOrRespondForbidden { principal ->
            val delete = Delete(call, principal)
            val result = delete.block()
            call.respondResult(result)
        }
    }
}

suspend fun ApplicationCall.respondResult(result: Any?) {
    when (result) {
        null -> respond(HttpStatusCode.Unauthorized, "Failure")
        false -> respond(HttpStatusCode.Unauthorized, "Failure")
        true -> respond(HttpStatusCode.OK, "Success")
        Unit -> respond(HttpStatusCode.OK, "Success")
        else -> respond(HttpStatusCode.OK, result)
    }
}

fun ApplicationCall.findJwtToken(): String {
    val authHeader = request.parseAuthorizationHeader() as HttpAuthHeader.Single
    return authHeader.blob
}

suspend fun PipelineContext<*, ApplicationCall>.findPrincipalOrRespondForbidden(block: PrincipalBlock) {
    val principal: UserIdPrincipal? = call.principal()
    if (principal != null) {
        block(principal)
    } else {
        call.respond(HttpStatusCode.Forbidden, "User not logged in")
    }
}

/*internals*/

private typealias PrincipalBlock = suspend PipelineContext<*, ApplicationCall>.(UserIdPrincipal) -> Unit
