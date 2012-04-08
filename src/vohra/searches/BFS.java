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

public class BFS extends Planner {

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell start, CELLTYPE goalType) {

		// holds cell references which is used to backtrack for route plan
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = bfs(worldMap, start, goalType, prev);
		// If target is null, the search wasn't able to find desired goal type
		if (target == null)
			return null;
		// Returning a Stack<Cell> for the route plan
		return constructPlan(worldMap, target, prev);

	}

	public Cell bfs(WorldMap worldMap, Cell startCell, CELLTYPE goalType,
			Hashtable<Cell, Cell> prev) {
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
			LinkedList<Cell> neighbors = this.listNeighbors(worldMap, cell,
					goalType);

			// if looking for unexplored, shuffle list
			if (goalType == CELLTYPE.UNEXPLORED)
				Collections.shuffle(neighbors, new Random(System.nanoTime()));

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

	public LinkedList<Cell> listNeighbors(WorldMap worldMap, Cell cell,
			CELLTYPE goalType) {
		// If searching for unexplored, must be sure to include that type
		// otherwise, food/home searches do not track unexplored for efficiency
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