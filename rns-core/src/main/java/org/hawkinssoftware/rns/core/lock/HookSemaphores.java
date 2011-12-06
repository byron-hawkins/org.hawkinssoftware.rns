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
package org.hawkinssoftware.rns.core.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hawkinssoftware.rns.core.publication.InvocationConstraint;

/**
 * The annotated class will be instrumented with a pointcut before and after every <code>synchronized</code> statement
 * and <code>synchronized</code> method entry/exit. The pointcut will invoke the type of <code>SemaphoreHook</code>
 * specified in the annotation entry <code>hook()</code>, and will find its singleton instance by the entry
 * <code>instance()</code>, which may be the name of either a static method or a static field.
 * 
 * @author Byron Hawkins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HookSemaphores
{
	/**
	 * Identifies the type of <code>SemaphoreHook</code> to receive the pointcut.
	 */
	Class<? extends SemaphoreHook> hook();

	/**
	 * Identifies the static field or method of the <code>SemaphoreHook</code> to invoke in the pointcut.
	 */
	String instance() default "INSTANCE";

	/**
	 * The instrumentation of the enclosing <code>HookSempahores</code> annotation actually goes to this
	 * <code>Relay</code>, which finds the <code>SemaphoreHook</code> instance to invoke. The <code>Relay</code> also
	 * maintains a stack to simplify entry and exit of <code>synchronized</code> methods.
	 * 
	 * @author Byron Hawkins
	 */
	@InvocationConstraint(packages = "org.hawkinssoftware.rns.agent.*")
	public static class Relay
	{
		/**
		 * Maintains a map of method entry frames per thread, and also caches the <code>SemaphoreHook</code> instance.
		 * 
		 * @author Byron Hawkins
		 */
		private static class ThreadContext
		{
			final List<SynchronizedMethodEntry> methodEntryStack = new ArrayList<SynchronizedMethodEntry>();
			SemaphoreHook hook = null;

			SemaphoreHook getHook(Class<?> hookedType) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException
			{
				if (hook == null)
				{
					HookSemaphores annotation = hookedType.getAnnotation(HookSemaphores.class);

					if (annotation.instance().endsWith("()"))
					{
						Method staticGetter = annotation.hook().getMethod(annotation.instance().substring(0, annotation.instance().length() - 2));
						staticGetter.setAccessible(true);
						hook = (SemaphoreHook) staticGetter.invoke(null);
					}
					else
					{
						Field instanceField = annotation.hook().getField(annotation.instance());
						instanceField.setAccessible(true);
						hook = (SemaphoreHook) instanceField.get(null);
					}
				}
				return hook;
			}
		}

		/**
		 * Method entry frame used in the enclosing <code>Relay</code>'s <code>ThreadContext.methodEntryStack</code>.
		 * 
		 * @author Byron Hawkins
		 */
		private static class SynchronizedMethodEntry
		{
			final Class<?> hookedType;
			final Object semaphore;

			public SynchronizedMethodEntry(Class<?> hookedType, Object semaphore)
			{
				this.hookedType = hookedType;
				this.semaphore = semaphore;
			}
		}

		private static final ThreadLocal<ThreadContext> CONTEXT = new ThreadLocal<ThreadContext>() {
			protected ThreadContext initialValue()
			{
				return new ThreadContext();
			}
		};

		private static final boolean enabled = System.getProperty("disable-access-validation") == null;

		public static void resetHook()
		{
			CONTEXT.get().hook = null;
		}

		public static void methodEntry(Class<?> hookedType, Object semaphore)
				throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException
		{
			if (!enabled)
			{
				return;
			}

			CONTEXT.get().methodEntryStack.add(new SynchronizedMethodEntry(hookedType, semaphore));

			attemptingAcquisition(hookedType, semaphore);
			semaphoreAcquired(hookedType, semaphore);
		}

		public static void methodExit() throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException
		{
			if (!enabled)
			{
				return;
			}

			ThreadContext context = CONTEXT.get();
			SynchronizedMethodEntry pop = context.methodEntryStack.remove(context.methodEntryStack.size() - 1);

			semaphoreReleased(pop.hookedType, pop.semaphore);
		}

		public static void attemptingAcquisition(Class<?> hookedType, Object semaphore)
				throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException
		{
			if (!enabled)
			{
				return;
			}

			CONTEXT.get().getHook(hookedType).attemptingAcquisition(semaphore);
		}

		public static void semaphoreAcquired(Class<?> hookedType, Object semaphore)
				throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException
		{
			if (!enabled)
			{
				return;
			}

			CONTEXT.get().getHook(hookedType).semaphoreAcquired(semaphore);
		}

		public static void semaphoreReleased(Class<?> hookedType, Object semaphore)
				throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException
		{
			if (!enabled)
			{
				return;
			}

			CONTEXT.get().getHook(hookedType).semaphoreReleased(semaphore);
		}
	}
}
