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
package org.dyn4j.collision;

import java.util.List;

import junit.framework.TestCase;

import org.dyn4j.collision.broadphase.BroadphasePair;
import org.dyn4j.collision.manifold.ClippingManifoldSolver;
import org.dyn4j.collision.manifold.Manifold;
import org.dyn4j.collision.manifold.ManifoldPoint;
import org.dyn4j.collision.narrowphase.Gjk;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.collision.narrowphase.Sat;
import org.dyn4j.collision.narrowphase.Separation;
import org.dyn4j.geometry.Segment;
import org.dyn4j.geometry.Shape;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link Segment} - {@link Triangle} collision detection.
 * @author William Bittle
 * @version 3.1.5
 * @since 1.0.0
 */
public class SegmentTriangleTest extends AbstractTest {
	/** The test {@link Segment} */
	private Segment seg;
	
	/** The test {@link Triangle} */
	private Triangle tri;
	
	/**
	 * Sets up the test.
	 */
	@Before
	public void setup() {
		this.seg = new Segment(new Vector2(-0.3, 0.2), new Vector2(0.0, -0.1));
		this.tri = new Triangle(
						new Vector2(0.45, -0.12),
						new Vector2(-0.45, 0.38),
						new Vector2(-0.15, -0.22));
	}
	
	/**
	 * Tests {@link Shape} AABB.
	 */
	@Test
	public void detectShapeAABB() {
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		// test containment
		TestCase.assertTrue(this.sap.detect(seg, t1, tri, t2));
		TestCase.assertTrue(this.sap.detect(tri, t2, seg, t1));
		
		// test overlap
		t2.translate(0.15, 0.0);
		TestCase.assertTrue(this.sap.detect(seg, t1, tri, t2));
		TestCase.assertTrue(this.sap.detect(tri, t2, seg, t1));
		
		// test only AABB overlap
		t2.translate(0.0, 0.2);
		TestCase.assertTrue(this.sap.detect(seg, t1, tri, t2));
		TestCase.assertTrue(this.sap.detect(tri, t2, seg, t1));
		
		// test no overlap
		t2.translate(0.0, 0.5);
		TestCase.assertFalse(this.sap.detect(seg, t1, tri, t2));
		TestCase.assertFalse(this.sap.detect(tri, t2, seg, t1));
	}
	
	/**
	 * Tests {@link Collidable} AABB.
	 */
	@Test	
	public void detectCollidableAABB() {
		// create some collidables
		CollidableTest ct1 = new CollidableTest(seg);
		CollidableTest ct2 = new CollidableTest(tri);
		
		// test containment
		TestCase.assertTrue(this.sap.detect(ct1, ct2));
		TestCase.assertTrue(this.sap.detect(ct2, ct1));
		
		// test overlap
		ct2.translate(0.15, 0.0);
		TestCase.assertTrue(this.sap.detect(ct1, ct2));
		TestCase.assertTrue(this.sap.detect(ct2, ct1));
		
		// test only AABB overlap
		ct2.translate(0.0, 0.2);
		TestCase.assertTrue(this.sap.detect(ct1, ct2));
		TestCase.assertTrue(this.sap.detect(ct2, ct1));
		
		// test no overlap
		ct2.translate(0.0, 0.5);
		TestCase.assertFalse(this.sap.detect(ct1, ct2));
		TestCase.assertFalse(this.sap.detect(ct2, ct1));
	}
	
	/**
	 * Tests the broadphase detectors.
	 */
	@Test
	public void detectBroadphase() {
		List<BroadphasePair<CollidableTest, Fixture>> pairs;
		
		// create some collidables
		CollidableTest ct1 = new CollidableTest(seg);
		CollidableTest ct2 = new CollidableTest(tri);
		
		this.sap.add(ct1);
		this.sap.add(ct2);
		this.dyn.add(ct1);
		this.dyn.add(ct2);
		
		// test containment
		pairs = this.sap.detect();
		TestCase.assertEquals(1, pairs.size());
		pairs = this.dyn.detect();
		TestCase.assertEquals(1, pairs.size());
		
		// test overlap
		ct1.translate(0.15, 0.0);
		this.sap.update(ct1);
		this.dyn.update(ct1);
		pairs = this.sap.detect();
		TestCase.assertEquals(1, pairs.size());
		pairs = this.dyn.detect();
		TestCase.assertEquals(1, pairs.size());
		
		// test only AABB overlap
		ct2.translate(0.0, 0.2);
		this.sap.update(ct2);
		this.dyn.update(ct2);
		pairs = this.sap.detect();
		TestCase.assertEquals(1, pairs.size());
		pairs = this.dyn.detect();
		TestCase.assertEquals(1, pairs.size());
		
		// test no overlap
		ct1.translate(0.0, 0.9);
		this.sap.update(ct1);
		this.dyn.update(ct1);
		pairs = this.sap.detect();
		TestCase.assertEquals(0, pairs.size());
		pairs = this.dyn.detect();
		TestCase.assertEquals(0, pairs.size());
	}
	
