package org.hawkinssoftware.rns.agent.message;

import org.apache.bcel.classfile.Method;
import org.hawkinssoftware.rns.agent.AbstractMethodTransformer;
import org.hawkinssoftware.rns.agent.InstrumentationMethodFactory.MethodInvocation;

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
