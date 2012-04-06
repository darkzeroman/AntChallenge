package vohra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;

import ants.Direction;

public class MapOps {
	static final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
	static Hashtable<Cell, Integer> dist = new Hashtable<Cell, Integer>();

	public static boolean planRoute(Knowledge knowledge, Cell.CellType type,
			RoutePlanner routePlanner) {
		return routePlanner.makeRoute(knowledge, type);

	}

	public static boolean planRoute(Knowledge knowledge, Cell target,
			RoutePlanner routePlanner) {
		return routePlanner.makeRoute(knowledge, target);

	}

	public static Direction oppositeDir(Direction dir) {
		if (dir == null) {
			MyAnt.debugPrint(2, "Why is Dir  Null");
		}
		return Direction.values()[(dir.ordinal() + 2) % 4];
	}

	public static Cell bfs(Knowledge knowledge, Cell.CellType goalType,
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
			ArrayList<Cell> neighbors = findNeighbors(knowledge, t,
					goalType == Cell.CellType.UNEXPLORED, null);

			if ((knowledge.getMode() == Knowledge.Mode.SCOUT)
					|| (knowledge.getMode() == Knowledge.Mode.EXPLORE))
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

	public static ArrayList<Cell> findNeighbors(Knowledge knowledge, Cell cell,
			boolean includeUnexplored, PriorityQueue<Cell> pq) {
		ArrayList<Cell> list = new ArrayList<Cell>();
		for (int i = 0; i < 4; i++) { // for each cardinal direction
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];
			// exit if the requested cell is out of bounds

			Cell neighborCell = knowledge.get(xPos, yPos);

			if (pq == null) { // for BFS search
				if (includeUnexplored
						&& neighborCell.getType() != Cell.CellType.WATER)
					list.add(neighborCell);
				else if (!includeUnexplored
						&& (neighborCell.getType() != Cell.CellType.UNEXPLORED)
						&& (neighborCell.getType() != Cell.CellType.WATER))
					list.add(neighborCell);
			} else if (pq != null) { // for Djikstra search
				if (includeUnexplored
						&& neighborCell.getType() != Cell.CellType.WATER
						&& pq.contains(neighborCell))
					list.add(neighborCell);
				else if (!includeUnexplored
						&& ((neighborCell.getType() != Cell.CellType.UNEXPLORED) && (neighborCell
								.getType() != Cell.CellType.WATER))
						&& (pq.contains(neighborCell)))
					list.add(neighborCell);
			}

		}
		return list;
	}

	public static void main(String[] args) {

	}

}
