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
package org.hawkinssoftware.rns.analysis.compile.source;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.hawkinssoftware.rns.core.util.UnknownEnumConstantException;

/**
 * DOC comment task awaits.
 * 
 * @param <InstructionType>
 *            the generic type
 * @param <InstructionBindingType>
 *            the generic type
 * @author Byron Hawkins
 */
public class SourceDeclarationInstruction<InstructionType extends ASTNode, InstructionBindingType extends IBinding> extends
		SourceInstruction<SourceDeclarationInstruction.Kind, InstructionType, InstructionBindingType>
{

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public enum Kind implements SourceInstruction.InstructionKind
	{
		/*
		 * Qualified name is IType.getFullyQualifiedName(). Checked for type visibility constraints.
		 */
		TYPE_DECLARATION(AbstractTypeDeclaration.class, ITypeBinding.class),
		/**
		 * Qualified name is (IType.getFullyQualifiedName() + "." + IMethodBinding.getName()).
		 */
		METHOD_DECLARATION(MethodDeclaration.class, IMethodBinding.class),
		/**
		 * Qualified name is (IType.getFullyQualifiedName() + "." + IVariableBinding.getName()).
		 */
		FIELD_DECLARATION(FieldDeclaration.class, IVariableBinding.class);

		final Class<? extends ASTNode> instructionType;
		final Class<? extends IBinding> instructionBindingType;

		Kind(Class<? extends ASTNode> instructionType, Class<? extends IBinding> instructionBindingType)
		{
			this.instructionType = instructionType;
			this.instructionBindingType = instructionBindingType;
		}

		@Override
		public boolean isCompatible(ASTNode instruction, IBinding instructionBinding)
		{
			return (instructionType.isAssignableFrom(instruction.getClass()) && instructionBindingType.isAssignableFrom(instructionBinding.getClass()));
		}
	}

	private static String createQualifiedName(Kind kind, IBinding binding)
	{
		switch (kind)
		{
			case TYPE_DECLARATION:
				return ((IType) binding.getJavaElement()).getFullyQualifiedName();
			case METHOD_DECLARATION:
				return ((IType) ((IMethodBinding) binding).getDeclaringClass().getJavaElement()).getFullyQualifiedName() + "." + binding.getName();
			case FIELD_DECLARATION:
				return ((IType) ((IVariableBinding) binding).getDeclaringClass().getJavaElement()).getFullyQualifiedName() + "." + binding.getName();
			default:
				throw new UnknownEnumConstantException(kind);
		}
	}

	private static String createTypeQualifiedName(Kind kind, IBinding binding)
	{
		switch (kind)
		{
			case TYPE_DECLARATION:
				return ((IType) binding.getJavaElement()).getTypeQualifiedName();
			case METHOD_DECLARATION:
				return ((IType) ((IMethodBinding) binding).getDeclaringClass().getJavaElement()).getTypeQualifiedName() + "." + binding.getName();
			case FIELD_DECLARATION:
				return ((IType) ((IVariableBinding) binding).getDeclaringClass().getJavaElement()).getTypeQualifiedName() + "." + binding.getName();
			default:
				throw new UnknownEnumConstantException(kind);
		}
	}

	private final String qualifiedName;

	SourceDeclarationInstruction(Kind kind, InstructionType instructionNode, ASTNode markerReferenceNode, InstructionBindingType instructionNodeBinding)
	{
		super(kind, instructionNode, markerReferenceNode, instructionNodeBinding, createTypeQualifiedName(kind, instructionNodeBinding));
		qualifiedName = createQualifiedName(kind, instructionNodeBinding);
	}

	@Override
	String getQualifiedName()
	{
		return qualifiedName;
	}
}
