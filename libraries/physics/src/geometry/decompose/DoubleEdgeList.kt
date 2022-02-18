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
import org.dyn4j.geometry.Geometry.createPolygon
import org.dyn4j.geometry.Geometry.createTriangle
import org.dyn4j.resources.Messages.getString

/**
 * Highly specialized Doubly Connected Edge List (DCEL) used to store vertices of a simple polygon and then be used
 * to create and store triangulations and convex decompositions of that same polygon.
 *
 *
 * Upon creation and initialization, the [.vertices], [.edges], and [.faces] lists are
 * populated.  The [.vertices] list will have the same indexing as the source [Vector2][].
 * The [.edges] list will have edges with the same indexing as the source [Vector2][]
 * with the exception that twin vertices are stored in odd indices.
 *
 *
 * Since this implementation only handles simple polygons, only one [DoubleEdgeListFace] will be created
 * when the DCEL is created.  As more [DoubleEdgeListHalfEdge]s are added the number of faces will
 * increase.
 *
 *
 * [DoubleEdgeListHalfEdge]s are added to the DCEL via the [.addHalfEdges] method.
 * It's the responsibility of the calling class(es) to store references to the DCEL vertices.  This
 * can be achieved since the indexing of the [.vertices] list is the same as the source [Vector2][].
 * No check is performed to ensure that a pair of [DoubleEdgeListHalfEdge]s are added that already exist.
 * @author William Bittle
 * @version 3.4.0
 * @since 2.2.0
 */
class DoubleEdgeList {

    /** The list of nodes  */
    lateinit var vertices: MutableList<DoubleEdgeListVertex>

    /** The list of half edges  */
    lateinit var edges: MutableList<DoubleEdgeListHalfEdge>

    /** The list of faces  */
    lateinit var faces: MutableList<DoubleEdgeListFace?>

    /**
     * Full constructor.
     * @param points the points of the simple polygon
     */
    constructor(points: Array<out Vector2>) {
        vertices = ArrayList()
        edges = ArrayList()
        faces = ArrayList()
        initialize(points)
    }

    /**
     * Initializes the DCEL class given the points of the polygon.
     * @param points the points of the polygon
     */
    fun initialize(points: Array<out Vector2>) {
        // get the number of points
        val size = points.size

        // we will always have exactly one face at the beginning
        val face = DoubleEdgeListFace()
        faces!!.add(face)
        var prevLeftEdge: DoubleEdgeListHalfEdge? = null
        var prevRightEdge: DoubleEdgeListHalfEdge? = null

        // loop over the points creating the vertices and
        // half edges for the data structure
        for (i in 0 until size) {
            val point = points[i]
            val vertex = DoubleEdgeListVertex(point)
            val left = DoubleEdgeListHalfEdge()
            val right = DoubleEdgeListHalfEdge()

            // create and populate the left
            // and right half edges
            left.face = face
            left.next = null
            left.origin = vertex
            left.twin = right
            right.face = null
            right.next = prevRightEdge
            right.origin = null
            right.twin = left

            // add the edges the edge list
            edges.add(left)
            edges.add(right)

            // populate the vertex
            vertex.leaving = left

            // add the vertex to the vertices list
            vertices.add(vertex)

            // set the previous next edge to this left edge
            if (prevLeftEdge != null) {
                prevLeftEdge.next = left
            }

            // set the previous right edge origin to this vertex
            if (prevRightEdge != null) {
                prevRightEdge.origin = vertex
            }

            // set the new previous edges
            prevLeftEdge = left
            prevRightEdge = right
        }

        // set the last left edge's next pointer to the
        // first left edge we created
        val firstLeftEdge = edges[0]
        prevLeftEdge!!.next = firstLeftEdge

        // set the first right edge's next pointer
        // to the last right edge we created
        // (note that right edges are at odd indices)
        val firstRightEdge = edges[1]
        firstRightEdge.next = prevRightEdge
        // set the last right edge's origin to the first
        // vertex in the list
        prevRightEdge!!.origin = vertices[0]

        // set the edge of the only face to the first
        // left edge
        // (note that the interior of each face has CCW winding)
        face.edge = firstLeftEdge
    }

    /**
     * Adds two half edges to this DCEL object given the vertices to connect.
     *
     *
     * This method assumes that no crossing edges will be added.
     * @param i the first vertex index
     * @param j the second vertex index
     */
    fun addHalfEdges(i: Int, j: Int) {
        val vertex1 = vertices[i]
        val vertex2 = vertices[j]
        this.addHalfEdges(vertex1, vertex2)
    }

