package test;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

import ants.Action;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

import vohra.*;

public class MyAntTests {
	MyAnt ant = new MyAnt(3, 1);

	PriorityQueue<Cell> pq;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testUpdatingMap() {
		testSurroundings tS = new testSurroundings();
		ant.origin = 1;

		ant.setXY(1, 1);
		ant.getMap().update(tS, ant.getLocX(), ant.getLocY());
		// printMap();

		assertEquals(1, 1);
	}

	@Test
	public void testReadyMap() {
		testUpdatingMap();
		pq = ant.getMap().beforeSearch(ant.getLocX(), ant.getLocY(), false);
		// System.out.println("pq size: " + pq.size());
		assertEquals(ant.getCell(1, 1).dist, 0);
		assertEquals(ant.getCell(1, 0).dist, Integer.MAX_VALUE);

		assertEquals(5, pq.size());
		// fail("Not yet implemented");
	}

	@Test
	public void testSearch() {
		testReadyMap();

		ant.origin = 1;
		ant.setXY(1, 1);

		// printMap();
		Direction dir = MapOps.makeRoute(ant, ant.getCell(1, 2));
		assertEquals(dir, Direction.SOUTH);
		dir = MapOps.makeRoute(ant, ant.getMap().get(2, 1));
		assertEquals(dir, Direction.EAST);
		dir = MapOps.makeRoute(ant, ant.getCell(1, 0));
		assertEquals(dir, Direction.NORTH);
		dir = MapOps.makeRoute(ant, ant.getCell(0, 1));
		assertEquals(dir, Direction.WEST);

		ant.setXY(0, 1);

		dir = MapOps.makeRoute(ant, ant.getCell(2, 1));
		assertEquals(dir, Direction.EAST);

		ant.setXY(1, 1);

		assertEquals(MapOps.makeRoute(ant, ant.getCell(2, 1)), Direction.EAST);

		ant.setXY(0, 1);
		dir = MapOps.makeRoute(ant, ant.getMap().get(1, 2));
		assertEquals(dir, Direction.EAST);

		ant.setXY(1, 1);
		assertEquals(MapOps.makeRoute(ant, ant.getCell(1, 2)), Direction.SOUTH);

		System.out.println(dir);
		System.out.println(ant.getMap().sizeOfKnowledge());
		// fail("Not yet implemented");
	}

	@Test
	public void testClosestUnexplored() {
		testReadyMap();

		ant.origin = 1;
		ant.setXY(0, 0);
		// printMap();

		// G W W
		// U W U
		// G U G
		ant.getCell(1, 0).setType(WorldMap.type.WALL);
		ant.getCell(1, 1).setType(WorldMap.type.WALL);
		ant.getCell(0, 2).setType(WorldMap.type.GRASS);
		ant.getCell(0, 0).setType(WorldMap.type.GRASS);
		ant.getCell(2, 2).setType(WorldMap.type.GRASS);

		Cell cell = MapOps.findClosest(ant, WorldMap.type.UNEXPLORED);
		System.out.println("target: " + cell);
		Direction dir = MapOps.makeRoute(ant, cell);
		System.out.println(dir);
		assertEquals(Direction.SOUTH, dir);
	}

	@Test
	public void testClosestFood() {
		testReadyMap();

		ant.origin = 1;
		ant.setXY(1, 1);
		ant.getCell(0, 1).setAmntFood(100);
		ant.getCell(0, 1).setType(WorldMap.type.FOOD);
		ant.getCell(1, 1).setAmntFood(100);

		// printMap();
		Action action = ant.findFood("error");
		System.out.println(action);
		assertEquals(Direction.WEST, action.getDirection());
	}

	@Test
	public void testMapInsert() {
		Cell t1 = ant.getCell(1, 1);
		System.out.println(ant.getMap().sizeOfKnowledge());
		Cell t2 = ant.getCell(1, 1);
		System.out.println(ant.getMap().sizeOfKnowledge());

		System.out.println(t1 == t2);

	}

	@Test
	public void testMapMerge() {
		WorldMap map1 = new WorldMap(3, 1, 1);
		WorldMap map2 = new WorldMap(3, 1, 1);

		System.out.println(map1.isRecentlyUpdated());
		assertTrue(map1.isRecentlyUpdated());
		map1.setRecentlyUpdated(false);
		assertFalse(map1.isRecentlyUpdated());

		map1.merge(map2);
		assertFalse(map1.isRecentlyUpdated());

		map2.set(new Cell(WorldMap.type.GRASS, 0, 1));
		map1.merge(map2);
		assertTrue(map1.isRecentlyUpdated());

		map1.setRecentlyUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isRecentlyUpdated());

		MyAnt.induceSleep(1, "test");
		map2.set(new Cell(WorldMap.type.FOOD, 0, 1));
		map1.merge(map2);
		assertTrue(map1.isRecentlyUpdated());
		
		map1.setRecentlyUpdated(false);
		map1.merge(map2);
		assertFalse(map1.isRecentlyUpdated());
		
		
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