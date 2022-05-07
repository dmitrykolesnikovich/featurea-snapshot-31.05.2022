package featurea.utils

class ByteQueue constructor(private var queue: ByteArray = ByteArray(1024)) {

    private var head = -1
    private var tail = 0
    var size = 0
        private set
    private var markHead = 0
    private var markTail = 0
    private var markSize = 0

    fun pushByte(b: Byte) {
        try {
            if (room() == 0) {
                expand()
            }
            queue[tail] = b
            if (head == -1) {
                head = 0
            }
            tail = (tail + 1) % queue.size
            size++
        } catch (e: Throwable) {
            println(e)
        }
    }

    fun pushAll(array: ByteArray, pos: Int = 0, length: Int = array.size) {
        try {
            if (length == 0) {
                return
            }
            while (room() < length) {
                expand()
            }
            val tailLength = queue.size - tail
            if (tailLength > length) {
                array.copyInto(queue, tail, pos, length + pos)
            } else {
                array.copyInto(queue, tail, pos, tailLength + pos)
            }
            if (length > tailLength) {
                array.copyInto(queue, 0, tailLength + pos, length + pos)
            }
            if (head == -1) {
                head = 0
            }
            tail = (tail + length) % queue.size
            size += length
        } catch (e: Throwable) {
            println(e)
        }
    }

    fun pushAll(source: ByteQueue) {
        var source = source
        if (source.size == 0) {
            return
        }
        if (source === this) {
            source = ByteQueue(queue.copyOf())
        }
        var firstCopyLen = source.queue.size - source.head
        if (source.size < firstCopyLen) {
            firstCopyLen = source.size
        }
        pushAll(source.queue, source.head, firstCopyLen)
        if (firstCopyLen < source.size) {
            pushAll(source.queue, 0, source.tail)
        }
    }

    fun mark() {
        markHead = head
        markTail = tail
        markSize = size
    }

    fun reset() {
        head = markHead
        tail = markTail
        size = markSize
    }

    fun popByte(): Byte {
        return try {
            val retval = queue[head]
            if (size == 1) {
                head = -1
                tail = 0
            } else {
                head = (head + 1) % queue.size
            }
            size--
            retval
        } catch (e: Throwable) {
            println(e)
            -1
        }
    }

    fun pop(buf: ByteArray, pos: Int = 0, length: Int = buf.size) {
        var length = length
        try {
            length = peek(buf, pos, length)
            size -= length
            if (size == 0) {
                head = -1
                tail = 0
            } else {
                head = (head + length) % queue.size
            }
        } catch (e: Throwable) {
            println(e)
        }
    }

    fun popAll(): ByteArray {
        val data = ByteArray(size)
        pop(data, 0, data.size)
        return data
    }

    fun indexOf(b: Byte, start: Int): Int {
        return try {
            if (start >= size) {
                return -1
            }
            var index = (head + start) % queue.size
            for (i in start until size) {
                if (queue[index] == b) {
                    return i
                }
                index = (index + 1) % queue.size
            }
            -1
        } catch (e: Exception) {
            println(e)
            -1
        }
    }

    fun clear() {
        size = 0
        head = -1
        tail = 0
    }

    /*internals*/
    private fun room(): Int {
        return queue.size - size
    }

    private fun expand() {
        try {
            val newb = ByteArray(queue.size * 2)
            if (head == -1) {
                queue = newb
                return
            }
            if (tail > head) {
                queue.copyInto(newb, head, head, tail)
                queue = newb
                return
            }
            queue.copyInto(newb, head + queue.size, head, queue.size)
            queue.copyInto(newb, 0, 0, tail)
            head += queue.size
            queue = newb
        } catch (e: Throwable) {
            println(e)
        }
    }

    private fun peek(buf: ByteArray, pos: Int, length: Int): Int {
        var length = length
        return try {
            if (length == 0) {
                return 0
            }
            if (size == 0) {
                throw IndexOutOfBoundsException("-1")
            }
            if (length > size) {
                length = size
            }
            var firstCopyLen = queue.size - head
            if (length < firstCopyLen) {
                firstCopyLen = length
            }
            // arraycopy(queue, startIndex = head, destination = buf, destinationOffset = pos, length = firstCopyLen)
            queue.copyInto(
                destination = buf,
                destinationOffset = pos,
                startIndex = head,
                endIndex = firstCopyLen + head
            )
            if (firstCopyLen < length) {
                // arraycopy(queue, startIndex= 0, destination = buf, destinationOffset = pos + firstCopyLen, length = length - firstCopyLen)
                queue.copyInto(
                    destination = buf,
                    destinationOffset = pos + firstCopyLen,
                    startIndex = 0,
                    endIndex = length - firstCopyLen
                )
            }
            length
        } catch (e: IndexOutOfBoundsException) {
            println(e)
            0
        }
    }

    val isEmpty: Boolean
        get() = size == 0

    fun popShort(): Short {
        return convertTwoBytesToShort(popByte(), popByte())
    }

    fun pushShort(value: Short) {
        val int = value.toInt()
        pushByte((0xff and (int shr 8)).toByte())
        pushByte((0xff and int).toByte())
    }

    fun toByteArray(): ByteArray = queue.sliceArray(0 until size)

    /*internals*/

    private fun convertTwoBytesToShort(b1: Byte, b2: Byte): Short {
        return ((b1.toInt() shl 8) or (b2.toInt() and 0xff)).toShort()
    }

}
