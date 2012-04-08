package vohra.searches;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import vohra.Cell;
import vohra.Cell.TYPE;
import vohra.Planner;
import vohra.WorldMap;

public class BFS extends Planner {

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell start,
			Cell.TYPE goalType) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = bfs(worldMap, start, goalType, prev);
		if (target == null)
			return null;
		Stack<Cell> newPlan = constructPath(worldMap, target, prev);
		return newPlan;

	}

	public Cell bfs(WorldMap worldMap, Cell startCell, Cell.TYPE goalType,
			Hashtable<Cell, Cell> prev) {
		boolean checkUnexplored = (goalType == Cell.TYPE.UNEXPLORED);

		HashSet<Cell> markedSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		markedSet.add(startCell);
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell cell = queue.remove();
			if (cell.getType() == goalType)
				return cell;
			LinkedList<Cell> neighbors = this.listNeighbors(worldMap, cell,
					checkUnexplored, goalType);

			if (goalType == Cell.TYPE.UNEXPLORED)
				Collections.shuffle(neighbors, new Random(System.nanoTime()));

			for (Cell neighbor : neighbors) {
				if (!markedSet.contains(neighbor)) {
					markedSet.add(neighbor);
					queue.add(neighbor);
					prev.put(neighbor, cell);
				}
			}
		}
		// goalType doesn't exist
		return null;
	}

	public LinkedList<Cell> listNeighbors(WorldMap worldMap, Cell cell,
			boolean includeUnexplored, TYPE goalType) {
		LinkedList<Cell> neighborsList = new LinkedList<Cell>();

		int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

		for (int i = 0; i < 4; i++) { // for each cardinal direction
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];

			Cell neighborCell = worldMap.getCell(xPos, yPos);

			if (neighborCell.getType() != Cell.TYPE.WATER) {
				if (includeUnexplored)
					neighborsList.add(neighborCell);
				else if (neighborCell.getType() != Cell.TYPE.UNEXPLORED)
					neighborsList.add(neighborCell);
			}
		}
		return neighborsList;
	}
}