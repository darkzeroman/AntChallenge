package vohra.tests;

import static org.junit.Assert.assertEquals;

import java.util.Stack;

import org.junit.Test;

import vohra.Cell;
import vohra.MapOps;
import vohra.MyAnt;
import vohra.Planner;
import vohra.WorldMap;
import vohra.searches.BFS;
import ants.Direction;

public class SearchTests {
	MyAnt ant = new MyAnt();

	@Test
	public void testSearch() {
		Planner searchAlgorithm = new BFS();

		ant = makeSquareGrassMap(3);
		ant.getCell(0, 1).setType(Cell.TYPE.FOOD);
		Stack<Cell> plan = MapOps.makePlan(ant.getWorldMap(),
				ant.getCurrCell(), Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));

		ant = makeSquareGrassMap(3);
		ant.getCell(1, 0).setType(Cell.TYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrCell(),
				Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.EAST, nextPlanDir(plan, ant.getCurrCell()));

		ant = makeSquareGrassMap(3);
		ant.getCell(0, -1).setType(Cell.TYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrCell(),
				Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.SOUTH, nextPlanDir(plan, ant.getCurrCell()));

		ant = makeSquareGrassMap(3);
		ant.getCell(-1, 0).setType(Cell.TYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrCell(),
				Cell.TYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.WEST, nextPlanDir(plan, ant.getCurrCell()));

	}

	@Test
	public void testClosestType() {
		MyAnt ant = new MyAnt();
		ant.setXY(0, 0);
		Planner searchAlgorithm = new BFS();

		// U: Unexplored, W: Water, G: Grass, A: Ant,
		// X: Goal (Either Food or Unexplored)

		// U W X W U
		// U W G W U
		// U W G W U
		// U W G W U
		// U W A W U
		// U W W W U

		int[][] waterCellCoords = new int[][] { { 0, -1 }, { -1, 0 }, { -1, 1 },
				{ -1, 2 }, { -1, 3 }, { -1, 4 }, { 0, -1 }, { 1, 0 }, { 1, 1 },
				{ 1, 2 }, { 1, 3 }, { 1, 4 } };
		int[][] grassCellCoords = new int[][] { { 0, 0 }, { 0, 1 }, { 0, 2 },
				{ 0, 3 }, };

		for (int[] arr : waterCellCoords)
			ant.getCell(arr[0], arr[1]).setType(Cell.TYPE.WATER);
		for (int[] arr : grassCellCoords)
			ant.getCell(arr[0], arr[1]).setType(Cell.TYPE.GRASS);

		Stack<Cell> plan = MapOps.makePlan(ant.getWorldMap(),
				ant.getCurrCell(), Cell.TYPE.UNEXPLORED, searchAlgorithm);
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));

		ant.getCell(0, 4).setType(Cell.TYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrCell(),
				Cell.TYPE.UNEXPLORED, searchAlgorithm);
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrCell()));

	}

	@Test
	public void testAStar() {
		// MyAnt ant = makeSquareGrassMap(4);
		//
		// ant.setXY(0, 0);
		// ant.getCell(1, 1).setType(Cell.TYPE.FOOD);
		// // ant.getCell(1, 0).setNumAnts(100);
		// // ant.getCell(2, 0).setNumAnts(100);
		//
		// // ant.getCell(2, 2).setNumAnts(100);
		// AStar aStar = new AStar();
		// aStar.makePlan(knowledge, knowledge.getCell(1, 1));
		// printPath(knowledge);
		// assertEquals(Direction.NORTH, nextPlanActionDirection(ant));
		// assertEquals(Direction.EAST, nextPlanActionDirection(ant));

	}

	@Test
	public void testReturnPath() {
		MyAnt ant = makeSquareGrassMap(6);

		ant.getWorldMap().getCell(2, 2).setType(Cell.TYPE.FOOD);

		MapOps.makePlan(ant.getWorldMap(), ant.getCurrCell(), Cell.TYPE.FOOD,
				new BFS());
		// Stack<Cell> pathToGoal = (Stack<Cell>) ant.getCurrPlan().clone();

		// MyAnt ant = new MyAnt();

		// assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge);

		// ant.getCell(1, 0).setNumAnts(2);
		// ant.getCell(2, 1).setNumAnts(2);
		// ant.getCell(0, 1).setNumAnts(2);

		// ant.getCell(1, 2).setNumAnts(2);
		// ant.knowledge.setXY(2, 1);
		// MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new Djikstra());
		// assertEquals(Direction.WEST, nextRouteDir(ant.knowledge);
	}

	private MyAnt makeSquareGrassMap(int length) {
		MyAnt ant = new MyAnt();
		WorldMap worldMap = ant.getWorldMap();
		for (int i = -1 * length / 2; i < length / 2; i++)
			for (int j = -1 * length / 2; j < length / 2; j++)
				worldMap.getCell(i, j).setType(Cell.TYPE.GRASS);

		return ant;
	}

	private Direction nextPlanDir(Stack<Cell> plan, Cell currCell) {

		if (plan.size() > 0) {
			Cell to = plan.pop();
			return currCell.dirTo(to);
		}
		return null;
	}

	private void printPath(MyAnt ant) {
		Stack<Cell> currRoute = ant.getCurrPlan();
		System.out
				.println("Printing Path:  (size: " + currRoute.size() + "): ");
		Cell old = ant.getCell(ant.getX(), ant.getY());
		for (int i = currRoute.size() - 1; i >= 0; i--) {
			System.out.println(old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}

}