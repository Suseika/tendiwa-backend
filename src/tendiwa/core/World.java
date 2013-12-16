package tendiwa.core;

import java.util.Set;

import static tendiwa.core.DSL.worldBuilder;

public class World {

protected final int width;
protected final int height;
final HorizontalPlane defaultPlane;
private Character playerCharacter;
private TimeStream timeStream;

public World(int width, int height) {
	this.width = width;
	this.height = height;
	defaultPlane = new HorizontalPlane(width, height);
	defaultPlane.touchChunks(0, 0, width, height);
	this.timeStream = new TimeStream();
}

public static World create(WorldDrawer worldDrawer, int width, int height) {
	WorldRectangleBuilder builder = worldBuilder();
	worldDrawer.drawWorld(builder, width, height);
	World world = new World(width, height);
	builder.done();
	for (LocationPlace place : builder.rectanglesToPlaces.values()) {
		LocationDrawer locationDrawer = ResourcesRegistry.getLocationDrawerFor(place);
		locationDrawer.draw(
			new Location(world.defaultPlane, place.x, place.y, place.width, place.height),
			place
		);
	}
//	world.checkIfLocationPlacesFillAllWorld(builder);
	return world;
}

public HorizontalPlane getDefaultPlane() {
	return defaultPlane;
}

private void checkIfLocationPlacesFillAllWorld(WorldRectangleBuilder builder) {
	boolean filled[][] = new boolean[width][height];
	for (LocationPlace place : builder.rectanglesToPlaces.values()) {
		for (int x = place.x; x < place.width; x++) {
			for (int y = place.y; y < place.height; y++) {
				filled[x][y] = true;
			}
		}
	}
	for (int x = 0; x < width; x++) {
		for (int y = 0; y < height; y++) {
			if (!filled[x][y]) {
				throw new WorldException("Not the whole world was filled with locations: " + x + " " + y);
			}
		}
	}
}

public Character getPlayer() {
	assert playerCharacter != null;
	return playerCharacter;
}

public void setPlayerCharacter(Character playerCharacter) {
	this.playerCharacter = playerCharacter;
	timeStream.addPlayerCharacter(playerCharacter);
	playerCharacter.setTimeStream(timeStream);
}

public void placePlayerCharacter(Character player, int x, int y) {
	defaultPlane.placeCharacter(player, x, y);
}

public int getWidth() {
	return width;
}

public int getHeight() {
	return height;
}

public Character createCharacter(int x, int y, CharacterType type, String name) {
	NonPlayerCharacter character = new NonPlayerCharacter(defaultPlane, type, x, y, name);
	defaultPlane.addCharacter(character);
	timeStream.addNonPlayerCharacter(character);
	return character;

}

/**
 * Creates a new {@link Character}, makes it the player character.
 *
 * @param x
 * 	X coordinate of player in world coordinates. Will be shifted to the east if {x:y} is occupied by something
 * 	non-passable.
 * @param y
 * 	Y coordinate of player in world coordinates
 * @param type
 * 	Player's speices.
 * @param name
 * 	Name of a player.
 * @return A new Character which is set to be World's player character.
 * @throws RuntimeException
 * 	If the cell {x:y} was not passable and neither were all the cells from it till {width-1:y}
 */
public Character createPlayerCharacter(int x, int y, CharacterType type, String name) {
	playerCharacter = new Character(defaultPlane, type, x, y, name);
	try {
		while (defaultPlane.getPassability(playerCharacter.x, playerCharacter.y) == Passability.NO) {
			playerCharacter.x++;
		}
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new RuntimeException("Could not place player character because the whole world is non-passable");
	}
	defaultPlane.addCharacter(playerCharacter);
	timeStream.addPlayerCharacter(playerCharacter);
	Set<Chunk> chunks = defaultPlane.getChunksAroundCoordinate(x, y, Chunk.SIZE * 5);
	for (Chunk chunk : chunks) {
		timeStream.addChunk(chunk);
	}
	return playerCharacter;
}

public TimeStream getTimeStream() {
	return timeStream;
}
}
