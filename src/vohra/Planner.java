package vohra;

import java.util.Stack;

import vohra.Cell.CELLTYPE;

public interface Planner {
	public enum SEARCHTYPE {
		BFS, // Djikstra, AStar
		// Used to be implemented, but chose BFS to final implementation
	}

	/**
	 * Since every ant needs access to a planner and I had implemented/tested 3
	 * different search algorithms, I decided to make a singleton for the search
	 * class. While I know singletons aren't usually preferred, I made this
	 * decision because the project is small and I didn't want multiple search
	 * classes instantiated for each search type. And using an interface allowed
	 * me to easily switch between any search type during design/testing phase.
	 */

	public Stack<Cell> makePlan(WorldMap worldMap, Cell startCell,
			CELLTYPE goalCellType);

}
