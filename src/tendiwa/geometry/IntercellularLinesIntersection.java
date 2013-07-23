package tendiwa.geometry;

import java.awt.Point;

public class IntercellularLinesIntersection extends Intersection {
	final int x;
	final int y;
	IntercellularLinesIntersection(IntercellularLine line1, IntercellularLine line2) {
		IntercellularLine verticalLine, horizontalLine;
		if (line1.orientation.isVertical()) {
			verticalLine = line1;
			horizontalLine = line2;
		} else {
			verticalLine = line2;
			horizontalLine = line1;
		}

		x = verticalLine.getStaticCoordFromSide(CardinalDirection.E);
		y = horizontalLine.getStaticCoordFromSide(CardinalDirection.N);
	}
	public Point getCornerPointOfQuarter(OrdinalDirection side) {
		switch (side) {
			case NE:
				return new Point(x, y);
			case SE:
				return new Point(x, y+1);
			case SW:
				return new Point(x-1, y+1);
			case NW:
				return new Point(x-1, y);
			default:
				throw new IllegalArgumentException("Only ordinal sides can be used here");
					
		}
	}
}
