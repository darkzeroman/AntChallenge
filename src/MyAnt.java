import java.util.Random;

import ants.*;

public class MyAnt implements Ant {
	Random rand = new Random();

	public Action getAction(Surroundings surroundings) {
		int rInt = rand.nextInt(4);
		return Action.move(Direction.values()[rInt]);
		// return Action.move(Direction.EAST);
	}

	public byte[] send() {
		return null;
	}

	public void receive(byte[] data) {
		// Do nothing
	}

	public static void main(String[] args) {

	}

}