
import java.awt.Point;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PriorityQueue;

import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMap implements Serializable {
	public enum type {
		FOOD, GRASS, HOME, UNEXPLORED, WALL
	}

	public int locX, locY;

	private static final long serialVersionUID = 1L;
	// TODO for debugging, should be removed
	int antnum;
	int count;

	private Hashtable<Point, Cell> map;
	final int MAPSIZE;
	final int[][] offsets = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
	boolean updated = true;

	public WorldMap(int mapsize, int antnum, int origin) {
		this.MAPSIZE = mapsize;
		this.antnum = antnum;
		this.map = new Hashtable<Point, Cell>();
		get(origin, origin).setType(type.HOME);

	}

	public void markFalse() {
		Enumeration<Cell> e = map.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			cell.mark = false;
		}
	}

	public PriorityQueue<Cell> beforeSearch(int locX, int locY,
			boolean checkUnexplored) {
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();

		Enumeration<Cell> e = map.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			cell.presearch();
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
		if (map.get(coord) == null)
			map.put(coord, new Cell(type.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public void set(Cell cell) {
		map.put(new Point(cell.getXY()[0], cell.getXY()[1]), cell);
	}

	public int getTotalFoodFound() {
		int sum = 0;
		Enumeration<Cell> e = map.elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().origFood;
		}
		return sum;
	}

	public void merge(WorldMap other) {
		Enumeration<Cell> e = other.map.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			int x = cell.getXY()[0], y = cell.getXY()[1];
			// if local cell is unexplored and other isn't, copy type/food
			Cell localCell = get(x, y);
			if (get(x, y).getType() == type.UNEXPLORED
					&& other.get(x, y).getType() != type.UNEXPLORED) {
				set(other.get(x, y));
				updated = true;
			} else if (other.get(x, y).getType() != type.UNEXPLORED
					&& get(x, y).timeStamp < other.get(x, y).timeStamp) {
				// if local info is older, copy the newer info
				set(other.get(x, y));
				updated = true;
			}

		}

	}

	public int numKnownCells() {
		return map.keySet().size();
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public void updateMap(Surroundings surroundings, int locX, int locY) {
		// TODO remove
		if (surroundings == null) {
			MyAnt.induceSleep(10, "Why is surroundings null");
			return;
		}

		updated |= updateCell(get(locX, locY), surroundings.getCurrentTile());
		// NESW
		for (int i = 0; i < 4; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = get((locX + offsets[i][0]), (locY + offsets[i][1]));
			updated |= updateCell(cell, tile);
		}

	}

	// FOOD, GRASS, HOME, UNEXPLORED, WALL
	public boolean updateCell(Cell cell, Tile tile) {
		int tileAmountFood = tile.getAmountOfFood();
		// TODO remove
		cell.setNumAnts(tile.getNumAnts());
		if (cell.getType() == type.FOOD && tileAmountFood == 0) {
			// previously had food, now doesn't. so set to grass
			cell.setAmountFood(tileAmountFood);
			cell.setType(type.GRASS);
			return true;

		} else if (tileAmountFood > 0 && tileAmountFood != cell.getAmntFood()) {
			// still has food, but amount has changed
			cell.setAmountFood(tileAmountFood);
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

	public String toString() {
		return this.map.keySet().size() + "";
	}

	public void setLastXY(int x, int y) {
		locX = x;
		locY = y;
	}

	public boolean isSameLastXY(int x, int y) {
		return (locX == x && locY == y);
	}

}
