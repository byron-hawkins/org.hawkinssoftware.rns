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
package org.hawkinssoftware.rns.agent.validation;

import java.io.IOException;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.FLOAD;
import org.apache.bcel.generic.FSTORE;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.StoreInstruction;
import org.hawkinssoftware.rns.agent.BytecodeInstrumentationUtil;
import org.hawkinssoftware.rns.agent.InstrumentationClassFile;
import org.hawkinssoftware.rns.agent.RNSInstrumentationConstants;
import org.hawkinssoftware.rns.agent.util.ClassfileLoader;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class ValidationFieldMetadata implements RNSInstrumentationConstants
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	static class FieldDefinition
	{
		final JavaClass definingType;
		final Field field;

		public FieldDefinition(JavaClass definingType, Field field)
		{
			this.definingType = definingType;
			this.field = field;
		}
	}

	final InstrumentationClassFile c;
	final FieldDefinition definition;
	final JavaClass fieldOwnerType;
	final FieldInstruction access;
	final AnnotationEntry[] fieldAnnotations;
	final AnnotationEntry validationAnnotation;
	final AnnotationEntry exemptionAnnotation;

	public ValidationFieldMetadata(InstrumentationClassFile c, FieldInstruction access) throws ClassFormatException, IOException, ClassNotFoundException
	{
		this.c = c;
		this.access = access;

		if (BytecodeInstrumentationUtil.getReceiverClassname(c, access).equals(c.parsedType.getClassName()))
		{
			fieldOwnerType = c.parsedType;
		}
		else
		{
			String classname = BytecodeInstrumentationUtil.getReceiverClassname(c, access);
			fieldOwnerType = ClassfileLoader.getInstance().loadClass(classname.replace('.', '/') + ".class");
		}

		definition = getFieldDefinition(fieldOwnerType, access.getFieldName(c.constants));

		String validationAnnotationTypename = isPut() ? VALIDATE_WRITE_ANNOTATION_TYPENAME : VALIDATE_READ_ANNOTATION_TYPENAME;
		AnnotationEntry foundValidationAnnotation = null;
		for (AnnotationEntry annotation : definition.definingType.getAnnotationEntries())
		{
			if (annotation.getAnnotationType().equals(validationAnnotationTypename))
			{
				foundValidationAnnotation = annotation;
				// System.out.println("Annotation " + annotation.getAnnotationType() + " on " + describeField() +
				// " via type annotation on "
				// + field.definingType.getClassName() + ".");
				break;
			}
		}

		fieldAnnotations = definition.field.getAnnotationEntries();
		if (foundValidationAnnotation == null)
		{
			for (AnnotationEntry annotation : fieldAnnotations)
			{
				if (annotation.getAnnotationType().equals(validationAnnotationTypename))
				{
					foundValidationAnnotation = annotation;
					// System.out.println("Annotation " + annotation.getAnnotationType() + " on " + describeField());
					break;
				}
			}
		}
		validationAnnotation = foundValidationAnnotation;
		
		AnnotationEntry foundExemptionAnnotation = null;
		String exemptionAnnotationTypename = isPut() ? WRITE_EXEMPTION_ANNOTATION_TYPENAME : READ_EXEMPTION_ANNOTATION_TYPENAME;
		for (AnnotationEntry annotation : fieldAnnotations)
		{
			if (annotation.getAnnotationType().equals(exemptionAnnotationTypename))
			{
				foundExemptionAnnotation = annotation;
				// System.out.println("Annotation " + annotation.getAnnotationType() + " on " + describeField());
				break;
			}
		}
		exemptionAnnotation = foundExemptionAnnotation;
	}

	private FieldDefinition getFieldDefinition(JavaClass type, String fieldName) throws ClassNotFoundException
	{
		if (type.getClassName().equals(Object.class.getName()))
		{
			throw new IllegalArgumentException("No field named " + fieldName + " exists in class " + fieldOwnerType.getClassName());
		}

		for (Field field : type.getFields())
		{
			if (field.getName().equals(fieldName))
			{
				return new FieldDefinition(type, field);
			}
		}

		return getFieldDefinition(type.getSuperClass(), fieldName);
	}

	boolean isValidated()
	{
		return (validationAnnotation != null) && (exemptionAnnotation == null) && (!definition.field.isFinal()) && !definition.field.getName().contains("$SWITCH_TABLE$");
	}

	boolean isPut()
	{
		return (access instanceof PUTFIELD) || (access instanceof PUTSTATIC);
	}

	String describeField()
	{
		return fieldOwnerType.getClassName() + "." + access.getFieldName(c.constants);
	}

	LoadInstruction createLoad(int index)
	{
		switch (definition.field.getType().getType())
		{
			case Constants.T_BOOLEAN:
			case Constants.T_BYTE:
			case Constants.T_CHAR:
			case Constants.T_INT:
				return new ILOAD(index);
			case Constants.T_DOUBLE:
				return new DLOAD(index);
			case Constants.T_FLOAT:
				return new FLOAD(index);
			case Constants.T_LONG:
				return new LLOAD(index);
			default:
				return new ALOAD(index);
		}
	}

	StoreInstruction createStore(int index)
	{
		switch (definition.field.getType().getType())
		{
			case Constants.T_BOOLEAN:
			case Constants.T_BYTE:
			case Constants.T_CHAR:
			case Constants.T_INT:
				return new ISTORE(index);
			case Constants.T_DOUBLE:
				return new DSTORE(index);
			case Constants.T_FLOAT:
				return new FSTORE(index);
			case Constants.T_LONG:
				return new LSTORE(index);
			default:
				return new ASTORE(index);
		}
	}
}
