package org.tendiwa.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.Set;

/**
 * The purpose of HorizontalPlane is to store and access chunks of terrain located on the same absolute height in the
 * world.
 * <p>
 * A HorizontalPlane is a single storey of the world, much like a single level of a dungeon in traditional rogue-likes.
 * It is a potentially infinite Cartesian plane with integral coordinates divided by square {@link Chunk}s. No actions
 * can be inter-planar (you can't shoot an arrow from one plane to another), however characters can move from one plane
 * to another using stairs, shafts, teleportation or other means; and also certain interactions can be interplanar, for
 * example, sound waves spreading.
 * <p>
 * Not to be mistaken with {@link TimeStream}
 */
public class HorizontalPlane {
	protected final Chunk[][] chunks;
	private final int width;
	private final int height;
	private final int level;
	public HorizontalPlane upperPlane;
	public HorizontalPlane lowerPlane;
	private int numberOfChunks = 0;
	private World world;

	/**
	 * @param width
	 * 	Width of plane in cells.
	 * @param height
	 * 	Height of plane in cells.
	 * @param world
	 * 	World in which this HorizontalPlane resides.
	 */

	HorizontalPlane(int width, int height, World world, int level) {
		this.world = world;
		chunks = new Chunk[width / Chunk.SIZE + 1][height / Chunk.SIZE + 1];
		this.width = width;
		this.height = height;
		this.level = level;
	}

	public Chunk loadChunk(int x, int y) {
		int chunkX = (x - x % Chunk.SIZE) / Chunk.SIZE;
		int chunkY = (y - y % Chunk.SIZE) / Chunk.SIZE;
		if (chunks[chunkX][chunkY] != null) {
			throw new RuntimeException("Trying to load a chunk that is already loaded.");
		}
		return chunks[chunkX][chunkY] = loadChunkFromFilesystem(chunkX, chunkY);
	}

	private Chunk loadChunkFromFilesystem(int chunkX, int chunkY) {
		throw new UnsupportedOperationException();
	}

	public void touchChunk(int x, int y) {
		if (!hasChunk(x, y)) {
			chunks[x / Chunk.SIZE][y / Chunk.SIZE] = new Chunk(this, x, y);
		}
	}

	public void touchChunks(int x, int y, int width, int height) {
		for (int j = getChunkRoundedCoord(y); j <= y + height; j += Chunk.SIZE) {
			for (int i = getChunkRoundedCoord(x); i <= x + width; i += Chunk.SIZE) {
				touchChunk(i, j);
			}
		}
	}

