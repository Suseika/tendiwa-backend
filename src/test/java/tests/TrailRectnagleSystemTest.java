package tests;

import org.junit.Test;
import org.tendiwa.core.FuckingTrailRectangleSystem;
import org.tendiwa.core.meta.BasicRange;

import java.awt.Point;

public class TrailRectnagleSystemTest {

	@Test
	public void test() {
		int numberOfTests = 100;
		for (int i = 0; i < numberOfTests; i++) {
			FuckingTrailRectangleSystem trs = new FuckingTrailRectangleSystem(10, new BasicRange(1, 15), new Point(12, 18))
				.buildToPoint(new Point(212, 32));
		}
	}
}
