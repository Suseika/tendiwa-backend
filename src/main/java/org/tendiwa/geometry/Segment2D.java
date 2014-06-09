package org.tendiwa.geometry;

import com.google.common.collect.Lists;
import org.tendiwa.settlements.LineIntersection;

import java.util.List;
import java.util.function.Function;

/**
 * An immutable line
 */
public class Segment2D {
	public final Point2D start;
	public final Point2D end;


	public Segment2D(Point2D start, Point2D end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Creates a reverse segment.
	 *
	 * @return A new segment starting at {@code #end} and ending at {@code #start}.
	 */
	public Segment2D reverse() {
		return new Segment2D(end, start);
	}

	/**
	 * A convenience factory method to create Segment2D from 4 numbers.
	 *
	 * @param x1
	 * 	X coordinate of start point.
	 * @param y1
	 * 	Y coordinate of start point.
	 * @param x2
	 * 	X coordinate of end point.
	 * @param y2
	 * 	Y coordinate of end point.
	 * @return A new Segment2D
	 */
	public static Segment2D create(double x1, double y1, double x2, double y2) {
		return new Segment2D(new Point2D(x1, y1), new Point2D(x2, y2));
	}

	/**
	 * Distance by x-axis from {@link #start} to {@link #end}.
	 *
	 * @return {@code end.x-start.x}.
	 */
	public double dx() {
		return end.x - start.x;
	}

	/**
	 * Distance by y-axis from {@link #start} to {@link #end}.
	 *
	 * @return {@code end.y-start.y}.
	 */
	public double dy() {
		return end.y - start.y;
	}

	@Override
	public String toString() {
		return
			start +
			"," + end;
	}

	/**
	 * Length of this line segment.
	 *
	 * @return Distance from {@link #start} to {@link #end}.
	 */
	public double length() {
		return start.distanceTo(end);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Segment2D segment2D = (Segment2D) o;

		if (end != null ? !end.equals(segment2D.end) : segment2D.end != null) return false;
		if (start != null ? !start.equals(segment2D.start) : segment2D.start != null) return false;

		return true;
	}

	/**
	 * Finds a point of intersection between this line and another line.
	 * <p>
	 * An intersection at ends of lines doesn't count for an intersection.
	 *
	 * @param line
	 * 	Another line.
	 * @return A Point2D where these two lines intersect, or null if lines don't intersect.
	 * @see #intersects(Segment2D)
	 */
	public Point2D intersection(Segment2D line) {
		LineIntersection lineIntersection = new LineIntersection(start, end, line);
		if (!lineIntersection.segmentsIntersect()) {
			return null;
		}
		return lineIntersection.getIntersectionPoint();
	}

	/**
	 * Checks if this line intersects another line. This is less expensive than finding the intersection point with
	 * {@link #intersection(Segment2D)}.
	 * <p>
	 * An intersection at ends of lines doesn't count for an intersection.
	 *
	 * @param line
	 * 	Another line.
	 * @return true if lines intersect, false otherwise.
	 * @see #intersection(Segment2D)
	 */
	public boolean intersects(Segment2D line) {
		LineIntersection intersection = new LineIntersection(start, end, line);
		return intersection.segmentsIntersect();
	}

	@Override
	public int hashCode() {
		int result = start != null ? start.hashCode() : 0;
		result = 31 * result + (end != null ? end.hashCode() : 0);
		return result;
	}

	public static Function<Segment2D, List<Cell>> toCellList() {
		return e -> Lists.newArrayList(
			CellSegment.vector(e.start.toCell(), e.end.toCell())
		);
	}

	public boolean isParallel(Segment2D segment) {
		return Math.abs((segment.end.y - segment.start.y) / (segment.end.x - segment.start.x))
			== Math.abs((end.y - start.y) / (end.x - start.x));
	}
}