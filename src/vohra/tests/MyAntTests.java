package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.PriorityQueue;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import vohra.AStar;
import vohra.BFS;
import vohra.Cell;
import vohra.Cell.TYPE;
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
		knowledge.updateMap(tS);
		// printMap();

		assertEquals(1, 1);
	}

	@Test
	public void testReadyMap() {
		testUpdatingMap();
		Knowledge knowledge = new Knowledge(1);
		knowledge.get(0, 1);
		pq = knowledge.preSearch(true);
		// System.out.println("pq size: " + pq.size());
		assertEquals(knowledge.get(0, 0).dist, 0);
		assertEquals(knowledge.get(0, 1).dist, Integer.MAX_VALUE);

		assertEquals(2, pq.size());
		// fail("Not yet implemented");
	}

	@Test
	public void testSearch() {
		testReadyMap();

		ant.knowledge.x = 0;
		ant.knowledge.y = 0;
		// printMap();
		MapOps.planRoute(ant.knowledge, ant.getCell(0, 1), new BFS());
		assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge));

		MapOps.planRoute(ant.knowledge, ant.getCell(1, 0), new BFS());
		assertEquals(nextRouteDir(ant.knowledge), Direction.EAST);

		MapOps.planRoute(ant.knowledge, ant.getCell(0, -1), new BFS());
		assertEquals(nextRouteDir(ant.knowledge), Direction.SOUTH);

		MapOps.planRoute(ant.knowledge, ant.getCell(-1, 0), new BFS());
		assertEquals(nextRouteDir(ant.knowledge), Direction.WEST);

		ant.knowledge.setXY(0, 1);
		MapOps.planRoute(ant.knowledge, ant.getCell(2, 1), new BFS());
		assertEquals(nextRouteDir(ant.knowledge), Direction.EAST);

		ant.knowledge.setXY(1, 1);
		MapOps.planRoute(ant.knowledge, ant.getCell(2, 1), new BFS());
		assertEquals(nextRouteDir(ant.knowledge), Direction.EAST);

		// fail("Not yet implemented");
	}

	private Direction nextRouteDir(Knowledge knowledge) {
		if (knowledge.getCurrRoute().size() > 0) {
			Cell from = knowledge.getCurrCell();
			Cell to = knowledge.getCurrRoute().pop();
			return from.dirTo(to);
		}
		return null;
	}

	@Test
	public void testClosestUnexplored() {
		testReadyMap();
		ant = new MyAnt();
		ant.knowledge.setXY(0, 0);
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
			ant.getCell(arr[0], arr[1]).setType(Cell.TYPE.WATER);
		for (int[] arr : grass)
			ant.getCell(arr[0], arr[1]).setType(Cell.TYPE.GRASS);

		MapOps.planRoute(ant.knowledge, Cell.TYPE.UNEXPLORED, new BFS());
		assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge));
	}

	public static Direction dir(Action action) {
		return action.getDirection();
	}

	@Test
	public void testClosestFood() {
		testReadyMap();

		ant.knowledge.setXY(1, 1);
		ant.getCell(0, 1).setAmountOfFood(100);
		ant.getCell(0, 1).setType(Cell.TYPE.FOOD);
		ant.getCell(1, 1).setAmountOfFood(100);

		// printMap();
		ant.canFindFood();
		// assertEquals(Direction.WEST, nextRouteDir(ant.knowledge);
		assertTrue(true);
	}

	@Test
	public void testMapInsert() {

	}

	@Test
	public void testReturnPath() {

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				ant.getCell(i, j).setType(Cell.TYPE.GRASS);
			}
		ant.getCell(2, 2).setType(Cell.TYPE.FOOD);

		ant.knowledge.setXY(0, 0);

		MapOps.planRoute(ant.knowledge, Cell.TYPE.FOOD, new BFS());
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

	@Test
	public void testAStar() {
		testReadyMap();

		ant.knowledge.setXY(0, 0);

		// printMap();
		MapOps.planRoute(ant.knowledge, ant.getCell(1, 0), new AStar());
		assertEquals(Direction.EAST, nextRouteDir(ant.knowledge));

		MapOps.planRoute(ant.knowledge, ant.getCell(0, 1), new AStar());
		assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge));

		MapOps.planRoute(ant.knowledge, ant.getCell(0, -1), new AStar());
		assertEquals(Direction.SOUTH, nextRouteDir(ant.knowledge));

		MapOps.planRoute(ant.knowledge, ant.getCell(-1, 0), new AStar());
		assertEquals(Direction.WEST, nextRouteDir(ant.knowledge));

	}

	@Test
	public void testAStarWithAnts() {
		ant = new MyAnt();
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				Cell cell = ant.knowledge.get(i, j);
				cell.setType(TYPE.GRASS);

			}

		ant.knowledge.getCurrRoute().clear();
		ant.knowledge.setXY(0, 0);
		// ant.getCell(1, 0).setNumAnts(100);
		// ant.getCell(2, 0).setNumAnts(100);

		// ant.getCell(2, 2).setNumAnts(100);

		MapOps.planRoute(ant.knowledge, ant.getCell(2, 2), new AStar());
		printPath(ant.knowledge);
		assertEquals(Direction.EAST, nextRouteDir(ant.knowledge));
		assertEquals(Direction.EAST, nextRouteDir(ant.knowledge));
		assertEquals(Direction.EAST, nextRouteDir(ant.knowledge));
		// ant.setXY(4, 0);
		// assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge);
		// assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge);
		// assertEquals(Direction.NORTH, nextRouteDir(ant.knowledge);
		//
		// ant.setXY(4, 4);
		// //MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new AStar());
		// printPath(ant.knowledge);

	}

	@Test
	public void testAStarWithAnts2() {
		ant = new MyAnt();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				Cell cell = ant.knowledge.get(i, j);
				cell.setType(TYPE.GRASS);
			}

		ant.knowledge.getCurrRoute().clear();
		ant.knowledge.setXY(0, 0);

		ant.getCell(0, 2).setType(Cell.TYPE.WATER);
		ant.getCell(0, 3).setType(Cell.TYPE.WATER);
		ant.getCell(0, 4).setType(Cell.TYPE.WATER);
		// ant.getCell(2, 0).setNumAnts(10);
		for (int i = 0; i < 10; i++) {
			MapOps.planRoute(ant.knowledge, ant.getCell(3, 3), new AStar());
			printPath(ant.knowledge);

		}
		ant.knowledge.setXY(3, 4);

		MapOps.planRoute(ant.knowledge, ant.getCell(0, 0), new AStar());

	}

	@Test
	public void testAStarWithAnts3() {
		ant = new MyAnt();
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5; j++) {
				ant.knowledge.set(new Cell(TYPE.GRASS, i, j));

			}

		ant.knowledge.getCurrRoute().clear();
		ant.knowledge.setXY(3, 3);

		// ant.getCell(2, 0).setType(Cell.TYPE.WATER);
		// ant.getCell(1, 0).setType(Cell.TYPE.WATER);
		// ant.getCell(1, 1).setType(Cell.TYPE.WATER);

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
		ant.knowledge.setXY(0, 0);

		MapOps.planRoute(ant.knowledge, ant.getCell(2, 3), new AStar());
		// printPath(ant.knowledge);

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