	/**
	 * Tests {@link Sat}.
	 */
	@Test
	public void detectSat() {
		Penetration p = new Penetration();
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		Vector2 n = null;
		
		// test containment
		TestCase.assertTrue(this.sat.detect(seg, t1, tri, t2, p));
		TestCase.assertTrue(this.sat.detect(seg, t1, tri, t2));
		n = p.getNormal();
		TestCase.assertEquals(0.894, n.x, 1.0e-3);
		TestCase.assertEquals(0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.187, p.getDepth(), 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.sat.detect(tri, t2, seg, t1, p));
		TestCase.assertTrue(this.sat.detect(tri, t2, seg, t1));
		n = p.getNormal();
		TestCase.assertEquals(-0.894, n.x, 1.0e-3);
		TestCase.assertEquals(-0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.187, p.getDepth(), 1.0e-3);
		
		// test overlap
		t2.translate(0.15, 0.0);
		TestCase.assertTrue(this.sat.detect(seg, t1, tri, t2, p));
		TestCase.assertTrue(this.sat.detect(seg, t1, tri, t2));
		n = p.getNormal();
		TestCase.assertEquals(0.894, n.x, 1.0e-3);
		TestCase.assertEquals(0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.053, p.getDepth(), 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.sat.detect(tri, t2, seg, t1, p));
		TestCase.assertTrue(this.sat.detect(tri, t2, seg, t1));
		n = p.getNormal();
		TestCase.assertEquals(-0.894, n.x, 1.0e-3);
		TestCase.assertEquals(-0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.053, p.getDepth(), 1.0e-3);
		
		// test AABB overlap
		t2.translate(0.0, 0.2);
		TestCase.assertFalse(this.sat.detect(seg, t1, tri, t2, p));
		TestCase.assertFalse(this.sat.detect(seg, t1, tri, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.sat.detect(tri, t2, seg, t1, p));
		TestCase.assertFalse(this.sat.detect(tri, t2, seg, t1));
		
		// test no overlap
		t2.translate(0.0, 0.5);
		TestCase.assertFalse(this.sat.detect(seg, t1, tri, t2, p));
		TestCase.assertFalse(this.sat.detect(seg, t1, tri, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.sat.detect(tri, t2, seg, t1, p));
		TestCase.assertFalse(this.sat.detect(tri, t2, seg, t1));
	}
	
	/**
	 * Tests {@link Gjk}.
	 */
	@Test
	public void detectGjk() {
		Penetration p = new Penetration();
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		Vector2 n = null;
		
		// test containment
		TestCase.assertTrue(this.gjk.detect(seg, t1, tri, t2, p));
		TestCase.assertTrue(this.gjk.detect(seg, t1, tri, t2));
		n = p.getNormal();
		TestCase.assertEquals(0.894, n.x, 1.0e-3);
		TestCase.assertEquals(0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.187, p.getDepth(), 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.detect(tri, t2, seg, t1, p));
		TestCase.assertTrue(this.gjk.detect(tri, t2, seg, t1));
		n = p.getNormal();
		TestCase.assertEquals(-0.894, n.x, 1.0e-3);
		TestCase.assertEquals(-0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.187, p.getDepth(), 1.0e-3);
		
		// test overlap
		t2.translate(0.15, 0.0);
		TestCase.assertTrue(this.gjk.detect(seg, t1, tri, t2, p));
		TestCase.assertTrue(this.gjk.detect(seg, t1, tri, t2));
		n = p.getNormal();
		TestCase.assertEquals(0.894, n.x, 1.0e-3);
		TestCase.assertEquals(0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.053, p.getDepth(), 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.detect(tri, t2, seg, t1, p));
		TestCase.assertTrue(this.gjk.detect(tri, t2, seg, t1));
		n = p.getNormal();
		TestCase.assertEquals(-0.894, n.x, 1.0e-3);
		TestCase.assertEquals(-0.447, n.y, 1.0e-3);
		TestCase.assertEquals(0.053, p.getDepth(), 1.0e-3);
		
		// test AABB overlap
		t2.translate(0.0, 0.2);
		TestCase.assertFalse(this.gjk.detect(seg, t1, tri, t2, p));
		TestCase.assertFalse(this.gjk.detect(seg, t1, tri, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.detect(tri, t2, seg, t1, p));
		TestCase.assertFalse(this.gjk.detect(tri, t2, seg, t1));
		
		// test no overlap
		t2.translate(0.0, 0.5);
		TestCase.assertFalse(this.gjk.detect(seg, t1, tri, t2, p));
		TestCase.assertFalse(this.gjk.detect(seg, t1, tri, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.detect(tri, t2, seg, t1, p));
		TestCase.assertFalse(this.gjk.detect(tri, t2, seg, t1));
	}
	
	/**
	 * Tests the {@link Gjk} distance method.
	 */
	@Test
	public void gjkDistance() {
		Separation s = new Separation();
		
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		Vector2 n, p1, p2;
		
		// test containment
		TestCase.assertFalse(this.gjk.distance(seg, t1, tri, t2, s));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.distance(tri, t2, seg, t1, s));
		
		// test overlap
		t2.translate(0.15, 0.0);
		TestCase.assertFalse(this.gjk.distance(seg, t1, tri, t2, s));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.distance(tri, t2, seg, t1, s));
		
		// test AABB overlap
		t2.translate(0.0, 0.2);
		TestCase.assertTrue(this.gjk.distance(seg, t1, tri, t2, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(0.056, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(0.707, n.x, 1.0e-3);
		TestCase.assertEquals(0.707, n.y, 1.0e-3);
		TestCase.assertEquals(-0.040, p1.x, 1.0e-3);
		TestCase.assertEquals(-0.060, p1.y, 1.0e-3);
		TestCase.assertEquals(0.000, p2.x, 1.0e-3);
		TestCase.assertEquals(-0.019, p2.y, 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.distance(tri, t2, seg, t1, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(0.056, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(-0.707, n.x, 1.0e-3);
		TestCase.assertEquals(-0.707, n.y, 1.0e-3);
		TestCase.assertEquals(0.000, p1.x, 1.0e-3);
		TestCase.assertEquals(-0.019, p1.y, 1.0e-3);
		TestCase.assertEquals(-0.040, p2.x, 1.0e-3);
		TestCase.assertEquals(-0.060, p2.y, 1.0e-3);
		
		// test no overlap
		t2.translate(0.0, 0.5);
		TestCase.assertTrue(this.gjk.distance(seg, t1, tri, t2, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(0.410, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(0.707, n.x, 1.0e-3);
		TestCase.assertEquals(0.707, n.y, 1.0e-3);
		TestCase.assertEquals(-0.290, p1.x, 1.0e-3);
		TestCase.assertEquals(0.190, p1.y, 1.0e-3);
		TestCase.assertEquals(0.000, p2.x, 1.0e-3);
		TestCase.assertEquals(0.480, p2.y, 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.distance(tri, t2, seg, t1, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(0.410, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(-0.707, n.x, 1.0e-3);
		TestCase.assertEquals(-0.707, n.y, 1.0e-3);
		TestCase.assertEquals(0.000, p1.x, 1.0e-3);
		TestCase.assertEquals(0.480, p1.y, 1.0e-3);
		TestCase.assertEquals(-0.290, p2.x, 1.0e-3);
		TestCase.assertEquals(0.190, p2.y, 1.0e-3);
	}
	
	/**
	 * Test the {@link ClippingManifoldSolver}.
	 */
	@Test
	public void getClipManifold() {
		Manifold m = new Manifold();
		Penetration p = new Penetration();
		
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		ManifoldPoint mp;
		Vector2 p1;
		
		// test containment gjk
		this.gjk.detect(seg, t1, tri, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, seg, t1, tri, t2, m));
		TestCase.assertEquals(2, m.getPoints().size());
		// try reversing the shapes
		this.gjk.detect(tri, t2, seg, t1, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, tri, t2, seg, t1, m));
		TestCase.assertEquals(2, m.getPoints().size());
		
		// test containment sat
		this.sat.detect(seg, t1, tri, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, seg, t1, tri, t2, m));
		TestCase.assertEquals(2, m.getPoints().size());
		// try reversing the shapes
		this.sat.detect(tri, t2, seg, t1, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, tri, t2, seg, t1, m));
		TestCase.assertEquals(2, m.getPoints().size());
		
		t2.translate(0.15, 0.0);
		
		// test overlap gjk
		this.gjk.detect(seg, t1, tri, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, seg, t1, tri, t2, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(0.000, p1.x, 1.0e-3);
		TestCase.assertEquals(-0.100, p1.y, 1.0e-3);
		TestCase.assertEquals(0.053, mp.getDepth(), 1.0e-3);
		// try reversing the shapes
		this.gjk.detect(tri, t2, seg, t1, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, tri, t2, seg, t1, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(0.000, p1.x, 1.0e-3);
		TestCase.assertEquals(-0.100, p1.y, 1.0e-3);
		TestCase.assertEquals(0.053, mp.getDepth(), 1.0e-3);
		
		// test overlap sat
		this.sat.detect(seg, t1, tri, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, seg, t1, tri, t2, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(0.000, p1.x, 1.0e-3);
		TestCase.assertEquals(-0.100, p1.y, 1.0e-3);
		TestCase.assertEquals(0.053, mp.getDepth(), 1.0e-3);
		// try reversing the shapes
		this.sat.detect(tri, t2, seg, t1, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, tri, t2, seg, t1, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(0.000, p1.x, 1.0e-3);
		TestCase.assertEquals(-0.100, p1.y, 1.0e-3);
		TestCase.assertEquals(0.053, mp.getDepth(), 1.0e-3);
	}
}
