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
package org.hawkinssoftware.rns.analysis.compile;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.ASTNode;
import org.hawkinssoftware.rns.analysis.compile.source.ParsedJavaSource;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public abstract class HierarchyAnnotationChecker extends RNSAnnotationChecker
{
	protected ITypeHierarchy hierarchy;

	public HierarchyAnnotationChecker(String problemMarkerTypeFragment)
	{
		super(problemMarkerTypeFragment);
	}

	protected abstract void startHierarchyAnalysis();

	@Override
	protected final void start() throws CoreException
	{
		deleteMarkers();

		startHierarchyAnalysis();
	}

	public final void analyzeHierarchy(ParsedJavaSource source, ITypeHierarchy hierarchy) throws CoreException
	{
		Log.out(Tag.PUB_OPT, "%s analyzes the hierarchy of %s", getClass().getSimpleName(), hierarchy.getType().getFullyQualifiedName());
		this.hierarchy = hierarchy;

		startAnalysis(source);

		this.hierarchy = null;
	}

	protected void createError(String message, ASTNode markerNode) throws CoreException
	{
		createMarker(message, IMarker.SEVERITY_ERROR, markerNode);
	}

	protected void createMarker(String message, int severity, ASTNode markerNode) throws CoreException
	{
		source.createMemberTypeMarker(problemMarkerType, message, severity, markerNode, hierarchy.getType().getFullyQualifiedName());
	}

	protected void deleteMarkers() throws CoreException
	{
		source.deleteMemberTypeMarkers(problemMarkerType, hierarchy.getType().getFullyQualifiedName());
	}
}
