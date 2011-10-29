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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;
import org.hawkinssoftware.rns.core.util.UnknownEnumConstantException;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
@SuppressWarnings("restriction")
public class PublicationMethodConstraintCollector
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	enum Type
	{
		METHOD_CALL,
		METHOD_OVERRIDE,
		METHOD_PROXY;
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private class MethodScanPass
	{
		final List<AggregatePublicationConstraint> applicableConstraints = new ArrayList<AggregatePublicationConstraint>();
		final ITypeHierarchy hierarchy;
		final IMethod method;
		boolean foundMethodDeclaration = false;

		MethodScanPass(ITypeHierarchy hierarchy, IMethod target)
		{
			this.hierarchy = hierarchy;
			this.method = target;
		}

		MethodScanPass copy()
		{
			return new MethodScanPass(hierarchy, method);
		}
	}

	private final PublicationConstraintCache constraintCache = new PublicationConstraintCache();
	private final CollectionRuleProvider ruleProvider;
	private final Class<? extends Annotation> annotationType;
	private final PublicationTypeConstraintCollector typeConstraintCollector;

	PublicationMethodConstraintCollector(Type type, Class<? extends Annotation> annotationType)
	{
		switch (type)
		{
			case METHOD_CALL:
				this.ruleProvider = new MethodCallProvider();
				break;
			case METHOD_OVERRIDE:
				this.ruleProvider = new MethodOverrideProvider();
				break;
			case METHOD_PROXY:
				this.ruleProvider = new MethodProxyProvider();
				break;
			default:
				throw new UnknownEnumConstantException(type);
		}
		this.annotationType = annotationType;
		this.typeConstraintCollector = new PublicationTypeConstraintCollector(annotationType);
	}

	void typeChanged(IType type)
	{
		constraintCache.typeChanged(type);
		typeConstraintCollector.typeChanged(type);
	}

	String getConstraintName()
	{
		return annotationType.getSimpleName();
	}

	private AggregatePublicationConstraint parseMethodConstraints(IMethod method) throws JavaModelException
	{
		for (IAnnotation annotation : method.getAnnotations())
		{
			if (annotation.getElementName().contains(annotationType.getSimpleName()))
			{
				AggregatePublicationConstraint constraints = constraintCache.getMethodConstraints(method);
				if (constraints == null)
				{
					constraints = new AggregatePublicationConstraint(method.getDeclaringType(), annotation);
					constraintCache.putMethodConstraints(method, constraints);
				}
				return constraints;
			}
		}
		return null;
	}

	private IMethod getMethodDeclarationInType(IMethod method, IType inType, ITypeHierarchy hierarchy) throws JavaModelException
	{
		IMethod directDeclaration = inType.getMethod(method.getElementName(), method.getParameterTypes());
		if ((directDeclaration != null) && directDeclaration.exists())
		{
			return directDeclaration;
		}

		IMethod override = MethodChecks.overridesAnotherMethod(method, hierarchy);
		while (override != null)
		{
			if (override.getDeclaringType().getFullyQualifiedName().equals(inType.getFullyQualifiedName()))
			{
				return override;
			}
			override = MethodChecks.overridesAnotherMethod(override, hierarchy);
		}
		return null;
	}

	/**
	 * Collect all the constraints that may affect <code>method</code>, aggregating them according to fixed rules of:
	 * 
	 * <pre>
	 * 1. inheritance along hierarchy edges 
	 * 2. from a type to its contained methods
	 * </pre>
	 * 
	 * Nodes not declaring `inherited are skipped over as if they were not annotated. A node having `voidInheritance or
	 * having no declared elements truncates the path (allow all and deny all, respectively).
	 */
	AggregatePublicationConstraint collectMethodConstraints(ITypeHierarchy hierarchyOfReferredType, IMethod referredMethod) throws JavaModelException
	{
		if (referredMethod == null)
		{
			return ruleProvider.getDirectConstraints(hierarchyOfReferredType.getType(), null);
		}

		IMethod override = getMethodDeclarationInType(referredMethod, hierarchyOfReferredType.getType(), hierarchyOfReferredType);
		AggregatePublicationConstraint directConstraints = ruleProvider.getDirectConstraints(hierarchyOfReferredType.getType(), override);
		if ((directConstraints != null) && !directConstraints.traverse())
		{
			if (directConstraints.isEmpty())
			{
				return null;
			}
			return directConstraints;
		}

		MethodScanPass pass = new MethodScanPass(hierarchyOfReferredType, referredMethod);
		for (IType supertype : ruleProvider.getRelevantSupertypes(hierarchyOfReferredType, hierarchyOfReferredType.getType()))
		{
			collectMethodConstraints(supertype, pass);
		}

		// compound all the contraints with `inheritFrom()
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

	private void collectMethodConstraints(IType type, MethodScanPass fromBelow) throws JavaModelException
	{
		IMethod override = getMethodDeclarationInType(fromBelow.method, type, fromBelow.hierarchy);
		AggregatePublicationConstraint constraintsForThisMethod = ruleProvider.getDirectConstraints(type, override);

		MethodScanPass goingAbove = fromBelow.copy();
		for (IType supertype : ruleProvider.getRelevantSupertypes(fromBelow.hierarchy, type))
		{
			collectMethodConstraints(supertype, goingAbove);
		}

		if (goingAbove.foundMethodDeclaration || (override != null))
		{
			fromBelow.foundMethodDeclaration = true;
		}
		else
		{
			// the target method isn't here, so ignore all constraints in this branch
			return;
		}

		boolean traverse = true;
		if (constraintsForThisMethod != null)
		{
			traverse = constraintsForThisMethod.traverse();
			if (constraintsForThisMethod.applyConstraints())
			{
				fromBelow.applicableConstraints.add(constraintsForThisMethod);
				for (AggregatePublicationConstraint applicableConstraint : goingAbove.applicableConstraints)
				{
					constraintsForThisMethod.inheritFrom(applicableConstraint);
				}
			}
			else
			{
				constraintsForThisMethod = null;
			}
		}

		if (traverse && (constraintsForThisMethod == null))
		{
			// I have no constraints here, so I will pass along every constraint that I would inherit
			fromBelow.applicableConstraints.addAll(goingAbove.applicableConstraints);
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	interface CollectionRuleProvider
	{
		AggregatePublicationConstraint getDirectConstraints(IType type, IMethod method) throws JavaModelException;

		IType[] getRelevantSupertypes(ITypeHierarchy hierarchy, IType type);
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	class MethodCallProvider implements CollectionRuleProvider
	{
		@Override
		public AggregatePublicationConstraint getDirectConstraints(IType type, IMethod method) throws JavaModelException
		{
			AggregatePublicationConstraint directConstraints = null;
			if ((method != null) && method.exists())
			{
				directConstraints = parseMethodConstraints(method);
			}
			if (directConstraints == null)
			{
				directConstraints = typeConstraintCollector.parseTypeConstraints(type);
			}
			return directConstraints;
		}

		@Override
		public IType[] getRelevantSupertypes(ITypeHierarchy hierarchy, IType type)
		{
			return hierarchy.getSupertypes(type);
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	class MethodOverrideProvider implements CollectionRuleProvider
	{
		@Override
		public AggregatePublicationConstraint getDirectConstraints(IType type, IMethod method) throws JavaModelException
		{
			if (method != null)
			{
				return parseMethodConstraints(method);
			}
			return null;
		}

		@Override
		public IType[] getRelevantSupertypes(ITypeHierarchy hierarchy, IType type)
		{
			IType supertype = hierarchy.getSuperclass(type);
			if (supertype == null)
			{
				return new IType[0];
			}
			else
			{
				return new IType[] { supertype };
			}
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	class MethodProxyProvider implements CollectionRuleProvider
	{
		@Override
		public AggregatePublicationConstraint getDirectConstraints(IType type, IMethod method) throws JavaModelException
		{
			if (method != null)
			{
				return parseMethodConstraints(method);
			}
			return null;
		}

		@Override
		public IType[] getRelevantSupertypes(ITypeHierarchy hierarchy, IType type)
		{
			return hierarchy.getSupertypes(type);
		}
	}
}
