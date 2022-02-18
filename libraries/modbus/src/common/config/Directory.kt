package featurea.modbus.config

import featurea.modbus.ModbusConfig
import featurea.runtime.Component
import featurea.runtime.Constructor
import featurea.runtime.Module
import featurea.runtime.create

open class Directory(override val module: Module) : Component {

    var id: String = "undefined"
    var name: String = "undefined"
    internal lateinit var config: ModbusConfig
    internal var withDocket: Boolean = false

    val children: MutableList<Directory> = mutableListOf()
    var parent: Directory? = null
        private set

    fun append(directory: Directory) {
        children.add(directory)
        directory.parent = this
    }

    fun insert(index: Int, directory: Directory) {
        children.add(index, directory)
        directory.parent = this
    }

    fun remove(directory: Directory) {
        children.remove(directory)
        directory.parent = null
    }

    override fun toString(): String = "Connection(name='$name')"

    override fun equals(other: Any?): Boolean = other === this

}

@Constructor
fun Component.Directory(init: Directory.() -> Unit = {}): Directory = create(init)

@Constructor
fun Directory.Directory(init: Directory.() -> Unit = {}): Directory = create(init).also { append(it) }

@Constructor
fun Connection.Directory(init: Directory.() -> Unit = {}): Directory = create(init).also { append(it) }

/*convenience*/

val Directory.path: String
    get() {
        val name = when {
            name != "undefined" -> name
            id != "undefined" -> id
            else -> "undefined"
        }
        val parent = parent
        if (parent != null) {
            return "${parent.path}/$name"
        } else {
            return name
        }
    }
