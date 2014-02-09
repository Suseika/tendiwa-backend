package org.tendiwa.core;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sun.nio.sctp.AssociationChangeNotification;
import org.apache.log4j.Logger;
import org.tendiwa.core.events.*;
import org.tendiwa.core.observation.Observable;
import org.tendiwa.core.player.PlayerModule;

import java.util.LinkedList;
import java.util.List;

@Singleton
public class Tendiwa extends Observable {
private static final Object clientWaitLock = new Object();
private static final String MODULES_CONF_FILE = "/modules.conf";
public static TendiwaClient CLIENT;
private static AssociationChangeNotification clientEventManager;
private static List<Class<? extends Module>> modulesCreatingWorlds;
private static Injector injector;
public final org.apache.log4j.Logger logger = Logger.getLogger("org/tendiwa");
public final Server SERVER = Server.SERVER;
private Thread SERVER_THREAD;

@Inject
public Tendiwa() {
	initEmitters();
}

public static Injector getInjector() {
	if (injector == null) {
		injector = Guice.createInjector(new TendiwaBackendModule(), new PlayerModule());
	}
	return injector;
}

public static Tendiwa newBackend() {
	return getInjector().getInstance(Tendiwa.class);
}

public static void main(String args[]) {
	if (args.length > 0 && args[0].equals("--ontology")) {
		// Ontology building utility
		String moduleDir = args[1];
		if (moduleDir == null) {
			throw new RuntimeException("Modules directory not provided");
		}
	} else {
	}
}

public static List<Class<? extends Module>> loadModules() {
	String[] modules = Module.getModulesFromConfig(MODULES_CONF_FILE);
	modulesCreatingWorlds = new LinkedList<>();
	for (String module : modules) {
//		Class<?> moduleClass = classLoader.loadClass("tendiwa.modules." + module);
//		if (!Module.class.isAssignableFrom(moduleClass)) {
//			logger.warn(moduleClass + " is not a module class");
//			continue;
//		}
//		if (WorldProvidingModule.class.isAssignableFrom(moduleClass)) {
//			modulesCreatingWorlds.add(moduleClass);
//		}
		modulesCreatingWorlds.add(new ScriptShell(module).getModuleClass());
	}
	if (modulesCreatingWorlds.size() == 0) {
		throw new RuntimeException("No world-creating modules provided");
	}
	return modulesCreatingWorlds;
}

public static Module getMainModule() {
	try {
		return (Module) modulesCreatingWorlds.iterator().next().newInstance();
	} catch (InstantiationException | IllegalAccessException e) {
		throw new RuntimeException(e);
	}
}

private void initEmitters() {
	createEventEmitter(EventWield.class);
	createEventEmitter(EventGetDamage.class);
	createEventEmitter(EventGetItem.class);
	createEventEmitter(EventLoseItem.class);
	createEventEmitter(EventFovChange.class);
	createEventEmitter(EventSelectPlayerCharacter.class);
	createEventEmitter(EventInitialTerrain.class);
	createEventEmitter(EventMoveToPlane.class);
	createEventEmitter(EventPutOn.class);
	createEventEmitter(EventTakeOff.class);
	createEventEmitter(EventWield.class);
	createEventEmitter(EventUnwield.class);
	createEventEmitter(EventMove.class);
	createEventEmitter(EventItemAppear.class);
	createEventEmitter(EventItemDisappear.class);
	createEventEmitter(EventSound.class);
	createEventEmitter(EventDie.class);
	createEventEmitter(EventAttack.class);
	createEventEmitter(EventAttack.class);
	createEventEmitter(EventProjectileFly.class);
}

public void start() {
	// Loading modules
	List<Class<? extends Module>> modulesCreatingWorlds = loadModules();
	WorldProvidingModule worldProvidingModule = (WorldProvidingModule) getInjector().getInstance(modulesCreatingWorlds.get(0));
	worldProvidingModule.createWorld();

	// Starting server
	SERVER_THREAD = new Thread(Server.SERVER);
	SERVER_THREAD.start();
}
}
