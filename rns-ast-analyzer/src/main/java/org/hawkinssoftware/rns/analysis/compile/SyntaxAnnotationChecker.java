package org.hawkinssoftware.rns.analysis.compile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.hawkinssoftware.rns.analysis.compile.source.ParsedJavaSource;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

public abstract class SyntaxAnnotationChecker extends RNSAnnotationChecker
{
	public SyntaxAnnotationChecker(String problemMarkerTypeFragment)
	{
		super(problemMarkerTypeFragment);
	}

	public final void analyzeSyntax(ParsedJavaSource source) throws CoreException
	{
		Log.out(Tag.PUB_OPT, "%s analyzes the syntax of %s", getClass().getSimpleName(), source);

		deleteAllMarkers();
		startAnalysis(source);
	}

	protected void createError(String message, ASTNode markerNode, String relatedTypename) throws CoreException
	{
		// createMarker(message, IMarker.SEVERITY_ERROR, markerNode, relatedTypename);
	}

	protected void createMarker(String message, int severity, ASTNode markerNode, String relatedTypename) throws CoreException
	{
		// source.createMarker(problemMarkerType, message, severity, markerNode, relatedTypename);
	}

	protected void deleteAllMarkers() throws CoreException
	{
		// source.deleteMarkers(problemMarkerType);
	}
}
