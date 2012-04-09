package vohra;

import java.awt.Point;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Stack;

import vohra.Cell.CELLTYPE;
import vohra.searches.BFS;
import vohra.searches.Djikstra;
import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class MyAnt implements Ant {

	// Modes the ant can be in, used for the FSM
	public enum MODE {
		EXPLORE, SCOUT, TOFOOD, TOHOME
	}

	public static int order = 0;
	public static final int DEBUGLEVEL = 1;
	private final int antnum;

	// Initial number of turns to take before returning home for scout
	private int scoutModeCounter = 20;

	// The reset number for the scout mode counter
	private final int scoutModeTurns = 30;

	private final int NUMFOODATHOME_TOFOODCONSTANT = 600;

	// Scouts will be in scout mode until below number of food are found
	private final int numFoodToFindforScouts = 625;

	// Ant properties
	private boolean carryingFood = false;
	private boolean isScout = false;
	private int x, y; // location of the ant
	private MODE mode;
	private Stack<Cell> currentPlan; // pre-determined planned route to a target
	private final Stack<Cell> fromHomePlan; // path from last time at home

	// Holds the knowledge each ant has of the world
	private final WorldMap worldMap;

	// Surroundings object given by engine, used as a class variable because it
	// is used at various times, easier than passing it around
	private Surroundings surroundings;

	// Se-Deserializer for communication
	private final ObjectIO<Hashtable<Point, Cell>> ObjectIO = new ObjectIO<Hashtable<Point, Cell>>();

	// Flag for indicating new info has been merged in the world map from
	// another ant
	private boolean mergeMapUpdate = true;

	// Flag for when world map receives new information from surroundings
	// Instead of searching whole map when just surroundings have been updated
	// this allows for just the immediate cells to be searched
	private boolean surroundingsUpdated = false;

	private Planner planner = new BFS();

	public MyAnt() {
		this.antnum = order++; // TODO remove
		this.worldMap = new WorldMap();
		this.currentPlan = new Stack<Cell>();
		this.fromHomePlan = new Stack<Cell>();
		this.mode = MODE.EXPLORE;
	}

	public Action getAction(Surroundings surroundings) {
		this.surroundings = surroundings;
		surroundingsUpdated |= worldMap.updateMap(surroundings, x, y);

		debugPrint(1, "");
		debugPrint(1, this.toString());
		debugPrint(1, toString());
		debugPrint(1, getCurrentCell().toString());

		int currentCellNumFood = getCurrentCell().getNumFood();
		int currentCellNumAnts = getCurrentCell().getNumAnts();

		// The initial three ants to be scouts, this is checked by the following
		// conditions which can happen at times, but are rare
		if (isAtHome() && currentCellNumFood == 0 && !isScout
				&& currentCellNumAnts == 3) {
			isScout = true;
			mode = MODE.SCOUT;
			debugPrint(1, "Initial Scout Mode");
			return Action.HALT;
		}

		debugPrint(1, "Starting in Mode: " + mode);

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
				debugPrint(1, "Action: " + actionToString(action));
			return action;
		}
		throw new RuntimeException("Invalid Action");
	}

	private Action modeScout() {
		Action action;

		// Decreasing the scout mode counter before switching to TOFOOD mode
		scoutModeCounter--;
		System.out.println("scoutModeTurns: " + scoutModeCounter);
		if (scoutModeCounter > 0) { // Still in scout mode
			// If plan exists, continue with it
			action = nextPlanAction();
			if (action != null)
				return action;
			// If not, make one to closest unexplored
			else if (canFindValidPlanTo(CELLTYPE.UNEXPLORED))
				return nextPlanAction();
		}
		// Not in scout mode anymore, transition to Food mode
		return changeMode(MODE.TOFOOD);
	}

	private Action modeToFood() {
		int currentCellNumFood = getCurrentCell().getNumFood();
		Action action;

		// If food on target doesn't doesn't exist anymore, re-plan
		if ((mergeMapUpdate || surroundingsUpdated) && !currentPlan.isEmpty()
				&& currentPlan.firstElement().getNumFood() == 0) {
			mergeMapUpdate = false;
			surroundingsUpdated = false;
			currentPlan.clear(); // clearing, so re-planning is neeeded
			debugPrint(1, "Target doesn't have food, need to replan");
		}

		// Continue a plan if it exists
		action = nextPlanAction();
		if (action != null)
			return action;

		// Ant is on food cell (which is not home), gather
		if (!isAtHome() && currentCellNumFood > 0 && !carryingFood) {
			carryingFood = true;
			getCurrentCell().decrementFood();
			debugPrint(1, "GATHERING");
			return changeModeWithAction(MODE.TOHOME, Action.GATHER);
		}

		// Make plan to closest food source
		if (canFindValidPlanTo(CELLTYPE.FOOD))
			return nextPlanAction();

		// If current cell food is > constant, food close to the mound has
		// probably been found. It's better to wait for an update from another
		// ant
		if (isAtHome() && currentCellNumFood > NUMFOODATHOME_TOFOODCONSTANT) {
			return Action.HALT;
		}

		debugPrint(1, "Can't find food, going to explore");
		return changeMode(MODE.EXPLORE);
	}

	private Action modeExplore() {
		Action action;
		int currentCellNumFood = getCurrentCell().getNumFood();

		// WorldMap was recently updated, see if food source exists nearby
		if (mergeMapUpdate && currentCellNumFood == 0) {
			mergeMapUpdate = false;
			return changeMode(MODE.TOFOOD);
		}

		// Doing a full food search when only the immediate surroundings is
		// updated is wasteful, so just check for food in 4 surrounding tiles
		if (surroundingsUpdated) {
			surroundingsUpdated = false;
			for (int i = 0; i < 4; i++) {
				Tile tile = surroundings.getTile(Direction.values()[i]);
				if (tile.getAmountOfFood() > 0)
					return changeMode(MODE.TOFOOD);
			}
		}

		// If a plan exists, follow it
		action = nextPlanAction();
		if (action != null)
			return action;

		// Try to find closest unexplored
		if (worldMap.getCell(0, 0).getNumFood() < NUMFOODATHOME_TOFOODCONSTANT
				&& canFindValidPlanTo(CELLTYPE.UNEXPLORED))
			return nextPlanAction();

		// Everything is explored, go home
		return changeMode(MODE.TOHOME);
	}

	private Action modeToHome() {
		Action action;

		// If ant went to home because there is nothing left
		// to explore, try to find food again
		if (isAtHome() && !carryingFood)
			return changeMode(MODE.TOFOOD);

		// Drop off food
		if (isAtHome() && carryingFood) {
			carryingFood = false;
			fromHomePlan.clear();

			// If scout, go back to scout mode
			if (isScout && worldMap.getNumFoodFound() <= numFoodToFindforScouts) {
				debugPrint(1, "Resetting Countdown");
				scoutModeCounter = scoutModeTurns;
				return changeModeWithAction(MODE.SCOUT, Action.DROP_OFF);
			}
			// Not scout, so go back to food
			return changeModeWithAction(MODE.TOFOOD, Action.DROP_OFF);
		}

		// If a plan exists, follow it
		if ((action = nextPlanAction()) != null)
			return action;

		// Attempt to make plan to go home
		else if (canFindValidPlanTo(CELLTYPE.HOME)) {

			// If the plan to home is the same size as the path from home,
			// take the reverse of path from home. To hopefully
			// maximize talking with ants who followed the same path.
			if (fromHomePlan.size() == currentPlan.size()) {
				currentPlan.clear();
				copyStack(fromHomePlan, currentPlan);
			}
			// Since at home
			fromHomePlan.clear();
			return nextPlanAction();
		}
		throw new RuntimeException(
				"Can't find home, map might have been accidentally changed");
	}

	private Action changeMode(MODE nextMode) {
		debugPrint(1, "Changing from: " + this.mode + " to: " + nextMode);

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

	private Action changeModeWithAction(MODE nextMode, Action action) {
		debugPrint(1, "Changing to Mode: " + nextMode + " and Action: "
				+ actionToString(action));

		// Set a new mode with action
		currentPlan.clear();
		this.mode = nextMode;
		return action;
	}

	private Action nextPlanAction() {
		Action action;

		// Get the next action from the current plan, plan isn't valid if empty
		if (currentPlan.size() > 0) {
			// "From" is the current location of the ant
			Cell from = getCurrentCell();
			// Keep track of all the movements since last at home
			fromHomePlan.push(from);
			Cell to = currentPlan.pop();
			Direction dir = from.dirTo(to);
			action = Action.move(dir);

			if (isActionValid(action))
				return action;
		}
		return null;
	}

	public byte[] send() {
		long start = System.currentTimeMillis();
		byte[] arr = ObjectIO.toByteArray(worldMap.getMap());
		debugPrint(1, "To Serialize: " + worldMap.numKnownCells() + " "
				+ (System.currentTimeMillis() - start));
		return arr;
	}

	public void receive(byte[] data) {
		Hashtable<Point, Cell> otherWorldMap = ObjectIO.fromByteArray(data);
		debugPrint(1, " Merging on: ");
		mergeMapUpdate |= worldMap.mergeMaps(otherWorldMap);
	}

	private boolean canFindValidPlanTo(CELLTYPE goalType) {
		// Try to find the requested cell type, return false if not valid
		Stack<Cell> newPlan = MapOps.makePlan(worldMap, this.getCurrentCell(),
				goalType, planner);
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

	private void copyStack(Stack<Cell> from, Stack<Cell> to) {
		for (int i = 0; i < from.size(); i++)
			to.push(from.get(i));
	}

	private boolean isAtHome() {
		return (this.x == 0 && this.y == 0);
	}

	private boolean isActionValid(Action action) {
		if (action == null)
			return false;
		Direction dir;
		if ((dir = action.getDirection()) == null)
			return true; // action is non-directional

		// action has a direction, so check if travelable
		if (surroundings.getTile(dir).isTravelable())
			return true;
		return false;
	}

	private String actionToString(Action action) {
		if (action == Action.DROP_OFF)
			return "Drop off";
		else if (action == Action.GATHER)
			return "Gather";
		else if (action == Action.HALT)
			return "Halt";
		throw new IllegalArgumentException("UnrecognizedAction: " + action);
	}

	private void printPath(Stack<Cell> currRoute) {
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		for (int i = 0; i < currRoute.size(); i++) {
			MyAnt.debugPrint(1, currRoute.get(i).toString());
		}
		MyAnt.debugPrint(1, "");
	}

	public String toString() {
		return "Ant Num: " + antnum + " at: [" + x + ", " + y + "] ";
	}

	public static void debugPrint(int num, String message) {
		if (DEBUGLEVEL == 0)
			return;
		if (num >= DEBUGLEVEL) {
			System.out.println(num + ": " + message);
			if (num == 2)
				try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}

	private void waitForReturn() {
		Scanner sc = new Scanner(System.in);
		while (!sc.nextLine().equals(""))
			;
	}

	// Getters and Setters below are either for methods that
	// are called frequently or used for JUnit tests
	public Cell getCurrentCell() {
		return getCell(this.x, this.y);
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
