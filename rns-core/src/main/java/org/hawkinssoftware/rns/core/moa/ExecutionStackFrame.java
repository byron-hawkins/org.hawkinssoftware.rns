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
public class ExecutionStackFrame
{
	private final Object receiver;
	private final String methodDescription;
	
	private boolean hasMessageFrame = false;

	public ExecutionStackFrame(Object receiver, String methodDescription)
	{
		this.receiver = receiver;
		this.methodDescription = methodDescription;
	}

	public Object getReceiver()
	{
		return receiver;
	}
	
	public String getMethodDescription()
	{
		return methodDescription;
	}
	
	public boolean hasMessageFrame()
	{
		return hasMessageFrame;
	}
	
	public void setHasMessageFrame(boolean hasMessageFrame)
	{
		this.hasMessageFrame = hasMessageFrame;
	}
}