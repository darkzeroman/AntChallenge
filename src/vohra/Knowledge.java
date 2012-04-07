package vohra;

import java.awt.Point;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Stack;

import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class Knowledge implements Serializable {

	public enum MODE {
		EXPLORE, SCOUT, TOFOOD, TOHOME
	}

	private static final long serialVersionUID = 1L;

	// TODO for debugging, should be removed
	int antnum;
	boolean carryingFood = false;
	private final Stack<Cell> currPlan;
	boolean isScout = false;
	Direction lastDir;
	private final Hashtable<Point, Cell> map;

	MODE mode;
	private final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
	int round;
	boolean updated = true;
	public int x, y;

	public Knowledge(int antnum) {
		this.antnum = antnum;
		this.map = new Hashtable<Point, Cell>();
		this.currPlan = new Stack<Cell>();
		get(0, 0).setType(Cell.TYPE.HOME);
		this.mode = MODE.EXPLORE;
		this.lastDir = Direction.SOUTH;
	}

	public PriorityQueue<Cell> preSearch(boolean checkUnexplored) {
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();

		Enumeration<Cell> e = map.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			cell.presearch();
			if (cell.getX() == this.x && cell.getY() == this.y) {
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

	public void updateMap(Surroundings surroundings) {
		// TODO remove
		if (surroundings == null) {
			MyAnt.debugPrint(2, "Why is surroundings null");
			return;
		}

		updated |= updateCell(get(x, y), surroundings.getCurrentTile());
		// NESW
		for (int i = 0; i < 4; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = get((x + offsets[i][0]), (y + offsets[i][1]));
			updated |= updateCell(cell, tile);
		}

	}

	// FOOD, GRASS, HOME, UNEXPLORED, WALL
	public boolean updateCell(Cell cell, Tile tile) {
		int tileAmountFood = tile.getAmountOfFood();
		// TODO remove
		cell.setNumAnts(tile.getNumAnts());
		if (cell.getType() == Cell.TYPE.FOOD && tileAmountFood == 0) {
			// previously had food, now doesn't. so set to grass
			cell.setAmountOfFood(tileAmountFood);
			cell.setType(Cell.TYPE.GRASS);
			return true;

		} else if (tileAmountFood > 0
				&& tileAmountFood != cell.getAmountOfFood()) {
			// still has food, but amount has changed
			cell.setAmountOfFood(tileAmountFood);
			if (!(cell.getType() == Cell.TYPE.HOME))
				cell.setType(Cell.TYPE.FOOD);
			return true;

		} else if (tile.isTravelable() && tileAmountFood == 0
				&& cell.getType() != Cell.TYPE.HOME
				&& cell.getType() != Cell.TYPE.GRASS) {
			// new info, set to grass
			cell.setType(Cell.TYPE.GRASS);
			return true;

		} else if (!tile.isTravelable() && tileAmountFood == 0) {
			cell.setType(Cell.TYPE.WATER);
			return true;
		}

		return false;
	}

	public void merge(Knowledge toMerge) {
		Enumeration<Cell> e = toMerge.map.elements();
		while (e.hasMoreElements()) {
			Cell other = e.nextElement();
			// if local cell is unexplored and other isn't, copy type/food
			Cell local = get(other.getX(), other.getY());
			if (local.getType() == Cell.TYPE.UNEXPLORED
					&& other.getType() != Cell.TYPE.UNEXPLORED) {
				set(other);
				updated = true;
			} else if (other.getType() != Cell.TYPE.UNEXPLORED
					&& local.timeStamp < other.timeStamp) {
				// if local info is older, copy the newer info
				set(other);
				updated = true;
			}

		}

	}

	public Enumeration<Cell> elements() {
		return map.elements();
	}

	void updateCurrLoc(Direction dir) {
		lastDir = dir;

		// Directions: NORTH, EAST, SOUTH, WEST;
		switch (dir) {
		case NORTH:
			this.y++;
			break;
		case EAST:
			this.x++;
			break;
		case SOUTH:
			this.y--;
			break;
		case WEST:
			this.x--;
			break;
		default:
			MyAnt.debugPrint(2, "Not a valid direction");
		}
	}

	public int getTotalFoodFound() {
		int sum = 0;
		Enumeration<Cell> e = map.elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().origFood;
		}
		return sum;
	}

	public int numKnownCells() {
		return map.keySet().size();
	}

	public Cell get(int row, int col) {
		Point coord = new Point(row, col);
		if (map.get(coord) == null)
			map.put(coord, new Cell(Cell.TYPE.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public Cell getCurrCell() {
		return get(x, y);
	}

	public Stack<Cell> getCurrPlan() {
		return currPlan;
	}

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void set(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);

	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return this.map.keySet().size() + "";
	}

	public MODE getMode() {
		return mode;
	}

}
