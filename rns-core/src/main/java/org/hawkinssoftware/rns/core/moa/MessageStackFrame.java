package org.hawkinssoftware.rns.core.moa;

import org.hawkinssoftware.rns.core.role.TypeRole;

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
