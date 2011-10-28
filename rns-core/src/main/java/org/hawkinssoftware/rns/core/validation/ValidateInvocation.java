package org.hawkinssoftware.rns.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface ValidateInvocation
{
	/**
	 * The type on which to invoke the specified static <code>method()</code>. Defaults to the type in which the
	 * annotation occurs.
	 */
	Class<?> type() default ValidateInvocation.class; 

	/**
	 * Executed before the message stack frame is pushed, so thrown exceptions will not corrupt the stack. Any call from
	 * this method to another method that requires a new message stack frame will cause a circularity exception to be
	 * thrown. Defaults to validateMessage(TypeRole senderRole, TypeRole receiverRole).
	 */
	String method() default "validateMessage";
}