    /**
     * Adds two half edges to this DCEL object given the vertices to connect.
     *
     *
     * This method assumes that no crossing edges will be added.
     * @param v1 the first vertex
     * @param v2 the second vertex
     */
    fun addHalfEdges(v1: DoubleEdgeListVertex?, v2: DoubleEdgeListVertex?) {
        // adding an edge splits the current face into two faces
        // so we need to create a new face
        val face = DoubleEdgeListFace()

        // create the new half edges for the new edge
        val left = DoubleEdgeListHalfEdge()
        val right = DoubleEdgeListHalfEdge()

        // find the reference face for these two vertices
        // the reference face is the face on which both the given
        // vertices are on
        val referenceDoubleEdgeListFace = getReferenceFace(v1, v2)

        // get the previous edges for these vertices that are on
        // the reference face
        val prev1 = getPreviousEdge(v1, referenceDoubleEdgeListFace)
        val prev2 = getPreviousEdge(v2, referenceDoubleEdgeListFace)

        // check for self intersection before setting up half edges
        if (Segment.getSegmentIntersection(
                prev1!!.origin!!.point,
                prev1.next!!.origin!!.point,
                prev2!!.origin!!.point,
                prev2.next!!.origin!!.point
            ) != null
        ) {
            throw IllegalArgumentException("The input must be a simple polygon. Edges " + prev1!!.origin!!.point + " -> " + prev1.next!!.origin!!.point + " and " + prev2!!.origin!!.point + " -> " + prev2.next!!.origin!!.point + " cross each other.")
        }
        face.edge = left
        referenceDoubleEdgeListFace!!.edge = right

        // setup both half edges
        left.face = face
        left.next = prev2.next
        left.origin = v1
        left.twin = right
        right.face = referenceDoubleEdgeListFace
        right.next = prev1.next
        right.origin = v2
        right.twin = left

        // set the previous edge's next pointers to the new half edges
        prev1.next = left
        prev2.next = right

        // set the new face for all the edges in the left list
        var curr = left.next
        while (curr != left) {
            curr!!.face = face
            curr = curr.next
        }

        // add the new edges to the list
        edges.add(left)
        edges.add(right)

        // add the new face to the list
        faces.add(face)
    }

    /**
     * Walks around the given face and finds the previous edge
     * for the given vertex.
     *
     *
     * This method assumes that the given vertex will be on the given face.
     * @param vertex the vertex to find the previous edge for
     * @param face the face the edge should lie on
     * @return [DoubleEdgeListHalfEdge] the previous edge
     */
    fun getPreviousEdge(vertex: DoubleEdgeListVertex?, face: DoubleEdgeListFace?): DoubleEdgeListHalfEdge? {
        // find the vertex on the given face and return the
        // edge that points to it
        val twin = vertex!!.leaving!!.twin
        var edge = vertex.leaving!!.twin!!.next!!.twin
        // look at all the edges that have their
        // destination as this vertex
        while (edge != twin) {
            // we can't use the getPrevious method on the leaving
            // edge since this doesn't give us the right previous edge
            // in all cases.  The real criteria is to find the edge that
            // has this vertex as the destination and has the same face
            // as the given face
            if (edge!!.face == face) {
                return edge
            }
            edge = edge.next!!.twin
        }
        // if we get here then its the last edge
        return edge
    }

    /**
     * Finds the face that both vertices are on.
     *
     *
     * If the given vertices are connected then the first common face is returned.
     *
     *
     * If the given vertices do not have a common face the first vertex's leaving
     * edge's face is returned.
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @return [DoubleEdgeListFace] the face on which both vertices lie
     */
    fun getReferenceFace(v1: DoubleEdgeListVertex?, v2: DoubleEdgeListVertex?): DoubleEdgeListFace? {
        // find the face that both vertices are on

        // if the leaving edge faces are already the same then just return
        if (v1!!.leaving!!.face == v2!!.leaving!!.face) return v1.leaving!!.face

        // loop over all the edges whose destination is the first vertex (constant time)
        var e1 = v1.leaving!!.twin!!.next!!.twin
        while (e1 != v1.leaving!!.twin) {
            // loop over all the edges whose destination is the second vertex (constant time)
            var e2 = v2.leaving!!.twin!!.next!!.twin
            while (e2 != v2.leaving!!.twin) {
                // if we find a common face, that must be the reference face
                if (e1!!.face == e2!!.face) return e1.face
                e2 = e2.next!!.twin
            }
            e1 = e1!!.next!!.twin
        }

        // if we don't find a common face then return v1.leaving.face
        return v1.leaving!!.face
    }

