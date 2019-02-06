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
package org.hawkinssoftware.rns.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.Type;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class AbstractMethodTransformer 
{
	protected InstrumentationClassFile c;
	protected Method method;
	protected MethodGen methodGenerator;
	protected InstructionList instructions;

	protected AbstractMethodTransformer(InstrumentationClassFile c, Method method)
	{
		this.c = c;
		this.method = method;
		methodGenerator = new MethodGen(method, BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType), c.constants);
		instructions = methodGenerator.getInstructionList();

		fixLocalVariables();
	}

	private void fixLocalVariables()
	{
		Map<Integer, InstructionHandle> handlesByPosition = new HashMap<Integer, InstructionHandle>();
		for (InstructionHandle handle : instructions.getInstructionHandles())
		{
			handlesByPosition.put(handle.getPosition(), handle);
		}

		if (method.getLocalVariableTable() != null)
		{
			methodGenerator.removeLocalVariables();
			for (LocalVariable local : method.getLocalVariableTable().getLocalVariableTable())
			{
				methodGenerator.addLocalVariable(local.getName(), Type.getType(local.getSignature()), local.getIndex(),
						handlesByPosition.get(local.getStartPC()), handlesByPosition.get(local.getStartPC() + local.getLength()));
			}
		}
	}

	/**
	 * Wrap the method in a try/catch block (not really a try/finally, but it works just like one), popping message
	 * frames at every return and in the catch (which then rethrows).
	 * 
	 * @param methodGenerator
	 *            the method to wrap
	 */
	protected void instrumentInvocationOnReturn(InvokeInstruction invokeInstruction)
	{
		InstructionList tryBody = methodGenerator.getInstructionList();
		InstructionHandle iteratorHandle = tryBody.getStart();

		/*
		 * Invoke <code>invocation</code> before each return
		 */
		while (iteratorHandle != null)
		{
			Instruction instruction = iteratorHandle.getInstruction();
			if (instruction instanceof ReturnInstruction)
			{
				tryBody.insert(iteratorHandle, invokeInstruction);
			}
			iteratorHandle = iteratorHandle.getNext();
		}

		/*
		 * Retarget all branch instructions pointed at a return (except JSR, which is sequential in the method body)
		 */
		iteratorHandle = tryBody.getStart();
		while (iteratorHandle != null)
		{
			Instruction instruction = iteratorHandle.getInstruction();
			if (instruction instanceof BranchInstruction)
			{
				if (instruction instanceof Select)
				{
					Select select = (Select) instruction;
					InstructionHandle[] targets = select.getTargets();
					for (int i = 0; i < targets.length; i++)
					{
						if (targets[i].getInstruction() instanceof ReturnInstruction)
						{
							select.setTarget(i, targets[i].getPrev());
						}
					}
				}
				else if (!(instruction instanceof JsrInstruction))
				{
					// JSR will return to its successor, which will be the inserted method invocation
					BranchInstruction branch = (BranchInstruction) instruction;
					if (branch.getTarget().getInstruction() instanceof ReturnInstruction)
					{
						branch.setTarget(branch.getTarget().getPrev());
					}
				}
			}
			iteratorHandle = iteratorHandle.getNext();
		}

		tryBody.setPositions(true);
		InstructionHandle tryEnd = tryBody.getEnd();

		/*
		 * Invoke <code>invocation</code> if any exception is thrown
		 */
		InstructionHandle catchStart = tryBody.append(invokeInstruction);
		tryBody.append(new ATHROW());

		/*
		 * List the catch block (directly above) for all exceptions
		 */
		methodGenerator.addExceptionHandler(tryBody.getStart(), tryEnd, catchStart, null);
	}

	protected Method compileMethod()
	{
		Set<String> originalLocalsAt_pc0 = new HashSet<String>();
		if ((method.getLocalVariableTable() != null) && (method.getLocalVariableTable().getLocalVariableTable() != null))
		{
			for (LocalVariable originalLocal : method.getLocalVariableTable().getLocalVariableTable())
			{
				if (originalLocal.getStartPC() == 0)
				{
					originalLocalsAt_pc0.add(originalLocal.getName());
				}
			}

			for (LocalVariableGen newLocal : methodGenerator.getLocalVariables())
			{
				if (originalLocalsAt_pc0.contains(newLocal.getName()))
				{
					newLocal.setStart(instructions.getStart());
				}
			}
		}

		LineNumberGen[] newLines = methodGenerator.getLineNumbers();
		if (newLines.length > 0)
		{
			newLines[0].setInstruction(instructions.getStart());
		}

		instructions.setPositions(true);
		methodGenerator.setMaxStack();
		methodGenerator.setMaxLocals();

		Method newMethod = methodGenerator.getMethod();
		instructions.dispose();
		return newMethod;
	}
}
