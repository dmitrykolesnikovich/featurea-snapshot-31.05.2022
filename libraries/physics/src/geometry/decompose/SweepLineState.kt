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

import featurea.PriorityQueue
import org.dyn4j.BinarySearchTree
import org.dyn4j.Reference
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.Messages.getString


/**
 * Represents the current state of the SweepLine algorithm.
 *
 *
 * The SweepLine algorithm maintains a DCEL to hold the triangulation, a binary tree for edge
 * searching and the current sweepline intercept value.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 */
@OptIn(ExperimentalStdlibApi::class)
class SweepLineState {

    /** The current sweepline y-intercept value  */
    lateinit var referenceY: Reference<Double>

    /** The edge binary tree  */
    lateinit var tree: BinarySearchTree<SweepLineEdge>

    /** The DCEL  */
    lateinit var dcel: DoubleEdgeList

    /**
     * Default constructor.
     */
    fun SweepLineState() {
        referenceY = Reference(0.0)
        tree = BinarySearchTree(true)
    }

    /**
     * Returns a priority queue of the points in the given array and initializes
     * the Binary Tree and DCEL for the SweepLine algorithm.
     * @param points the array of polygon points
     * @return PriorityQueue&lt;[SweepLineVertex]&gt;
     */
    fun initialize(points: Array<out Vector2>): PriorityQueue<SweepLineVertex> {
        // initialize the DCEL
        dcel = DoubleEdgeList(points)

        // get the number points
        val size = points.size

        // create a priority queue for the vertices
        val queue: PriorityQueue<SweepLineVertex> = PriorityQueue(size)
        var rootVertex: SweepLineVertex? = null
        var prevVertex: SweepLineVertex? = null
        var rootEdge: SweepLineEdge? = null
        var prevEdge: SweepLineEdge? = null

        // build the vertices and edges
        for (i in 0 until size) {
            // get this vertex point
            val point = points[i]

            // create the vertex for this point
            val vertex = SweepLineVertex(point, i)
            // default the type to regular
            vertex.type = SweepLineVertexType.REGULAR
            vertex.prev = prevVertex

            // set the previous vertex's next pointer
            if (prevVertex != null) {
                prevVertex.next = vertex
            }

            // make sure we save the first vertex so we
            // can wire up the last and first to create
            // a cyclic list
            if (rootVertex == null) {
                rootVertex = vertex
            }

            // get the neighboring points
            val point1 = points[if (i + 1 == size) 0 else i + 1]
            val point0 = points[if (i == 0) size - 1 else i - 1]

            // get the vertex type
            vertex.type = getType(point0, point, point1)

            // set the previous vertex to this vertex
            prevVertex = vertex
            // add the vertex to the priority queue
            queue.offer(vertex)

            // create the next edge
            val e = SweepLineEdge(referenceY!!)
            // the first vertex is this vertex
            e.v0 = vertex

            // compute the slope
            val my = point.y - point1.y
            if (my == 0.0) {
                e.slope = Double.POSITIVE_INFINITY
            } else {
                val mx = point.x - point1.x
                e.slope = mx / my
            }

            // set the previous edge's end vertex and
            // next edge pointers
            if (prevEdge != null) {
                prevEdge.v1 = vertex
            }

            // make sure we save the first edge so we
            // can wire up the last and first to create
            // a cyclic list
            if (rootEdge == null) {
                rootEdge = e
            }

            // set the vertex's left and right edges
            vertex.left = e
            vertex.right = prevEdge

            // set the previous edge to this edge
            prevEdge = e
        }

        // set the last edge's end vertex pointer to
        // the first edge's start vertex
        prevEdge!!.v1 = rootEdge!!.v0

        // set the previous edge of the first vertex
        rootVertex!!.right = prevEdge
        // set the previous vertex of the first vertex
        rootVertex.prev = prevVertex
        // set the last vertex's next pointer to the
        // first vertex
        prevVertex!!.next = rootVertex

        // return the priority queue
        return queue
    }

    /**
     * Returns the vertex type given the previous and next points.
     * @param point0 the previous point
     * @param point the vertex point
     * @param point1 the next point
     * @return [SweepLineVertexType]
     */
    fun getType(
        point0: Vector2,
        point: Vector2,
        point1: Vector2
    ): SweepLineVertexType {
        // create the edge vectors
        val v1 = point0.to(point)
        val v2 = point.to(point1)

        // check for coincident points
        if (v1.isZero || v2.isZero) throw IllegalArgumentException(getString("geometry.decompose.coincident"))

        // get the angle between the two edges (we assume CCW winding)
        val cross = v1.cross(v2)
        val pBelowP0 = isBelow(point, point0)
        val pBelowP1 = isBelow(point, point1)

        // where is p relative to its neighbors?
        if (pBelowP0 && pBelowP1) {
            // then check if the 
            // if its below both of them then we need
            // to check the interior angle
            return if (cross > 0.0) {
                // if the cross product is greater than zero
                // this indicates that the angle is < pi

                // this vertex is an end vertex
                SweepLineVertexType.END
            } else {
                // this indicates that the angle is pi or greater

                // this vertex is a merge vertex
                SweepLineVertexType.MERGE
            }
        } else if (!pBelowP0 && !pBelowP1) {
            // if its above both of them then we need
            // to check the interior angle
            return if (cross > 0.0) {
                // if the cross product is greater than zero
                // this indicates that the angle is < pi

                // this vertex is a start vertex
                SweepLineVertexType.START
            } else {
                // this indicates that the angle is pi or greater

                // this vertex is a split vertex
                SweepLineVertexType.SPLIT
            }
        }
        return SweepLineVertexType.REGULAR
    }

    /**
     * Returns true if the given point p is below the given point q.
     *
     *
     * If the point p and q form a horizontal line then p is considered
     * below if its x coordinate is greater than q's x coordinate.
     * @param p the point
     * @param q another point
     * @return boolean true if p is below q; false if p is above q
     */
    fun isBelow(p: Vector2, q: Vector2): Boolean {
        val diff = p.y - q.y
        return if (diff == 0.0) {
            if (p.x > q.x) {
                true
            } else {
                false
            }
        } else {
            if (diff < 0.0) {
                true
            } else {
                false
            }
        }
    }
}