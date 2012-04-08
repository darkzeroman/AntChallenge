package vohra;

import java.awt.Point;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Stack;

import vohra.searches.BFS;
import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;
import ants.Tile;

public class MyAnt implements Ant {

	public static int order = 0;
	public static final int DEBUGLEVEL = 1;

	private int scoutModeTurns = 20;
	private final int scoutSearchsLimit = 35;
	public final Knowledge knowledge;
	public final ObjectIO<Hashtable<Point, Cell>> ObjectIO = new ObjectIO<Hashtable<Point, Cell>>();
	private Surroundings surroundings;
	boolean firstAction = true;

	public MyAnt() {
		// TODO remove
		knowledge = new Knowledge(order++);
	}

	public Action getAction(Surroundings surroundings) {
		this.surroundings = surroundings;
		knowledge.round++;
		knowledge.updateMap(surroundings);

		debugPrint(1, "");
		debugPrint(1, this.toString());
		debugPrint(1, knowledge.toString());
		debugPrint(1, knowledge.getCurrCell().toString());

		int currCellFood = knowledge.getCurrCell().getAmountOfFood();
		int currCellNumAnts = knowledge.getCurrCell().getNumAnts();

		if (isAtHome() && currCellFood == 0 && !knowledge.isScout
				&& currCellNumAnts == 3) {
			knowledge.isScout = true;
			knowledge.mode = Knowledge.MODE.SCOUT;
			debugPrint(1, "Initial Scout Mode");
			return Action.HALT;
		}

		// Special Actions here

		;// return Action.HALT;

		debugPrint(1, "Starting in Mode: " + knowledge.mode);
		// Determine next action by the use of the finite state machines
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
		default:
			throw new RuntimeException("Undefined state");
		}
		// waitForReturn();
		firstAction = false;
		// updating local knowledge
		if (isActionValid(action)) {
			if (action.getDirection() != null)
				// directional actions need to update location
				knowledge.updateCurrLoc(action.getDirection());
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

		// If still in scout mode:
		if (scoutModeTurns > 0) {
			// If plan exists, continue with it
			if ((action = nextPlanAction()) != null)
				return action;
			// If not, make one
			else if (canFindUnexplored())
				return nextPlanAction();
		}
		// Not in scout mode anymore, transition to Food mode
		return changeMode(Knowledge.MODE.TOFOOD);
	}

	private Action modeToFood() {
		int currCellFood = knowledge.getCurrCell().getAmountOfFood();
		Action action;

		// if food on target doesn't doesn't exist anymore, re-plan
		if (knowledge.updated && !getCurrPlan().isEmpty()
				&& getCurrPlan().firstElement().getAmountOfFood() == 0) {
			getCurrPlan().clear();
			debugPrint(1, "Target doesn't have food, need to replan");
		}

		// Continue the plan if it exists
		if ((action = nextPlanAction()) != null) {
			return action;

		} else if (!isAtHome() && currCellFood > 0 && !knowledge.carryingFood) {
			// ant is on food tile, gather
			knowledge.carryingFood = true;
			knowledge.getCurrCell().decrementFood();
			debugPrint(1, "GATHERING");

			return changeModeAndAction(Knowledge.MODE.TOHOME, Action.GATHER);

		} else if ((canFindFood())) {
			// don't have a plan, so make one
			return nextPlanAction();
		}
		if (isAtHome() && currCellFood > 100) {
			// Stay at home, some ant should come with a plan
			return Action.HALT;
		}

		debugPrint(1, "Can't find food, going to explore");
		return changeMode(Knowledge.MODE.EXPLORE);
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
		int currCellFood = knowledge.getCurrCell().getAmountOfFood();

		// Knowledge was recently updated, see if food source exists nearby
		if (knowledge.updated && currCellFood == 0) {
			knowledge.updated = false;
			return changeMode(Knowledge.MODE.TOFOOD);
		}
		if (knowledge.surroundingsUpdate) {
			knowledge.surroundingsUpdate = false;
			for (int i = 0; i < 4; i++) {
				Tile tile = surroundings.getTile(Direction.values()[i]);
				if (tile.getAmountOfFood() > 0) {
					return changeMode(Knowledge.MODE.TOFOOD);
				}
			}
		}
		// Keep following plan
		if ((action = nextPlanAction()) != null)
			return action;

		// Try to find closest unexplored
		if (canFindUnexplored())
			return nextPlanAction();

		// Everything is explored, Go home
		return changeMode(Knowledge.MODE.TOHOME);
	}

