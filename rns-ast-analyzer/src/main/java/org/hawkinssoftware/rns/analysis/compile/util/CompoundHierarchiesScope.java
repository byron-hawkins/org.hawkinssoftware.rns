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
package org.hawkinssoftware.rns.analysis.compile.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class CompoundHierarchiesScope implements IJavaSearchScope
{
	public static CompoundHierarchiesScope create(ICompilationUnit unit) throws CoreException
	{
		final List<IJavaSearchScope> hierarchyScopes = new ArrayList<IJavaSearchScope>();
		RNSBuildAnalyzerUtils.visitAllTypes(unit, new TypeVisitor() {
			@Override
			public void visit(IType type) throws CoreException
			{
				hierarchyScopes.add(SearchEngine.createHierarchyScope(type));
			}
		});
		return new CompoundHierarchiesScope(hierarchyScopes);
	}

	private final List<IJavaSearchScope> hierarchyScopes;

	private CompoundHierarchiesScope(List<IJavaSearchScope> hierarchyScopes)
	{
		this.hierarchyScopes = hierarchyScopes;
	}

	@Override
	public boolean encloses(String resourcePath)
	{
		for (IJavaSearchScope scope : hierarchyScopes)
		{
			if (scope.encloses(resourcePath))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean encloses(IJavaElement element)
	{
		for (IJavaSearchScope scope : hierarchyScopes)
		{
			if (scope.encloses(element))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IPath[] enclosingProjectsAndJars()
	{
		List<IPath> allPaths = new ArrayList<IPath>();
		for (IJavaSearchScope scope : hierarchyScopes)
		{
			for (IPath path : scope.enclosingProjectsAndJars())
			{
				allPaths.add(path);
			}
		}
		return allPaths.toArray(new IPath[0]);
	}

	@Override
	@Deprecated
	public boolean includesBinaries()
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean includesClasspaths()
	{
		return false;
	}

	@Override
	@Deprecated
	public void setIncludesBinaries(boolean includesBinaries)
	{
	}

	@Override
	@Deprecated
	public void setIncludesClasspaths(boolean includesClasspaths)
	{
	}
}
