package vohra.tests;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import vohra.BFS;
import vohra.Cell;
import vohra.Cell.CellType;
import vohra.CellComparator;
import vohra.Knowledge;
import vohra.MapOps;

public class KnowledgeTests {

	@Before
	public void setUp() throws Exception {
		ArrayList<Cell> test = new ArrayList<Cell>();
		test.add(new Cell(Cell.CellType.GRASS, 1, 1));
		test.add(new Cell(Cell.CellType.FOOD, 4, 4));

		Collections.sort(test, new CellComparator());
		System.out.println(test.get(0));
	}

	@Test
	public void reverseRoute() {
		Knowledge knowledge = new Knowledge(1);
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				knowledge.get(i, j).setType(Cell.CellType.GRASS);
			}
		MapOps.planRoute(knowledge, knowledge.get(3, 3), new BFS());
		prepareBackHomeRoute(knowledge);
		System.out.println(knowledge.backHomeRoute.firstElement());
		switchRoutes(knowledge);
		int x = 5;
	}

	public void prepareBackHomeRoute(Knowledge knowledge) {
		knowledge.backHomeRoute.push(knowledge.get(0, 0));
		for (int i = knowledge.getCurrRoute().size() - 1; i >= 0; i--) {
			knowledge.backHomeRoute.push(knowledge.getCurrRoute().get(i));
		}
		knowledge.backHomeRoute.pop();
	}

	public void switchRoutes(Knowledge knowledge) {
		knowledge.getCurrRoute().clear();
		for (int i = knowledge.backHomeRoute.size() - 1; i >= 0; i--) {
			knowledge.getCurrRoute().push(knowledge.backHomeRoute.get(i));
		}
		int x = 5;
	}

}
