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
package org.hawkinssoftware.rns.core.moa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * DOC comment task awaits.
 * 
 * @param <FrameType>
 *            the generic type
 * @author Byron Hawkins
 */
public class MethodInvocationStack<FrameType>
{
	final List<FrameType> stack = new ArrayList<FrameType>();
	final Set<HistoryIterator> historyIterators = new HashSet<HistoryIterator>();

	void push(FrameType frame)
	{
		invalidateHistoryIterator();
		stack.add(frame);
	}

	FrameType peek()
	{
		return stack.get(stack.size() - 1);
	}

	FrameType pop()
	{
		invalidateHistoryIterator();

		if (stack.isEmpty())
		{
			// this is lame, but if the stack is empty because of an instrumentation problem, an instrumentation
			// exception may otherwise be obscured by ArrayIndexOutOfBounds
			return null;
		}
		return stack.remove(stack.size() - 1);
	}

	boolean isEmpty()
	{
		return stack.isEmpty();
	}

	Iterable<FrameType> iterateHistory()
	{
		synchronized (historyIterators)
		{
			HistoryIterator iterator = new HistoryIterator();
			historyIterators.add(iterator);
			return iterator;
		}
	}

	private void invalidateHistoryIterator()
	{
		synchronized (historyIterators)
		{
			historyIterators.clear();
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private class HistoryIterator implements Iterator<FrameType>, Iterable<FrameType>
	{
		private int index = stack.size() - 1;

		@Override
		public Iterator<FrameType> iterator()
		{
			return this;
		}

		@Override
		public boolean hasNext()
		{
			if (!historyIterators.contains(this))
			{
				throw new IllegalStateException("This iterator is no longer valid because th stack has changed since this iterator was created.");
			}
			return index >= 0;
		}

		@Override
		public FrameType next()
		{
			if (!historyIterators.contains(this))
			{
				throw new IllegalStateException("This iterator is no longer valid because th stack has changed since this iterator was created.");
			}

			FrameType frame = stack.get(index);
			index--;
			return frame;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("No removal of stack frames via iteration!");
		}
	}
}
