package org.hawkinssoftware.rns.core.test;

import org.hawkinssoftware.rns.core.aop.ObservableClassLoader;

public class ClassLoaderTest
{
	public static void main(String[] args)
	{
		try
		{
			ObservableClassLoader.launchApplication("org.hawkinssoftware.rns.core.test.LogTestMain");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
