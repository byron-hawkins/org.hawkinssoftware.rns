package org.hawkinssoftware.rns.test.agent;

import java.io.IOException;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionFactory;
import org.hawkinssoftware.rns.agent.InstrumentationClassFile;

/**
 * @author b
 * 
 */
public class TestClassTransformer
{
	final InstrumentationClassFile classfile;
	final InstructionFactory factory;

	public TestClassTransformer(InstrumentationClassFile classfile) throws ClassFormatException, IOException, ClassNotFoundException
	{
		this.classfile = classfile;
		factory = new InstructionFactory(classfile.constants);
	}

	public void instrumentMethods()
	{
		Method[] methods = classfile.parsedType.getMethods();
		for (int methodIndex = 0; methodIndex < methods.length; methodIndex++)
		{
			Method method = methods[methodIndex];

			try
			{
				new TestMethodTransformer(this, method).examineFields();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
}
