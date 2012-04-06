package vohra;

import java.util.Arrays;
import java.util.PriorityQueue;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new test();

	}

	public test() {
		PriorityQueue<PQNode> pq = new PriorityQueue<PQNode>();
		pq.add(new PQNode(null, new int[] { 10, 1, 1 }));
		pq.add(new PQNode(null, new int[] { 123, 2, 0 }));
		PQNode node = new PQNode(null, new int[] { 50, 1, 0 });
		pq.add(node);
		System.out.println(pq.peek().fgh[0]);

		System.out.println(pq.remove(node));
		node.updateg(5);
		node.update();
		pq.add(node);

		System.out.println(pq.poll().fgh[0]);
		System.out.println(pq.poll().fgh[0]);
		System.out.println(pq.poll().fgh[0]);

		long start = System.nanoTime();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Time: "
				+ ((System.nanoTime() - start) < 1 * Math.pow(10, 9)));

	}

	private class PQNode implements Comparable<PQNode> {
		int[] fgh;;
		Cell cell;

		PQNode(Cell cell, int[] fgh) {
			this.cell = cell;
			this.fgh = fgh;
		}

		PQNode(Cell cell) {
			this(cell, new int[3]);
		}

		@Override
		public int compareTo(PQNode o) {
			return fgh[0] - o.fgh[0];
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Cell)
				return (this.cell == ((Cell) o));
			if (o instanceof PQNode)
				return (this == ((PQNode) o));
			return false;
		}

		void updategh(int g, int h) {
			fgh[1] = g;
			fgh[2] = h;
		}

		void updateg(int g) {
			fgh[1] = g;
			// fgh[0] = fgh[1] + fgh[2];
		}

		void update() {
			fgh[0] = fgh[1] + fgh[2];

		}

		void updateh(int h) {
			fgh[2] = h;
			// fgh[0] = fgh[1] + fgh[2];
		}

		public String toString() {
			return Arrays.toString(fgh);
		}

	}
}
