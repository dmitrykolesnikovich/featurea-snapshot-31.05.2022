package featurea

/*
// https://www.youtube.com/watch?v=EuRy4L4WixU
fun bootstrap(delegate: Component.() -> ApplicationDelegate, includes: DependencyBuilder.() -> Unit) = Runtime {
    exportComponents(DefaultArtifact(includes))
    injectContainer("featurea.ApplicationContainer")
    injectModule("featurea.ApplicationModule")
    complete { appModule ->
        val app: Application = appModule.importComponent()
        app.delegate = app.delegate()
    }
}

fun bootstrap(delegate: Component.() -> ApplicationDelegate) = bootstrapApplication(export = artifact) {
    val app: Application = import()
    app.delegate = app.delegate()
}
*/
