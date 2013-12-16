package tendiwa.core;

import org.tendiwa.events.*;

/**
 * Event manager that does nothing on all events just telling then backend to proceed. Used to handle events before any
 * client is attached to a backend so events can be issued (for example, to do actions right in a {@link Module} after
 * world generation but before actual client attachment).
 */
class DummyClient implements TendiwaClient {
TendiwaClientEventManager manager = new TendiwaClientEventManager() {
	@Override
	public void event(EventMove e) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventSay eventSay) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventFovChange eventFovChange) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventInitialTerrain eventInitialTerrain) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventItemDisappear eventItemDisappear) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventGetItem eventGetItem) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventLoseItem eventLoseItem) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventItemAppear eventItemAppear) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventPutOn eventPutOn) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventWield eventWield) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventTakeOff eventTakeOff) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventUnwield eventUnwield) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventProjectileFly eventProjectileFly) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventSound eventSound) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventExplosion eventExplosion) {
		Tendiwa.signalAnimationCompleted();
	}

	@Override
	public void event(EventGetDamage eventGetDamage) {
		Tendiwa.signalAnimationCompleted();
	}
};

@Override
public void startup() {

}

@Override
public TendiwaClientEventManager getEventManager() {
	return manager;
}

@Override
public boolean isAnimationCompleted() {
	return true;
}
}
