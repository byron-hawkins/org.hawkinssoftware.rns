package org.hawkinssoftware.rns.core.moa;

public abstract class ExecutionContext
{
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
