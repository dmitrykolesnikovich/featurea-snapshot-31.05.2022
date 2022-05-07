package featurea.modbus.support

import featurea.modbus.ModbusConfig
import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.config.Directory
import featurea.utils.Stack
import featurea.utils.isNotEmpty

fun ModbusConfig.traverseNodes(observe: (connection: Connection, channel: Channel) -> Unit) {
    val stack = Stack(directory.node)
    var currentConnection: Connection? = null
    var currentChannel: Channel? = null

    fun update(node: Node) {
        if (node.value is Connection) {
            currentConnection = node.value.also { it.config = this }
            currentChannel = null
        }
        if (node.value is Channel) {
            currentChannel = node.value.also { it.config = this }
        }
    }

    fun popNode(): Node? {
        if (stack.isEmpty()) return null
        val node = stack.pop()
        update(node)
        return node
    }

    fun pushNode(node: Node): Node {
        stack.push(node)
        update(node)
        return node
    }

    fun currentNode(): Node? {
        if (stack.isEmpty()) return null
        val node = stack.last()
        update(node)
        return node
    }

    fun move(): Node? {
        do {
            val currentNode = currentNode() ?: return null
            if (currentNode.children.isNotEmpty()) {
                return pushNode(currentNode.children.next())
            } else {
                popNode()
            }
        } while (stack.isNotEmpty())
        return null
    }


    while (true) {
        move() ?: break
        if (currentConnection != null && currentChannel != null) {
            observe(currentConnection!!, currentChannel!!)
        }
    }

}

/*internals*/

private val Directory.node: Node
    get() = Node(this, children.map { it.node }.iterator())

private class Node(val value: Any, val children: Iterator<Node>) {
    override fun toString(): String = "Node(value=$value)"
}
