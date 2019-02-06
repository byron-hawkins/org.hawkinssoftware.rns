package org.hawkinssoftware.rns.test.agent;

import java.io.IOException;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.hawkinssoftware.rns.agent.AbstractMethodTransformer;
import org.hawkinssoftware.rns.agent.BytecodeInstrumentationUtil;
import org.hawkinssoftware.rns.agent.util.ClassfileLoader;

public class TestMethodTransformer extends AbstractMethodTransformer
{
	TestMethodTransformer(TestClassTransformer classTransformer, Method method)
	{
		super(classTransformer.classfile, method);
	}

	void examineFields() throws ClassFormatException, IOException
	{
		System.out.println("==== " + c.parsedType.getClassName() + " ====");

		InstructionList body = methodGenerator.getInstructionList();
		InstructionHandle iteratorHandle = body.getStart();
		while (iteratorHandle != null)
		{
			Instruction instruction = iteratorHandle.getInstruction();
			if (instruction instanceof FieldInstruction)
			{
				FieldInstruction access = (FieldInstruction) instruction;
				if ((access instanceof PUTFIELD) || (access instanceof PUTSTATIC))
				{
					System.out.println("put " + access.getFieldName(c.constants));
				}
				else
				{
					System.out.println("get " + access.getFieldName(c.constants));
				}

				for (AnnotationEntry annotation : getAnnotationEntries(access))
				{
					System.out.println("Annotation " + annotation.getAnnotationType() + " on " + access.getFieldName(c.constants));
				}
			}
			iteratorHandle = iteratorHandle.getNext();
		}
	}

	private AnnotationEntry[] getAnnotationEntries(FieldInstruction access) throws ClassFormatException, IOException
	{
		JavaClass parsedType;
		if (BytecodeInstrumentationUtil.getReceiverClassname(c, access).equals(c.parsedType.getClassName()))
		{
			parsedType = c.parsedType;
		}
		else
		{
			String classname = BytecodeInstrumentationUtil.getReceiverClassname(c, access);
			parsedType = ClassfileLoader.getInstance().loadClass(classname.replace('.', '/') + ".class");
		}
		return getAnnotationEntries(access.getFieldName(c.constants), parsedType);
	}

	private AnnotationEntry[] getAnnotationEntries(String fieldName, JavaClass type)
	{
		for (Field field : type.getFields())
		{
			if (field.getName().equals(fieldName))
			{
				return field.getAnnotationEntries();
			}
		}
		throw new IllegalArgumentException("No field named " + fieldName + " exists in class " + type.getClassName());
	}
}
