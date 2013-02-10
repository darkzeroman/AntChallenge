package vohra.searches;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import vohra.Cell;
import vohra.Cell.CELLTYPE;
import vohra.Planner;
import vohra.WorldMap;

/**
 * Pretty much standard BFS algorithm. I'm leaving out anything obvious assuming
 * any one looking at this already knows BFS (or general implementation).
 */
public class BFS implements Planner {

	/** Reference to singleton object */
	static BFS BFSPlanner;

	public static BFS getSingleInstance() {
		if (BFSPlanner == null)
			BFSPlanner = new BFS();
		return BFSPlanner;

	}

	/** Offsets for cardinal direction that helps to find neighbors, NESW, {x,y} */
	private final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	private BFS() {
	}

	/**
	 * Standard BFS algorithm implementation, fills the prev hashtable with the
	 * previous references for a plan to goalType from the startCell
	 */
	private Cell breadthFirstSearch(WorldMap worldMap, Cell startCell, CELLTYPE goalType, Hashtable<Cell, Cell> prev) {

		// Using markedSet to avoid re-checking already visited nodes
		HashSet<Cell> markedSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		markedSet.add(startCell);
		queue.add(startCell);

		while (!queue.isEmpty()) {
			Cell cell = queue.remove();
			if (cell.getCellType() == goalType)
				return cell;
			LinkedList<Cell> neighbors = generateSuccessors(worldMap, cell, goalType);

			/*
			 * If looking for unexplored, shuffle list. Since the neighbors are
			 * always returned NESW, it's best to shuffle the list if ant wants
			 * to explore a random portion of the map.
			 */
			if (goalType == CELLTYPE.UNEXPLORED)
				Collections.shuffle(neighbors, new Random(System.currentTimeMillis()));

			for (Cell neighbor : neighbors)
				if (!markedSet.contains(neighbor)) {
					markedSet.add(neighbor);
					queue.add(neighbor);
					prev.put(neighbor, cell);
				}
		}
		// If reached here, goal type doesn't exist
		return null;
	}

	/** Reconstructs a plan to target by using the cells' prev references */
	private Stack<Cell> constructPlan(WorldMap worldMap, Cell target, Hashtable<Cell, Cell> prev) {

		// Starting from the target and backtracking to find the route plan
		Stack<Cell> newPlan = new Stack<Cell>();

		Cell cell = target;
		while (prev.containsKey(cell)) {
			newPlan.push(cell);
			cell = prev.get(cell);
		}
		return newPlan;
	}

	/** Generates successors for BFS with some logic */
	private LinkedList<Cell> generateSuccessors(WorldMap worldMap, Cell cell, CELLTYPE goalCellType) {

		// Food/home searches do not to check unexplored cells
		boolean addUnexplored = (goalCellType == CELLTYPE.UNEXPLORED);

		LinkedList<Cell> neighbors = new LinkedList<Cell>();

		// For each cardinal direction find the neighbor
		for (int i = 0; i < offsets.length; i++) {
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];
			Cell neighborCell = worldMap.getCell(xPos, yPos);

			// Waters are never needed, so if water, move on
			if (neighborCell.getCellType() == CELLTYPE.WATER) {
				continue;
			}

			if (addUnexplored) {
				neighbors.add(neighborCell);
			} else if (neighborCell.getCellType() != CELLTYPE.UNEXPLORED) {
				neighbors.add(neighborCell);
			}

		}
		return neighbors;
	}

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell start, CELLTYPE goalCellType) {

		// holds cell references which is used to backtrack for route plan
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = breadthFirstSearch(worldMap, start, goalCellType, prev);

		// If target is null, the search wasn't able to find desired goal type
		if (target == null)
			return null;

		// Returning a Stack<Cell> for the route plan
		return constructPlan(worldMap, target, prev);

	}
}