package featurea.studio

import featurea.desktop.MainNodePlugin
import featurea.desktop.jfx.FSMenuBar
import featurea.featureaDir
import featurea.runtime.*
import featurea.studio.editor.Editor
import featurea.studio.editor.EditorModule
import featurea.studio.editor.components.*
import featurea.studio.editor.features.*
import featurea.studio.home.StudioContainer
import featurea.studio.home.StudioModule
import featurea.studio.home.StudioPanel
import featurea.studio.home.components.DefaultsService
import featurea.studio.home.components.FileChooserDialog
import featurea.studio.home.features.openProject
import featurea.studio.project.Project
import featurea.studio.project.ProjectContainer
import featurea.studio.project.ProjectModule
import featurea.studio.project.components.Clipboard
import featurea.studio.project.components.Palette
import featurea.studio.project.components.ProjectPanel
import featurea.studio.project.components.ProjectTabPanel
import featurea.window.WindowPlugin

/*dependencies*/

val artifact = Artifact("featurea.studio") {
    includeContentRootWithConfig { "$featureaDir/tools/editor/res" }
    include(featurea.config.artifact)
    include(featurea.graphics.artifact)
    include(featurea.rml.writer.artifact)
    include(featurea.window.artifact)

    /*editor*/

    "editor.Docket" to ::EditorDocket
    "editor.Editor" to ::Editor
    "editor.EditorModule" to ::EditorModule
    "editor.HeadlessEditorDelegate" to ::HeadlessEditorDelegate
    "editor.RmlTableView" to ::RmlTableView
    "editor.RmlTreeView" to ::RmlTreeView
    "editor.SelectionService" to ::SelectionService
    "editor.Selection" to ::Selection
    "editor.EditorTab" to ::EditorTab
    "editor.DocumentListView" to ::DocumentListView
    "editor.ColorChooser" to ::ColorChooser

    MainNodePlugin {
        "editor.ClearValueEditorFeature" to ::ClearValueEditorFeature
    }

    WindowPlugin {
        "editor.AutoloadFontEditorFeature" to ::AutoloadFontEditorFeature
        "editor.AutoloadTextureEditorFeature" to ::AutoloadTextureEditorFeature
        "editor.GridEditorFeature" to ::GridEditorFeature
        "editor.ScreenEditorFeature" to ::ScreenEditorFeature
        "editor.moveSelection" to { moveSelection() }
        "editor.SelectionRegionOutlineEditorFeature" to ::SelectionRegionOutlineEditorFeature
        "editor.SelectionResizeFeature" to ::SelectionResizeFeature
        "editor.SelectionResizeTrackFeature" to ::SelectionResizeTrackFeature
        "editor.SelectRegionEditorFeature" to ::SelectRegionEditorFeature
        "editor.MouseService" to ::MouseService
        "editor.ZoomEditorFeature" to ::ZoomEditorFeature
        // features
        "editor.AnchorEditorFeature" to ::AnchorEditorFeature
    }

    /*home*/

    "Docket" to ::Docket
    "StudioPanel" to ::StudioPanel
    "StudioContainer" to ::StudioContainer
    "StudioModule" to ::StudioModule
    "DefaultsService" to ::DefaultsService
    "FileChooserDialog" to ::FileChooserDialog
    "openProject" to openProject

    /*project*/

    "project.Project" to ::Project
    "project.ProjectContainer" to ::ProjectContainer
    "project.ProjectModule" to ::ProjectModule
    "project.ProjectPanel" to ::ProjectPanel
    "project.Clipboard" to ::Clipboard
    "project.Palette" to ::Palette
    "project.ProjectTabPanel" to ::ProjectTabPanel
    "project.ProjectMenuBarProxy" to ProjectMenuBarProxy::class
}

fun DependencyBuilder.ProjectPlugin(plugin: Plugin<Project>) = install(plugin)
fun DependencyBuilder.StudioPlugin(plugin: Plugin<StudioPanel>) = install(plugin) // quickfix todo improve

class ProjectMenuBarProxy(override val delegate: FSMenuBar) : Proxy<FSMenuBar> {
    companion object : Delegate<FSMenuBar>(proxyType = ProjectMenuBarProxy::class)
}
