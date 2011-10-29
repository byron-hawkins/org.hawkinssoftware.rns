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
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ValidationMethod
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	public @interface Delegate
	{
	}
}
