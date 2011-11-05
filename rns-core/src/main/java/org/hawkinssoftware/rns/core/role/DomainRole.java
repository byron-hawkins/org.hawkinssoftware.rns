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
package org.hawkinssoftware.rns.core.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DomainRole is the base class for the domain hierarchy, which the developer constructs and uses to delineate
 * groups of types (classes, interfaces and enums). Every DomainRole must have a static singleton instance annotated
 * with @DomainRole.Instance; the RNS build analyzer will call out any DomainRole lacking this singleton field.
 * <p>
 * <b>Containment:</b> DomainRole A is said to contain DomainRole B if the class A is a superclass of B.
 * <p>
 * <b>Inclusion:</b> A DomainRole can itself be a member of another DomainRole, and in that sense it is included in the
 * other DomainRole. This relation is not presently used and seems risky because it allows for multiple inheritance in
 * the domain graph. The containment relation has much lighter implications and seems sufficient for all ordinary
 * purposes.
 * 
 * @author Byron Hawkins
 */
public class DomainRole extends CommunicationRole
{
	/**
	 * Place this annotation on any type (class, interface or enum) to identify it as a member of the DomainRoles
	 * indicated in <code>membership()</code>.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Join
	{
		Class<? extends DomainRole>[] membership() default {};
	}

	/**
	 * Required annotation on the singleton instance of a DomainRole class declaration.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Instance
	{
	}

	private String name;

	public boolean includes(CommunicationRole other)
	{
		if (other == this)
		{
			return true;
		}

		for (CommunicationRole category : other.membership)
		{
			if (includes(category))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Defines a set of domains and package patterns which must share no members. For example, an OrthogonalSet
	 * <code>{"com.foo.package.*", KeyboardInputDomain.class}</code> indicates that no type in package
	 * <code>com.foo.package</code> may be a member of the <code>KeyboardInputDomain</code>.
	 * 
	 * @author Byron Hawkins
	 */
	public static class OrthogonalSet
	{
		final List<DomainRole> domainsAssembly = new ArrayList<DomainRole>();
		final List<String> packagePatternsAssembly = new ArrayList<String>();

		public final List<DomainRole> domains = Collections.unmodifiableList(domainsAssembly);
		public final List<String> packagePatterns = Collections.unmodifiableList(packagePatternsAssembly);
	}

	/**
	 * Finds and returns the static singleton instance of a DomainRole, given the
	 * <code>Class<? extends DomainRole></code>.
	 * 
	 * @author Byron Hawkins
	 */
	public static class Resolver
	{
		private static final Map<Class<? extends DomainRole>, DomainRole> INSTANCE_CACHE = new HashMap<Class<? extends DomainRole>, DomainRole>();

		public static DomainRole getInstance(Class<? extends DomainRole> type) throws IllegalArgumentException, IllegalAccessException
		{
			DomainRole instance = INSTANCE_CACHE.get(type);
			if (instance == null)
			{
				for (Field field : type.getFields())
				{
					if (Modifier.isStatic(field.getModifiers()) && (field.getAnnotation(DomainRole.Instance.class) != null))
					{
						field.setAccessible(true);
						instance = (DomainRole) field.get(null);
						INSTANCE_CACHE.put(type, instance);
					}
				}
				if (instance == null)
				{
					throw new IllegalStateException("Domain role " + type.getCanonicalName() + " has no instance!");
				}
			}
			return instance;
		}
	}
}