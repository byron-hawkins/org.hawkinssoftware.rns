package org.hawkinssoftware.rns.core.test;

public class AdHocTest
{
	private class A
	{
	}

	private class B extends A
	{
	}

	private void something(A a)
	{
		System.out.println("a");
	}

	private void something(B b)
	{
		System.out.println("b");
	}

	private void start()
	{
		A a = new B();
//		B b = new B();
		B b = null;

		something(a);
		something(b);
	}

	public static void main(String[] args)
	{
		new AdHocTest().start();
	}
}
