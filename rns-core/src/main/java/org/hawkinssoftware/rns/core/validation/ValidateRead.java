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
 * DOC comment task awaits.
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
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Exempt
	{
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public interface Validator
	{
		void validateRead(Object reader, Object fieldOwner, String fieldName);
	}

	/**
	 * DOC comment task awaits.
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
