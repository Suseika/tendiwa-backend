package org.tendiwa.drawing.extensions;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.apache.log4j.Logger;

import java.util.Random;

public class DrawingModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(TimeProfiler.class)
			.to(PieChartTimeProfiler.class)
			.in(Scopes.SINGLETON);
		bind(Logger.class)
			.annotatedWith(Names.named("imageInfoLogger"))
			.toInstance(Logger.getLogger("imageInfoLogger"));
		bind(Random.class)
			.annotatedWith(Names.named("genesis"))
			.toInstance(new Random(100));
	}
}
