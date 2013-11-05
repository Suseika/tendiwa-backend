package tendiwa.core;

public interface TendiwaClient {
/**
 * Called by {@link Tendiwa} on game initialization. Client is initialized after the server is initialized (world loaded
 * and so on).
 * @see Tendiwa#main(String[])
 */

public void startup();

/**
 * Returns an object that manages events received from server.
 *
 * @return Event manager.
 * @see ServerEvent
 */
TendiwaClientEventManager getEventManager();
}