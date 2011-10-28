package org.hawkinssoftware.rns.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ValidateWrite
{
	Class<?> validatorType() default ValidateWrite.class;

	/**
	 * Names a method having a signature matching "validateWrite" in the enclosed `ValidationAgent
	 */
	String method() default "validateWrite";

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Exempt
	{
	}

	public interface Validator
	{
		void validateWrite(Object writer, Object fieldOwner, String fieldName);
	}

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
