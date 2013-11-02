package tendiwa.core;

import tendiwa.core.meta.Chance;
import tendiwa.core.meta.Coordinate;
import tendiwa.core.terrain.settlements.BuildingPlace;

import java.awt.*;
import java.util.*;

public class Location extends TerrainBasics {
public final String name;
protected final int width;
protected final int height;
private HorizontalPlane plane;

Location(HorizontalPlane plane, int x, int y, int width, int height) {
	super(x, y);
	this.cells = plane.getCells(x, y, width, height);
	this.name = "Empty location name!";
	this.plane = plane;
	this.width = width;
	this.height = height;
	Chat.initLocationChat(this);
}

public void line(int startX, int startY, int endX, int endY, PlaceableInCell placeable) {
	if (startX == endX && startY == endY) {
		placeable.place(cells[startX][startY]);
		return;
	}
	Coordinate[] cells = vector(startX, startY, endX, endY);
	int size = cells.length;
	for (int i = 0; i < size - 1; i++) {
		int x = cells[i].x;
		int y = cells[i].y;
		int x2 = cells[i + 1].x;
		int y2 = cells[i + 1].y;

		placeable.place(this.cells[x][y]);
		if (i < cells.length - 1 && x != x2 && y != y2) {
			int cx = x + ((x2 > x) ? 1 : -1);
			placeable.place(this.cells[cx][y]);
		}
		if (i == size - 2) {
			placeable.place(this.cells[x2][y2]);
		}
	}
}

public void square(int startX, int startY, int w, int h, PlaceableInCell placeable) {
	square(startX, startY, w, h, placeable, false);
}

public void square(Rectangle r, PlaceableInCell placeable, boolean fill) {
	square(r.x, r.y, r.width, r.height, placeable, fill);
}

public void square(int startX, int startY, int w, int h, PlaceableInCell placeable, boolean fill) {
	if (startX + w > getWidth() || startY + h > getHeight()) {
		throw new LocationException("Square " + startX + "," + startY + "," + w + "," + h + " goes out of borders of a "+getWidth()+"*"+getHeight()+" location");
	}
	if (w == 1) {
		line(startX, startY, startX, startY + h - 1, placeable);
	} else if (h == 1) {
		line(startX, startY, startX + w - 1, startY, placeable);
	} else {
		line(startX, startY, startX + w - 2, startY, placeable);
		line(startX, startY, startX, startY + h - 2, placeable);
		line(startX + w - 1, startY, startX + w - 1, startY + h - 1, placeable);
		line(startX, startY + h - 1, startX + w - 2, startY + h - 1, placeable);
		if (fill) {
			for (int i = 1; i < h - 1; i++) {
				line(startX + 1, startY + i, startX + w - 1, startY + i, placeable);
			}
		}
	}
}

public ArrayList<Coordinate> getCircle(int cx, int cy, int r) {
	ArrayList<Coordinate> answer = new ArrayList<>();
	int d = -r / 2;
	int xCoord = 0;
	int yCoord = r;
	HashMap<Integer, Integer> x = new HashMap<>();
	HashMap<Integer, Integer> y = new HashMap<>();
	x.put(0, 0);
	y.put(0, r);
	do {
		if (d < 0) {
			xCoord += 1;
			d += xCoord;
		} else {
			yCoord -= 1;
			d -= yCoord;
		}
		x.put(x.size(), xCoord);
		y.put(y.size(), yCoord);
	} while (yCoord > 0);
	int size = x.size();
	for (int i = 0; i < size; i++) {
		answer.add(new Coordinate(cx + x.get(i), cy + y.get(i)));
		answer.add(new Coordinate(cx - x.get(i), cy + y.get(i)));
		answer.add(new Coordinate(cx + x.get(i), cy - y.get(i)));
		answer.add(new Coordinate(cx - x.get(i), cy - y.get(i)));
	}
	return answer;
}

/**
 * Uses a {@link Segment} to drawWorld a rectangle. This method is almost identical to {@link Location#square(int, int, int,
 * int, PlaceableInCell)}, it is just more convenient to use when Segments are often used. The drawn rectangle's
 * top-left cell will be {segment.x;segment.y}.
 *
 * @param segment
 * 	A segment of cells to drawWorld.
 * @param width
 * 	Defines width (if segment.getDirection() == DirectionToBERemoved.V) of height (if segment.getDirection() ==
 * 	DirectionToBERemoved.H) of the drawn rectangle.
 * @param placeable
 * 	What to drawWorld in each cell
 */
public void drawSegment(Segment segment, int width, PlaceableInCell placeable) {
	if (segment.getOrientation().isHorizontal()) {
		square(segment.getX(), segment.getY(), segment.getLength(), width, placeable, true);
	} else {
		square(segment.getX(), segment.getY(), width, segment.getLength(), placeable, true);
	}
}

public void circle(int cX, int cY, int r, PlaceableInCell placeable) {
	circle(cX, cY, r, placeable, false);
}

public void circle(int cX, int cY, int r, PlaceableInCell placeable, boolean fill) {
	int d = -r / 2;
	int xCoord = 0;
	int yCoord = r;
	HashMap<Integer, Integer> x = new HashMap<>();
	HashMap<Integer, Integer> y = new HashMap<>();
	x.put(0, 0);
	y.put(0, r);
	do {
		if (d < 0) {
			xCoord += 1;
			d += xCoord;
		} else {
			yCoord -= 1;
			d -= yCoord;
		}
		x.put(x.size(), xCoord);
		y.put(y.size(), yCoord);
	} while (yCoord > 0);
	int size = x.size();
	for (int i = 0; i < size; i++) {
		placeable.place(cells[cX + x.get(i)][cY + y.get(i)]);
		placeable.place(cells[cX - x.get(i)][cY + y.get(i)]);
		placeable.place(cells[cX + x.get(i)][cY - y.get(i)]);
		placeable.place(cells[cX - x.get(i)][cY - y.get(i)]);
	}
}

public TerrainModifier getTerrainModifier(RectangleSystem rs) {
	return new TerrainModifier(this, rs);
}

public CellCollection getCellCollection(ArrayList<Coordinate> cls) {
	return new CellCollection(cls, this);
}

// From LocationGenerator
public NonPlayerCharacter createCharacter(String type, int characterTypeId, int x, int y) {
	NonPlayerCharacter ch = new NonPlayerCharacter(plane, StaticData.getCharacterType(characterTypeId), x, y, name);
	characters.put(ch.getId(), ch);
	cells[x][y].character(ch);
	return ch;
}

public void selectPlane(HorizontalPlane plane) {
	this.plane = plane;
}

public <T extends Building> void placeBuilding(T building) {
	/**
	 * Places building when current location is not Settlement.
	 *
	 * @param side
	 *            What side a building is rotated to.
	 */
	BuildingPlace place = new BuildingPlace(x, y, width, height);
	building.draw();
}

// From TerrainGenerator
public ArrayList<Coordinate> polygon(ArrayList<Coordinate> coords) {
	return polygon(coords, false);
}

public ArrayList<Coordinate> polygon(ArrayList<Coordinate> coords, boolean mode) {
	ArrayList<Coordinate> answer = new ArrayList<>();

	int size = coords.size();
	Coordinate[] v;
	int vSize;
	for (int i = 0; i < size; i++) {
		Coordinate coord = coords.get(i);
		Coordinate nextCoord = coords.get((i == size - 1) ? 0 : i + 1);
		v = vector(coord.x, coord.y, nextCoord.x, nextCoord.y);
		vSize = v.length;
		for (int j = 0; j < vSize - 1; j++) {
			answer.add(v[j]);
		}
	}
	int startX = (int) Math.floor((coords.get(0).x + coords.get(1).x + coords.get(2).x) / 3);
	int startY = (int) Math.floor((coords.get(0).y + coords.get(1).y + coords.get(2).y) / 3);
	if (!mode) {
		HashSet<Coordinate> oldFront = new HashSet<>();
		HashSet<Coordinate> newFront = new HashSet<>();
		newFront.add(new Coordinate(startX, startY));
		int[][] pathTable = new int[getWidth()][getHeight()];
		for (int i = 0; i < getWidth(); i++) {
			Arrays.fill(pathTable[i], 0);
		}
		Iterator<Coordinate> it = answer.iterator();
		while (it.hasNext()) {
			Coordinate cell = it.next();
			pathTable[cell.x][cell.y] = 2;
		}
		answer = new ArrayList<>();
		do {
			oldFront = newFront;
			newFront = new HashSet<>();
			size = oldFront.size();
			it = oldFront.iterator();
			while (it.hasNext()) {
				Coordinate cell = it.next();
				int x = cell.x;
				int y = cell.y;
				int[] adjactentX = {x + 1, x, x, x - 1};
				int[] adjactentY = {y, y - 1, y + 1, y};
				for (int j = 0; j < 4; j++) {
					int thisNumX = adjactentX[j];
					int thisNumY = adjactentY[j];
					if (pathTable[thisNumX][thisNumY] != 0 && pathTable[thisNumX][thisNumY] != 2) {
						continue;
					}
					if (thisNumX < 0 || thisNumX >= getWidth() || thisNumY < 0 || thisNumY >= getHeight()) {
						continue;
					}
					// if (thisNumX<=0 || thisNumX>=w-1 || thisNumY<=0 ||
					// thisNumY>=h-1) {
					// // ��������, ����� ��� ��������� �������� ������ ��
					// �������� �� �������
					// continue;
					// }
					if (pathTable[thisNumX][thisNumY] == 0) {
						newFront.add(new Coordinate(thisNumX, thisNumY));
					}
					answer.add(new Coordinate(thisNumX, thisNumY));
					pathTable[thisNumX][thisNumY] = 1;
				}
			}
		} while (newFront.size() > 0);
	}
	return answer;
}

public void fillWithCells(int floorId, int objectId) {
	for (int i = 0; i < width; i++) {
		for (int j = 0; j < height; j++) {
			setFloor(i, j, floorId);
			setObject(i, j, objectId);
		}
	}
}

public ArrayList<Coordinate> closeCells(int startX, int startY, int length, int pass, boolean noDiagonal) {
	ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
	ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
	ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
	answer.add(new Coordinate(startX, startY));
	newFront.add(new Coordinate(startX, startY));
	int[][] pathTable = new int[width][height];
	for (int i = 0; i < width; i++) {
		Arrays.fill(pathTable[i], 0);
	}
	int numOfSides = noDiagonal ? 4 : 8;
	int[] adjactentX;
	int[] adjactentY;
	if (noDiagonal) {
		adjactentX = new int[]{0, 1, 0, -1};
		adjactentY = new int[]{-1, 0, 1, 0};
	} else {
		adjactentX = new int[]{0, 1, 0, -1, 1, 1, -1, -1};
		adjactentY = new int[]{-1, 0, 1, 0, 1, -1, 1, -1};
	}
	do {
		oldFront = newFront;
		newFront = new ArrayList<Coordinate>();
		Iterator<Coordinate> it = oldFront.iterator();
		while (it.hasNext()) {
			// ������� ����� �� ������ ��������� ������ �� ������ ������
			Coordinate c = it.next();
			int x = c.x;
			int y = c.y;

			for (int j = 0; j < numOfSides; j++) {
				int thisNumX = x + adjactentX[j];
				int thisNumY = y + adjactentY[j];
				if (thisNumX <= 0 || thisNumX >= getWidth() - 1 || thisNumY <= 0 || thisNumY >= getHeight() - 1) {
					// ��������, ����� ��� ��������� �������� ������ ��
					// �������� �� �������
					continue;
				}
				// if (thisNumX < 0 || thisNumX >= width || thisNumY < 0
				// || thisNumY >= height) {
				// // �� ������� ������ �� �������, ������� ������� ��
				// // ������� ���� ��� �������� ��� ���������
				// continue;
				// }
				if (pathTable[thisNumX][thisNumY] != 0) {
					continue;
				}

				if (cells[thisNumX][thisNumY].getPassability() != pass) {
					continue;
				}
				if (Math.floor(distance(startX, startY, thisNumX, thisNumY)) >= length) {
					continue;
				}
				newFront.add(new Coordinate(thisNumX, thisNumY));
				answer.add(new Coordinate(thisNumX, thisNumY));
				pathTable[thisNumX][thisNumY] = 1;
			}
		}
	} while (newFront.size() > 0);
	return answer;
}

public ArrayList<Coordinate> getElementsAreaBorder(int startX, int startY, PlaceableInCell placeable, int depth, boolean noDiagonal) {
	// �������� ������� ������� � ���������� ���� %type% ���� %val%, �������
	// �� ����� ��� � %depth% ������� �� ��������� ������
	// noDiagonal - �������� ������� ���������� ������ �� ������ �������,
	// ��� �� ��� ������ ������.
	int[][] pathTable = new int[getWidth()][getHeight()];
	ArrayList<Coordinate> cells = new ArrayList<>();
	ArrayList<Coordinate> oldFront = new ArrayList<>();
	ArrayList<Coordinate> newFront = new ArrayList<>();
	// �� ����� ������ �������� ������
	newFront.add(new Coordinate(startX, startY));
	for (int i = 0; i < getWidth(); i++) {
		for (int j = 0; j < getHeight(); j++) {
			pathTable[i][j] = 0;
		}
	}
	pathTable[startX][startY] = 0;
	int t = 0;
	int numOfSides = noDiagonal ? 4 : 8;
	int[] adjactentX;
	int[] adjactentY;
	if (noDiagonal) {
		adjactentX = new int[]{0, 1, 0, -1};
		adjactentY = new int[]{-1, 0, 1, 0};
	} else {
		adjactentX = new int[]{0, 1, 0, -1, 1, 1, -1, -1};
		adjactentY = new int[]{-1, 0, 1, 0, 1, -1, 1, -1};
	}
	do {
		oldFront = newFront;
		newFront = new ArrayList<>();
		for (int i = 0; i < oldFront.size(); i++) {
			// ������� ����� �� ������ ��������� ������ �� ������ ������
			int x = oldFront.get(i).x;
			int y = oldFront.get(i).y;
			for (int j = 0; j < numOfSides; j++) {
				int thisNumX = x + adjactentX[j];
				int thisNumY = y + adjactentY[j];
				if (thisNumX < 0 || thisNumX >= getWidth() || thisNumY < 0 || thisNumY >= getHeight() || pathTable[thisNumX][thisNumY] != 0 || distance(startX, startY, thisNumX, thisNumY) > depth) {
					continue;
				}
				int currElemVal = getElement(thisNumX, thisNumY, placeable.getClass());
				if (placeable.containedIn(this.cells[thisNumX][thisNumY]) && !(thisNumX == startX && thisNumY == startY)) {
					pathTable[thisNumX][thisNumY] = t + 1;
					newFront.add(new Coordinate(thisNumX, thisNumY));
				} else if (!placeable.containedIn(this.cells[thisNumX][thisNumY])) {
					cells.add(new Coordinate(x, y));
				}
			}
		}
		t++;
	} while (newFront.size() > 0);
	return cells;
}

public void waveStructure(int startX, int startY, PlaceableInCell placeable, int maxSize) {
	Hashtable<Integer, Coordinate> newFront = new Hashtable<>();
	newFront.put(0, new Coordinate(startX, startY));
	int[][] canceled = new int[getWidth()][getHeight()];
	int[][] pathTable = new int[getWidth()][getHeight()];
	for (int i = 0; i < getWidth(); i++) {
		Arrays.fill(pathTable[i], 0);
		Arrays.fill(canceled[i], 0);
	}
	setElement(startX, startY, placeable);
	int t = 0;
	do {
		int size = newFront.size();
		for (int i = 0; i < size; i++) {
			Coordinate c = newFront.get(i);
			int x = c.x;
			int y = c.y;
			int[] adjactentX = {x + 1, x, x, x - 1};
			int[] adjactentY = {y, y - 1, y + 1, y};
			for (int j = 0; j < 4; j++) {
				int thisNumX = adjactentX[j];
				int thisNumY = adjactentY[j];
				if (thisNumX < 0 || thisNumX >= getWidth() || thisNumY < 0 || thisNumY >= getHeight() || canceled[thisNumX][thisNumY] != 0) {
					continue;
				}
				if (thisNumX <= 0 || thisNumX >= getWidth() - 1 || thisNumY <= 0 || thisNumY >= getHeight() - 1) {
					continue;
				}
				// TODO: This has been making compile time errors so I commented it out : (
//				if (getElement(thisNumX + 1, thisNumY, type) + getElement(thisNumX - 1, thisNumY, type) + getElement(thisNumX, thisNumY + 1, type) + getElement(thisNumX, thisNumY - 1, type) + getElement(thisNumX + 1, thisNumY + 1, type) + getElement(thisNumX - 1, thisNumY + 1, type) + getElement(thisNumX + 1, thisNumY - 1, type) + getElement(thisNumX - 1, thisNumY - 1, type) > 3 && t > 4) {
//					continue;
//				}
				Chance chance = new Chance(15);
				if (chance.roll()) {
					canceled[thisNumX][thisNumY] = 1;
					continue;
				}
				setElement(thisNumX, thisNumY, placeable);
				newFront.put(newFront.size(), new Coordinate(thisNumX, thisNumY));
			}
		}
		t++;
	} while (newFront.size() > 0 && t < maxSize);
}

public CellCollection newCellCollection(Collection<Coordinate> cls) {
	return new CellCollection(cls, this);
}

public int[][] getPathTable(int startX, int startY, int endX, int endY, boolean noDiagonal) {
	int[][] pathTable = new int[getWidth()][getHeight()];
	boolean isPathFound = false;
	ArrayList<Coordinate> oldFront = new ArrayList<>();
	ArrayList<Coordinate> newFront = new ArrayList<>();
	newFront.add(new Coordinate(startX, startY));
	for (int i = 0; i < getWidth(); i++) {
		for (int j = 0; j < getHeight(); j++) {
			pathTable[i][j] = 0;
		}
	}
	pathTable[startX][startY] = 0;
	int t = 0;
	int numOfSides = noDiagonal ? 4 : 8;
	int[] adjactentX;
	int[] adjactentY;
	if (noDiagonal) {
		adjactentX = new int[]{0, 1, 0, -1};
		adjactentY = new int[]{-1, 0, 1, 0};
	} else {
		adjactentX = new int[]{0, 1, 0, -1, 1, 1, -1, -1};
		adjactentY = new int[]{-1, 0, 1, 0, 1, -1, 1, -1};
	}
	do {
		oldFront = newFront;
		newFront = new ArrayList<Coordinate>();
		for (int i = 0; i < oldFront.size(); i++) {
			int x = oldFront.get(i).x;
			int y = oldFront.get(i).y;
			for (int j = 0; j < numOfSides; j++) {
				int thisNumX = x + adjactentX[j];
				int thisNumY = y + adjactentY[j];
				if (thisNumX < 0 || thisNumX >= getWidth() || thisNumY < 0 || thisNumY >= getHeight() || pathTable[thisNumX][thisNumY] != 0) {
					continue;
				}
				if (thisNumX == endX && thisNumY == endY) {
					isPathFound = true;
				}
				if (cells[thisNumX][thisNumY].getPassability() == TerrainBasics.PASSABILITY_FREE && !(thisNumX == startX && thisNumY == startY)) {
					pathTable[thisNumX][thisNumY] = t + 1;
					newFront.add(new Coordinate(thisNumX, thisNumY));
				}
			}
		}
		t++;
	} while (newFront.size() > 0 && !isPathFound && t < 1000);
	return pathTable;
}

public ArrayList<Coordinate> getPath(int startX, int startY, int destinationX, int destinationY, boolean noDiagonal) {
	// �������� ���� �� ������ � ���� ������� ��������� (0 - ������ ��� � �.
	// �.)
	if (destinationX == startX && destinationY == startY) {
		throw new Error("Getting path to itself");
	}
	int[][] pathTable = getPathTable(startX, startY, destinationX, destinationY, noDiagonal);
	ArrayList<Coordinate> path = new ArrayList<Coordinate>();
	if (Coordinate.isNear(startX, startY, destinationX, destinationY)) {
		path.add(new Coordinate(destinationX, destinationY));
		return path;
	}
	// ���������� ����
	path.add(new Coordinate(startX, startY));
	int currentNumX = destinationX;
	int currentNumY = destinationY;
	int x = currentNumX;
	int y = currentNumY;
	int numOfSides = noDiagonal ? 4 : 8;
	int[] adjactentX;
	int[] adjactentY;
	if (noDiagonal) {
		adjactentX = new int[]{0, 1, 0, -1};
		adjactentY = new int[]{-1, 0, 1, 0};
	} else {
		adjactentX = new int[]{0, 1, 0, -1, 1, 1, -1, -1};
		adjactentY = new int[]{-1, 0, 1, 0, 1, -1, 1, -1};
	}
	for (int j = pathTable[currentNumX][currentNumY]; j > 0; j = pathTable[currentNumX][currentNumY]) {
		// �������: �� ���-�� ����� �� ������ dest �� ��������� ������ (���
		// 1)
		path.add(0, new Coordinate(currentNumX, currentNumY));
		currentNumX = -1;
		for (int i = 0; i < numOfSides; i++) {
			// ��� ������ �� ��������� ������ (�, �, �, �)
			int thisNumX = x + adjactentX[i];
			if (thisNumX < 0 || thisNumX >= getWidth()) {
				continue;
			}
			int thisNumY = y + adjactentY[i];
			if (thisNumY < 0 || thisNumY >= getHeight()) {
				continue;
			}
			if (pathTable[thisNumX][thisNumY] == j - 1 && (currentNumX == -1 || distance(thisNumX, thisNumY, destinationX, destinationY) < distance(currentNumX, currentNumY, destinationX, destinationY))) {
				// ���� ������ � ���� ������� �������� ���������� �����,
				// ������� �� ��
				currentNumX = thisNumX;
				currentNumY = thisNumY;
			}
		}
		x = currentNumX;
		y = currentNumY;
	}
	return path;
}

protected void cellularAutomataSmooth(int level, int type, PlaceableInCell formerContent, PlaceableInCell newContent) {
	// Smooth the borders of terrain's areas consisting of
	// elements with %type% and %val%
	for (int l = 0; l < level; l++) {
		Cell[][] bufCells = new Cell[getWidth()][getHeight()];
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				bufCells[j][i] = new Cell(cells[j][i]);
			}
		}
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				int count = 0;
				boolean iGT0 = i > 0;
				boolean iLTw = i < getWidth() - 1;
				boolean jGT0 = j > 0;
				boolean jLTh = j < getHeight() - 1;
				if (jGT0 && bufCells[i][j - 1].contains(formerContent)) {
					count++;
				}
				if (iLTw && jGT0 && bufCells[i + 1][j - 1].contains(formerContent)) {
					count++;
				}
				if (iLTw && bufCells[i + 1][j].contains(formerContent)) {
					count++;
				}
				if (iLTw && jLTh && bufCells[i + 1][j + 1].contains(formerContent)) {
					count++;
				}
				if (jLTh && bufCells[i][j + 1].contains(formerContent)) {
					count++;
				}
				if (iGT0 && jLTh && bufCells[i - 1][j + 1].contains(formerContent)) {
					count++;
				}
				if (iGT0 && bufCells[i - 1][j].contains(formerContent)) {
					count++;
				}
				if (iGT0 && jGT0 && bufCells[i - 1][j - 1].contains(formerContent)) {
					count++;
				}

