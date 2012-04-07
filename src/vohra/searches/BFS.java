package vohra.searches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import vohra.Cell;
import vohra.Knowledge;
import vohra.MapOps;
import vohra.Planner;
import vohra.Cell.TYPE;
import vohra.Knowledge.MODE;

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
			LinkedList<Cell> neighbors = MapOps.listNeighbors(knowledge, cell,
					checkUnexplored);

			if ((knowledge.getMode() == MODE.SCOUT)
					|| (knowledge.getMode() == Knowledge.MODE.EXPLORE))
				Collections.shuffle(neighbors);

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

	public Cell bfs(Knowledge knowledge, Cell target, Hashtable<Cell, Cell> prev) {
		boolean includeUnexplored = (target.getType() == Cell.TYPE.UNEXPLORED);
		// BFS Search
		HashSet<Cell> markedSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		Cell startCell = knowledge.getCurrCell();
		markedSet.add(startCell);
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell t = queue.remove();
			if (t == target)
				return t;
			LinkedList<Cell> neighbors = MapOps.listNeighbors(knowledge, t,
					includeUnexplored);

			if ((knowledge.getMode() == Knowledge.MODE.SCOUT)
					|| (knowledge.getMode() == Knowledge.MODE.EXPLORE))
				Collections.shuffle(neighbors, new Random(System.nanoTime()));

			for (Cell cell : neighbors) {
				if (!markedSet.contains(cell)) {
					markedSet.add(cell);
					queue.add(cell);
					prev.put(cell, t);
				}
			}
		}
		// goalType doesn't exist
		return null;
	}

}