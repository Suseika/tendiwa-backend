package org.tendiwa.geometry;

import org.junit.Test;

import static org.junit.Assert.*;

public class StupidPriceduralRecsTest {

	@Test
	public void testRectangleIntersectsSegment() {
		assertTrue(
			StupidPriceduralRecs.rectangleIntersectsSegment(
				new Rectangle(10, 10, 10, 10),
				new Segment2D(
					new Point2D(5, 5),
					new Point2D(15, 15)
				)
			)
		);
	}

	@Test
	public void testRectangleDoesntIntersectSegment() {
		assertFalse(
			StupidPriceduralRecs.rectangleIntersectsSegment(
				new Rectangle(10, 10, 10, 10),
				new Segment2D(
					new Point2D(50, 50),
					new Point2D(21, 15)
				)
			)
		);
		assertFalse(
			StupidPriceduralRecs.rectangleIntersectsSegment(
				new Rectangle(40, 40, 40, 40),
				new Segment2D(
					new Point2D(20, 20),
					new Point2D(20, 100)
				)
			)
		);
	}
}