				if (bufCells[i][j].contains(formerContent) && count > 4) {
					setElement(i, j, formerContent);
				} else if (bufCells[i][j].contains(formerContent) && count < 4) {
					setElement(i, j, newContent);
				}
			}
		}
	}
}

/**
 * Default bold line with width of 3 cells
 *
 * @param startX
 * @param startY
 * @param endX
 * @param endY
 * @param placeable
 * @see Location#boldLine(int, int, int, int, PlaceableInCell, int)
 */
public void boldLine(int startX, int startY, int endX, int endY, PlaceableInCell placeable) {
	boldLine(startX, startY, endX, endY, placeable, 3);
}

public void boldLine(int startX, int startY, int endX, int endY, PlaceableInCell placeable, int w) {
	int dx;
	int dy;
	if (endX - startX == 0) {
		dx = 1;
		dy = 0;
	} else {
		int tg = (endY - startY) / (endX - startX);
		if (tg > -0.5 && tg < 0.5) {
			dx = 0;
			dy = 1;
		} else {
			dx = 1;
			dy = 0;
		}
	}
	int coeff = (int) Math.floor(w / 2);
	startX -= dx * coeff;
	startY -= dy * coeff;
	endX -= dx * coeff;
	endY -= dy * coeff;
	if (startX < 0) {
		startX = 0;
	} else if (startX >= getWidth()) {
		startX = getWidth() - 1;
	}
	if (startY < 0) {
		startY = 0;
	} else if (startY >= getHeight()) {
		startY = getHeight() - 1;
	}
	if (endX < 0) {
		endX = 0;
	} else if (endX >= getWidth()) {
		endX = getWidth();
	}
	if (endY < 0) {
		endY = 0;
	} else if (endY >= getHeight()) {
		endY = getHeight() - 1;
	}
	for (int i = 0; i < w; i++) {
//		line(startX, startY, endX, endY, placeable);
		startX += dx;
		startY += dy;
		endX += dx;
		endY += dy;
	}
}

