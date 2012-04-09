
import vohra.MyAnt;
import ants.Action;
import ants.Ant;
import ants.Surroundings;

public class TestAnt implements Ant {
	// Needed to use this because the engine complains if the class is in a
	// package but I needed packages for JUnit tests
	MyAnt ant = new MyAnt();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public Action getAction(Surroundings surroundings) {
		return ant.getAction(surroundings);
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
