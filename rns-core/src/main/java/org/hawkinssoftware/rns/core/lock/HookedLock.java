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
package org.hawkinssoftware.rns.core.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A simple wrapper for a <code>Lock</code> which optionally notifies a <code>LockHook</code> before and after every
 * lock operation.
 * 
 * @param <LockType>
 *            the implementation of <code>Lock</code> contained in this wrapper
 * @author Byron Hawkins
 */
public class HookedLock<LockType extends Lock> implements Lock
{
	private final LockType lock;

	private volatile LockHook hook = null;

	public HookedLock(LockType lock)
	{
		this.lock = lock;
	}

	// synchronize, or leave it volatile?
	public void setHook(LockHook hook)
	{
		this.hook = hook;
	}

	public LockType getLock()
	{
		return lock;
	}

	@Override
	public boolean tryLock()
	{
		if (hook != null)
		{
			hook.attemptingAcquisition(this);
		}

		boolean result = lock.tryLock();

		if (result && (hook != null))
		{
			hook.lockAcquired(this);
		}

		return result;
	}

	@Override
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException
	{
		if (hook != null)
		{
			hook.attemptingAcquisition(this);
		}

		boolean result = lock.tryLock(timeout, unit);

		if (result && (hook != null))
		{
			hook.lockAcquired(this);
		}

		return result;
	}

	@Override
	public void lock()
	{
		if (hook != null)
		{
			hook.attemptingAcquisition(this);
		}

		lock.lock();

		if (hook != null)
		{
			hook.lockAcquired(this);
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException
	{
		if (hook != null)
		{
			hook.attemptingAcquisition(this);
		}

		lock.lockInterruptibly();

		if (hook != null)
		{
			hook.lockAcquired(this);
		}
	}

	@Override
	public void unlock()
	{
		lock.unlock();

		if (hook != null)
		{
			hook.lockReleased(this);
		}
	}

	@Override
	public Condition newCondition()
	{
		return lock.newCondition();
	}

}
