package org.tendiwa.geometry.smartMesh;

import org.jgrapht.UndirectedGraph;
import org.tendiwa.geometry.Point2D;
import org.tendiwa.geometry.Segment2D;
import org.tendiwa.geometry.graphs2d.Graph2D;
import org.tendiwa.graphs.graphs2d.BasicMutableGraph2D;
import org.tendiwa.graphs.graphs2d.MutableGraph2D;

import java.util.Collection;
import java.util.Random;

/**
 * A fluent builder to create instances of {@link SmartMeshedNetwork}.
 */
public class MeshedNetworkBuilder {
	/**
	 * @see #withSamplesPerStep(int)
	 */
	public static final int DEFAULT_SAMPLES_PER_STEP = 8;
	/**
	 * @see #withRoadsFromPoint(int)
	 */
	public static final int DEFAULT_ROADS_FROM_POINT = 4;
	/**
	 * @see #withRoadSegmentLength(double)
	 */
	public static final double DEFAULT_ROAD_SEGMENT_LENGTH = 10.;
	/**
	 * @see #withSnapSize(double)
	 */
	public static final double DEFAULT_SNAP_SIZE = 4.;
	/**
	 * @see #withDeviationAngle(double)
	 */
	public static final double DEFAULT_DEVIATION_ANGLE = Math.toRadians(20);
	/**
	 * @see #withMaxStartPointsPerCycle(int)
	 */
	public static final int DEFAULT_MAX_START_POINTS_PER_CYCLE = 2;
	/**
	 * @see #withSecondaryRoadNetworkDeviationAngle(double)
	 */
	public static final double DEFAULT_SECONDARY_ROAD_NETWORK_DEVIATION_ANGLE = 0.1;
	/**
	 * @see #withSampleRadius(double)
	 */
	private static final double DEFAULT_SAMPLE_RADIUS = 30.;
	/**
	 * @see #withSecondaryRoadNetworkRoadLengthDeviation(double)
	 */
	public static final double DEFAULT_SECONDARY_ROAD_NETWORK_ROAD_LENGTH_DEVIATION = 0;
	private Graph2D graph;
	private Double sampleRadius;
	private Integer samplesPerStep;
	private Double deviationAngle;
	private Integer roadsFromPoint;
	private Double roadSegmentLength;
	private Double snapSize;
	private Integer maxNumOfStartPoints;
	private Double secondaryRoadNetworkDeviationAngle;
	private Double secondaryRoadNetworkRoadLengthDeviation;
	private Random seededRandom;
	private boolean favourAxisAlignedSegments;

	/**
	 * Starts constructing a City defined by a {@code graph}.
	 *
	 * @param graph
	 * 	The high level road graph of a city.
	 * @see [Kelly 4.2.0]
	 */
	public MeshedNetworkBuilder(Graph2D graph) {
		this.graph = graph;
	}

