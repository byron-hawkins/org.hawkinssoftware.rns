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
package org.hawkinssoftware.rns.agent.aop;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.Type;
import org.hawkinssoftware.rns.agent.AbstractMethodTransformer;
import org.hawkinssoftware.rns.core.aop.InitializationAspect;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class InitializationMethodTransformer extends AbstractMethodTransformer
{
	private static final String INITIALIZATION_POINTCUT_CLASSNAME = InitializationAspect.Pointcut.class.getName();

	private final InitializationClassTransformer classTransformer;

	/**
	 * 
	 * @param classTransformer
	 * @param method
	 *            a constructor
	 */
	InitializationMethodTransformer(InitializationClassTransformer classTransformer, Method method)
	{
		super(classTransformer.classfile, method);

		this.classTransformer = classTransformer;
	}

	/**
	 * Wrap a constructor in a try/catch block (not really a try/finally, but it works just like one), invoking
	 * InitializationAspect.Pointcut.initializationPointcut() with the constructed instance at every return point in the
	 * constructor (so that the pointcut receives a fully constructed object, never something partially constructed).
	 */
	Method instrumentInitializationAspects()
	{
		Instruction loadThis = InstructionFactory.createThis();
		InvokeInstruction invocation = classTransformer.factory.createInvoke(INITIALIZATION_POINTCUT_CLASSNAME, "initializationPointcut", Type.VOID,
				Type.getTypes(new Class<?>[] { Object.class }), Constants.INVOKESTATIC);

		InstructionList body = methodGenerator.getInstructionList();
		InstructionHandle iteratorHandle = body.getStart();

		/*
		 * Invoke <code>invocation</code> before each return
		 */
		while (iteratorHandle != null)
		{
			Instruction instruction = iteratorHandle.getInstruction();
			if (instruction instanceof ReturnInstruction)
			{
				body.insert(iteratorHandle, loadThis);
				body.insert(iteratorHandle, invocation);
			}
			iteratorHandle = iteratorHandle.getNext();
		}

		/*
		 * Retarget all branch instructions pointed at a return (except JSR, which is sequential in the method body)
		 */
		iteratorHandle = body.getStart();
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
							select.setTarget(i, targets[i].getPrev().getPrev());
						}
					}
				}
				else if (!(instruction instanceof JsrInstruction))
				{
					// JSR will return to its successor, which will be the inserted method invocation
					BranchInstruction branch = (BranchInstruction) instruction;
					if (branch.getTarget().getInstruction() instanceof ReturnInstruction)
					{
						branch.setTarget(branch.getTarget().getPrev().getPrev());
					}
				}
			}
			iteratorHandle = iteratorHandle.getNext();
		}

		body.setPositions(true);
		return compileMethod();
	}
}
