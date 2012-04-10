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

	/**
	 * Modes the ant can be in, used for the FSM, described in README
	 */
	public enum ANTMODE {
		EXPLORE, SCOUT, TOFOOD, TOHOME
	}

	public static int order = 0;
	public static final int DEBUGLEVEL = 1;
	private final int antnum;

	/**
	 * Initial number of turns to take before returning home for scout
	 */
	private int scoutModeTurnsCounter = 20;

	/**
	 * The reset number for the scout mode counter
	 */
	private final int scoutModeCounterResetValue = 25;

	/**
	 * If more than the following number of food exists on HOME, ants will not
	 * explore but instead wait at home because the idea is that food sources
	 * close to the mound are probably depleted, so it would be wiser to wait
	 * for a world map update from another ant to find the food source. This
	 * minimizes wasted time in EXPLORE mode.
	 */
	private final int stopExploringAfterNumFoodFound = 200;

	/**
	 * Scouts will be in scout mode until below number of food are found
	 */
	private final int numFoodToFindForScouts = 650;

	// Ant properties
	private boolean carryingFood = false;
	private boolean isScout = false;
	private int x, y; // location of the ant
	private ANTMODE mode;
	private Stack<Cell> currentPlan; // Pre-determined planned route to a target
	private Stack<Cell> fromHomePlan; // Path from last time at home

	// Holds the knowledge each ant has of the world
	private final WorldMap worldMap;

	// Given by engine, making a class variable because it is used at various
	// times, easier than passing it around
	private Surroundings surroundings;

	// Se-Deserializer object for communication
	private final ObjectIO<Hashtable<Point, Cell>> ObjectIO = new ObjectIO<Hashtable<Point, Cell>>();

	// Flag for indicating new info has been merged in the world map from
	// another ant

	// Flag for when world map receives new information from surroundings
	// Instead of searching whole map when just surroundings have been updated
	// this allows for just the immediate cells to be searched

	// The type of search algorithm used, have implemented BFS
	private Planner planner = BFS.getSingleInstance();

	public VohraAnt() {
		this.antnum = order++; // TODO remove
		this.worldMap = new WorldMap();
		this.currentPlan = new Stack<Cell>();
		this.fromHomePlan = new Stack<Cell>();
		this.mode = ANTMODE.EXPLORE;
	}

	public Action getAction(Surroundings surroundings) {

		this.surroundings = surroundings;
		worldMap.surroundingsUpdate(surroundings, x, y);

		ExtraMethods.debugPrint(1, "");
		ExtraMethods.debugPrint(1, this.toString());
		ExtraMethods.debugPrint(1, toString());
		ExtraMethods.debugPrint(1, getCurrentCell().toString());

		int currentCellNumFood = getCurrentCell().getNumFood();
		int numAntsAround = worldMap.getNumAntsAroundCell(getCurrentCell());

		// The initial three ants to be scouts are meant to be scouts
		if (isAtHome() && currentCellNumFood == 0 && !isScout
				&& numAntsAround == 3) {
			//isScout = true;
			mode = ANTMODE.SCOUT;
			ExtraMethods.debugPrint(1, "Initial Scout Mode");
			return Action.HALT;
		}

		ExtraMethods.debugPrint(1, "Starting in Mode: " + mode);

		// Determine next action using FSMs
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
			if (action.getDirection() != null)
				// directional actions need to update location
				updateLocation(action.getDirection());
			else
				// Action is a non-direction move
				ExtraMethods.debugPrint(1,
						"Action: " + ExtraMethods.actionToString(action));
			return action;
		}
		throw new RuntimeException("Invalid Action");
	}

	private Action modeScout() {
		// If high amount of food has already been found, exit scout mode
		if (worldMap.getNumFoodFound() >= numFoodToFindForScouts)
			isScout = false;

		// Number of turns before exiting scout mode and changing to explore
		// mode
		scoutModeTurnsCounter--;

		if (scoutModeTurnsCounter > 0) { // Still in scout mode
			// If plan exists, continue with it
			Action action = nextCurrentPlanAction();
			if (action != null)
				return action;
			// If not, make one to closest unexplored
			else if (canFindValidPlanTo(CELLTYPE.UNEXPLORED))
				return nextCurrentPlanAction();
		} else {
			ExtraMethods.debugPrint(1, "Resetting Countdown");
			scoutModeTurnsCounter = scoutModeCounterResetValue;
		}
		// Not in scout mode anymore, transition to Food mode
		return changeMode(ANTMODE.EXPLORE);
	}

	private Action modeToFood() {
		int currentCellNumFood = getCurrentCell().getNumFood();

		// If food on target doesn't doesn't exist anymore, re-plan
		if (worldMap.isFoodUpdatedAndReset() && !currentPlan.isEmpty()
				&& currentPlan.firstElement().getNumFood() == 0) {
			currentPlan.clear(); // clearing, so re-planning is neeeded
			ExtraMethods.debugPrint(1,
					"Target doesn't have food, need to replan");
		}

		// Continue a plan if it exists
		Action action = nextCurrentPlanAction();
		if (action != null)
			return action;

		// Ant is on food cell (which is not home), gather
		if (!isAtHome() && currentCellNumFood > 0 && !carryingFood) {
			carryingFood = true;
			getCurrentCell().decrementFood();
			ExtraMethods.debugPrint(1, "GATHERING");
			return changeModeWithAction(ANTMODE.TOHOME, Action.GATHER);
		}

		// Make plan to closest food source
		if (canFindValidPlanTo(CELLTYPE.FOOD))
			return nextCurrentPlanAction();

		ExtraMethods.debugPrint(1, "Can't find food, going to explore");
		return changeMode(ANTMODE.EXPLORE);
	}

	private Action modeExplore() {

		// WorldMap was recently updated, see if food source exists nearby
		if (worldMap.isFoodUpdatedAndReset()) {
			return changeMode(ANTMODE.TOFOOD);
		}

		// If a plan exists, follow it
		Action action = nextCurrentPlanAction();
		if (action != null)
			return action;

		Cell HOMECell = worldMap.getCell(0, 0);

		// If HOME has more than CONSTANT num of food, food close to the mound
		// has probably been found. It's better to wait for an update from
		// another ant
		if (HOMECell.getNumFood() > this.stopExploringAfterNumFoodFound)
			if (isAtHome()) {
				ExtraMethods.debugPrint(1, "test");
				return Action.HALT;
			} else
				changeMode(ANTMODE.TOHOME);

		// Try to find closest unexplored
		if (canFindValidPlanTo(CELLTYPE.UNEXPLORED))
			return nextCurrentPlanAction();

		if (isAtHome()) {
			if (getCurrentCell().getNumAnts() < 10) {
				ExtraMethods.debugPrint(1, "AtHome");
				return Action.HALT;
			} else {
				isScout = true;
				changeModeWithAction(ANTMODE.SCOUT, Action.HALT);
			}

		}
		// Everything is explored, go home
		return changeMode(ANTMODE.TOHOME);
	}

	private Action modeToHome() {

		// If ant went to home because there is nothing left
		// to explore, try to find food again
		if (isAtHome() && !carryingFood)
			return changeMode(ANTMODE.EXPLORE);

		// Drop off food
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

		// If a plan exists, continue with it
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

		// When at home and can't find food or unexplored,
		// happens when mound is on an island
		if (isAtHome() && !canFindValidPlanTo(CELLTYPE.UNEXPLORED)
				&& !canFindValidPlanTo(CELLTYPE.FOOD))
			return Action.HALT;
		throw new RuntimeException("Can't find home, map error?");
	}

	/**
	 * Useful for FSM transitions Deleting the current plan whenever there is a
	 * transition
	 */
	private Action changeMode(ANTMODE nextMode) {
		ExtraMethods.debugPrint(1, "Changing from: " + this.mode + " to: "
				+ nextMode);

		// Clear current plan when changing modes
		currentPlan.clear();
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

	private Action changeModeWithAction(ANTMODE nextMode, Action action) {
		ExtraMethods.debugPrint(1, "Changing to Mode: " + nextMode
				+ " and Action: " + ExtraMethods.actionToString(action));

		// Set a new mode with action
		currentPlan.clear();
		this.mode = nextMode;
		return action;
	}

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

	public byte[] send() {
		long start = System.currentTimeMillis();
		byte[] arr = ObjectIO.toByteArray(worldMap.getMap());
		ExtraMethods.debugPrint(1, "To Serialize: " + worldMap.numKnownCells()
				+ " " + (System.currentTimeMillis() - start));
		return arr;
	}

	public void receive(byte[] data) {
		Hashtable<Point, Cell> otherWorldMap = ObjectIO.fromByteArray(data);
		ExtraMethods.debugPrint(1, " Merging on: ");
		worldMap.mergeMaps(otherWorldMap);
	}

	private boolean canFindValidPlanTo(CELLTYPE goalType) {
		// Try to find the requested cell type, return false if not valid
		Stack<Cell> newPlan = planner.makePlan(worldMap, this.getCurrentCell(),
				goalType);

		// If newPlan is not valid, return false because no path can be found
		if (newPlan == null || newPlan.size() == 0)
			return false;

		// Plan is valid, so replace current plan with it
		this.currentPlan = newPlan;

		return true;
	}

	private void updateLocation(Direction direction) {
		// Depending on the direction, update location
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
		return "Ant Num: " + antnum + " at: [" + x + ", " + y + "] ";
	}

	public Cell getCurrentCell() {
		return getCell(this.x, this.y);
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
