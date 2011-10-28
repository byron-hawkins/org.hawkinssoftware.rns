package org.hawkinssoftware.rns.core.moa;

import java.util.HashMap;
import java.util.Map;

public class ExecutionStack extends MethodInvocationStack<ExecutionStackFrame>
{
	final Map<ExecutionContext.Key<?>, ExecutionContext> clientContexts = new HashMap<ExecutionContext.Key<?>, ExecutionContext>();

	@Override
	void push(ExecutionStackFrame frame)
	{
		super.push(frame);

		for (ExecutionContext context : clientContexts.values())
		{
			context.pushFrame(frame);
		}
	}

	@Override
	ExecutionStackFrame pop()
	{
		ExecutionStackFrame pop = super.pop();
		for (ExecutionContext context : clientContexts.values())
		{
			context.popFromFrame(pop);
		}
		return pop;
	}

	<ContextType extends ExecutionContext> void installContext(ExecutionContext.Key<ContextType> key, ContextType context)
	{
		clientContexts.put(key, context);
		context.entryFrame = peek();
	}

	@SuppressWarnings("unchecked")
	<ContextType extends ExecutionContext> ContextType getContext(ExecutionContext.Key<ContextType> key)
	{
		return (ContextType) clientContexts.get(key);
	}

	void removeContext(ExecutionContext.Key<?> key)
	{
		clientContexts.remove(key).close();
	}
}
