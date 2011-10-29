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
package org.hawkinssoftware.rns.analysis.compile.domain;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.hawkinssoftware.rns.analysis.compile.HierarchyAnnotationChecker;
import org.hawkinssoftware.rns.analysis.compile.source.SourceDeclarationInstruction;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.util.RNSUtils;

/**
 * Checks all subtypes of DomainRole for the presence of the required instance field, which must be annotated
 * 
 * @DomainRole.Instance. Conversely checks that every field annotated with @DomainRole.Instance is a member of a
 *                       DomainRole subtype.
 * 
 * @author Byron Hawkins
 */
public class DomainRoleChecker extends HierarchyAnnotationChecker
{
	public DomainRoleChecker()
	{
		super("domain-role");
	}

	@Override
	protected void startHierarchyAnalysis()
	{
		try
		{
			boolean isDomainRole = isDomainRole(hierarchy.getType());
			for (IType superclass : hierarchy.getAllSuperclasses(hierarchy.getType()))
			{
				isDomainRole |= isDomainRole(superclass);
			}

			int instanceCount = 0;
			for (IField field : hierarchy.getType().getFields())
			{
				if (Flags.isStatic(field.getFlags()))
				{
					for (IAnnotation annotation : field.getAnnotations())
					{
						if (annotation.getElementName().contains(RNSUtils.getPlainName(DomainRole.Instance.class)))
						{
							String message;
							if (isDomainRole)
							{
								// TODO: would much rather use type-qualified names, but the sig doesn't have them
								if (Signature.getSignatureSimpleName(field.getTypeSignature()).equals(hierarchy.getType().getElementName()))
								{
									// The field is of the same type as the class that declares it, so this can be a
									// valid instance
									instanceCount++;
									continue;
								}
								else
								{
									message = "Field '" + field.getElementName() + "' is annotated @" + RNSUtils.getPlainName(DomainRole.Instance.class)
											+ " and therefore must be of type " + hierarchy.getType().getFullyQualifiedName() + ".";
								}
							}
							else
							{
								message = "Field '" + field.getElementName() + "' is annotated @" + RNSUtils.getPlainName(DomainRole.Instance.class)
										+ ", but the containing type " + hierarchy.getType().getFullyQualifiedName() + " is not a DomainRole.";
							}
							SourceDeclarationInstruction<FieldDeclaration, IVariableBinding> fieldDeclaration = source.getFieldDeclaration(field);
							createError(message, fieldDeclaration.markerReferenceNode);
						}
					}
				}
			}

			SourceDeclarationInstruction<AbstractTypeDeclaration, ITypeBinding> typeDeclaration = source.getTypeDeclaration(hierarchy.getType());

			if (isDomainRole)
			{
				if (instanceCount == 0)
				{
					String message = "Domain role " + hierarchy.getType().getFullyQualifiedName()
							+ " must contain one static field of its own type annotated with @" + RNSUtils.getPlainName(DomainRole.Instance.class) + ".";
					createError(message, typeDeclaration.markerReferenceNode);
				}
				else if (instanceCount > 1)
				{
					String message = "Domain role " + hierarchy.getType().getFullyQualifiedName() + " has multiple fields annotated @"
							+ RNSUtils.getPlainName(DomainRole.Instance.class) + ".";
					createError(message, typeDeclaration.markerReferenceNode);
				}
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	private boolean isDomainRole(IType type)
	{
		if (type == null)
		{
			return false;
		}
		return (DomainRole.class.getName().contains(type.getFullyQualifiedName()));
	}
}
