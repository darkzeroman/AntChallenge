package vohra;

import java.awt.Point;
import java.io.Serializable;

import ants.Direction;

public class Cell implements Comparable<Cell>, Serializable {
	public enum TYPE {
		FOOD, GRASS, HOME, UNEXPLORED, WATER
	}

	private static final long serialVersionUID = 1L;

	// used by Djikstra, public because meant to be overwritten
	public int dist = 0;

	public long numOfAntsTimeStamp = 0;
	public long timeStamp;
	private int amountOfFood = 0;
	private final Point coord;
	private int numOfAnts = 0;
	private int originalAmountOfFood = 0;
	private TYPE type;

	public Cell(TYPE tileType, int x, int y) {
		coord = new Point(x, y);
		this.setType(tileType);
		timeStamp = System.currentTimeMillis();
	}

	public int compareTo(Cell cell) {
		return (this.dist - cell.dist);
	}

	public void decrementFood() {
		this.amountOfFood--;
		this.timeStamp = System.nanoTime();
	}

	public Direction dirTo(Cell to) {
		int fromX = this.coord.x;
		int fromY = this.coord.y;
		int toX = to.getX();
		int toY = to.getY();

		if ((fromX == toX) && (fromY > toY))
			return Direction.SOUTH;
		else if ((fromX == toX) && (fromY < toY))
			return Direction.NORTH;
		else if ((fromY == toY) && (fromX > toX))
			return Direction.WEST;
		else
			return Direction.EAST;

	}

	public int getAmountOfFood() {
		return amountOfFood;
	}

	public int getNumAnts() {
		return numOfAnts;
	}

	public int getOriginalAmountOfFood() {
		return this.originalAmountOfFood;
	}

	public TYPE getType() {
		return type;
	}

	public int getX() {
		return coord.x;
	}

	public int getY() {
		return coord.y;
	}

	public void setAmountOfFood(int amountOfFood) {
		originalAmountOfFood = Math.max(amountOfFood, originalAmountOfFood);
		this.amountOfFood = amountOfFood;
		timeStamp = System.nanoTime();
	}

	public void setNumAnts(int numAnts) {
		this.numOfAnts = numAnts;
		this.numOfAntsTimeStamp = System.nanoTime();

	}

	public void setType(TYPE type) {
		this.type = type;

	}

	public void setXY(int x, int y) {
		this.coord.x = x;
		this.coord.y = y;
	}

	public String toString() {
		String temp = "[" + coord.x + "," + coord.y + "], type: " + this.type;
		temp += ", Amount of Food: " + this.amountOfFood + ", NumAnts: "
				+ this.numOfAnts;
		return temp.substring(0, 6);
		// cost: " + this.dist + " ";
	}
}
