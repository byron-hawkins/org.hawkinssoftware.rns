package org.hawkinssoftware.rns.agent.validation;

import java.io.IOException;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.SWAP;
import org.hawkinssoftware.rns.agent.AbstractMethodTransformer;
import org.hawkinssoftware.rns.agent.BytecodeInstrumentationUtil;
import org.hawkinssoftware.rns.agent.InstrumentationMethodFactory.MethodInvocation;
import org.hawkinssoftware.rns.agent.RNSInstrumentationConstants;

public class ValidationMethodTransformer extends AbstractMethodTransformer implements RNSInstrumentationConstants
{
	private final ValidationClassTransformer classTransformer;

	ValidationMethodTransformer(ValidationClassTransformer classTransformer, Method method)
	{
		super(classTransformer.classfile, method);

		this.classTransformer = classTransformer;
	}

	private InstructionList createValidationMethod(AnnotationEntry validationMethod)
	{
		String validationCalleeClassname = EXECUTION_PATH_CLASSNAME;
		String validationMethodName = DEFAULT_VALIDATION_METHOD_NAME;
		if (validationMethod != null)
		{
			validationCalleeClassname = BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType);
			for (ElementValuePair element : validationMethod.getElementValuePairs())
			{
				if (element.getNameString().equals("type"))
				{
					validationCalleeClassname = BytecodeInstrumentationUtil.getCanonicalClassname(element.getValue());
				}
				if (element.getNameString().equals("method"))
				{
					validationMethodName = element.getValue().stringifyValue();
				}
			}
		}

		// TODO: verify in bytecode that validationCalleeClassname has @ValidationMethod (the corresponding JDT task has
		// been done)

		InstructionList instructions = new InstructionList();
		instructions.append(classTransformer.methodFactory.buildInvocation(MethodInvocation.GET_CALLER_ROLE));
		instructions.append(new PUSH(c.constants, new ObjectType(BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType))));
		instructions.append(classTransformer.methodFactory.buildInvocation(MethodInvocation.GET_CALLEE_ROLE));
		InvokeInstruction insertion = classTransformer.methodFactory.buildValidationMethodInvocation(validationCalleeClassname, validationMethodName);
		instructions.append(insertion);

		return instructions;
	}

	private boolean instrumentFieldAccessValidation() throws ClassFormatException, IOException, ClassNotFoundException
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
				if (instruction instanceof FieldInstruction)
				{
					FieldInstruction access = (FieldInstruction) instruction;

					ValidationFieldMetadata field = new ValidationFieldMetadata(c, access);

					if (field.isValidated())
					{
						String validationCalleeClassname = field.isPut() ? WRITE_VALIDATION_AGENT_CLASSNAME : READ_VALIDATION_AGENT_CLASSNAME;
						String validationMethodName = field.isPut() ? VALIDATE_WRITE_METHOD_NAME : VALIDATE_READ_METHOD_NAME;
						for (ElementValuePair element : field.validationAnnotation.getElementValuePairs())
						{
							if (element.getNameString().equals("validatorType"))
							{
								validationCalleeClassname = BytecodeInstrumentationUtil.getCanonicalClassname(element.getValue());
							}
							if (element.getNameString().equals("method"))
							{
								validationMethodName = element.getValue().stringifyValue();
							}
						}

						if (field.definition.field.isStatic())
						{
							// push the accessing instance
							if (method.isStatic())
							{
								// -> <this>.class
								instructions.insert(iterationHandle,
										new PUSH(c.constants, new ObjectType(BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType))));
							}
							else
							{
								// -> this
								instructions.insert(iterationHandle, InstructionFactory.createThis());
							}

							// push the field owner instance -> <field-owner>.class
							instructions.insert(iterationHandle,
									new PUSH(c.constants, new ObjectType(BytecodeInstrumentationUtil.getCanonicalClassname(field.fieldOwnerType))));
						}
						else
						{
							if (field.isPut())
							{
								instructions.insert(iterationHandle, new DUP2());
								instructions.insert(iterationHandle, new POP());
							}
							else
							{
								instructions.insert(iterationHandle, new DUP());
							}

							if (method.isStatic())
							{
								// push the accessing instance -> <this>.class
								instructions.insert(iterationHandle,
										new PUSH(c.constants, new ObjectType(BytecodeInstrumentationUtil.getCanonicalClassname(c.parsedType))));
							}
							else
							{
								// push the accessing instance -> this
								instructions.insert(iterationHandle, InstructionFactory.createThis());
							}
							
							instructions.insert(iterationHandle, new SWAP());
						}

						// push the field name
						instructions.insert(iterationHandle, new PUSH(c.constants, access.getFieldName(c.constants)));
						Instruction invocation = classTransformer.methodFactory.buildAccessValidationMethodInvocation(validationCalleeClassname,
								validationMethodName);
						// invoke the validation method
						instructions.insert(iterationHandle, invocation);

						insertedCode = true;
					}
				}
			}
			finally
			{
				iterationHandle = iterationHandle.getNext();
			}
		}

		return insertedCode;
	}

	Method instrumentMethod() throws ClassFormatException, IOException, ClassNotFoundException
	{
		boolean insertedCode = false;

		AnnotationEntry validationMethod = classTransformer.classfile.metadata.classValidationMethodAnnotation;
		for (AnnotationEntry annotation : method.getAnnotationEntries())
		{
			if (annotation.getAnnotationType().equals(VALIDATE_INVOCATION_ANNOTATION_TYPENAME))
			{
				validationMethod = annotation;
			}
		}

		InstructionList validationMethodCode = null;
		if (classTransformer.classfile.metadata.hasCommunicationRole || (validationMethod != null))
		{
			validationMethodCode = createValidationMethod(validationMethod);
		}

		if (instrumentFieldAccessValidation())
		{
			insertedCode = true;
		}

		if (validationMethod != null)
		{
			instructions.insert(validationMethodCode);
			insertedCode = true;
		}

		if (insertedCode)
		{
			return compileMethod();
		}
		else
		{
			return null;
		}
	}
}
