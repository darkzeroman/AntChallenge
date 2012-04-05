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

	public static boolean makeRoute(Knowledge knowledge, Cell.CellType type,
			String error) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();
		Cell target = MapOps.bfs(knowledge, type, prev);
		MyAnt.debugPrint(1, "target: " + target);
		if (target == null) // try to make a path if it exists
			return false;
		return makeRoute(knowledge, target, error);
	}

	public static boolean makeRoute(Knowledge knowledge, Cell target,
			String error) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();
		printPath(knowledge);
		MapOps.djikstra(knowledge, target, prev);
		constructPath(knowledge, target, prev);
		if (knowledge.getCurrRoute().size() > 0)
			return true;
		else
			return false;
	}

	public static boolean newMakeRoute(Knowledge knowledge, Cell.CellType type,
			String error) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = MapOps.bfs(knowledge, type, prev);
		if (target == null)
			return false;
		constructPath(knowledge, target, prev);
		if (knowledge.getCurrRoute().size() > 0)
			return true;
		else
			return false;
	}

	public static void constructPath(Knowledge knowledge, Cell target,
			Hashtable<Cell, Cell> prev) {
		knowledge.getCurrRoute().clear();
		Cell u = target;
		while (prev.containsKey(u)) {
			knowledge.getCurrRoute().push(u);
			u = prev.get(u);
		}
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

	public static void djikstra(Knowledge knowledge, Cell target,
			Hashtable<Cell, Cell> prev) {
		boolean includeUnexplored = false;
		if (target.getType() == Cell.CellType.UNEXPLORED)
			includeUnexplored = true;

		MyAnt.debugPrint(1, "Searching Path:");
		MyAnt.debugPrint(1,
				knowledge.toString() + " Going to: " + target.getX() + " "
						+ target.getY());

		if (target.getX() == knowledge.getLocX()
				&& target.getY() == knowledge.getLocY()) {
			MyAnt.debugPrint(1, "Sitting on top of target");
			return;
		}
		PriorityQueue<Cell> pq = knowledge.beforeSearch(includeUnexplored);
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
			ArrayList<Cell> al = findNeighbors(knowledge, u, includeUnexplored,
					pq);
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
		knowledge.getCurrRoute().clear();
		Cell u = target;
		while (prev.containsKey(u)) {
			knowledge.getCurrRoute().push(u);
			u = prev.get(u);
		}

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

	public static void printPath(Knowledge knowledge) {
		Stack<Cell> currRoute = knowledge.getCurrRoute();
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		Cell old = knowledge.get(knowledge.getLocX(), knowledge.getLocY());
		for (int i = 0; i < currRoute.size(); i++) {
			MyAnt.debugPrint(1, old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}

}
