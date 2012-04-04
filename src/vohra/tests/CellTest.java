package vohra.tests;

import static org.junit.Assert.assertEquals;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

import vohra.Cell;
import vohra.Knowledge;

public class CellTest {

	@Before
	public void setUp() throws Exception {
		Cell a;
	}

	@Test
	public void testCompareTo() {
		Cell a = new Cell(Cell.type.WALL, 1, 1);
		a.dist = 10;
		Cell b = new Cell(Cell.type.GRASS, 1, 1);
		b.dist = Integer.MAX_VALUE;
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>();
		pq.add(a);
		pq.add(b);
		assertEquals(a, pq.poll());
	}

}
