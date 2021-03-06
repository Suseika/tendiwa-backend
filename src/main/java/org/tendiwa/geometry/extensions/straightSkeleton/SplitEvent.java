package org.tendiwa.geometry.extensions.straightSkeleton;

import org.tendiwa.geometry.Point2D;
import org.tendiwa.geometry.RayIntersection;

/**
 * Note: this class has natural ordering that is inconsistent with {@link Object#equals(Object)}.
 */
final class SplitEvent extends SkeletonEvent {
	private final OriginalEdgeStart oppositeEdgeStart;
	private final Node parent;

	SplitEvent(
		Point2D point,
		Node parent,
		OriginalEdgeStart oppositeEdgeStart
	) {
		super(point, parent);
		this.parent = parent;
		this.oppositeEdgeStart = oppositeEdgeStart;
	}

	Node parent() {
		return parent;
	}

	@Override
	void handle(SuseikaStraightSkeleton skeleton) {
		if (parent().isProcessed()) {
			return;
		}
		if (parent().previous().previous().previous() == parent()) {
			// Non-convex 2c
//				connectLast3SegmentsOfLav(point);
			assert false;
		} else if (parent().isInLavOf2Nodes()) {
			eliminate2NodeLav(parent(), skeleton);
		} else if (oppositeEdgeStart.face().isClosed()) {
			replaceWithEventOverClosedFace(skeleton);
		} else {
			splitEdge(skeleton);
		}
	}

	private boolean wedgesIntoOppositeFace(LeftSplitNode left, RightSplitNode right) {
		boolean leftBisectorFromLeft =
			new RayIntersection(
				left.bisector,
				oppositeEdgeStart.face().getNodeFromLeft(left).bisector
			).r > 0;
		boolean rightBisectorFromRight =
			new RayIntersection(
				right.bisector,
				oppositeEdgeStart.face().getNodeFromRight(right).bisector
			).r > 0;
		return rightBisectorFromRight && leftBisectorFromLeft;
	}

	private void replaceWithEventOverClosedFace(SuseikaStraightSkeleton skeleton) {
		skeleton.queueEvent(
			new SplitEvent(
				point,
				parent,
				oppositeEdgeStart.findAnotherOppositeEdgeStart(parent)
			)
		);
	}

	private void splitEdge(SuseikaStraightSkeleton skeleton) {
		// Non-convex 2e

		// Split event produces two nodes at the same point, and those two nodes have distinct LAVs.
		LeftSplitNode leftNode = new LeftSplitNode(
			point,
			parent().previousEdgeStart,
			oppositeEdgeStart
		);
		RightSplitNode rightNode = new RightSplitNode(
			point,
			oppositeEdgeStart,
			parent().currentEdgeStart
		);
		leftNode.setPair(rightNode);
		rightNode.setPair(leftNode);
		leftNode.computeReflexAndBisector();
		rightNode.computeReflexAndBisector();
		if (!wedgesIntoOppositeFace(leftNode, rightNode)) {
			return;
		}
		// Non-convex 2d
		skeleton.outputArc(parent().vertex, point);
		skeleton.debug.drawSplitEventArc(this);

		oppositeEdgeStart.integrateSplitNodes(parent(), leftNode, rightNode);

		// Non-convex 2
		integrateNewSplitNode(leftNode, skeleton);
		integrateNewSplitNode(rightNode, skeleton);
	}

	private void integrateNewSplitNode(Node node, SuseikaStraightSkeleton skeleton) {
		if (node.isInLavOf2Nodes()) {
			// Such lavs can form after a split event
			eliminate2NodeLav(node, skeleton);
		} else {
//			node.computeReflexAndBisector();
			skeleton.queueEventFromNode(node);
		}
	}
}
