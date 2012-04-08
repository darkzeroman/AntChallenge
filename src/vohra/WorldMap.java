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
		int tileNumFood = tile.getAmountOfFood();
		cell.setNumAnts(tile.getNumAnts());
		if (cell.getCellType() == CELLTYPE.UNEXPLORED) {
			if (tileNumFood > 0) {
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.FOOD);
			} else if (tileNumFood == 0 && tile.isTravelable())
				cell.setCellType(CELLTYPE.GRASS);
			else if (!tile.isTravelable())
				cell.setCellType(CELLTYPE.WATER);
			return true;

		} else if (cell.getCellType() == CELLTYPE.FOOD) {
			if (tileNumFood == 0) {
				cell.setNumFood(tileNumFood);
				cell.setCellType(CELLTYPE.GRASS);
				return true;

			} else if (tileNumFood != cell.getNumFood()) {
				cell.setNumFood(tileNumFood);
				return true;

			}
		} else if (cell.getCellType() == CELLTYPE.HOME) {
			cell.setNumFood(tileNumFood);
			return false;
		}
		return false;

	}

	public boolean mergeMaps(Hashtable<Point, Cell> mapToMerge) {
		Enumeration<Cell> e = mapToMerge.elements();
		boolean updated = false;
		while (e.hasMoreElements()) {
			Cell otherCell = e.nextElement();
			// Only explored "other cells" are of interest
			if (otherCell.getCellType() != CELLTYPE.UNEXPLORED) {
				Cell localCell = getCell(otherCell.getX(), otherCell.getY());

				// If local cell is unexplored and other isn't, merge
				if (localCell.getCellType() == CELLTYPE.UNEXPLORED) {
					setCell(otherCell);
					updated = true;

					// If local info is older, merge
				} else if (localCell.getTimeStamp() < otherCell.getTimeStamp()) {
					localCell.copyCell(otherCell);
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
			map.put(coord, new Cell(CELLTYPE.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public void setCell(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);
	}

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

	public int getNumFoodFound() {
		int sum = 0;
		Enumeration<Cell> e = getMap().elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().getInitialNumFood();
		}
		return sum;
	}

}
