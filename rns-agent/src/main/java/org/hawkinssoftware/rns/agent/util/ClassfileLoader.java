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
package org.hawkinssoftware.rns.agent.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class ClassfileLoader
{
	private static final ClassfileLoader INSTANCE = new ClassfileLoader();
	
	public static ClassfileLoader getInstance()
	{
		return INSTANCE;
	}
	
	private final Map<String, JavaClass> classfileCache = new HashMap<String, JavaClass>();

	public JavaClass loadClass(String filename) throws ClassFormatException, IOException
	{
		JavaClass loaded = classfileCache.get(filename);
		if (loaded == null)
		{
			loaded = loadClassFromResource(filename);
			classfileCache.put(filename, loaded);
		}
		return loaded;
	}

	private JavaClass loadClassFromResource(String filename) throws ClassFormatException, IOException
	{
		InputStream classfileInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
		ClassParser parser = new ClassParser(classfileInput, filename);
		return parser.parse();
	}
}
