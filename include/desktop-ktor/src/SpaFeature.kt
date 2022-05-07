package featurea.ktor

import featurea.joinUrlPath
import featurea.utils.log
import io.ktor.application.*
import io.ktor.http.CacheControl.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.io.File
import java.util.concurrent.TimeUnit

// https://stackoverflow.com/a/2576735/909169
val disableCacheConfig = CacheConfig {
    CachingOptions(MaxAge(maxAgeSeconds = 0, mustRevalidate = true, visibility = Visibility.Private))
}

// >> just for try todo replace with `disableCacheConfig`
val oneDayCacheConfig = CacheConfig {
    CachingOptions(
        MaxAge(
            maxAgeSeconds = TimeUnit.DAYS.toSeconds(1L).toInt(),
            mustRevalidate = false,
            visibility = Visibility.Public
        )
    )
}
// <<

class SpaFeature {

    class Configuration {
        var staticRootDocs = "static"
        var defaultFile = "index.html"
        var apiUrl = "/api"
    }

    companion object Feature : ApplicationFeature<Application, Configuration, SpaFeature> {

        override val key: AttributeKey<SpaFeature> = AttributeKey<SpaFeature>("featurea.ktor.SpaFeature")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): SpaFeature = with(pipeline) {
            val configuration: Configuration = Configuration().apply(configure)

            routing {
                static("/") {
                    staticRootFolder = File(configuration.staticRootDocs)
                    files("./")
                    default(configuration.defaultFile)
                }
                get("/*") {
                    call.respondRedirect("/")
                }
            }

            intercept(ApplicationCallPipeline.Features) {
                // init
                val staticRootDocs: String = configuration.staticRootDocs
                val requestUri: String = call.request.uri
                val host: String = call.request.host()
                val uri: String = requestUri.replace("${host}/".toRegex(), "")
                val tokens: List<String> = uri.split("/")
                // log("[SpaFeature] uri: $uri")

                // filter
                if (requestUri == "/ws" || requestUri == "/wss") {
                    return@intercept proceed() // quickfix todo improve
                }
                if (uri.startsWith(configuration.apiUrl)) {
                    return@intercept proceed()
                }

                // default
                if (!tokens.last().matches(Regex("[\\S]+\\.[\\S]+"))) {
                    val file = File(staticRootDocs, configuration.defaultFile)
                    // log("[SpaFeature] default: ${file.absolutePath}")
                    call.respondFile(file, oneDayCacheConfig)
                    return@intercept finish()
                }

                // static
                val urlPathString = joinUrlPath(staticRootDocs, tokens.subList(1, tokens.lastIndex))
                val staticFile = File(urlPathString, tokens.last())
                if (staticFile.exists()) {
                    call.respondFile(staticFile, oneDayCacheConfig)
                    return@intercept finish()
                } else {
                    log("[SpaFeature] proceed: $staticFile")
                    return@intercept proceed()
                }
            }

            return SpaFeature()
        }
    }
}

fun Application.installSpaFeature(configure: SpaFeature.Configuration.() -> Unit = {}) {
    install(SpaFeature) {
        configure()
    }
}
