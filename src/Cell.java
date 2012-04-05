import java.io.Serializable;

import ants.Direction;

public class Cell implements Comparable<Cell>, Serializable {
	private static final long serialVersionUID = 1L;
	public enum CellType {
		FOOD, GRASS, HOME, UNEXPLORED, WALL
	}
	// used by searches, public because meant to be overwritten
	public int dist = 0;

	public long timeStamp;
	private CellType type;
	private int x, y;
	private int amountOfFood = 0;
	int origFood = 0;
	private int numOfAnts = 0;

	public Cell(CellType tileType, int x, int y) {
		this.setType(tileType);
		timeStamp = System.currentTimeMillis();
		this.setXY(x, y);
	}

	public void presearch() {
		// used by BFS/Djikstra
		this.dist = Integer.MAX_VALUE;

	}

	public Direction dirTo(Cell to) {
		int fromX = this.x;
		int fromY = this.y;
		int toX = to.getX(), toY = to.getY();

		if ((fromX == toX) && (fromY > toY))
			return Direction.SOUTH;
		else if ((fromX == toX) && (fromY < toY))
			return Direction.NORTH;
		else if ((fromY == toY) && (fromX > toX))
			return Direction.WEST;
		else
			return Direction.EAST;

	}

	public void decrementFood() {
		this.amountOfFood--;
		this.timeStamp = System.nanoTime();
	}

	public int getAmountOfFood() {
		return amountOfFood;
	}

	public void setAmountOfFood(int amountOfFood) {
		origFood = Math.max(amountOfFood, origFood);
		this.amountOfFood = amountOfFood;
		timeStamp = System.currentTimeMillis();
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public CellType getType() {
		return type;
	}

	public void setType(CellType tileType) {
		this.type = tileType;

	}

	public String toString() {
		String temp = "[" + x + "," + y + "], type: " + this.type;
		temp += ", Amount of Food: " + this.amountOfFood + ", NumAnts: "
				+ this.numOfAnts;
		return temp;
		// cost: " + this.dist + " ";
	}

	public int compareTo(Cell cell) {
		return (this.dist - cell.dist);
	}

	public int getNumAnts() {
		return numOfAnts;
	}

	public void setNumAnts(int numAnts) {
		this.numOfAnts = numAnts;
	}
}
