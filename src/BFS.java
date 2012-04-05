import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

public class BFS extends RoutePlanner {

	@Override
	// public static boolean makeRoute(Knowledge knowledge, Cell.CellType type,
	public boolean makeRoute(Knowledge knowledge, Cell.CellType type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = bfs(knowledge, type, prev);
		if (target == null)
			return false;
		constructPath(knowledge, target, prev);
		if (knowledge.getCurrRoute().size() > 0)
			return true;
		else
			return false;
	}

	public Cell bfs(Knowledge knowledge, Cell.CellType goalType,
			Hashtable<Cell, Cell> prev) {
		boolean includeUnexplored = (goalType == Cell.CellType.UNEXPLORED);
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
					includeUnexplored);

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

	public ArrayList<Cell> findNeighbors(Knowledge knowledge, Cell cell,
			boolean includeUnexplored) {

		ArrayList<Cell> list = new ArrayList<Cell>();
		for (int i = 0; i < 4; i++) { // for each cardinal direction

			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];
			// exit if the requested cell is out of bounds

			Cell neighborCell = knowledge.get(xPos, yPos);

			// for BFS search
			if (includeUnexplored
					&& neighborCell.getType() != Cell.CellType.WALL)
				list.add(neighborCell);
			else if (!includeUnexplored
					&& (neighborCell.getType() != Cell.CellType.UNEXPLORED)
					&& (neighborCell.getType() != Cell.CellType.WALL))
				list.add(neighborCell);
		}
		return list;
	}

	public Cell bfs(Knowledge knowledge, Cell target, Hashtable<Cell, Cell> prev) {
		boolean includeUnexplored = (target.getType() == Cell.CellType.UNEXPLORED);
		// BFS Search
		HashSet<Cell> markSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		Cell startCell = knowledge.getCurrCell();
		markSet.add(startCell);
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell t = queue.remove();
			if (t == target)
				return t;
			ArrayList<Cell> neighbors = findNeighbors(knowledge, t,
					includeUnexplored);

			if ((knowledge.getMode() == Knowledge.Mode.SCOUT)
					|| (knowledge.getMode() == Knowledge.Mode.EXPLORE))
				Collections.shuffle(neighbors, new Random(System.nanoTime()));

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

	@Override
	public boolean makeRoute(Knowledge knowledge, Cell target) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		bfs(knowledge, target, prev);
		constructPath(knowledge, target, prev);
		if (knowledge.getCurrRoute().size() > 0)
			return true;
		else
			return false;
	}
}