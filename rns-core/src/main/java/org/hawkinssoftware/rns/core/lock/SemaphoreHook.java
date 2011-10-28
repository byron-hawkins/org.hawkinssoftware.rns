package org.hawkinssoftware.rns.core.lock;

public interface SemaphoreHook
{
	void attemptingAcquisition(Object semaphore);

	void semaphoreAcquired(Object semaphore);

	void semaphoreReleased(Object semaphore);
}
