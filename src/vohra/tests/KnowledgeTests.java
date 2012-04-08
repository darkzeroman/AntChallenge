package vohra.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

import vohra.Cell;
import vohra.Knowledge;
import vohra.searches.CellComparator;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class KnowledgeTests {

	@Test
	public void testReadyMap() {
		PriorityQueue<Cell> pq;

		Knowledge knowledge = new Knowledge(1);
		knowledge.getCell(0, 1);
		pq = knowledge.preSearch(true);
		assertEquals(0, knowledge.getCell(0, 0).dist);
		assertEquals(Integer.MAX_VALUE, knowledge.getCell(0, 1).dist);

		assertEquals(2, pq.size());
	}

	@Test
	public void testUpdatingMap() {
		testSurroundings tS = new testSurroundings();
		Knowledge knowledge = new Knowledge(1);
		knowledge.updateMap(tS);
		assertEquals(5, knowledge.numKnownCells());
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

		map1.merge(map2.getMap());
		assertTrue(map1.isUpdated());
		assertEquals(map1.numKnownCells(), 1);
		assertEquals(map2.numKnownCells(), 1);

		map2.set(new Cell(Cell.TYPE.GRASS, 0, 1));
		map1.merge(map2.getMap());
		assertTrue(map1.isUpdated());
		assertEquals(map1.numKnownCells(), 2);
		assertEquals(map2.numKnownCells(), 2);

		map1.setUpdated(false);
		map1.merge(map2.getMap());
		assertFalse(map1.isUpdated());

		map2.set(new Cell(Cell.TYPE.FOOD, 2, 2));
		map1.merge(map2.getMap());
		assertEquals(map1.numKnownCells(), 3);
		assertEquals(map2.numKnownCells(), 3);
		assertTrue(map1.isUpdated());

		map1.setUpdated(false);
		map1.merge(map2.getMap());
		assertFalse(map1.isUpdated());

	}

	@Test
	public void testDistanceComparator() {
		ArrayList<Cell> test = new ArrayList<Cell>();
		test.add(new Cell(Cell.TYPE.GRASS, 1, 1));
		test.add(new Cell(Cell.TYPE.FOOD, 4, 4));

		Collections.sort(test, new CellComparator());
		assertEquals(Cell.TYPE.GRASS, test.get(0).getType());
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
