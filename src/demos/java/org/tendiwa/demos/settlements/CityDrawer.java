package org.tendiwa.demos.settlements;

import org.tendiwa.drawing.DrawingAlgorithm;
import org.tendiwa.drawing.DrawingLine2D;
import org.tendiwa.drawing.TestCanvas;
import org.tendiwa.geometry.Line2D;
import org.tendiwa.settlements.City;

import java.awt.*;

import static java.awt.Color.BLACK;
import static java.awt.Color.RED;

public class CityDrawer implements DrawingAlgorithm<City> {

    @Override
    public void draw(City city, TestCanvas canvas) {
        System.out.println(city.getHighLevelRoadGraph().vertexSet().size());
        for (Line2D roadSegment : city.getLowLevelRoadGraph().edgeSet()) {
            canvas.drawLine(roadSegment, RED);
        }
        city.getCells().stream()
                .forEach(c -> c.secondaryRoadNetwork().edgeSet().stream()
                        .forEach(line -> canvas.drawLine(line.start.toCell(), line.end.toCell(), BLACK))
                );
    }
}
