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

public class BFS implements Planner {
	static BFS BFSPlanner;

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell start, CELLTYPE goalType) {

		// holds cell references which is used to backtrack for route plan
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = breadthFirstSearch(worldMap, start, goalType, prev);

		// If target is null, the search wasn't able to find desired goal type
		if (target == null)
			return null;

		// Returning a Stack<Cell> for the route plan
		return constructPlan(worldMap, target, prev);

	}

	private BFS() {

	}

	public static BFS getSingleInstance() {
		if (BFSPlanner == null)
			BFSPlanner = new BFS();
		return BFSPlanner;

	}

	// Offsets in each cardinal direction that helps to find neighbors, NESW,
	// {x,y}
	private final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

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
			CELLTYPE goalType) {
		// If searching for unexplored, add that type to list of neighbors.
		// Food/home searches do not check unexplored cells for efficiency
		boolean addUnexplored = (goalType == CELLTYPE.UNEXPLORED);

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
					neighbors.add(neighborCell); // only traversable cells
			}
		}
		return neighbors;
	}

	private Cell breadthFirstSearch(WorldMap worldMap, Cell startCell,
			CELLTYPE goalType, Hashtable<Cell, Cell> prev) {
		// Standard BFS algorithm
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

}