package vohra;

import java.util.Scanner;
import java.util.Stack;

import vohra.searches.BFS;

import ants.Action;
import ants.Ant;
import ants.Direction;
import ants.Surroundings;

public class MyAnt implements Ant {

	public static int order = 0;
	public static final int DEBUG = 1;

	public static void main(String[] args) {
		new MyAnt();
		System.out.println("compiled");
	}

	private int scoutSearchLimit = 20;
	public final Knowledge knowledge;
	public final ObjectIO<Knowledge> oio = new ObjectIO<Knowledge>();
	private Surroundings surroundings;

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
		if (knowledge.antnum == 4)
			;// return Action.HALT;

		debugPrint(1, "Starting in Mode: " + knowledge.getMode());

		Action action = null;

		switch (knowledge.getMode()) {
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
			if (action.getDirection() != null)
				// directional actions need to update location
				knowledge.updateCurrLoc(action.getDirection());
			else
				// Action is a non-Direction move
				debugPrint(1, "Action: " + actionToString(action));
			return action;
		}

		debugPrint(2, "Action was not valid");
		return Action.HALT;

	}

	private Action modeScout() {
		Action action;
		scoutSearchLimit--;
		// if still in scout mode: if following route, keep going. Or make one
		if (scoutSearchLimit > 0) {
			if ((action = nextPlanAction()) != null)
				return action;
			else if (canFindUnexplored())
				return nextPlanAction();
		}
		// not in scout mode anymore, switch to food mode;
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

		// if ant already has a target, keep going
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

		debugPrint(1, "Can't find food, going to explore");
		return changeMode(Knowledge.MODE.EXPLORE);
	}

	private Action modeExplore() {
		Action action;
		int currCellFood = knowledge.getCurrCell().getAmountOfFood();
		// map was recently updated, see if food source exists nearby
		if (knowledge.updated && currCellFood == 0) {
			knowledge.updated = false;
			return changeMode(Knowledge.MODE.TOFOOD);
		}

		// keep following route
		if ((action = nextPlanAction()) != null)
			return action;

		// try to find closest unexplored
		debugPrint(1, "looking for unexplored");
		if (canFindUnexplored())
			return nextPlanAction();

		// if everything is explored, go home
		return changeMode(Knowledge.MODE.TOHOME);
	}

	private Action modeToHome() {
		Action action;
		// drop off food if if at home
		if (isAtHome()) {
			knowledge.carryingFood = false;
			knowledge.mode = Knowledge.MODE.TOFOOD;

			if (knowledge.isScout && knowledge.getTotalFoodFound() < 650) {
				debugPrint(1, "Resetting Countdown");
				scoutSearchLimit = 25;
				knowledge.mode = Knowledge.MODE.SCOUT;
			}
			return changeModeAndAction(knowledge.getMode(), Action.DROP_OFF);
		}

		// continue with route
		if ((action = nextPlanAction()) != null)
			return action;
		// find route to home
		else if (canFindHome()) {
			return nextPlanAction();
		} else
			// throw error here
			debugPrint(2, "No route && can't find home");
		debugPrint(1, "End Home");
		return null;
	}

	private Action changeMode(Knowledge.MODE mode) {
		debugPrint(1, "Changing from: " + knowledge.getMode() + " to: " + mode);

		// when changing modes, clear the plan
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
		debugPrint(2, "Change mode shouldn't be null");
		return null;

	}

	private Action changeModeAndAction(Knowledge.MODE mode, Action action) {
		debugPrint(1, "Changing to Mode: " + mode + " and Action: "
				+ actionToString(action));
		getCurrPlan().clear();
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
			actionString = "Halt";
		return actionString;
	}

	private Action nextPlanAction() {
		Action action;

		if (getCurrPlan().size() > 0) {
			Cell from = knowledge.getCurrCell();
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
		byte[] arr = oio.toByteArray(knowledge);
		debugPrint(1, "To Serialize: " + knowledge.numKnownCells() + " "
				+ (System.currentTimeMillis() - start));
		return arr;
	}

	public void receive(byte[] data) {
		Knowledge otherKnowledge = oio.fromByteArray(data);
		debugPrint(1, " Merging on: " + otherKnowledge);
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
		return knowledge.get(row, col);
	}

	public Stack<Cell> getCurrPlan() {
		return knowledge.getCurrPlan();
	}

	public String toString() {
		return "Ant Num: " + knowledge.antnum + " at: [" + knowledge.x + ", "
				+ knowledge.y + "] ";
	}

	public static void debugPrint(int num, String message) {
		if (num >= DEBUG) {
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