public void placeSeveralObjects(ArrayList<Integer> objects, int num, Rectangle r) {
	// ���������� ��������� ��������
	// in: objects - ������ � id ������ ��������
	// num - ���������� ��������.
	// r - �������������, � ������� ����������� �������
	int size = objects.size();
	for (int i = 0; i < num; i++) {
		this.setObject(r.x + Chance.rand(0, r.width - 1), r.y + Chance.rand(0, r.height - 1), objects.get(Chance.rand(0, size - 1)));
	}
}

public void drawPath(int startX, int startY, int endX, int endY, PlaceableInCell placeable) {
	ArrayList<Coordinate> path = getPath(startX, startY, endX, endY, true);
	int size = path.size();
	for (int i = 0; i < size; i++) {
		setElement(path.get(i).x, path.get(i).y, placeable);
	}
}

protected CellCollection getCoast(int startX, int startY) {
	int[][] pathTable = new int[getWidth()][getHeight()];
	ArrayList<Coordinate> cells = new ArrayList<Coordinate>();
	ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
	ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
	// �� ����� ������ �������� ������
	newFront.add(new Coordinate(startX, startY));
	for (int i = 0; i < getWidth(); i++) {
		for (int j = 0; j < getHeight(); j++) {
			pathTable[i][j] = 0;
		}
	}
	pathTable[startX][startY] = 0;
	int t = 0;
	do {
		oldFront = newFront;
		newFront = new ArrayList<Coordinate>();
		for (int i = 0; i < oldFront.size(); i++) {
			// ������� ����� �� ������ ��������� ������ �� ������ ������
			int x = oldFront.get(i).x;
			int y = oldFront.get(i).y;
			int[] adjactentX = new int[]{x + 1, x, x, x - 1,};
			int[] adjactentY = new int[]{y, y - 1, y + 1, y};
			for (int j = 0; j < 4; j++) {
				int thisNumX = adjactentX[j];
				int thisNumY = adjactentY[j];
				if (thisNumX < 0 || thisNumX >= getWidth() || thisNumY < 0 || thisNumY >= getHeight() || pathTable[thisNumX][thisNumY] != 0) {
					continue;
				}
				if (this.cells[thisNumX][thisNumY].getPassability() == 0 && !(thisNumX == startX && thisNumY == startY)) {
					pathTable[thisNumX][thisNumY] = t + 1;
					newFront.add(new Coordinate(thisNumX, thisNumY));
				} else if (this.cells[thisNumX][thisNumY].getPassability() != 0) {
					cells.add(new Coordinate(x, y));
				}
			}
		}
		t++;
	} while (newFront.size() > 0 && t < 2000);
	return newCellCollection(cells);
}

