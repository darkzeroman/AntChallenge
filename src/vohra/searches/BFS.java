package vohra.searches;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import vohra.Cell;
import vohra.Cell.TYPE;
import vohra.Knowledge;
import vohra.Knowledge.MODE;
import vohra.Planner;

public class BFS extends Planner {

	@Override
	// public static boolean makeRoute(Knowledge knowledge, Cell.TYPE type,
	public boolean makePlan(Knowledge knowledge, Cell.TYPE type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = bfs(knowledge, type, prev);
		if (target == null)
			return false;
		constructPath(knowledge, target, prev);
		if (knowledge.getCurrPlan().size() > 0)
			return true;
		else
			return false;
	}

	public Cell bfs(Knowledge knowledge, Cell.TYPE goalType,
			Hashtable<Cell, Cell> prev) {
		boolean checkUnexplored = (goalType == Cell.TYPE.UNEXPLORED);

		HashSet<Cell> markedSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		Cell startCell = knowledge.getCurrCell();
		markedSet.add(startCell);
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell cell = queue.remove();
			if (cell.getType() == goalType)
				return cell;
			LinkedList<Cell> neighbors = this.listNeighbors(knowledge, cell,
					checkUnexplored, goalType);

			if ((knowledge.getMode() == MODE.SCOUT)
					|| (knowledge.getMode() == Knowledge.MODE.EXPLORE))
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

	public LinkedList<Cell> listNeighbors(Knowledge knowledge, Cell cell,
			boolean includeUnexplored, TYPE goalType) {
		LinkedList<Cell> neighborsList = new LinkedList<Cell>();

		int[] N = { 0, 1 };
		int[] E = { 1, 0 };
		int[] S = { 0, -1 };
		int[] W = { -1, 0 };
		int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

		// if (goalType == Cell.TYPE.HOME) {
		// int x = knowledge.x;
		// int y = knowledge.y;
		// if (y > 0 && x > 0) {
		// offsets = new int[][] { E, N, W, S };
		// } else if (y > 0 && x < 0)
		// offsets = new int[][] { N, W, S, E };
		// else if (y < 0 && x > 0)
		// offsets = new int[][] { S, E, N, W };
		// else
		// offsets = new int[][] { W, S, E, N };
		//
		// }

		for (int i = 0; i < 4; i++) { // for each cardinal direction
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];

			Cell neighborCell = knowledge.getCell(xPos, yPos);

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