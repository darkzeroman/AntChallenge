package vohra;

import java.io.Serializable;

import ants.Direction;

/**
 * World Map contains a hash table of Cells, this holds the information provided
 * for each tile in the world.
 */
public class Cell implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum CELLTYPE {
		FOOD, GRASS, HOME, UNEXPLORED, WATER
	}

	/**
	 * Always taking the max of any number given of how much food was in cell.
	 * Used to track how much food was at this cell. Used to track totalNumFood.
	 */
	private int initialNumFood = 0;

	// Properties of the cell
	private int numFood = 0;
	private int numAnts = 0;
	private long timeStamp; // To track which info is newer
	private CELLTYPE cellType;
	private final int x, y; // location

	public Cell(CELLTYPE cellType, int x, int y) {
		this.x = x;
		this.y = y;
		this.cellType = cellType;
		timeStamp = System.currentTimeMillis();
	}

	/** Use when ant has picked up a food unit from a cell. */
	public void decrementFood() {
		this.numFood--;
		this.timeStamp = System.currentTimeMillis();
	}

	/** Gives the direction needed to get from this cell to the argument cell. */
	public Direction directionTo(Cell to) {
		int fromX = this.x;
		int fromY = this.y;
		int toX = to.getX();
		int toY = to.getY();

		if (fromX == toX) {
			if (fromY > toY)
				return Direction.SOUTH;
			else
				return Direction.NORTH;
		} else if (fromY == toY) {
			if (fromX > toX)
				return Direction.WEST;
			else
				return Direction.EAST;
		}
		throw new RuntimeException("Can't reach cell");

	}

	/** Copy the contents of otherCell into this one */
	public void copyCell(Cell otherCell) {
		this.cellType = otherCell.getCellType();
		this.timeStamp = otherCell.timeStamp;
		this.numFood = otherCell.numFood;
		this.initialNumFood = Math.max(initialNumFood, otherCell.getInitialNumFood());
	}

	public void setNumFood(int numFood) {
		initialNumFood = Math.max(numFood, initialNumFood);
		this.numFood = numFood;
		// Need to track when the food info was updated
		timeStamp = System.currentTimeMillis();
	}

	public int getNumFood() {
		return numFood;
	}

	public int getNumAnts() {
		return numAnts;
	}

	public int getInitialNumFood() {
		return this.initialNumFood;
	}

	public CELLTYPE getCellType() {
		return cellType;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setNumAnts(int numAnts) {
		this.numAnts = numAnts;
	}

	public void setCellType(CELLTYPE type) {
		this.cellType = type;
	}

	public String toString() {
		String str = "[" + this.x + "," + this.y + "], type: " + this.cellType;
		str += ", Amount of Food: " + this.numFood + ", NumAnts: " + this.numAnts;
		return str;
	}
}
