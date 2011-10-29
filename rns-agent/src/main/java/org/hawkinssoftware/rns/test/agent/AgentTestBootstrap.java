package org.hawkinssoftware.rns.test.agent;

import org.hawkinssoftware.rns.core.aop.ClassLoadObserver;
import org.hawkinssoftware.rns.core.aop.ClassLoadObserver.MethodFilter;
import org.hawkinssoftware.rns.core.aop.ClassLoadObserver.ObservedType;

public class AgentTestBootstrap
{
	private static class HookMethodFilter implements MethodFilter
	{
		@Override
		public boolean acceptMethodName(String name)
		{
			return !(name.equals("<init>") || name.equals("<clinit>"));
		}

		@Override
		public int getParameterCount()
		{
			return 1;
		}

		@Override
		public String getParameterType(int index)
		{
			return Object.class.getName();
		}
	}

	private static class SinglePolygonFilter implements MethodFilter
	{
		@Override
		public boolean acceptMethodName(String name)
		{
			return !(name.equals("<init>") || name.equals("<clinit>"));
		}

		@Override
		public int getParameterCount()
		{
			return 1;
		}

		@Override
		public String getParameterType(int index)
		{
			return "org.hawkinssoftware.rns.test.agent.AgentTest$Shape";
		}
	}

	private static class SingleTriangleFilter implements MethodFilter
	{
		@Override
		public boolean acceptMethodName(String name)
		{
			return !(name.equals("<init>") || name.equals("<clinit>"));
		}

		@Override
		public int getParameterCount()
		{
			return 1;
		}

		@Override
		public String getParameterType(int index)
		{
			return "org.hawkinssoftware.rns.test.agent.AgentTest$Triangle";
		}
	}

	private static class PolygonTriangleFilter implements MethodFilter
	{
		@Override
		public boolean acceptMethodName(String name)
		{
			return !(name.equals("<init>") || name.equals("<clinit>"));
		}

		@Override
		public int getParameterCount()
		{
			return 2;
		}

		@Override
		public String getParameterType(int index)
		{
			switch (index)
			{
				case 0:
					return "org.hawkinssoftware.rns.test.agent.AgentTest$Shape";
				case 1:
					return "org.hawkinssoftware.rns.test.agent.AgentTest$Polygon";
			}
			return null;
		}
	}

	private static class SingleIntFilter implements MethodFilter
	{
		@Override
		public boolean acceptMethodName(String name)
		{
			return !(name.equals("<init>") || name.equals("<clinit>"));
		}

		@Override
		public int getParameterCount()
		{
			return 1;
		}

		@Override
		public String getParameterType(int index)
		{
			return int.class.getName();
		}
	}

	private static class DoubleIntFilter implements MethodFilter
	{
		@Override
		public boolean acceptMethodName(String name)
		{
			return !(name.equals("<init>") || name.equals("<clinit>"));
		}

		@Override
		public int getParameterCount()
		{
			return 2;
		}

		@Override
		public String getParameterType(int index)
		{
			return int.class.getName();
		}
	}

	private static class SingleByteFilter implements MethodFilter
	{
		@Override
		public boolean acceptMethodName(String name)
		{
			return !(name.equals("<init>") || name.equals("<clinit>"));
		}

		@Override
		public int getParameterCount()
		{
			return 1;
		}

		@Override
		public String getParameterType(int index)
		{
			return byte.class.getName();
		}
	}

	private static class ClassLoadListener implements ClassLoadObserver.FilteredObserver
	{
		private final String[] observedTypenames = new String[] { "org.hawkinssoftware.rns.test.agent.AgentTest$AgentLockHook" };
		private final MethodFilter[] methodFilters = new MethodFilter[] { new SinglePolygonFilter(), new SingleTriangleFilter(), new PolygonTriangleFilter(),
				new SingleIntFilter(), new DoubleIntFilter(), new SingleByteFilter() };

		@Override
		public MethodFilter[] getMethodFilters()
		{
			return methodFilters;
		}

		@Override
		public String[] getObservedTypenames()
		{
			return observedTypenames;
		}

		@Override
		public void matchingTypeObserved(ObservedType type)
		{
			System.out.println("Found type: " + type.typeHierarchy.qualifiedName);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			ClassLoadObserver.observe(new ClassLoadListener());

			AgentTest test = new AgentTest();
			test.doSomething();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
