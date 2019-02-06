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
import java.util.List;
import java.util.ListIterator;

/**
 * DOC comment task awaits.
 * 
 * @param <T>
 *            the generic type
 * @author Byron Hawkins
 */
public class AccessValidatingList<T> implements List<T>, AccessValidatedCollection<List<T>>
{
	public static <T> AccessValidatingList<T> create(List<T> list)
	{
		return new AccessValidatingList<T>(list);
	}

	private final List<T> list;

	private CollectionAccessValidator<List<T>> validator;

	private AccessValidatingList(List<T> list)
	{
		this.list = list;
	}

	@Override
	public void setValidator(CollectionAccessValidator<List<T>> validator)
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
			validator.validateRead(list, "size");
		}
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		if (validator != null)
		{
			validator.validateRead(list, "isEmpty");
		}
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		if (validator != null)
		{
			validator.validateRead(list, "contains", 9);
		}
		return list.contains(o);
	}

	@Override
	public Iterator<T> iterator()
	{
		if (validator != null)
		{
			validator.validateRead(list, "iterator");
		}
		return list.iterator();
	}

	@Override
	public Object[] toArray()
	{
		if (validator != null)
		{
			validator.validateRead(list, "toArray");
		}
		return list.toArray();
	}

	@Override
	public <E> E[] toArray(E[] a)
	{
		if (validator != null)
		{
			validator.validateRead(list, "toArray", a);
		}
		return list.toArray(a);
	}

	@Override
	public boolean add(T e)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "add", e);
		}
		return list.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "remove", o);
		}
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		if (validator != null)
		{
			validator.validateRead(list, "containsAll", c);
		}
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "addAll", c);
		}
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "addAll", c);
		}
		return list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "removeAll", c);
		}
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "retainAll", c);
		}
		return list.retainAll(c);
	}

	@Override
	public void clear()
	{
		if (validator != null)
		{
			validator.validateWrite(list, "clear");
		}
		list.clear();
	}

	@Override
	public T get(int index)
	{
		if (validator != null)
		{
			validator.validateRead(list, "get", index);
		}
		return list.get(index);
	}

	@Override
	public T set(int index, T element)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "set", element);
		}
		return list.set(index, element);
	}

	@Override
	public void add(int index, T element)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "add", index, element);
		}
		list.add(index, element);
	}

	@Override
	public T remove(int index)
	{
		if (validator != null)
		{
			validator.validateWrite(list, "remove", index);
		}
		return list.remove(index);
	}

	@Override
	public int indexOf(Object o)
	{
		if (validator != null)
		{
			validator.validateRead(list, "indexOf", o);
		}
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		if (validator != null)
		{
			validator.validateRead(list, "lastIndexOf", o);
		}
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		if (validator != null)
		{
			validator.validateRead(list, "listIterator");
		}
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index)
	{
		if (validator != null)
		{
			validator.validateRead(list, "listIterator", index);
		}
		return list.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		if (validator != null)
		{
			validator.validateRead(list, "subList", fromIndex, toIndex);
		}
		return list.subList(fromIndex, toIndex);
	}
}
