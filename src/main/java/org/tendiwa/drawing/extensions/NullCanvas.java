package org.tendiwa.drawing.extensions;

import org.tendiwa.drawing.DrawableInto;
import org.tendiwa.drawing.DrawingAlgorithm;
import org.tendiwa.drawing.TestCanvas;
import org.tendiwa.geometry.BasicCell;
import org.tendiwa.geometry.BasicRectangle2D;
import org.tendiwa.geometry.Rectangle;

import java.awt.Color;
import java.awt.Shape;

public class NullCanvas implements DrawableInto {
	@Override
	public <T> void draw(T what, DrawingAlgorithm<? super T> how, TestCanvas.Layer where) {

	}

	@Override
	public <T> void draw(T what, DrawingAlgorithm<? super T> how) {

	}

	@Override
	public <T> void drawAll(Iterable<T> what, DrawingAlgorithm<? super T> how) {

	}

	@Override
	public void fillBackground(Color backgroundColor) {

	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getScale() {
		return 0;
	}

	@Override
	public void drawCell(int x, int y, Color color) {

	}

	@Override
	public void drawRectangle(Rectangle r, Color color) {

	}

	@Override
	public void drawRectangle2D(BasicRectangle2D r, Color color) {

	}

	@Override
	public void drawRasterLine(BasicCell p1, BasicCell p2, Color color) {

	}

	@Override
	public void drawLine(double startX, double startY, double endX, double endY, Color color) {

	}

	@Override
	public void fillShape(Shape shape, Color color) {

	}

	@Override
	public void drawShape(Shape shape, Color color) {

	}

	@Override
	public void drawString(String text, double x, double y, Color color) {

	}

	@Override
	public int textWidth(String string) {
		return 0;
	}

	@Override
	public int textLineHeight() {
		return 0;
	}

}
