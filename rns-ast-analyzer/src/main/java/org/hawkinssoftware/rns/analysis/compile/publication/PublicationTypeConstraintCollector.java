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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class PublicationTypeConstraintCollector
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private class TypeScanPass
	{
		final List<AggregatePublicationConstraint> applicableConstraints = new ArrayList<AggregatePublicationConstraint>();
		final ITypeHierarchy hierarchy;

		TypeScanPass(ITypeHierarchy hierarchy)
		{
			this.hierarchy = hierarchy;
		}

		TypeScanPass copy()
		{
			return new TypeScanPass(hierarchy);
		}
	}

	private final PublicationConstraintCache constraintCache = new PublicationConstraintCache();
	private final Class<? extends Annotation> constraintAnnotationType;

	PublicationTypeConstraintCollector(Class<? extends Annotation> constraintAnnotationType)
	{
		this.constraintAnnotationType = constraintAnnotationType;
	}

	void typeChanged(IType type)
	{
		constraintCache.typeChanged(type);
	}

	String getConstraintName()
	{
		return constraintAnnotationType.getSimpleName();
	}

	/**
	 * Parse the constraint annotations directly appearing on <code>type</code>.
	 */
	AggregatePublicationConstraint parseTypeConstraints(IType type) throws JavaModelException
	{
		if (!constraintCache.hasTypeConstraints(type))
		{
			AggregatePublicationConstraint aggregate = null;
			for (IAnnotation annotation : type.getAnnotations())
			{
				if (annotation.getElementName().contains(constraintAnnotationType.getSimpleName()))
				{
					aggregate = new AggregatePublicationConstraint(type, annotation);
					break;
				}
			}
			constraintCache.putTypeConstraints(type, aggregate);
		}

		return constraintCache.getTypeConstraints(type);
	}

	/**
	 * Collect all the constraints that may affect <code>type</code>, aggregating them according to fixed rules of:
	 * 
	 * <pre>
	 * 1. inheritance along hierarchy edges 
	 * 2. from a type to its contained methods
	 * </pre>
	 * 
	 * Nodes not declaring `inherited are skipped over as if they were not annotated. A node having `voidInheritance or
	 * having no declared elements truncates the path (allow all and deny all, respectively).
	 * 
	 * @throws JavaModelException
	 */
	AggregatePublicationConstraint collectTypeConstraints(ITypeHierarchy hierarchy) throws JavaModelException
	{
		AggregatePublicationConstraint directConstraints = parseTypeConstraints(hierarchy.getType());
		if ((directConstraints != null) && !directConstraints.traverse())
		{
			if (directConstraints.voidInheritance && directConstraints.isEmpty())
			{
				return null;
			}
			return directConstraints;
		}

		TypeScanPass pass = new TypeScanPass(hierarchy);
		for (IType supertype : hierarchy.getSupertypes(hierarchy.getType()))
		{
			collectTypeConstraints(supertype, pass);
		}

		if (directConstraints == null)
		{
			if (pass.applicableConstraints.isEmpty())
			{
				return null;
			}
			directConstraints = pass.applicableConstraints.remove(pass.applicableConstraints.size() - 1);
		}
		for (AggregatePublicationConstraint constraints : pass.applicableConstraints)
		{
			directConstraints.inheritFrom(constraints);
		}

		// TODO: the error will not know which @PublicationConstraint is causing a violation (if any)
		return directConstraints;
	}

	private void collectTypeConstraints(IType type, TypeScanPass fromBelow) throws JavaModelException
	{
		TypeScanPass goingAbove = fromBelow.copy();
		for (IType supertype : fromBelow.hierarchy.getSupertypes(type))
		{
			collectTypeConstraints(supertype, goingAbove);
		}

		AggregatePublicationConstraint constraintsForThisType = parseTypeConstraints(type);
		boolean traverse = true;
		if (constraintsForThisType != null)
		{
			traverse = constraintsForThisType.traverse();
			if (constraintsForThisType.applyConstraints())
			{
				fromBelow.applicableConstraints.add(constraintsForThisType);
				for (AggregatePublicationConstraint applicableConstraint : goingAbove.applicableConstraints)
				{
					constraintsForThisType.inheritFrom(applicableConstraint);
				}
			}
			else
			{
				constraintsForThisType = null;
			}
		}

		if (traverse && (constraintsForThisType == null))
		{
			// I have no constraints here, so I will pass along every constraint that I would inherit
			fromBelow.applicableConstraints.addAll(goingAbove.applicableConstraints);
		}
	}
}
