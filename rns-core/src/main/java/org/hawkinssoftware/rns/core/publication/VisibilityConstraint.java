package org.hawkinssoftware.rns.core.publication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hawkinssoftware.rns.core.role.DomainRole;

// visibility never applies to methods or constructors, because they have no role other than to be invoked.
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
	
	public static class MyDomains extends DomainRole
	{
	}
}
