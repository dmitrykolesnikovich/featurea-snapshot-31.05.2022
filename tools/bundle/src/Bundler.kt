package featurea.bundler

import featurea.*
import featurea.content.Content
import featurea.content.Resource
import featurea.content.ResourceTag
import featurea.content.mainProject
import featurea.jvm.createNewFileAndDirs
import featurea.jvm.normalizedPath
import featurea.jvm.readBytes
import featurea.jvm.relativeTo
import featurea.rml.writer.RmlDeserializer
import featurea.runtime.Component
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.replaceSuffix

class Bundler(override val module: Module) : Component {

    private val content: Content = import()
    private val rmlDeserializer: RmlDeserializer = import()
    private val system: System = import()

    suspend fun createBundle(options: BundleOptions, config: (Bundle) -> Unit = {}): Bundle {
        val (projectFile, bundleFile, contentRoots) = options
        val projectPath: String = projectFile.normalizedPath
        println("[Bundler] createBundle: $projectPath, $bundleFile, ${contentRoots.joinToString()}")
        val bundle = Bundle()
        system.contentRoots.addAll(contentRoots)
        content.providedResources.add("package.properties") // just for now todo delete this

        println("progress: 0")
        createProjectTag(bundle, projectPath)
        println("progress: 0.25")
        content.flush(bundle)
        println("progress: 0.5")
        createBundleEntries(bundle)
        config(bundle)
        createBundleManifestEntry(bundle)
        println("progress: 0.75")
        if (bundleFile.exists()) {
            bundleFile.delete()
        } else {
            bundleFile.parentFile.mkdirs()
        }
        val success: Boolean = BundleSerializer.serializeBundle(bundle, bundleFile)
        println("progress: 1.0")
        if (success) {
            println("[Bundler] createBundle: ${bundleFile.absolutePath}")
        } else {
            println("[Bundler] createBundle: failure")
        }
        return bundle
    }

    /*internals*/

    private suspend fun createProjectTag(bundle: Bundle, projectPath: String) {
        val providedResources = ArrayList(content.providedResources)
        val projectTag: ResourceTag = rmlDeserializer.deserialize(projectPath, bundle)
        initBundleManifest(bundle, projectTag)
        for (providedResource in providedResources) {
            // quickfix todo replace `_` with some concept
            content.writeIfAccept(projectTag, "_", providedResource, bundle)
        }
    }

    private fun initBundleManifest(bundle: Bundle, projectTag: ResourceTag) {
        bundle.manifest.mainProject = system.relativeTo(projectTag.filePath)
        bundle.manifest.putAll(projectTag.attributes)
        // log("[Manifest] initBundleManifest: ${bundle.manifest.joinToString { it.key }}")
    }

    private suspend fun createBundleEntries(bundle: Bundle) {
        for (providedResource in content.providedResources) {
            val resource: Resource = content.findResource(providedResource, bundle)
            for (resourceFile in resource.files) {
                createBundleEntry(bundle, resourceFile)
            }
            val resourceManifestFile: String? = resource.manifestFile
            if (resourceManifestFile != null) {
                createBundleEntry(bundle, resourceManifestFile)
            }
        }
    }

    private fun createBundleEntry(bundle: Bundle, entryPath: String) {
        if (bundle.entries.containsKey(entryPath)) return

        val bytes: ByteArray? = system.readBytes(entryPath)
        if (bytes != null) {
            @Suppress("NAME_SHADOWING")
            val entryPath: String = entryPath.replaceSuffix("Transpiled.shader", ".shader") // quickfix todo improve
            bundle.entries[entryPath] = bytes
        }
    }

    private fun createBundleManifestEntry(bundle: Bundle) {
        // log("[Manifest] createBundleManifestEntry: ${bundle.manifest.joinToString { it.key }}")
        val manifestPropertiesText: String = bundle.toManifestPropertiesText(content.providedResources)
        bundle.entries["manifest.properties"] = manifestPropertiesText.encodeToByteArray()
    }

}
