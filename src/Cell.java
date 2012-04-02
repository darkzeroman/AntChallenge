import java.io.Serializable;

public class Cell implements Comparable<Cell>, Serializable {

	private static final long serialVersionUID = 1L;
	public int dist = 0;
	public boolean mark = false;
	public Cell prev;
	public long timeStamp;

	private WorldMap.type type;
	private int x, y;
	private int amntFood = 0;
	int origFood = 0;

	public Cell(WorldMap.type tileType) {
		this.setType(tileType);
		timeStamp = System.currentTimeMillis();
	}

	public Cell(WorldMap.type tileType, int x, int y) {
		this(tileType);
		this.setXY(x, y);
	}

	public int compareTo(Cell cell) {
		return (this.dist - cell.dist);
	}

	public void resetForSearch() {
		this.dist = Integer.MAX_VALUE;
		prev = null;
		mark = false;

	}

	public void decrementAmntFood() {
		this.amntFood--;
		this.timeStamp = System.currentTimeMillis();
	}

	public int getAmntFood() {
		return amntFood;
	}

	public void setAmntFood(int amountFood) {
		origFood = Math.max(amountFood, origFood);
		this.amntFood = amountFood;
		timeStamp = System.currentTimeMillis();
	}

	public void mark() {
		this.mark = true;
	}

	public int[] getXY() {
		return new int[] { x, y };
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return "[" + x + "," + y + "] cost: " + this.dist + " ";
	}

	public WorldMap.type getType() {
		return type;
	}

	public void setType(WorldMap.type tileType) {
		this.type = tileType;

	}
}
