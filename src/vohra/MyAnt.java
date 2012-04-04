package vohra;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;

public class MyAnt implements Ant {

	public enum Mode {
		EXPLORE, TOFOOD, TOHOME, SCOUT
	}

	static int order = 0;
	static int DEBUG = 2;

	public static void main(String[] args) {
		new MyAnt();
		System.out.println("compiled");
	}

	public int origin, round = 0, scoutSearchLimit = 20, antnum = 0;
	private int locX, locY;
	private WorldMap map;
	private Random rand = new Random(System.currentTimeMillis());
	boolean hasFood = false, isScout = false, firstAction = true;
	private Direction lastDir;
	protected Mode mode;
	private Stack<Cell> currRoute;
	private ObjectIO<WorldMap> oio = new ObjectIO<WorldMap>();
	Surroundings surroundings;
	private Action action;

	public MyAnt() {
		this(40, 20); // default map size and origin
	}

	public MyAnt(int mapsize, int origin) {
		// TODO remove
		this.antnum = order;
		order++;

		this.origin = origin;
		map = new WorldMap(mapsize, antnum, origin);
		locX = locY = origin;

		this.mode = Mode.EXPLORE;
		this.currRoute = new Stack<Cell>();
		this.lastDir = Direction.SOUTH;

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
		round++;
		getMap().updateMap(surroundings, locX, locY);
		// waitForReturn();

		System.out.println();
		debugPrint(1, this.toString());
		debugPrint(1, map.toString());
		debugPrint(1, getCurrCell().toString());

		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();
		int currTileNumAnts = surroundings.getCurrentTile().getNumAnts();

		if (isAtHome() && currTileFood == 0 && currTileNumAnts == 3) {
			isScout = true;
			mode = Mode.SCOUT;
			debugPrint(1, "Initial Scout Mode");
		}

		if (firstAction && isScout) {
			firstAction = false;
			return Action.HALT;
		}
		// Special Actions here

		if (antnum < 3)
			isScout = true;
		// if (antnum > 5)
		// return Action.move(Direction.EAST);
		// return Action.HALT;
		debugPrint(1, "Starting in Mode: " + mode);
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
		}

		// updating local knowledge
		if (isActionValid(action)) {
			if (action.getDirection() != null) {
				this.lastDir = action.getDirection();
				updateCurrLoc(lastDir);
				debugPrint(1, "Moving: " + lastDir);
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
			else if ((action = findUnexplored("Scout Mode")) != null)
				return action;

		}
		// not in scout mode anymore, switch to food mode;
		return changeMode(Mode.TOFOOD);

	}

	private Action modeToFood() {
		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();

		Action action;

		// if food doesn't exist, re-plan
		// follow path
		// if at food, pick it up
		// don't have a plan, make one

		if (map.updated && !currRoute.isEmpty()
				&& currRoute.lastElement().getAmntFood() == 0) {
			currRoute.clear();
			debugPrint(1, "Target doesn't have food, need to replan");
		}

		// if ant already has a goal, keep going
		if ((action = nextRouteAction()) != null) {
			return action;

		} else if (!isAtHome() && currTileFood > 0 && !hasFood) {
			// ant is on food tile, gather
			hasFood = true;
			getCurrCell().decrementAmountFood();
			debugPrint(1, "GATHERING");
			return changeModeAndAction(Mode.TOHOME, Action.GATHER);

		} else if ((action = findFood("Food")) != null) {
			// don't have a plan, so make one
			return action;
		}
		debugPrint(1, "Can't find food, going to explore");

		return changeMode(Mode.EXPLORE);

	}

	private Action modeExplore() {
		Action action;
		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();

		if (map.updated && currTileFood == 0
				&& findFood("Explore food") != null) {
			// map was recently updated, see if food source exists nearby
			getMap().updated = false;
			return changeMode(Mode.TOFOOD);
		}

		// get next step from route and check if it's travelable
		if ((action = nextRouteAction()) != null)
			return action;

		// try to find closest unexplored
		debugPrint(1, "looking for unexplored");
		if ((action = findUnexplored("Exploring unexplored")) != null)
			return action;

		// if everything is explored, return to home
		return changeMode(Mode.TOHOME);
	}

