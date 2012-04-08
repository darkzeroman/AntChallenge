package vohra.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import org.junit.Test;

import vohra.Cell;
import vohra.Cell.CELLTYPE;
import vohra.searches.DistFromOriginComparator;

public class CellTest {

	@Test
	public void testCompareTo() {
		Cell a = new Cell(CELLTYPE.WATER, 1, 1);
		a.dist = 10;
		Cell b = new Cell(Cell.CELLTYPE.GRASS, 1, 1);
		b.dist = Integer.MAX_VALUE;
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();
		pq.add(a);
		pq.add(b);
		assertEquals(a, pq.poll());
	}

	@Test
	public void testDistanceComparator() {
		ArrayList<Cell> test = new ArrayList<Cell>();
		test.add(new Cell(Cell.CELLTYPE.GRASS, 1, 1));
		test.add(new Cell(Cell.CELLTYPE.FOOD, 4, 4));

		Collections.sort(test, new DistFromOriginComparator());
		assertEquals(Cell.CELLTYPE.GRASS, test.get(0).getCellType());
	}

}
