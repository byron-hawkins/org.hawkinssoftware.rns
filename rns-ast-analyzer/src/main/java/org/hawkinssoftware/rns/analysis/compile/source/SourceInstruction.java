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
import org.eclipse.jdt.core.dom.IBinding;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

/**
 * DOC comment task awaits.
 * 
 * @param <Kind>
 *            the generic type
 * @param <InstructionType>
 *            the generic type
 * @param <InstructionBindingType>
 *            the generic type
 * @author Byron Hawkins
 */
public abstract class SourceInstruction<Kind extends SourceInstruction.InstructionKind, InstructionType extends ASTNode, InstructionBindingType extends IBinding>
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	interface InstructionKind
	{
		boolean isCompatible(ASTNode instruction, IBinding instructionBinding);
	}

	public final Kind kind;
	public final InstructionType instructionNode;
	public final ASTNode markerReferenceNode;
	public final InstructionBindingType instructionNodeBinding;
	public final String displayName;

	protected ParsedJavaSource containingSource;

	SourceInstruction(Kind kind, InstructionType instructionNode, ASTNode markerReferenceNode, InstructionBindingType instructionNodeBinding, String displayName)
	{
		if (!kind.isCompatible(instructionNode, instructionNodeBinding))
		{
			throw new IllegalArgumentException(String.format("Attempt to instantiate an instruction of kind %s with a %s instruction.", kind, instructionNode
					.getClass().getSimpleName()));
		}

		this.kind = kind;
		this.instructionNode = instructionNode;
		this.markerReferenceNode = markerReferenceNode;
		this.instructionNodeBinding = instructionNodeBinding;
		this.displayName = displayName;
	}
	
	abstract String getQualifiedName();

	void setContainingSource(ParsedJavaSource containingSource)
	{
		this.containingSource = containingSource;
	}

	private IType getContainingType()
	{
		ASTNode traversal = instructionNode;
		while ((traversal != null) && !(traversal instanceof AbstractTypeDeclaration))
		{
			traversal = traversal.getParent();
		}
		if (traversal == null)
		{
			Log.out(Tag.WARNING, "Failed to find the type containing instruction %s", instructionNode);
			return null;
		}
		IType type = (IType) ((AbstractTypeDeclaration) traversal).resolveBinding().getJavaElement();
		if (type == null)
		{
			Log.out(Tag.WARNING, "Failed to find the type containing instruction %s", instructionNode);
		}
		return type;
	}

	public String getContainingTypename()
	{
		IType containingType = getContainingType();
		if (containingType == null)
		{
			return "";
		}
		return containingType.getFullyQualifiedName();
	}

	public String getContainingPackageName()
	{
		IType containingType = getContainingType();
		if (containingType == null)
		{
			return "";
		}
		return containingType.getPackageFragment().getElementName();
	}

	@Override
	public String toString()
	{
		return instructionNode.toString();
	}

}
