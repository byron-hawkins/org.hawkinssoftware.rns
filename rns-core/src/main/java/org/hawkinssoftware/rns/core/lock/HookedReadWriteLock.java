package org.hawkinssoftware.rns.core.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HookedReadWriteLock extends ReentrantReadWriteLock
{
	private class HookedReadLock extends ReadLock
	{
		// TODO: is volatile ok, or should this be synchronized?
		volatile LockHook hook = null;

		private HookedReadLock()
		{
			super(HookedReadWriteLock.this);
		}

		@Override
		public boolean tryLock()
		{
			if (hook != null)
			{
				hook.attemptingAcquisition(this);
			}

			boolean result = super.tryLock();

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

			boolean result = super.tryLock(timeout, unit);

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

			super.lock();

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

			super.lockInterruptibly();

			if (hook != null)
			{
				hook.lockAcquired(this);
			}
		}

		@Override
		public void unlock()
		{
			super.unlock();

			if (hook != null)
			{
				hook.lockReleased(this);
			}
		}
	}

	private class HookedWriteLock extends WriteLock
	{
		// TODO: is volatile ok, or should this be synchronized?
		volatile LockHook hook = null;

		private HookedWriteLock()
		{
			super(HookedReadWriteLock.this);
		}

		@Override
		public boolean tryLock()
		{
			if (hook != null)
			{
				hook.attemptingAcquisition(this);
			}

			boolean result = super.tryLock();

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

			boolean result = super.tryLock(timeout, unit);

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

			super.lock();

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

			super.lockInterruptibly();

			if (hook != null)
			{
				hook.lockAcquired(this);
			}
		}

		@Override
		public void unlock()
		{
			super.unlock();

			if (hook != null)
			{
				hook.lockReleased(this);
			}
		}
	}

	private HookedReadLock readLock = new HookedReadLock();
	private HookedWriteLock writeLock = new HookedWriteLock();

	@Override
	public ReadLock readLock()
	{
		return readLock;
	}

	@Override
	public WriteLock writeLock()
	{
		return writeLock;
	}

	public void setReadHook(LockHook hook)
	{
		readLock.hook = hook;
	}

	public void setWriteHook(LockHook hook)
	{
		writeLock.hook = hook;
	}

	public void setBothHooks(LockHook hook)
	{
		readLock.hook = hook;
		writeLock.hook = hook;
	}
}
