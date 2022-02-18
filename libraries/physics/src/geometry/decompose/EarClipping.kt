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

import org.dyn4j.Epsilon
import org.dyn4j.geometry.*
import org.dyn4j.geometry.Geometry.getWinding
import org.dyn4j.geometry.Geometry.reverseWinding
import org.dyn4j.resources.message
import kotlin.math.sqrt

/**
 * Implementation of the Ear Clipping convex decomposition algorithm for simple polygons.
 *
 *
 * This algorithm operates only on simple polygons.  A simple polygon is a polygon that
 * has vertices that are connected by edges where:
 *
 *  * Edges can only intersect at vertices
 *  * Vertices have at most two edge connections
 *
 *
 *
 * This implementation does not handle polygons with holes, but accepts both counter-clockwise
 * and clockwise polygons.
 *
 *
 * The polygon to decompose must be 4 or more vertices.
 *
 *
 * This algorithm creates a valid triangulation (N - 2) triangles, then employs the Hertel-Mehlhorn
 * algorithm to reduce the number of convex pieces.
 *
 *
 * This algorithm is O(n<sup>2</sup>).
 * @author William Bittle
 * @version 3.2.0
 * @since 2.2.0
 */
class EarClipping : Decomposer, Triangulator {
    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.decompose.Decomposer#decompose(org.dyn4j.geometry.Vector2[])
	 */
    override fun decompose(vararg points: Vector2): List<Convex> {
        // triangulate
        val dcel: DoubleEdgeList = createTriangulation(*points)

        // perform the Hertel-Mehlhorn algorithm to reduce the number
        // of convex pieces
        dcel.hertelMehlhorn()

        // return the convex pieces
        return dcel.getConvexDecomposition()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.decompose.Triangulator#triangulate(org.dyn4j.geometry.Vector2[])
	 */
    override fun triangulate(vararg points: Vector2): List<Triangle> {
        // triangulate
        val dcel: DoubleEdgeList = createTriangulation(*points)

        // return the triangulation
        return dcel.getTriangulation()
    }

    /**
     * Creates a triangulation of the given simple polygon and places it into the returned
     * doubly-connected edge list (DCEL).
     * @param points the simple polygon vertices
     * @return [DoubleEdgeList]
     * @since 3.1.9
     */
    fun createTriangulation(vararg points: Vector2): DoubleEdgeList {
        // get the number of points
        val size = points.size
        // check the size
        if (size < 4) throw IllegalArgumentException(message("geometry.decompose.invalidSize"))

        // get the winding order
        val winding = getWinding(*points)

        // reverse the array if the points are in clockwise order
        if (winding < 0.0) {
            reverseWinding(points)
        }

        // create a DCEL to store the decomposition
        val dcel = DoubleEdgeList(points)

        // create a doubly link list for the vertices
        var root: EarClippingVertex? = null
        var curr: EarClippingVertex? = null
        var prev: EarClippingVertex? = null
        for (i in 0 until size) {
            // get the current point
            val p = points[i]
            // create the vertex
            curr = EarClippingVertex(p)
            // get the vertices around the current point
            val p0 = points[if (i == 0) size - 1 else i - 1]
            val p1 = points[if (i + 1 == size) 0 else i + 1]
            // create the vectors representing the V
            val v1 = p.to(p0)
            val v2 = p.to(p1)
            // check for coincident vertices
            if (v2.isZero) {
                throw IllegalArgumentException(message("geometry.decompose.coincident"))
            }
            // check the angle between the two vectors
            curr.reflex = v1.cross(v2) >= 0.0
            // set the previous
            curr.prev = prev
            // set the previous node's next to the current node
            if (prev != null) {
                prev.next = curr
            }
            // set the current point's reference vertex
            curr.index = i
            // set the new previous to the current
            prev = curr
            if (root == null) {
                root = curr
            }
        }
        // finally wire up the first and last nodes
        root!!.prev = prev
        prev!!.next = root

        // set the ear flag
        var node: EarClippingVertex? = root
        for (i in 0 until size) {
            // set the ear flag
            node!!.ear = isEar(node, size)
            // go to the next vertex
            node = node.next
        }

        // decompose the linked list into the triangles
        node = root
        var n = size
        // stop when we only have 3 vertices left
        while (n > 3) {

            // is the node an ear node?
            if (node!!.ear) {
                // create a diagonal for this ear
                dcel.addHalfEdges(node.next!!.index, node.prev!!.index)
                // get the previous and next nodes
                val pNode: EarClippingVertex = node.prev!!
                val nNode: EarClippingVertex = node.next!!
                // remove this node from the list
                pNode.next = node.next
                nNode.prev = node.prev
                // re-evaluate the adjacent vertices reflexive-ness only if its reflex
                // (convex vertices will remain convex)
                if (pNode.reflex) {
                    // determine if it is still reflex
                    pNode.reflex = isReflex(pNode)
                }
                if (nNode.reflex) {
                    // determine if it is still reflex
                    nNode.reflex = isReflex(nNode)
                }
                // re-evaluate the ear-ness of the adjacent vertices
                if (!pNode.reflex) {
                    pNode.ear = isEar(pNode, n)
                }
                // re-evaluate the ear-ness of the adjacent vertices
                if (!nNode.reflex) {
                    nNode.ear = isEar(nNode, n)
                }
                n--
            }
            node = node.next
        }
        return dcel
    }

    /**
     * Returns true if the given vertex is a reflex vertex.
     *
     *
     * A reflex vertex is a vertex who's adjacent vertices create an
     * an angle greater than 180 degrees (or the cross product is
     * positive) for CCW vertex winding.
     * @param vertex the vertex to test
     * @return boolean true if the given vertex is considered a reflex vertex
     */
    fun isReflex(vertex: EarClippingVertex): Boolean {
        // get the triangle points
        val p: Vector2 = vertex.point
        val p0: Vector2 = vertex.prev!!.point
        val p1: Vector2 = vertex.next!!.point
        // create vectors from the current point
        val v1 = p.to(p0)
        val v2 = p.to(p1)
        // check for reflex
        return v1.cross(v2) >= 0.0
    }

    /**
     * Returns true if the given vertex is considered an ear vertex.
     *
     *
     * A vertex is an ear vertex if the triangle created by the adjacent vertices
     * of the given vertex does not contain any other vertices within it.
     *
     *
     * A reflex vertex cannot be an ear.
     * @param vertex the vertex to test for ear-ness
     * @param n the number of vertices
     * @return boolean true if the given vertex is considered an ear vertex
     */
    fun isEar(vertex: EarClippingVertex, n: Int): Boolean {
        // reflex vertices cannot be ears
        if (vertex.reflex) return false
        var ear = true
        // get the triangle created by this point and its adjacent vertices
        val a: Vector2 = vertex.point
        val b: Vector2 = vertex.next!!.point
        val c: Vector2 = vertex.prev!!.point

        // check if any other points in the linked list are contained within
        // this triangle

        // don't check any points on the triangle for containment
        var tNode: EarClippingVertex = vertex.next!!.next!!
        for (j in 0 until n - 3) {
            // we only need to test reflex nodes
            if (tNode.reflex) {
                // then check for containment
                if (this.contains(a, b, c, tNode.point)) {
                    // if there exists a vertex that is contained in the triangle
                    // then we can immediately exit the loop
                    ear = false
                    break
                }
            }
            // test the next vertex
            tNode = tNode.next!!
        }
        return ear
    }

    /**
     * Returns true if the given point, p, is contained in the triangle created
     * by a, b, and c.
     * @param a the first point of the triangle
     * @param b the second point of the triangle
     * @param c the third point of the triangle
     * @param p the point to test for containment
     * @return boolean true if the given point is contained in the given triangle
     */
    protected fun contains(a: Vector2, b: Vector2, c: Vector2, p: Vector2): Boolean {
        // create a vector representing edge ab
        val ab: Vector2 = a.to(b)
        // create a vector representing edge ac
        val ac: Vector2 = a.to(c)
        // create a vector from a to the point
        val pa: Vector2 = a.to(p)
        val dot00 = ac.dot(ac)
        val dot01 = ac.dot(ab)
        val dot02 = ac.dot(pa)
        val dot11 = ab.dot(ab)
        val dot12 = ab.dot(pa)
        val denominator = dot00 * dot11 - dot01 * dot01
        val u = (dot11 * dot02 - dot01 * dot12) / denominator
        val v = (dot00 * dot12 - dot01 * dot02) / denominator
        return u > 0 && v > 0 && u + v <= 1 + CONTAINS_EPSILON
    }

    companion object {
        /** Epsilon for checking for near containment of vertices within triangles  */
        private val CONTAINS_EPSILON: Double = sqrt(Epsilon.E)
    }
}