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

		for (int i = 0; i < s1.size(); i++)
			s2.push(s1.get(i));
		System.out.println(s2.pop());
		System.out.println(s2.pop());

	}

}
