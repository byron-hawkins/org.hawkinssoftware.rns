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
package org.hawkinssoftware.rns.agent.message;

import org.apache.bcel.classfile.Method;
import org.hawkinssoftware.rns.agent.AbstractMethodTransformer;
import org.hawkinssoftware.rns.agent.InstrumentationMethodFactory.MethodInvocation;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class MessageStackMetaMethodTransformer extends AbstractMethodTransformer
{
	private final MessageStackClassTransformer classTransformer;

	MessageStackMetaMethodTransformer(MessageStackClassTransformer classTransformer, Method method)
	{
		super(classTransformer.classfile, method);

		this.classTransformer = classTransformer;
	}

	Method instrumentMetaMethod()
	{
		instructions.insert(classTransformer.methodFactory.buildInvocation(MethodInvocation.BEGIN_META_METHOD));
		instrumentInvocationOnReturn(classTransformer.methodFactory.buildInvocation(MethodInvocation.END_META_METHOD));
		return compileMethod();
	}
}
