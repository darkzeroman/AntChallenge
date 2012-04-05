package vohra;

import java.awt.Point;
import java.util.Arrays;
import java.util.Stack;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Stack<Integer> s = new Stack<Integer>();
		for (int i = 0; i < 10; i++)
			s.push(i);
		System.out.println(s.firstElement());
	}
}
