package org.tendiwa.settlements.networks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.UndirectedGraph;
import org.tendiwa.geometry.GeometryException;
import org.tendiwa.geometry.Point2D;
import org.tendiwa.geometry.Segment2D;
import org.tendiwa.geometry.extensions.Point2DVertexPositionAdapter;
import org.tendiwa.geometry.extensions.PolygonSegments;
import org.tendiwa.geometry.extensions.ShamosHoeyAlgorithm;
import org.tendiwa.geometry.extensions.straightSkeleton.SuseikaStraightSkeleton;
import org.tendiwa.geometry.extensions.twakStraightSkeleton.TwakStraightSkeleton;
import org.tendiwa.graphs.MinimumCycleBasis;
import org.tendiwa.settlements.BlockRegion;
import org.tendiwa.settlements.EnclosedBlock;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SecondaryRoadNetworkBlock extends EnclosedBlock {
	private final List<Point2D> outline;

	SecondaryRoadNetworkBlock(List<Point2D> outline) {
		super(outline);
		assert outline.size() > 2;
		assert !ShamosHoeyAlgorithm.areIntersected(PolygonSegments.toSegments(outline));
		this.outline = ImmutableList.copyOf(outline);
	}

	public Set<BlockRegion> shrinkToRegions(double depth, int seed) {
		UndirectedGraph<Point2D, Segment2D> cap;
		try {
			cap = new SuseikaStraightSkeleton(outline).cap(depth);
		} catch (GeometryException e) {
			return ImmutableSet.of();
		}
		MinimumCycleBasis<Point2D, Segment2D> basis = new MinimumCycleBasis<>(
			cap,
			Point2DVertexPositionAdapter.get()
		);
		Set<BlockRegion> blocks = basis
			.minimalCyclesSet()
			.stream()
			.map(cycle -> new BlockRegion(cycle.vertexList(), seed))
			.collect(Collectors.toSet());
//		assert blocks.size() > 0;
		return blocks;
	}


}
