package featurea.font.content.samples

import featurea.app.Application
import Dependencies

import featurea.runtime.*
import featurea.launchBlocking

/*constructors*/

// constructor
fun SampleContext(setup: Task) = injectDependencies(dependencies) {
    launchBlocking {
        setup()
    }
}

// constructor
fun ApplicationContext(init: Application.() -> Unit) = injectModule(Context(), dependencies, init)

/*content*/

object Content {
    val arial16Font = "font:/arial16"
    val arial18Font = "font:/arial18"
}

/*dependencies*/

val dependencies =
    Dependencies("featurea.font.samples", include(featurea.font.dependencies, featurea.textureBatch.dependencies)) {
        "Sample2Layer" to ::Sample2Layer
        "Sample3Layer" to ::Sample3Layer
    }
