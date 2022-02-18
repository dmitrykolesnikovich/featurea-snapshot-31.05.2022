package featurea.desktop.jfx

import javafx.beans.property.ObjectProperty
import javafx.scene.control.*
import javafx.util.Callback

fun TreeTableView<*>.toTornadoFXTable() = TornadoFXTreeTable(this)

interface TornadoFXTable<COLUMN, out TABLE : Any> {
    val table: TABLE
    val contentWidth: Double
    val properties: Properties
    val contentColumns: List<TornadoFXColumn<COLUMN>>
    var skin: Skin<*>?
    val skinProperty: ObjectProperty<Skin<*>>
}

class TornadoFXTreeTable(override val table: TreeTableView<*>) : TornadoFXTable<TreeTableColumn<*, *>, TreeTableView<*>> {

    override val skinProperty = table.skinProperty()

    override var skin
        get() = table.skin
        set(value) {
            table.skin = value
        }

    override val contentColumns
        get() = table.columns.flatMap {
            if (it.columns.isEmpty()) listOf(it) else it.columns
        }.mapEach { toTornadoColumn() }

    override val properties = table.properties

    private val contentWidthField by lazy {
        TreeTableView::class.java.getDeclaredField("contentWidth").also { it.isAccessible = true }
    }

    override val contentWidth get() = contentWidthField.get(table) as Double

}

class TornadoFXNormalTable(override val table: TableView<*>) : TornadoFXTable<TableColumn<*, *>, TableView<*>> {

    override val skinProperty: ObjectProperty<Skin<*>> get() = table.skinProperty()

    override val contentColumns
        get() = table.columns.flatMap {
            if (it.columns.isEmpty()) listOf(it) else it.columns
        }.mapEach { toTornadoColumn() }

    override var skin
        get() = table.skin
        set(value) {
            table.skin = value
        }

    private val contentWidthField by lazy {
        TableView::class.java.getDeclaredField("contentWidth").also {
            it.isAccessible = true
        }
    }

    override val contentWidth get() = contentWidthField.get(table) as Double

    override val properties = table.properties

}
