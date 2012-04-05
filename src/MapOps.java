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

	public static boolean planRoute(MyAnt ant, Cell.CellType type) {

		return false;
	}

	public static boolean makeRoute(MyAnt ant, Cell.CellType type, String error) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();
		Cell target = MapOps.bfs(ant, type, prev);
		MyAnt.debugPrint(1, "target: " + target);
		if (target == null) // try to make a path if it exists
			return false;
		return makeRoute(ant, target, error);
	}

	public static boolean makeRoute(MyAnt ant, Cell target, String error) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();
		printPath(ant);
		MapOps.djikstra(ant, target, prev);
		constructPath(ant, target, prev);
		if (ant.getCurrRoute().size() > 0)
			return true;
		else
			return false;
	}

	public static boolean newMakeRoute(MyAnt ant, Cell.CellType type,
			String error) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = MapOps.bfs(ant, type, prev);
		if (target == null)
			return false;
		constructPath(ant, target, prev);
		if (ant.getCurrRoute().size() > 0)
			return true;
		else
			return false;
	}

	public static void constructPath(MyAnt ant, Cell target,
			Hashtable<Cell, Cell> prev) {
		ant.getCurrRoute().clear();
		Cell u = target;
		while (prev.containsKey(u)) {
			ant.getCurrRoute().push(u);
			u = prev.get(u);
		}
	}

	public static Cell bfs(MyAnt ant, Cell.CellType goalType,
			Hashtable<Cell, Cell> prev) {
		// BFS Search
		HashSet<Cell> markSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		Cell startCell = ant.getCurrCell();
		markSet.add(startCell);
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell t = queue.remove();
			if (t.getType() == goalType)
				return t;
			ArrayList<Cell> neighbors = findNeighbors(ant, t,
					goalType == Cell.CellType.UNEXPLORED, null);

			if ((ant.getMode() == MyAnt.Mode.SCOUT)
					|| (ant.getMode() == MyAnt.Mode.EXPLORE))
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

	public static void djikstra(MyAnt ant, Cell target,
			Hashtable<Cell, Cell> prev) {
		boolean includeUnexplored = false;
		if (target.getType() == Cell.CellType.UNEXPLORED)
			includeUnexplored = true;

		MyAnt.debugPrint(1, "Searching Path:");
		MyAnt.debugPrint(1, ant.toString() + " Going to: " + target.getX()
				+ " " + target.getY());

		if (target.getX() == ant.getLocX() && target.getY() == ant.getLocY()) {
			MyAnt.debugPrint(1, "Sitting on top of target");
			return;
		}
		PriorityQueue<Cell> pq = ant.prepareForSearch(includeUnexplored);
		MyAnt.debugPrint(1, "PQ: " + pq.size());
		int count = 0;

		while (!pq.isEmpty()) {
			count++;
			Cell u = pq.peek();
			if (u.dist == Integer.MAX_VALUE) {
				MyAnt.debugPrint(1, "exiting after: " + count);
				break; // nothing past here is reachable

			}
			u = pq.poll();
			if (u == target) // reached target, can end
				break;
			ArrayList<Cell> al = findNeighbors(ant, u, includeUnexplored, pq);
			for (Cell cell : al) {
				int alt = u.dist + 2;
				if (cell.getNumAnts() > 0) {
					MyAnt.debugPrint(1, "has ants!");
					alt--;
				}

				if (alt < cell.dist) {
					cell.dist = alt;
					prev.put(cell, u);
					if (pq.remove(cell))
						pq.add(cell);
					else
						throw new Error("Can't find element in PQ");

				}
			}
		}
		MyAnt.debugPrint(1, "TARGET COST: " + target.dist);
		pq.clear();
		// constructing path
		ant.getCurrRoute().clear();
		Cell u = target;
		while (prev.containsKey(u)) {
			ant.getCurrRoute().push(u);
			u = prev.get(u);
		}

	}

	public static ArrayList<Cell> findNeighbors(MyAnt ant, Cell cell,
			boolean includeUnexplored, PriorityQueue<Cell> pq) {
		ArrayList<Cell> list = new ArrayList<Cell>();
		for (int i = 0; i < 4; i++) { // for each cardinal direction
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];
			// exit if the requested cell is out of bounds

			Cell neighborCell = ant.getCell(xPos, yPos);

			if (pq == null) { // for BFS search
				if (includeUnexplored
						&& neighborCell.getType() != Cell.CellType.WALL)
					list.add(neighborCell);
				else if (!includeUnexplored
						&& (neighborCell.getType() != Cell.CellType.UNEXPLORED)
						&& (neighborCell.getType() != Cell.CellType.WALL))
					list.add(neighborCell);
			} else if (pq != null) { // for Djikstra search
				if (includeUnexplored
						&& neighborCell.getType() != Cell.CellType.WALL
						&& pq.contains(neighborCell))
					list.add(neighborCell);
				else if (!includeUnexplored
						&& ((neighborCell.getType() != Cell.CellType.UNEXPLORED) && (neighborCell
								.getType() != Cell.CellType.WALL))
						&& (pq.contains(neighborCell)))
					list.add(neighborCell);
			}

		}
		return list;
	}

	public static void main(String[] args) {

	}

	public static Direction oppositeDir(Direction dir) {
		if (dir == null) {
			MyAnt.debugPrint(2, "Why is Dir  Null");
		}
		return Direction.values()[(dir.ordinal() + 2) % 4];
	}

	public static void printPath(MyAnt ant) {
		Stack<Cell> currRoute = ant.getCurrRoute();
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		Cell old = ant.getCell(ant.getLocX(), ant.getLocY());
		for (int i = 0; i < currRoute.size(); i++) {
			MyAnt.debugPrint(1, old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}

}
