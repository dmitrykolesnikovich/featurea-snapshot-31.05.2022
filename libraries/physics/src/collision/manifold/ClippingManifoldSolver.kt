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
package org.dyn4j.collision.manifold

import org.dyn4j.collision.narrowphase.NarrowphaseDetector
import org.dyn4j.collision.narrowphase.Penetration
import org.dyn4j.geometry.*
import kotlin.math.abs

/**
 * Implementation of a Sutherland-Hodgman clipping [ManifoldSolver] algorithm.
 *
 *
 * A [NarrowphaseDetector] should return a penetration normal and depth when two [Convex] [Shape]s are
 * intersecting.  The penetration normal should always point from the first [Shape] to the second.  Using the
 * [Penetration], this class will find the closest features and perform a series of clipping operations to build
 * a contact [Manifold].
 *
 *
 * In the case that a [Convex] [Shape] returns a [PointFeature] [Feature], that feature will always
 * take precedence.
 *
 *
 * It's possible that no contact points are returned, in which case the [.getManifold]
 * method will return false.
 * @author William Bittle
 * @version 3.0.2
 * @since 1.0.0
 * @see [Contact Points Using Clipping](http://www.dyn4j.org/2011/11/contact-points-using-clipping/)
 */
class ClippingManifoldSolver : ManifoldSolver {
    /* (non-Javadoc)
	 * @see org.dyn4j.collision.manifold.ManifoldSolver#getManifold(org.dyn4j.collision.narrowphase.Penetration, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.collision.manifold.Manifold)
	 */
    override fun getManifold(
        penetration: Penetration,
        convex1: Convex,
        transform1: Transform,
        convex2: Convex,
        transform2: Transform,
        manifold: Manifold
    ): Boolean {
        // make sure the manifold passed in is cleared
        manifold.clear()

        // get the penetration normal
        val n: Vector2 = penetration.normal!!

        // get the reference feature for the first convex shape
        val feature1: Feature = convex1.getFarthestFeature(n, transform1)
        // check for vertex
        if (feature1 is PointFeature) {
            // if the maximum
            val vertex: PointFeature = feature1 as PointFeature
            val mp = ManifoldPoint(ManifoldPointId.DISTANCE, vertex.point, penetration.depth)
            manifold.points.add(mp)
            manifold.normal = n.negative
            return true
        }

        // get the reference feature for the second convex shape
        val ne: Vector2 = n.negative
        val feature2: Feature = convex2.getFarthestFeature(ne, transform2)
        // check for vertex
        if (feature2 is PointFeature) {
            val vertex: PointFeature = feature2 as PointFeature
            val mp = ManifoldPoint(ManifoldPointId.DISTANCE, vertex.point, penetration.depth)
            manifold.points.add(mp)
            manifold.normal = ne
            return true
        }

        // both features are edge features
        var reference: EdgeFeature = feature1 as EdgeFeature
        var incident: EdgeFeature = feature2 as EdgeFeature

        // choose the reference and incident edges
        var flipped = false
        // which edge is more perpendicular?
        val e1: Vector2 = reference.edge
        val e2: Vector2 = incident.edge
        if (abs(e1.dot(n)) > abs(e2.dot(n))) {
            // shape2's edge is more perpendicular
            // so swap the reference and incident edges
            val e: EdgeFeature = reference
            reference = incident
            incident = e
            // flag that the features flipped
            flipped = true
        }

        // create the reference edge vector
        val refev: Vector2 = reference.edge
        // normalize it
        refev.normalize()

        // compute the offset of the reference edge points along the reference edge
        val offset1: Double = -refev.dot(reference.vertex1.point!!)

        // clip the incident edge by the reference edge's left edge
        val clip1: List<PointFeature> =
            clip(incident.vertex1, incident.vertex2, refev.negative, offset1)
        // check the number of points
        if (clip1.size < 2) {
            return false
        }

        // compute the offset of the reference edge points along the reference edge
        val offset2: Double = refev.dot(reference.vertex2.point!!)

        // clip the clip1 edge by the reference edge's right edge
        val clip2: List<PointFeature> = clip(clip1[0], clip1[1], refev, offset2)
        // check the number of points
        if (clip2.size < 2) {
            return false
        }

        // we need to change the normal to the reference edge's normal
        // since they may not have been the same
        val frontNormal: Vector2 = refev.rightHandOrthogonalVector
        // also get the maximum point's depth
        val frontOffset: Double = frontNormal.dot(reference.maximum.point!!)

        // set the normal
        manifold.normal = if (flipped) frontNormal.negative else frontNormal

        // test if the clip points are behind the reference edge
        for (i in clip2.indices) {
            val vertex: PointFeature = clip2[i]
            val point: Vector2 = vertex.point!!
            val depth: Double = frontNormal.dot(point) - frontOffset
            // make sure the point is behind the front normal
            if (depth >= 0.0) {
                // create an id for the manifold point
                val id =
                    IndexedManifoldPointId(reference.index, incident.index, vertex.index, flipped)
                // create the manifold point
                val mp = ManifoldPoint(id, point, depth)
                // add it to the list
                manifold.points.add(mp)
            }
        }
        // make sure we didn't clip all the points
        return manifold.points.size != 0
        // return the clipped points
    }

    /**
     * Clips the segment given by s1 and s2 by n.
     * @param v1 the first vertex of the segment to be clipped
     * @param v2 the second vertex of the segment to be clipped
     * @param n the clipping plane/line
     * @param offset the offset of the end point of the segment to be clipped
     * @return List&lt;[Vector2]&gt; the clipped segment
     */
    protected fun clip(
        v1: PointFeature,
        v2: PointFeature,
        n: Vector2,
        offset: Double
    ): List<PointFeature> {
        val points: MutableList<PointFeature> = ArrayList(2)
        val p1: Vector2 = v1.point!!
        val p2: Vector2 = v2.point!!

        // calculate the distance between the end points of the edge and the clip line
        val d1: Double = n.dot(p1) - offset
        val d2: Double = n.dot(p2) - offset

        // add the points if they are behind the line
        if (d1 <= 0.0) points.add(v1)
        if (d2 <= 0.0) points.add(v2)

        // check if they are on opposing sides of the line
        if (d1 * d2 < 0.0) {
            // get the edge vector
            val e: Vector2 = p1.to(p2)
            // clip to obtain another point
            val u = d1 / (d1 - d2)
            e.multiply(u)
            e.add(p1)
            if (d1 > 0.0) {
                points.add(PointFeature(e, v1.index))
            } else {
                points.add(PointFeature(e, v2.index))
            }
        }
        return points
    }
}