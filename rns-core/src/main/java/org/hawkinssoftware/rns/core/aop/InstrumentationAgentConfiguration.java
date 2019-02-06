package org.hawkinssoftware.rns.core.aop;

import java.util.ArrayList;
import java.util.List;

public class InstrumentationAgentConfiguration
{
	private static List<String> omittedPackagePaths = new ArrayList<String>();
	
	public static void addOmittedPackagePath(String omittedPackagePath)
	{
		omittedPackagePaths.add(omittedPackagePath);
	}
	
	public static boolean isInstrumented(String classname)
	{
		for (String ommittedPackagePath : omittedPackagePaths)
		{
			if (classname.startsWith(ommittedPackagePath))
			{
				return false;
			}
		}
		return true;
	}
}
