import java.awt.Point;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;

public class MyAnt implements Ant {

	static int order = 0;
	static int DEBUG = 2;

	public static void main(String[] args) {
		new MyAnt();
		System.out.println("compiled");
	}

	private int scoutSearchLimit = 20;

	private final Knowledge knowledge;

	private final ObjectIO<Knowledge> oio = new ObjectIO<Knowledge>();
	private Surroundings surroundings;

	public MyAnt() {
		// TODO remove
		knowledge = new Knowledge(order++);

	}

	int count = 2;

	private void waitForReturn() {
		if (count == 0) {
			Scanner sc = new Scanner(System.in);
			while (!sc.nextLine().equals(""))
				;
			count = 2;
		}
		count--;
	}

	public Action getAction(Surroundings surroundings) {

		this.surroundings = surroundings;
		knowledge.round++;

		knowledge.updateMap(surroundings, getLocX(), getLocY());
		// waitForReturn();

		System.out.println();
		debugPrint(1, this.toString());
		debugPrint(1, knowledge.toString());
		debugPrint(1, getCurrCell().toString());

		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();
		int currTileNumAnts = surroundings.getCurrentTile().getNumAnts();

		if (isAtHome() && currTileFood == 0 && !knowledge.isScout && currTileNumAnts == 3) {
			knowledge.isScout = true;
			knowledge.mode = Knowledge.Mode.SCOUT;
			debugPrint(1, "Initial Scout Mode");
			return Action.HALT;
		}

		// Special Actions here

		if (knowledge.antnum < 3)
			knowledge.isScout = true;
		if (knowledge.antnum == 4)
			return Action.HALT;

		debugPrint(1, "Starting in Mode: " + knowledge.mode);

		Action action = null;

		switch (knowledge.mode) {
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
		}

		// updating local knowledge
		if (isActionValid(action)) {
			if (action.getDirection() != null) {
				Direction direction = action.getDirection();
				updateCurrLoc(direction);
				debugPrint(1, "Moving: " + direction);
				knowledge.lastDir = direction;
			} else {
				debugPrint(1, "Action: " + actionToString(action));
			}
			return action;

		} else {
			debugPrint(2, "Action was not valid");
			return Action.HALT;
		}

	}

	private Action modeScout() {

		Action action;
		scoutSearchLimit--;
		// if still in scout mode, and if path exists follow it, otherwise
		// make one
		if (scoutSearchLimit > 0) {
			if ((action = nextRouteAction()) != null)
				return action;
			else if (foundUnexplored("Scout Mode"))
				return nextRouteAction();

		}
		// not in scout mode anymore, switch to food mode;
		return changeMode(Knowledge.Mode.TOFOOD);

	}

	private Action modeToFood() {
		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();

		Action action;

		// if food doesn't exist, re-plan
		// follow path
		// if at food, pick it up
		// don't have a plan, make one

		if (knowledge.isUpdated() && !getCurrRoute().isEmpty()
				&& getCurrRoute().lastElement().getAmountOfFood() == 0) {
			getCurrRoute().clear();
			debugPrint(1, "Target doesn't have food, need to replan");
		}

		// if ant already has a goal, keep going
		if ((action = nextRouteAction()) != null) {
			return action;

		} else if (!isAtHome() && currTileFood > 0 && !knowledge.carryingFood) {
			// ant is on food tile, gather
			knowledge.carryingFood = true;
			getCurrCell().decrementFood();
			debugPrint(1, "GATHERING");
			return changeModeAndAction(Knowledge.Mode.TOHOME, Action.GATHER);

		} else if ((foundFood("Food"))) {
			// don't have a plan, so make one
			return nextRouteAction();
		}
		debugPrint(1, "Can't find food, going to explore");
		// order, hasFood, isScout, lastDir, mode, round,

		return changeMode(Knowledge.Mode.EXPLORE);

	}

	private Action modeExplore() {
		Action action;
		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();

		if (knowledge.isUpdated() && currTileFood == 0
				&& foundFood("Explore food")) {
			// map was recently updated, see if food source exists nearby
			knowledge.setUpdated(false);
			return changeMode(Knowledge.Mode.TOFOOD);
		}

		// get next step from route and check if it's travelable
		if ((action = nextRouteAction()) != null)
			return action;

		// try to find closest unexplored
		debugPrint(1, "looking for unexplored");
		if (foundUnexplored("Exploring unexplored"))
			return nextRouteAction();
		// if everything is explored, return to home
		return changeMode(Knowledge.Mode.TOHOME);
	}

	private Action modeToHome() {
		Action action;
		// TODO remove
		if (!knowledge.carryingFood)
			debugPrint(2, "Why is ant trying to go home without food?");

		if (isAtHome()) { // at home
			knowledge.carryingFood = false;
			knowledge.mode = Knowledge.Mode.TOFOOD;
			if (knowledge.isScout && knowledge.getTotalFoodFound() < 650) {
				debugPrint(1, "Resetting Countdown");
				scoutSearchLimit = 20;
				knowledge.mode = Knowledge.Mode.SCOUT;
			}
			return changeModeAndAction(knowledge.mode, Action.DROP_OFF);

		}
		// continue with path
		debugPrint(1, "continuing with home path");
		if ((action = nextRouteAction()) != null)
			return action;
		else if (foundHome("TOHOME")) {
			return nextRouteAction();

		} else
			debugPrint(2, "No route && can't find home");
		debugPrint(1, "End Home");
		return null;
	}

