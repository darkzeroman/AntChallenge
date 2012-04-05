import java.util.Hashtable;

/**
 * 
 */

/**
 * @author 01
 * 
 */
public interface PathPlanning {
	public boolean makeRoute(MyAnt ant, Cell.CellType type);

	public abstract boolean makeRoute(MyAnt ant, Cell target);

	public static void constructPath(MyAnt ant, Cell target,
			Hashtable<Cell, Cell> prev) {
		ant.getCurrRoute().clear();
		Cell u = target;
		while (prev.containsKey(u)) {
			ant.getCurrRoute().push(u);
			u = prev.get(u);
		}
	}
}
