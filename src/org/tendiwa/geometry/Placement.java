package org.tendiwa.geometry;

public interface Placement {
    EnhancedRectangle placeIn(Placeable placeable, RectangleSystemBuilder builder);
}