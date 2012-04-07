package vohra.searches;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;

import vohra.Cell;
import vohra.Knowledge;
import vohra.MapOps;
import vohra.MyAnt;
import vohra.Planner;
import vohra.Cell.TYPE;

public class Djikstra extends Planner {

	@Override
	public boolean makePlan(Knowledge knowledge, Cell.TYPE type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = djikstra(knowledge, type, prev);
		if (target == null)
			return false;
		constructPath(knowledge, target, prev);
		if (knowledge.getCurrPlan().size() > 0)
			return true;
		else
			return false;
	}

	public Cell djikstra(Knowledge knowledge, Cell.TYPE type,
			Hashtable<Cell, Cell> prev) {
		boolean includeUnexplored = false;
		if (type == Cell.TYPE.UNEXPLORED)
			includeUnexplored = true;

		// MyAnt.debugPrint(1, "Searching Path:");

		// if (k.getX() == knowledge.x && target.getY() == knowledge.y) {
		// MyAnt.debugPrint(1, "Sitting on top of target");
		// return;
		// }

		PriorityQueue<Cell> pq = knowledge.preSearch(includeUnexplored);
		int count = 0;

		while (!pq.isEmpty()) {
			count++;
			Cell u = pq.peek();
			if (u.dist == Integer.MAX_VALUE) {
				MyAnt.debugPrint(1, "exiting after: " + count);
				break; // nothing past here is reachable

			}
			u = pq.poll();
			if (u.getType() == type) { // reached target, can end
				pq.clear();

				return u;

			}
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
		return null;

	}
}
