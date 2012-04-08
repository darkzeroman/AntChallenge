package vohra.searches;


import java.awt.Point;
import java.util.Comparator;

import vohra.Cell;

public class CellComparator implements Comparator<Cell> {

	@Override
	public int compare(Cell o1, Cell o2) {
		Point home = new Point(0, 0);
		return (int) (o1.getXY().distance(home) - o2.getXY().distance(home));

	}

}