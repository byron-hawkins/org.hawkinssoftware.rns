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
package org.hawkinssoftware.rns.analysis.compile.publication;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.hawkinssoftware.rns.analysis.compile.domain.DomainRoleTypeBinding;
import org.hawkinssoftware.rns.analysis.compile.source.SourceReferenceInstruction;
import org.hawkinssoftware.rns.analysis.compile.source.TypeHierarchyCache;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class PublicationConstraintUsageChecker
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public interface ElementUsage
	{
		ASTNode getNode();

		String createMessage(String constraintType, ITypeBinding violatingType);
	}

	boolean isValid(SourceReferenceInstruction<?, ?> reference, AggregatePublicationConstraint publicationConstraint,
			AggregatePublicationConstraint proxyConstraint)
	{
		if (isValid(reference, publicationConstraint))
		{
			return true;
		}

		if ((proxyConstraint != null) && !proxyConstraint.exceeds(publicationConstraint))
		{
			return true;
		}

		return false;
	}

	/**
	 * 
	 * @param entries
	 *            annotation entries on the declaration of the type which is now being referenced by the possibly guilty
	 *            ASTNode.
	 * @return true if the node is guilty of violation according to <code>entries</code>.
	 */
	boolean isValid(SourceReferenceInstruction<?, ?> reference, AggregatePublicationConstraint publicationConstraint)
	{
		for (String packagePattern : publicationConstraint.packages)
		{
			if (reference.getContainingPackageName().matches(packagePattern))
			{
				return true;
			}
		}

		for (DomainRoleTypeBinding domainBinding : publicationConstraint.domains)
		{
			if (domainBinding.isMember(reference.getContainingTypename()))
			{
				return true;
			}
		}

		for (String typename : publicationConstraint.typenames)
		{
			if (typename.equals(reference.getContainingTypename()))
			{
				return true;
			}
		}

		ITypeHierarchy hierarchy = TypeHierarchyCache.getInstance().get(reference.getContainingTypename());
		for (String extendedTypename : publicationConstraint.extendedTypenames)
		{
			if (extendedTypename.equals(hierarchy.getType().getFullyQualifiedName()))
			{
				return true;
			}

			for (IType type : hierarchy.getAllSupertypes(hierarchy.getType()))
			{
				if (extendedTypename.equals(type.getFullyQualifiedName()))
				{
					return true;
				}
			}
		}

		return false;
	}
}
