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
package org.dyn4j.collision.narrowphase

import featurea.PriorityQueue
import org.dyn4j.geometry.Vector2

/**
 * Represents a simplex that is progressively expanded by splitting
 * the closest edge to the origin by adding new points.
 *
 *
 * This class is used with the [Epa] class to maintain the state
 * of the algorithm.
 *
 *
 * Given the way the simplex is expanded, the winding can be computed initially
 * and will never change.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 */
@OptIn(kotlin.ExperimentalStdlibApi::class)
internal class ExpandingSimplex {

    /** The winding direction of the simplex  */
    private val winding: Int

    /** The priority queue of simplex edges  */
    private val queue: PriorityQueue<ExpandingSimplexEdge?>

    /**
     * Minimal constructor.
     * @param simplex the starting simplex from GJK
     */
    constructor(simplex: MutableList<Vector2>) {
        // compute the winding
        winding = getWinding(simplex)
        // build the initial edge queue
        queue = PriorityQueue()
        val size = simplex.size
        for (i in 0 until size) {
            // compute j
            val j = if (i + 1 == size) 0 else i + 1
            // get the points that make up the current edge
            val a: Vector2 = simplex[i]
            val b: Vector2 = simplex[j]
            // create the edge
            queue.offer(ExpandingSimplexEdge(a, b, winding))
        }
    }

    /**
     * Returns the winding of the given simplex.
     *
     *
     * Returns -1 if the winding is Clockwise.<br></br>
     * Returns 1 if the winding is Counter-Clockwise.
     *
     *
     * This method will continue checking all edges until
     * an edge is found whose cross product is less than
     * or greater than zero.
     *
     *
     * This is used to get the correct edge normal of
     * the simplex.
     * @param simplex the simplex
     * @return int the winding
     */
    protected fun getWinding(simplex: MutableList<Vector2>): Int {
        val size = simplex.size
        for (i in 0 until size) {
            val j = if (i + 1 == size) 0 else i + 1
            val a: Vector2 = simplex[i]
            val b: Vector2 = simplex[j]
            if (a.cross(b) > 0) {
                return 1
            } else if (a.cross(b) < 0) {
                return -1
            }
        }
        return 0
    }// O(1)

    /**
     * Returns the edge on the simplex that is closest to the origin.
     * @return [ExpandingSimplexEdge] the closest edge to the origin
     */
    val closestEdge: ExpandingSimplexEdge?
        get() = queue.peek() // O(1)

    /**
     * Expands the simplex by the given point.
     *
     *
     * Removes the closest edge to the origin and adds
     * two new edges using the given point and the removed
     * edge's vertices.
     * @param point the new point
     */
    fun expand(point: Vector2) {
        // remove the edge we are splitting
        val edge: ExpandingSimplexEdge = queue.poll()!! // O(log n)
        // create two new edges
        val edge1 = ExpandingSimplexEdge(edge.point1, point, winding)
        val edge2 = ExpandingSimplexEdge(point, edge.point2, winding)
        queue.add(edge1) // O(log n)
        queue.add(edge2) // O(log n)
    }

}