	private Action modeToHome() {
		Action action;
		// TODO remove
		if (!hasFood)
			debugPrint(2, "Why without food?");

		if (isAtHome()) { // at home
			hasFood = false;
			mode = Mode.TOFOOD;
			if (isScout && map.getTotalFoodFound() < 650) {
				debugPrint(1, "Resetting Countdown");
				scoutSearchLimit = 20;
				mode = Mode.SCOUT;
			}
			return changeModeAndAction(mode, Action.DROP_OFF);

		}
		// continue with path
		debugPrint(1, "continuing with home path");
		if ((action = nextRouteAction()) != null)
			return action;

		else if ((action = findHome("TOHOME")) != null)
			return action;
		else
			debugPrint(2, "No route && can't find home");
		debugPrint(1, "End Home");
		return null;
	}

	private Action changeMode(Mode mode) {
		debugPrint(1, "Changing from: " + this.mode + " to: " + mode);
		currRoute.clear();
		this.mode = mode;
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

	private Action changeModeAndAction(Mode mode, Action action) {
		String actionString = actionToString(action);
		debugPrint(1, "Changing to Mode: " + mode + " and Action: "
				+ actionString);
		currRoute.clear();
		this.mode = mode;
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
		Cell fromCell = getCurrCell();
		if (currRoute.size() > 0) {
			Direction dir = MapOps.dirTo(fromCell, currRoute.pop());
			action = Action.move(dir);
			if (isActionValid(action))
				return action;

		}
		return null;
	}

	public boolean isNull(Object obj) {
		return (obj == null);
	}

	public byte[] send() {
		long start = System.currentTimeMillis();
		byte[] arr = oio.toByteArray(getMap());
		debugPrint(1, "To Serialize: " + getMap().numKnownCells() + " "
				+ (System.currentTimeMillis() - start));
		return arr;

	}

	public void receive(byte[] data) {
		WorldMap otherKnowledge = oio.fromByteArray(data);
		getMap().merge(otherKnowledge);

	}

	private boolean isAtHome() {
		return (locX == origin && locY == origin);
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

	public static void induceSleep(long numSeconds, String error) {
		System.out.println(error);
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void debugPrint(int num, String message) {
		if (num <= DEBUG)
			System.out.println(num + ": " + message);
	}

	private Direction randomDir(Surroundings surroundings) {
		boolean[] choices = new boolean[4];
		int numChoices = 0;
		Direction oppDir = MapOps.oppositeDir(lastDir);
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
			return lastDir = MapOps.oppositeDir(lastDir);
		}
		// change to a for loop at some point
		int rInt = rand.nextInt(4);
		while (!choices[rInt])
			rInt = rand.nextInt(4);
		return lastDir = Direction.values()[rInt];

	}

	public Action findFood(String error) {
		return MapOps.newMakeRoute(this, WorldMap.type.FOOD, error);
	}

	public Action findUnexplored(String error) {
		return MapOps.newMakeRoute(this, WorldMap.type.UNEXPLORED, error);
	}

	public Action findHome(String error) {
		return MapOps.makeRoute(this, WorldMap.type.HOME, error);
		// return MapOps.newMakeRoute(this, WorldMap.type.HOME, error);
	}

	private void updateCurrLoc(Direction dir) {
		// Directions: NORTH, EAST, SOUTH, WEST;
		switch (dir) {
		case NORTH:
			locY--;
			break;
		case EAST:
			locX++;
			break;
		case SOUTH:
			locY++;
			break;
		case WEST:
			locX--;
			break;
		default:
			debugPrint(2, "Not a valid move");
		}
	}

	private Cell getCurrCell() {
		return getCell(locX, locY);
	}

	public Cell getCell(int row, int col) {
		return getMap().get(row, col);
	}

	public int getLocX() {
		return locX;
	}

	public int getLocY() {
		return locY;
	}

	public void setXY(int x, int y) {
		locX = x;
		locY = y;
	}

	public WorldMap getMap() {
		return map;
	}

	public Stack<Cell> getCurrRoute() {
		return this.currRoute;
	}

	public String toString() {
		return "Ant Num: " + antnum + " at: [" + locX + ", " + locY + "] ";
	}

}
