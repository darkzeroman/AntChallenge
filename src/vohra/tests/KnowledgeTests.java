package vohra.tests;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import vohra.BFS;
import vohra.Cell;
import vohra.Cell.CellType;
import vohra.CellComparator;
import vohra.Knowledge;
import vohra.MapOps;
import vohra.MyAnt;

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
		// System.out.println(knowledge.getCurrRoute().pop());
		printPathCells(knowledge);
		knowledge.setCurrLoc(new Point(3, 3));
		prepareBackHomeRoute(knowledge);

		switchRoutes(knowledge);
		printPathCells(knowledge);
		
		System.out.println(knowledge.getCurrRoute().firstElement());
		// System.out.println("pop: " + knowledge.getCurrRoute().pop());
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
		for (int i = 0; i < knowledge.backHomeRoute.size(); i++) {
			knowledge.getCurrRoute().push(knowledge.backHomeRoute.get(i));
		}
		//knowledge.getCurrRoute().pop();
		int x = 5;
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
		Cell old = knowledge.get(knowledge.getLocX(), knowledge.getLocY());
		for (int i = currRoute.size() - 1; i >= 0; i--) {
			MyAnt.debugPrint(1, old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}

}
