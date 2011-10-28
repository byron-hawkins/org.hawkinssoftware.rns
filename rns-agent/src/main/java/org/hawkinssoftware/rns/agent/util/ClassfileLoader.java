package org.hawkinssoftware.rns.agent.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

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
