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

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HookSemaphores
{
	Class<? extends SemaphoreHook> hook();

	String instance() default "INSTANCE";

	@InvocationConstraint(packages = "org.hawkinssoftware.rns.agent.*")
	public static class Relay
	{
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
