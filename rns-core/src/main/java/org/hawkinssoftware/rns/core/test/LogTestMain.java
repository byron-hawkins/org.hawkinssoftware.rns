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

import org.hawkinssoftware.rns.core.aop.ObservableClassLoader;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class LogTestMain
{
	
	/**
	 * The listener interface for receiving classLoad events. The class that is interested in processing a classLoad
	 * event implements this interface, and the object created with that class is registered with a component using the
	 * component's <code>addClassLoadListener<code> method. When
	 * the classLoad event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see ClassLoadEvent
	 */
	private static class ClassLoadListener implements ObservableClassLoader.Listener
	{
		@Override
		public void classLoaded(Class<?> type)
		{
			System.out.println("loading class: " + type);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			ObservableClassLoader.addListener(new ClassLoadListener());

			LogTest test = new LogTest();
			test.start();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
