package vohra;
import java.util.Hashtable;

public abstract class RoutePlanner {
	static final int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

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

}
