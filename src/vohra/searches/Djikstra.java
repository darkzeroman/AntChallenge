package vohra.searches;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Stack;

import vohra.Cell;
import vohra.MapOps;
import vohra.MyAnt;
import vohra.Planner;
import vohra.WorldMap;

public class Djikstra extends Planner {

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell startCell,
			Cell.TYPE type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = djikstra(worldMap, startCell, type, prev);
		if (target == null)
			return null;
		Stack<Cell> newPlan = constructPath(worldMap, target, prev);
		return newPlan;
	}

	public PriorityQueue<Cell> preSearch(WorldMap worldMap, Cell startCell,
			boolean checkUnexplored) {
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();

		Enumeration<Cell> e = worldMap.getMap().elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			cell.dist = Integer.MAX_VALUE;
			if (cell.getX() == startCell.getX()
					&& cell.getY() == startCell.getY()) {
				cell.dist = 0;
			}
			if (!checkUnexplored && cell.getType() != Cell.TYPE.UNEXPLORED
					&& cell.getType() != Cell.TYPE.WATER)
				pq.add(cell);
			else if (checkUnexplored && cell.getType() != Cell.TYPE.WATER)
				pq.add(cell);
		}
		return pq;
	}

	public Cell djikstra(WorldMap worldMap, Cell startCell, Cell.TYPE type,
			Hashtable<Cell, Cell> prev) {
		boolean includeUnexplored = false;
		if (type == Cell.TYPE.UNEXPLORED)
			includeUnexplored = true;

		PriorityQueue<Cell> pq = preSearch(worldMap, startCell,
				includeUnexplored);
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
			LinkedList<Cell> al = MapOps.listNeighbors(worldMap, u,
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
