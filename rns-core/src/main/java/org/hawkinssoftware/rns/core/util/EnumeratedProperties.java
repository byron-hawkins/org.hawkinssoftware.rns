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
package org.hawkinssoftware.rns.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class EnumeratedProperties
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public enum PropertyStatus
	{
		DOMAIN_ABSENT,
		EXACT_MATCH,
		PARTIAL_MATCH,
		NO_MATCH;
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private enum DomainType
	{
		INCLUSIVE,
		EXCLUSIVE;
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Exclusive
	{
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Inclusive
	{
	}

	private final Map<Class<? extends Enum<?>>, Object> exclusiveDomains = new HashMap<Class<? extends Enum<?>>, Object>();
	private final Map<Class<? extends Enum<?>>, EnumSet<? extends Enum<?>>> inclusiveDomains = new HashMap<Class<? extends Enum<?>>, EnumSet<? extends Enum<?>>>();

	public Map<Class<? extends Enum<?>>, Object> getExclusivePropertyDomains()
	{
		return exclusiveDomains;
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> E getProperty(Class<E> key)
	{
		return (E) exclusiveDomains.get(key);
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> PropertyStatus getPropertyStatus(E... queryValues)
	{
		Class<? extends Enum<?>> key = (Class<? extends Enum<?>>) queryValues[0].getClass();
		switch (getDomainType(key))
		{
			case INCLUSIVE:
			{
				EnumSet<? extends Enum<?>> inclusiveSet = inclusiveDomains.get(key);
				if (inclusiveSet == null)
				{
					return PropertyStatus.DOMAIN_ABSENT;
				}
				else
				{
					int matchCount = 0;
					for (E queryValue : queryValues)
					{
						if (inclusiveSet.contains(queryValue))
						{
							matchCount++;
						}
					}
					if (matchCount == 0)
					{
						return PropertyStatus.NO_MATCH;
					}
					else if (matchCount == queryValues.length)
					{
						return PropertyStatus.EXACT_MATCH;
					}
					else
					{
						return PropertyStatus.PARTIAL_MATCH;
					}
				}
			}
			case EXCLUSIVE:
			{
				E value = getProperty((Class<E>) queryValues[0].getClass());
				if (value == null)
				{
					return PropertyStatus.DOMAIN_ABSENT;
				}
				else
				{
					for (E queryValue : queryValues)
					{
						if (queryValue == value)
						{
							if (queryValues.length == 1)
							{
								return PropertyStatus.EXACT_MATCH;
							}
							else
							{
								return PropertyStatus.PARTIAL_MATCH;
							}
						}
					}
					return PropertyStatus.NO_MATCH;
				}
			}
		}
		throw new IllegalStateException("Unknown DomainType " + getDomainType(key));
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void setProperty(Class<E> key, E value)
	{
		switch (getDomainType(key))
		{
			case INCLUSIVE:
			{
				EnumSet<E> inclusiveSet = (EnumSet<E>) inclusiveDomains.get(key);
				if (inclusiveSet == null)
				{
					inclusiveSet = EnumSet.of(value);
					inclusiveDomains.put(key, inclusiveSet);
				}
				else
				{
					inclusiveSet.add(value);
				}
			}
			case EXCLUSIVE:
			{
				exclusiveDomains.put(key, value);
			}
		}
	}

	public <E extends Enum<E>> void clearDomain(Class<E> key)
	{
		switch (getDomainType(key))
		{
			case INCLUSIVE:
			{
				EnumSet<? extends Enum<?>> inclusiveSet = inclusiveDomains.get(key);
				if (inclusiveSet != null)
				{
					inclusiveSet.clear();
				}
			}
			case EXCLUSIVE:
			{
				exclusiveDomains.remove(key);
			}
		}
	}

	public <E extends Enum<E>> void clearProperty(Class<E> key, E value)
	{
		switch (getDomainType(key))
		{
			case INCLUSIVE:
			{
				EnumSet<? extends Enum<?>> inclusiveSet = inclusiveDomains.get(key);
				if (inclusiveSet != null)
				{
					inclusiveSet.remove(value);
				}
			}
			case EXCLUSIVE:
			{
				if (exclusiveDomains.get(key) == value)
				{
					exclusiveDomains.remove(key);
				}
			}
		}
	}

	private DomainType getDomainType(Class<? extends Enum<?>> type)
	{
		if (type.getAnnotation(Inclusive.class) != null)
		{
			return DomainType.INCLUSIVE;
		}
		return DomainType.EXCLUSIVE;
	}
}
