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
 * All write references to the receiving field are instrumented with a preceding call to the enclosed
 * <code>ValidationAgent</code>. The intended application usage is:
 * 
 * <pre>
 * 1. Place annotations on all fields requiring some kind of validation for write access.
 * 2. Assign a <code>Validator</code> to the enclosed <code>ValidationAgent</code>.
 * </pre>
 * 
 * The <code>Validator</code> will then have an opportunity to examine every write access to the annotated fields and
 * take action if the access is not allowable. Ideally, this functionality would be implemented in an AST analyzer, but
 * this is all I know how to do at the moment. Annotate a type to have all its fields write-validated; exclude any
 * individual field using <code>@ValidateRead.Exempt</code>.
 * 
 * @author Byron Hawkins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ValidateWrite
{
	Class<?> validatorType() default ValidateWrite.class;

	/**
	 * Names a method having a signature matching "validateWrite" in the enclosed `ValidationAgent
	 */
	String method() default "validateWrite";

	/**
	 * Exempts an individual field from <code>@ValidateRead</code> placed on an entire type.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Exempt
	{
	}

	/**
	 * The implementor is eligible for registration with the <code>ValidationAgent</code>.
	 * 
	 * @author Byron Hawkins
	 */
	public interface Validator
	{
		void validateWrite(Object writer, Object fieldOwner, String fieldName);
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
		private static volatile Validator validator = null;

		public static void setValidator(Validator validator)
		{
			ValidationAgent.validator = validator;
		}

		public static void validateWrite(Object writer, Object fieldOwner, String fieldName)
		{
			// System.out.println("Validate write of " + fieldOwner + "." + fieldName + " by " + writer);

			if (validator != null)
			{
				validator.validateWrite(writer, fieldOwner, fieldName);
			}
		}
	}
}
