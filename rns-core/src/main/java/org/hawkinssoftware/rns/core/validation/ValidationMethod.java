package org.hawkinssoftware.rns.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ValidationMethod
{
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	public @interface Delegate
	{
	}
}
