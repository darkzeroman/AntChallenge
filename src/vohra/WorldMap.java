package vohra;
import java.awt.Point;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;

import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMap implements Serializable {
	public enum type {
		FOOD, GRASS, HOME, UNEXPLORED, WALL
	}

	private static final long serialVersionUID = 1L;

	int antnum;
	int count;

	public Hashtable<Point, Cell> knowledge;
	final int MAPSIZE;
	int[][] offsets = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
	boolean recentlyUpdated = true;

	public WorldMap(int mapsize, int antnum, int origin) {
		MAPSIZE = mapsize;
		this.antnum = antnum;
		knowledge = new Hashtable<Point, Cell>();
		get(origin, origin).setType(type.HOME);

	}

	public void markFalse() {
		Enumeration<Cell> e = knowledge.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			cell.mark = false;
		}
	}

	public PriorityQueue<Cell> beforeSearch(int locX, int locY,
			boolean checkUnexplored) {
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();

		Enumeration<Cell> e = knowledge.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			cell.resetForSearch();
			if (cell.getXY()[0] == locX && cell.getXY()[1] == locY) {
				cell.dist = 0;
			}
			if (!checkUnexplored && cell.getType() != type.UNEXPLORED
					&& cell.getType() != type.WALL)
				pq.add(cell);
			else if (checkUnexplored && cell.getType() != type.WALL)
				pq.add(cell);
		}
		return pq;
	}

	public Cell get(int row, int col) {
		Point coord = new Point(row, col);
		if (knowledge.get(coord) == null)
			knowledge.put(coord, new Cell(type.UNEXPLORED, row, col));
		return knowledge.get(coord);
	}

	public void set(Cell cell) {
		knowledge.put(new Point(cell.getXY()[0], cell.getXY()[1]), cell);
	}

	public int getTotalFoodFound() {
		int sum = 0;
		Enumeration<Cell> e = knowledge.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			sum += cell.origFood;
		}
		return sum;
	}

	public void merge(WorldMap other) {
		Enumeration<Cell> e = other.knowledge.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			int x = cell.getXY()[0], y = cell.getXY()[1];
			Cell otherCell = other.get(x, y);
			// if local cell is unexplored and other isn't, take it
			// if other cell is not unexplored and fresher, take it
			if (otherCell.getType() != type.UNEXPLORED
					&& (cell.getType() == type.UNEXPLORED || (cell.timeStamp < otherCell.timeStamp))) {
				set(otherCell);
				recentlyUpdated = true;
			}
		}
	}

	public int sizeOfKnowledge() {
		return knowledge.keySet().size();
	}

	public void update(Surroundings surroundings, int locX, int locY) {
		// TODO remove
		if (surroundings == null) {
			MyAnt.induceSleep(10, "Why is surroundings null");
			return;
		}

		updateCell(get(locX, locY), surroundings.getCurrentTile());
		// NESW
		for (int i = 0; i < 4; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = get((locX + offsets[i][0]), (locY + offsets[i][1]));
			recentlyUpdated |= updateCell(cell, tile);
		}

	}

	// FOOD, GRASS, HOME, UNEXPLORED, WALL
	public boolean updateCell(Cell cell, Tile tile) {
		int tileAmountFood = tile.getAmountOfFood();

		if (cell.getType() == type.FOOD && tileAmountFood == 0) {
			// previously had food, now doesn't. so set to grass
			cell.setAmntFood(tileAmountFood);
			cell.setType(type.GRASS);
			return true;

		} else if (tileAmountFood > 0 && tileAmountFood != cell.getAmntFood()) {
			// still has food, but amount has changed
			cell.setAmntFood(tileAmountFood);
			if (!(cell.getType() == WorldMap.type.HOME))
				cell.setType(WorldMap.type.FOOD);
			return true;

		} else if (tile.isTravelable() && tileAmountFood == 0
				&& cell.getType() != type.HOME && cell.getType() != type.GRASS) {
			// new info, set to grass
			cell.setType(WorldMap.type.GRASS);
			return true;

		} else if (!tile.isTravelable() && tileAmountFood == 0) {
			cell.setType(WorldMap.type.WALL);
			return true;
		}

		return false;
	}

}
