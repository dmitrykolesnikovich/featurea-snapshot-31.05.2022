package featurea.runtime

class ModuleProvider(val type: ProvideType, val canonicalName: String, val builder: ModuleBuilder) {

    val includedModules = mutableMapOf<String, Module>()

    fun include(module: Module) {
        includedModules[module.key.packageId] = module
    }

    fun includeAll(modules: Iterable<Module>) {
        for (module in modules) {
            include(module)
        }
    }

    fun provideModule(runtime: Runtime, container: Container): Module {
        when (type) {
            ProvideType.INJECT -> {
                // existing
                val existingModule: Module? = container.modules.getOrNull(canonicalName)
                if (existingModule != null) {
                    return existingModule
                }

                // newly created
                val newModule = Module(runtime, container)
                container.injectModule(canonicalName, newModule)
                return newModule
            }
            ProvideType.APPEND -> {
                val newModule: Module = Module(runtime, container)
                container.appendModule(canonicalName, newModule)
                return newModule
            }
            ProvideType.REPLACE -> {
                TODO()
            }
        }
    }

    fun findPrimaryModuleOrNull(canonicalName: String): Module? {
        return includedModules[canonicalName.packageId]
    }

}

/*internals*/

private val String.packageId: String
    get() {
        val lastIndexOfDot: Int = lastIndexOf(".")
        if (lastIndexOfDot == -1) return ""
        return substring(0, lastIndexOfDot)
    }
