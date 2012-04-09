package vohra.tests;

import static org.junit.Assert.assertEquals;

import java.util.PriorityQueue;

import org.junit.Test;

import vohra.Cell;
import vohra.Cell.CELLTYPE;

public class CellTest {

	@Test
	public void testCompareTo() {
		Cell a = new Cell(CELLTYPE.WATER, 1, 1);
		a.dist = 10;
		Cell b = new Cell(CELLTYPE.GRASS, 1, 1);
		b.dist = Integer.MAX_VALUE;
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();
		pq.add(a);
		pq.add(b);
		assertEquals(a, pq.poll());
	}

}
