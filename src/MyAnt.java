import java.util.Random;

import ants.*;

public class MyAnt implements Ant {
	int homex, homey;
	boolean savedHome = false;
	Random rand = new Random();

	public Action getAction(Surroundings surroundings) {
		if (!savedHome){
			;
			//surroundings.getCurrentTile().
		}
			
		if (surroundings.getCurrentTile().getAmountOfFood() > 0)
			return Action.GATHER;
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