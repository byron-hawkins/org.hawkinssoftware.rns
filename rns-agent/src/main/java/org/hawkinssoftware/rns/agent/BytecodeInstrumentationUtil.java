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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ReferenceType;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class BytecodeInstrumentationUtil
{
	private static String getBytecodeClassname(String canonicalClassname)
	{
		return "L" + canonicalClassname.replace(".", "/") + ";";
	}

	private static String getCanonicalClassname(String bytecodeClassname)
	{
		return bytecodeClassname.substring(1, bytecodeClassname.length() - 1).replace("/", ".");
	}

	public static String getBytecodeClassname(JavaClass type)
	{
		return getBytecodeClassname(type.getClassName());
	}

	public static String getBytecodeClassname(Class<?> type)
	{
		return getBytecodeClassname(type.getName());
	}

	public static String getCanonicalClassname(JavaClass type)
	{
		return type.getClassName();
	}

	public static String getCanonicalClassname(ElementValue annotationValue)
	{
		return getCanonicalClassname(annotationValue.stringifyValue());
	}

	public static String getCanonicalClassname(ReferenceType type)
	{
		return type.toString();
	}

	public static String getReceiverClassname(InstrumentationClassFile c, Instruction instruction)
	{
		if (instruction instanceof InvokeInstruction)
		{
			return BytecodeInstrumentationUtil.getCanonicalClassname(((InvokeInstruction) instruction).getReferenceType(c.constants));
		}
		else if (instruction instanceof NEW)
		{
			return BytecodeInstrumentationUtil.getCanonicalClassname(((NEW) instruction).getLoadClassType(c.constants));
		}
		else if (instruction instanceof FieldInstruction)
		{
			return BytecodeInstrumentationUtil.getCanonicalClassname(((FieldInstruction) instruction).getReferenceType(c.constants));
		}
		else
		{
			throw new UnsupportedOperationException(BytecodeInstrumentationUtil.class.getSimpleName() + " doesn't know how to get the reference type from a "
					+ instruction.getClass().getSimpleName());
		}
	}

	public static String describeMethod(InstrumentationClassFile c, String calleeClassname, Instruction instruction)
	{
		if (instruction instanceof InvokeInstruction)
		{
			return calleeClassname + "." + ((InvokeInstruction) instruction).getMethodName(c.constants) + "()";
		}
		else
		{
			return "new " + calleeClassname + "()";
		}
	}

	public static String getTypename(BasicType type)
	{
		switch (type.getType())
		{
			case Constants.T_BOOLEAN:
				return boolean.class.getName();
			case Constants.T_BYTE:
				return byte.class.getName();
			case Constants.T_CHAR:
				return char.class.getName();
			case Constants.T_DOUBLE:
				return double.class.getName();
			case Constants.T_FLOAT:
				return float.class.getName();
			case Constants.T_INT:
				return int.class.getName();
			case Constants.T_LONG:
				return long.class.getName();
			case Constants.T_SHORT:
				return short.class.getName();
			default:
				throw new IllegalArgumentException("Unknown BasicType " + type);
		}
	}
}