	private Action changeMode(Knowledge.Mode mode) {
		debugPrint(1, "Changing from: " + knowledge.mode + " to: " + mode);
		getCurrRoute().clear();
		knowledge.mode = mode;
		switch (mode) {
		case SCOUT:
			return modeScout();
		case TOFOOD:
			return modeToFood();
		case EXPLORE:
			return modeExplore();
		case TOHOME:
			return modeToHome();
		}
		debugPrint(2, "Change mode shouldn't be null");
		return null;

	}

	private Action changeModeAndAction(Knowledge.Mode mode, Action action) {
		String actionString = actionToString(action);
		debugPrint(1, "Changing to Mode: " + mode + " and Action: "
				+ actionString);
		getCurrRoute().clear();
		knowledge.mode = mode;
		return action;
	}

	private String actionToString(Action action) {
		String actionString;
		if (action == Action.DROP_OFF)
			actionString = "Drop off";
		else if (action == Action.GATHER)
			actionString = "Gather";
		else
			actionString = "HALT";

		return actionString;
	}

	private Action nextRouteAction() {
		Action action;

		if (getCurrRoute().size() > 0) {
			Cell from = getCurrCell();
			Cell to = getCurrRoute().pop();
			Direction dir = from.dirTo(to);
			action = Action.move(dir);
			if (isActionValid(action))
				return action;
		}
		return null;
	}

	public Direction nextRouteDir() {
		if (getCurrRoute().size() > 0) {
			Cell from = getCurrCell();
			Cell to = getCurrRoute().pop();
			return from.dirTo(to);
		}
		return null;
	}

	public byte[] send() {
		long start = System.currentTimeMillis();
		byte[] arr = oio.toByteArray(knowledge);
		debugPrint(1, "To Serialize: " + knowledge.numKnownCells() + " "
				+ (System.currentTimeMillis() - start));
		return arr;

	}

	public void receive(byte[] data) {
		Knowledge otherKnowledge = oio.fromByteArray(data);
		knowledge.merge(otherKnowledge);

	}

	private boolean isAtHome() {
		return (getLocX() == 0 && getLocY() == 0);
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

	private Direction randomDir(Surroundings surroundings) {
		boolean[] choices = new boolean[4];
		int numChoices = 0;
		Direction oppDir = MapOps.oppositeDir(knowledge.lastDir);
		for (int i = 0; i < 4; i++) {
			if ((oppDir != Direction.values()[i])
					&& (surroundings.getTile(Direction.values()[i])
							.isTravelable())) {
				choices[i] = true;
				numChoices++;
			}

		}
		// if count is zero, we're stuck in a corner, so need to go back
		if (numChoices == 0) {
			return MapOps.oppositeDir(knowledge.lastDir);
		}
		Random rand = new Random(System.currentTimeMillis());

		// change to a for loop at some point
		int rInt = rand.nextInt(4);
		while (!choices[rInt])
			rInt = rand.nextInt(4);
		return Direction.values()[rInt];

	}

	public boolean foundFood(String error) {
		return MapOps.makeRoute(knowledge, Cell.CellType.FOOD, error);
	}

	public boolean foundUnexplored(String error) {
		return MapOps.makeRoute(knowledge, Cell.CellType.UNEXPLORED, error);

		// return MapOps.newMakeRoute(this, Cell.CellType.UNEXPLORED, error);
	}

	public boolean foundHome(String error) {
		return MapOps.makeRoute(knowledge, Cell.CellType.HOME, error);
		// return MapOps.newMakeRoute(this, Cell.CellType.HOME, error);
	}

	public PriorityQueue<Cell> prepareForSearch(boolean checkUnexplored) {
		return knowledge.beforeSearch(checkUnexplored);
	}

	public Knowledge.Mode getMode() {
		return knowledge.mode;
	}

	private void updateCurrLoc(Direction dir) {
		// Directions: NORTH, EAST, SOUTH, WEST;
		switch (dir) {
		case NORTH:
			knowledge.setLocY(getLocY() + 1);
			break;
		case EAST:
			knowledge.setLocX(getLocX() + 1);
			break;
		case SOUTH:
			knowledge.setLocY(getLocY() - 1);
			break;
		case WEST:
			knowledge.setLocX(getLocX() - 1);
			break;
		default:
			debugPrint(2, "Not a valid direction");
		}
	}

	protected Cell getCurrCell() {
		return getCell(knowledge.getLocX(), knowledge.getLocY());
	}

	public Cell getCell(int row, int col) {
		return knowledge.get(row, col);
	}

	public int getLocX() {
		return knowledge.getLocX();
	}

	public int getLocY() {
		return knowledge.getLocY();
	}

	public void setCurrLoc(int x, int y) {
		knowledge.setCurrLoc(new Point(x, y));

	}

	public Stack<Cell> getCurrRoute() {
		return knowledge.getCurrRoute();
	}

	public void setXY(Point currLoc) {
		knowledge.setCurrLoc(currLoc);
	}

	public void setXY(int x, int y) {
		setXY(new Point(x, y));
	}

	public String toString() {

		return "Ant Num: " + knowledge.antnum + " at: [" + getLocX() + ", "
				+ getLocY() + "] ";
	}

	public static void induceSleep(long numSeconds, String error) {

	}

	public static void debugPrint(int num, String message) {
		if (num >= DEBUG) {
			System.out.println(num + ": " + message);
			if (num == 2) {
				try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
