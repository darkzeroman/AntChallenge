package vohra;

import java.util.Hashtable;
import java.util.Stack;

public abstract class Planner {
	final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	public abstract boolean makePlan(Knowledge knowledge, Cell.TYPE type);

	public void constructPath(Knowledge knowledge, Cell target,
			Hashtable<Cell, Cell> prev) {
		knowledge.getCurrPlan().clear();
		Cell u = target;
		while (prev.containsKey(u)) {
			knowledge.getCurrPlan().push(u);
			u = prev.get(u);
		}
	}

	public void printPath(Knowledge knowledge) {
		Stack<Cell> currRoute = knowledge.getCurrPlan();
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		Cell old = knowledge.getCell(knowledge.x, knowledge.y);
		for (int i = 0; i < currRoute.size(); i++) {
			MyAnt.debugPrint(1, old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}
}
