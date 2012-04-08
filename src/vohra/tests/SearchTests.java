package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.PriorityQueue;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import vohra.Cell;
import vohra.Cell.TYPE;
import vohra.searches.AStar;
import vohra.searches.BFS;
import vohra.searches.Djikstra;
import vohra.Knowledge;
import vohra.MapOps;
import vohra.MyAnt;
import vohra.Planner;
import ants.Action;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class SearchTests {

	MyAnt ant = new MyAnt();

	@Test
	public void testSearch() {
		Knowledge knowledge;
		Planner searchAlgorithm = new BFS();

		knowledge = makeSquareGrassMap(3);
		knowledge.getCell(0, 1).setType(Cell.TYPE.FOOD);
		MapOps.makePlan(knowledge, Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));

		knowledge = makeSquareGrassMap(3);
		knowledge.getCell(1, 0).setType(Cell.TYPE.FOOD);
		MapOps.makePlan(knowledge, Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.EAST, nextRouteDir(knowledge));

		knowledge = makeSquareGrassMap(3);
		knowledge.getCell(0, -1).setType(Cell.TYPE.FOOD);
		MapOps.makePlan(knowledge, Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.SOUTH, nextRouteDir(knowledge));

		knowledge = makeSquareGrassMap(3);
		knowledge.getCell(-1, 0).setType(Cell.TYPE.FOOD);
		MapOps.makePlan(knowledge, Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.WEST, nextRouteDir(knowledge));

	}

	@Test
	public void testClosestType() {
		Knowledge knowledge = new Knowledge(0);
		knowledge.setXY(0, 0);
		Planner searchAlgorithm = new BFS();

		// U: Unexplored, W: Water, G: Grass, A: Ant,
		// X: Goal (Either Food or Unexplored)

		// U W X W U
		// U W G W U
		// U W G W U
		// U W G W U
		// U W A W U
		// U W W W U

		int[][] waterCoords = new int[][] { { 0, -1 }, { -1, 0 }, { -1, 1 },
				{ -1, 2 }, { -1, 3 }, { -1, 4 }, { 0, -1 }, { 1, 0 }, { 1, 1 },
				{ 1, 2 }, { 1, 3 }, { 1, 4 } };
		int[][] grassCoords = new int[][] { { 0, 0 }, { 0, 1 }, { 0, 2 },
				{ 0, 3 }, };

		for (int[] arr : waterCoords)
			knowledge.getCell(arr[0], arr[1]).setType(Cell.TYPE.WATER);
		for (int[] arr : grassCoords)
			knowledge.getCell(arr[0], arr[1]).setType(Cell.TYPE.GRASS);

		MapOps.makePlan(knowledge, Cell.TYPE.UNEXPLORED, searchAlgorithm);
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));

		knowledge.getCell(0, 4).setType(Cell.TYPE.FOOD);
		MapOps.makePlan(knowledge, Cell.TYPE.UNEXPLORED, searchAlgorithm);
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));

	}

	@Test
	public void testAStar() {
		Knowledge knowledge = makeSquareGrassMap(4);

		knowledge.setXY(0, 0);
		knowledge.getCell(1, 1).setType(Cell.TYPE.FOOD);
		// ant.getCell(1, 0).setNumAnts(100);
		// ant.getCell(2, 0).setNumAnts(100);

		// ant.getCell(2, 2).setNumAnts(100);
		AStar aStar = new AStar();
		aStar.makePlan(knowledge, knowledge.getCell(1, 1));
		printPath(knowledge);
		assertEquals(Direction.NORTH, nextRouteDir(knowledge));
		assertEquals(Direction.EAST, nextRouteDir(knowledge));

	}

	@Test
	public void testReturnPath() {

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				ant.getCell(i, j).setType(Cell.TYPE.GRASS);
			}
		ant.getCell(2, 2).setType(Cell.TYPE.FOOD);

		ant.knowledge.setXY(0, 0);

		MapOps.makePlan(ant.knowledge, Cell.TYPE.FOOD, new BFS());
		this.printPath(ant.knowledge);
		// assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge);

		// ant.getCell(1, 0).setNumAnts(2);
		// ant.getCell(2, 1).setNumAnts(2);
		// ant.getCell(0, 1).setNumAnts(2);

		// ant.getCell(1, 2).setNumAnts(2);
		ant.knowledge.setXY(2, 1);
		// MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new Djikstra());
		// assertEquals(Direction.WEST, nextRouteDir(ant.knowledge);
	}

	private Knowledge makeSquareGrassMap(int length) {
		Knowledge knowledge = new Knowledge(1);
		for (int i = -1 * length / 2; i < length / 2; i++)
			for (int j = -1 * length / 2; j < length / 2; j++)
				knowledge.getCell(i, j).setType(Cell.TYPE.GRASS);

		return knowledge;
	}

	private Direction nextRouteDir(Knowledge knowledge) {
		if (knowledge.getCurrPlan().size() > 0) {
			Cell from = knowledge.getCurrCell();
			Cell to = knowledge.getCurrPlan().pop();
			return from.dirTo(to);
		}
		return null;
	}

	private void printPath(Knowledge knowledge) {
		Stack<Cell> currRoute = knowledge.getCurrPlan();
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		Cell old = knowledge.getCell(knowledge.x, knowledge.y);
		for (int i = currRoute.size() - 1; i >= 0; i--) {
			MyAnt.debugPrint(1, old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}

}