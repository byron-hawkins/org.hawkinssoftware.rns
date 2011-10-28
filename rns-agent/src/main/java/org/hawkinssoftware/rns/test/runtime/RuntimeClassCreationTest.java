package org.hawkinssoftware.rns.test.runtime;

import java.io.IOException;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;
import org.hawkinssoftware.rns.agent.util.ClassfileLoader;

public class RuntimeClassCreationTest
{
	private void start() throws ClassFormatException, IOException
	{
		JavaClass parsedType = ClassfileLoader.getInstance().loadClass(TestNotificationRouterExample.class.getName().replace('.', '/') + ".class");
		BCELifier coder = new BCELifier(parsedType, System.out);
		coder.start();
	}

	public static void main(String[] args)
	{
		try
		{
			RuntimeClassCreationTest test = new RuntimeClassCreationTest();
			test.start();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
