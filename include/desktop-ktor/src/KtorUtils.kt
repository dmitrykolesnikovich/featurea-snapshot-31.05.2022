package featurea.ktor

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import java.text.DateFormat

typealias PipelineBlock = suspend PipelineContext<*, ApplicationCall>.() -> Unit

fun CacheConfig(cachingOptions: () -> CachingOptions): OutgoingContent.() -> Unit = { caching = cachingOptions() }

@OptIn(KtorExperimentalAPI::class)
inline operator fun <reified T> ApplicationConfig.get(key: String): T {
    val (config, property) = key.split(":")
    val value = config(config).property(property)
    return when (T::class) {
        Boolean::class -> value.getString().toBoolean() as T
        String::class -> value.getString() as T
        List::class -> value.getList() as T
        Int::class -> value.getString().toInt() as T
        Long::class -> value.getString().toLong() as T
        else -> error("value: $value")
    }
}

@OptIn(KtorExperimentalAPI::class)
fun sha256(password: String): ByteArray {
    val digester = getDigestFunction("SHA-256") { text -> "cloudscada${text.length}" } // todo replace `cloudscada`
    val result = digester(password)
    return result
}

val Application.config: ApplicationConfig get() = environment.config

fun Application.installJsonRest() {
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }
}