    /**
     * Removes the half edges specified by the given interior edge index.
     *
     *
     * This method removes both halves of the edge.
     * @param index the index of the interior half edge to remove
     */
    fun removeHalfEdges(index: Int) {
        val e = edges[index]
        this.removeHalfEdges(index, e)
    }

    /**
     * Removes the given half edge and its twin.
     * @param edge the half edge to remove
     */
    fun removeHalfEdges(edge: DoubleEdgeListHalfEdge) {
        val index = edges.indexOf(edge)
        this.removeHalfEdges(index, edge)
    }

    /**
     * Removes the given half edge and its twin.
     * @param index the index of the given edge
     * @param edge the half edge to remove
     */
    fun removeHalfEdges(index: Int, edge: DoubleEdgeListHalfEdge) {
        // wire up the two end points to remove the edge
        val face = edge.twin!!.face

        // we only need to re-wire the internal edges
        val ePrev = edge.previous
        val tPrev = edge.twin!!.previous
        val eNext = edge.next
        val tNext = edge.twin!!.next
        ePrev!!.next = tNext
        tPrev!!.next = eNext
        face!!.edge = eNext

        // set the face
        var te = eNext
        while (te != tNext) {
            te!!.face = face
            te = te.next
        }

        // remove the unneeded face
        faces.remove(edge.face)

        // remove the edges
        edges.removeAt(index) // the edge
        edges.removeAt(index) // the edge's twin
    }

    /**
     * Returns the convex decomposition of this DCEL assuming that the remaining
     * faces are all convex polygons.
     * @return List&lt;[Convex]&gt;
     */
    fun getConvexDecomposition(): List<Convex> {
        val convexes: MutableList<Convex> = ArrayList()

        // get the number of faces
        val fSize = faces.size

        // create a y-monotone polygon for each face
        for (i in 0 until fSize) {
            // get the face
            val face = faces[i]

            // get the number of Edges ( = the number of vertices) on this face
            val size = face!!.edgeCount

            // get the reference edge of the face
            var left = face.edge
            val vertices = arrayOfNulls<Vector2>(size)
            vertices[0] = left!!.origin!!.point
            left = left.next
            var j = 1
            while (left != face.edge) {
                vertices[j++] = left!!.origin!!.point
                left = left.next
            }
            if (vertices.size < 3) {
                throw IllegalArgumentException(getString("geometry.decompose.crossingEdges"))
            }
            val p = createPolygon(*vertices)
            convexes.add(p)
        }
        return convexes
    }

    /**
     * Returns the triangulation of this DCEL assuming that the remaining
     * faces are all triangles.
     * @return List&lt;[Triangle]&gt;
     * @since 3.1.9
     */
    fun getTriangulation(): List<Triangle> {
        val triangles: MutableList<Triangle> = ArrayList()

        // get the number of faces
        val fSize = faces.size

        // create a y-monotone polygon for each face
        for (i in 0 until fSize) {
            // get the face
            val face = faces[i]

            // get the number of Edges ( = the number of vertices) on this face
            val size = face!!.edgeCount

            // get the reference edge of the face
            var left = face.edge
            val vertices = arrayOfNulls<Vector2>(size) as Array<Vector2>
            vertices[0] = left!!.origin!!.point
            left = left.next
            var j = 1
            while (left != face.edge) {
                vertices[j++] = left!!.origin!!.point
                left = left.next
            }

            // the vertices should form a triangle
            if (vertices.size != 3) {
                throw IllegalArgumentException(getString("geometry.decompose.crossingEdges"))
            }

            // add the triangle
            val t = createTriangle(vertices[0], vertices[1], vertices[2])
            triangles.add(t)
        }
        return triangles
    }

    /**
     * Performs a triangulation of the DCEL assuming all faces are Monotone Y polygons.
     */
    fun triangulateYMonotonePolygons() {
        val monotonePolygons =
            getYMonotonePolygons()
        val size = monotonePolygons.size
        for (i in 0 until size) {
            triangulateYMonotonePolygon(monotonePolygons[i])
        }
    }

