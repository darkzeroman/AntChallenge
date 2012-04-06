/**
 * 
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import vohra.Cell.CellType;

/**
 * @author dkz
 * 
 */
public class AStar extends RoutePlanner {

	/*
	 * (non-Javadoc)
	 * 
	 * @see vohra.RoutePlanner#makeRoute(vohra.Knowledge, vohra.Cell.CellType)
	 */
	@Override
	public boolean makeRoute(Knowledge knowledge, Cell.CellType type) {
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell target = MapOps.bfs(knowledge, type, prev);
		if (target == null)
			return false;
		return makeRoute(knowledge, target);

	}

	public int h(Cell from, Cell to, Cell before, Hashtable<Cell, Cell> prev) {

		int temp = Math.abs(from.getX() - to.getX())
				+ Math.abs((from.getY() - to.getY()));
		int h = temp;
		if (from.getNumAnts() > 0
				&& (System.nanoTime() - from.numOfAntsTimeStamp) < 3 * Math
						.pow(10, 9))
			h += 0;
		else
			h += 0;

		Cell beforebefore;
		if (before != null && (beforebefore = prev.get(before)) != null
				&& (beforebefore.dirTo(before) == before.dirTo(from))) {
			h += 0;
		} else
			h += temp;
		return h;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vohra.RoutePlanner#makeRoute(vohra.Knowledge, vohra.Cell)
	 */
	@Override
	public boolean makeRoute(Knowledge knowledge, Cell target) {
		HashSet<Cell> closedSet = new HashSet<Cell>();
		LinkedList<Cell> openSet = new LinkedList<Cell>();
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		Cell start = knowledge.getCurrCell();
		openSet.add(start);

		start.g = 0;
		start.h = this.h(start, target, null, prev);
		start.f = start.g + start.h;
		while (!openSet.isEmpty()) {
			Collections.sort(openSet, new Comp());

			Cell currCell = openSet.poll();
			if (currCell == target) {
				// printPathlol(prev, target);
				constructPath(knowledge, target, prev);

				return true;
			}
			closedSet.add(currCell);
			ArrayList<Cell> al = MapOps.findNeighbors(knowledge, currCell,
					target.getType() == Cell.CellType.UNEXPLORED, null);
			Collections.shuffle(al);
			for (Cell neighbor : al) {
				if (closedSet.contains(neighbor))
					continue;
				int tentative_g_score = currCell.f;
				boolean tentative_is_better = false;
				if (!openSet.contains(neighbor)) {
					openSet.add(neighbor);
					neighbor.h = this.h(neighbor, target, currCell, prev);
					tentative_is_better = true;
				} else if (tentative_g_score < neighbor.g)
					tentative_is_better = true;
				else
					tentative_is_better = false;
				if (tentative_is_better) {
					prev.put(neighbor, currCell);
					neighbor.g = tentative_g_score;
					neighbor.f = neighbor.g + neighbor.h;
				}

			}
			// printPathlol(prev, currCell);

		}
		return false;
	}

	public boolean nothing(Knowledge knowledge, Cell target) {
		Cell start = knowledge.getCurrCell();
		HashSet<Cell> closedSet = new HashSet<Cell>();
		PriorityQueue<PQNode> openSet = new PriorityQueue<PQNode>();
		Hashtable<Cell, Cell> prev = new Hashtable<Cell, Cell>();

		openSet.add(new PQNode(start, new int[] { h(start, target, null, null),
				0, h(start, target, null, null) }));

		while (!openSet.isEmpty()) {
			System.out.println(openSet);
			PQNode currNode = openSet.peek();
			if (currNode.cell == target) {
				constructPath(knowledge, target, prev);
				System.out.println();
				// printPathlol(prev, target);
				System.out.println();
				return true;
			}
			currNode = openSet.poll();
			System.out.println(currNode.cell);
			closedSet.add(currNode.cell);

			ArrayList<Cell> al = MapOps.findNeighbors(knowledge, currNode.cell,
					target.getType() == Cell.CellType.UNEXPLORED, null);

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
		return false;

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

	private class Comp implements Comparator<Cell> {

		@Override
		public int compare(Cell o1, Cell o2) {
			return o1.f - o2.f;
		}

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
