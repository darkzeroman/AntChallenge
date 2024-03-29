package vohra.tests;

import static org.junit.Assert.assertEquals;

import java.util.Stack;

import org.junit.Test;

import vohra.Cell;
import vohra.Cell.CELLTYPE;
import vohra.Planner;
import vohra.VohraAnt;
import vohra.WorldMap;
import vohra.searches.BFS;
import ants.Direction;

public class SearchTests {
	VohraAnt ant = new VohraAnt();
	final Planner planner = BFS.getSingleInstance();

	/**
	 * Creating an ant in a square world with food one square away for all of
	 * the cardinal directions and making sure the search algorithm produces the
	 * expected output.
	 */
	@Test
	public void testSearchFood() {

		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(0, 1).setCellType(CELLTYPE.FOOD);
		Stack<Cell> plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.FOOD);
		Direction direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.NORTH, direction);

		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(1, 0).setCellType(CELLTYPE.FOOD);
		plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.FOOD);
		direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.EAST, direction);

		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(0, -1).setCellType(CELLTYPE.FOOD);
		plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.FOOD);
		direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.SOUTH, direction);

		ant = makeAntInSquareWorldWithGrass(3);
		ant.getCell(-1, 0).setCellType(CELLTYPE.FOOD);
		plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.FOOD);
		direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.WEST, direction);

	}

	/**
	 * Creating an ant with an unexplored cell one square away in all the
	 * cardinal directions. Then making sure when an "UNEXPLORED" search
	 * happens, the ant finds the expected cell.
	 */
	@Test
	public void testSearchUnexplored() {

		ant = makeAntInSquareWorldWithGrass(4);
		ant.getCell(0, 1).setCellType(CELLTYPE.UNEXPLORED);
		Stack<Cell> plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.UNEXPLORED);
		Direction direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.NORTH, direction);

		ant = makeAntInSquareWorldWithGrass(4);
		ant.getCell(1, 0).setCellType(CELLTYPE.UNEXPLORED);
		plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.UNEXPLORED);
		direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.EAST, direction);

		ant = makeAntInSquareWorldWithGrass(4);
		ant.getCell(0, -1).setCellType(CELLTYPE.UNEXPLORED);
		plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.UNEXPLORED);
		direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.SOUTH, direction);

		ant = makeAntInSquareWorldWithGrass(4);
		ant.getCell(-1, 0).setCellType(CELLTYPE.UNEXPLORED);
		plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.UNEXPLORED);
		direction = nextPlanDir(plan, ant.getCurrentCell());
		assertEquals(Direction.WEST, direction);

	}

	/**
	 * Test to check if plan can work if the target is multiple cells away
	 */
	@Test
	public void testClosestType() {
		ant = this.makeAntInSquareWorldWithGrass(10);
		ant.getCell(0, 4).setCellType(CELLTYPE.UNEXPLORED);

		// U: Unexplored, W: Water, G: Grass, A: Ant,
		// X: Goal (Either Food or Unexplored)

		// Relevant portion of map that is created: A is origin/ant
		// U W X W U
		// U W G W U
		// U W G W U
		// U W G W U
		// U W A W U
		// U W W W U

		int[][] waterCellCoords = new int[][] { { 0, -1 }, { -1, 0 }, { -1, 1 }, { -1, 2 }, { -1, 3 }, { -1, 4 },
				{ 0, -1 }, { 1, 0 }, { 1, 1 }, { 1, 2 }, { 1, 3 }, { 1, 4 }, { 0, 5 } };
		int[][] grassCellCoords = new int[][] { { 0, 0 }, { 0, 1 }, { 0, 2 }, { 0, 3 }, };

		for (int[] arr : waterCellCoords)
			ant.getCell(arr[0], arr[1]).setCellType(CELLTYPE.WATER);
		for (int[] arr : grassCellCoords)
			ant.getCell(arr[0], arr[1]).setCellType(CELLTYPE.GRASS);

		Stack<Cell> plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.UNEXPLORED);

		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));

		// Switching goal type to food, still should be same answer
		ant.getCell(0, 4).setCellType(CELLTYPE.FOOD);
		plan = planner.makePlan(ant.getWorldMap(), ant.getCurrentCell(), CELLTYPE.FOOD);
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
		assertEquals(Direction.NORTH, nextPlanDir(plan, ant.getCurrentCell()));
	}

	/**
	 * Makes an ant in a world of given dimension centered around origin.
	 * Example: 5 leads to a world with coordinates -2 to 2 for both x and y
	 */
	private VohraAnt makeAntInSquareWorldWithGrass(int dimension) {
		VohraAnt ant = new VohraAnt();
		WorldMap worldMap = ant.getWorldMap();
		for (int i = -1 * dimension / 2; i < dimension / 2; i++)
			for (int j = -1 * dimension / 2; j < dimension / 2; j++)
				worldMap.getCell(i, j).setCellType(CELLTYPE.GRASS);

		return ant;
	}

	/** Gets the direction from the ant's current plan action */
	private Direction nextPlanDir(Stack<Cell> plan, Cell from) {
		if (plan.size() > 0) {
			Cell to = plan.pop();
			return from.directionTo(to);
		}
		return null;
	}

}