package vohra;

import java.awt.Point;
import java.util.Comparator;

public class CellComparator implements Comparator<Cell> {

	@Override
	public int compare(Cell o1, Cell o2) {
		Point home = new Point(0, 0);
		return (int) (o2.getXY().distance(home) - o1.getXY().distance(home));

	}

}