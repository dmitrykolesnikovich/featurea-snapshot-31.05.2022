package featurea.desktop.compiler

// https://github.com/WeilerWebServices/Bootstrap/blob/master/kotlin/libraries/scripting/jvm-host-test/test/kotlin/script/experimental/jvmhost/test/ResolveDependenciesTest.kt#L115
fun test1() {
    val host = BasicJvmScriptingHost()
    runBlocking {
        val result = host.compiler.invoke(
            StringScriptSource("""featurea.desktop.compiler.X()"""),
            ScriptCompilationConfiguration {
                dependencies(listOf(JvmDependencyFromClassLoader { Context::class.java.classLoader!! }))
            }
        )
        when (result) {
            is ResultWithDiagnostics.Success -> {
                val message: ResultWithDiagnostics<EvaluationResult> = host.evaluator.invoke(result.value)
                when (message) {
                    is ResultWithDiagnostics.Success -> {
                        val evaluationResult: EvaluationResult = message.value
                        val returnValue: ResultValue = evaluationResult.returnValue
                        when (returnValue) {
                            is ResultValue.Value -> {
                                println(returnValue.value)
                            }
                            is ResultValue.Error -> {
                                println("error: ${returnValue.error}")
                            }
                        }
                    }
                }
            }
            is ResultWithDiagnostics.Failure -> {
                println(result.reports.joinToString())
            }
        }
    }
}