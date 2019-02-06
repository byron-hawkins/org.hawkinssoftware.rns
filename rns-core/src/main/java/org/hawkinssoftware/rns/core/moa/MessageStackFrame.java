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

import org.hawkinssoftware.rns.core.role.TypeRole;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class MessageStackFrame
{
	static final MessageStackFrame EMPTY = new MessageStackFrame("<empty", "<empty>", TypeRole.NONE, TypeRole.NONE);

	private final String invocationDescription;
	private final String methodDescription;

	private final TypeRole senderRole;
	private TypeRole receiverRole;

	public MessageStackFrame(String invocationDescription, String methodDescription, TypeRole senderRole, TypeRole receiverRole)
	{
		this.invocationDescription = invocationDescription;
		this.methodDescription = methodDescription;

		this.senderRole = senderRole;
		this.receiverRole = receiverRole;
	}

	public String getInvocationDescription()
	{
		return invocationDescription;
	}

	public String getMethodDescription()
	{
		return methodDescription;
	}

	public TypeRole getReceiverRole()
	{
		return receiverRole;
	}

	void setReceiverRole(TypeRole receiverRole)
	{
		this.receiverRole = receiverRole;
	}

	public TypeRole getSenderRole()
	{
		return senderRole;
	}
}
