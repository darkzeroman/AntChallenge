import java.util.Random;

import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;

public class MyAnt implements Ant {
	public static enum type {
		HOME, WALL, UNEXPLORED, FOOD
	};
	MapTile[][] map = new MapTile[40][40];
	boolean initialized = false;

	Random rand = new Random();
	ObjectIO<Integer> oio = new ObjectIO<Integer>();
	boolean carryingFood = false;

	public Action getAction(Surroundings surroundings) {
		if (!initialized)

			if (surroundings.getCurrentTile().getAmountOfFood() > 0
					&& !carryingFood) {
				carryingFood = true;
				return Action.GATHER;
			}
		return Action.move(getRandomDirection(surroundings));
		// return Action.move(Direction.EAST);
	}

	public void intializeMap(){
		for (int i  =0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++){
				map[i][j] = new MapTile(type.HOME);
			}
	}
	private Direction getRandomDirection(Surroundings surroundings) {
		boolean[] choices = new boolean[4];
		for (int i = 0; i < 4; i++) {
			if (surroundings.getTile(Direction.values()[i]).isTravelable())
				choices[i] = true;

		}

		int rInt = rand.nextInt(4);
		if (choices[rInt])
			return Direction.values()[rInt];
		while (!choices[rInt])
			rInt = rand.nextInt(4);
		return Direction.values()[rInt];

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

	public static void main(String[] args) {
		new MyAnt();
		System.out.println("hi");
	}

}
