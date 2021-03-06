package org.tendiwa.core;

import org.tendiwa.geometry.Dimension;
import org.tendiwa.geometry.Rectangle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import static org.tendiwa.geometry.GeometryPrimitives.rectangle;

public class World {

	private static final int defaultPlaneIndex = Integer.MAX_VALUE / 2;
	protected final int width;
	protected final int height;
	final HorizontalPlane defaultPlane;
	private final Rectangle rectangle;
	private Character playerCharacter;
	private HashMap<Integer, HorizontalPlane> planes = new HashMap<>();

	public World(
		Dimension dimension
	) {
		this.rectangle = rectangle(
			dimension.width(),
			dimension.height()
		);
		this.width = dimension.width();
		this.height = dimension.height();
		defaultPlane = initPlane(0);
		planes.put(0, defaultPlane);
	}

	public HorizontalPlane initPlane(int level) {
		HorizontalPlane plane = new HorizontalPlane(width, height, this, level);
		plane.touchChunks(0, 0, width, height);
		return plane;
	}

	public HorizontalPlane getDefaultPlane() {
		return defaultPlane;
	}

	public Character getPlayer() {
		assert playerCharacter != null;
		return playerCharacter;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Character addCharacter(NonPlayerCharacter character) {
		try {
			while (defaultPlane.getPassability(character.x, character.y) == Passability.NO) {
				character.x++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Could not place a character because the whole world is non-passable");
		}
		defaultPlane.addCharacter(character);
//	timeStream.addNonPlayerCharacter(character);
		return character;
	}

	/**
	 * Creates a new {@link Character}, makes it the player character.
	 *
	 * @throws RuntimeException
	 * 	If the cell {x:y} was not passable and neither were all the cells from it till {width-1:y}
	 */
	public Character addPlayerCharacter(Character character) {
		playerCharacter = character;
		try {
			while (defaultPlane.getPassability(playerCharacter.x, playerCharacter.y) == Passability.NO) {
				playerCharacter.x++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Could not place player character because the whole world is non-passable");
		}
		defaultPlane.addCharacter(playerCharacter);
//	timeStream.addPlayerCharacter(playerCharacter);
		Set<Chunk> chunks = defaultPlane.getChunksAroundCoordinate(character.x(), character.y(), Chunk.SIZE * 5);
		for (Chunk chunk : chunks) {
//		timeStream.addChunk(chunk);
		}
		return playerCharacter;
	}

	/**
	 * Lazily returns a HorizontalPlane with index {@code level}. Planes stack on top of each other, with default plane
	 * having index 0. If plane with index {@code level} doesn't exist, this method creates that plane. If it does
	 * exist, an
	 * existing plane is returned. However, to create a plane with index {@code level}, a plane with index {@code
	 * level-1}
	 * must exist.
	 *
	 * @param level
	 * 	Index of plane to retrieve.
	 * @return An existing plane or a new plane, if a plane with that index doesn't exist.
	 */
	public HorizontalPlane getPlane(int level) {
		if (planes.get(level) == null) {
			if (planes.get(level - 1) == null) {
				throw new IllegalArgumentException("Can't create plane " + level + " because plane " + (level - 1) + " doesn't exist yet");
			}
			HorizontalPlane newPlane = initPlane(level);
			planes.put(level, newPlane);
			return newPlane;
		} else {
			return planes.get(level);
		}
	}

	public Rectangle asRectangle() {
		return rectangle;

	}

	public Collection<HorizontalPlane> getPlanes() {
		return planes.values();
	}
}
