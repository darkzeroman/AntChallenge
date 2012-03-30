package test;

import static org.junit.Assert.*;

import vohra.MapTile;
import vohra.myant;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

public class MapTileTest {

	@Before
	public void setUp() throws Exception {
		MapTile a;
	}

	@Test
	public void testCompareTo() {
		MapTile a = new MapTile(myant.type.WALL);
		a.distanceFromSource = 10;
		MapTile b = new MapTile(myant.type.GRASS);
		b.distanceFromSource = Integer.MAX_VALUE;
		PriorityQueue<MapTile> pq = new PriorityQueue<MapTile>();
		pq.add(a);
		pq.add(b);
		assertEquals(a, pq.poll());
	}

}
