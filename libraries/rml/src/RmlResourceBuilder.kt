package featurea.rml

import featurea.app.ApplicationDelegate
import featurea.content.ResourceSchema
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.script.Script
import featurea.utils.Scope

typealias ApplicationBuilder = DefaultRmlResourceBuilder<ApplicationDelegate>

typealias RmlBuilder = RmlResourceBuilder<Any, Any, Any>

typealias RmlBuilderInit = () -> RmlBuilder

interface RmlResourceBuilder<CreateResult : Any, BuildResult : Any, WrapResult : Any> {
    suspend fun create(rmlResource: RmlResource, rmlTag: RmlTag, scope: Scope): CreateResult
    suspend fun build(rmlResource: RmlResource, rmlTag: RmlTag, scope: Scope, root: CreateResult): BuildResult
    suspend fun wrap(rmlResource: RmlResource, rmlTag: RmlTag, origin: BuildResult): WrapResult
    suspend fun attributeOn(tag: RmlTag, scope: Scope, root: Any, key: String, value: String)
    suspend fun attributeOff(tag: RmlTag, scope: Scope, root: Any, key: String, value: String)
    suspend fun propertyOn(tag: RmlTag, scope: Scope, root: Any, key: String, property: RmlTag, origin: Any)
    suspend fun propertyOff(tag: RmlTag, scope: Scope, root: Any, key: String, property: RmlTag, origin: Any)
    suspend fun append(tag: RmlTag, scope: Scope, root: Any, child: RmlTag, origin: Any)
    suspend fun insert(tag: RmlTag, scope: Scope, root: Any, index: Int, child: RmlTag, origin: Any)
    suspend fun remove(tag: RmlTag, scope: Scope, root: Any, child: RmlTag, origin: Any)
    suspend fun replace(tag: RmlTag, scope: Scope, root: Any, child: RmlTag, index: Int, origin: Any)
}

/*convenience*/

open class DefaultRmlResourceBuilder<T : Any>(override val module: Module) : Component, RmlResourceBuilder<Script, Any, T> {

    override suspend fun create(rmlResource: RmlResource, rmlTag: RmlTag, scope: Scope): Script {
        val rmlSchema: ResourceSchema = rmlResource.rmlFile.rmlSchema
        val name: String = rmlTag.name
        val docketKey: String = rmlSchema.findSuperKeyForKeyOrNull(name) ?: name
        val canonicalClassName: String = rmlSchema.canonicalClassNameByKey[docketKey] ?: error("docketKey: $docketKey")
        val docketName: String = "${canonicalClassName}Docket"
        val docket: Script = module.createComponent<Script>(docketName)
        docket.execute("create", args = listOf(name), isSuper = false)
        return docket
    }

    override suspend fun build(rmlResource: RmlResource, rmlTag: RmlTag, scope: Scope, root: Script): Any {
        if (scope.isSuper()) return root
        return root.execute("build", args = emptyList(), isSuper = false) as Any
    }

    override suspend fun wrap(rmlResource: RmlResource, rmlTag: RmlTag, origin: Any): T {
        @Suppress("UNCHECKED_CAST")
        return origin as T
    }

    override suspend fun attributeOn(tag: RmlTag, scope: Scope, root: Any, key: String, value: String) {
        check(root is Script)
        root.execute(action = key, args = listOf(value), isSuper = scope.isSuper())
    }

    override suspend fun attributeOff(tag: RmlTag, scope: Scope, root: Any, key: String, value: String) {
        check(root is Script)
        root.execute(action = key, args = emptyList(), isSuper = scope.isSuper())
    }

    override suspend fun propertyOn(tag: RmlTag, scope: Scope, root: Any, key: String, property: RmlTag, origin: Any) {
        check(root is Script)
        root.execute(action = key, args = listOf(origin), isSuper = scope.isSuper())
    }

    override suspend fun propertyOff(tag: RmlTag, scope: Scope, root: Any, key: String, property: RmlTag, origin: Any) {
        throw UnsupportedOperationException("propertyOff")
    }

    override suspend fun append(tag: RmlTag, scope: Scope, root: Any, child: RmlTag, origin: Any) {
        check(root is Script)
        root.execute(action = "append", args = listOf(origin), isSuper = scope.isSuper())
    }

    override suspend fun insert(tag: RmlTag, scope: Scope, root: Any, index: Int, child: RmlTag, origin: Any) {
        check(root is Script)
        root.execute(action = "insert", args = listOf(index, origin), isSuper = scope.isSuper())
    }

    override suspend fun remove(tag: RmlTag, scope: Scope, root: Any, child: RmlTag, origin: Any) {
        check(root is Script)
        root.execute(action = "remove", args = listOf(origin), isSuper = scope.isSuper())
    }

    override suspend fun replace(tag: RmlTag, scope: Scope, root: Any, child: RmlTag, index: Int, origin: Any) {
        check(root is Script)
        root.execute(action = "replace", args = listOf(index, origin), isSuper = scope.isSuper())
    }

}