public ArrayList<Coordinate> getCellsAroundCell(int x, int y) {
	ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
	int x1[] = {x, x + 1, x + 1, x + 1, x, x - 1, x - 1, x - 1};
	int y1[] = {y - 1, y - 1, y, y + 1, y + 1, y + 1, y, y - 1};
	for (int i = 0; i < 8; i++) {
		if (cells[x1[i]][y1[i]].getPassability() == PASSABILITY_FREE) {
			answer.add(new Coordinate(x1[i], y1[i]));
		}
	}
	return answer;
}

public void lineToRectangleBorder(int startX, int startY, CardinalDirection side, Rectangle r, PlaceableInCell placeable) {
	if (!r.contains(startX, startY)) {
		throw new Error("Rectangle " + r + " contains no point " + startX + ":" + startY);
	}
	if (side == null) {
		throw new NullPointerException();
	}
	int endX, endY;
	switch (side) {
		case N:
			endX = startX;
			endY = r.y;
			break;
		case E:
			endX = r.x + r.width - 1;
			endY = startY;
			break;
		case S:
			endX = startX;
			endY = r.y + r.height - 1;
			break;
		case W:
		default:
			endX = r.x;
			endY = startY;
	}
	line(startX, startY, endX, endY, placeable);
}

public void fillSideOfRectangle(Rectangle r, CardinalDirection side, PlaceableInCell placeable) {
	int startX, startY, endX, endY;
	switch (side) {
		case N:
			startX = r.x;
			startY = r.y;
			endX = r.x + r.width - 1;
			endY = r.y;
			break;
		case E:
			startX = r.x + r.width - 1;
			startY = r.y;
			endX = r.x + r.width - 1;
			endY = r.y + r.height - 1;
			break;
		case S:
			startX = r.x;
			startY = r.y + r.height - 1;
			endX = r.x + r.width - 1;
			endY = r.y + r.height - 1;
			break;
		case W:
			startX = r.x;
			startY = r.y;
			endX = r.x;
			endY = r.y + r.height - 1;
			break;
		default:
			throw new Error("Incorrect side " + side);
	}
	line(startX, startY, endX, endY, placeable);
}

public void fillRectangle(Rectangle r, PlaceableInCell placeable) {
	/**
	 * Fill rectngle with objects randomly. chance% of cells will be filled
	 * with these objects.
	 */
	int x = 0 ;
	int y = 0;
	try {
		for (x = r.x; x < r.x + r.width; x++) {
			for (y = r.y; y < r.y + r.height; y++) {
				placeable.place(cells[x][y]);
			}
		}
	} catch (IndexOutOfBoundsException e) {
		throw new LocationException("Trying to place entity " + placeable + " outside of location at cell " + x + ":" + y);
	}
}

public int getWidth() {
	return width;
}

public int getHeight() {
	return height;
}

public TerrainTransition.TerrainTransitionBuilder transitionBuilder() {
	return new TerrainTransition.TerrainTransitionBuilder().setLocation(this);
}
}
