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

	/** Relative coordinates for each cardinal direction. (NESW) */
	static final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	/**
	 * When adding/deleting/updating cells that have food, set this flag because
	 * a new food search is only required if food sources have changed. This is
	 * done for efficiency.
	 */
	private boolean foodUpdated = true;

	public WorldMap() {
		this.map = new Hashtable<Point, Cell>();
		// Setting the home tile upon instantiation
		getCell(0, 0).setCellType(CELLTYPE.HOME);
	}

	public void surroundingsUpdate(Surroundings surroundings, int x, int y) {

		updateCell(getCell(x, y), surroundings.getCurrentTile());

		// Update from neighboring tiles
		for (int i = 0; i < Direction.values().length; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = getCell((x + offsets[i][0]), (y + offsets[i][1]));

			updateCell(cell, tile);
		}
	}

	/**
	 * Takes in a CELL and TILE and checks if there is new info from TILE
	 * 
	 * @param cell
	 *            Local copy
	 * @param tile
	 *            Given by engine
	 */
	private void updateCell(Cell cell, Tile tile) {

		int tileNumFood = tile.getAmountOfFood();
		cell.setNumAnts(tile.getNumAnts()); // always set number of ants

		// If the cell (local) is unexplored, anything given is new info
		if (cell.getCellType() == CELLTYPE.UNEXPLORED) {
			if (tileNumFood > 0) { // Checking if food tile
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.FOOD);
				foodUpdated = true;

			} else if (tileNumFood == 0 && tile.isTravelable()) {
				cell.setCellType(CELLTYPE.GRASS); // Only grass can be this
			} else if (!tile.isTravelable()) {
				cell.setCellType(CELLTYPE.WATER);
			}

		} else if (cell.getCellType() == CELLTYPE.FOOD) {
			if (tileNumFood == 0) {
				// Cell had food, not anymore
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.GRASS);
				foodUpdated = true;

			} else if (tileNumFood != cell.getNumFood()) {
				// Need to update numFood on CELL
				cell.setNumFood(tileNumFood);
				foodUpdated = true;
			}
		} else if (cell.getCellType() == CELLTYPE.HOME) {
			cell.setNumFood(tileNumFood);
		}
	}

	/** Merges local map with other ant's Map */
	public void mergeMaps(Hashtable<Point, Cell> otherMap) {

		Enumeration<Cell> e = otherMap.elements();

		while (e.hasMoreElements()) {
			Cell otherCell = e.nextElement();
			// Only explored "otherCells" are of interest
			if (otherCell.getCellType() != CELLTYPE.UNEXPLORED) {
				Cell localCell = getCell(otherCell.getX(), otherCell.getY());

				// If localCell is unexplored and otherCell isn't.
				// Or if localCell info is older, Copy over
				if (localCell.getCellType() == CELLTYPE.UNEXPLORED
						|| localCell.getTimeStamp() < otherCell.getTimeStamp()) {

					// Set foodUpdated flag is either cells have food.
					if (otherCell.getCellType() == CELLTYPE.FOOD || localCell.getCellType() == CELLTYPE.FOOD) {
						this.foodUpdated = true;
					}
					localCell.copyCell(otherCell);
				}

			}
		}
	}

	/**
	 * Returns the total number of food found, useful to check if scout mode
	 * should be exited
	 */
	public int getNumFoodFound() {

		int sum = 0;
		Enumeration<Cell> e = getMap().elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().getInitialNumFood();
		}
		return sum;
	}

	/** Checks if there was a food update and resets it. */
	public boolean isFoodUpdatedAndReset() {
		if (this.foodUpdated) {
			this.foodUpdated = false;
			return true;
		}
		return this.foodUpdated;
	}

	public Cell getCell(int row, int col) {
		// If requested cell doesn't exist, create it and return
		Point coord = new Point(row, col);
		if (map.get(coord) == null)
			map.put(coord, new Cell(CELLTYPE.UNEXPLORED, row, col));
		return map.get(coord);
	}

	// Below used for JUnit tests

	// Only used for testing purposes
	public int numKnownCells() {
		// Number of known cells in the world
		return map.keySet().size();
	}

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

	public void setCell(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);
	}

}
