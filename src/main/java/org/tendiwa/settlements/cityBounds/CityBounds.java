package org.tendiwa.settlements.cityBounds;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import org.tendiwa.core.Direction;
import org.tendiwa.core.Directions;
import org.tendiwa.core.meta.Cell;
import org.tendiwa.geometry.*;
import org.tendiwa.geometry.extensions.CachedCellSet;
import org.tendiwa.geometry.extensions.ChebyshovDistanceBufferBorder;
import org.tendiwa.geometry.extensions.PlanarGraphs;
import org.tendiwa.geometry.graphs2d.Graph2D;
import org.tendiwa.geometry.smartMesh.MeshedNetwork;
import org.tendiwa.geometry.smartMesh.MeshedNetworkBuilder;
import org.tendiwa.graphs.algorithms.SameOrPerpendicularSlopeGraphEdgesPerturbations;
import org.tendiwa.graphs.graphs2d.BasicMutableGraph2D;
import org.tendiwa.graphs.graphs2d.MutableGraph2D;
import org.tendiwa.pathfinding.dijkstra.PathTable;
import org.tendiwa.terrain.WorldGenerationException;

import java.util.HashSet;
import java.util.Set;

/**
 * From {@link CellSet}, creates a graph used as a base for a {@link MeshedNetwork}.
 */
public final class CityBounds {

	private CityBounds() {
	}

	/**
	 * Creates a new graph that can be used as a base for {@link MeshedNetwork}.
	 *
	 * @param startCell
	 * 	A cell from which a City originates. Roughly denotes its final position.
	 * @param maxCityRadius
	 * 	A maximum radius of a Rectangle containing resulting City.
	 * @return A new graph that can be used as a base for {@link MeshedNetwork}.
	 * @see MeshedNetworkBuilder
	 */
	public static Graph2D create(
		BoundedCellSet cityShape,
		Cell startCell,
		int maxCityRadius
	) {
		if (!isCellDeepEnoughInsideShape(startCell, cityShape)) {
			throw new IllegalArgumentException(
				"Start cell " + startCell + " must be within cityShape " +
					"and at least 1 cell away from cityShape's border"
			);
		}
		Graph2D answer = computeCityBoundingRoads(cityShape, startCell, maxCityRadius);
		assert !minimalCyclesOfGraphHaveCommonVertices(answer);
		return answer;
	}

	/**
	 * Checks that eight cells around {@code cell} and {@code cell} itseft are all contained in
	 * {@code shape}.
	 *
	 * @param cell
	 * @param shape
	 * @return
	 */
	private static boolean isCellDeepEnoughInsideShape(Cell cell, CellSet shape) {
		return shape.contains(cell)
			&& shape.contains(cell.x(), cell.y() - 1)
			&& shape.contains(cell.x() + 1, cell.y() - 1)
			&& shape.contains(cell.x() + 1, cell.y())
			&& shape.contains(cell.x() + 1, cell.y() + 1)
			&& shape.contains(cell.x(), cell.y() + 1)
			&& shape.contains(cell.x() - 1, cell.y() + 1)
			&& shape.contains(cell.x() - 1, cell.y())
			&& shape.contains(cell.x() - 1, cell.y() - 1);
	}

	/**
	 * Checks if there in any vertex in graph that is present in more than one of graph's minimal cycles.
	 *
	 * @param graph
	 * 	A graph.
	 * @return true if there is such vertex, false otherwise.
	 */
	private static boolean minimalCyclesOfGraphHaveCommonVertices(Graph2D graph) {
		Set<Polygon> minimalCycles =
			PlanarGraphs.minimumCycleBasis(graph).minimalCyclesSet();
		Set<Point2D> usedVertices = new HashSet<>();
		for (Polygon cycle : minimalCycles) {
			for (Point2D vertex : cycle) {
				boolean added = usedVertices.add(vertex);
				if (!added) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Flood-fills all the area inside {@code bufferBorder}, but not the cells of {@code bufferBorder} itself.
	 *
	 * @param bufferBorder
	 * 	Coastal road's cells.
	 * @param start
	 * 	The point from which the City originated.
	 * @return A path table whose bounds end on bufferBorder's cells, but not past them.
	 */
	private static PathTable areaBoundedByBufferBorderExclusive(
		CellSet bufferBorder,
		Cell start,
		Rectangle boundingRec,
		int radius
	) {
		return new PathTable(
			start,
			(x, y) -> boundingRec.contains(x, y) && !bufferBorder.contains(x, y),
			radius
		).computeFull();
	}

	private static Graph2D computeCityBoundingRoads(
		BoundedCellSet cityShape,
		Cell startCell,
		int radius
	) {
		CachedCellSet bufferBorder = new CachedCellSet(
			new ChebyshovDistanceBufferBorder(
				1,
				(x, y) -> !cityShape.getBounds().contains(x, y) || !cityShape.contains(x, y)
			),
			cityShape.getBounds()
		).computeAll();
		if (bufferBorder.contains(startCell)) {
			throw new WorldGenerationException(
				"Starting cell for computing bounds of a city resides right on the city bound."
			);
		}
		PathTable cellsInsideBufferBorder = areaBoundedByBufferBorderExclusive(
			bufferBorder,
			startCell,
			cityShape.getBounds(),
			radius + 1
		);
		CellSet cellsRejectedBecauseOfKnots = new KnotResolvingCells(bufferBorder, cellsInsideBufferBorder);
		CachedCellSet culledBufferBorder = new CachedCellSet(
			new ChebyshovDistanceBufferBorder(
				1,
				(x, y) -> cellsInsideBufferBorder.isCellComputed(x, y) && !cellsRejectedBecauseOfKnots.contains(x, y)
			),
			cellsInsideBufferBorder.getBounds()
		).computeAll();
		return bufferBorderToGraph(culledBufferBorder, cellsInsideBufferBorder);
	}


	/**
	 * Transforms a 1 cell wide border to a graph.
	 *
	 * @param bufferBorder
	 * 	One cell wide border, with cells being neighbors with each other from cardinal sides.
	 * @param cellsInsideBufferBorder
	 * 	Cells inside {@code bufferBorder}. Doesn't include any cells of {@code bufferBorder}.
	 * @return A graph where vertices are all the cells of the one cell wide border,
	 * and edges are two cells being near each other from cardinal sides.
	 */
	private static Graph2D bufferBorderToGraph(
		FiniteCellSet bufferBorder,
		CellSet cellsInsideBufferBorder
	) {
		MutableGraph2D graph = new BasicMutableGraph2D();

		ImmutableSet<Cell> borderCells = bufferBorder.toSet();
		BiMap<Cell, Point2D> cell2PointMap = HashBiMap.create();
		for (Cell cell : borderCells) {
			cell2PointMap.put(cell, cell.toPoint());
		}
		for (Cell cell : borderCells) {
			graph.addVertex(cell2PointMap.get(cell));
		}
		for (Cell cell : borderCells) {
			for (Direction dir : Directions.CARDINAL_DIRECTIONS) {
				Point2D neighbour = cell2PointMap.get(cell.moveToSide(dir));
				if (graph.containsVertex(neighbour)) {
					graph.addEdge(cell2PointMap.get(cell), neighbour);
				}
			}
		}
		EdgeReducer.reduceEdges(graph, cell2PointMap);
		LadderyEdgesOptimizer.optimize(graph, cellsInsideBufferBorder.or(bufferBorder), 2);
		SameOrPerpendicularSlopeGraphEdgesPerturbations.perturb(graph, 1e-4);
		return graph;
	}

}
