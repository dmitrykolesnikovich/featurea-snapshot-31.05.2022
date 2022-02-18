package featurea.studio.home

import featurea.BundleOptions
import featurea.runtime.Component
import java.io.File

interface StudioDelegate : Component {
    fun openProject(rmlFile: File)
    fun newProject(rmlFile: File): Unit = error("unsupported")
    fun createBundle(options: BundleOptions, progress: (Double) -> Unit, complete: (Int) -> Unit) {}
}
