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

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.hawkinssoftware.rns.analysis.compile.RNSAnnotationChecker;
import org.hawkinssoftware.rns.analysis.compile.source.SourceReferenceInstruction;
import org.hawkinssoftware.rns.analysis.compile.source.TypeHierarchyCache;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.ExtensionConstraint;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.hawkinssoftware.rns.core.util.RNSUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Checks references to all types and methods for accessibility according to the developer-annotated rules
 * 
 * @VisibilityConstraint, @InvocationConstraint and @ExtensionConstraint.
 * 
 * @author Byron Hawkins
 */
public class PublicationConstraintChecker
{
	public static final String MARKER_ID = RNSAnnotationChecker.qualifyMarkerTypename("publication-constraint");

	private static final PublicationTypeConstraintCollector EXTENSION_COLLECTOR = new PublicationTypeConstraintCollector(ExtensionConstraint.class);
	private static final PublicationTypeConstraintCollector VISIBILITY_COLLECTOR = new PublicationTypeConstraintCollector(VisibilityConstraint.class);
	private static final PublicationMethodConstraintCollector OVERRIDE_COLLECTOR = new PublicationMethodConstraintCollector(
			PublicationMethodConstraintCollector.Type.METHOD_OVERRIDE, ExtensionConstraint.class);
	private static final PublicationMethodConstraintCollector INVOCATION_COLLECTOR = new PublicationMethodConstraintCollector(
			PublicationMethodConstraintCollector.Type.METHOD_CALL, InvocationConstraint.class);
	private static final PublicationMethodConstraintCollector PROXY_COLLECTOR = new PublicationMethodConstraintCollector(
			PublicationMethodConstraintCollector.Type.METHOD_PROXY, InvocationConstraint.class);

	private final PublicationConstraintUsageChecker referenceChecker = new PublicationConstraintUsageChecker();

	public void typeChanged(IType type)
	{
		EXTENSION_COLLECTOR.typeChanged(type);
		OVERRIDE_COLLECTOR.typeChanged(type);
		VISIBILITY_COLLECTOR.typeChanged(type);
		INVOCATION_COLLECTOR.typeChanged(type);
	}

	public void analyzeReferences(ITypeHierarchy hierarchyOfReferredType, Collection<SourceReferenceInstruction<?, ?>> references) throws CoreException
	{
		if (hierarchyOfReferredType == null)
		{
			// build errors exist, skip analysis
			return;
		}
		if (references.isEmpty())
		{
			return;
		}

		Log.out(Tag.PUB_OPT, "%s analyzes %d references to %s", getClass().getSimpleName(), references.size(), hierarchyOfReferredType.getType()
				.getFullyQualifiedName());

		AggregatePublicationConstraint visibilityConstraint = VISIBILITY_COLLECTOR.collectTypeConstraints(hierarchyOfReferredType);
		AggregatePublicationConstraint extensionConstraint = EXTENSION_COLLECTOR.collectTypeConstraints(hierarchyOfReferredType);

		Multimap<IMethodBinding, SourceReferenceInstruction<Expression, IMethodBinding>> referencesByMethod = ArrayListMultimap.create();

		for (SourceReferenceInstruction<?, ?> reference : references)
		{
			if (reference.getContainingTypename().equals(hierarchyOfReferredType.getType().getFullyQualifiedName()))
			{
				continue;
			}

			ICompilationUnit referredTypeSource = hierarchyOfReferredType.getType().getCompilationUnit();
			ICompilationUnit referenceSource = reference.getCompilationUnit();
			if ((referredTypeSource != null) && (referenceSource != null) && referredTypeSource.equals(referenceSource))
			{
				continue;
			}

			switch (reference.getKind())
			{
				case EXTENSION:
					if (extensionConstraint != null)
					{
						if (!referenceChecker.isValid(reference, extensionConstraint))
						{
							String message = String.format("Illegal extension: supertype %s has @%ss which are not met by class extending %s.",
									reference.displayName, ExtensionConstraint.class.getSimpleName(), RNSUtils.getPlainName(reference.getContainingTypename()));
							reference.createMarker(MARKER_ID, message, IMarker.SEVERITY_ERROR);
						}
					}
				case EXPRESSION:
				case TYPE_REFERENCE:
					if (visibilityConstraint != null)
					{
						if (!referenceChecker.isValid(reference, visibilityConstraint))
						{
							String message = String
									.format("Illegal reference: type %s has @%ss which are not met by referring class %s.", reference.displayName,
											VisibilityConstraint.class.getSimpleName(), RNSUtils.getPlainName(reference.getContainingTypename()));
							reference.createMarker(MARKER_ID, message, IMarker.SEVERITY_ERROR);
						}
					}
					break;
				case METHOD_CALL:
					@SuppressWarnings("unchecked")
					SourceReferenceInstruction<Expression, IMethodBinding> methodReference = (SourceReferenceInstruction<Expression, IMethodBinding>) reference;
					referencesByMethod.put(methodReference.instructionNodeBinding, methodReference);
					break;
				case METHOD_OVERRIDE:
					@SuppressWarnings("unchecked")
					SourceReferenceInstruction<MethodDeclaration, IMethodBinding> override = (SourceReferenceInstruction<MethodDeclaration, IMethodBinding>) reference;
					IMethod method = (IMethod) reference.instructionNodeBinding.getJavaElement();
					// WIP: should I use the overridden method instead, to avoid any confusion?
					if (method == null)
					{
						Log.out(Tag.WARNING, "Warning: method %s has no java element. Skipping analysis.", override.instructionNodeBinding.getName());
						continue;
					}
					AggregatePublicationConstraint overrideConstraint = OVERRIDE_COLLECTOR.collectMethodConstraints(hierarchyOfReferredType, method);
					if (overrideConstraint != null)
					{
						if (!referenceChecker.isValid(override, overrideConstraint))
						{
							String message = String.format("Illegal override: super method %s() has @%ss which are not met by overriding class %s.",
									override.displayName, ExtensionConstraint.class.getSimpleName(), RNSUtils.getPlainName(reference.getContainingTypename()));
							reference.createMarker(MARKER_ID, message, IMarker.SEVERITY_ERROR);
						}
					}
					break;
			}
		}

		for (IMethodBinding referredMethod : referencesByMethod.keySet())
		{
			IMethod method = (IMethod) referredMethod.getJavaElement();
			if (method == null)
			{
				Log.out(Tag.WARNING, "Warning: method %s has no java element. Analyzing type constraints only.", referredMethod.getName());
			}
			analyzeMethodInvocations(hierarchyOfReferredType, method, referencesByMethod.get(referredMethod));
		}
	}

