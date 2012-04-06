package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.PriorityQueue;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import vohra.AStar;
import vohra.BFS;
import vohra.Cell;
import vohra.Knowledge;
import vohra.MapOps;
import vohra.MyAnt;
import vohra.Cell.CellType;
import ants.Action;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class MyAntTests {
	MyAnt ant = new MyAnt();

	PriorityQueue<Cell> pq;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testUpdatingMap() {
		testSurroundings tS = new testSurroundings();
		Knowledge knowledge = new Knowledge(1);
		knowledge.updateMap(tS, ant.getLocX(), ant.getLocY());
		// printMap();

		assertEquals(1, 1);
	}

	@Test
	public void testReadyMap() {
		testUpdatingMap();
		Knowledge knowledge = new Knowledge(1);
		knowledge.get(0, 1);
		pq = knowledge.beforeSearch(true);
		// System.out.println("pq size: " + pq.size());
		assertEquals(knowledge.get(0, 0).dist, 0);
		assertEquals(knowledge.get(0, 1).dist, Integer.MAX_VALUE);

		assertEquals(2, pq.size());
		// fail("Not yet implemented");
	}

	@Test
	public void testSearch() {
		testReadyMap();

		ant.setXY(0, 0);

		// printMap();
		MapOps.planRoute(ant.knowledge, ant.getCell(0, 1), new BFS());
		assertEquals(Direction.NORTH, ant.nextRouteDir());

		MapOps.planRoute(ant.knowledge, ant.getCell(1, 0), new BFS());
		assertEquals(ant.nextRouteDir(), Direction.EAST);

		MapOps.planRoute(ant.knowledge, ant.getCell(0, -1), new BFS());
		assertEquals(ant.nextRouteDir(), Direction.SOUTH);

		MapOps.planRoute(ant.knowledge, ant.getCell(-1, 0), new BFS());
		assertEquals(ant.nextRouteDir(), Direction.WEST);

		ant.setXY(0, 1);
		MapOps.planRoute(ant.knowledge, ant.getCell(2, 1), new BFS());
		assertEquals(ant.nextRouteDir(), Direction.EAST);

		ant.setXY(1, 1);
		MapOps.planRoute(ant.knowledge, ant.getCell(2, 1), new BFS());
		assertEquals(ant.nextRouteDir(), Direction.EAST);

		// fail("Not yet implemented");
	}

	@Test
	public void testClosestUnexplored() {
		testReadyMap();
		ant = new MyAnt();
		ant.setXY(0, 0);
		// printMap();

		// U W U W U
		// U W G W U
		// U W G W U
		// U W A W U
		// U W W W U

		int[][] walls = new int[][] { { 0, -1 }, { -1, 0 }, { -1, 1 },
				{ -1, 2 }, { -1, 3 }, { -1, 4 }, { 0, -1 }, { 1, 0 }, { 1, 1 },
				{ 1, 2 }, { 1, 3 }, { 1, 4 } };
		int[][] grass = new int[][] { { 0, 0 }, { 0, 1 }, { 0, 2 }, { 0, 3 }, };

		for (int[] arr : walls)
			ant.getCell(arr[0], arr[1]).setType(Cell.CellType.WATER);
		for (int[] arr : grass)
			ant.getCell(arr[0], arr[1]).setType(Cell.CellType.GRASS);

		MapOps.planRoute(ant.knowledge, Cell.CellType.UNEXPLORED, new BFS());
		assertEquals(Direction.NORTH, ant.nextRouteDir());
	}

	public static Direction dir(Action action) {
		return action.getDirection();
	}

	@Test
	public void testClosestFood() {
		testReadyMap();

		ant.setXY(1, 1);
		ant.getCell(0, 1).setAmountOfFood(100);
		ant.getCell(0, 1).setType(Cell.CellType.FOOD);
		ant.getCell(1, 1).setAmountOfFood(100);

		// printMap();
		ant.foundFood("error");
		// assertEquals(Direction.WEST, ant.nextRouteDir());
		assertTrue(true);
	}

	@Test
	public void testMapInsert() {

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

		map2.set(new Cell(Cell.CellType.GRASS, 0, 1));
		map1.merge(map2);
		assertTrue(map1.isUpdated());
		assertEquals(map1.numKnownCells(), 2);
		assertEquals(map2.numKnownCells(), 2);

		map1.setUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isUpdated());

		map2.set(new Cell(Cell.CellType.FOOD, 2, 2));
		map1.merge(map2);
		assertEquals(map1.numKnownCells(), 3);
		assertEquals(map2.numKnownCells(), 3);
		assertTrue(map1.isUpdated());

		map1.setUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isUpdated());

		map1.updateMap(new testSurroundings(), 0, 0);
		assertEquals(map1.numKnownCells(), 6);

	}

	@Test
	public void testReturnPath() {

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				ant.getCell(i, j).setType(Cell.CellType.GRASS);
			}

		ant.setXY(0, 0);

		MapOps.planRoute(ant.knowledge, ant.getCell(2, 1), new BFS());
		// assertEquals(Direction.NORTH, ant.nextRouteDir());

		// ant.getCell(1, 0).setNumAnts(2);
		ant.getCell(1, 1).setNumAnts(2);
		// ant.getCell(2, 1).setNumAnts(2);
		// ant.getCell(0, 1).setNumAnts(2);

		// ant.getCell(1, 2).setNumAnts(2);
		ant.setXY(2, 1);
		MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new BFS());
		// assertEquals(Direction.WEST, ant.nextRouteDir());
	}

	@Test
	public void testAStar() {
		testReadyMap();

		ant.setXY(0, 0);

		// printMap();
		MapOps.planRoute(ant.knowledge, ant.getCell(1, 0), new AStar());
		assertEquals(Direction.EAST, ant.nextRouteDir());

		MapOps.planRoute(ant.knowledge, ant.getCell(0, 1), new AStar());
		assertEquals(Direction.NORTH, ant.nextRouteDir());

		MapOps.planRoute(ant.knowledge, ant.getCell(0, -1), new AStar());
		assertEquals(Direction.SOUTH, ant.nextRouteDir());

		MapOps.planRoute(ant.knowledge, ant.getCell(-1, 0), new AStar());
		assertEquals(Direction.WEST, ant.nextRouteDir());

	}

	@Test
	public void testAStarWithAnts() {
		ant = new MyAnt();
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5; j++) {
				Cell cell = ant.knowledge.get(i, j);
				cell.setType(CellType.GRASS);

			}

		ant.knowledge.getCurrRoute().clear();
		ant.setXY(0, 0);
		ant.getCell(1, 0).setNumAnts(100);
		// ant.getCell(2, 0).setNumAnts(100);

		// ant.getCell(2, 2).setNumAnts(100);

		MapOps.planRoute(ant.knowledge, ant.getCell(4, 4), new AStar());
		printPath(ant.knowledge);
		assertEquals(Direction.EAST, ant.nextRouteDir());
		assertEquals(Direction.EAST, ant.nextRouteDir());
		assertEquals(Direction.EAST, ant.nextRouteDir());
		assertEquals(Direction.EAST, ant.nextRouteDir());
		ant.setXY(4, 0);
		assertEquals(Direction.NORTH, ant.nextRouteDir());
		assertEquals(Direction.NORTH, ant.nextRouteDir());
		assertEquals(Direction.NORTH, ant.nextRouteDir());
		assertEquals(Direction.NORTH, ant.nextRouteDir());

		ant.setXY(4, 4);
		MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new AStar());
		printPath(ant.knowledge);

	}

	@Test
	public void testAStarWithAnts2() {
		ant = new MyAnt();
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5; j++) {
				Cell cell = ant.knowledge.get(i, j);
				cell.setType(CellType.GRASS);
			}

		ant.knowledge.getCurrRoute().clear();
		ant.setXY(0, 0);

		ant.getCell(0, 2).setType(Cell.CellType.WATER);
		ant.getCell(0, 3).setType(Cell.CellType.WATER);
		ant.getCell(0, 4).setType(Cell.CellType.WATER);
		// ant.getCell(2, 0).setNumAnts(10);
		for (int i = 0; i < 10; i++) {
			MapOps.planRoute(ant.knowledge, ant.getCell(3, 4), new AStar());
			printPath(ant.knowledge);

		}
		ant.setXY(3, 4);

		MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new AStar());

	}

	@Test
	public void testAStarWithAnts3() {
		ant = new MyAnt();
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5; j++) {
				Cell cell = ant.knowledge.get(i, j);
				cell.setType(CellType.GRASS);
			}

		ant.knowledge.getCurrRoute().clear();
		ant.setXY(2, 3);

		ant.getCell(2, 0).setType(Cell.CellType.WATER);

		// ant.getCell(2, 0).setNumAnts(10);
		for (int i = 0; i < 1; i++) {
			MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new AStar());
			this.printPath(ant.knowledge);
			assertEquals(ant.knowledge.get(1, 3), ant.knowledge.getCurrRoute()
					.pop());
			assertEquals(ant.knowledge.get(0, 3), ant.knowledge.getCurrRoute()
					.pop());
			assertEquals(ant.knowledge.get(0, 2), ant.knowledge.getCurrRoute()
					.pop());
			assertEquals(ant.knowledge.get(0, 1), ant.knowledge.getCurrRoute()
					.pop());
			assertEquals(ant.knowledge.get(0, 0), ant.knowledge.getCurrRoute()
					.pop());

		}
		ant.setXY(0, 0);

		MapOps.planRoute(ant.knowledge, ant.getCell(2, 3), new AStar());
		// printPath(ant.knowledge);

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

	private class TestTile implements Tile {

		@Override
		public int getAmountOfFood() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getNumAnts() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isTravelable() {
			// TODO Auto-generated method stub
			return true;
		}

	}

	private class testSurroundings implements Surroundings {

		@Override
		public Tile getCurrentTile() {
			// TODO Auto-generated method stub
			return new TestTile();
		}

		@Override
		public Tile getTile(Direction direction) {
			// TODO Auto-generated method stub
			return new TestTile();
		}

	}

}