package tendiwa.core;

import tendiwa.core.meta.Coordinate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Chunk implements Serializable {
public static final byte SIZE = 32;
public final int x;
public final int y;
public HorizontalPlane plane;
protected HashMap<Integer, ItemCollection> items = new HashMap<>();
Map<Integer, Character> characters = new HashMap<>();
short[][] terrain;
Map<Integer, GameObject> objects = new HashMap<>();
private ArrayList<SoundSource> soundSources = new ArrayList<>();
transient private TimeStream timeStream;

public Chunk(HorizontalPlane plane, int x, int y) {
	this.x = x;
	this.y = y;
	this.terrain = new short[SIZE][SIZE];
	this.plane = plane;
}

public static Coordinate[] vector(int startX, int startY, int endX, int endY) {
	int l = Math.round(Math.max(Math.abs(endX - startX),
		Math.abs(endY - startY)));
	float x[] = new float[l + 2];
	float y[] = new float[l + 2];
	Coordinate result[] = new Coordinate[l + 1];

	x[0] = startX;
	y[0] = startY;

	if (startX == endX && startY == endY) {
		result = new Coordinate[1];
		result[0] = new Coordinate(startX, startY);
		return result;
	}
	float dx = (endX - startX) / (float) l;
	float dy = (endY - startY) / (float) l;
	for (int i = 1; i <= l; i++) {
		x[i] = x[i - 1] + dx;
		y[i] = y[i - 1] + dy;
	}
	x[l + 1] = endX;
	y[l + 1] = endY;

	for (int i = 0; i <= l; i++) {
		result[i] = new Coordinate(Math.round(x[i]), Math.round(y[i]));
	}
	return result;
}

public int getX() {
	return x;
}

public int getY() {
	return y;
}

protected NonPlayerCharacter createCharacter(int relX, int relY, CharacterType characterType, String name, int fraction) {
	NonPlayerCharacter character = new NonPlayerCharacter(plane, characterType, x + relX, y + relY, name);
	character.setFraction(fraction);
	addCharacter(character);
		/*
		 * timeStream.fireEvent(new EventCharacterAppear( character.getId(),
		 * character.x, character.y, character.getType().getId(),
		 * character.name, character.getEffects(), character.getEquipment(),
		 * character.getFraction()));
		 */
	timeStream.notifyNeighborsVisiblilty(character);
	character.getVisibleEntities();
	return character;
}

void addCharacter(Character character) {
	characters.put(character.x * Chunk.SIZE + character.y, character);
}

void removeCharacter(Character character) {
	characters.remove(character.x * Chunk.SIZE + character.y);
}

public void removeObject(int x, int y) {
	objects.remove(x * Chunk.SIZE + y);
	if (Tendiwa.getPlayer().canSee(x, y) && Tendiwa.getPlayer().isVisionCacheEmpty()) {
		Tendiwa.getPlayer().invalidateVisionCache();
	}
	throw new UnsupportedOperationException();
}

public void createSoundSource(int x, int y, SoundType type) {
	soundSources.add(new SoundSource(x, y, type, 1000));
	throw new UnsupportedOperationException();
}

public void removeSoundSource(int x, int y) {
	int size = soundSources.size();
	for (int i = 0; i < size; i++) {
		Sound s = soundSources.get(i);
		if (s.x == x && s.y == y) {
			soundSources.remove(i);
			throw new UnsupportedOperationException();
//			return;
		}
	}
	throw new Error("Sound source at " + x + ":" + y + " not found");
}

/**
 * Sets or unsets a TimeStream this Chunk belongs to.
 *
 * @param timeStream
 * 	A TimeStream, or null to let this Chunk belong to no TimeStream.
 */
public void setTimeStream(TimeStream timeStream) {
	if (timeStream != null && this.timeStream != null) {
		throw new RuntimeException(this + " is already in a time stream!");
	}
	this.timeStream = timeStream;
}

public String toString() {
	return "Chunk " + x + " " + y;
}

/**
 * Checks if this chunk is inside a TimeStream
 *
 * @param timeStream
 * @return True if it is, false otherwise (if it belongs to another TimeStream or to no TimeStream at all)
 */
public boolean belongsToTimeStream(TimeStream timeStream) {
	return this.timeStream == timeStream;
}

public void setTerrainElement(short id, int x, int y) {
	terrain[x - this.x][y - this.y] = id;
}

public Character getCharacter(int x, int y) {
	return characters.get(x * SIZE + y);
}

public GameObject getGameObject(int x, int y) {
	return objects.get(x * SIZE + y);
}

/**
 * Places a UniqueItem on a certain cell in this Chunk.
 *
 * @param x
 * 	Relative coordinates of cell
 * @param y
 * 	Relative coordinates of cell
 */
public void addItem(UniqueItem item, int x, int y) {
	items.get(x * Chunk.SIZE + y).add(item);
}

/**
 * Places a ItemPile on a certain cell in this Chunk.
 *
 * @param x
 * 	Relative coordinates of cell
 * @param y
 * 	Relative coordinates of cell
 */
public void addItem(ItemPile item, int x, int y) {
	items.get(x * SIZE + y).add(item);
}

public boolean hasObject(int x, int y) {
	return objects.containsKey(x * SIZE + y);
}

public boolean hasCharacter(int x, int y) {
	for (Character character : characters.values()) {
		if (character.x == x && character.y == y) {
			return true;
		}
	}
	return false;
}

public ItemCollection getItems(int x, int y) {
	return items.get(x * SIZE + y);
}

public float distance(int startX, int startY, int endX, int endY) {
	return (float) Math.sqrt(Math.pow(startX - endX, 2)
		+ Math.pow(startY - endY, 2));
}

public void removeItem(UniqueItem item, int x, int y) {
	items.get(x * SIZE + y).removeUnique(item);
}

public void removeItem(ItemPile item, int x, int y) {
	items.get(x * SIZE + y).removePile(item);
}

public boolean isDoor(int x, int y) {
	return getObject(x, y).getType().getObjectClass() == ObjectType.ObjectClass.DOOR;
}

private GameObject getObject(int x, int y) {
	return objects.get(x * SIZE + y);
}

/**
 * @param x
 * 	Absolute x coordinate.
 * @param y
 * 	Absolute y coordinate.
 * @return Id of {@link TerrainType} in that cell.
 */
public short getTerrainElement(int x, int y) {
	return terrain[x-this.x][y-this.y];
}

public enum Passability {
	FREE, SEE, NO;
}
}
