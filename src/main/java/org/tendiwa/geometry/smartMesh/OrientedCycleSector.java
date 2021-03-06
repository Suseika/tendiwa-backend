package org.tendiwa.geometry.smartMesh;

import org.tendiwa.geometry.Point2D;
import org.tendiwa.geometry.Sector;
import org.tendiwa.geometry.Segment2D;
import org.tendiwa.geometry.Vector2D;

import java.util.Iterator;
import java.util.Set;

final class OrientedCycleSector implements Sector {
	private final Vector2D cw;
	private final Vector2D ccw;

	OrientedCycleSector(
		MeshedNetworkCycle cycle,
		Point2D source,
		boolean inner
	) {
		assert cycle.containsVertex(source);
		Set<Segment2D> edges = cycle.edgesOf(source);
		assert edges.size() == 2;
		Iterator<Segment2D> iterator = edges.iterator();
		Segment2D oneEdge = toCounterClockwise(iterator.next(), cycle);
		Segment2D anotherEdge = toCounterClockwise(iterator.next(), cycle);
		if (oneEdge.start() == source ^ !inner) {
			cw = oneEdge.asVector();
			ccw = anotherEdge.reverse().asVector();
		} else {
			assert anotherEdge.start() == source ^ !inner;
			cw = anotherEdge.asVector();
			ccw = oneEdge.reverse().asVector();
		}
	}

	private static Segment2D toCounterClockwise(Segment2D oneEdge, MeshedNetworkCycle cycle) {
		assert cycle.containsEdge(oneEdge);
		if (cycle.isClockwise(oneEdge)) {
			oneEdge = oneEdge.reverse();
		}
		return oneEdge;
	}

	@Override
	public boolean contains(Vector2D vector) {
		return vector.isBetweenVectors(cw, ccw);
	}
}