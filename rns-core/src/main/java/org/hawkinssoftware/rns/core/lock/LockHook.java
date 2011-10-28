package org.hawkinssoftware.rns.core.lock;

import java.util.concurrent.locks.Lock;

public interface LockHook
{
	void attemptingAcquisition(Lock lock);
	
	void lockAcquired(Lock lock);
	
	void lockReleased(Lock lock);
}
