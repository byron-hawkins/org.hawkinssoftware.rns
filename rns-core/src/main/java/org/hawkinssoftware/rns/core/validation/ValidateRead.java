package org.hawkinssoftware.rns.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ValidateRead
{
	Class<?> validatorType() default ValidateRead.class;

	/**
	 * Names a method having a signature matching "validateRead" in the enclosed `ValidationAgent
	 */
	String method() default "validateRead";

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Exempt
	{
	}

	public interface Validator
	{
		void validateRead(Object reader, Object fieldOwner, String fieldName);
	}

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
