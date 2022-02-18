/*
 * Copyright (c) 2010-2016 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.dynamics

/**
 * Represents an iterator for [Body]s in a [World].
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 */
internal class BodyIterator(world: World) : MutableIterator<Body?> {
    /** The world to iterate over  */
    private val world: World

    /** The current index  */
    private var index: Int

    /* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
    override fun hasNext(): Boolean {
        return index + 1 < world.bodyCount
    }

    /* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
    override fun next(): Body? {
        if (index >= world.bodyCount) {
            throw IndexOutOfBoundsException()
        }
        return try {
            index++
            world.getBody(index)
        } catch (ex: IndexOutOfBoundsException) {
            throw ConcurrentModificationException()
        }
    }

    /* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
    override fun remove() {
        check(index >= 0)
        if (index >= world.bodyCount) {
            throw IndexOutOfBoundsException()
        }
        try {
            world.removeBody(index)
            index--
        } catch (ex: IndexOutOfBoundsException) {
            throw ConcurrentModificationException()
        }
    }

    /**
     * Minimal constructor.
     * @param world the world to iterate over
     */
    init {
        this.world = world
        index = -1
    }
}