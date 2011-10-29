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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
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
public class SourceReferenceInstruction<InstructionType extends ASTNode, InstructionBindingType extends IBinding> extends
		SourceInstruction<SourceReferenceInstruction.Kind, InstructionType, InstructionBindingType>
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public enum Kind implements SourceInstruction.InstructionKind
	{
		/**
		 * Qualified name is IType.getFullyQualifiedName(). Found in the context of a TypeDeclaration. Checked for
		 * extension contraints.
		 */
		EXTENSION(Type.class, ITypeBinding.class),
		/**
		 * Qualified name is IType.getFullyQualifiedName(). Found in the context of a body declaration. Checked for type
		 * visibility constraints.
		 */
		EXPRESSION(Expression.class, ITypeBinding.class),
		/**
		 * Qualified name is IType.getFullyQualifiedName(). Checked for type visibility constraints.
		 */
		TYPE_REFERENCE(Type.class, ITypeBinding.class),
		/*
		 * Qualified name is (IType.getFullyQualifiedName() + "." + IMethodBinding.getName()). Checked for method
		 * invocation constraints.
		 */
		METHOD_CALL(Expression.class, IMethodBinding.class),
		/*
		 * Qualified name is (IType.getFullyQualifiedName() + "." + IMethodBinding.getName()). Checked for extension
		 * constraints.
		 */
		METHOD_OVERRIDE(MethodDeclaration.class, IMethodBinding.class);

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
	
	private static String createDisplayName(Kind kind, IType referredType, IBinding instructionNodeBinding)
	{
		switch (kind)
		{
			case EXPRESSION:
			case EXTENSION:
			case TYPE_REFERENCE:
				return referredType.getFullyQualifiedName();
			case METHOD_CALL:
			case METHOD_OVERRIDE:
				return referredType.getFullyQualifiedName() + "." + instructionNodeBinding.getName();
			default:
				throw new UnknownEnumConstantException(kind);
		}
	}

	public final IType referredType;

	SourceReferenceInstruction(Kind kind, InstructionType instructionNode, ASTNode markerReferenceNode, InstructionBindingType instructionNodeBinding,
			IType referredType)
	{
		super(kind, instructionNode, markerReferenceNode, instructionNodeBinding, createDisplayName(kind, referredType, instructionNodeBinding));

		this.referredType = referredType;
	}

	@Override
	String getQualifiedName()
	{
		return referredType.getFullyQualifiedName();
	}

	public Kind getKind()
	{
		return kind;
	}

	public void createMarker(String markerType, String message, int severity) throws CoreException
	{
		containingSource.createReferredTypeMarker(markerType, message, severity, markerReferenceNode, referredType.getFullyQualifiedName(),
				getContainingTypename());
	}
}
