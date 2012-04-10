import vohra.VohraAnt;
import ants.Action;
import ants.Ant;
import ants.Surroundings;

public class AdapterAnt implements Ant {
	// Needed to use this because the engine complains if this class is in a
	// package but I needed packages for JUnit tests
	VohraAnt ant = new VohraAnt();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public Action getAction(Surroundings surroundings) {
		// Since there are runtime exceptions
		// Need to account for if they trigger
		Action action = null;
		try {
			action = ant.getAction(surroundings);
		} catch (RuntimeException e) {
			System.out.println("Runtime Error");
			e.printStackTrace();
			action = Action.HALT;
		}
		return action;

	}

	@Override
	public byte[] send() {
		return ant.send();
	}

	@Override
	public void receive(byte[] data) {
		ant.receive(data);

	}

}
