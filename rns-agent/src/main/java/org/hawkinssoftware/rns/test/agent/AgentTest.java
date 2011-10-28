package org.hawkinssoftware.rns.test.agent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hs.rns.core.lock.HookSemaphores;
import org.hs.rns.core.lock.SemaphoreHook;
import org.hs.rns.core.role.DomainRole;
import org.hs.rns.core.validation.ValidateWrite;

import com.google.common.collect.HashMultimap;

@DomainRole.Join
@HookSemaphores(hook = AgentTest.AgentLockHook.class)
public class AgentTest extends AbstractAgentTest
{
	@ValidateWrite(validatorType = AgentTest.class, method = "nothing")
	public String first;
	public String second = "second at first";

	public static void farfus(String a)
	{
		a.toString();
	}

	public static void farfusEmpty(String a)
	{
	}

	public static void farfusBlank()
	{
		"".toString();
	}

	public AgentTest()
	{
		"AgentTest".toString();
		
		HashMultimap.create();

		farfusEmpty("");

		try
		{
			farfusBlank();

			mapTest();
			
			synchronized (second)
			{
				farfus("");
				lockedMethod();
			}
			
			nothing("abc");
		}
		catch (Exception e)
		{
			System.out.println("e: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private void nothing(String parameter)
	{
		parameter.toString();
	}

	public void mapTest()
	{
		HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
		Map<Object, Object> mapInterface = hashMap;

		hashMap.get("");
		mapInterface.get("");
	}

	public void doSomething()
	{
		first = "arf";
		second = first;
		zeroth = second;

		Iterator<String> iterator = new Thing();
		iterator.next();

		try
		{
			while (true)
			{
				synchronized (first)
				{
					synchronized (second)
					{
						try
						{
							"in synchronized (first)".toString();
						}
						catch (Exception e)
						{
							continue;
						}
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		AgentTestSecondaryClass.thingy = 3;
	}

	private String getFirst()
	{
		return first;
	}

	@HookSemaphores(hook = AgentTest.AgentLockHook.class)
	private class Thing implements Iterator<String>
	{
		@Override
		public synchronized boolean hasNext()
		{
			return false;
		}

		@Override
		public synchronized String next()
		{
			System.out.println("next()");
			return null;
		}

		@Override
		public synchronized void remove()
		{
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName();
		}
	}

	@SuppressWarnings("unused")
	private synchronized String lockedMethod()
	{
		System.out.println("locked method");
		if (true)
		{
			throw new IllegalStateException("What is truth?");
		}
		return "locked method";
	}

	public static void nothing(Object writer, Object fieldOwner, String something)
	{
		System.out.println("validate write of " + fieldOwner.getClass().getSimpleName() + "." + something + " @" + fieldOwner);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	private class Shape
	{
	}

	private class Polygon extends Shape
	{
	}

	private class Triangle extends Polygon
	{
	}

	public static class AbstractAgentLockHook
	{
		public void intyThing(int i)
		{
		}

		public void doubleIntyThing(int i, int j)
		{
		}

		public void singlePolygon(Shape s)
		{
		}
	}

	public static class AgentLockHook extends AbstractAgentLockHook implements SemaphoreHook
	{
		public static final AgentLockHook INSTANCE = new AgentLockHook();

		@Override
		public void attemptingAcquisition(Object semaphore)
		{
			System.out.println("attemptingAcquisition: " + semaphore);
		}

		@Override
		public void semaphoreAcquired(Object semaphore)
		{
			System.out.println("semaphoreAcquired: " + semaphore);
		}

		@Override
		public void semaphoreReleased(Object semaphore)
		{
			System.out.println("semaphoreReleased: " + semaphore);
		}

		public void singlePolygon(Polygon p)
		{
		}

		public void singleTriangle(Triangle t)
		{
		}

		public void polygonTriangle(Polygon p, Triangle t)
		{
		}

		public void biteyThing(byte b)
		{
		}

		public void intyThing(int i)
		{
		}
	}
}
