package featurea

@Target(AnnotationTarget.TYPE, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class JsQuickfix

val Bundle.texturePack: MutableMap<String, String>
    get() {
        var texturePack = manifest.map["texturePack"] as MutableMap<String, String>?
        if (texturePack == null) {
            texturePack = mutableMapOf()
            manifest["texturePack"] = texturePack
        }
        return texturePack
    }

expect suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T
