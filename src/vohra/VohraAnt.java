package vohra;

import java.awt.Point;
import java.util.Hashtable;
import java.util.Stack;

import vohra.Cell.CELLTYPE;
import vohra.searches.BFS;
import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;

/**
 * The ant class used for the engine. While I have commented parts of the FSM
 * transitions, it would be beneficial to glance over the FSM part of the README
 * for a quick run-through of the FSM.
 */
public class VohraAnt implements Ant {

	/** Modes the ant can be in, used for the FSM, described in README */
	public enum ANTMODE {
		EXPLORE, SCOUT, TOFOOD, TOHOME
	}

	/** Initial and reset value */
	private final int scoutModeCounterResetValue = 25;

	/** Number of turns to take before switching from SCOUT to TOFOOD */
	private int scoutModeTurnsCounter = scoutModeCounterResetValue;

	/**
	 * Scouts will exit scout mode after finding this amount of food. Game ends
	 * at 500.
	 */
	private final int numFoodToFindForScouts = 600;

	/** If more than this amount of ants are on HOME, for everyone to SCOUT mode */
	private final int numAntsMaxOnHOME = 10;

	// Ant properties
	private boolean carryingFood = false;
	private boolean isScout = false;
	private ANTMODE mode;
	/** Location of ant */
	private int x, y;
	/** Predetermined planned route to a target */
	private Stack<Cell> currentPlan;
	/** Path from last time at home */
	private Stack<Cell> fromHomePlan;

	/** Holds the knowledge each ant has of the world */
	private final WorldMap worldMap;

	/** Given by engine. Saving it because it is easier than passing it around */
	private Surroundings surroundings;

	/** Se/Deserializer object for communication */
	private final ObjectIO<Hashtable<Point, Cell>> ObjectIO = new ObjectIO<Hashtable<Point, Cell>>();

	/** The type of search algorithm used, have implemented BFS */
	private Planner planner = BFS.getSingleInstance();

	public VohraAnt() {
		this.worldMap = new WorldMap();
		this.currentPlan = new Stack<Cell>();
		this.fromHomePlan = new Stack<Cell>();
		this.mode = ANTMODE.EXPLORE;
	}

	@Override
	public Action getAction(Surroundings surroundings) {

		this.surroundings = surroundings;
		worldMap.surroundingsUpdate(surroundings, x, y);

		// If ant spawns before any food is on HOME, force SCOUT mode
		if (getCell(0, 0).getNumFood() == 0 && !isScout) {
			isScout = true;
			mode = ANTMODE.SCOUT;
		}

		// Determine next action using FSM
		Action action = null;
		switch (mode) {
		case SCOUT:
			action = modeScout();
			break;
		case TOFOOD:
			action = modeToFood();
			break;
		case EXPLORE:
			action = modeExplore();
			break;
		case TOHOME:
			action = modeToHome();
			break;
		default:
			throw new RuntimeException("Undefined state");
		}

		if (isActionValid(action)) {
			// Directional actions need to update location
			if (action.getDirection() != null)
				updateLocation(action.getDirection());
			return action;
		}
		throw new RuntimeException("Invalid Action");
	}

	private Action modeScout() {

		// If high amount of food has already been found, exit scout mode
		if (worldMap.getNumFoodFound() >= numFoodToFindForScouts) {
			isScout = false;
		}

		scoutModeTurnsCounter--;

		if (scoutModeTurnsCounter > 0) {
			// If plan exists, continue with it
			Action action = nextCurrentPlanAction();
			if (action != null)
				return action;
			// If not, make one to closest unexplored
			else if (canFindValidPlanTo(CELLTYPE.UNEXPLORED))
				return nextCurrentPlanAction();
		}

		// Resetting turn counter for when the ant returns to scout mode
		scoutModeTurnsCounter = scoutModeCounterResetValue;

		// The ant at this point exits Scout mode and goes to Explore Mode
		// Which means the ant will find the closest food and go home
		return changeMode(ANTMODE.EXPLORE);
	}

	private Action modeToFood() {

		// If food on target doesn't doesn't exist anymore, re-plan
		if (worldMap.isFoodUpdatedAndReset() && !currentPlan.isEmpty() && currentPlan.firstElement().getNumFood() == 0) {
			currentPlan.clear(); // clearing, so re-planning will be necessary
		}

		// Continue a plan if it exists
		Action action = nextCurrentPlanAction();
		if (action != null)
			return action;

		int currentCellNumFood = getCurrentCell().getNumFood();
		// Ant is on food cell (which is not home), so gather
		if (!isAtHome() && currentCellNumFood > 0 && !carryingFood) {
			carryingFood = true;
			getCurrentCell().decrementFood();
			return changeModeWithAction(ANTMODE.TOHOME, Action.GATHER);
		}

		// Make plan to closest food source
		if (canFindValidPlanTo(CELLTYPE.FOOD))
			return nextCurrentPlanAction();

		// If can't find food, go back to EXPLORE mode
		return changeMode(ANTMODE.EXPLORE);
	}

	private Action modeExplore() {

		// If worldMap was recently updated, see if food source exists nearby
		if (worldMap.isFoodUpdatedAndReset()) {
			return changeMode(ANTMODE.TOFOOD);
		}

		// Continue a plan if it exists
		Action action = nextCurrentPlanAction();
		if (action != null)
			return action;

		// Try to find closest unexplored
		if (canFindValidPlanTo(CELLTYPE.UNEXPLORED))
			return nextCurrentPlanAction();

		// If can't find anything new to explore wait at home
		// for another ant to hopefully share info
		if (isAtHome()) {
			if (getCurrentCell().getNumAnts() < numAntsMaxOnHOME) {
				return Action.HALT;
			} else {
				// If more than the numAntsMAX are on HOME, force SCOUT mode
				isScout = true;
				changeModeWithAction(ANTMODE.SCOUT, Action.HALT);
			}

		}
		// Everything is explored, go home
		return changeMode(ANTMODE.TOHOME);
	}

