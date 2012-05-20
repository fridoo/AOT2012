package de.dailab.gridworld.ontology;

public enum Direction {

	NORTH(0, -1), NORTHEAST(1, -1), EAST(1, 0), SOUTHEAST(1, 1), SOUTH(0, 1), SOUTHWEST(-1, 1), WEST(-1, 0), NORTHWEST(-1, -1);

	public final int dx;
	public final int dy;

	private Direction(int x, int y) {
		dx = x;
		dy = y;
	}
	
	public static Direction getDirection(int dx, int dy) {
		if (dx > 0) {
			if (dy <  0) return NORTHEAST;
			if (dy == 0) return     EAST;
			if (dy  > 0) return SOUTHEAST;
		}
		if (dx == 0) {
			if (dy <  0) return     NORTH;
			if (dy  > 0) return     SOUTH;
		}
		if (dx < 0) {
			if (dy <  0) return NORTHWEST;
			if (dy == 0) return      WEST;
			if (dy  > 0) return SOUTHWEST;
		}
		System.err.println("Illegal direction: " + dx + ", " + dy);
		return null;
	}

}
