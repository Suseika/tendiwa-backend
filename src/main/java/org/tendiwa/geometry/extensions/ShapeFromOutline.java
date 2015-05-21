package org.tendiwa.geometry.extensions;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.tendiwa.geometry.*;
import org.tendiwa.geometry.extensions.polygonRasterization.PolygonRasterizer;
import org.tendiwa.graphs.GraphChainTraversal;
import org.tendiwa.graphs.MinimalCycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShapeFromOutline {
	private ShapeFromOutline() {

	}

	public static BoundedCellSet from(UndirectedGraph<Point2D, Segment2D> outline) {
		List<Set<Point2D>> components = new ConnectivityInspector<>(outline).connectedSets();
		List<List<Point2D>> polygons = new ArrayList<>(components.size());
		for (Set<Point2D> component : components) {
			List<Point2D> polygon = new ArrayList<>(component.size());
			GraphChainTraversal
				.traverse(outline)
				.startingWith(component.iterator().next())
				.stream()
				.map(GraphChainTraversal.NeighborsTriplet::current)
				.forEach(polygon::add);
			polygons.add(polygon);
		}
		Rectangle bounds = computeBounds(outline);

		CellSet shape = polygons.stream()
			.map((p) -> (CellSet) PolygonRasterizer.rasterizeToCellSet(p))
			.reduce((a, b) -> a.xor(b))
			.get();
		return new CachedCellSet(shape, bounds);

//		FiniteCellSet edgeCells = requireNonNull(outline)
//			.edgeSet()
//			.segmentStream()
//			.map(Segment2D.toCellList())
//			.flatMap(a -> a.segmentStream())
//			.distinct()
//			.collect(CellSet.toCellSet());
//		Rectangle graphBounds = Recs.boundsOfCells(edgeCells);
//		return new CachedCellSet(
//			new MinimumCycleBasis<>(outline, Point2DVertexPositionAdapter.get())
//				.minimalCyclesSet()
//				.segmentStream()
//				.map(ShapeFromOutline::polygonCells)
//				.reduce(edgeCells, (a, b) -> a.or(b)),
//			graphBounds
//		);


	}

	private static Rectangle computeBounds(UndirectedGraph<Point2D, Segment2D> outline) {
		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;
		for (Point2D point : outline.vertexSet()) {
			if (minX > point.x()) {
				minX = point.x();
			}
			if (minY > point.y()) {
				minY = point.y();
			}
			if (maxX < point.x()) {
				maxX = point.x();
			}
			if (maxY < point.y()) {
				maxY = point.y();
			}
		}
		return new BasicRectangle(
			(int) Math.floor(minX),
			(int) Math.floor(minY),
			(int) Math.ceil(maxX - minX),
			(int) Math.ceil(maxY - minY)
		);
	}

	/**
	 * @param cycle
	 * 	A minimal cycle.
	 * @return A cell that is within a cycle.
	 */
	private static CellSet polygonCells(MinimalCycle cycle) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coordinates = cycle
			.stream()
			.map(p -> new Coordinate(p.x(), p.y()))
			.collect(Collectors.toList())
			.toArray(new Coordinate[cycle.size() + 1]);
		coordinates[coordinates.length - 1] = coordinates[0];

		Polygon polygon = gf.createPolygon(gf.createLinearRing(coordinates), null);
		return (x, y) -> polygon.contains(gf.createPoint(new Coordinate(x, y)));
	}
}
