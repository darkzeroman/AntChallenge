package vohra;

import java.util.Hashtable;
import java.util.Stack;

public abstract class Planner {
	protected final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	public abstract Stack<Cell> makePlan(WorldMap worldMap, Cell startCell,
			Cell.CELLTYPE goalType);

	public Stack<Cell> constructPlan(WorldMap worldMap, Cell target,
			Hashtable<Cell, Cell> prev) {
		Stack<Cell> newPlan = new Stack<Cell>();
		Cell u = target;
		while (prev.containsKey(u)) {
			newPlan.push(u);
			u = prev.get(u);
		}
		return newPlan;
	}

}