    /**
     * Triangulates the given y-monotone polygon adding the new diagonals to this DCEL.
     * @param monotonePolygon the monotone polygon (x or y) to triangulate
     */
    fun triangulateYMonotonePolygon(monotonePolygon: MonotonePolygon<DoubleEdgeListVertex?>) {
        // create a stack to support triangulation
        val stack: MutableList<MonotoneVertex<DoubleEdgeListVertex?>> =
            ArrayList()

        // get the sorted monotone vertices
        val vertices: List<MonotoneVertex<DoubleEdgeListVertex?>> = monotonePolygon.vertices

        // a monotone polygon can be triangulated in O(n) time

        // push the first two onto the stack
        // push
        stack.add(vertices[0])
        stack.add(vertices[1])
        var i = 2
        while (!stack.isEmpty()) {
            // get the next vertex in the sorted list
            val v = vertices[i]

            // get the bottom and top elements of the stack
            val vBot = stack[0]
            val vTop = stack[stack.size - 1]

            // is the current vertex adjacent to the bottom element
            // but not to the top element?
            if (v.isAdjacent(vBot) && !v.isAdjacent(vTop)) {
                // create the triangles and pop all the points
                while (stack.size > 1) {
                    // pop
                    val vt = stack.removeAt(stack.size - 1)
                    // create diagonal
                    this.addHalfEdges(v.data, vt.data)
                }
                // clear the bottom point
                stack.clear()

                // push the remaining edge
                stack.add(vTop)
                stack.add(v)
            } else if (v.isAdjacent(vTop) && !v.isAdjacent(vBot)) {
                var cross = 0.0
                var sSize = stack.size
                while (sSize > 1) {
                    val vt = stack[sSize - 1]
                    val vt1 = stack[sSize - 2]
                    val p1 = v.data!!.point
                    val p2 = vt.data!!.point
                    val p3 = vt1.data!!.point

                    // what chain is the current vertex on
                    cross = if (v.chainType === MonotoneChainType.LEFT || v.chainType === MonotoneChainType.BOTTOM) {
                        val v1: Vector2 = p2.to(p3)
                        val v2: Vector2 = p2.to(p1)
                        v1.cross(v2)
                    } else {
                        val v1: Vector2 = p1.to(p2)
                        val v2: Vector2 = p3.to(p2)
                        v1.cross(v2)
                    }

                    // make sure the angle is less than pi before we create
                    // a triangle from the points
                    if (cross < -Epsilon.E) {
                        // add the half edges
                        this.addHalfEdges(v.data, vt1.data)
                        // remove the top element
                        // pop
                        stack.removeAt(sSize - 1)
                        sSize--
                    } else {
                        // once we find an angle that is greater than pi then
                        // we can quit and move to the next vertex in the sorted list
                        break
                    }
                }
                stack.add(v)
            } else if (v.isAdjacent(vTop) && v.isAdjacent(vBot)) {
                // create the triangles and pop all the points
                // pop
                stack.removeAt(stack.size - 1)
                while (stack.size > 1) {
                    // pop
                    val vt = stack.removeAt(stack.size - 1)
                    // create diagonal
                    this.addHalfEdges(v.data, vt.data)
                }
                // we are done
                break
            }
            i++
        }
    }

