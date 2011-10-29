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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.hawkinssoftware.rns.analysis.compile.source.ParsedJavaSource;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public abstract class RNSAnnotationChecker
{
	public static String qualifyMarkerTypename(String problemMarkerTypeFragment)
	{
		return "org.hawkinssoftware.rns.analysis.compile." + problemMarkerTypeFragment + ".error";
	}

	protected final String problemMarkerType;

	protected ParsedJavaSource source;

	public RNSAnnotationChecker(String problemMarkerTypeFragment)
	{
		this.problemMarkerType = qualifyMarkerTypename(problemMarkerTypeFragment);
	}

	protected abstract void start() throws CoreException, JavaModelException;

	protected final void startAnalysis(ParsedJavaSource source) throws JavaModelException, CoreException
	{
		this.source = source;

		start();

		this.source = null;
	}
}
