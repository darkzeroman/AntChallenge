package vohra;

import java.io.Serializable;

import ants.Direction;

public class Cell implements Comparable<Cell>, Serializable {
	public enum CELLTYPE {
		FOOD, GRASS, HOME, UNEXPLORED, WATER
	}

	private static final long serialVersionUID = 1L;

	// used by Djikstra, public because meant to be overwritten
	public int dist = 0;

	private int initialNumFood = 0;
	private int numFood = 0;
	private int numAnts = 0;
	private long timeStamp;
	private CELLTYPE cellType;
	private final int x, y;

	public Cell(CELLTYPE cellType, int x, int y) {
		this.x = x;
		this.y = y;
		this.cellType = cellType;
		timeStamp = System.currentTimeMillis();
	}

	// From Comparable interface, used for Djikstra
	public int compareTo(Cell cell) {
		return (this.dist - cell.dist);
	}

	public void decrementFood() {
		this.numFood--;
		this.timeStamp = System.currentTimeMillis();
	}

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

	// Copy the otherCell's data to this one
	public void copyCell(Cell otherCell) {
		this.cellType = otherCell.getCellType();
		this.timeStamp = otherCell.timeStamp;
		this.numFood = otherCell.numFood;
		this.initialNumFood = Math.max(initialNumFood,
				otherCell.getInitialNumFood());
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

	public void setNumFood(int numFood) {
		initialNumFood = Math.max(numFood, initialNumFood);
		this.numFood = numFood;
		timeStamp = System.currentTimeMillis();
	}

	public void setNumAnts(int numAnts) {
		this.numAnts = numAnts;

	}

	public void setCellType(CELLTYPE type) {
		this.cellType = type;

	}

	public String toString() {
		String temp = "[" + this.x + "," + this.y + "], type: " + this.cellType;
		temp += ", Amount of Food: " + this.numFood + ", NumAnts: "
				+ this.numAnts;
		return temp.substring(0, 6).concat(this.dist + "");
		// cost: " + this.dist + " ";
	}
}
