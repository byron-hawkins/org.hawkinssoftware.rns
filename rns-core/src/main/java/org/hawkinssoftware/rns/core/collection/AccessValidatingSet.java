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
package org.hawkinssoftware.rns.core.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * DOC comment task awaits.
 * 
 * @param <T>
 *            the generic type
 * @author Byron Hawkins
 */
public class AccessValidatingSet<T> implements Set<T>, AccessValidatedCollection<Set<T>>
{
	public static <T> AccessValidatingSet<T> create(Set<T> set)
	{
		return new AccessValidatingSet<T>(set);
	}

	public static <T> AccessValidatingSet<T> create(Set<T> set, CollectionAccessValidator<Set<T>> validator)
	{
		AccessValidatingSet<T> validatingSet = new AccessValidatingSet<T>(set);
		validatingSet.setValidator(validator);
		return validatingSet;
	}

	private final Set<T> set;

	private CollectionAccessValidator<Set<T>> validator;

	public AccessValidatingSet(Set<T> set)
	{
		this.set = set;
	}

	@Override
	public void setValidator(CollectionAccessValidator<Set<T>> validator)
	{
		if (enabled)
		{
			this.validator = validator;
		}
	}

	@Override
	public int size()
	{
		if (validator != null)
		{
			validator.validateRead(set, "size");
		}
		return set.size();
	}

	@Override
	public boolean isEmpty()
	{
		if (validator != null)
		{
			validator.validateRead(set, "isEmpty");
		}
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		if (validator != null)
		{
			validator.validateRead(set, "contains", o);
		}
		return set.contains(o);
	}

	@Override
	public Iterator<T> iterator()
	{
		if (validator != null)
		{
			validator.validateRead(set, "iterator");
		}
		return set.iterator();
	}

	@Override
	public Object[] toArray()
	{
		if (validator != null)
		{
			validator.validateRead(set, "toArray");
		}
		return set.toArray();
	}

	@Override
	public <E extends Object> E[] toArray(E[] a)
	{
		if (validator != null)
		{
			validator.validateRead(set, "toArray", a);
		}
		return set.toArray(a);
	}

	@Override
	public boolean add(T e)
	{
		if (validator != null)
		{
			validator.validateWrite(set, "add", e);
		}
		return set.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		if (validator != null)
		{
			validator.validateWrite(set, "remove", o);
		}
		return set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		if (validator != null)
		{
			validator.validateRead(set, "containsAll", c);
		}
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		if (validator != null)
		{
			validator.validateWrite(set, "addAll", c);
		}
		return set.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		if (validator != null)
		{
			validator.validateWrite(set, "retainAll", c);
		}
		return set.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		if (validator != null)
		{
			validator.validateWrite(set, "removeAll", c);
		}
		return set.removeAll(c);
	}

	@Override
	public void clear()
	{
		if (validator != null)
		{
			validator.validateWrite(set, "clear");
		}
		set.clear();
	}
}
