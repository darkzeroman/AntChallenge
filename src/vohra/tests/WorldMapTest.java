package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import vohra.Cell;
import vohra.Cell.CELLTYPE;
import vohra.WorldMap;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMapTest extends WorldMap {

	@Test
	public void testUpdatingMap() {
		// Starting with fresh world map, check if updating with a surroundings
		// increases number of known cells
		WorldMap worldMap = new WorldMap();
		worldMap.surroundingsUpdate(new DummySurroundings(), 0, 0);
		assertEquals(5, worldMap.numKnownCells());
	}

	@Test
	public void testMapMerge() {
		// Testing various parts of a merge between two world maps
		WorldMap map1 = new WorldMap();
		WorldMap map2 = new WorldMap();

		// Both maps should only have one known cell: HOME
		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		// On creation, flag should be set
		assertTrue(map1.checkAndToggleFoodUpdated());

		// Merging now should not yield any new information
		map1.mergeMaps(map2.getMap());
		assertFalse(map1.checkAndToggleFoodUpdated());
		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		// Setting a new type of cell in map 2, but since this isn't a food
		// source map 1's foodupdate should not be set
		map2.setCell(new Cell(CELLTYPE.GRASS, 0, 1));
		map1.mergeMaps(map2.getMap());
		assertFalse(map1.checkAndToggleFoodUpdated());
		assertEquals(map1.numKnownCells(), 2);
		assertEquals(map2.numKnownCells(), 2);

		// Nothing new added, so merging again should lead to no triggering of
		// update flag
		map1.mergeMaps(map2.getMap());
		assertFalse(map1.checkAndToggleFoodUpdated());

		// Making sure food is updated correctly
		map2.setCell(new Cell(CELLTYPE.FOOD, 2, 2));
		map1.mergeMaps(map2.getMap());
		assertEquals(map1.numKnownCells(), 3); // known cells should be 3
		assertEquals(map2.numKnownCells(), 3);
		assertTrue(map1.checkAndToggleFoodUpdated());

		// Final test to merge, nothing new should be merged
		map1.mergeMaps(map2.getMap());
		assertFalse(map1.checkAndToggleFoodUpdated());

	}

	// Below are used to testing purposes
	private class DummySurroundings implements Surroundings {
		@Override
		public Tile getCurrentTile() {
			// TODO Auto-generated method stub
			return new DummyTile();
		}

		@Override
		public Tile getTile(Direction direction) {
			// TODO Auto-generated method stub
			return new DummyTile();
		}
	}

	private class DummyTile implements Tile {
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
