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
package org.hawkinssoftware.rns.core.log;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * DOC comment task awaits.
 * 
 * @param <CategoryType>
 *            the generic type
 * @author Byron Hawkins
 */
public class LogTag<CategoryType extends Enum<CategoryType>> implements LogTagFilter<CategoryType>
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public interface Category
	{
		@SuppressWarnings("rawtypes")
		Class<Enum> getTokenType();
	}

	private Map<CategoryType, Set<?>> tokens;

	@SuppressWarnings("unchecked")
	public LogTag(Class<CategoryType> categoryClass)
	{
		if (!Category.class.isAssignableFrom(categoryClass))
		{
			throw new IllegalArgumentException("Attempt to instantiate a LogTag with a type that does not implement LogTag.Category: "
					+ categoryClass.getName());
		}

		tokens = new EnumMap<CategoryType, Set<?>>(categoryClass);

		for (CategoryType category : categoryClass.getEnumConstants())
		{
			tokens.put(category, EnumSet.noneOf(((Category) category).getTokenType()));
		}

		initialize();
	}

	// hook for subclasses
	protected void initialize()
	{
	}

	// hook for subclasses
	protected boolean override()
	{
		return false;
	}
	
	@Override
	public boolean includes(LogTag<CategoryType> tag)
	{
		return intersects(tag);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> Set<E> get(Class<E> tokenType)
	{
		for (CategoryType category : tokens.keySet())
		{
			if (((Category) category).getTokenType() == tokenType)
			{
				return (Set<E>) tokens.get(category);
			}
		}
		return Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void put(E token)
	{
		get(token.getClass()).add(token);
	}

	public <E extends Enum<E>> void put(CategoryType category, Set<E> putTokens)
	{
		tokens.put(category, putTokens);
	}

	@SuppressWarnings("unchecked")
	public void fill(CategoryType category)
	{
		tokens.put(category, EnumSet.allOf(((Category) category).getTokenType()));
	}

	protected boolean intersects(LogTag<CategoryType> tag)
	{
		for (CategoryType category : tokens.keySet())
		{
			Set<?> myTokens = tokens.get(category);
			Set<?> tagTokens = tag.tokens.get(category);
			
			if (tagTokens == null)
			{
				// foreign tag, ignore it
				return false;
			}

			for (Object myToken : myTokens)
			{
				if (tagTokens.contains(myToken))
				{
					return true;
				}
			}
		}

		return false;
	}
}
