package featurea.utils

@Target(AnnotationTarget.TYPE, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class JsQuickfix

expect suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T
