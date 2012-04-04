import java.io.Serializable;

public class Cell implements Comparable<Cell>, Serializable {
	private static final long serialVersionUID = 1L;

	// used by searches, public because meant to be overwritten
	public int dist = 0;
	public boolean mark = false;
	public Cell prev;

	public long timeStamp;
	private WorldMap.type type;
	private int x, y;
	private int amntFood = 0;
	int origFood = 0;
	private int numAnts = 0;

	public Cell(WorldMap.type tileType, int x, int y) {
		this.setType(tileType);
		timeStamp = System.currentTimeMillis();
		this.setXY(x, y);
	}

	public void presearch() {
		// used by BFS/Djikstra
		this.dist = Integer.MAX_VALUE;
		prev = null;
		mark = false;

	}

	public void mark() {
		this.mark = true;
	}

	public void decrementAmountFood() {
		this.amntFood--;
		this.timeStamp = System.currentTimeMillis();
	}

	public int getAmntFood() {
		return amntFood;
	}

	public void setAmountFood(int amountFood) {
		origFood = Math.max(amountFood, origFood);
		this.amntFood = amountFood;
		timeStamp = System.currentTimeMillis();
	}

	public int[] getXY() {
		return new int[] { x, y };
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public WorldMap.type getType() {
		return type;
	}

	public void setType(WorldMap.type tileType) {
		this.type = tileType;

	}

	public String toString() {
		String temp = "[" + x + "," + y + "] type: " + this.type;
		temp += " Amount of Food: " + this.amntFood + " NumAnts: "
				+ this.numAnts;
		return temp;
		// cost: " + this.dist + " ";
	}

	public int compareTo(Cell cell) {
		return (this.dist - cell.dist);
	}

	public int getNumAnts() {
		return numAnts;
	}

	public void setNumAnts(int numAnts) {
		this.numAnts = numAnts;
	}
}
