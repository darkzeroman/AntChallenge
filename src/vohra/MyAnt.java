package vohra;

import java.awt.Point;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Stack;

import vohra.Cell.TYPE;
import vohra.searches.BFS;
import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class MyAnt implements Ant {
	private int[][] offsets = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

	public enum MODE {
		EXPLORE, SCOUT, TOFOOD, TOHOME
	}

	public static int order = 0;
	public static final int DEBUGLEVEL = 1;

	private int scoutModeTurns = 20;
	private final int scoutSearchsLimit = 30;
	private final ObjectIO<Hashtable<Point, Cell>> ObjectIO = new ObjectIO<Hashtable<Point, Cell>>();
	private final int antnum;
	private boolean carryingFood = false;
	private boolean isScout = false;
	private boolean mapUpdated = true;
	private boolean surroundingsUpdated = false;
	private MODE mode;
	private int x, y;
	private Stack<Cell> currPlan;
	private final Stack<Cell> totalPlan;
	private final WorldMap worldMap;
	private Surroundings surroundings;

	public MyAnt() {
		// TODO remove
		this.antnum = order++;
		this.worldMap = new WorldMap();
		this.currPlan = new Stack<Cell>();
		this.totalPlan = new Stack<Cell>();
		this.mode = MODE.EXPLORE;
		worldMap.getCell(0, 0).setType(Cell.TYPE.HOME);
	}

	public Action getAction(Surroundings surroundings) {
		this.surroundings = surroundings;
		surroundingsUpdated |= worldMap.updateMap(surroundings, x, y);

		debugPrint(1, "");
		debugPrint(1, this.toString());
		debugPrint(1, toString());
		debugPrint(1, getCurrCell().toString());

		int currCellFood = getCurrCell().getAmountOfFood();
		int currCellNumAnts = getCurrCell().getNumAnts();

		if (isAtHome() && currCellFood == 0 && !isScout && currCellNumAnts == 3) {
			isScout = true;
			mode = MODE.SCOUT;
			debugPrint(1, "Initial Scout Mode");
			return Action.HALT;
		}

		// Special Actions here

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
				updateXY(action.getDirection());
			else
				// Action is a non-direction move
				debugPrint(1, "Action: " + actionToString(action));
			return action;
		}
		throw new RuntimeException("Invalid Action");
	}

	private Action modeScout() {
		Action action;
		scoutModeTurns--;

		if (scoutModeTurns > 0) {
			// If plan exists, continue with it
			if ((action = nextPlanAction()) != null)
				return action;
			// If not, make one
			else if (canFindType(Cell.TYPE.UNEXPLORED))
				return nextPlanAction();
		}
		// Not in scout mode anymore, transition to Food mode
		return changeMode(MODE.TOFOOD);
	}

	private Action modeToFood() {
		int currCellFood = getCurrCell().getAmountOfFood();
		Action action;

		// if food on target doesn't doesn't exist anymore, re-plan
		if (mapUpdated && !currPlan.isEmpty()
				&& currPlan.firstElement().getAmountOfFood() == 0) {
			currPlan.clear();
			debugPrint(1, "Target doesn't have food, need to replan");
		}

		// Continue a plan if it exists
		if ((action = nextPlanAction()) != null) {
			return action;

		} else if (!isAtHome() && currCellFood > 0 && !carryingFood) {
			// ant is on food tile, gather
			carryingFood = true;
			getCurrCell().decrementFood();
			debugPrint(1, "GATHERING");

			return changeModeAndAction(MODE.TOHOME, Action.GATHER);

		} else if ((canFindType(Cell.TYPE.FOOD))) {
			// don't have a plan, so make one
			return nextPlanAction();
		}
		if (isAtHome() && currCellFood > 200) {
			// Stay at home, some ant should come with a plan
			return Action.HALT;
		}

		debugPrint(1, "Can't find food, going to explore");
		return changeMode(MODE.EXPLORE);
	}

	public void printPath(Stack<Cell> currRoute) {
		MyAnt.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		for (int i = 0; i < currRoute.size(); i++) {
			MyAnt.debugPrint(1, currRoute.get(i).toString());
		}
		MyAnt.debugPrint(1, "");
	}

	private Action modeExplore() {
		Action action;
		int currCellFood = getCurrCell().getAmountOfFood();

		// WorldMap was recently updated, see if food source exists nearby
		if (mapUpdated && currCellFood == 0) {
			mapUpdated = false;
			return changeMode(MODE.TOFOOD);
		}
		// Instead of doing a full graph search when the immediate surroundings
		// are updated
		// check specifically for food in 4 surrounding tiles
		if (surroundingsUpdated) {
			surroundingsUpdated = false;
			for (int i = 0; i < 4; i++) {
				Tile tile = surroundings.getTile(Direction.values()[i]);
				if (tile.getAmountOfFood() > 0) {
					return changeMode(MODE.TOFOOD);
				}
			}
		}
		// If a plan exists, follow it
		if ((action = nextPlanAction()) != null)
			return action;

		// Try to find closest unexplored
		if (worldMap.getCell(0, 0).getAmountOfFood() < 300
				&& canFindType(Cell.TYPE.UNEXPLORED))
			return nextPlanAction();

		// Everything is explored, Go home
		return changeMode(MODE.TOHOME);
	}

	private Action modeToHome() {
		Action action;
		if (isAtHome() && !carryingFood)
			return changeMode(MODE.TOFOOD);

		// drop off food if at home
		if (isAtHome() && carryingFood) {
			carryingFood = false;
			mode = MODE.TOFOOD;
			totalPlan.clear();

			// For resetting the scout mode limit
			if (isScout && getAmountFoodFound() < 625) {
				// getAmountFoodFound() < 6
				debugPrint(1, "Resetting Countdown");
				scoutModeTurns = scoutSearchsLimit;
				mode = MODE.SCOUT;
			}
			return changeModeAndAction(mode, Action.DROP_OFF);
		}

		// Continue with plan
		if ((action = nextPlanAction()) != null)
			return action;
		// Make plan to home
		else if (canFindType(Cell.TYPE.HOME)) {
			// TODO test
			printPath(totalPlan);
			printPath(currPlan);
			if (totalPlan.size() == currPlan.size()) {
				currPlan.clear();

				for (int i = 0; i < totalPlan.size(); i++)
					currPlan.push(totalPlan.get(i));
			}
			printPath(currPlan);

			totalPlan.clear();
			// waitForReturn();
			return nextPlanAction();
		}
		throw new RuntimeException("Can't find home, map might be corrupted");
	}

	private Action changeMode(MODE nextMode) {
		debugPrint(1, "Changing from: " + this.mode + " to: " + nextMode);

		// Clear current plan when changing modes
		currPlan.clear();
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

	private Action changeModeAndAction(MODE nextMode, Action action) {
		debugPrint(1, "Changing to Mode: " + nextMode + " and Action: "
				+ actionToString(action));
		// Set a new mode and action
		currPlan.clear();
		this.mode = nextMode;
		return action;
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

	private Action nextPlanAction() {
		Action action;

		if (currPlan.size() > 0) {
			Cell from = getCurrCell();
			totalPlan.push(from);

			Cell to = currPlan.pop();
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
		// debugPrint(1, " Merging on: " + otherantnum);
		mapUpdated |= worldMap.merge(otherWorldMap);
	}

	private boolean isAtHome() {
		return (x == 0 && y == 0);
	}

	private boolean isActionValid(Action action) {
		if (action == null)
			return false;
		Direction dir;
		if ((dir = action.getDirection()) == null)
			return true;
		if (surroundings.getTile(dir).isTravelable())
			return true;
		return false;
	}

	public boolean canFindType(Cell.TYPE goalType) {
		Stack<Cell> newPlan = MapOps.makePlan(worldMap, this.getCurrCell(),
				goalType, new BFS());

		if (newPlan == null || newPlan.size() == 0)
			return false;

		this.currPlan = newPlan;
		return true;
	}

	public String toString() {
		return "Ant Num: " + antnum + " at: [" + x + ", " + y + "] ";
	}

	public static void debugPrint(int num, String message) {
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

	public void updateXY(Direction dir) {
		switch (dir) {
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

	public int getAmountFoodFound() {
		int sum = 0;
		Enumeration<Cell> e = worldMap.getMap().elements();
		while (e.hasMoreElements()) {
			sum += e.nextElement().getOriginalAmountOfFood();
		}
		return sum;
	}

	public Cell getCurrCell() {
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
		return currPlan;
	}

	public Cell getCell(int x, int y) {
		return worldMap.getCell(x, y);
	}
}
