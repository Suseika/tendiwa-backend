package tendiwa.geometry;

import java.util.HashSet;
import java.util.Set;

import tendiwa.core.meta.Chance;
import tendiwa.core.meta.Direction;


/**
 * A RectangleSystem that is built randomly splitting an initial rectangle of a
 * particular size.
 */
public class RandomRectangleSystem extends RectangleSystem {
	public int minRectangleWidth;
	
	public RandomRectangleSystem(int startX, int startY, int width, int height, int minRectangleWidth, int borderWidth) {
		super(borderWidth);
		this.minRectangleWidth = minRectangleWidth;
		// If a Rectangle is less than this by a certain dimension, it won't be
		// split by that dimension
		int splitableRecSizeLimit = minRectangleWidth * 2 + borderWidth + 1;
		RectangleArea r1 = addRectangleArea(startX, startY, width, height);
		Chance ch = new Chance(50);
		boolean noMoreRectangles = false;
		// Randomly split area into content, saving the resulting graph
		while (!noMoreRectangles) {
			noMoreRectangles = true;
			Set<RectangleArea> rectangles = new HashSet<RectangleArea>(content);
			for (RectangleArea r : rectangles) {
				if (r.width > splitableRecSizeLimit && r.height > splitableRecSizeLimit) {
					noMoreRectangles = false;
					randomlySplitRectangle(r, ch.roll() ? Direction.V
						: Direction.H);
				} else if (r.width > splitableRecSizeLimit) {
					noMoreRectangles = false;
					randomlySplitRectangle(r, Direction.V);
				} else if (r.height > splitableRecSizeLimit) {
					noMoreRectangles = false;
					randomlySplitRectangle(r, Direction.H);
				}
			}
		}
	}

	/**
	 * Splits a RectangleArea into two RectangleAreas, randomly choosing the
	 * line of splitting.
	 * 
	 * @param r
	 *            A RectangleArea to split.
	 * @param dir
	 *            Direction of splitting.
	 */
	private void randomlySplitRectangle(RectangleArea r, Direction dir) {
		if (dir == Direction.H) {
			int y = Chance.rand(r.y + minRectangleWidth, r.y + r.height - minRectangleWidth - borderWidth);
			splitRectangle(r, dir, y - r.y, false);
		} else if (dir == Direction.V) {
			int x = Chance.rand(r.x + minRectangleWidth, r.x + r.width - minRectangleWidth - borderWidth);
			splitRectangle(r, dir, x - r.x, false);
		} else {
			throw new Error("Dafuq");
		}
	}
}