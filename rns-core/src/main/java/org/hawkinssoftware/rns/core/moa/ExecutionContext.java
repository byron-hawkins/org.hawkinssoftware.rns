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

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public abstract class ExecutionContext
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @param <ContextType>
	 *            the generic type
	 * @author Byron Hawkins
	 */
	public interface Key<ContextType extends ExecutionContext>
	{
	}
	
	ExecutionStackFrame entryFrame;
	
	protected abstract void pushFrame(ExecutionStackFrame frame);
	
	protected abstract void popFromFrame(ExecutionStackFrame frame);
	
	protected void close()
	{
		// hook
	}
}
