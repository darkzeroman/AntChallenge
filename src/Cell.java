
import java.awt.Point;
import java.io.Serializable;

import ants.Direction;

public class Cell implements Comparable<Cell>, Serializable {
	private static final long serialVersionUID = 1L;

	public enum TYPE {
		FOOD, GRASS, HOME, UNEXPLORED, WATER
	}

	// used by searches, public because meant to be overwritten
	public int dist = 0;
	public int f = 0;
	public int g = 0;
	public int h = 0;

	public long timeStamp;
	private TYPE type;
	private final Point coord;
	private int amountOfFood = 0;
	int origFood = 0;
	private int numOfAnts = 0;
	public long numOfAntsTimeStamp = 0;

	public Cell(TYPE tileType, int x, int y) {
		coord = new Point(x, y);
		this.setType(tileType);
		timeStamp = System.nanoTime();
	}

	public void presearch() {
		// used by BFS/Djikstra
		this.dist = Integer.MAX_VALUE;
		this.f = this.g = this.h = 0;

	}

	public Direction dirTo(Cell to) {
		int fromX = this.coord.x;
		int fromY = this.coord.y;
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
		timeStamp = System.nanoTime();
	}

	public void setXY(int x, int y) {
		this.coord.x = x;
		this.coord.y = y;
	}

	public Point getXY() {
		return new Point(coord.x, coord.y);
	}

	public int getX() {
		return coord.x;
	}

	public int getY() {
		return coord.y;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;

	}

	public String toString() {
		String temp = "[" + coord.x + "," + coord.y + "], type: " + this.type;
		temp += ", Amount of Food: " + this.amountOfFood + ", NumAnts: "
				+ this.numOfAnts;
		return temp.substring(0, 6);
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
		this.numOfAntsTimeStamp = System.nanoTime();

	}
}
