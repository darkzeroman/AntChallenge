package vohra.searches;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;

import vohra.Cell;
import vohra.Cell.CELLTYPE;
import vohra.ExtraMethods;
import vohra.Planner;
import vohra.WorldMap;

public class Djikstra extends Planner {

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell startCell, CELLTYPE type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = djikstra(worldMap, startCell, type, prev);
		if (target == null)
			return null;
		Stack<Cell> newPlan = constructPlan(worldMap, target, prev);
		return newPlan;
	}

	public PriorityQueue<Cell> preSearch(WorldMap worldMap, Cell startCell,
			boolean addUnexplored) {
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();

		Enumeration<Cell> e = worldMap.getMap().elements();
		while (e.hasMoreElements()) {

			Cell cell = e.nextElement();
			cell.dist = Integer.MAX_VALUE;
			if (cell.getX() == startCell.getX()
					&& cell.getY() == startCell.getY()) {
				cell.dist = 0;
			}

			if (!addUnexplored && cell.getCellType() != CELLTYPE.UNEXPLORED
					&& cell.getCellType() != CELLTYPE.WATER)
				pq.add(cell);
			else if (addUnexplored && cell.getCellType() != CELLTYPE.WATER)
				pq.add(cell);
		}
		return pq;
	}

	public Cell djikstra(WorldMap worldMap, Cell startCell, CELLTYPE type,
			Hashtable<Cell, Cell> prev) {

		LinkedList<Cell> listqueue = new LinkedList<Cell>();
		PriorityQueue<Cell> pq = preSearch(worldMap, startCell,
				type == CELLTYPE.UNEXPLORED);
		for (Cell cell : pq) {
			listqueue.add(cell);
		}
		int count = 0;

		while (!listqueue.isEmpty()) {
			Collections.sort(listqueue);

			count++;
			Cell u = listqueue.peek();
			if (u.dist == Integer.MAX_VALUE) {
				ExtraMethods.debugPrint(1, "exiting after: " + count);
				break; // nothing past here is reachable

			}
			u = listqueue.poll();
			if (u.getCellType() == type) { // reached target, can end
				listqueue.clear();
				return u;

			}
			LinkedList<Cell> neighbors = listNeighbors(worldMap, u, type);
			ListIterator<Cell> it = neighbors.listIterator();
			while (it.hasNext())
				if (!listqueue.contains(it.next()))
					it.remove();
			if (type == CELLTYPE.UNEXPLORED)
				Collections.shuffle(neighbors, new Random(System.nanoTime()));

			for (Cell cell : neighbors) {
				int alt = u.dist + 1;
				if (cell.getNumAnts() > 0) {
					ExtraMethods.debugPrint(1, "has ants!");
					alt = u.dist + 1;
					;
				}

				if (alt < cell.dist) {
					cell.dist = alt;
					prev.put(cell, u);
					if (listqueue.remove(cell))
						listqueue.add(cell);
					else
						throw new Error("Can't find element in PQ");

				}
			}
		}
		return null;

	}
}