	private void analyzeMethodInvocations(ITypeHierarchy hierarchyOfReferredType, IMethod referredMethod,
			Collection<SourceReferenceInstruction<Expression, IMethodBinding>> methodReferences) throws CoreException
	{
		AggregatePublicationConstraint publicationConstraint = INVOCATION_COLLECTOR.collectMethodConstraints(hierarchyOfReferredType, referredMethod);
		if (publicationConstraint == null)
		{
			return;
		}

		for (SourceReferenceInstruction<Expression, IMethodBinding> methodReference : methodReferences)
		{
			AggregatePublicationConstraint proxyConstraint = getProxyConstraint(methodReference);
			if (!referenceChecker.isValid(methodReference, publicationConstraint, proxyConstraint))
			{
				String message = String.format("Illegal invocation: method %s() has @%ss which are not met by class %s.", methodReference.displayName,
						InvocationConstraint.class.getSimpleName(), RNSUtils.getPlainName(methodReference.getContainingTypename()));
				methodReference.createMarker(MARKER_ID, message, IMarker.SEVERITY_ERROR);
			}
		}
	}

	private AggregatePublicationConstraint getProxyConstraint(SourceReferenceInstruction<Expression, IMethodBinding> methodReference) throws JavaModelException
	{
		ITypeHierarchy hierarchy = TypeHierarchyCache.getInstance().get(methodReference.getContainingTypename());
		if (hierarchy == null)
		{
			Log.out(Tag.WARNING, "Cannot find the proxy constraint for a call from %s because the hierarchy cannot be found.",
					methodReference.getContainingTypename());
			return null;
		}

		ASTNode parent = methodReference.instructionNode.getParent();
		while ((parent != null) && !(parent instanceof MethodDeclaration))
		{
			parent = parent.getParent();
		}
		if (parent == null)
		{
			return null;
		}

		IMethodBinding containingMethodBinding = ((MethodDeclaration) parent).resolveBinding();
		if (containingMethodBinding == null)
		{
			Log.out(Tag.WARNING, "Warning: method %s has no java element. Skipping proxy evaluation.", ((MethodInvocation) parent).getName());
			return null;
		}
		IMethod containingMethod = (IMethod) containingMethodBinding.getJavaElement();
		if (containingMethod == null)
		{
			Log.out(Tag.WARNING, "Warning: method %s has no java element. Skipping proxy evaluation.", ((MethodInvocation) parent).getName());
			return null;
		}

		return PROXY_COLLECTOR.collectMethodConstraints(hierarchy, containingMethod);
	}
}
