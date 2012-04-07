package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import vohra.Cell;
import vohra.CellComparator;
import vohra.Knowledge;
import vohra.MyAnt;

public class KnowledgeTests {

	@Before
	public void setUp() throws Exception {
		ArrayList<Cell> test = new ArrayList<Cell>();
		test.add(new Cell(Cell.TYPE.GRASS, 1, 1));
		test.add(new Cell(Cell.TYPE.FOOD, 4, 4));

		Collections.sort(test, new CellComparator());
		System.out.println(test.get(0));
	}

	@Test
	public void testMapMerge() {
		Knowledge map1 = new Knowledge(1);
		Knowledge map2 = new Knowledge(2);

		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		assertTrue(map1.isUpdated());
		map1.setUpdated(false);
		assertFalse(map1.isUpdated());

		map1.merge(map2);
		assertTrue(map1.isUpdated());
		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		map2.set(new Cell(Cell.TYPE.GRASS, 0, 1));
		map1.merge(map2);
		assertTrue(map1.isUpdated());
		assertEquals(map1.numKnownCells(), 2);
		assertEquals(map2.numKnownCells(), 2);

		map1.setUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isUpdated());

		map2.set(new Cell(Cell.TYPE.FOOD, 2, 2));
		map1.merge(map2);
		assertEquals(map1.numKnownCells(), 3);
		assertEquals(map2.numKnownCells(), 3);
		assertTrue(map1.isUpdated());

		map1.setUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isUpdated());

	}

	public void printPathCells(Knowledge knowledge) {
		System.out.println();
		for (int i = 0; i < knowledge.getCurrRoute().size(); i++)
			System.out.print(knowledge.getCurrRoute().get(i) + " ");
		System.out.println();
	}

	public void printPath(Knowledge knowledge) {
		Stack<Cell> currRoute = knowledge.getCurrRoute();
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		Cell old = knowledge.get(knowledge.x, knowledge.y);
		for (int i = currRoute.size() - 1; i >= 0; i--) {
			MyAnt.debugPrint(1, old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}

}