	/**
	 * Returns the chunk that contains cell with absolute coordinates x:y. Loads it if it was not loaded.
	 *
	 * @param x
	 * 	Absolute x coordinate of a cell.
	 * @param y
	 * 	Absolute y coordinate of a cell.
	 * @return Chunk that contains a cell with given absolute coordinates.
	 */
	public Chunk getChunkWithCell(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			throw new ArrayIndexOutOfBoundsException("Point " + x + ":" + y + " is not inside plane of " + width + "x" + height + " cells large.");
		}
		int chunkX = (x - x % Chunk.SIZE) / Chunk.SIZE;
		int chunkY = (y - y % Chunk.SIZE) / Chunk.SIZE;
		Chunk chunk = chunks[chunkX][chunkY];
		return chunk == null ? loadChunk(x, y) : chunk;
	}

	public boolean hasChunk(int x, int y) {
		return chunks[(x - x % Chunk.SIZE) / Chunk.SIZE][(y - y % Chunk.SIZE) / Chunk.SIZE] != null;
	}

	/**
	 * Round a coordinate (x or y, works equal) down to the nearest value in which may be a corner of a chunk.
	 *
	 * @param coord
	 * 	x or y coordinate.
	 * @return Rounded coordinate value.
	 */
	public int getChunkRoundedCoord(int coord) {
		return (coord < 0) ? coord - ((coord % Chunk.SIZE == 0) ? 0
			: Chunk.SIZE) - coord % Chunk.SIZE : coord - coord % Chunk.SIZE;
	}

	public FloorType getFloor(int x, int y) {
		return getChunkWithCell(x, y).getFloor(x, y);
	}

	public Passability getPassability(int x, int y) {
		if (getChunkWithCell(x, y).getCharacter(x, y) != null) {
			return Passability.SEE;
		} else if (getChunkWithCell(x, y).getWall(x, y) == null) {
			return Passability.FREE;
		} else {
			return Passability.NO;
		}
	}

	public void addItem(ItemPile pile, int x, int y) {
		Chunk chunk = getChunkWithCell(x, y);
		chunk.addItem(pile, x, y);
	}

	public void addItem(UniqueItem item, int x, int y) {
		Chunk chunk = getChunkWithCell(x, y);
		chunk.addItem(item, x, y);
	}

	public void removeItem(ItemPile pile, int x, int y) {
		Chunk chunk = getChunkWithCell(x, y);
		chunk.removeItem(pile, x, y);
	}

	public void removeItem(UniqueItem item, int x, int y) {
		Chunk chunk = getChunkWithCell(x, y);
		chunk.removeItem(item, x, y);
	}

	public ItemCollection getItems(int x, int y) {
		Chunk chunk = getChunkWithCell(x, y);
		return chunk.getItems(x, y);
	}

	public void placeCharacter(Character character, int x, int y) {
		Chunk chunk = getChunkWithCell(x, y);
		chunk.addCharacter(character);
	}

	public void removeObject(int x, int y) {
		Chunk chunkWithCell = getChunkWithCell(x, y);
		chunkWithCell.removeObject(x - chunkWithCell.x, y - chunkWithCell.y);
	}

	public void placeFloor(FloorType floor, int x, int y) {
		getChunkWithCell(x, y).setFloor(floor, x, y);
	}

	public Character getCharacter(int x, int y) {

		return getChunkWithCell(x, y).getCharacter(x, y);
	}

	public void removeCharacter(Character character) {
		getChunkWithCell(character.x, character.y).removeCharacter(character);
	}

	public void addCharacter(Character character) {
		getChunkWithCell(character.x, character.y).addCharacter(character);
	}

	public GameObject getGameObject(int x, int y) {
		return getChunkWithCell(x, y).getGameObject(x, y);
	}

	public void placeWall(WallType wall, int x, int y) {
		getChunkWithCell(x, y).setWall(wall, x, y);
	}

	public boolean hasAnyItems(int x, int y) {
		return getChunkWithCell(x, y).hasAnyItems(x, y);
	}

	public boolean hasCharacter(int x, int y) {
		return getChunkWithCell(x, y).hasCharacter(x, y);
	}

	public boolean hasObject(int x, int y) {
		return getChunkWithCell(x, y).hasObject(x, y);
	}

	public void placeObject(GameObject gameObject, int x, int y) {
		getChunkWithCell(x, y).setObject(gameObject, x, y);
	}

	public Set<Chunk> getChunksAroundCoordinate(int x, int y, int squareSide) {
		int startChunkX = Math.max(0, getChunkRoundedCoord(x - squareSide / 2));
		int startChunkY = Math.max(0, getChunkRoundedCoord(y - squareSide / 2));
		int endChunkX = Math.min((chunks.length - 1) * Chunk.SIZE, getChunkRoundedCoord(x + squareSide / 2));
		int endChunkY = Math.min((chunks[0].length - 1) * Chunk.SIZE, getChunkRoundedCoord(y + squareSide / 2));
		ImmutableSet.Builder<Chunk> builder = ImmutableSet.builder();
		for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
			for (int chunkY = startChunkY; chunkY < endChunkY; chunkY++) {
				builder.add(getChunkAt(chunkX, chunkY));
			}
		}
		return builder.build();
	}

	private Chunk getChunkAt(int chunkX, int chunkY) {
		return chunks[chunkX / Chunk.SIZE][chunkY / Chunk.SIZE];
	}

	public World getWorld() {
		return world;
	}

	public int getLevel() {
		return level;
	}

	public boolean hasWall(int x, int y) {
		GameObject wall = getChunkWithCell(x, y).getWall(x, y);
		return wall != null && wall instanceof WallType;
	}

	public BorderObject setBorderObject(int x, int y, CardinalDirection side, BorderObjectType type) {
		return getChunkWithCell(x, y).setBorderObject(x, y, side, type);
	}

	public BorderObject getBorderObject(int x, int y, CardinalDirection side) {
		return getChunkWithCell(x, y).getBorderObject(x, y, side);
	}

	public BorderObject getBorderObject(Border border) {
		return getChunkWithCell(border.x, border.y).getBorderObject(border);
	}

	public boolean hasBorderObject(Border border) {
		return getChunkWithCell(border.x, border.y).hasBorderObject(border.x, border.y, border.side);
	}

	public boolean hasBorderObject(int x, int y, CardinalDirection side) {
		assert side != null;
		if (side != Directions.N && side != Directions.W) {
			if (side == Directions.E) {
				side = Directions.W;
				x += 1;
			} else {
				assert side == Directions.S;
				side = Directions.N;
				y += 1;
			}
		}
		if (x == world.getWidth() && side == Directions.W || y == world.getHeight() && side == Directions.N) {
//		throw new IllegalArgumentException("South side of southest cell row and east side of eastest cell column can't have border objects");
			return false;
		}
		return getChunkWithCell(x, y).hasBorderObject(x, y, side);
	}

	public boolean containsCell(int x, int y) {
		return x >= 0 && y >= 0 && x < world.getWidth() && y < world.getHeight();
	}

	public Iterable<Chunk> getChunks() {
		final Chunk[] chunks1 = new Chunk[chunks.length * chunks[0].length];
		int i = 0;
		for (Chunk[] row : chunks) {
			for (Chunk chunk : row) {
				chunks1[i++] = chunk;
			}
		}
		return new Iterable<Chunk>() {
			@Override
			public Iterator<Chunk> iterator() {
				return Iterators.forArray(chunks1);
			}
		};
	}

	public WallType wall(int x, int y) {
		GameObject gameObject = getGameObject(x, y);
		if (gameObject instanceof WallType) {
			return (WallType) gameObject;
		}
		throw new RuntimeException("No wall in cell " + x + " " + y);
	}
}
