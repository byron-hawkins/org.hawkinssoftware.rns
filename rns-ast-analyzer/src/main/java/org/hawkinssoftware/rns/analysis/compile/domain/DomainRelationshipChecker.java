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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.hawkinssoftware.rns.analysis.compile.HierarchyAnnotationChecker;
import org.hawkinssoftware.rns.analysis.compile.source.SourceDeclarationInstruction;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.role.DomainSpecifications;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

/**
 * Checks all subtypes of DomainRole for the presence of the required instance field, which must be annotated
 * 
 * @DomainRole.Instance. Conversely checks that every field annotated with @DomainRole.Instance is a member of a
 *                       DomainRole subtype.
 * 
 * @author Byron Hawkins
 */
public class DomainRelationshipChecker extends HierarchyAnnotationChecker
{
	private final IJavaProject project;

	public DomainRelationshipChecker(IJavaProject project)
	{
		super("domain-relationship");

		this.project = project;
		refreshSpecifications();
	}

	public boolean isSpecificationFile(IFile file)
	{
		return file.getName().endsWith(DomainSpecifications.SPECIFICATION_FILENAME_SUFFIX);
	}

	public void refreshSpecifications()
	{
		WorkspaceDomainSpecifications.getInstance().load(project.getProject());
	}

	@Override
	protected void startHierarchyAnalysis()
	{
		try
		{
			// WIP: should be able to process these, I think...
			if (hierarchy.getType().getFullyQualifiedName().matches(".*\\$[0-9].*"))
			{
				Log.out(Tag.WARNING, "Warning: Can't process anonymous inner type %s b/c it's declaration is not cached.", hierarchy.getType()
						.getFullyQualifiedName());
				return;
			}

			Set<DomainRoleTypeBinding> domains = new HashSet<DomainRoleTypeBinding>();
			for (DomainRoleTypeBinding role : DomainRoleTypeBinding.Cache.getInstance().getDomainRoles(hierarchy.getType().getFullyQualifiedName()))
			{
				domains.add(role);
			}
			for (IType type : hierarchy.getAllSupertypes(hierarchy.getType()))
			{
				for (DomainRoleTypeBinding role : DomainRoleTypeBinding.Cache.getInstance().getDomainRoles(type.getFullyQualifiedName()))
				{
					domains.add(role);
				}
			}

			DomainSpecificationBindings.EvaluationResult result = WorkspaceDomainSpecifications.getInstance().compilation.evaluate(hierarchy.getType()
					.getFullyQualifiedName(), domains);
			for (String problem : result.getProblems())
			{
				SourceDeclarationInstruction<AbstractTypeDeclaration, ITypeBinding> typeDeclaration = source.getTypeDeclaration(hierarchy.getType());
				if (typeDeclaration == null)
				{
					Log.out(Tag.CRITICAL, "Failed to find the type declaration for type %s", hierarchy.getType());
					continue;
				}
				createError(problem, typeDeclaration.markerReferenceNode);
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
