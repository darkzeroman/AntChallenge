package vohra;

import java.util.Stack;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new test();
		Stack<Integer> s1 = new Stack<Integer>();
		Stack<Integer> s2 = new Stack<Integer>();

		s1.push(1);
		s1.push(2);
		s2.push(3);
		s2.push(4);

		s1 = s2;
		s2 = new Stack<Integer>();
		System.out.println(s1.size());
		System.out.println(s2.size());


	}

}
