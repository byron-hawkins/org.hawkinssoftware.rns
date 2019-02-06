/*
 * Copyright (c) 2011 HawkinsSoftware
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Byron Hawkins of HawkinsSoftware
 */
package org.hawkinssoftware.rns.core.test;

/**
 * A random manual test of the RNS type restrictions. Also used 
 * for verifying the exact 
 * 
 * @author Byron Hawkins
 */
public class AdHocTest
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private class A
	{
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
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