	public MeshedNetworkBuilder withMaxStartPointsPerCycle(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("NumOfStartPoints must be at least 0");
		}
		this.maxNumOfStartPoints = amount;
		return this;
	}

	public MeshedNetworkBuilder withSampleRadius(double sampleRadius) {
		if (sampleRadius <= 0) {
			throw new IllegalArgumentException("Sample radius must be > 0");
		}
		this.sampleRadius = sampleRadius;
		return this;
	}

	public MeshedNetworkBuilder withRoadSegmentLength(double length) {
		if (length <= 0) {
			throw new IllegalArgumentException(
				"segmentLength must be > 0 (" + length + " provided)"
			);
		}
		this.roadSegmentLength = length;
		return this;
	}

	public MeshedNetworkBuilder withRoadSegmentLength(double min, double max) {
		if (min > max) {
			throw new IllegalArgumentException("min must be <= max");
		}
		this.roadSegmentLength = min / 2 + max / 2;
		this.secondaryRoadNetworkRoadLengthDeviation = (max - min) / 2;
		return this;
	}

	public MeshedNetworkBuilder withSamplesPerStep(int samplesPerStep) {
		if (samplesPerStep <= 0) {
			throw new IllegalArgumentException("Samples per step must be > 0");
		}
		this.samplesPerStep = samplesPerStep;
		return this;
	}

	public MeshedNetworkBuilder withDeviationAngle(double dAngle) {
		if (dAngle < 0 || dAngle >= Math.PI / 4) {
			throw new IllegalArgumentException(
				"Deviation angle must be >= 0 and < Math.PI/4 (" + dAngle + " provided)"
			);
		}
		this.deviationAngle = dAngle;
		return this;
	}

	/**
	 * Sets secondary road network deviation angle. Fluent interface method.
	 * <p>
	 * When {@link #withAxisAlignedSegments(boolean)} is set to true, this parameter defines how quickly a segment
	 * chain will take an axis aligned direction, that is, how much segments will be likely to be axis-aligned. The
	 * greater this parameter, the more axis-aligned segments will be.
	 *
	 * @param dAngle
	 * 	Angle in radians.
	 * @return The same builder.
	 */
	public MeshedNetworkBuilder withSecondaryRoadNetworkDeviationAngle(double dAngle) {
		if (Math.abs(secondaryRoadNetworkDeviationAngle) >= Math.PI * 2) {
			throw new IllegalArgumentException(
				"secondaryRoadNetworkDeviationAngle must be in [0; Math.PI*2)"
			);
		}
		this.secondaryRoadNetworkDeviationAngle = dAngle;
		return this;
	}

	public MeshedNetworkBuilder withSecondaryRoadNetworkRoadLengthDeviation(double dLength) {
		if (Math.abs(dLength) >= roadSegmentLength) {
			throw new IllegalArgumentException(
				"innerNetworkSegmentLengthDeviation can't be greater than " +
					"segmentLength (the former is " + secondaryRoadNetworkDeviationAngle + ", " +
					"the latter is " + roadSegmentLength + ")"
			);
		}
		this.secondaryRoadNetworkRoadLengthDeviation = dLength;
		return this;
	}

	public MeshedNetworkBuilder withRoadsFromPoint(int amount) {
		if (amount < 2) {
			throw new IllegalArgumentException("roadsFromPoint must be >= 2");
		}
		this.roadsFromPoint = amount;
		return this;
	}

	public MeshedNetworkBuilder withSnapSize(double snapSize) {
		if (snapSize < 0) {
			throw new IllegalArgumentException("snapSize must be >= 0");
		}
		this.snapSize = snapSize;
		return this;
	}

	public MeshedNetworkBuilder withAxisAlignedSegments(boolean favourAxisAligned) {
		this.favourAxisAlignedSegments = favourAxisAligned;
		return this;
	}


	/**
	 * Fills builder config with default parameters from this class's DEFAULT_* fields.
	 *
	 * @return this
	 */
	public MeshedNetworkBuilder withDefaults() {
		sampleRadius = DEFAULT_SAMPLE_RADIUS;
		samplesPerStep = DEFAULT_SAMPLES_PER_STEP;
		roadsFromPoint = DEFAULT_ROADS_FROM_POINT;
		roadSegmentLength = DEFAULT_ROAD_SEGMENT_LENGTH;
		snapSize = DEFAULT_SNAP_SIZE;
		deviationAngle = DEFAULT_DEVIATION_ANGLE;
		maxNumOfStartPoints = DEFAULT_MAX_START_POINTS_PER_CYCLE;
		secondaryRoadNetworkDeviationAngle = DEFAULT_SECONDARY_ROAD_NETWORK_DEVIATION_ANGLE;
		secondaryRoadNetworkRoadLengthDeviation = DEFAULT_SECONDARY_ROAD_NETWORK_ROAD_LENGTH_DEVIATION;
		seededRandom = new Random(0);
		return this;
	}

	public SmartMeshedNetwork build() {
		if (graph == null) {
			throw new IllegalStateException("Graph not set");
		}
		if (sampleRadius == null) {
			throw new IllegalStateException("sampleRadius not set");
		}
		if (samplesPerStep == null) {
			throw new IllegalStateException("samplesPerStep not set");
		}
		if (deviationAngle == null) {
			throw new IllegalStateException("deviationAngle not set");
		}
		if (roadsFromPoint == null) {
			throw new IllegalStateException("roadsFromPoint not set");
		}
		if (roadSegmentLength == null) {
			throw new IllegalStateException("segmentLength not set");
		}
		if (snapSize == null) {
			throw new IllegalStateException("snapSize not set");
		}
		if (secondaryRoadNetworkDeviationAngle == null) {
			throw new IllegalStateException("secondaryRoadNetworkDeviationAngle not set");
		}
		if (maxNumOfStartPoints == null) {
			throw new IllegalStateException("maxNumOfStartPoints not set");
		}
		if (secondaryRoadNetworkRoadLengthDeviation == null) {
			throw new IllegalStateException("innerNetworkSegmentLengthDeviation not set");
		}
		if (seededRandom == null) {
			throw new IllegalStateException("seed not set");
		}
		NetworkGenerationParameters parameters = new NetworkGenerationParameters(
			roadsFromPoint,
			roadSegmentLength,
			snapSize,
			maxNumOfStartPoints,
			secondaryRoadNetworkDeviationAngle,
			secondaryRoadNetworkRoadLengthDeviation,
			favourAxisAlignedSegments
		);
		return new SmartMeshedNetwork(
			createRoadGraph(graph.vertexSet(), graph.edgeSet()),
			parameters,
			seededRandom
		);
	}


	private static Graph2D createRoadGraph(
		Collection<Point2D> vertices,
		Collection<Segment2D> edges
	) {
		MutableGraph2D answer = new BasicMutableGraph2D();
		vertices.forEach(answer::addVertex);
		edges.forEach(answer::addSegmentAsEdge);
		return answer;
	}

	public MeshedNetworkBuilder withSeed(int i) {
		seededRandom = new Random(i);
		return this;
	}
}
