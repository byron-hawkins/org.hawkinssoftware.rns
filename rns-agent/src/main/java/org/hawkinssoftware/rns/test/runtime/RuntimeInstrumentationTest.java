package org.hawkinssoftware.rns.test.runtime;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawkinssoftware.rns.test.runtime.RouterInstrumentation.RouterType;

public class RuntimeInstrumentationTest
{
	private class ClassCreator extends ClassLoader
	{
		Class<?> createClass(String name, byte[] classfileBytes)
		{
			return defineClass(name, classfileBytes, 0, classfileBytes.length);
		}
	}

	private class RouterDefinition implements RouterInstrumentation.Definition
	{
		private final RouterType routerType;
		private final String handlerMethodName;
		private final Class<?> parameterClass;
		private final String handlerClassname;

		RouterDefinition(RouterType routerType, String handlerMethodName, Class<?> parameterClass, String handlerClassname)
		{
			this.routerType = routerType;
			this.handlerMethodName = handlerMethodName;
			this.parameterClass = parameterClass;
			this.handlerClassname = handlerClassname;
		}

		@Override
		public RouterType getRouterType()
		{
			return routerType;
		}

		@Override
		public String getHandlerMethodName()
		{
			return handlerMethodName;
		}

		@Override
		public String getParameterClassname()
		{
			return parameterClass.getName();
		}

		@Override
		public String getHandlerClassname()
		{
			return handlerClassname;
		}

		@Override
		public String getInstrumentedRouterClassname()
		{
			return getClass().getPackage().getName() + ".InstrumentedRouter";
		}

		@Override
		public String getInstrumentedRouterSimpleName()
		{
			return "InstrumentedRouter";
		}
	}

	private class InstrumentationDefinition implements RouterInstrumentation.BaseClassnames
	{
		private static final String DIRECTIVE_ROUTER_BASE_CLASSNAME = "org.hawkinssoftware.rns.test.runtime.TestDirectiveRouter";
		private static final String NOTIFICATION_ROUTER_BASE_CLASSNAME = "org.hawkinssoftware.rns.test.runtime.TestNotificationRouter";
		private static final String HANDLER_BASE_CLASSNAME = "org.hawkinssoftware.rns.test.runtime.TestHandler";
		private static final String DIRECTIVE_BASE_CLASSNAME = "org.hawkinssoftware.rns.test.runtime.TestDirective";
		private static final String NOTIFICATION_BASE_CLASSNAME = "org.hawkinssoftware.rns.test.runtime.TestNotification";
		private static final String TRANSACTION_CLASSNAME = "java.util.List";

		@Override
		public String getRouterBaseClassname(RouterType routerType)
		{
			switch (routerType)
			{
				case DIRECTIVE:
					return DIRECTIVE_ROUTER_BASE_CLASSNAME;
				case NOTIFICATION:
					return NOTIFICATION_ROUTER_BASE_CLASSNAME;
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getTransactionElementBaseClassname(RouterType routerType)
		{
			switch (routerType)
			{
				case DIRECTIVE:
					return DIRECTIVE_BASE_CLASSNAME;
				case NOTIFICATION:
					return NOTIFICATION_BASE_CLASSNAME;
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getHandlerBaseClassname()
		{
			return HANDLER_BASE_CLASSNAME;
		}

		@Override
		public String getTransactionClassname()
		{
			return TRANSACTION_CLASSNAME;
		}
	}

	private final RouterInstrumentation agent = new RouterInstrumentation(new InstrumentationDefinition());

	@SuppressWarnings("unchecked")
	private TestDirectiveRouter createDirectiveRouter(RouterInstrumentation.Definition definition)
			throws IOException, InstantiationException, IllegalAccessException
	{
		byte[] classfileBytes = agent.create(definition);

		ClassCreator creator = new ClassCreator();
		Class<? extends TestDirectiveRouter> routerClass = (Class<? extends TestDirectiveRouter>) creator.createClass(
				definition.getInstrumentedRouterClassname(), classfileBytes);
		System.out.println("Created router class " + routerClass.getName());

		TestDirectiveRouter router = routerClass.newInstance();
		return router;
	}

	@SuppressWarnings("unchecked")
	private TestNotificationRouter createNotificationRouter(RouterInstrumentation.Definition definition)
			throws IOException, InstantiationException, IllegalAccessException
	{
		byte[] classfileBytes = agent.create(definition);

		ClassCreator creator = new ClassCreator();
		Class<? extends TestNotificationRouter> routerClass = (Class<? extends TestNotificationRouter>) creator.createClass(
				definition.getInstrumentedRouterClassname(), classfileBytes);
		System.out.println("Created router class " + routerClass.getName());

		TestNotificationRouter router = routerClass.newInstance();
		return router;
	}

	private RouterDefinition getInstrumentationDefinition(Method method)
	{
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 1)
		{
			if (!TestDirective.class.isAssignableFrom(parameterTypes[0]))
			{
				return null;
			}
			return new RouterDefinition(RouterInstrumentation.RouterType.DIRECTIVE, method.getName(), parameterTypes[0], method.getDeclaringClass().getName());
		}
		else if (parameterTypes.length == 2)
		{
			if (!TestNotification.class.isAssignableFrom(parameterTypes[0]))
			{
				return null;
			}
			if (!List.class.isAssignableFrom(parameterTypes[1]))
			{
				return null;
			}
			return new RouterDefinition(RouterInstrumentation.RouterType.NOTIFICATION, method.getName(), parameterTypes[0], method.getDeclaringClass()
					.getName());
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private void start() throws IOException, InstantiationException, IllegalAccessException
	{
		Class<? extends TestHandler> handlerClass = ParticularTestHandler.class;
		Map<Class<? extends TestDirective>, TestDirectiveRouter> actionRouters = new HashMap<Class<? extends TestDirective>, TestDirectiveRouter>();
		Map<Class<? extends TestNotification>, TestNotificationRouter> notificationRouters = new HashMap<Class<? extends TestNotification>, TestNotificationRouter>();
		for (Method method : handlerClass.getMethods())
		{
			RouterDefinition definition = getInstrumentationDefinition(method);
			if (definition != null)
			{
				if (definition.getRouterType() == RouterType.DIRECTIVE)
				{
					TestDirectiveRouter router = createDirectiveRouter(definition);
					actionRouters.put((Class<? extends TestDirective>) definition.parameterClass, router);
				}
				else
				{
					TestNotificationRouter router = createNotificationRouter(definition);
					notificationRouters.put((Class<? extends TestNotification>) definition.parameterClass, router);
				}
			}
		}

		ParticularTestHandlerImpl handler = new ParticularTestHandlerImpl();
		for (Map.Entry<Class<? extends TestDirective>, TestDirectiveRouter> entry : actionRouters.entrySet())
		{
			TestDirective action = entry.getKey().newInstance();
			entry.getValue().route(action, handler);
		}

		List<Integer> transaction = new ArrayList<Integer>();
		transaction.add(1);
		transaction.add(2);
		transaction.add(3);
		for (Map.Entry<Class<? extends TestNotification>, TestNotificationRouter> entry : notificationRouters.entrySet())
		{
			TestNotification notification = entry.getKey().newInstance();
			entry.getValue().route(notification, handler, transaction);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			RuntimeInstrumentationTest test = new RuntimeInstrumentationTest();
			test.start();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
