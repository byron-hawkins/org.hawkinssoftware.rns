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
package org.hawkinssoftware.rns.agent.validation;

import java.io.IOException;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.hawkinssoftware.rns.agent.InstrumentationClassFile;
import org.hawkinssoftware.rns.agent.InstrumentationMethodFactory;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class ValidationClassTransformer
{
	final InstrumentationClassFile classfile;
	final InstrumentationMethodFactory methodFactory;

	public ValidationClassTransformer(InstrumentationClassFile classfile, InstrumentationMethodFactory methodFactory)
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
		if (classfile.metadata.isExcluded)
		{
			return; 
		}

		Method[] methods = classfile.parsedType.getMethods();
		"".toString();
		for (int methodIndex = 0; methodIndex < methods.length; methodIndex++)
		{
			Method method = methods[methodIndex];
			if (method.getCode() == null)
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
						newMethod = new ValidationMethodTransformer(this, method).instrumentMethod();
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
