package org.hawkinssoftware.rns.analysis.compile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.hawkinssoftware.rns.analysis.compile.source.ParsedJavaSource;

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
