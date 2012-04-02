package test;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

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

		ant.locX = 1;
		ant.locY = 1;
		ant.map.updateMap(tS, ant.locX, ant.locY);
		// printMap();

		assertEquals(1, 1);
	}

	@Test
	public void testReadyMap() {
		testUpdatingMap();
		pq = ant.map.beforeSearch(ant.locX, ant.locY, false);
		// System.out.println("pq size: " + pq.size());
		assertEquals(ant.map.get(1, 1).dist, 0);
		assertEquals(ant.map.get(1, 0).dist, Integer.MAX_VALUE);

		assertEquals(pq.size(), 6);
		// fail("Not yet implemented");
	}

	@Test
	public void testSearch() {
		testReadyMap();

		ant.origin = 1;
		ant.locX = 1;
		ant.locY = 1;
		// printMap();
		Direction dir = MapOps.search(ant, ant.map.get(1, 2));
		assertEquals(dir, Direction.SOUTH);
		dir = MapOps.search(ant, ant.map.get(2, 1));
		assertEquals(dir, Direction.EAST);
		dir = MapOps.search(ant, ant.map.get(1, 0));
		assertEquals(dir, Direction.NORTH);
		dir = MapOps.search(ant, ant.map.get(0, 1));
		assertEquals(dir, Direction.WEST);

		ant.locX = 0;
		ant.locY = 1;
		dir = MapOps.search(ant, ant.map.get(2, 1));
		assertEquals(dir, Direction.EAST);

		ant.locX = 1;
		ant.locY = 1;
		assertEquals(MapOps.search(ant, ant.map.get(2, 1)), Direction.EAST);

		ant.locX = 0;
		ant.locY = 1;
		dir = MapOps.search(ant, ant.map.get(1, 2));
		assertEquals(dir, Direction.EAST);

		ant.locX = 1;
		ant.locY = 1;
		assertEquals(MapOps.search(ant, ant.map.get(1, 2)), Direction.SOUTH);

		System.out.println(dir);
		System.out.println(ant.map.sizeOfKnowledge());
		// fail("Not yet implemented");
	}

	@Test
	public void testClosestUnexplored() {
		testReadyMap();

		ant.origin = 1;
		ant.locX = 0;
		ant.locY = 0;
		// printMap();
		ant.map.get(1, 0).setType(WorldMap.type.WALL);
		ant.map.get(1, 1).setType(WorldMap.type.WALL);
		ant.map.get(0, 2).setType(WorldMap.type.GRASS);
		ant.map.get(0, 0).setType(WorldMap.type.GRASS);
		ant.map.get(2, 2).setType(WorldMap.type.GRASS);

		Cell cell = MapOps.findClosest(ant, WorldMap.type.UNEXPLORED);
		System.out.println("target: " + cell);
		Direction dir = MapOps.search(ant, cell);
		System.out.println(dir);
		assertEquals(dir, Direction.NORTH);
	}

	@Test
	public void testClosestFood() {
		testReadyMap();

		ant.origin = 1;
		ant.locX = 1;
		ant.locY = 2;
		ant.getCell(1, 0).setAmntFood(100);
		ant.getCell(1, 1).setAmntFood(100);

		// printMap();
		Cell cell = MapOps.findClosest(ant, WorldMap.type.FOOD);
		System.out.println(cell);
		Direction dir = MapOps.search(ant, cell);
		System.out.println(dir);
		assertEquals(Direction.WEST, dir);
	}

	@Test
	public void testMapInsert() {
		Cell t1 = ant.getCell(1, 1);
		System.out.println(ant.map.sizeOfKnowledge());
		Cell t2 = ant.getCell(1, 1);
		System.out.println(ant.map.sizeOfKnowledge());

		System.out.println(t1 == t2);

	}

	@Test
	public void testIntialize() {
		fail("Not yet implemented");
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
