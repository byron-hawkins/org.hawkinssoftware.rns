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
