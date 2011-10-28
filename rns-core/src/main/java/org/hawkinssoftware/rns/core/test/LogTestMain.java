package org.hawkinssoftware.rns.core.test;

import org.hawkinssoftware.rns.core.aop.ObservableClassLoader;

public class LogTestMain
{
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
