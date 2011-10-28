package org.hawkinssoftware.rns.agent.lock;

import java.io.IOException;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.SWAP;
import org.hawkinssoftware.rns.agent.AbstractMethodTransformer;
import org.hawkinssoftware.rns.agent.BytecodeInstrumentationUtil;
import org.hawkinssoftware.rns.agent.InstrumentationMethodFactory.MethodInvocation;
import org.hawkinssoftware.rns.agent.RNSInstrumentationConstants;

public class SemaphoreMethodTransformer extends AbstractMethodTransformer implements RNSInstrumentationConstants
{
	private final SemaphoreClassTransformer classTransformer;
	private final PUSH thisClassReference;

	SemaphoreMethodTransformer(SemaphoreClassTransformer classTransformer, Method method)
	{
		super(classTransformer.classfile, method);

		this.classTransformer = classTransformer;
		thisClassReference = new PUSH(classTransformer.classfile.constants, new ObjectType(
				BytecodeInstrumentationUtil.getCanonicalClassname(classTransformer.classfile.parsedType)));
	}

	private boolean instrumentMonitors() throws ClassFormatException, IOException, ClassNotFoundException
	{
		if (method.getName().equals("<init>") || method.getName().equals("<clinit>"))
		{
			// constructors are always free to read and write
			return false;
		}

		boolean insertedCode = false;

		InstructionHandle iterationHandle = instructions.getStart();
		while (iterationHandle != null)
		{
			try
			{
				Instruction instruction = iterationHandle.getInstruction();
				if (instruction instanceof MONITORENTER)
				{
					instructions.insert(iterationHandle, new DUP());
					instructions.insert(iterationHandle, new DUP());
					instructions.insert(iterationHandle, thisClassReference);
					instructions.insert(iterationHandle, new SWAP());
					instructions.insert(iterationHandle, classTransformer.methodFactory.buildInvocation(MethodInvocation.ATTEMPTING_ACQUISITION_RELAY));

					iterationHandle = iterationHandle.getNext();
					instructions.insert(iterationHandle, thisClassReference);
					instructions.insert(iterationHandle, new SWAP());
					instructions.insert(iterationHandle, classTransformer.methodFactory.buildInvocation(MethodInvocation.SEMAPHORE_ACQUIRED_RELAY));

					insertedCode = true;
				}
				else if (instruction instanceof MONITOREXIT)
				{
					instructions.insert(iterationHandle, new DUP());
					iterationHandle = iterationHandle.getNext();
					instructions.insert(iterationHandle, thisClassReference);
					instructions.insert(iterationHandle, new SWAP());
					instructions.insert(iterationHandle, classTransformer.methodFactory.buildInvocation(MethodInvocation.SEMAPHORE_RELEASED_RELAY));
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

	private void instrumentMethodMonitor()
	{
		InstructionHandle start = instructions.getStart();
		instructions.insert(start, thisClassReference);
		instructions.insert(start, InstructionFactory.createThis());
		instructions.insert(start, classTransformer.methodFactory.buildInvocation(MethodInvocation.SYNCHRONIZED_METHOD_ENTRY_RELAY));

		instrumentInvocationOnReturn(classTransformer.methodFactory.buildInvocation(MethodInvocation.SYNCHRONIZED_METHOD_EXIT_RELAY));
	}

	private void insertStatementHookInvocation(InstructionHandle handle, MethodInvocation invocation)
	{
		instructions.insert(handle, new DUP());
		instructions.insert(handle, thisClassReference);
		instructions.insert(handle, new SWAP());
		instructions.insert(handle, classTransformer.methodFactory.buildInvocation(invocation));
	}

	Method instrumentMethod() throws ClassFormatException, IOException, ClassNotFoundException
	{
		boolean insertedCode = instrumentMonitors();

		if (method.isSynchronized())
		{
			instrumentMethodMonitor();
			insertedCode = true;
		}

		if (insertedCode)
		{
			return compileMethod();
		}

		return null;
	}
}
