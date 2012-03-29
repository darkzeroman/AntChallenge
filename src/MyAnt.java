import java.util.Random;

import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class MyAnt implements Ant {
	public static enum type {
		HOME, WALL, UNEXPLORED, FOOD, GRASS
	};

	MapTile[][] map = new MapTile[40][40];
	boolean initialized = false;
	Direction lastDir = null;
	Random rand = new Random();
	ObjectIO<Integer> oio = new ObjectIO<Integer>();
	boolean carryingFood = false;
	int origin = 20;
	int currLocX, currLocY;

	public enum Mode {
		EXPLORE, TOFOOD, TOHOME
	};

	public Action getAction(Surroundings surroundings) {
		if (!initialized)
			this.intialize();

		if (surroundings.getCurrentTile().getAmountOfFood() > 0
				&& !carryingFood) {
			carryingFood = true;
			return Action.GATHER;
		}

		return Action.move(getRandomDirection(surroundings));
	}

	public void updatingMap(Surroundings surroundings) {
		Tile currTile = surroundings.getCurrentTile();

		MapTile currMapTile = map[currLocX][currLocY];
		if (currTile.getAmountOfFood() > 0) {
			currMapTile.setAmountFood(currTile.getAmountOfFood());
			currMapTile.setType(type.FOOD);
		} else {
			currMapTile.setType(type.GRASS);
		}
		// NESW
		//
		int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
		for (int i = 0; i < 4; i++) {
			Tile temp = surroundings.getTile(Direction.values()[i]);
			MapTile mapTile = map[currLocX + offsets[i][0]][currLocY
					+ offsets[i][1]];

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

	public void intialize() {
		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = new MapTile(type.UNEXPLORED);
			}
		map[origin][origin].setType(type.HOME);
		currLocX = origin;
		currLocY = origin;
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

		// Updating current location
		// Directions: NORTH, EAST, SOUTH, WEST;
		switch (rInt) {
		case 0:
			currLocY++;
		case 1:
			currLocX++;
			break;
		case 2:
			currLocY--;
			break;
		case 3:
			currLocX--;
			break;
		default:
			System.out.println("Not valid move");
			break;
		}
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
		new MyAnt();
		System.out.println("hi");
	}

}
