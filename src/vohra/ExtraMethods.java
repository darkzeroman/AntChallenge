/**
 * 
 */
package vohra;

import java.util.Scanner;
import java.util.Stack;

import ants.Action;
// Used for debugging purposes
public class ExtraMethods {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	static String actionToString(Action action) {
		if (action == Action.DROP_OFF)
			return "Drop off";
		else if (action == Action.GATHER)
			return "Gather";
		else if (action == Action.HALT)
			return "Halt";
		throw new IllegalArgumentException("UnrecognizedAction: " + action);
	}

	static void printPath(Stack<Cell> currRoute) {
		ExtraMethods.debugPrint(1, "Printing Path:  (size: " + currRoute.size()
				+ "): ");
		for (int i = 0; i < currRoute.size(); i++) {
			ExtraMethods.debugPrint(1, currRoute.get(i).toString());
		}
		ExtraMethods.debugPrint(1, "");
	}

	public static void debugPrint(int num, String message) {
		if (VohraAntWithDebugStatements.DEBUGLEVEL == 0)
			return;
		if (num >= VohraAntWithDebugStatements.DEBUGLEVEL) {
			System.out.println(num + ": " + message);
			if (num == 2)
				try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}

	public static void waitForReturn() {
		Scanner sc = new Scanner(System.in);
		while (!sc.nextLine().equals(""))
			;
	}

}
