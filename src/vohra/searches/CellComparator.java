package vohra.searches;

import java.awt.Point;
import java.util.Comparator;

import vohra.Cell;

public class CellComparator implements Comparator<Cell> {

	@Override
	public int compare(Cell o1, Cell o2) {
		Point home = new Point(0, 0);
		return (int) (new Point(o1.getX(), o1.getY()).distance(home) - new Point(
				o2.getX(), o2.getY()).distance(home));

	}

}