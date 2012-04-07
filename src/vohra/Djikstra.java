package vohra;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;

public class Djikstra extends RoutePlanner {

	@Override
	public boolean makeRoute(Knowledge knowledge, Cell.TYPE type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = MapOps.bfs(knowledge, type, prev);
		if (target == null)
			return false;
		return makeRoute(knowledge, target);
	}

	@Override
	public boolean makeRoute(Knowledge knowledge, Cell target) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();
		// printPath(knowledge);
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
		if (target.getType() == Cell.TYPE.UNEXPLORED)
			includeUnexplored = true;

		MyAnt.debugPrint(1, "Searching Path:");
		MyAnt.debugPrint(1,
				knowledge.toString() + " Going to: " + target.getX() + " "
						+ target.getY());

		if (target.getX() == knowledge.x && target.getY() == knowledge.y) {
			MyAnt.debugPrint(1, "Sitting on top of target");
			return;
		}
		PriorityQueue<Cell> pq = knowledge.preSearch(includeUnexplored);
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
			LinkedList<Cell> al = MapOps.listNeighbors(knowledge, u,
					includeUnexplored);
			ListIterator<Cell> it = al.listIterator();
			while (it.hasNext())
				if (!pq.contains(it.next()))
					it.remove();
			
			for (Cell cell : al) {
				int alt = u.dist + 10;
				if (cell.getNumAnts() > 0) {
					MyAnt.debugPrint(1, "has ants!");
					alt = u.dist + 1;
					;
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
}
