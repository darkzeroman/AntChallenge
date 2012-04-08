package vohra;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import ants.Direction;

public class MapOps {

	static final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	static Hashtable<Cell, Integer> dist = new Hashtable<Cell, Integer>();

	public static Direction oppositeDir(Direction dir) {
		if (dir == null)
			MyAnt.debugPrint(2, "Why is Dir  Null");
		return Direction.values()[(dir.ordinal() + 2) % 4];
	}

	public static boolean makePlan(Knowledge knowledge, Cell.TYPE type,
			Planner planner) {
		return planner.makePlan(knowledge, type);
	}

	public static Cell bfs(Knowledge knowledge, Cell.TYPE goalType,
			Hashtable<Cell, Cell> prev) {
		// BFS Search
		HashSet<Cell> markSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		Cell startCell = knowledge.getCurrCell();
		markSet.add(startCell);
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell t = queue.remove();
			if (t.getType() == goalType)
				return t;
			LinkedList<Cell> neighbors = listNeighbors(knowledge, t,
					goalType == Cell.TYPE.UNEXPLORED);

			if ((knowledge.mode == Knowledge.MODE.SCOUT)
					|| (knowledge.mode == Knowledge.MODE.EXPLORE))
				Collections.shuffle(neighbors,
						new Random(System.currentTimeMillis()));

			for (Cell cell : neighbors) {
				if (!markSet.contains(cell)) {
					markSet.add(cell);
					queue.add(cell);
					prev.put(cell, t);
				}
			}
		}
		// goalType doesn't exist
		return null;
	}

	public static LinkedList<Cell> listNeighbors(Knowledge knowledge,
			Cell cell, boolean includeUnexplored) {
		LinkedList<Cell> neighborsList = new LinkedList<Cell>();

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
