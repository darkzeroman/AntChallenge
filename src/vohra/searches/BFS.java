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
 * 
 */
public class BFS implements Planner {
	/**
	 * Reference to singleton object
	 */
	static BFS BFSPlanner;

	/**
	 * Returns singleton for of BFS, design decision is explained in README
	 */
	public static BFS getSingleInstance() {
		if (BFSPlanner == null)
			BFSPlanner = new BFS();
		return BFSPlanner;

	}

	// Offsets in each cardinal direction that helps to find neighbors, NESW,
	// {x,y}
	private final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	private BFS() {
	}

	/**
	 * Standard BFS algorithm implementation, fills the "prev" hashtable of the
	 * previous references for a plan to the desired goal type from the
	 * source/start cell
	 */
	private Cell breadthFirstSearch(WorldMap worldMap, Cell startCell,
			CELLTYPE goalType, Hashtable<Cell, Cell> prev) {

		// Using markedSet to avoid re-checking already visited nodes
		HashSet<Cell> markedSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		markedSet.add(startCell);
		queue.add(startCell);

		while (!queue.isEmpty()) {
			Cell cell = queue.remove();
			if (cell.getCellType() == goalType)
				return cell;
			LinkedList<Cell> neighbors = listNeighbors(worldMap, cell, goalType);

			// if looking for unexplored, shuffle list
			// Since the neighbors are always returned NESW, it's best to
			// shuffle the list if ant wants to explore a random portion of the
			// map. Could have used a seed but decided not to
			if (goalType == CELLTYPE.UNEXPLORED)
				Collections.shuffle(neighbors,
						new Random(System.currentTimeMillis()));

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

	/**
	 * Reconstructs a plan to desired target by using the prev references for
	 * the cells
	 */
	public Stack<Cell> constructPlan(WorldMap worldMap, Cell target,
			Hashtable<Cell, Cell> prev) {

		// Starting from the target and backtracking to find the route plan
		Stack<Cell> newPlan = new Stack<Cell>();

		Cell cell = target;
		while (prev.containsKey(cell)) {
			newPlan.push(cell);
			cell = prev.get(cell);
		}
		return newPlan;
	}

	protected LinkedList<Cell> listNeighbors(WorldMap worldMap, Cell cell,
			CELLTYPE goalCellType) {
		// If searching for unexplored, need to add make sure the list of
		// neighbors includes unexplored

		// Food/home searches do not check unexplored cells for efficiency
		boolean addUnexplored = (goalCellType == CELLTYPE.UNEXPLORED);

		LinkedList<Cell> neighbors = new LinkedList<Cell>();

		// for each cardinal direction find the neighbor
		for (int i = 0; i < 4; i++) {
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];
			Cell neighborCell = worldMap.getCell(xPos, yPos);

			// Waters are never needed, so if water, move on
			if (neighborCell.getCellType() != CELLTYPE.WATER) {

				if (addUnexplored)
					neighbors.add(neighborCell);

				else if (neighborCell.getCellType() != CELLTYPE.UNEXPLORED)
					neighbors.add(neighborCell);
			}
		}
		return neighbors;
	}

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell start,
			CELLTYPE goalCellType) {

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