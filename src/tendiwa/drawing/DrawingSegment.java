package tendiwa.drawing;

import java.awt.Color;
import java.awt.Point;

import tendiwa.geometry.Segment;

public class DrawingSegment {
	private DrawingSegment() {
	}
	public static DrawingAlgorithm<Segment> withColor(final Color color) {
		return new DrawingAlgorithm<Segment>() {
			
			@Override
			public void draw(Segment segment) {
				for (Point point : segment) {
					drawPoint(point.x, point.y, color);
				}
				
			}
		};
	}

}