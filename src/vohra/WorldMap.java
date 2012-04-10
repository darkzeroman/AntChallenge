package vohra;

import java.awt.Point;
import java.util.Enumeration;
import java.util.Hashtable;

import vohra.Cell.CELLTYPE;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMap {
	/**
	 * Holds knowledge of the world. Chose hash table because this can
	 * dynamically get larger vs an array/matrix which requires to iteration of
	 * all elements during the merging process
	 */
	private final Hashtable<Point, Cell> map;
	final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	/**
	 * When adding or deleting cells that have food, set this flag because a new
	 * food search is only required if food sources have changed
	 */
	private boolean foodUpdated = true;

	public WorldMap() {
		this.map = new Hashtable<Point, Cell>();
		// Setting the home tile upon instantiation
		getCell(0, 0).setCellType(CELLTYPE.HOME);
	}

	public void surroundingsUpdate(Surroundings surroundings, int x, int y) {

		// Checking the current tile
		updateCell(getCell(x, y), surroundings.getCurrentTile());

		// Checking the neighbors
		for (int i = 0; i < Direction.values().length; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = getCell((x + offsets[i][0]), (y + offsets[i][1]));

			updateCell(cell, tile);
		}
	}

	public void updateCell(Cell cell, Tile tile) {
		// CELL = local copy, TILE = given by engine

		// Takes in a CELL and TILE and checks if there is new info from TILE

		// Doing a full food search when only the immediate surroundings is
		// updated is wasteful, so set "foodUpdated" flag to true if food
		// sources have changed

		int tileNumFood = tile.getAmountOfFood();
		cell.setNumAnts(tile.getNumAnts()); // always set number of ants

		// If the CELL is unexplored, anything given is new info
		if (cell.getCellType() == CELLTYPE.UNEXPLORED) {
			if (tileNumFood > 0) { // Checking for food TILE
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.FOOD);
				foodUpdated = true;

			} else if (tileNumFood == 0 && tile.isTravelable())
				cell.setCellType(CELLTYPE.GRASS); // Only grass can be this

			else if (!tile.isTravelable())
				cell.setCellType(CELLTYPE.WATER);

		} else if (cell.getCellType() == CELLTYPE.FOOD) {
			if (tileNumFood == 0) {
				// CELL had food, not anymore
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.GRASS);
				foodUpdated = true;

			} else if (tileNumFood != cell.getNumFood()) {
				// Need to update numfood on CELL
				cell.setNumFood(tileNumFood);
				foodUpdated = true;
			}
		} else if (cell.getCellType() == CELLTYPE.HOME) {
			// Updating HOME's numFood
			cell.setNumFood(tileNumFood);
		}
	}

	public void mergeMaps(Hashtable<Point, Cell> otherMap) {
		// Merges local and other ant's Map

		Enumeration<Cell> e = otherMap.elements();

		while (e.hasMoreElements()) {
			Cell otherCell = e.nextElement();
			// Only explored "other cells" are of interest
			if (otherCell.getCellType() != CELLTYPE.UNEXPLORED) {
				Cell localCell = getCell(otherCell.getX(), otherCell.getY());

				// If local cell is unexplored and other isn't, copy
				if (localCell.getCellType() == CELLTYPE.UNEXPLORED) {

					// if either the localCell or otherCell are food there was a
					// food update
					if (otherCell.getCellType() == CELLTYPE.FOOD
							|| localCell.getCellType() == CELLTYPE.FOOD)
						this.foodUpdated = true;
					localCell.copyCell(otherCell);

					// If local cell info is older, copy over
				} else if (localCell.getTimeStamp() < otherCell.getTimeStamp()) {

					if (otherCell.getCellType() == CELLTYPE.FOOD
							|| localCell.getCellType() == CELLTYPE.FOOD)
						this.foodUpdated = true;

					localCell.copyCell(otherCell);

				}
			}
		}
	}

	public int numKnownCells() {
		// Number of known cells in the world
		return map.keySet().size();
	}

	public Enumeration<Cell> elements() {
		// Used for merging
		return map.elements();
	}

	public int getNumFoodFound() {
		// Returns the total of food found
		// Useful to check to stop scouting mode
		int sum = 0;
		Enumeration<Cell> e = getMap().elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().getInitialNumFood();
		}
		return sum;
	}

	public boolean checkAndToggleFoodUpdated() {
		if (this.foodUpdated) {
			this.foodUpdated = false;
			return true;
		}
		return this.foodUpdated;
	}

	public int getNumAntsAroundCell(Cell cell) {
		int sum = cell.getNumAnts();
		// Checking the neighbors
		for (int i = 0; i < 4; i++) {
			sum += getCell((cell.getX() + offsets[i][0]),
					(cell.getY() + offsets[i][1])).getNumAnts();
		}
		return sum;
	}

	public Cell getCell(int row, int col) {
		// If requested cell doesn't exist, create it and return
		Point coord = new Point(row, col);
		if (map.get(coord) == null)
			map.put(coord, new Cell(CELLTYPE.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

	// Used for JUnit tests
	public void setCell(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);
	}

}
