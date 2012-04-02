import java.util.ArrayList;
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

	public static void main(String[] args) {
		new MyAnt();
		System.out.println("ran");
	}

	public int origin, round = 0, roundCountdown = 20, antnum = 0;
	private int locX, locY;
	private WorldMap map;
	private Random rand = new Random(System.currentTimeMillis());
	boolean carryingFood = false, isScout = false, firstAction = true;
	private Direction lastDir;
	protected Mode mode;
	private Stack<Cell> currRoute;
	private ObjectIO<WorldMap> oio = new ObjectIO<WorldMap>();
	Surroundings surroundings;

	public MyAnt() {
		this(40, 20); // default map size and origin
	}

	public MyAnt(int mapsize, int origin) {
		this.antnum = order;
		order++;
		this.origin = origin;
		map = new WorldMap(mapsize, antnum, origin);
		locX = locY = origin;
		mode = Mode.EXPLORE;
		currRoute = new Stack<Cell>();
		lastDir = Direction.SOUTH;

	}

	public Action getAction(Surroundings surroundings) {
		this.surroundings = surroundings;
		round++;

		Scanner sc = new Scanner(System.in);
		// if (order > 2)
		// while (!sc.nextLine().equals(""))
		// ;

		System.out.println("\nAnt Num: " + antnum + " mapSize: "
				+ getMap().sizeOfKnowledge());
		System.out.println("numAnts: "
				+ surroundings.getCurrentTile().getNumAnts());

		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();

		if (locX == origin && locY == origin && currTileFood == 0
				&& surroundings.getCurrentTile().getNumAnts() == 3) {
			isScout = true;
			mode = Mode.SCOUT;
			System.out.println("initial Scout Mode");
		}

		System.out.println(" Current " + getCurrCell());

		getMap().update(surroundings, locX, locY);

		if (firstAction) {
			firstAction = false;
			return Action.HALT;
		}
		// Special Actions here
		if (antnum == 2)
			;// return action.HALT;

		if (antnum < 3)
			isScout = true;
		// if (antnum > 5)
		// return Action.move(Direction.EAST);
		// return Action.HALT;

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
		if (action != null && action.getDirection() != null
				&& checkIfTravelable(action) == false) {
			induceSleep(10,
					this.toString() + " can't go to: " + action.getDirection());
			return Action.HALT;
		} else if (action != null && action.getDirection() != null) {
			System.out.println("Ant: " + antnum + " Moving: "
					+ action.getDirection());
			updateCurrLoc(action.getDirection());
			lastDir = action.getDirection();
			return action;
		} else if (action != null) {
			return action;

		}
		induceSleep(10, "Why is Action Null");
		return Action.HALT;

	}

	private Action modeScout() {
		Action action;
		System.out.println("SCOUT MODE");
		roundCountdown--;
		// if still in scout mode, and if path exists follow it, otherwise
		// make one
		if (roundCountdown > 0) {
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
		System.out.println("TOFOOD MODE");

		// if food doesn't exist, re-plan
		// follow path
		// if at food, pick it up
		// don't have a plan, make one

		if (getMap().recentlyUpdated && !currRoute.isEmpty()
				&& currRoute.lastElement().getAmntFood() == 0) {
			currRoute.clear();
			System.out.println("need to replan");
			// induceSleep(1000);
		}

		// if ant already has a goal, keep going
		if ((action = nextRouteAction()) != null) {
			return action;

		} else if (!isAtHome() && currTileFood > 0 && !carryingFood) {
			// ant is on food tile, gather
			carryingFood = true;
			getCurrCell().decrementAmntFood();
			System.out.println("GATHERING");
			return changeModeAndAction(Mode.TOHOME, Action.GATHER);

		} else if ((action = findFood("Food")) != null) {
			// don't have a plan, so make one
			System.out.println("making food plan and: " + carryingFood);
			return action;
		}
		System.out.println("Can't find food, going to explore");

		return changeMode(Mode.EXPLORE);

	}

	private Action modeExplore() {
		System.out.println("EXPLORE MODE");
		Action action;
		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();

		if (getMap().recentlyUpdated && currTileFood == 0
				&& findFood("Explore food") != null) {
			// map was recently updated, see if food source exists nearby
			getMap().recentlyUpdated = false;
			return changeMode(Mode.TOFOOD);
		}

		// get next step from route and check if it's travelable
		if ((action = nextRouteAction()) != null)
			return action;

		// try to find closest unexplored
		System.out.println("looking for unexplored");
		if ((action = findUnexplored("Exploring unexplored")) != null)
			return action;

		// if everything is explored, return to home
		return changeMode(Mode.TOHOME);
	}

	private Action modeToHome() {
		Action action;
		System.out.println("HOME MODE");

		if (isAtHome() && carryingFood) { // at home
			carryingFood = false;
			mode = Mode.TOFOOD;
			if (isScout && map.getTotalFoodFound() < 600) {
				System.out.println("resetting countdown");
				roundCountdown = 25;
				mode = Mode.SCOUT;
			}
			System.out.println("DROPPING OFF");
			return changeModeAndAction(mode, Action.DROP_OFF);

		}
		// continue with path
		if ((action = nextRouteAction()) != null)
			return action;
		else if (isAtHome() && !carryingFood) {
			induceSleep(10, "at home without food");
			return changeMode(Mode.EXPLORE);
		} else if ((action = findHome("TOHOME")) != null)
			return action;
		else
			induceSleep(10, "No route && can't find home");
		System.out.println("end home");
		return null;
	}

	public Action changeMode(Mode mode) {
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
		default:
			return null;
		}
	}

	public Action changeModeAndAction(Mode mode, Action action) {
		currRoute.clear();
		this.mode = mode;
		return action;
	}

	private Action nextRouteAction() {
		Action action;
		Cell fromCell = getCurrCell();
		if (currRoute.size() > 0
				&& (action = Action
						.move(MapOps.dirTo(fromCell, currRoute.pop()))) != null)
			if (checkIfTravelable(action))
				return action;
		return null;
	}

	public byte[] send() {
		return oio.toByteArray(getMap());
	}

	public void receive(byte[] data) {
		WorldMap otherKnowledge = oio.fromByteArray(data);
		System.out.println(this.antnum + " MERGING with: "
				+ otherKnowledge.antnum);
		getMap().merge(otherKnowledge);

	}

	private boolean isAtHome() {
		return (locX == origin && locY == origin);
	}

	private boolean checkIfTravelable(Action action) {
		return surroundings.getTile(action.getDirection()).isTravelable();
	}

	public static void induceSleep(long numSeconds, String error) {
		System.out.println(error);
		try {
			Thread.sleep(numSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		return MapOps.newMakeRoute(this, WorldMap.type.HOME, error);
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
			System.out.println("Not valid move");
			throw new RuntimeException("");
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
		return "Antnum: " + antnum + " Location: " + locX + ", " + locY;
	}

}
