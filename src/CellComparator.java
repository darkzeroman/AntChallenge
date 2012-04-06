import java.awt.Point;
import java.util.Comparator;
import java.util.Random;

public class CellComparator implements Comparator<Cell> {

	@Override
	public int compare(Cell o1, Cell o2) {
		Point home = new Point(0, 0);
		int val = (int) (o2.getXY().distance(home) - o1.getXY().distance(home));
		Random rand = new Random();
		int randNum = rand.nextInt(10);

		if (randNum < 3)
			return val;
		else if (randNum < 6)
			return 0;
		else
			return -1 * val;

	}

}