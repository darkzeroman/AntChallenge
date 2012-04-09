package vohra;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

import vohra.Cell.CELLTYPE;
import vohra.searches.BFS;

public abstract class Planner {
	public static enum SEARCHTYPE {
		BFS, // Djikstra, // AStar
	}

	private static Planner planner;

	public static Planner getSingleInstance(SEARCHTYPE searchType) {
		switch (searchType) {
		case BFS:
			planner = new BFS();
			break;
		default:
			throw new IllegalArgumentException("Invalid search type");
		}
		return planner;

	}

	private final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 },
			{ -1, 0 } };

	public Stack<Cell> constructPlan(WorldMap worldMap, Cell target,
			Hashtable<Cell, Cell> prev) {
		Stack<Cell> newPlan = new Stack<Cell>();
		Cell u = target;
		while (prev.containsKey(u)) {
			newPlan.push(u);
			u = prev.get(u);
		}
		return newPlan;
	}

	public abstract Stack<Cell> makePlan(WorldMap worldMap, Cell startCell,
			CELLTYPE goalType);

	protected LinkedList<Cell> listNeighbors(WorldMap worldMap, Cell cell,
			CELLTYPE goalType) {
		// If searching for unexplored, add that type to list of neighbors
		// food/home searches do not track unexplored cells for efficiency
		boolean addUnexplored = (goalType == CELLTYPE.UNEXPLORED);
		LinkedList<Cell> neighbors = new LinkedList<Cell>();

		// for each cardinal direction find the neighbor
		for (int i = 0; i < 4; i++) {
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];
			Cell neighborCell = worldMap.getCell(xPos, yPos);

			// Waters are not needed, so if water, move on
			if (neighborCell.getCellType() != CELLTYPE.WATER) {
				if (addUnexplored)
					neighbors.add(neighborCell);
				else if (neighborCell.getCellType() != CELLTYPE.UNEXPLORED)
					neighbors.add(neighborCell);
			}
		}
		return neighbors;
	}

}
