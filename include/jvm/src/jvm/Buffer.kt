package featurea.jvm

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

object BufferFactory {

    fun createFloatBuffer(size: Int): FloatBuffer {
        return ByteBuffer.allocateDirect(size shl 2).order(ByteOrder.nativeOrder()).asFloatBuffer()
    }

    fun createIntBuffer(size: Int): IntBuffer {
        return createByteBuffer(size shl 2).asIntBuffer()
    }

    fun createByteBuffer(size: Int): ByteBuffer {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
    }

}

fun IntBuffer.firstInt(action: (buffer: IntBuffer) -> Unit): Int {
    clear()
    action(this)
    return get(0)
}

fun IntBuffer.rewindFirst(value: Int): IntBuffer = apply {
    position(0)
    put(value)
    rewind()
}

fun FloatBuffer.rewindData(data: FloatArray) = apply {
    clear()
    position(0)
    put(data, 0, data.size)
    rewind()
}

fun IntBuffer.rewindData(data: IntArray) = apply {
    clear()
    position(0)
    put(data, 0, data.size)
    rewind()
}

fun IntBuffer.rewindValue(value: Int) = apply {
    clear()
    position(0)
    put(value)
    rewind()
}
