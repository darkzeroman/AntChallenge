package vohra;

import java.awt.Point;
import java.util.Enumeration;
import java.util.Hashtable;

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
		getCell(0, 0).setCellType(Cell.CELLTYPE.HOME);
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
		// Below CELL means local copy and TILE is from engine
		int tileAmountFood = tile.getAmountOfFood();
		cell.setNumAnts(tile.getNumAnts());
		
		// TILE is not traversable, merge if CELL isn't already water
		if (!tile.isTravelable() && cell.getCellType() != Cell.CELLTYPE.WATER) {
			cell.setCellType(Cell.CELLTYPE.WATER);
			return true;
		}
		// CELL previously had food, now doesn't. Change food and set to Grass
		else if (cell.getCellType() == Cell.CELLTYPE.FOOD
				&& tileAmountFood == 0) {
			cell.setNumFood(0);
			cell.setCellType(Cell.CELLTYPE.GRASS);
			return true;

			// CELL has food, but amount has changed
		} else if (tileAmountFood > 0 && tileAmountFood != cell.getNumFood()) {
			cell.setNumFood(tileAmountFood);
			if (!(cell.getCellType() == Cell.CELLTYPE.HOME))
				cell.setCellType(Cell.CELLTYPE.FOOD);
			return true;

			// TILE is traversable and CELL isn't home or grass, so new info
		} else if (tile.isTravelable() && tileAmountFood == 0
				&& cell.getCellType() != Cell.CELLTYPE.HOME
				&& cell.getCellType() != Cell.CELLTYPE.GRASS) {
			cell.setCellType(Cell.CELLTYPE.GRASS);
			return true;
		}
		return false;
	}

	public boolean mergeMaps(Hashtable<Point, Cell> mapToMerge) {
		Enumeration<Cell> e = mapToMerge.elements();
		boolean updated = false;
		while (e.hasMoreElements()) {
			Cell otherCell = e.nextElement();
			// Only explored "other cells" are of interest
			if (otherCell.getCellType() != Cell.CELLTYPE.UNEXPLORED) {
				Cell localCell = getCell(otherCell.getX(), otherCell.getY());

				// If local cell is unexplored and other isn't, merge
				if (localCell.getCellType() == Cell.CELLTYPE.UNEXPLORED) {
					setCell(otherCell);
					updated = true;

					// If local info is older, merge
				} else if (localCell.getTimeStamp() < otherCell.getTimeStamp()) {
					setCell(otherCell);
					updated = true;
				}
			}
		}
		return updated;
	}

	public int numKnownCells() {
		// returns the number of known cells
		return map.keySet().size();
	}

	public Enumeration<Cell> elements() {
		return map.elements();
	}

	public Cell getCell(int row, int col) {
		// If requested cell doesn't exist, create it and return
		Point coord = new Point(row, col);
		if (map.get(coord) == null)
			map.put(coord, new Cell(Cell.CELLTYPE.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public void setCell(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);
	}

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

}
