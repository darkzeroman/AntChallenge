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
	public static enum type {
		FOOD, GRASS, HOME, UNEXPLORED, WALL
	}

	private static final long serialVersionUID = 1L;
	static int staticcount = 0;
	final int MAPSIZE;

	public static Direction dirTo(Cell from, Cell to) {
		if ((from.getXY()[0] == to.getXY()[0])
				&& (from.getXY()[1] > to.getXY()[1]))
			return Direction.NORTH;
		else if ((from.getXY()[0] == to.getXY()[0])
				&& (from.getXY()[1] < to.getXY()[1]))
			return Direction.SOUTH;
		else if ((from.getXY()[1] == to.getXY()[1])
				&& (from.getXY()[0] > to.getXY()[0]))
			return Direction.WEST;
		else
			return Direction.EAST;

	}

	public static Direction oppositeDir(Direction dir) {
		if (dir == null) {
			System.out.println("why is dir null");
			MyAnt.induceSleep(10 * 1000);
		}
		switch (dir) {
		case NORTH:
			return Direction.SOUTH;
		case EAST:
			return Direction.WEST;
		case SOUTH:
			return Direction.NORTH;
		case WEST:
			return Direction.EAST;
		default:
			return null;
		}
	}

	public Hashtable<Point, Cell> knowledge;

	int antnum;
	int count;
	LinkedList<Cell> lastChanges = new LinkedList<Cell>();

	int[][] offsets = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

	boolean recentlyUpdated = false;

	public WorldMap(int mapsize, int antnum, int origin) {
		MAPSIZE = mapsize;
		this.antnum = antnum;
		knowledge = new Hashtable<Point, Cell>();

		get(origin, origin).setType(type.HOME);
		count = staticcount;
		staticcount++;
	}

	public void merge(WorldMap other) {
		Enumeration<Cell> e = other.knowledge.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			int x = cell.getXY()[0], y = cell.getXY()[1];
			// if local cell is unexplored and other isn't, copy type/food
			if (get(x, y).getType() == type.UNEXPLORED
					&& other.get(x, y).getType() != type.UNEXPLORED) {
				get(x, y).setType(other.get(x, y).getType());
				get(x, y).setAmntFood(other.get(x, y).getAmntFood());

			} else if (other.get(x, y).getType() != type.UNEXPLORED
					&& get(x, y).timeStamp < other.get(x, y).timeStamp)
				// if local info is older, copy the newer info
				get(x, y).setAmntFood(other.get(x, y).getAmntFood());

		}

	}

	public void update(Surroundings surroundings, int locX, int locY) {
		// TODO remove
		if (surroundings == null) {
			System.out.println("why is surroundings null");
			MyAnt.induceSleep(10 * 1000);
			return;
		}

		updateCell(get(locX, locY), surroundings.getCurrentTile());
		// NESW
		for (int i = 0; i < 4; i++) {
			Tile temp = surroundings.getTile(Direction.values()[i]);
			Cell mapTile = get((locX + offsets[i][0]), (locY + offsets[i][1]));
			recentlyUpdated |= updateCell(mapTile, temp);
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
		if (knowledge.get(new Point(row, col)) == null) {
			Cell mT = new Cell(type.UNEXPLORED, row, col);
			knowledge.put(new Point(row, col), mT);
			return mT;
		} else
			return knowledge.get(new Point(row, col));
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

	public int sizeOfKnowledge() {
		return knowledge.keySet().size();
	}

}
