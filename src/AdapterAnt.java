import vohra.VohraAnt;
import ants.Action;
import ants.Ant;
import ants.Surroundings;

public class AdapterAnt implements Ant {
	/*
	 * Needed to use this because engine complains if this class is in a package
	 * but I needed packages for JUnit tests.
	 */
	VohraAnt ant = new VohraAnt();

	public static void main(String[] args) {
	}

	@Override
	public Action getAction(Surroundings surroundings) {
		// Need to check if there are runtime exceptions
		Action action = null;
		try {
			action = ant.getAction(surroundings);
		} catch (RuntimeException e) {
			System.out.println("Runtime Error");
			e.printStackTrace();
			action = Action.HALT;
		} catch (Exception e) {
			System.out.println("Unexpected Error");
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
