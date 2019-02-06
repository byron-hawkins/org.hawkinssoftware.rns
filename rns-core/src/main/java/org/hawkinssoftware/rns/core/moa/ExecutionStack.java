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

import java.util.HashMap;
import java.util.Map;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
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
