package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

import vohra.BFS;
import vohra.Cell;
import vohra.Knowledge;
import vohra.MapOps;
import vohra.MyAnt;
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
			ant.getCell(arr[0], arr[1]).setType(Cell.CellType.WALL);
		for (int[] arr : grass)
			ant.getCell(arr[0], arr[1]).setType(Cell.CellType.GRASS);

		MapOps.newMakeRoute(ant.knowledge, Cell.CellType.UNEXPLORED, "test");
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
		assertEquals(Direction.WEST, ant.nextRouteDir());
	}

	@Test
	public void testMapInsert() {

	}

	@Test
	public void testMapMerge() {
		Knowledge map1 = new Knowledge(1);
		Knowledge map2 = new Knowledge(2);

		assertEquals(map1.numKnownCells(), 1);
		assertTrue(map1.isUpdated());
		map1.setUpdated(false);
		assertFalse(map1.isUpdated());

		map1.merge(map2);
		assertFalse(map1.isUpdated());

		map2.set(new Cell(Cell.CellType.GRASS, 0, 1));
		map1.merge(map2);
		assertTrue(map1.isUpdated());

		map1.setUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isUpdated());

		MyAnt.induceSleep(1000, "test");

		map2.set(new Cell(Cell.CellType.FOOD, 0, 1));
		MyAnt.induceSleep(1000, "test");

		map1.merge(map2);

		assertTrue(map1.isUpdated());

		map1.setUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isUpdated());

	}

	@Test
	public void testReturnPath() {

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				ant.getCell(i, j).setType(Cell.CellType.GRASS);
			}

		ant.setXY(0, 0);

		MapOps.planRoute(ant.knowledge, ant.getCell(2, 1), new BFS());
		assertEquals(Direction.NORTH, ant.nextRouteDir());

		// ant.getCell(1, 0).setNumAnts(2);
		ant.getCell(1, 1).setNumAnts(2);
		// ant.getCell(2, 1).setNumAnts(2);
		// ant.getCell(0, 1).setNumAnts(2);

		// ant.getCell(1, 2).setNumAnts(2);
		ant.setXY(2, 1);
		MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new BFS());
		assertEquals(Direction.WEST, ant.nextRouteDir());

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