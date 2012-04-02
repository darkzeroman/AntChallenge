import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;

import ants.Action;
import ants.Direction;

public class MapOps {
	static int[][] offsets = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

	public static Action makeRoute(MyAnt ant, WorldMap.type type, String error) {
		Cell target = MapOps.findClosest(ant, type);
		if (target != null) // try to make a path if it exists
			return makeRoute(ant, target, error);
		else
			return null;
	}

	public static Action makeRoute(MyAnt ant, Cell target, String error) {
		Action nextMove = null;
		Direction dir = null;

		if ((dir = MapOps.searchRoute(ant, target)) != null)
			nextMove = Action.move(dir);
		else if (target != null && dir == null) {
			// target exists, but no path to it, possibly coding error
			nextMove = Action.HALT;
			System.out.println(error + " HALT, closest: " + target.toString());

		}
		return nextMove;
	}

	public static Cell findClosest(MyAnt ant, WorldMap.type goalType) {
		// BFS Search
		LinkedList<Cell> queue = new LinkedList<Cell>();
		Cell startCell = ant.getCell(ant.getLocX(), ant.getLocY());
		startCell.mark();
		queue.add(startCell);
		while (!queue.isEmpty()) {
			Cell t = queue.remove();
			if (t.getType() == goalType)
				return t;
			ArrayList<Cell> neighbors = findNeighbors(ant, t,
					goalType == WorldMap.type.UNEXPLORED, null);

			if (ant.isScout && ant.mode == MyAnt.Mode.SCOUT)
				Collections.shuffle(neighbors);

			for (Cell cell : neighbors) {
				if (!cell.mark) {
					cell.mark = true;
					queue.add(cell);
				}
			}
		}
		return null;
	}

	public static Direction searchRoute(MyAnt ant, Cell target) {
		boolean checkUnexplored = false;
		if (target.getType() == WorldMap.type.UNEXPLORED)
			checkUnexplored = true;
	
		System.out.print("SEARCHING: " + ant.antnum);
		System.out.println(" Current X: " + ant.getLocX() + " Y: "
				+ ant.getLocY() + " Going to: " + target.getXY()[0] + " "
				+ target.getXY()[1]);
	
		if (target.getXY()[0] == ant.getLocX()
				&& target.getXY()[1] == ant.getLocY()) {
			System.out.println("Sitting on top of target");
			return null;
		}
		PriorityQueue<Cell> pq = ant.getMap().beforeSearch(ant.getLocX(),
				ant.getLocY(), checkUnexplored);
		System.out.println("PQ: " + pq.size());
		int count = 0;
	
		while (!pq.isEmpty()) {
			count++;
			Cell u = pq.peek();
			if (u.dist == Integer.MAX_VALUE) {
				System.out.println("exiting after: " + count);
				break; // nothing past here is reachable
	
			}
			u = pq.poll();
			if (u == target) // reached target, can end
				break;
			ArrayList<Cell> al = findNeighbors(ant, u, checkUnexplored, pq);
			for (Cell mapTile : al) {
				int alt = u.dist + 1;
				if (alt < mapTile.dist) {
					mapTile.dist = alt;
					mapTile.prev = u;
					if (pq.remove(mapTile))
						pq.add(mapTile);
					else
						throw new Error("Can't find element in PQ");
	
				}
			}
		}
		pq.clear();
		// constructing path
		ant.getCurrRoute().clear();
		Cell u = target;
		while (u.prev != null) {
			ant.getCurrRoute().add(0, u);
			u = u.prev;
		}
	
		// System.out.print("Printing Path:  (size: " + ant.currRoute.size()
		// + "): ");
		// MapTile old = ant.map.get(ant.locX, ant.locY);
		// for (int i = 0; i < ant.currRoute.size(); i++) {
		// System.out.print(WorldMap.dirTo(old, ant.currRoute.get(i)) + " ");
		// old = ant.currRoute.get(i);
		// }
		// System.out.println();
	
		if (ant.getCurrRoute().size() > 0) {
			// System.out.println("returning path");
			return WorldMap.dirTo(ant.getMap()
					.get(ant.getLocX(), ant.getLocY()), ant.getCurrRoute()
					.remove(0));
		} else {
			System.out.println("returning null PATH");
			return null;
		}
	}

	public static ArrayList<Cell> findNeighbors(MyAnt ant, Cell cell,
			boolean checkUnexplored, PriorityQueue<Cell> pq) {
		ArrayList<Cell> list = new ArrayList<Cell>();
		for (int i = 0; i < 4; i++) { // for each cardinal direction
			int xPos = cell.getXY()[0] + offsets[i][0];
			int yPos = cell.getXY()[1] + offsets[i][1];
			// exit if the requested cell is out of bounds
			if ((xPos < 0) || (yPos < 0) || (xPos >= ant.getMap().MAPSIZE)
					|| (yPos >= ant.getMap().MAPSIZE))
				continue;

			Cell neighborCell = ant.getCell(xPos, yPos);

			if (pq == null) { // for BFS search
				if (checkUnexplored
						&& neighborCell.getType() != WorldMap.type.WALL)
					list.add(neighborCell);
				else if (!checkUnexplored
						&& (neighborCell.getType() != WorldMap.type.UNEXPLORED)
						&& (neighborCell.getType() != WorldMap.type.WALL))
					list.add(neighborCell);
			} else if (pq != null) { // for Djikstra search
				if (checkUnexplored
						&& neighborCell.getType() != WorldMap.type.WALL
						&& pq.contains(neighborCell))
					list.add(neighborCell);
				else if (!checkUnexplored
						&& ((neighborCell.getType() != WorldMap.type.UNEXPLORED) && (neighborCell
								.getType() != WorldMap.type.WALL))
						&& (pq.contains(neighborCell)))
					list.add(neighborCell);
			}

		}
		return list;
	}

	public boolean isNotTypes(Cell cell, WorldMap.type[] types) {
		for (int i = 0; i < types.length; i++) {
			if (cell.getType() == types[i])
				return false;
		}
		return true;
	}
}
