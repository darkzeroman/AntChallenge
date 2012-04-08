package vohra;

import java.awt.Point;
import java.util.Enumeration;
import java.util.Hashtable;

import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class WorldMap {
	private final Hashtable<Point, Cell> map;
	int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	public Hashtable<Point, Cell> getMap() {
		return map;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public WorldMap() {

		this.map = new Hashtable<Point, Cell>();
	}

	public Cell getCell(int row, int col) {
		// If requested cell doesn't exist, create it and return
		Point coord = new Point(row, col);
		if (map.get(coord) == null)
			map.put(coord, new Cell(Cell.TYPE.UNEXPLORED, row, col));
		return map.get(coord);
	}

	public int numKnownCells() {
		return map.keySet().size();
	}

	public boolean updateMap(Surroundings surroundings, int x, int y) {
		// If any cells are updated, set updated to true
		boolean surroundingsUpdate = false;
		surroundingsUpdate |= updateCell(getCell(x, y),
				surroundings.getCurrentTile());

		for (int i = 0; i < 4; i++) {
			Tile tile = surroundings.getTile(Direction.values()[i]);
			Cell cell = getCell((x + offsets[i][0]), (y + offsets[i][1]));
			surroundingsUpdate |= updateCell(cell, tile);
		}
		return surroundingsUpdate;

	}

	public boolean updateCell(Cell cell, Tile tile) {
		int tileAmountFood = tile.getAmountOfFood();
		// TODO remove
		cell.setNumAnts(tile.getNumAnts());
		if (cell.getType() == Cell.TYPE.FOOD && tileAmountFood == 0) {
			// previously had food, now doesn't. so set to grass
			cell.setAmountOfFood(0);
			cell.setType(Cell.TYPE.GRASS);
			return true;

		} else if (tileAmountFood > 0
				&& tileAmountFood != cell.getAmountOfFood()) {
			// still has food, but amount has changed
			cell.setAmountOfFood(tileAmountFood);
			if (!(cell.getType() == Cell.TYPE.HOME))
				cell.setType(Cell.TYPE.FOOD);
			return true;

		} else if (tile.isTravelable() && tileAmountFood == 0
				&& cell.getType() != Cell.TYPE.HOME
				&& cell.getType() != Cell.TYPE.GRASS) {
			// new info, set to grass
			cell.setType(Cell.TYPE.GRASS);
			return true;

		} else if (!tile.isTravelable() && tileAmountFood == 0) {
			cell.setType(Cell.TYPE.WATER);
			return true;
		}

		return false;
	}

	public boolean merge(Hashtable<Point, Cell> toMerge) {
		Enumeration<Cell> e = toMerge.elements();
		boolean updated = false;
		while (e.hasMoreElements()) {
			Cell otherCell = e.nextElement();
			// only explored "other cells" are of interest
			if (otherCell.getType() != Cell.TYPE.UNEXPLORED) {
				Cell localCell = getCell(otherCell.getX(), otherCell.getY());
				if (localCell.getType() == Cell.TYPE.UNEXPLORED) {
					// if local cell is unexplored and other isn't, copy
					// type/food
					set(otherCell);
					// localCell.copy(otherCell);
					updated = true;
				} else if (localCell.timeStamp < otherCell.timeStamp) {
					// if local info is older, copy the newer info
					set(otherCell);
					// localCell.copy(otherCell);

					updated = true;
				}
			}
		}
		return updated;
	}

	public Enumeration<Cell> elements() {
		return map.elements();
	}

	public void set(Cell cell) {
		map.put(new Point(cell.getX(), cell.getY()), cell);
	}

}
