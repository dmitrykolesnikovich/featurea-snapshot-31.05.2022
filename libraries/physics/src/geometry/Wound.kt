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
package org.dyn4j.geometry

import org.dyn4j.DataContainer
import kotlin.jvm.JvmField

/**
 * Represents a shape that is defined by vertices with line segment connections
 * with counter-clockwise winding.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
interface Wound : Shape, Transformable, DataContainer {
    /**
     * Returns an iterator for the vertices.
     *
     *
     * The iterator does not support the remove method and will return a new
     * [Vector2] in the next method.
     *
     *
     * This method is safer than the [.getVertices] since its not
     * possible to modify the array or its elements.
     * @return Iterator&lt;[Vector2]&gt;
     * @since 3.2.0
     */
    val vertexIterator: Iterator<Vector2?>?

    /**
     * Returns an iterator for the normals.
     *
     *
     * The iterator does not support the remove method and will return a new
     * [Vector2] in the next method rather than the underlying value.
     *
     *
     * This method is safer than the [.getNormals] since its not
     * possible to modify the array or its elements.
     * @return Iterator&lt;[Vector2]&gt;
     * @since 3.2.0
     */
    val normalIterator: Iterator<Vector2?>?

    /**
     * Returns the array of vertices in local coordinates.
     *
     *
     * For performance, this array may be the internal storage array of the shape.
     * Both the array elements and their properties should not be modified via this
     * method.
     *
     *
     * It's possible that this method will be deprecated and/or removed in later versions.
     * @return [Vector2][]
     * @see .getVertexIterator
     */
    val woundVertices: Array<Vector2> // quickfix todo uncomment

    /**
     * Returns the array of edge normals in local coordinates.
     *
     *
     * For performance, this array may be the internal storage array of the shape.
     * Both the array elements and their properties should not be modified via this
     * method.
     *
     *
     * It's possible that this method will be deprecated and/or removed in later versions.
     * @return [Vector2][]
     * @see .getNormalIterator
     */
    val woundNormals: Array<Vector2> // quickfix todo uncomment
}