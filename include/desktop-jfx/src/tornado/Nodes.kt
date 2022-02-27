@file:Suppress("UNCHECKED_CAST")

package featurea.desktop.jfx

import javafx.scene.Node
import javafx.scene.control.*

fun TableView<out Any>.resizeColumnsToFitContent(columns: List<TableColumn<*, *>>, limit: Int, complete: () -> Unit) {
    val doResize = {
        try {
            val method = skin.javaClass.getDeclaredMethod("resizeColumnToFitContent", TableColumn::class.java, Int::class.java)
            method.isAccessible = true
            columns.forEach {
                if (it.isVisible)
                    try {
                        method(skin, it, limit)
                    } catch (skip: Exception) {
                        // no op
                    }
            }
            complete()
        } catch (skip: Throwable) {
            // no op
        }
    }
    if (skin == null) {
        skinProperty().onChangeOnce { doResize() }
    } else {
        doResize()
    }
}

fun <T> TreeTableView<T>.resizeColumnsToFitContent(columns: List<TreeTableColumn<*, *>>, limit: Int, complete: () -> Unit) {
    val doResize = {
        try {
            val method = skin.javaClass.getDeclaredMethod("resizeColumnToFitContent", TreeTableColumn::class.java, Int::class.java)
            method.isAccessible = true
            columns.forEach {
                if (it.isVisible)
                    try {
                        method.invoke(skin, it, limit)
                    } catch (skip: Exception) {
                        // no op
                    }
            }
            complete.invoke()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    if (skin == null) {
        skinProperty().onChangeOnce { doResize() }
    } else {
        doResize()
    }
}

fun <S> TableView<S>.onSelectionChange(func: (S?) -> Unit) =
    selectionModel.selectedItemProperty().addListener { _, _, newValue -> func(newValue) }

val <S, T> TableCell<S, T>.rowItem: S get() = tableView.items[index]

fun Node.onDoubleClick(action: () -> Unit) {
    setOnMouseClicked {
        if (it.clickCount == 2) {
            action()
        }
    }
}
