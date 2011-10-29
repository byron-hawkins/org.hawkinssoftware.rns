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
package org.hawkinssoftware.rns.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.hawkinssoftware.rns.agent.aop.InitializationClassTransformer;
import org.hawkinssoftware.rns.agent.lock.SemaphoreClassTransformer;
import org.hawkinssoftware.rns.agent.message.MessageStackClassTransformer;
import org.hawkinssoftware.rns.agent.validation.ValidationClassTransformer;
import org.hawkinssoftware.rns.core.aop.ClassLoadObserver;
import org.hawkinssoftware.rns.core.aop.ClassLoadObserver.FilteredObserver;
import org.hawkinssoftware.rns.test.agent.TestClassTransformer;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class RNSInstrumentationAgent implements ClassFileTransformer
{
	private static final String TEST_AGENT = "test-agent";

	public byte[] transform(ClassLoader loader, String classname, Class<?> type, ProtectionDomain protectionDomain, byte[] bytes)
			throws IllegalClassFormatException
	{
		/*
		 * Not messing with java.* or sun instrumented classes
		 */
		// TODO: hack skipping InstrumentedRouter in RNS agent
		if (classname.startsWith("java") || classname.startsWith("org/xml/sax") || classname.contains("InstrumentedRouter"))
		{
			return null;
		}

		try
		{
			InstrumentationClassFile classfile = new InstrumentationClassFile(classname, bytes);
			InstrumentationMethodFactory methodFactory = new InstrumentationMethodFactory(classfile.constants);

			for (FilteredObserver observer : ClassLoadObserver.getObservers())
			{
				classfile.observe(observer);
			}

			if (System.getProperty(TEST_AGENT) == null)
			{
				new MessageStackClassTransformer(classfile, methodFactory).instrumentMethods();
				new ValidationClassTransformer(classfile, methodFactory).instrumentMethods();
				new InitializationClassTransformer(classfile).instrumentMethods();
				new SemaphoreClassTransformer(classfile, methodFactory).instrumentMethods();
			}
			else
			{
				new TestClassTransformer(classfile).instrumentMethods();
			}

			if (classfile.hasChanges())
			{
				return classfile.compile();
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}

		return null;
	}

	public static void premain(String args, Instrumentation instrumentation)
	{
		instrumentation.addTransformer(new RNSInstrumentationAgent());
	}
}
