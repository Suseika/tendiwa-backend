package org.tendiwa.core;

public class StepUnitedWithFromSideAlign implements Placement {
private final RectanglePointer pointer;
private final CardinalDirection fromSide;
private final CardinalDirection alignSide;

public StepUnitedWithFromSideAlign(RectanglePointer pointer, CardinalDirection fromSide, CardinalDirection alignSide) {
	this.pointer = pointer;
	this.fromSide = fromSide;
	this.alignSide = alignSide;
}

@Override
public EnhancedRectangle placeIn(Placeable placeable, RectangleSystemBuilder builder) {
	return shift(0).placeIn(placeable, builder);
}

public Placement shift(final int shift) {
	return new Placement() {
		@Override
		public EnhancedRectangle placeIn(Placeable placeable, RectangleSystemBuilder builder) {
			EnhancedRectangle placeableBounds = placeable.getBounds();
			EnhancedRectangle existingRec = builder.getRectangleByPointer(pointer).getBounds();
			int staticCoord = existingRec.getStaticCoordOfSide(fromSide) + fromSide.getGrowing();
			if (fromSide == Directions.N) {
				staticCoord -= placeableBounds.getHeight();
			} else if (fromSide == Directions.W) {
				staticCoord -= placeableBounds.getWidth() - 1;
			}
			int dynamicCoord = (fromSide.isVertical() ? existingRec.getX() : existingRec.getY()) + shift * alignSide.getGrowing();
			if (alignSide == Directions.E) {
				dynamicCoord += existingRec.getWidth() - placeableBounds.getWidth();
			} else if (alignSide == Directions.S) {
				dynamicCoord += existingRec.getHeight() - placeableBounds.getHeight();
			}
			int x, y;
			if (fromSide.isVertical()) {
				x = dynamicCoord;
				y = staticCoord;
			} else {
				x = staticCoord;
				y = dynamicCoord;
			}
			return placeable.place(builder, x, y);
		}
	};
}
}
