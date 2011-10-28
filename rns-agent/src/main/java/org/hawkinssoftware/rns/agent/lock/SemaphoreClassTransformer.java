package org.hawkinssoftware.rns.agent.lock;

import java.io.IOException;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.hawkinssoftware.rns.agent.InstrumentationClassFile;
import org.hawkinssoftware.rns.agent.InstrumentationMethodFactory;

/**
 * @author b
 * 
 */
public class SemaphoreClassTransformer
{
	final InstrumentationClassFile classfile;
	final InstrumentationMethodFactory methodFactory;

	public SemaphoreClassTransformer(InstrumentationClassFile classfile, InstrumentationMethodFactory methodFactory)
			throws ClassFormatException, IOException, ClassNotFoundException
	{
		this.classfile = classfile;
		this.methodFactory = methodFactory;
	}

	/**
	 * Instrument all the methods in parsedType according to the rules of this.instrumentMethod(), skipping classes in
	 * the RNS core and agent, and also skipping sun.*, com.sun.* and $* classes because things crash when I instrument
	 * the current caller invocations in them
	 */
	public void instrumentMethods()
	{
		if (classfile.metadata.isExcluded || (classfile.metadata.hookSemaphoresAnnotation == null))
		{
			return;
		}

		Method[] methods = classfile.parsedType.getMethods();

		"".toString();

		for (int methodIndex = 0; methodIndex < methods.length; methodIndex++)
		{
			Method method = methods[methodIndex];
			if ((method.getCode() == null) || method.isSynthetic())
			{
				continue;
			}

			try
			{
				Method newMethod = null;
				if (classfile.metadata.canInstrument(method))
				{
					if (!classfile.metadata.isInMetaDomain(method))
					{
						newMethod = new SemaphoreMethodTransformer(this, method).instrumentMethod();
					}
				}
				if (newMethod != null)
				{
					classfile.setChanged();
					methods[methodIndex] = newMethod;
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
}
