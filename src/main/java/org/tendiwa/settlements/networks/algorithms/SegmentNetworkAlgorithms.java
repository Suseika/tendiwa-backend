package org.tendiwa.settlements.networks.algorithms;

import com.google.common.collect.ImmutableSet;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.UndirectedSubgraph;
import org.tendiwa.geometry.Point2D;
import org.tendiwa.geometry.Segment2D;
import org.tendiwa.geometry.extensions.PlanarGraphs;
import org.tendiwa.settlements.networks.NetworkWithinCycle;
import org.tendiwa.settlements.networks.Segment2DSmartMesh;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SegmentNetworkAlgorithms {
	/**
	 * Returns a graph of roads that form actual cycles. Actual cycles are made of edges and vertices of original
	 * cycles and also split edges remembered in {@link org.tendiwa.settlements.networks.NetworksProducer#holderOfSplitCycleEdges}.
	 * Actual cycles don't include cycles that are formed with any of the secondary road network's edges.
	 *
	 * @return Actual road cycles graph.
	 * @see org.tendiwa.settlements.networks.Segment2DSmartMesh#originalGraph for original cycles.
	 */
	public static UndirectedGraph<Point2D, Segment2D> createFullCycleGraph(Segment2DSmartMesh model) {
		UndirectedGraph<Point2D, Segment2D> answer = PlanarGraphs.createGraph();
		model.getNetworks().forEach(network -> Graphs.addGraph(answer, network.cycle()));
		return answer;
	}

	/**
	 * @return A graph of all resulting roads in this city.
	 */
	public static UndirectedGraph<Point2D, Segment2D> createFullGraph(Segment2DSmartMesh model) {
		UndirectedGraph<Point2D, Segment2D> answer = PlanarGraphs.createGraph();
		model.getNetworks().forEach(network -> {
			Graphs.addGraph(answer, network.cycle());
			Graphs.addGraph(answer, network.network());
		});
		return answer;
	}

	/**
	 * In each {@link NetworkWithinCycle} that this {@link
	 * org.tendiwa.settlements.networks.Segment2DSmartMesh} consists of, finds such edges that are part of only one
	 * {@link NetworkWithinCycle#cycle()}.
	 *
	 * @return Map from {@link NetworkWithinCycle} to subgraphs of {@link
	 * NetworkWithinCycle#network()} described in this method's description.
	 */
	public static Map<NetworkWithinCycle, UndirectedGraph<Point2D, Segment2D>> outerCycleEdges(
		Segment2DSmartMesh model
	) {
		Map<NetworkWithinCycle, UndirectedGraph<Point2D, Segment2D>> answer = new LinkedHashMap<>();
		TObjectIntMap<Segment2D> usedEdges = new TObjectIntHashMap<>();
		for (NetworkWithinCycle network : model.getNetworks()) {
			UndirectedGraph<Point2D, Segment2D> subgraph = new UndirectedSubgraph<>(
				network.cycle(),
				network.cycle().vertexSet(),
				network.cycle().edgeSet()
			);
			answer.put(network, subgraph);
			for (Segment2D edge : network.cycle().edgeSet()) {
				if (usedEdges.containsKey(edge)) {
					usedEdges.increment(edge);
				} else {
					usedEdges.put(edge, 1);
				}
			}
		}
		assert model.getNetworks().stream()
			.flatMap(network -> network.cycle().edgeSet().stream())
			.allMatch(usedEdges::containsKey);
		for (UndirectedGraph<Point2D, Segment2D> subgraph : answer.values()) {
			Set<Segment2D> edgesOfSubgraph = ImmutableSet.copyOf(subgraph.edgeSet());
			for (Segment2D edge : edgesOfSubgraph) {
				int timesEdgeUsed = usedEdges.get(edge);
				if (timesEdgeUsed > 1) {
					PlanarGraphs.removeEdgeAndOrphanedVertices(subgraph, edge);
				} else {
					assert timesEdgeUsed == 1 : timesEdgeUsed;
				}
			}
		}
		assert answer.values()
			.stream()
			.flatMap(graph -> graph.edgeSet().stream())
			.allMatch(edge -> usedEdges.get(edge) == 1);
		return answer;
	}
}
