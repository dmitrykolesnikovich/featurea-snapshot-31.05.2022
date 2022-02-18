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

/**
 * This class is a specialization of the [Segment] class that provides smooth sliding across
 * a chain of line segments.  This is achieved by storing the connectivity information between the
 * links.  With this, a correction process is performed to avoid the 'internal edge' problem.
 *
 *
 * A [Link] is an infinitely thin line segment and will behave like the [Segment] class in
 * collision response.
 *
 *
 * Like the [Segment] class, this class can be locally rotated or translated.  However, doing
 * so will also translate/rotated the next or previous [Link]s.
 *
 *
 * For ease of use, it's recommended to use the Geometry class to create chains of [Link]s.
 * @author William Bittle
 * @version 3.2.2
 * @since 3.2.2
 */
class Link : Segment, Convex, Wound, Shape, Transformable, DataContainer {

    /** The previous link in the chain  */
    var previous: Link? = null
        set(value) {
            if (field != null) {
                field!!.next = null
            }
            field = value
            if (value != null) {
                value.next = this
            }
        }

    /** The next link in the chain  */
    var next: Link? = null
        set(value) {
            if (field != null) {
                field!!.previous = null
            }
            field = value
            if (value != null) {
                value.previous = this
            }
        }

    /**
     * Creates a new link.
     * @param point1 the first vertex
     * @param point2 the last vertex
     */
    constructor(point1: Vector2, point2: Vector2) : super(point1, point2)

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Segment#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Link[").append(super.toString())
            .append("|Length=").append(this.length)
            .append("]")
        return sb.toString()
    }

    // NOTE: local rotation and translation will modify the next and previous links

    // NOTE: local rotation and translation will modify the next and previous links
    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Segment#rotate(double, double, double)
	 */
    override fun rotate(theta: Double, x: Double, y: Double) {
        super.rotate(theta, x, y)
        // we need to update the next/prev links to reflect
        // the change in this link's vertices
        val next = next
        if (next != null) {
            next!!.vertices!![0]!!.set(vertices!![1]!!)
            // update normals
            updateNormals(next)
            updateLength(next)
        }
        val previous = previous
        if (previous != null) {
            previous!!.vertices!![1]!!.set(vertices!![0]!!)
            // update normals
            updateNormals(previous)
            updateLength(previous)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Segment#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        super.translate(x, y)
        // we need to update the next/prev links to reflect
        // the change in this link's vertices
        val next = next
        if (next != null) {
            next!!.vertices!![0]!!.set(vertices!![1]!!)
            updateLength(next)
        }
        val previous = previous
        if (previous != null) {
            previous!!.vertices!![1]!!.set(vertices!![0]!!)
            updateLength(previous)
        }
    }

    /**
     * Updates the normals of the given [Segment].
     *
     *
     * When rotating a link in a link chain, the connected links
     * will need their normals recomputed to match the change.
     * @param segment the segment to update
     */
    private fun updateNormals(segment: Segment) {
        val v: Vector2 = segment.vertices[0].to(segment.vertices[1])
        segment.normals[0] = v.copy().apply { normalize() }
        segment.normals[1] = v.right().apply { normalize() }
    }

    /**
     * Updates the length and radius of the given [Segment].
     *
     *
     * When rotating or translating a link in a link chain, the connected links
     * will need their lengths and maximum radius recomputed to match the change.
     * @param segment the segment to update
     */
    private fun updateLength(segment: Segment) {
        val length: Double = segment.vertices.get(0).distance(segment.vertices.get(1))
        segment.length = length
        segment.radius = length * 0.5
    }

}