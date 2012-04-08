package vohra;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

import ants.Direction;

public class MapOps {

	static final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	static Hashtable<Cell, Integer> dist = new Hashtable<Cell, Integer>();

	public static Direction oppositeDir(Direction dir) {
		if (dir == null)
			MyAnt.debugPrint(2, "Why is Dir  Null");
		return Direction.values()[(dir.ordinal() + 2) % 4];
	}

	public static Stack<Cell> makePlan(WorldMap worldMap, Cell startCell,
			Cell.CELLTYPE type, Planner planner) {
		return planner.makePlan(worldMap, startCell, type);
	}

	

	public static LinkedList<Cell> listNeighbors(WorldMap worldMap, Cell cell,
			boolean includeUnexplored) {
		LinkedList<Cell> neighborsList = new LinkedList<Cell>();

		for (int i = 0; i < 4; i++) { // for each cardinal direction
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];

			Cell neighborCell = worldMap.getCell(xPos, yPos);

			if (neighborCell.getCellType() != Cell.CELLTYPE.WATER) {
				if (includeUnexplored)
					neighborsList.add(neighborCell);
				else if (neighborCell.getCellType() != Cell.CELLTYPE.UNEXPLORED)
					neighborsList.add(neighborCell);
			}
		}
		return neighborsList;
	}
}
