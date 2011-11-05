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
package org.hawkinssoftware.rns.core.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import org.hawkinssoftware.rns.core.publication.ExtensionConstraint;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.util.DefinesIdentity;

/**
 * The receiving type is instrumented with a post-constructor call to the <code>Agent</code> specified in annotation
 * entry <code>agent()</code>. If the type is an interface, all implementors will be instrumented with the
 * initialization call to the agent. The instrumented agent method call is placed at every exit point in the
 * constructor, so it will be invoked even if the constructor throws an exception.
 * 
 * @author Byron Hawkins
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializationAspect
{
	Class<? extends Agent<?>> agent();

	/**
	 * Implementors are eligible to receive initialization invocations at the constructor exit of every class annotated
	 * with <code>@InitializationAspect</code>. The annotated type will specify an <code>Agent</code> implementor in its
	 * annotation entry <code>agent()</code>.
	 * 
	 * @param <Type>
	 *            the generic type
	 * @author Byron Hawkins
	 */
	@DefinesIdentity
	@InvocationConstraint
	@ExtensionConstraint(domains = InitializationDomain.class)
	@VisibilityConstraint(domains = InitializationDomain.class)
	public interface Agent<Type>
	{
		void initialize(Type instance);
	}

	/**
	 * The constructor-exit instrumentation of <code>InitializationAgent</code> is always directed here for relay to the
	 * <code>Agent</code> specified in its annotation entry <code>agent()</code>.
	 * 
	 * @author Byron Hawkins
	 */
	public static class Pointcut
	{
		// TODO: this can be implemented without keeping a set here, if it becomes too bulky. When a class has multiple
		// superclasses which implement the annotated interface, each constructor calls this pointcut. But it's only
		// necessary to initialize from the constructor of the class implementing the annotated interface.
		private static final Set<Object> INITIALIZED_INSTANTIATIONS = new HashSet<Object>();

		public static void initializationPointcut(Object instantiation)
		{
			if (INITIALIZED_INSTANTIATIONS.contains(instantiation))
			{
				return;
			}

			Set<Class<Agent<?>>> agentClasses = new HashSet<Class<Agent<?>>>();
			Class<?> type = instantiation.getClass();
			while (!type.equals(Object.class))
			{
				for (Class<?> implemented : type.getInterfaces())
				{
					findAgentClasses(instantiation, implemented, agentClasses);
				}
				type = type.getSuperclass();
			}

			if (!agentClasses.isEmpty())
			{
				initializeInstance(instantiation, agentClasses);
				INITIALIZED_INSTANTIATIONS.add(instantiation);
			}
		}

		@SuppressWarnings("unchecked")
		private static void findAgentClasses(Object instantiation, Class<?> implemented, Set<Class<Agent<?>>> agentClasses)
		{
			InitializationAspect aspect = implemented.getAnnotation(InitializationAspect.class);
			if (aspect != null)
			{
				agentClasses.add((Class<Agent<?>>) aspect.agent());
			}

			for (Class<?> superImplemented : implemented.getInterfaces())
			{
				findAgentClasses(instantiation, superImplemented, agentClasses);
			}
		}

		@SuppressWarnings("unchecked")
		private static <AspectType> void initializeInstance(Object instantiation, Set<Class<Agent<?>>> agentClasses)
		{
			for (Class<Agent<?>> agentClass : agentClasses)
			{
				try
				{
					// TODO: annotate the instance and find it with reflection
					// TODO: AST analyzer for declaration of instance in an InitializationAspect.Agent
					Agent<AspectType> agent = (Agent<AspectType>) agentClass.getDeclaredField("INSTANCE").get(null);
					agent.initialize((AspectType) instantiation);
				}
				catch (Throwable t)
				{
					// TODO: not sure what to do on exception here
					throw new RuntimeException("Failed to invoke the initialization aspect of " + agentClass.getName() + " for "
							+ instantiation.getClass().getName(), t);
				}
			}
		}
	}
}