	private Action modeToHome() {
		Action action;
		// drop off food if if at home
		if (isAtHome() && knowledge.carryingFood) {
			knowledge.carryingFood = false;
			knowledge.mode = Knowledge.MODE.TOFOOD;
			knowledge.totalPlan.clear();

			// For resetting the scout mode limit
			if (knowledge.isScout && knowledge.getAmountFoodFound() < 700) {
				// knowledge.getAmountFoodFound() < 650
				debugPrint(1, "Resetting Countdown");
				scoutModeTurns = scoutSearchsLimit;
				knowledge.mode = Knowledge.MODE.SCOUT;
			}
			return changeModeAndAction(knowledge.mode, Action.DROP_OFF);
		}

		// Continue with plan
		if ((action = nextPlanAction()) != null)
			return action;
		// Make plan to home
		else if (canFindHome()) {
			// TODO test
			printPath(knowledge.totalPlan);
			printPath(knowledge.getCurrPlan());
			if (knowledge.totalPlan.size() == knowledge.getCurrPlan().size()) {
				knowledge.getCurrPlan().clear();

				for (int i = 0; i < knowledge.totalPlan.size(); i++)
					knowledge.getCurrPlan().push(knowledge.totalPlan.get(i));
			}
			printPath(knowledge.getCurrPlan());

			knowledge.totalPlan.clear();
			//waitForReturn();
			return nextPlanAction();
		}
		throw new RuntimeException("Can't find home, map might be corrupted");
	}

	private Action changeMode(Knowledge.MODE mode) {
		debugPrint(1, "Changing from: " + knowledge.mode + " to: " + mode);

		// Clear current plan when changing modes
		getCurrPlan().clear();
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
		throw new IllegalArgumentException("UnrecognizedMode: " + mode);

	}

	private Action changeModeAndAction(Knowledge.MODE mode, Action action) {
		debugPrint(1, "Changing to Mode: " + mode + " and Action: "
				+ actionToString(action));
		// Set a new mode and action
		getCurrPlan().clear();
		knowledge.mode = mode;
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

		if (getCurrPlan().size() > 0) {
			Cell from = knowledge.getCurrCell();
			knowledge.totalPlan.push(from);

			Cell to = getCurrPlan().pop();
			Direction dir = from.dirTo(to);
			action = Action.move(dir);
			if (isActionValid(action))
				return action;
		}
		return null;
	}

	public byte[] send() {
		long start = System.currentTimeMillis();
		byte[] arr = ObjectIO.toByteArray(knowledge.getMap());
		debugPrint(1, "To Serialize: " + knowledge.numKnownCells() + " "
				+ (System.currentTimeMillis() - start));
		return arr;
	}

	public void receive(byte[] data) {
		Hashtable<Point, Cell> otherKnowledge = ObjectIO.fromByteArray(data);
		// debugPrint(1, " Merging on: " + otherKnowledge.antnum);
		knowledge.merge(otherKnowledge);
	}

	private boolean isAtHome() {
		return (knowledge.x == 0 && knowledge.y == 0);
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

	public boolean makePlan(Cell.TYPE type, Planner planner) {
		return false;
	}

	public boolean canFindFood() {
		return MapOps.makePlan(knowledge, Cell.TYPE.FOOD, new BFS());
	}

	public boolean canFindUnexplored() {
		return MapOps.makePlan(knowledge, Cell.TYPE.UNEXPLORED, new BFS());
	}

	public boolean canFindHome() {
		return MapOps.makePlan(knowledge, Cell.TYPE.HOME, new BFS());
	}

	public Cell getCell(int row, int col) {
		return knowledge.getCell(row, col);
	}

	public Stack<Cell> getCurrPlan() {
		return knowledge.getCurrPlan();
	}

	public String toString() {
		return "Ant Num: " + knowledge.antnum + " at: [" + knowledge.x + ", "
				+ knowledge.y + "] ";
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

	int count = 1;

	private void waitForReturn() {
		if (count == 0) {
			Scanner sc = new Scanner(System.in);
			while (!sc.nextLine().equals(""))
				;
			count = 2;
		}
		count--;
	}

}
