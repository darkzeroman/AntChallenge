package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import vohra.Cell;
import vohra.Cell.CELLTYPE;
import vohra.MyAnt;
import vohra.WorldMap;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMapTest extends WorldMap {

	@Test
	public void testUpdatingMap() {
		WorldMap worldMap = new WorldMap();
		boolean surroundingsUpdated = worldMap.updateMap(
				new testSurroundings(), 0, 0);
		assertEquals(5, worldMap.numKnownCells());
		assertTrue(surroundingsUpdated);
	}

	@Test
	public void testMapMerge() {
		WorldMap map1 = new WorldMap();
		WorldMap map2 = new WorldMap();

		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		boolean isUpdated = map1.mergeMaps(map2.getMap());
		assertFalse(isUpdated);
		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		map2.setCell(new Cell(CELLTYPE.GRASS, 0, 1));
		isUpdated = map1.mergeMaps(map2.getMap());
		assertTrue(isUpdated);
		assertEquals(map1.numKnownCells(), 2);
		assertEquals(map2.numKnownCells(), 2);

		isUpdated = map1.mergeMaps(map2.getMap());
		assertFalse(isUpdated);

		map2.setCell(new Cell(CELLTYPE.FOOD, 2, 2));
		isUpdated = map1.mergeMaps(map2.getMap());
		assertEquals(map1.numKnownCells(), 3);
		assertEquals(map2.numKnownCells(), 3);
		assertTrue(isUpdated);

		isUpdated = map1.mergeMaps(map2.getMap());
		assertFalse(isUpdated);

	}

	private MyAnt makeAntInSquareWorldWithGrass(int dimension) {
		MyAnt ant = new MyAnt();
		WorldMap worldMap = ant.getWorldMap();
		for (int i = -1 * dimension / 2; i < dimension / 2; i++)
			for (int j = -1 * dimension / 2; j < dimension / 2; j++)
				worldMap.getCell(i, j).setCellType(CELLTYPE.GRASS);

		return ant;
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
