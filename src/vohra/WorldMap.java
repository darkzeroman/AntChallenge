package vohra;

import java.awt.Point;
import java.util.Enumeration;
import java.util.Hashtable;

import vohra.Cell.CELLTYPE;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMap {
	// Holds the knowledge of the world
	private final Hashtable<Point, Cell> map;
	final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	public WorldMap() {
		this.map = new Hashtable<Point, Cell>();
		// Setting the home tile upon instantiation
		getCell(0, 0).setCellType(CELLTYPE.HOME);
	}

	public boolean updateMap(Surroundings surroundings, int x, int y) {
		// Return boolean to indicate surroundings were updated
		boolean surroundingsUpdated = false;
		
		// Checking the current tile
		surroundingsUpdated |= updateCell(getCell(x, y),
				surroundings.getCurrentTile());
		
		// Checking the neighbors
		for (int i = 0; i < 4; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = getCell((x + offsets[i][0]), (y + offsets[i][1]));
			surroundingsUpdated |= updateCell(cell, tile);
		}
		return surroundingsUpdated;

	}

	public boolean updateCell(Cell cell, Tile tile) {
		// CELL = local copy, TILE = given by engine

		// Takes in a CELL and TILE and checks if there is new info from TILE
		// and returns boolean if CELL has been updated

		int tileNumFood = tile.getAmountOfFood();
		cell.setNumAnts(tile.getNumAnts()); // always set number of ants

		// If the CELL is unexplored, anything given is new info
		if (cell.getCellType() == CELLTYPE.UNEXPLORED) {
			if (tileNumFood > 0) { // Checking for food TILE
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.FOOD);

			} else if (tileNumFood == 0 && tile.isTravelable())
				cell.setCellType(CELLTYPE.GRASS);

			else if (!tile.isTravelable()) //
				cell.setCellType(CELLTYPE.WATER);
			else
				return false; // when nothing is updated
			return true;

		} else if (cell.getCellType() == CELLTYPE.FOOD) {
			if (tileNumFood == 0) { // CELL had food, not anymore
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.GRASS);
				return true;

			} else if (tileNumFood != cell.getNumFood()) {
				// Need to update num food on CELL
				cell.setNumFood(tileNumFood);
				return true;

			}
		} else if (cell.getCellType() == CELLTYPE.HOME) {
			// Updating HOME's numFood
			cell.setNumFood(tileNumFood);
			return true;
		}
		return false; // means no updates for current cell
	}

	public boolean mergeMaps(Hashtable<Point, Cell> otherMap) {
		// Merges local and other ant's Map
		Enumeration<Cell> e = otherMap.elements();
		boolean mergeMapUpdate = false; // flag for when new info is added
		while (e.hasMoreElements()) {
			Cell otherCell = e.nextElement();
			// Only explored "other cells" are of interest
			if (otherCell.getCellType() != CELLTYPE.UNEXPLORED) {
				Cell localCell = getCell(otherCell.getX(), otherCell.getY());

				// If local cell is unexplored and other isn't, merge
				if (localCell.getCellType() == CELLTYPE.UNEXPLORED) {
					localCell.copyCell(otherCell);
					mergeMapUpdate = true;

					// If local info is older, merge
				} else if (localCell.getTimeStamp() < otherCell.getTimeStamp()) {
					localCell.copyCell(otherCell);
					mergeMapUpdate = true;
				}
			}
		}
		return mergeMapUpdate;
	}

	public int numKnownCells() {
		return map.keySet().size();
	}

	public Enumeration<Cell> elements() {
		// Used for merging
		return map.elements();
	}

	public int getNumFoodFound() {
		int sum = 0;
		Enumeration<Cell> e = getMap().elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().getInitialNumFood();
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

	public void setCell(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);
	}

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

}
