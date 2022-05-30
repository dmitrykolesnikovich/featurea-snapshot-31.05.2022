package featurea.studio.project.components

import featurea.studio.project.Project
import kotlin.reflect.KProperty

class ProjectManifest(val key: String) {
    
    operator fun getValue(project: Project, property: KProperty<*>): String? {
        return project.rmlResource.rmlTag.attributes[key]
    }
    
    operator fun setValue(project: Project, property: KProperty<*>, value: String?) {
        val rmlTag = project.rmlResource.rmlTag
        if (value == null || value.isBlank()) {
            rmlTag.attributes.remove(key)
        } else {
            rmlTag.attributes[key] = value
        }
    }
    
}
