package org.hawkinssoftware.rns.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.hawkinssoftware.rns.agent.aop.InitializationClassTransformer;
import org.hawkinssoftware.rns.agent.lock.SemaphoreClassTransformer;
import org.hawkinssoftware.rns.agent.message.MessageStackClassTransformer;
import org.hawkinssoftware.rns.agent.validation.ValidationClassTransformer;
import org.hawkinssoftware.rns.test.agent.TestClassTransformer;
import org.hs.rns.core.aop.ClassLoadObserver;
import org.hs.rns.core.aop.ClassLoadObserver.FilteredObserver;

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
