package org.tendiwa.core;

import org.tendiwa.core.meta.Utils;
import org.tendiwa.core.meta.Chance;
import org.tendiwa.core.meta.Coordinate;
import org.tendiwa.core.terrain.settlements.BuildingPlace;
import org.tendiwa.core.terrain.settlements.Service;
import org.tendiwa.core.Settlement.RoadSystem.Road;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * A special ammunitionType of {@link Location} that has advanced instruments for placing {@link Building}s.
 * @author suseika
 *
 */
public class Settlement extends Location {
	protected RoadSystem roadSystem = new RoadSystem();
	protected QuarterSystem quarterSystem;
	public HashSet<RectangleSystem> quarters = new HashSet<>();
	public ArrayList<Building> buildings = new ArrayList<>();
	public ArrayList<Service> services = new ArrayList<>();
	public Settlement(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height);
		quarterSystem = new QuarterSystem(this);
	}
	public void placeBuilding(BuildingPlace place, Class<? extends Building> cls, CardinalDirection side) {
		Building building;
		try {
			@SuppressWarnings("unchecked")
			Constructor<? extends Building> ctor = (Constructor<? extends Building>) cls.getDeclaredConstructors()[0];
			building = ctor.newInstance(this, place, side);
			if (building.fitsToPlace(place)) {
				building.draw();
				buildings.add(building);
			} else {
				throw new RuntimeException("Couldn't place building " + cls.getSimpleName());
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		// if (ammunitionType == BuildingType.TEST) {
		// buildings.add(new TestBuilding(this, place));
		// } else if (ammunitionType == BuildingType.INN) {
		// buildings.add(new Inn(this, place));
		// } else if (ammunitionType == BuildingType.ONE_ROOM_HOUSE) {
		// buildings.add(new OneRoomHouse(this, place));
		// } else if (ammunitionType == BuildingType.TEMPLE) {
		// buildings.add(new Temple(this, place));
		// }
	}
	public void createRandomRoadSystem() {
		for (int y = Chance.rand(0, 20); y < height; y += Chance.rand(20, 25)) {
			roadSystem.createRoad(Chance.rand(0, 5), y, Chance.rand(width - 5, width - 1), y);
		}
		for (int x = Chance.rand(0, 20); x < width; x += Chance.rand(20, 25)) {
			roadSystem.createRoad(x, Chance.rand(0, 5), x, Chance.rand(height - 5, height - 1));
		}
	}
	public void markQuarter(EnhancedRectangle r, int minWidth/* =8 */, int borderWidth/* =2 */) {
		// Разметить квартал - создать систему прямоугольников и занести её в
		// Settlement::quarters
		// in: индекс пярмоугольника, на котором строится квартал, в
		// Settlement::rectangles
		quarters.add(RecursivelySplitRectangleSystemFactory.create(r.getX() + 1, r.getY() + 1, r.getWidth() - 2, r.getHeight() - 2, minWidth, borderWidth));
	}
	public Service createService(Character dweller, int type, String name) {
		Service service = new Service(dweller, type, name);
		services.add(service);
		return service;
	}
	public class RoadSystem {
		private ArrayList<Road> roads = new ArrayList<Road>();
		private HashMap<Road, ArrayList<Intersection>> intersections = new HashMap<Road, ArrayList<Intersection>>();
		private HashMap<Road, ArrayList<Intersection>> branches = new HashMap<Road, ArrayList<Intersection>>();

		public RoadSystem() {

		}

		public Road createRoad(int startX, int startY, int endX, int endY) {
			if (startX > endX) {
				int buf = startX;
				startX = endX;
				endX = buf;
			}
			if (startY > endY) {
				int buf = startY;
				startY = endY;
				endY = buf;
			}
			Road newRoad = new Road(startX, startY, endX, endY);

			// If new road lies on the same cells as one of
			// existing roads, then throw an error.
			if (newRoad.orientation.isVertical()) {
				for (Road road : roads) {
					if (road.orientation.isVertical() && road.start.x == newRoad.start.x && Utils.integersRangeIntersection(road.start.y, road.end.y, newRoad.start.y, newRoad.end.y) >= 1) {
						throw new Error("Two roads on the same vertical line: " + newRoad + " and " + road);
					}
				}
			} else {
				for (Road road : roads) {
					if (road.orientation.isVertical() && road.start.y == newRoad.start.y && Utils.integersRangeIntersection(road.start.x, road.end.x, newRoad.start.x, newRoad.end.x) >= 1) {
						throw new Error("Two roads on the same horizontal line: " + newRoad + " and " + road);
					}
				}
			}
			findChanges(newRoad);
			roads.add(newRoad);
			intersections.put(newRoad, new ArrayList<Intersection>());
			branches.put(newRoad, new ArrayList<Intersection>());

			return newRoad;
		}

		public void findChanges(Road newRoad) {
			/**
			 * Find new branches and intersections after adding newRoad.
			 */
			for (Road oldRoad : roads) {
				if (!areParallel(newRoad, oldRoad)) {
					// If roads are parallel, then go to next road
					if (newRoad.orientation.isVertical()) {
						// If new road is vertical
						if ((newRoad.start.y == oldRoad.start.y || newRoad.end.y == oldRoad.start.y) && newRoad.start.x >= oldRoad.start.x && newRoad.start.x <= oldRoad.end.x) {
							branches.get(oldRoad).add(new Intersection(newRoad, new Coordinate(newRoad.start.x, oldRoad.start.y)));
						} else if (newRoad.start.x >= oldRoad.start.x && newRoad.start.x <= oldRoad.end.x && oldRoad.start.y >= oldRoad.start.y && newRoad.start.y <= oldRoad.end.y) {
							intersections.get(oldRoad).add(new Intersection(newRoad, new Coordinate(newRoad.start.x, oldRoad.start.y)));
						}
					} else {
						// If new road is horizontal
						if ((newRoad.start.x == oldRoad.start.x || newRoad.end.x == oldRoad.start.x) && newRoad.start.y >= oldRoad.start.y && newRoad.start.y <= oldRoad.end.y) {
							branches.get(oldRoad).add(new Intersection(newRoad, new Coordinate(oldRoad.start.x, newRoad.start.y)));
						} else if (newRoad.start.y >= oldRoad.start.y && newRoad.start.y <= oldRoad.end.y && oldRoad.start.x >= oldRoad.start.x && newRoad.start.x <= oldRoad.end.x) {
							intersections.get(oldRoad).add(new Intersection(newRoad, new Coordinate(oldRoad.start.x, newRoad.start.y)));
						}
					}
				}
			}
		}

		public boolean areParallel(Road road1, Road road2) {
			return road1.orientation == road2.orientation;
		}

		public void drawRoads(FloorType floor) {
			for (Road road : roads) {
				boldLine(road.start.x, road.start.y, road.end.x, road.end.y, floor, 5);
			}
		}

		public void printStatistics() {
			System.out.println("Roads: " + roads.size());
			int count = 0;
			for (ArrayList<Intersection> entity : branches.values()) {
				count += entity.size();
			}
			System.out.println("Branches: " + count);
			count = 0;
			for (ArrayList<Intersection> entity : intersections.values()) {
				count += entity.size();
			}
			System.out.println("Intersections: " + count);
		}

		public ArrayList<Coordinate> getReferencePoints() {
			/**
			 * Get significant points of road systems: intersections, starts of
			 * branches and ends of roads
			 */
			ArrayList<Coordinate> answer = new ArrayList<>();
			for (ArrayList<Intersection> list : intersections.values()) {
				// Intersections
				for (Intersection intersection : list) {
					answer.add(intersection.point);
				}
			}
			// Branches points will be added in roads loop (all branch points
			// are road starts/ends)
			for (Road road : roads) {
				// Ends of roads
				answer.add(road.start);
				answer.add(road.end);
			}
			return answer;
		}

		public class Road {
			public final Coordinate start;
			protected final Coordinate end;
			protected final Orientation orientation;
			protected int width = 5;

			public Road(int startX, int startY, int endX, int endY) {
				if (startX != endX && startY != endY) {
					throw new Error("Inappropriate new road: " + startX + " " + startY + " " + endX + " " + endY);
				}
				start = new Coordinate(startX, startY);
				end = new Coordinate(endX, endY);
				orientation = start.x == end.x ? Orientation.VERTICAL : Orientation.HORIZONTAL;
			}

			public String toString() {
				return "Road [" + start.x + ", " + start.y + ", " + end.x + ", " + end.y + "];";
			}

			public CardinalDirection getSideOfRectangle(Rectangle r) {
				// Get direction of rectangle from which this road is located
				if (orientation.isVertical()) {
					if (this.start.x < r.x) {
						return CardinalDirection.W;
					} else if (this.start.x >= r.x + r.width) {
						return CardinalDirection.E;
					} else {
						throw new Error("Vertical road " + this + " is inside rectangle " + r);
					}
				} else {
					if (this.start.y < r.y) {
						return CardinalDirection.N;
					} else if (this.start.y >= r.y + r.height) {
						return CardinalDirection.S;
					} else {
						throw new Error("Horizontal road " + this + " is inside rectangle " + r);
					}
				}
			}

			public boolean crossesRectangle(Rectangle r) {
				if (orientation.isVertical()) {
					return start.x >= r.x && start.x < r.x + r.width;
				} else {
					return start.y >= r.y && start.y < r.y + r.height;
				}
			}

			public boolean isRectangleOverlapsRoad(Rectangle rectangle) {
				EnhancedRectangle ra = new EnhancedRectangle(rectangle);
				if (orientation.isVertical()) {
					if (Utils.integersRangeIntersection(rectangle.y, rectangle.y + rectangle.height - 1, start.y, end.y) > 0 && ra.distanceToLine(start, end) < width / 2) {
						// If road line and rectangle overlap in y-axis,
						// and road is close enough to rectangle
						return true;
					}
				} else {
					if (Utils.integersRangeIntersection(rectangle.x, rectangle.x + rectangle.width - 1, start.x, end.x) > 0 && ra.distanceToLine(start, end) < width / 2) {
						// Same for x-axis
						return true;
					}
				}
				return false;
			}

			public boolean isRectangleNearRoad(EnhancedRectangle rectangle) {
				/**
				 * Checks if this road goes along one of the borders of a
				 * rectangle
				 */
				EnhancedRectangle ra = new EnhancedRectangle(rectangle);
				if (orientation.isVertical()) {
					if (Utils.integersRangeIntersection(rectangle.getY(), rectangle.getY() + rectangle.getHeight() - 1, start.y, end.y) > 0 && ra.distanceToLine(start, end) == width / 2 + 1) {
						// If road line and rectangle overlap in y-axis,
						// and road is close enough to rectangle
						return true;
					}
				} else {
					if (Utils.integersRangeIntersection(rectangle.getX(), rectangle.getX() + rectangle.getWidth() - 1, start.x, end.x) > 0 && ra.distanceToLine(start, end) == width / 2 + 1) {
						// Same for x-axis
						return true;
					}
				}
				return false;
			}
		}

		public class Intersection {
			/**
			 * Intersection or branch
			 */
			protected Road road;
			protected Coordinate point;

			public Intersection(Road road, Coordinate point) {
				this.road = road;
				this.point = point;
			}
		}

	}
	public class QuarterSystem {
		private static final char EMPTY = '.';
		private static final char ROAD = '/';
		private static final char QUARTER = '#';
		private Settlement settlement;
		private char[][] grid;
		public ArrayList<Quarter> quarters = new ArrayList<Quarter>();
		public ArrayList<BuildingPlace> buildingPlaces = new ArrayList<BuildingPlace>();

		public QuarterSystem(Settlement settlement) {
			this.settlement = settlement;
		}

		public void showGrid() {
			for (int y = 0; y < settlement.height; y++) {
				for (int x = 0; x < settlement.height; x++) {
					System.out.print(grid[x][y]);
				}
				System.out.println();
			}
		}

		public void build(ArrayList<Coordinate> points) {
			/**
			 * Builds quarter system from significant points of road system
			 */
			// Grid will be filled with values that show what is in this cell:
			// road, quarter or nothing
			grid = new char[settlement.width][settlement.height];
			for (int i = 0; i < settlement.width; i++) {
				for (int j = 0; j < settlement.height; j++) {
					grid[i][j] = EMPTY;
				}
			}
			for (Road road : settlement.roadSystem.roads) {
				if (road.orientation.isVertical()) {
					for (int y = road.start.y; y <= road.end.y; y++) {
						grid[road.start.x][y] = ROAD;
					}
				} else {
					for (int x = road.start.x; x <= road.end.x; x++) {
						grid[x][road.start.y] = ROAD;
					}
				}
			}
			for (Coordinate point : points) {
				findQuarter(point, 1, 1);
				findQuarter(point, 1, -1);
				findQuarter(point, -1, 1);
				findQuarter(point, -1, -1);
			}
			for (Quarter quarter : quarters) {
				for (BuildingPlace place : quarter.getBuildingPlaces(25)) {
					buildingPlaces.add(place);
				}
			}
		}

		private void findQuarter(Coordinate point, int dx, int dy) {
			/**
			 * Find empty area from the particular side of point. SideTest is
			 * determined by dx and dy, both of which can be either 1 or -1. We
			 * expand the quarter until it stumbles upon a road, border of
			 * location or another quarter.
			 */
			Coordinate cornerPoint = new Coordinate(point.x + dx, point.y + dy);
			if (cornerPoint.x < 0 || cornerPoint.y < 0 || cornerPoint.x >= settlement.width || cornerPoint.y >= settlement.height) {
				return;
			}
			// grid[cornerPoint.x][cornerPoint.y] = QUARTER;
			boolean xStop = false;
			boolean yStop = false;
			int quarterWidth = 0;
			int quarterHeight = 1; 	// This is necessary, because
									// after width becomes 1, height also
									// becomes 1
			for (int step = 0; !(xStop && yStop); step++) {
				if (!xStop) {
					int x = cornerPoint.x + dx * quarterWidth;
					if (x >= 0 && x < settlement.width) {
						// If x is inside location
						for (int y = cornerPoint.y; y != cornerPoint.y + dy * quarterHeight; y += dy) {
							if (grid[x][y] != EMPTY) {
								xStop = true;
								break;
							} else {
								grid[x][y] = QUARTER;
							}
						}
						if (!xStop) {
							quarterWidth++;
						}
					} else {
						xStop = true;
					}
				}
				if (!yStop) {
					int y = cornerPoint.y + dy * quarterHeight;
					if (y >= 0 && y < settlement.height) {
						// If y is inside location
						for (int x = cornerPoint.x; x != cornerPoint.x + dx * quarterWidth; x += dx) {
							if (x == settlement.width || x == -1) {
								yStop = true;
								break;
							} else if (grid[x][y] != EMPTY) {
								yStop = true;
								break;
							} else {
								grid[x][y] = QUARTER;
							}
						}
						if (!yStop) {
							quarterHeight++;
						}
					} else {
						yStop = true;
					}
				}
			}
			if (quarterWidth > 3 && quarterHeight > 3) {
				quarters.add(new Quarter(this, new Rectangle(dx == 1 ? cornerPoint.x
					: cornerPoint.x - quarterWidth + 1, dy == 1 ? cornerPoint.y
					: cornerPoint.y - quarterHeight + 1, quarterWidth, quarterHeight)));
			}
		}

		public class Quarter extends Rectangle {
			private static final long serialVersionUID = 1L;
			public final QuarterSystem system;
			public final ArrayList<Road> closeRoads = new ArrayList<Road>();

			public Quarter(QuarterSystem system, Rectangle rectangle) {
				super(rectangle);
				this.system = system;
				for (Road road : system.settlement.roadSystem.roads) {
					/* */// May fail for roads with even width
					if (road.isRectangleOverlapsRoad(rectangle)) {
						closeRoads.add(road);
					}
				}

				// Now we have a rectangle with border near roads' center line.
				// Then we narrow rectangle by roads, according to roads' width.

				for (Road road : system.settlement.roadSystem.roads) {
					if (!road.crossesRectangle(this)) {
						narrowRectangleByRoad(this, road);
					}
				}
			}

			public HashSet<BuildingPlace> getBuildingPlaces(int minWidth) {
				HashSet<BuildingPlace> answer = new HashSet<BuildingPlace>();
				TerrainModifier modifier = settlement.getTerrainModifier(RecursivelySplitRectangleSystemFactory.create(x, y, width, height, minWidth, 1));
				RectangleSystem rs = modifier.getRectangleSystem(); 
				for (EnhancedRectangle r : rs.getRectangles()) {
					if (rs.isRectangleOuter(r)) {
						answer.add(new BuildingPlace(r, this));
					}
				}
				return answer;
			}

			private void narrowRectangleByRoad(Rectangle rec, Road road) {
				/**
				 * Change rectangle start and dimensions as if road would
				 * "bite off" a part of rectangle by road's width.
				 */
				CardinalDirection side = road.getSideOfRectangle(rec);
				if (side == CardinalDirection.N) {
					int newY = Math.max(road.start.y + road.width / 2 + 1, rec.y);
					if (newY != rec.y) {
						rec.setBounds(rec.x, newY, rec.width, rec.height - (newY - road.start.y) + 1);
					}
				} else if (side == CardinalDirection.E) {
					int newEndX = Math.min(rec.x + rec.width - 1, road.start.x - road.width / 2 - 1);
					if (newEndX != rec.x + rec.width - 1) {
						rec.setSize(newEndX - rec.x + 1, rec.height);
					}
				} else if (side == CardinalDirection.S) {
					int newEndY = Math.min(rec.y + rec.height - 1, road.start.y - road.width / 2 - 1);
					if (newEndY != rec.y + rec.height - 1) {
						rec.setSize(rec.width, newEndY - rec.y + 1);
					}
				} else if (side == CardinalDirection.W) {
					int newX = Math.max(road.start.x + road.width / 2 + 1, rec.x);
					if (newX != rec.x) {
						rec.setBounds(newX, rec.y, rec.width - (newX - road.start.x) + 1, rec.height);
					}
				}
			}
		}

	}
}
