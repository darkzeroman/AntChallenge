package vohra.tests;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;

import vohra.Cell;
import vohra.CellComparator;

public class KnowledgeTests {

	@Before
	public void setUp() throws Exception {
		ArrayList<Cell> test = new ArrayList<Cell>();
		test.add(new Cell(Cell.CellType.GRASS, 1, 1));
		test.add(new Cell(Cell.CellType.FOOD, 4, 4));

		Collections.sort(test, new CellComparator());
		System.out.println(test.get(0));
	}

}
