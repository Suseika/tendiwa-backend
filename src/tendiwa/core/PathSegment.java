package tendiwa.core;

import tendiwa.core.LocationFeature;

public class PathSegment {
private Class<LocationFeature> type;

public Class<? extends LocationFeature> getType() {
	return type;
}
}