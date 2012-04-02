package vohra;


/**
 * 
 */

/**
 * @author dkz
 * 
 */
public class MapTile implements Comparable<MapTile> {

	public WorldMap.type type;
	int amountFood = 0;
	public int distanceFromSource = 0;
	int x, y;
	public MapTile prev;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public MapTile(WorldMap.type tileType) {
		this.type = tileType;
	}

	public void setType(WorldMap.type tileType) {
		this.type = tileType;

	}

	public int getAmountFood() {
		return amountFood;
	}

	public void setAmountFood(int amountFood) {
		this.amountFood = amountFood;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int compareTo(MapTile mT) {
		// TODO Auto-generated method stub
		if (this.distanceFromSource > mT.distanceFromSource)
			return 1;
		else if (this.distanceFromSource == mT.distanceFromSource)
			return 0;
		else
			return -1;
	}

	public String toString() {
		return "[" + x + "," + y + "] cost: " + this.distanceFromSource + " ";
	}

	public String prevDirection() {
		if (prev == null)
			return "N";
		if ((this.x == prev.x) && (y > prev.y))
			return "V";
		else if ((x == prev.x) && (y < prev.y))
			return "^";
		else if ((y == prev.y) && (x > prev.x))
			return "<";
		else
			return ">";
	}
}
