package org.hawkinssoftware.rns.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RNSUtils
{
	public static final String RNS_RESOURCE_FOLDER_NAME = "rns";
	
	private static final Pattern PLAIN_NAME_START_PATTERN = Pattern.compile("\\.[A-Z]");

	public static String getPlainName(Class<?> type)
	{
		String typeName = type.getCanonicalName();
		if (typeName == null)
		{
			typeName = type.getName().replace('$', '.');
		}
		return getPlainName(typeName);
	}

	public static String getPlainName(String qualifiedName)
	{
		Matcher matcher = PLAIN_NAME_START_PATTERN.matcher(qualifiedName);
		if (matcher.find())
		{
			return qualifiedName.substring(matcher.start() + 1, qualifiedName.length());
		}
		else
		{
			return qualifiedName;
		}
	}

	public static String makeCanonical(String classname)
	{
		Matcher matcher = PLAIN_NAME_START_PATTERN.matcher(classname);
		if (matcher.find())
		{
			return classname.substring(0, matcher.start() + 1) + classname.substring(matcher.start() + 1, classname.length()).replace('.', '$');
		}
		else
		{
			throw new IllegalArgumentException("Unable to identify the canonical name of " + classname);
		}
	}
}
