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

import java.util.concurrent.locks.Lock;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public interface LockHook
{
	void attemptingAcquisition(Lock lock);
	
	void lockAcquired(Lock lock);
	
	void lockReleased(Lock lock);
}
