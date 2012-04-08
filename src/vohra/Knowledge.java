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
	private static final long serialVersionUID = 1L;
	private int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	public enum MODE {
		EXPLORE, SCOUT, TOFOOD, TOHOME
	}

	// TODO for debugging, should be removed
	int antnum;
	boolean carryingFood = false;
	boolean isScout = false;
	public boolean surroundingsUpdate = false;
	Direction lastDir;
	MODE mode;
	int round;
	boolean updated = true;
	public int x, y;

	private final Hashtable<Point, Cell> map;
	private final Stack<Cell> currPlan;
	final Stack<Cell> totalPlan;

	public Knowledge(int antnum) {
		this.antnum = antnum;
		this.map = new Hashtable<Point, Cell>();
		this.currPlan = new Stack<Cell>();
		this.totalPlan = new Stack<Cell>();
		this.mode = MODE.SCOUT;
		this.lastDir = Direction.SOUTH;
		getCell(0, 0).setType(Cell.TYPE.HOME);

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
		// If any cells are updated, set updated to true
		surroundingsUpdate |= updateCell(getCell(x, y),
				surroundings.getCurrentTile());

		for (int i = 0; i < 4; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = getCell((x + offsets[i][0]), (y + offsets[i][1]));
			surroundingsUpdate |= updateCell(cell, tile);
		}

	}

	public boolean updateCell(Cell cell, Tile tile) {
		int tileAmountFood = tile.getAmountOfFood();
		// TODO remove
		cell.setNumAnts(tile.getNumAnts());
		if (cell.getType() == Cell.TYPE.FOOD && tileAmountFood == 0) {
			// previously had food, now doesn't. so set to grass
			cell.setAmountOfFood(0);
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

	public void merge(Hashtable<Point, Cell> toMerge) {
		Enumeration<Cell> e = toMerge.elements();

		while (e.hasMoreElements()) {
			Cell otherCell = e.nextElement();
			// only explored "other cells" are of interest
			if (otherCell.getType() != Cell.TYPE.UNEXPLORED) {
				Cell localCell = getCell(otherCell.getX(), otherCell.getY());
				if (localCell.getType() == Cell.TYPE.UNEXPLORED) {
					// if local cell is unexplored and other isn't, copy
					// type/food
					set(otherCell);
					// localCell.copy(otherCell);
					updated = true;
				} else if (localCell.timeStamp < otherCell.timeStamp) {
					// if local info is older, copy the newer info
					set(otherCell);
					// localCell.copy(otherCell);

					updated = true;
				}
			}
		}
	}

	public Enumeration<Cell> elements() {
		return map.elements();
	}

	void updateCurrLoc(Direction dir) {
		lastDir = dir;
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
			throw new IllegalArgumentException("Invalid direction");
		}
	}

	public int getAmountFoodFound() {
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

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

	public Cell getCell(int row, int col) {
		// If requested cell doesn't exist, create it and return
		Point coord = new Point(row, col);
		if (map.get(coord) == null)
			map.put(coord, new Cell(Cell.TYPE.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public Cell getCurrCell() {
		return getCell(x, y);
	}

	public Stack<Cell> getCurrPlan() {
		return currPlan;
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
