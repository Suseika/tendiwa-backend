package tendiwa.buildings;

import tendiwa.core.Building;
import tendiwa.core.StaticData;
import tendiwa.core.meta.Side;
import tendiwa.core.terrain.settlements.BuildingPlace;

public class TestBuilding extends Building {
	protected TestBuilding(BuildingPlace bp, Side side) {
		super(bp, side);
	}
	public static final long serialVersionUID = 346347;
	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();
		
		getTerrainModifier(4);
		buildBasis(wallGreyStone);
		for (Side side : doorSides) {
			placeFrontDoor(side);
		}
	}
	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		// TODO Auto-generated method stub
		return true;
	}
}