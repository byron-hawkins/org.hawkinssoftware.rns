package org.hawkinssoftware.rns.core.moa;

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
