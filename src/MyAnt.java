import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

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
	Direction lastDir;
	protected Mode mode;
	private ArrayList<Cell> currRoute;
	private ObjectIO<WorldMap> oio = new ObjectIO<WorldMap>();;

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
		currRoute = new ArrayList<Cell>();
		lastDir = Direction.SOUTH;

	}

	public Action getAction(Surroundings surroundings) {

		// Scanner sc = new Scanner(System.in);
		// while (!sc.nextLine().equals(""))
		// ;

		round++;

		System.out.println();
		System.out.println("Ant Num: " + antnum + " mapSize: "
				+ getMap().sizeOfKnowledge());
		System.out.println("numAnts: "
				+ surroundings.getCurrentTile().getNumAnts());

		int currTileFood = surroundings.getCurrentTile().getAmountOfFood();

		if (getLocX() == origin && getLocY() == origin && currTileFood == 0
				&& surroundings.getCurrentTile().getNumAnts() == 3) {
			isScout = true;
			mode = Mode.SCOUT;
			System.out.println("initial Scout Mode");
		}

		System.out.println(" Current X: " + getLocX() + " Y: " + getLocY());

		getMap().update(surroundings, getLocX(), getLocY());

		Action nextMove = null;
		if (firstAction) {
			firstAction = false;
			return Action.HALT;
		}
		// Special Actions here
		if (antnum == 2)
			;
		// return Action.HALT;

		switch (mode) {
		case SCOUT:
			System.out.println("SCOUT MODE");
			roundCountdown--;

			// if still in scout mode, and if path exists follow it, otherwise
			// make one
			if (roundCountdown > 0) {
				if ((nextMove = nextRouteAction()) != null
						&& surroundings.getTile(nextMove.getDirection())
								.isTravelable())
					break;
				else if ((nextMove = MapOps.makeRoute(this,
						WorldMap.type.UNEXPLORED, "Scout Mode")) != null)
					break;
			}

			mode = Mode.TOFOOD;

		case TOFOOD:
			System.out.println("TOFOOD MODE");
			// if food doesn't exist, re-plan
			// follow path
			// if at food, pick it up
			// don't have a plan, make one

			if (getMap().recentlyUpdated && !currRoute.isEmpty()
					&& currRoute.get(currRoute.size() - 1).getAmntFood() == 0) {
				currRoute.clear();
				System.out.println("");

			}

			// if ant already has a goal, keep going
			if ((nextMove = nextRouteAction()) != null) {
				break;

			} else if (!isAtHome() && currTileFood > 0 && !carryingFood) {
				// ant is on food tile, gather
				carryingFood = true;
				mode = Mode.TOHOME;
				getCell(locX, locY).decrementAmntFood();
				System.out.println("GATHERING");
				return Action.GATHER;

			} else {
				// don't have a plan, so make one
				if ((nextMove = MapOps.makeRoute(this, WorldMap.type.FOOD,
						"Food")) != null)
					break;
				System.out.println("Can't find food, going to explore");

			}
			mode = Mode.EXPLORE;

		case EXPLORE:
			System.out.println("EXPLORE MODE");

			if (getMap().recentlyUpdated && currTileFood == 0
					&& currRoute.size() == 0) {
				// map was recently updated, see if food source exists nearby
				getMap().recentlyUpdated = false;
				if ((nextMove = MapOps.makeRoute(this, WorldMap.type.FOOD,
						"Recently Updated")) != null)

					break;
			}
			// not at home, and not carrying food and ant is on food
			if (!isAtHome() && currTileFood > 0 && !carryingFood) {
				System.out.println("FOUND FOOD");
				carryingFood = true;
				mode = Mode.TOHOME;
				currRoute.clear();
				getCell(locX, locY).decrementAmntFood();
				return Action.GATHER;
			}

			// get next step from route and check if it's travelable
			if ((nextMove = nextRouteAction()) != null
					&& surroundings.getTile(nextMove.getDirection())
							.isTravelable())
				break;

			// try to find closest unexplored
			if ((nextMove = MapOps.makeRoute(this, WorldMap.type.UNEXPLORED,
					"Exploring unexplored")) != null)
				break;
			// if everything is explored, return to home
			mode = Mode.TOHOME;
		case TOHOME:
			System.out.println("HOME MODE");

			if (isAtHome() && carryingFood) {
				carryingFood = false;
				mode = Mode.TOFOOD;
				currRoute.clear();
				System.out.println("DROPPING OFF");
				if (isScout) {// && map.getTotalFoodFound() < 600) {
					System.out.println("resetting countdown");
					roundCountdown = 500;
					currRoute.clear();
					mode = Mode.SCOUT;
				}
				return nextMove = Action.DROP_OFF;

			}
			if ((nextMove = nextRouteAction()) != null)
				break;
			else if ((nextMove = MapOps.makeRoute(this,
					getMap().get(origin, origin), "TOHOME")) != null)
				break;
			else if (isAtHome())
				return Action.HALT;
			else
				System.out
						.println("Ant doesn't have a route and can't find home, error!");
		}
		// updating local knowledge
		if (nextMove != null && nextMove.getDirection() != null) {
			System.out.println("Ant: " + antnum + " Moving: "
					+ nextMove.getDirection());
			updateCurrLoc(nextMove.getDirection());
			lastDir = nextMove.getDirection();
		} else {
			System.out.println("Why is nextMove null");
			nextMove = Action.HALT;
		}
		return nextMove;
	}

	public Action nextRouteAction() {
		if (currRoute.size() > 0)
			return Action.move(WorldMap.dirTo(getCell(getLocX(), getLocY()),
					currRoute.remove(0)));
		else
			return null;
	}

	private boolean isAtHome() {
		return (getLocX() == origin && getLocY() == origin);
	}

	public byte[] send() {
		return oio.toByteArray(getMap());
	}

	public void receive(byte[] data) {
		WorldMap otherKnowledge = oio.fromByteArray(data);
		if (getLocX() == origin && getLocY() == origin)
			System.out.println(this.antnum + " MERGING with: "
					+ otherKnowledge.antnum);
		getMap().merge(otherKnowledge);

	}

	public static void induceSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Direction randomDir(Surroundings surroundings) {
		boolean[] choices = new boolean[4];
		int numChoices = 0;
		Direction oppDir = WorldMap.oppositeDir(lastDir);
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
			return lastDir = WorldMap.oppositeDir(lastDir);
		}
		// change to a for loop at some point
		int rInt = rand.nextInt(4);
		while (!choices[rInt])
			rInt = rand.nextInt(4);
		return lastDir = Direction.values()[rInt];

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

	public Cell getCell(int row, int col) {
		return getMap().get(row, col);
	}

	int getLocX() {
		return locX;
	}

	int getLocY() {
		return locY;
	}

	public WorldMap getMap() {
		return map;
	}

	public ArrayList<Cell> getCurrRoute() {
		return this.currRoute;
	}

}