	private Action modeToHome() {

		// If ant went to home because there is nothing left to explore, try to
		// explore again
		if (isAtHome() && !carryingFood)
			return changeMode(ANTMODE.EXPLORE);

		// Drop off food if ant has it
		if (isAtHome() && carryingFood) {
			carryingFood = false;
			fromHomePlan.clear();

			// If scout, go back to scout mode
			if (isScout) {
				return changeModeWithAction(ANTMODE.SCOUT, Action.DROP_OFF);
			}
			// Not scout, so go back to food
			return changeModeWithAction(ANTMODE.TOFOOD, Action.DROP_OFF);
		}

		// Continue a plan if it exists
		Action action = nextCurrentPlanAction();
		if (action != null)
			return action;

		// Attempt to make plan to go home
		if (canFindValidPlanTo(CELLTYPE.HOME)) {

			// If the plan to home is the same size as the path from home,
			// take the reverse of path from home to hopefully
			// maximize talking with ants who followed the same path.
			if (fromHomePlan.size() == currentPlan.size()) {
				currentPlan.clear();
				copyStack(fromHomePlan, currentPlan);
			}
			// Since at home, clear path
			fromHomePlan.clear();
			return nextCurrentPlanAction();
		}

		// Halt when at home, no food or unexplored
		// occurs if mound is on island
		if (isAtHome() && !canFindValidPlanTo(CELLTYPE.UNEXPLORED) && !canFindValidPlanTo(CELLTYPE.FOOD))
			return Action.HALT;

		throw new RuntimeException("Can't find home, map error?");
	}

	/** Used for FSM transitions. Also deletes current plan. */
	private Action changeMode(ANTMODE nextMode) {

		currentPlan.clear(); // Clear current plan when changing modes
		this.mode = nextMode;

		switch (this.mode) {
		case SCOUT:
			return modeScout();
		case TOFOOD:
			return modeToFood();
		case EXPLORE:
			return modeExplore();
		case TOHOME:
			return modeToHome();
		}
		throw new IllegalArgumentException("UnrecognizedMode: " + nextMode);

	}

	/**
	 * Used for an action needs to accompany a FSM transition. Example: Drop
	 * food before going back to TOFOOD mode to get the next food unit.
	 */
	private Action changeModeWithAction(ANTMODE nextMode, Action action) {
		// Set a new mode with action
		currentPlan.clear();
		this.mode = nextMode;
		return action;
	}

	/** If a plan exists in currentPlan, get the next appropriate action */
	private Action nextCurrentPlanAction() {

		// Get the next action from the current plan, plan isn't valid if empty
		if (currentPlan.size() > 0) {
			// "From" is the current location of the ant
			Cell from = getCurrentCell();

			// Keep track of all the movements since last at home, useful for
			// taking the same route back to home that was taken to food
			fromHomePlan.push(from);
			Cell to = currentPlan.pop();
			Direction dir = from.directionTo(to);
			Action action = Action.move(dir);

			if (isActionValid(action))
				return action;
		}
		return null;
	}

	/** Tries to find a plan to goalType and writes the path to currentPlan. */
	private boolean canFindValidPlanTo(CELLTYPE goalType) {
		// Try to find the requested cell type, return false if not valid
		Stack<Cell> newPlan = planner.makePlan(worldMap, this.getCurrentCell(), goalType);

		// If newPlan is not valid, return false because no path can be found
		if (newPlan == null || newPlan.size() == 0)
			return false;

		// Plan is valid, so replace current plan with it
		this.currentPlan = newPlan;

		return true;
	}

	/**
	 * This updates the internal location of the ant depending on the direction
	 * of the next action
	 */
	private void updateLocation(Direction direction) {
		switch (direction) {
		case NORTH:
			this.y++;
			break;
		case EAST:
			this.x++;
			break;
		case SOUTH:
			this.y--;
			break;
		case WEST:
			this.x--;
			break;
		default:
			throw new IllegalArgumentException("Invalid direction");
		}
	}

	/**
	 * Before returning, making sure action is valid. This is important to check
	 * because if the action isn't checked and an invalid action is returned, it
	 * is possible for the ant to not know its true location, and then the ant
	 * has lots of errors to deal with.
	 */
	private boolean isActionValid(Action action) {
		if (action == null)
			return false;
		Direction dir = action.getDirection();
		if (dir == null)
			return true; // action is non-directional

		// action has a direction, so check if travelable
		if (surroundings.getTile(dir).isTravelable())
			return true;

		new RuntimeException("Invalid Action");
		return false;
	}

	// Below methods are either trivial, mutators, or used for JUnit tests
	public String toString() {
		return "Ant at: [" + x + ", " + y + "] ";
	}

	public Cell getCurrentCell() {
		return getCell(this.x, this.y);
	}

	public byte[] send() {
		byte[] arr = ObjectIO.toByteArray(worldMap.getMap());
		return arr;
	}

	public void receive(byte[] data) {
		Hashtable<Point, Cell> otherWorldMap = ObjectIO.fromByteArray(data);
		worldMap.mergeMaps(otherWorldMap);
	}

	private void copyStack(Stack<Cell> from, Stack<Cell> to) {
		for (int i = 0; i < from.size(); i++)
			to.push(from.get(i));
	}

	private boolean isAtHome() {
		return (this.x == 0 && this.y == 0);
	}

	public WorldMap getWorldMap() {
		return worldMap;
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Stack<Cell> getCurrPlan() {
		return currentPlan;
	}

	public Cell getCell(int x, int y) {
		return worldMap.getCell(x, y);
	}
}
