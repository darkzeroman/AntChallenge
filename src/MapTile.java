/**
 * 
 */

/**
 * @author dkz
 * 
 */
public class MapTile {

	MyAnt.type type;
	int amountFood = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public MapTile(MyAnt.type tileType) {
		this.type = tileType;
	}

	public void setType(MyAnt.type tileType) {
		this.type = tileType;
	}

	public int getAmountFood() {
		return amountFood;
	}

	public void setAmountFood(int amountFood) {
		this.amountFood = amountFood;
	}

}
