package tests.painting;

import java.awt.Point;
import java.awt.Rectangle;

import painting.TestCanvas;
import tendiwa.core.meta.Range;
import tendiwa.geometry.InterrectangularPath;
import tendiwa.geometry.RandomGrowingRectangleSystem;
import tendiwa.geometry.RectangleArea;
import tendiwa.geometry.RectangleSystem;
import tendiwa.geometry.RectangleSystem.NeighboursIterable;

public class RectangleSystemClockwiseIterationsDemo extends TestCanvas {

	public static void main(String[] args) {
		visualize();
	}

	@Override
	public void paint() {
		setScale(3);
		RectangleSystem rs = new RandomGrowingRectangleSystem(0, new Range(5, 5), 20, new Point(10, 12), this);
//		RectangleArea rMostNighbours = rs.findRectangleWithMostNeigbors();
//		NeighboursIterable neighbors = rs.getNeighboursIterable(rMostNighbours);
//		neighbors.setRandomStartingNeighbour();
//		neighbors.setClockwiseOrder();
//		InterrectangularPath trail = new InterrectangularPath(rs, 1);
//		for (RectangleArea r : neighbors) {
//			trail.addNextRectangle(r);
//		}
//		draw(trail);
	}

}
