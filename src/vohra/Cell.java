package vohra;
import java.io.Serializable;

public class Cell implements Comparable<Cell>, Serializable {

	private static final long serialVersionUID = 1L;
	public int dist = 0;
	public boolean mark = false;
	public Cell prev;
	public long timeStamp;
	private WorldMap.type type;
	public int x, y;
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

	@Override
	public int compareTo(Cell mapTile) {
		return (this.dist - mapTile.dist);
	}

	public int getAmntFood() {
		return amntFood;
	}
	public void decrementAmntFood(){
		this.amntFood--;
		this.timeStamp = System.currentTimeMillis();
	}

	public void prepareForSearch() {
		this.dist = Integer.MAX_VALUE;
		prev = null;
		mark = false;

	}

	public void setAmntFood(int amountFood) {
		origFood = Math.max(amountFood, origFood);
		this.amntFood = amountFood;
		timeStamp = System.currentTimeMillis();
	}

	public void mark() {
		this.mark = true;
	}

	public void setType(WorldMap.type tileType) {
		this.type = tileType;

	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int[] getXY(){
		return new int[]{x,y};
	}

	public String toString() {
		return "[" + x + "," + y + "] cost: " + this.dist + " ";
	}


	public WorldMap.type getType() {
		return type;
	}
}
