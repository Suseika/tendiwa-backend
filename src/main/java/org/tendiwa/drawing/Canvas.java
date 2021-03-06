package org.tendiwa.drawing;

import org.tendiwa.core.meta.Cell;
import org.tendiwa.geometry.*;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Canvas {
	void draw(Drawable drawable);

	int getScale();

	default <T> void drawAll(Iterable<T> what, Function<T, Drawable> toDrawable) {
		Objects.requireNonNull(what);
		for (T shape : what) {
			draw(toDrawable.apply(shape));
		}
	}

	default <T> void drawAll(Stream<T> what, Function<T, Drawable> toDrawable) {
		what.forEach(element -> toDrawable.apply(element).drawIn(this));
	}

	void fillBackground(Color backgroundColor);

	Dimension size();

	void clear();

	void drawCell(int x, int y, Color color);

	void drawRectangle(Rectangle r, Color color);

	void drawRectangle2D(Rectangle2D r, Color color);

	void drawRasterLine(Cell p1, Cell p2, Color color);

	void drawLine(double startX, double startY, double endX, double endY, Color color);

	default void drawRasterLine(Segment2D line, Color color) {
		drawRasterLine(line.start().toCell(), line.end().toCell(), color);
	}

	default void drawCell(Cell cell, Color color) {
		drawCell(cell.x(), cell.y(), color);
	}

	void drawString(String text, Point2D start, Color color);

	int textWidth(String string);

	int textLineHeight();

	void drawSegment2D(Segment2D segment, Color color);

	void drawCircle(Circle circle, Color color);
}
