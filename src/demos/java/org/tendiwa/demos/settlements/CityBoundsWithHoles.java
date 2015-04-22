package org.tendiwa.demos.settlements;

import org.tendiwa.demos.Demos;
import org.tendiwa.demos.DrawableRectangle;
import org.tendiwa.drawing.TestCanvas;
import org.tendiwa.drawing.extensions.DrawingCellSet;
import org.tendiwa.drawing.extensions.DrawingGraph;
import org.tendiwa.drawing.extensions.DrawingRectangle;
import org.tendiwa.geometry.BoundedCellSet;
import org.tendiwa.geometry.BasicCell;
import org.tendiwa.geometry.CellSet;
import org.tendiwa.geometry.Rectangle;
import org.tendiwa.geometry.graphs2d.Cycle2D;
import org.tendiwa.noise.Noise;
import org.tendiwa.pathfinding.dijkstra.PathTable;
import org.tendiwa.settlements.cityBounds.CityBounds;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

class CityBoundsWithHoles implements Runnable {
	Rectangle worldRec = new Rectangle(0, 0, 400, 400);
	TestCanvas canvas = new TestCanvas(1, worldRec.width, worldRec.height);
	//	GifBuilder gifBuilder = new GifBuilder(canvas, 6, Logger.getLogger("cityBoundsWithHoles"));
	int iterations = 55;
	int cityRadius = 100;
	BasicCell startCell = new BasicCell(200, 200);


	public static void main(String[] args) {
		Demos.run(CityBoundsWithHoles.class);
	}


	@Override
	public void run() {
		TestCanvas.canvas = canvas;
		AtomicInteger counter = new AtomicInteger(146);
		CellSet water = (x, y) -> Noise.noise(
			((double) x + 700 + 0) / 50,
			((double) y + 200 + 0) / 50,
			5
		) < counter.get();

		for (int i = 0; i < iterations; i++) {
			System.out.println(i);
			Cycle2D cityBounds = buildCityBoundsGraph(water);

			canvas.draw(
				new DrawableRectangle(
					worldRec,
					Color.green
				)
			);
			canvas.draw(
				new DrawableCellSet(
					water,
					Color.blue
				)
			);
			canvas.draw(
				cityBounds.graph(),
				DrawingGraph.withColorAndVertexSize(Color.red, 0)
			);

//			gifBuilder.saveFrame();
			canvas.clear();
			counter.decrementAndGet();
		}
//		gifBuilder.saveAnimation(System.getProperty("user.home") + "/cityBoundsWithHolesAnimation.gif");
	}

	private Cycle2D buildCityBoundsGraph(CellSet water) {
		BoundedCellSet cityShape = new PathTable(
			startCell,
			(x, y) -> !water.contains(x, y),
			cityRadius
		).computeFull();
		return CityBounds.create(
			cityShape,
			startCell,
			cityRadius
		);
	}
}
