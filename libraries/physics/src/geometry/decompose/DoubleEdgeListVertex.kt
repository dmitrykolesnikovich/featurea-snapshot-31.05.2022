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
package org.dyn4j.geometry.decompose

import org.dyn4j.geometry.Vector2

/**
 * Represents a vertex in the [DoubleEdgeList].
 * @author William Bittle
 * @version 3.2.0
 * @since 2.2.0
 */
class DoubleEdgeListVertex {
    /** The comparable data for this node  */
    lateinit var point: Vector2

    /** The the leaving edge  */
    var leaving: DoubleEdgeListHalfEdge? = null

    /**
     * Minimal constructor.
     * @param point the vertex point
     */
    constructor(point: Vector2) {
        this.point = point
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return point.toString()
    }

    /**
     * Returns the edge from this node to the given node.
     * @param node the node to find an edge to
     * @return [DoubleEdgeListHalfEdge]
     */
    fun getEdgeTo(node: DoubleEdgeListVertex): DoubleEdgeListHalfEdge? {
        if (leaving != null) {
            if (leaving!!.twin!!.origin == node) {
                return leaving
            } else {
                var edge = leaving!!.twin!!.next
                while (edge != leaving) {
                    edge = if (edge!!.twin!!.origin == node) {
                        return edge
                    } else {
                        edge.twin!!.next
                    }
                }
            }
        }
        return null
    }
}