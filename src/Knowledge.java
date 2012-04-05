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

	private final Point currLoc;
	private final Hashtable<Point, Cell> map;
	private final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	private boolean updated = true;
	// TODO for debugging, should be removed
	int antnum;
	int count;

	private final Stack<Cell> currRoute;

	public Knowledge(int antnum) {
		this.antnum = antnum;
		this.map = new Hashtable<Point, Cell>();
		currRoute = new Stack<Cell>();
		currLoc = new Point(0, 0);
		get(0, 0).setType(Cell.CellType.HOME);

	}

	public Stack<Cell> getCurrRoute() {
		return currRoute;
	}

	public Enumeration<Cell> elements() {
		return map.elements();
	}

	public void merge(Knowledge toMerge) {
		Enumeration<Cell> e = toMerge.map.elements();
		while (e.hasMoreElements()) {
			Cell other = e.nextElement();
			// if local cell is unexplored and other isn't, copy type/food
			Cell local = get(other.getX(), other.getY());
			if (local.getType() == Cell.CellType.UNEXPLORED
					&& other.getType() != Cell.CellType.UNEXPLORED) {
				set(other);
				updated = true;
			} else if (other.getType() != Cell.CellType.UNEXPLORED
					&& local.timeStamp < other.timeStamp) {
				// if local info is older, copy the newer info
				set(other);
				updated = true;
			}

		}

	}

	public void updateMap(Surroundings surroundings, int locX, int locY) {
		// TODO remove
		if (surroundings == null) {
			MyAnt.debugPrint(2, "Why is surroundings null");
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
		if (cell.getType() == Cell.CellType.FOOD && tileAmountFood == 0) {
			// previously had food, now doesn't. so set to grass
			cell.setAmountOfFood(tileAmountFood);
			cell.setType(Cell.CellType.GRASS);
			return true;

		} else if (tileAmountFood > 0
				&& tileAmountFood != cell.getAmountOfFood()) {
			// still has food, but amount has changed
			cell.setAmountOfFood(tileAmountFood);
			if (!(cell.getType() == Cell.CellType.HOME))
				cell.setType(Cell.CellType.FOOD);
			return true;

		} else if (tile.isTravelable() && tileAmountFood == 0
				&& cell.getType() != Cell.CellType.HOME
				&& cell.getType() != Cell.CellType.GRASS) {
			// new info, set to grass
			cell.setType(Cell.CellType.GRASS);
			return true;

		} else if (!tile.isTravelable() && tileAmountFood == 0) {
			cell.setType(Cell.CellType.WALL);
			return true;
		}

		return false;
	}

	public int numKnownCells() {
		return map.keySet().size();
	}

	public Cell get(int row, int col) {
		Point coord = new Point(row, col);
		if (map.get(coord) == null)
			map.put(coord, new Cell(Cell.CellType.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public int getLocX() {
		return currLoc.x;
	}

	public int getLocY() {
		return currLoc.y;
	}

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

	public int getTotalFoodFound() {
		int sum = 0;
		Enumeration<Cell> e = map.elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().origFood;
		}
		return sum;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void set(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);
	}

	public void setCurrLoc(Point currLoc) {
		setLocX(currLoc.x);
		setLocY(currLoc.y);

	}

	public void setLocX(int locX) {
		this.currLoc.x = locX;
	}

	public void setLocY(int locY) {
		this.currLoc.y = locY;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public String toString() {
		return this.map.keySet().size() + "";
	}

	public PriorityQueue<Cell> beforeSearch(boolean checkUnexplored) {

		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();

		Enumeration<Cell> e = map.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			cell.presearch();
			if (cell.getX() == currLoc.x && cell.getY() == currLoc.y) {
				cell.dist = 0;
			}
			if (!checkUnexplored && cell.getType() != Cell.CellType.UNEXPLORED
					&& cell.getType() != Cell.CellType.WALL)
				pq.add(cell);
			else if (checkUnexplored && cell.getType() != Cell.CellType.WALL)
				pq.add(cell);
		}
		return pq;
	}
}
