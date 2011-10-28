package org.hawkinssoftware.rns.agent.aop;

import java.io.IOException;
import java.util.List;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionFactory;
import org.hawkinssoftware.rns.agent.InstrumentationClassFile;

/**
 * @author b
 * 
 */
public class InitializationClassTransformer
{
	final InstrumentationClassFile classfile;
	final List<String> initializationAgentClassnames;
	final InstructionFactory factory;

	public InitializationClassTransformer(InstrumentationClassFile classfile) throws ClassFormatException, IOException, ClassNotFoundException
	{
		this.classfile = classfile;
		initializationAgentClassnames = classfile.metadata.getInitializationAgents();
		factory = new InstructionFactory(classfile.constants);
	}

	public void instrumentMethods()
	{
		if (initializationAgentClassnames.isEmpty())
		{
			return;
		}

		Method[] methods = classfile.parsedType.getMethods();
		for (int methodIndex = 0; methodIndex < methods.length; methodIndex++)
		{
			Method method = methods[methodIndex];
			if (!method.getName().contains("<init>"))
			{
				continue;
			}

			try
			{
				Method newMethod = new InitializationMethodTransformer(this, method).instrumentInitializationAspects();
				classfile.setChanged();
				methods[methodIndex] = newMethod;
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
}
