package tendiwa.geometry;

import java.awt.Rectangle;

import tendiwa.core.meta.Chance;
import tendiwa.core.meta.Range;


/**
 * A RectangleSystem that is constructed by setting an "initial" rectangle and 
 * then adding new rectangles adjacent to first the existing one, and later to 
 * the "grown" ones.
 * 
 * @author suseika
 *
 */
public class GrowingRectangleSystem extends RectangleSystem {

	public GrowingRectangleSystem(int borderWidth) {
		super(borderWidth);
	}

	public GrowingRectangleSystem(int borderWidth, Rectangle r) {
		this(borderWidth);
		addRectangleArea(new RectangleArea(r));
	}

	/**
	 * From certain side of a RectangleArea existing in this system, create
	 * another rectangle of given size. Then shift the created rectangle to a
	 * certain amount of cells.
	 * 
	 * @param side
	 *            Only cardinal sides.
	 * @param width
	 *            Width of created rectangle.
	 * @param height
	 *            Height of created rectangle.
	 * @param offset
	 *            If side is N or S, positive offset moves the rectangle to the
	 *            east, and negative — to the west. If side is W or E, positive
	 *            offset moves. the rectangle to the south, and negative — to
	 *            the north.
	 */
	public void grow(Rectangle r, CardinalDirection side, int width, int height, int offset) {
		addRectangleArea(create(r, side, width, height, offset));
	}
	protected RectangleArea create(Rectangle r, CardinalDirection side, int width, int height, int offset) {
		int startX = 0;
		int startY = 0;
		switch (side) {
		case N:
			startX = r.x + offset;
			startY = r.y - borderWidth - height;
			break;
		case E:
			startX = r.x + r.width + borderWidth;
			startY = r.y + offset;
			break;
		case S:
			startX = r.x + offset;
			startY = r.y + r.height + borderWidth;
			break;
		case W:
		default:
			startX = r.x - borderWidth - width;
			startY = r.y + offset;
			break;
		}
		return new RectangleArea(startX, startY, width, height);
	}
}
