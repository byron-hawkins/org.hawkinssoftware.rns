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
package org.hawkinssoftware.rns.core.publication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hawkinssoftware.rns.core.role.DomainRole;

// visibility never applies to methods or constructors, because they have no role other than to be invoked.
/**
 * Assigns a visibility constraint on the annotated type, such only those types which meet the criteria of the
 * annotation entries may make reference to it. The constraint is enforced by the RNS AST analyzer, which will create a
 * workspace error for every class referring to the annotated type without meeting the qualifications of the constraint.
 * All kinds of references are evaluated, including non-executional references such as type literals. No runtime
 * restriction is enforced.
 * <p>
 * <b>Warning:</b> <code>javac</code> from the JDK will fail on this class because of <a
 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6857918">bug 6857918</a>. This causes maven compile goals to
 * fail. Please compile in Eclipse and then execute the subsequent maven goals.
 * 
 * @author Byron Hawkins
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface VisibilityConstraint
{
	/**
	 * The set of classes whose members are allowed to hold references to the annotated type.
	 */
	Class<?>[] types() default {};

	/**
	 * The set of classes whose members are allowed to hold references to the annotated type.
	 */
	Class<?>[] extendedTypes() default {};

	/**
	 * The set of packages whose members are allowed to hold references to the annotated type.
	 */
	String[] packages() default {};

	/**
	 * The set of domains whose members are allowed to hold references to the annotated type.
	 */
	Class<? extends DomainRole>[] domains() default {};

	boolean inherit() default false;

	boolean voidInheritance() default false;

	/**
	 * Recognized packages() entry indicating the package of the class in which the annotation appears.
	 */
	public static final String MY_PACKAGE = "mine";

	public static final Class<? extends DomainRole> MY_DOMAINS = MyDomains.class;

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static class MyDomains extends DomainRole
	{
	}
}