    /**
     * Returns a list of y-monotone polygons from the faces of this DCEL.
     *
     *
     * This method assumes that all faces within this DCEL are y-monotone and does not
     * perform any verification of this assumption.
     * @return List&lt;[MonotonePolygon]&gt;
     */
    fun getYMonotonePolygons(): List<MonotonePolygon<DoubleEdgeListVertex?>> {
        // get the number of faces
        val fSize = faces!!.size

        // create a list to store the y-monotone polygons
        val yMonotonePolygons: MutableList<MonotonePolygon<DoubleEdgeListVertex?>> =
            ArrayList(fSize)

        // create a y-monotone polygon for each face
        for (i in 0 until fSize) {
            // get the face
            val face = faces[i]

            // Each face contains a y-monotone polygon.  We need to obtain a sorted
            // doubly-linked list of the vertices to triangulate easily.  We can create
            // the doubly-linked list while finding the maximum vertex in O(n) time.  
            // We can sort the list in O(n) time using the doubly-linked list we just
            // created.

            // get the number of Edges ( = the number of vertices) on this face
            val size = face!!.edgeCount

            // get the reference edge of the face
            var left = face.edge

            // create the first vertex
            val root = MonotoneVertex(left!!.origin)

            // move to the next origin
            left = left.next

            // build the doubly linked list of vertices
            var prev = root
            var curr: MonotoneVertex<DoubleEdgeListVertex?>? = null
            var max = root
            while (left != face.edge) {
                // create a new vertex
                curr = MonotoneVertex(left!!.origin)
                curr.previous = prev

                // set the previous vertex's next pointer to the new one
                prev.next = curr

                // find the point with maximum y
                val p = curr.data!!.point
                val q = max.data!!.point
                // compare the y values
                var diff = p!!.y - q!!.y
                if (diff == 0.0) {
                    // if they are near zero then
                    // compare the x values
                    diff = p.x - q.x
                    if (diff < 0) {
                        max = curr
                    }
                } else if (diff > 0.0) {
                    max = curr
                }

                // move to the next point
                left = left.next

                // set the previous to the current
                prev = curr
            }

            // wire up the last and first vertices
            root.previous = curr
            curr!!.next = root

            // create a sorted array of Vertices
            val sorted = ArrayList<MonotoneVertex<DoubleEdgeListVertex?>>(size)

            // the first point is the vertex with maximum y
            sorted.add(max)
            // default the location to the left chain
            max.chainType = MonotoneChainType.LEFT

            // perform a O(n) sorting routine starting from the
            // maximum y vertex
            var currLeft = max.next
            var currRight = max.previous
            var j = 1
            while (j < size) {
                // get the left and right chain points
                val l = currLeft!!.data!!.point
                val r = currRight!!.data!!.point

                // which has the smaller y?
                var diff = l.y - r.y
                // if it's equal compare the x values
                if (diff == 0.0) {
                    diff = r.x - l.x
                }
                if (diff > 0) {
                    sorted.add(currLeft)
                    currLeft.chainType = MonotoneChainType.LEFT
                    currLeft = currLeft.next
                } else {
                    sorted.add(currRight)
                    currRight.chainType = MonotoneChainType.RIGHT
                    currRight = currRight.previous
                }
                j++
            }
            // set the last point's chain to the right
            sorted[size - 1].chainType = MonotoneChainType.RIGHT

            // add a new y-monotone polygon to the list
            yMonotonePolygons.add(MonotonePolygon(MonotonePolygonType.Y, sorted))
        }
        return yMonotonePolygons
    }

    /**
     * Performs the Hertel-Mehlhorn algorithm on the given DCEL assuming that
     * it is a valid triangulation.
     *
     *
     * This method will remove unnecessary diagonals and remove faces that get merged
     * leaving a convex decomposition.
     *
     *
     * This method is guaranteed to produce a convex decomposition with no more than
     * 4 times the minimum number of convex pieces.
     */
    fun hertelMehlhorn() {
        // loop over all the edges and see which we can remove
        val vSize = vertices.size

        // This method will remove any unnecessary diagonals (those that do not
        // form reflex vertices when removed).  This method is O(n) where n is the
        // number of diagonals added to the original DCEL.  We can start processing
        // diagonals after all the initial diagonals (the initial diagonals are the
        // edges of the original polygon).  We can also skip every other half edge
        // since each edge is stored with its twin in the next index.
        var i = vSize * 2
        while (i < edges.size) {

            // see if removing this edge creates a reflex vertex at the end points
            val e = edges[i]

            // test the first end point
            var v1 = e.origin
            var v0 = e.previous!!.origin
            var v2 = e.twin!!.next!!.next!!.origin

            // check if removing this half edge creates a reflex vertex at the
            // origin vertex of this half edge
            if (isReflex(v0, v1, v2)) {
                // if it did, then we cannot remove this edge
                // so skip the next one and continue
                i += 2
                continue
            }

            // test the other end point
            v1 = e.twin!!.origin
            v0 = e.twin!!.previous!!.origin
            v2 = e.next!!.next!!.origin

            // check if removing this half edge creates a reflex vertex at the
            // origin of this half edge's twin
            if (isReflex(v0, v1, v2)) {
                // if it did, then we cannot remove this edge
                // so skip the next one and continue
                i += 2
                continue
            }

            // otherwise we can remove this edge
            this.removeHalfEdges(i, e)
        }
    }

    /**
     * Returns true if the given vertices create a reflex vertex.
     * @param v0 the previous vertex
     * @param v1 the vertex
     * @param v2 the next vertex
     * @return boolean
     */
    fun isReflex(v0: DoubleEdgeListVertex?, v1: DoubleEdgeListVertex?, v2: DoubleEdgeListVertex?): Boolean {
        val p0 = v0!!.point
        val p1 = v1!!.point
        val p2 = v2!!.point
        val e1: Vector2 = p0.to(p1)
        val e2: Vector2 = p1.to(p2)

        // get the angle between the two edges (we assume CCW winding)
        val cross = e1.cross(e2)
        return if (cross < 0) true else false
    }
}