
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.PriorityQueue;

public class Djikstra extends RoutePlanner {

	@Override
	public boolean makeRoute(Knowledge knowledge, Cell.CellType type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = MapOps.bfs(knowledge, type, prev);
		if (target == null)
			return false;
		return makeRoute(knowledge, target);
	}

	@Override
	public boolean makeRoute(Knowledge knowledge, Cell target) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();
		printPath(knowledge);
		djikstra(knowledge, target, prev);
		constructPath(knowledge, target, prev);
		if (knowledge.getCurrRoute().size() > 0)
			return true;
		else
			return false;
	}

	public void djikstra(Knowledge knowledge, Cell target,
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
		constructPath(knowledge, target, prev);

	}

	public ArrayList<Cell> findNeighbors(Knowledge knowledge, Cell cell,
			boolean includeUnexplored, PriorityQueue<Cell> pq) {
		ArrayList<Cell> list = new ArrayList<Cell>();
		for (int i = 0; i < 4; i++) { // for each cardinal direction
			int xPos = cell.getX() + offsets[i][0];
			int yPos = cell.getY() + offsets[i][1];
			// exit if the requested cell is out of bounds

			Cell neighborCell = knowledge.get(xPos, yPos);

			// for Djikstra search
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
		return list;
	}

}
