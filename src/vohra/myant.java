package vohra;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class myant implements Ant {
	public static enum type {
		HOME, WALL, UNEXPLORED, FOOD, GRASS
	};

	int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
	public MapTile[][] map = new MapTile[40][40];
	boolean initialized = false;
	Direction lastDir = null;
	Random rand = new Random();
	ObjectIO<Integer> oio = new ObjectIO<Integer>();
	boolean carryingFood = false;
	public int origin = 20;
	public int locX, locY;
	Mode mode;
	int round = 0;

	public enum Mode {
		EXPLORE, TOFOOD, TOHOME
	};

	public Action getAction(Surroundings surroundings) {

		round++;
		if (round > 5)
			mode = Mode.TOHOME;

		if (!initialized)
			this.intialize();
		Action nextMove = null;
		updatingMap(surroundings);

		switch (mode) {
		case EXPLORE:
			if (locX != origin && locY != origin
					&& surroundings.getCurrentTile().getAmountOfFood() > 0
					&& !carryingFood) {
				carryingFood = true;
				mode = Mode.TOHOME;
				return Action.GATHER;
			}
			nextMove = Action.move(getRandomDirection(surroundings));
			// break;
		case TOFOOD:
			break;
		case TOHOME:
			Direction dir = search(map[origin][origin]);
			if (dir != null)
				nextMove = Action.move(dir);
			else
				nextMove = Action.HALT;
			break;
		}
		if (nextMove.getDirection() != null)
			updateCurrLoc(nextMove.getDirection());
		return nextMove;
	}

	public void updatingMap(Surroundings surroundings) {
		if (surroundings == null)
			return;
		Tile currTile = surroundings.getCurrentTile();

		MapTile currMapTile = map[locX][locY];
		if (currTile.getAmountOfFood() > 0) {
			currMapTile.setAmountFood(currTile.getAmountOfFood());
			currMapTile.setType(type.FOOD);
		} else {
			currMapTile.setType(type.GRASS);
		}
		// NESW
		//
		for (int i = 0; i < 4; i++) {
			Tile temp = surroundings.getTile(Direction.values()[i]);
			MapTile mapTile = map[(locX + offsets[i][0])][(locY + offsets[i][1])];
			mapTile.setLocation((locX + offsets[i][0]), (locY + offsets[i][1]));
			if (temp.getAmountOfFood() > 0) {
				mapTile.setAmountFood(temp.getAmountOfFood());
				mapTile.setType(type.FOOD);
			} else if (temp.isTravelable()) {
				mapTile.setType(type.GRASS);
			} else {
				mapTile.setType(type.WALL);
			}
		}

	}

	public Direction search(MapTile target) {
		System.out.println("SEARCHING");
		PriorityQueue<MapTile> pq = readyMap();
		int count = 0;

		while (!pq.isEmpty()) {
			count++;
			MapTile u = pq.peek();
			if (u.distanceFromSource == Integer.MAX_VALUE) {
				System.out.println("exiting after: " + count);
				break;
				// System.out.println("ERROR IN DJIKSTRA!");
				// return lastDir;
			}
			u = pq.poll();
			// if (u == target)
			// break;
			ArrayList<MapTile> al = findNeighbors(pq, u);
			for (MapTile mapTile : al) {
				int alt = u.distanceFromSource + 1;
				if (alt < mapTile.distanceFromSource) {
					mapTile.distanceFromSource = alt;
					mapTile.prev = u;
					findAndUpdatePQ(pq, mapTile);
				}
			}
		}

		// System.out.println("prev: " + map[currLocX][currLocY].prev);
		ArrayList<MapTile> path = new ArrayList<MapTile>();
		MapTile u = target;
		while (u.prev != null) {
			path.add(0, u);
			u = u.prev;
		}

		System.out.println("Printing Path");
		MapTile old = map[locX][locY];
		for (int i = 0; i < path.size(); i++) {
			System.out.println(dirForMapTile(old, path.get(i)));
			old = path.get(i);
		}
		System.out.println("Done Printing Path");
		System.out.println("Current X: " + locX + " Y: " + locY);
		System.out.println("Going to: " + origin + " " + origin);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// if (path.size() > 2)
		if (path.size() > 0)
			return dirForMapTile(map[locX][locY], path.get(0));
		else {
			System.out.println("returning null");
			return null;
		}

	}

	public Direction dirForMapTile(MapTile from, MapTile to) {
		if ((from.x == to.x) && (from.y > to.y))
			return Direction.SOUTH;
		else if ((from.x == to.x) && (from.y < to.y))
			return Direction.NORTH;
		else if ((from.y == to.y) && (from.x > to.x))
			return Direction.WEST;
		else
			return Direction.EAST;

	}

	public void findAndUpdatePQ(PriorityQueue<MapTile> pq, MapTile mapTile) {
		if (pq.remove(mapTile))
			pq.add(mapTile);
		else {
			throw new Error("Can't find element in PQ");
		}
	}

	public ArrayList<MapTile> findNeighbors(PriorityQueue<MapTile> pq,
			MapTile mapTile) {
		ArrayList<MapTile> list = new ArrayList<MapTile>();
		for (int i = 0; i < 4; i++) {
			int xPos = mapTile.x + offsets[i][0];
			int yPos = mapTile.y + offsets[i][1];
			if ((xPos >= map.length) || (yPos >= map[0].length))
				continue;
			if ((xPos < 0) || (yPos < 0))
				continue;
			MapTile temp = map[xPos][yPos];
			if (((temp.type != type.UNEXPLORED) && (temp.type != type.WALL))
					&& (pq.contains(temp)))
				list.add(temp);

		}
		return list;
	}

	public PriorityQueue<MapTile> readyMap() {
		PriorityQueue<MapTile> pq = new PriorityQueue<MapTile>();

		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++) {
				// setting the distance from source
				if (i == locX && j == locY)
					map[locX][locY].distanceFromSource = 0;
				else
					map[i][j].distanceFromSource = Integer.MAX_VALUE;
				// setting the prev
				map[i][j].prev = null;
				if (map[i][j].type != type.UNEXPLORED
						&& map[i][j].type != type.WALL)
					pq.add(map[i][j]);

			}
		// System.out.println("PQ SIZE: " + pq.size());
		return pq;
	}

	public void intialize() {
		initialized = true;
		for (int i = map.length - 1; i >= 0; i--)
			for (int j = map[i].length - 1; j >= 0; j--) {
				map[j][i] = new MapTile(myant.type.UNEXPLORED);
				map[j][i].setLocation(i, j);
			}

		map[origin][origin].setType(type.HOME);
		locX = origin;
		locY = origin;
		mode = Mode.EXPLORE;
	}

	public int[] getAbsolute() {
		return new int[] { locX, locY };
	}

	private void updateCurrLoc(Direction dir) {
		// Updating current location
		// Directions: NORTH, EAST, SOUTH, WEST;
		switch (dir) {
		case NORTH:
			locY++;
		case EAST:
			locX++;
			break;
		case SOUTH:
			locY--;
			break;
		case WEST:
			locX--;
			break;
		default:
			System.out.println("Not valid move");
			break;
		}
	}

	private Direction getRandomDirection(Surroundings surroundings) {
		boolean[] choices = new boolean[4];
		int count = 0;
		for (int i = 0; i < 4; i++) {
			if ((getOppositeDirection(lastDir) != Direction.values()[i])
					&& (surroundings.getTile(Direction.values()[i])
							.isTravelable())) {
				choices[i] = true;
				count++;
			}

		}
		// if count is zero, we're stuck in a corner, so need to go back
		if (count == 0) {
			return lastDir = getOppositeDirection(lastDir);
		}
		// change to a for loop at some point
		int rInt = rand.nextInt(4);
		while (!choices[rInt])
			rInt = rand.nextInt(4);

		return lastDir = Direction.values()[rInt];

	}

	public byte[] send() {
		return oio.toByteArray(new Integer(5));
		// return null;
	}

	public void receive(byte[] data) {
		int x = oio.fromByteArray(data);
		// Do nothing
		System.out.println("RECEIVED X: " + x);
	}

	public Direction getOppositeDirection(Direction dir) {
		if (dir == null)
			return null;
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

	public static void main(String[] args) {
		myant ant = new myant();

		System.out.println("hi");

	}

	private void printMap() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				// HOME, WALL, UNEXPLORED, FOOD, GRASS

				if (i == locX && j == locY)
					System.out.print("X");
				else if (map[i][j].type == type.WALL)
					System.out.print("w");
				else if (map[i][j].type == type.GRASS)
					System.out.print("x");
				else if (map[i][j].type == type.HOME)
					System.out.print("H");
				else if (map[i][j].type == type.FOOD)
					System.out.print("F");
				else
					System.out.print(" ");

			}
			System.out.println();
		}
	}

	public MapTile getMapTileCoord(int x, int y) {
		return map[x][y];
	}
}
