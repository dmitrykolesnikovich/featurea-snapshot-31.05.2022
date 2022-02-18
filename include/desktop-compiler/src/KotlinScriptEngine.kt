package featurea.compiler

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import java.io.Writer
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class KotlinScriptEngine(classLoader: ClassLoader = ClassLoader.getSystemClassLoader()) {

    init {
        // https://stackoverflow.com/a/61865167/909169
        // https://discuss.kotlinlang.org/t/kotlin-script-engine-error/5654/2
        setIdeaIoUseFallback()
    }

    private val scriptEngine: ScriptEngine = ScriptEngineManager(classLoader).getEngineByExtension("kts")

    init {
        scriptEngine.eval("Unit") // to initialize script engine eagerly
    }

    fun <T> eval(script: String): T {
        val result: Any? = scriptEngine.eval(script)
        return result as T
    }

    var writer: Writer?
        set(value) {
            scriptEngine.context.writer = value
        }
        get() {
            return scriptEngine.context.writer
        }

    var errorWriter: Writer?
        set(value) {
            scriptEngine.context.errorWriter = value
        }
        get() {
            return scriptEngine.context.errorWriter
        }

}
