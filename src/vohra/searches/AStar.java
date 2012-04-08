package vohra.searches;

/**
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;

import vohra.Cell;
import vohra.MapOps;
import vohra.MyAnt;
import vohra.Planner;
import vohra.WorldMap;

/**
 * @author dkz
 * 
 */
public class AStar extends Planner {

	@Override
	public Stack<Cell> makePlan(WorldMap worldMap, Cell startCell,
			Cell.CELLTYPE type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = bfs(worldMap, startCell, type, prev);
		if (target == null)
			return null;
		if (astar(worldMap, startCell, target, prev) == null)
			return null;

		Stack<Cell> newPlan = constructPlan(worldMap, target, prev);
		return newPlan;

	}

	public static Cell bfs(WorldMap worldMap, Cell startCell,
			Cell.CELLTYPE goalType, Hashtable<Cell, Cell> prev) {
		// BFS Search
		HashSet<Cell> markSet = new HashSet<Cell>();
		LinkedList<Cell> queue = new LinkedList<Cell>();

		markSet.add(startCell);
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell t = queue.remove();
			if (t.getCellType() == goalType)
				return t;
			LinkedList<Cell> neighbors = MapOps.listNeighbors(worldMap, t,
					goalType == Cell.CELLTYPE.UNEXPLORED);

			if (goalType == Cell.CELLTYPE.UNEXPLORED)
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

	public int h(Cell from, Cell to, Cell before, Hashtable<Cell, Cell> prev) {

		int temp = Math.abs(from.getX() - to.getX())
				+ Math.abs((from.getY() - to.getY()));
		int h = temp;
		if (from.getNumAnts() > 0)
			// && (System.nanoTime() - from.numOfAntsTimeStamp) < 3 * Math
			// .pow(10, 9))
			h += 0;
		else
			h += 5;

		Cell beforebefore;
		if (before != null && (beforebefore = prev.get(before)) != null
				&& (beforebefore.dirTo(before) == before.dirTo(from))) {
			h += 0;
		} else
			h += temp;
		return h;

	}

	public Cell astar(WorldMap worldMap, Cell startCell, Cell target,
			Hashtable<Cell, Cell> prev) {
		HashSet<Cell> closedSet = new HashSet<Cell>();
		PriorityQueue<PQNode> openSet = new PriorityQueue<PQNode>();

		openSet.add(new PQNode(startCell, new int[] {
				h(startCell, target, null, null), 0,
				h(startCell, target, null, null) }));

		while (!openSet.isEmpty()) {
			System.out.println(openSet);
			PQNode currNode = openSet.peek();
			if (currNode.cell == target) {
				System.out.println();
				// printPathlol(prev, target);
				System.out.println();
				return target;
			}
			currNode = openSet.poll();
			System.out.println(currNode.cell);
			closedSet.add(currNode.cell);

			LinkedList<Cell> al = MapOps.listNeighbors(worldMap, currNode.cell,
					target.getCellType() == Cell.CELLTYPE.UNEXPLORED);

			for (Cell neighborCell : al) {
				if (closedSet.contains(neighborCell))
					continue;
				int tentative_g_score = currNode.fgh[1] + 1;
				boolean tentative_is_better = false;
				if (!openSet.contains(neighborCell)) {
					PQNode neighborNode = new PQNode(neighborCell);
					openSet.add(neighborNode);
					neighborNode.updateh(h(neighborNode.cell, target,
							currNode.cell, prev));
					tentative_is_better = true;
				} else if (tentative_g_score < findPQNode(openSet, neighborCell).fgh[1])
					tentative_is_better = true;
				else
					tentative_is_better = false;

				if (tentative_is_better) {
					prev.put(neighborCell, currNode.cell);
					PQNode neighborNode = findPQNode(openSet, neighborCell);
					neighborNode.updateg(tentative_g_score);
					neighborNode.update();
					if (!openSet.remove(neighborNode))
						MyAnt.debugPrint(2, "ERROR");
					openSet.add(neighborNode);
				}
			}
		}
		return null;

	}

	public void printPathlol(Hashtable<Cell, Cell> prev, Cell currCell) {
		if (prev.containsKey(currCell)) {
			System.out.print(currCell + " ,");
			printPathlol(prev, prev.get(currCell));
		}
	}

	private PQNode findPQNode(PriorityQueue<PQNode> pq, Cell cell) {
		Iterator<PQNode> it = pq.iterator();
		while (it.hasNext()) {
			PQNode node = it.next();
			if (node.equals(cell))
				return node;
		}
		return null;

	}

	private class PQNode implements Comparable<PQNode> {
		int[] fgh;;
		Cell cell;

		PQNode(Cell cell, int[] fgh) {
			this.cell = cell;
			this.fgh = fgh;
		}

		PQNode(Cell cell) {
			this(cell, new int[3]);
		}

		@Override
		public int compareTo(PQNode o) {
			return fgh[0] - o.fgh[0];
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Cell)
				return (this.cell == ((Cell) o));
			if (o instanceof PQNode)
				return (this == ((PQNode) o));
			return false;
		}

		void updategh(int g, int h) {
			fgh[1] = g;
			fgh[2] = h;
		}

		void updateg(int g) {
			fgh[1] = g;
			// fgh[0] = fgh[1] + fgh[2];
		}

		void update() {
			fgh[0] = fgh[1] + fgh[2];

		}

		void updateh(int h) {
			fgh[2] = h;
			// fgh[0] = fgh[1] + fgh[2];
		}

		public String toString() {
			return cell.toString() + " " + Arrays.toString(fgh);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
