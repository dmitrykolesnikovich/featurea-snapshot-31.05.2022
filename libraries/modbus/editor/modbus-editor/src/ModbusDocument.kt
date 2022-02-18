package featurea.modbus.editor

import featurea.content.ResourceTag
import featurea.desktop.jfx.isEditing
import featurea.desktop.jfx.onChange
import featurea.desktop.jfx.toImageView
import featurea.desktop.jfx.updateTreeItems
import featurea.jvm.normalizedPath
import featurea.modbus.config.ChannelSpecifier
import featurea.modbus.config.channelSpecifier
import featurea.rml.deepCopy
import featurea.rml.readRmlResource
import featurea.runtime.*
import featurea.studio.editor.Editor
import featurea.studio.editor.components.*
import featurea.studio.home.components.DefaultsService
import featurea.text.TextContent
import javafx.scene.image.ImageView
import java.io.File
import java.lang.Integer.min

class ModbusDocument(override val module: Module) : Component {

    val defaultsService: DefaultsService = import()
    val editor: Editor = import()
    val editorTab: EditorTab = import()
    val panel: ModbusDocumentPanel = import()
    val rmlTableView: RmlTableView = import()
    val rmlTreeView: RmlTreeView = import()
    val textContent: TextContent = import()

    init {
        editorTab.isDirtyProperty.watch { updateTreeItemIcons() }
        rmlTreeView.rootProperty().onChange { updateTreeItemIcons() }
    }

    suspend fun initFile(rmlFile: File) {
        // rmlContent.remove(rmlFile.normalizedPath) // just for try todo uncomment
        textContent.removeCachedText(rmlFile.normalizedPath)
        editor.project.rmlFile = rmlFile
        editor.project.rmlResource = readRmlResource(rmlFile.normalizedPath)
        editor.tab.rmlResource = readRmlResource("${rmlFile.normalizedPath}:/config")
        // rmlTreeView.select(rmlTreeView.root.value) // quickfix todo improve
    }

    fun appendDirectory() {
        val selectedRmlTag = editor.rmlTreeView.selectedRmlTagUI ?: return
        val parent = (if (selectedRmlTag.name == "Channel") selectedRmlTag.parent else selectedRmlTag) ?: return
        val directory = defaultsService.createDefaultRmlTag("Directory", parent)
        parent.children.add(directory)
        editorTab.isDirty = true
        rmlTreeView.updateRmlTag()
        rmlTreeView.select(directory)
    }

    fun appendConnection() {
        val selectedRmlTag = editor.rmlTreeView.selectedRmlTagUI ?: return
        val connection = defaultsService.createDefaultRmlTag("Connection", selectedRmlTag)
        selectedRmlTag.children.add(connection)
        editorTab.isDirty = true
        rmlTreeView.updateRmlTag()
        rmlTreeView.select(connection)
    }

    fun appendPhysicalChannel() {
        appendChannel { parent ->
            defaultsService.createDefaultRmlTag("Channel", parent).apply {
                attributes["isLocal"] = "false"
                attributes["address"] = "0"
                attributes["region"] = "Holdings"
                attributes["type"] = "UInt16"
                attributes["updateInterval"] = "1000"
            }
        }
    }

    fun appendVirtualChannel() {
        appendChannel { parent ->
            defaultsService.createDefaultRmlTag("Channel", parent).apply {
                attributes["isLocal"] = "false"
                attributes["type"] = "UInt16"
            }
        }
    }

    fun appendLocalChannel() {
        appendChannel { parent ->
            defaultsService.createDefaultRmlTag("Channel", parent).apply {
                attributes["isLocal"] = "true"
                attributes["type"] = "UInt16"
            }
        }
    }

    private fun appendChannel(createChannel: (parent: ResourceTag) -> ResourceTag) {
        val selectedRmlTag = editor.rmlTreeView.selectedRmlTagUI ?: return
        val parent = (if (selectedRmlTag.name == "Channel") selectedRmlTag.parent else selectedRmlTag) ?: return
        val channelRmlTag = createChannel(parent)
        parent.children.add(channelRmlTag)
        editorTab.isDirty = true
        rmlTreeView.updateRmlTag()
        rmlTreeView.select(channelRmlTag)
    }

    fun copyChannel() {
        val selectedRmlTag = editor.rmlTreeView.selectedRmlTagUI ?: return
        val parent = selectedRmlTag.parent ?: return
        val channelRmlTag = selectedRmlTag.deepCopy(parent)
        parent.children.add(channelRmlTag)
        editorTab.isDirty = true
        rmlTreeView.updateRmlTag()
        rmlTreeView.select(channelRmlTag)
    }

    fun removeSelected() {
        if (rmlTableView.isEditing()) return

        fun removeSelectedRmlTag(selectedRmlTag: ResourceTag) {
            val parent = selectedRmlTag.parent ?: return
            val index = parent.children.indexOf(selectedRmlTag)
            parent.children.remove(selectedRmlTag)
            editorTab.isDirty = true
            rmlTreeView.updateRmlTag()
            if (parent.children.isEmpty()) {
                rmlTreeView.select(parent)
            } else {
                val nextChild = parent.children[min(index, parent.children.size - 1)]
                rmlTreeView.select(nextChild)
            }
        }

        for (selectedRmlTag in rmlTreeView.selectedRmlTagsUI) {
            removeSelectedRmlTag(selectedRmlTag)
        }
    }

    fun save() {
        editorTab.save()
    }

    private fun updateTreeItemIcons() {
        rmlTreeView.updateTreeItems { treeItem ->
            val rmlTag = treeItem.value
            if (rmlTag.name == "Channel") {
                val hasTimeout = rmlTag.attributes["scriptType"] == "Timeout"
                val hasWriteEvent = rmlTag.attributes["scriptType"] == "WriteEvent"
                val icon: ImageView = when (rmlTag.channelSpecifier) {
                    ChannelSpecifier.Physical -> Resources.modbusPng.toImageView(32, 16)
                    ChannelSpecifier.Virtual -> {
                        when {
                            hasTimeout -> Resources.virtualTimeoutPng.toImageView(32, 16)
                            hasWriteEvent -> Resources.virtualWriteEventPng.toImageView(32, 16)
                            else -> Resources.virtualPng.toImageView(32, 16)
                        }
                    }
                    ChannelSpecifier.Local -> {
                        when {
                            hasTimeout -> Resources.localTimeoutPng.toImageView(32, 16)
                            hasWriteEvent -> Resources.localWriteEventPng.toImageView(32, 16)
                            else -> Resources.localPng.toImageView(32, 16)
                        }
                    }
                }
                treeItem.graphic = icon
            }
        }
    }

}
