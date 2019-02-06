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
package org.hawkinssoftware.rns.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All read references to the receiving field are instrumented with a preceding call to the enclosed
 * <code>ValidationAgent</code>. The intended application usage is:
 * 
 * <pre>
 * 1. Place annotations on all fields requiring some kind of validation for read access.
 * 2. Assign a <code>Validator</code> to the enclosed <code>ValidationAgent</code>.
 * </pre>
 * 
 * The <code>Validator</code> will then have an opportunity to examine every read access to the annotated fields and
 * take action if the access is not allowable. Ideally, this functionality would be implemented in an AST analyzer, but
 * this is all I know how to do at the moment.
 * <p>
 * Annotate a type to have all its fields read-validated; exclude any individual field using
 * <code>@ValidateRead.Exempt</code>.
 * 
 * @author Byron Hawkins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ValidateRead
{
	Class<?> validatorType() default ValidateRead.class;

	/**
	 * Names a method having a signature matching "validateRead" in the enclosed `ValidationAgent
	 */
	String method() default "validateRead";

	/**
	 * Exempts an individual field from <code>@ValidateRead</code> placed on an entire type.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Exempt
	{
	}

	/**
	 * The implementor is eligible for registration with the <code>ValidationAgent</code>.
	 * 
	 * @author Byron Hawkins
	 */
	public interface Validator
	{
		void validateRead(Object reader, Object fieldOwner, String fieldName);
	}

	/**
	 * Relay point for validation invocations instrumented according to <code>@ValidateRead</code>. The application is
	 * expected to assign one <code>Validator</code> using <code>setValidator()</code>, which will receive the relay of
	 * all instrumented validation calls.
	 * 
	 * @author Byron Hawkins
	 */
	public static class ValidationAgent
	{
		private static Validator validator = null;

		public static void setValidator(Validator validator)
		{
			synchronized (ValidationAgent.class)
			{
				ValidationAgent.validator = validator;
			}
		}

		public static void validateRead(Object reader, Object fieldOwner, String fieldName)
		{
			// System.out.println("Validate read of " + fieldOwner + "." + fieldName + " by " + reader);

			if (validator != null)
			{
				validator.validateRead(reader, fieldOwner, fieldName);
			}
		}
	}
}
