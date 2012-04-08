package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import vohra.Cell;
import vohra.WorldMap;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMapTest extends WorldMap {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatingMap() {
		WorldMap worldMap = new WorldMap();
		worldMap.updateMap(new testSurroundings(), 0, 0);
		assertEquals(5, worldMap.numKnownCells());
	}

	@Test
	public void testMapMerge() {
		WorldMap map1 = new WorldMap();
		WorldMap map2 = new WorldMap();

		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		boolean isUpdated = map1.merge(map2.getMap());
		assertTrue(isUpdated);
		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		map2.set(new Cell(Cell.TYPE.GRASS, 0, 1));
		isUpdated = map1.merge(map2.getMap());
		assertTrue(isUpdated);
		assertEquals(map1.numKnownCells(), 2);
		assertEquals(map2.numKnownCells(), 2);

		isUpdated = map1.merge(map2.getMap());
		assertTrue(isUpdated);

		map2.set(new Cell(Cell.TYPE.FOOD, 2, 2));
		isUpdated = map1.merge(map2.getMap());
		assertEquals(map1.numKnownCells(), 3);
		assertEquals(map2.numKnownCells(), 3);
		assertTrue(isUpdated);

		isUpdated = map1.merge(map2.getMap());
		assertFalse(isUpdated);

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
}
