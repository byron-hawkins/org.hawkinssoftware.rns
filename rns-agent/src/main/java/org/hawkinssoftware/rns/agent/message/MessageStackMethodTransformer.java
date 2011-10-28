package org.hawkinssoftware.rns.agent.message;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.hawkinssoftware.rns.agent.AbstractMethodTransformer;
import org.hawkinssoftware.rns.agent.BytecodeInstrumentationUtil;
import org.hawkinssoftware.rns.agent.InstrumentationMethodFactory.MethodInvocation;
import org.hawkinssoftware.rns.agent.RNSInstrumentationConstants;

public class MessageStackMethodTransformer extends AbstractMethodTransformer implements RNSInstrumentationConstants
{
	private MessageStackClassTransformer classTransformer;

	MessageStackMethodTransformer(MessageStackClassTransformer classTransformer, Method method)
	{
		super(classTransformer.classfile, method);

		this.classTransformer = classTransformer;
	}

	private boolean instrumentCurrentCaller()
	{
		boolean insertedCode = false;

		InstructionHandle iterationHandle = instructions.getStart();
		while (iterationHandle != null)
		{
			try
			{
				if ((iterationHandle.getInstruction() instanceof InvokeInstruction) || (iterationHandle.getInstruction() instanceof NEW))
				{
					String calleeClassname = BytecodeInstrumentationUtil.getReceiverClassname(classTransformer.classfile, iterationHandle.getInstruction());
					String callDescription = BytecodeInstrumentationUtil.describeMethod(classTransformer.classfile, calleeClassname,
							iterationHandle.getInstruction());

					if (calleeClassname.startsWith("org.hawkinssoftware.rns.core") || calleeClassname.startsWith("org.hawkinssoftware.rns.agent"))
					{
						// not instrumenting calls to the core facilities
						continue;
					}

					instructions
							.insert(iterationHandle, new PUSH(c.constants, new ObjectType(BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType))));
					instructions.insert(iterationHandle, new PUSH(c.constants, callDescription));
					instructions.insert(iterationHandle, classTransformer.methodFactory.buildInvocation(MethodInvocation.POST_CALLER));
					insertedCode = true;
				}
			}
			finally
			{
				iterationHandle = iterationHandle.getNext();
			}
		}

		return insertedCode;
	}

	private InstructionHandle wrapCallToSuper() throws ClassNotFoundException
	{
		InstructionHandle handle = instructions.getStart();
		String methodDescription = BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType) + ".<init-pre-super>()";

		instructions.insert(handle, new PUSH(c.constants, new ObjectType(BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType))));
		instructions.insert(handle, new PUSH(c.constants, methodDescription));
		instructions.insert(handle, classTransformer.methodFactory.buildInvocation(MethodInvocation.INVOCATION_STACK_PUSH));

		// warning: handle condition after insertion is quite unreliable
		handle = instructions.getStart();

		String classname = BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType);
		String superClassname = BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType.getSuperClass());
		while (true)
		{
			if (handle.getInstruction() instanceof INVOKESPECIAL)
			{
				INVOKESPECIAL invocation = (INVOKESPECIAL) handle.getInstruction();
				String calleeClassname = BytecodeInstrumentationUtil.getCanonicalClassname(invocation.getReferenceType(c.constants));
				if ((calleeClassname.equals(classname) || calleeClassname.equals(superClassname)) && invocation.getMethodName(c.constants).equals("<init>"))
				{
					handle = handle.getNext();
					break;
				}
			}
			handle = handle.getNext();
		}

		instructions.insert(handle, classTransformer.methodFactory.buildInvocation(MethodInvocation.INVOCATION_STACK_POP));

		return handle;
	}

	private void instrumentMessageStackPush() throws ClassNotFoundException
	{
		// TODO: if the method has no instructions, should this be skipped? And validation?

		String methodDescription = BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType) + "." + method.getName() + "()";

		InstructionHandle startHandle = instructions.getStart();

		if (method.getName().equals("<init>"))
		{
			startHandle = wrapCallToSuper();
		}

		if (method.isStatic())
		{
			instructions.insert(startHandle, new PUSH(c.constants, new ObjectType(BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType))));
		}
		else
		{
			instructions.insert(startHandle, InstructionFactory.createThis());
		}
		instructions.insert(startHandle, new PUSH(c.constants, methodDescription));
		instructions.insert(startHandle, classTransformer.methodFactory.buildInvocation(MethodInvocation.INVOCATION_STACK_PUSH));
	}

	/**
	 * Instrument the method:
	 * 
	 * <pre>
	 * 1. add a validation call at the top if the class has a communication role, 
	 *    or if the method or class is annotated with @ValidationMethod
	 * 2. add a message stack push 
	 * 3. for every method call, post the name of this class (parsedType) to ExecutionPath.postCurrentCaller(),
	 *    so that message stack pushes know where the call came from
	 * 4. wrap from #2 to the end of the method in a try/catch--very important not to include the validation call 
	 *    in the try, because it may throw and the message stack frame is not pushed yet, so the catch (b) would 
	 *    erroneously pop the previous method's stack frame.
	 *    a. insert message stack pops before every return
	 *    b. insert a message stack pop in the catch
	 *    classTransformer. rethrow after pop in the catch
	 * @param methods
	 * @param methodIndex
	 * @throws ClassNotFoundException
	 */
	Method instrumentMethod() throws ClassNotFoundException
	{
		instrumentCurrentCaller();
		instrumentMessageStackPush();
		instrumentInvocationOnReturn(classTransformer.methodFactory.buildInvocation(MethodInvocation.INVOCATION_STACK_POP));
		return compileMethod();
	}
}
