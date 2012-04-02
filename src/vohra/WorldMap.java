package vohra;

import java.awt.Point;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;

import ants.Action;
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
		if ((from.x == to.x) && (from.y > to.y))
			return Direction.NORTH;
		else if ((from.x == to.x) && (from.y < to.y))
			return Direction.SOUTH;
		else if ((from.y == to.y) && (from.x > to.x))
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

	public PriorityQueue<Cell> beforeSearch(int locX, int locY,
			boolean checkUnexplored) {
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();

		Enumeration<Cell> e = knowledge.elements();
		while (e.hasMoreElements()) {
			Cell mT = e.nextElement();
			mT.prepareForSearch();
			if (mT.x == locX && mT.y == locY) {
				System.out.println("currently: " + mT);
				mT.dist = 0;
			}
			if (!checkUnexplored && mT.getType() != type.UNEXPLORED
					&& mT.getType() != type.WALL)
				pq.add(mT);
			else if (checkUnexplored && mT.getType() != type.WALL)
				pq.add(mT);
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
			Cell mT = e.nextElement();
			sum += mT.origFood;
		}
		return sum;
	}

	public void markFalse() {
		Enumeration<Cell> e = knowledge.elements();
		while (e.hasMoreElements()) {
			Cell mT = e.nextElement();
			mT.mark = false;
		}
	}

	public void merge(WorldMap other) {
		Enumeration<Cell> e = other.knowledge.elements();
		while (e.hasMoreElements()) {
			Cell cell = e.nextElement();
			// if this is unexplored and other isn't, copy type
			int i = cell.getXY()[0], j = cell.getXY()[1];
			if (get(i, j).getType() == type.UNEXPLORED
					&& other.get(i, j).getType() != type.UNEXPLORED) {
				get(i, j).setType(other.get(i, j).getType());
				get(i, j).setAmntFood(other.get(i, j).getAmntFood());

			} else if (other.get(i, j).getType() != type.UNEXPLORED
					&& get(i, j).timeStamp < other.get(i, j).timeStamp)
				get(i, j).setAmntFood(other.get(i, j).getAmntFood());

		}

	}

	public void updateMap(Surroundings surroundings, int locX, int locY) {
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
			cell.setType(WorldMap.type.GRASS);
			return true;
		} else if (!tile.isTravelable() && tileAmountFood == 0) {
			cell.setType(WorldMap.type.WALL);
			return true;
		}

		return false;
	}

	public int sizeOfKnowledge() {
		return knowledge.keySet().size();
	}

}
