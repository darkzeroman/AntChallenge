package vohra;


import java.util.Hashtable;
import java.util.Stack;

public abstract class RoutePlanner {
	final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	public abstract boolean makeRoute(Knowledge knowledge, Cell.CellType type);

	public abstract boolean makeRoute(Knowledge knowledge, Cell target);

	public void constructPath(Knowledge knowledge, Cell target,
			Hashtable<Cell, Cell> prev) {
		knowledge.getCurrRoute().clear();
		Cell u = target;
		while (prev.containsKey(u)) {
			knowledge.getCurrRoute().push(u);
			u = prev.get(u);
		}
	}
	
	public void printPath(Knowledge knowledge) {
		Stack<Cell> currRoute = knowledge.getCurrRoute();
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		Cell old = knowledge.get(knowledge.getLocX(), knowledge.getLocY());
		for (int i = 0; i < currRoute.size(); i++) {
			MyAnt.debugPrint(1, old.dirTo(currRoute.get(i)) + " ");
			old = currRoute.get(i);
		}
		MyAnt.debugPrint(1, "");
	}
}
