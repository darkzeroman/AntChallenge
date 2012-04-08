package vohra.tests;

import static org.junit.Assert.assertEquals;

import java.util.Stack;

import org.junit.Test;

import vohra.Cell;
import vohra.Cell.CELLTYPE;
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
		// Creating a world with food one cell away and making sure search 
		// algorithm matches the expected output
		
		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(0, 1).setCellType(CELLTYPE.FOOD);
		Stack<Cell> plan = MapOps.makePlan(ant.getWorldMap(),
				ant.getCurrentCell(), CELLTYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));

		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(1, 0).setCellType(CELLTYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrentCell(),
				CELLTYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.EAST, nextPlanDir(plan, ant.getCurrentCell()));

		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(0, -1).setCellType(CELLTYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrentCell(),
				CELLTYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.SOUTH, nextPlanDir(plan, ant.getCurrentCell()));

		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(-1, 0).setCellType(CELLTYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrentCell(),
				CELLTYPE.FOOD, searchAlgorithm);
		assertEquals(Direction.WEST, nextPlanDir(plan, ant.getCurrentCell()));

	}

	@Test
	public void testClosestType() {
		ant = new MyAnt();
		Planner searchAlgorithm = new BFS();
		// Test to check if plan can work for multiple cells away
		
		// U: Unexplored, W: Water, G: Grass, A: Ant,
		// X: Goal (Either Food or Unexplored)
		
		// Map that is created: A is origin
		// U W X W U
		// U W G W U
		// U W G W U
		// U W G W U
		// U W A W U
		// U W W W U

		int[][] waterCellCoords = new int[][] { { 0, -1 }, { -1, 0 },
				{ -1, 1 }, { -1, 2 }, { -1, 3 }, { -1, 4 }, { 0, -1 },
				{ 1, 0 }, { 1, 1 }, { 1, 2 }, { 1, 3 }, { 1, 4 } };
		int[][] grassCellCoords = new int[][] { { 0, 0 }, { 0, 1 }, { 0, 2 },
				{ 0, 3 }, };

		for (int[] arr : waterCellCoords)
			ant.getCell(arr[0], arr[1]).setCellType(CELLTYPE.WATER);
		for (int[] arr : grassCellCoords)
			ant.getCell(arr[0], arr[1]).setCellType(CELLTYPE.GRASS);

		Cell currCell = ant.getCurrentCell();
		
		Stack<Cell> plan = MapOps.makePlan(ant.getWorldMap(),
				ant.getCurrentCell(), CELLTYPE.UNEXPLORED, searchAlgorithm);
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));

		ant.getCell(0, 4).setCellType(CELLTYPE.FOOD);
		plan = MapOps.makePlan(ant.getWorldMap(), ant.getCurrentCell(),
				CELLTYPE.UNEXPLORED, searchAlgorithm);
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));

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
		MyAnt ant = makeAntInSquareWorldWithGrass(6);

		ant.getWorldMap().getCell(2, 2).setCellType(CELLTYPE.FOOD);

		MapOps.makePlan(ant.getWorldMap(), ant.getCurrentCell(),
				CELLTYPE.FOOD, new BFS());
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

	private MyAnt makeAntInSquareWorldWithGrass(int dimension) {
		MyAnt ant = new MyAnt();
		WorldMap worldMap = ant.getWorldMap();
		for (int i = -1 * dimension / 2; i < dimension / 2; i++)
			for (int j = -1 * dimension / 2; j < dimension / 2; j++)
				worldMap.getCell(i, j).setCellType(CELLTYPE.GRASS);

		return ant;
	}

	private Direction nextPlanDir(Stack<Cell> plan, Cell currCell) {

		if (plan.size() > 0) {
			Cell to = plan.pop();
			return currCell.dirTo(to);
		}
		return null;
	}

}