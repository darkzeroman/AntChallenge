package test;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

import ants.Direction;
import ants.Surroundings;
import ants.Tile;

import vohra.MapTile;
import vohra.myant;
import vohra.myant.type;

public class MyAntTests {
	myant ant = new myant();
	PriorityQueue<MapTile> pq;

	@Before
	public void setUp() throws Exception {
		ant.map = new MapTile[3][3];

		for (int i = ant.map.length - 1; i >= 0; i--)
			for (int j = ant.map[i].length - 1; j >= 0; j--) {
				ant.map[j][i] = new MapTile(myant.type.UNEXPLORED);
				ant.map[j][i].setLocation(i, j);
			}
		// ant.map[0][0] = new MapTile(MyAnt.type.GRASS);
		int x = 0;
	}

	@Test
	public void testUpdatingMap() {
		testSurroundings tS = new testSurroundings();
		ant.origin = 1;

		ant.locX = 1;
		ant.locY = 1;
		ant.updatingMap(tS);
		// printMap();
		assertEquals(1, 1);
	}

	@Test
	public void testReadyMap() {
		testUpdatingMap();
		pq = ant.readyMap();
		// System.out.println("pq size: " + pq.size());
		assertEquals(ant.map[1][1].distanceFromSource, 0);
		assertEquals(ant.map[0][0].distanceFromSource, Integer.MAX_VALUE);

		assertEquals(pq.size(), 5);
		// fail("Not yet implemented");
	}

	@Test
	public void testSearch() {
		testReadyMap();

		ant.origin = 1;
		ant.locX = 1;
		ant.locY = 0;
		// printMap();
		Direction dir = ant.search(ant.map[2][1]);
		System.out.println(ant.map[0][1].toString() + "prev: "
				+ ant.map[0][1].prev + " dir: " + ant.map[0][1].prevDirection()
				+ " " + ant.dirForMapTile(ant.map[1][1], ant.map[0][1]));
		System.out.println(ant.map[2][1].toString() + "prev: "
				+ ant.map[2][1].prev + " dir: " + ant.map[2][1].prevDirection()
				+ " " + ant.dirForMapTile(ant.map[1][1], ant.map[2][1]));

		System.out.println(ant.map[1][2].toString() + "prev: "
				+ ant.map[1][2].prev + " dir: " + ant.map[1][2].prevDirection()
				+ " " + ant.dirForMapTile(ant.map[1][1], ant.map[1][2]));
		;
		System.out.println(ant.map[1][0].toString() + "prev: "
				+ ant.map[1][0].prev + " dir: " + ant.map[1][0].prevDirection()
				+ " " + ant.dirForMapTile(ant.map[1][1], ant.map[1][0]));
		;

		System.out.println(dir);
		fail("Not yet implemented");
	}

	@Test
	public void testDirForMapTile() {

		fail("Not yet implemented");
	}

	@Test
	public void testFindAndUpdatePQ() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindNeighbors() {
		fail("Not yet implemented");
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

	private void printMap() {
		for (int i = 0; i < ant.map.length; i++) {
			for (int j = 0; j < ant.map[i].length; j++) {
				// HOME, WALL, UNEXPLORED, FOOD, GRASS

				if (i == ant.locX && j == ant.locY)
					System.out.print("X");
				else if (ant.map[i][j].type == type.WALL)
					System.out.print("w");
				else if (ant.map[i][j].type == type.GRASS)
					System.out.print("x");
				else if (ant.map[i][j].type == type.HOME)
					System.out.print("H");
				else if (ant.map[i][j].type == type.FOOD)
					System.out.print("F");
				else
					System.out.print(" ");

			}
			System.out.println();
		}
	}

}
