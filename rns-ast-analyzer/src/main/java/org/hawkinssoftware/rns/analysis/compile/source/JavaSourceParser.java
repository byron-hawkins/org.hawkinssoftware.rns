package org.hawkinssoftware.rns.analysis.compile.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

public class JavaSourceParser extends ASTRequestor
{
	public interface Listener
	{
		void sourceParsed(ICompilationUnit source, CompilationUnit ast);
	}

	private final ASTParser parseEngine = ASTParser.newParser(AST.JLS3);

	private final IJavaProject project;
	private final Map<String, ICompilationUnit> requestsByPath = new HashMap<String, ICompilationUnit>();

	private final Set<IType> transitoryAddedTypes = new HashSet<IType>();
	private final Set<IType> transitoryRemovedTypes = new HashSet<IType>();

	private final List<Listener> listeners = new ArrayList<Listener>();

	public JavaSourceParser(IJavaProject project)
	{
		this.project = project;
	}

	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	public void requestParsing(IFile file)
	{
		requestsByPath.put(getSourcePath(file), JavaCore.createCompilationUnitFrom(file));
	}

	public void requestParsing(ICompilationUnit source)
	{
		requestsByPath.put(getSourcePath(source), source);
	}

	public int getRequestCount()
	{
		return requestsByPath.size();
	}

	public void parseRequests() throws CoreException
	{
		Log.out(Tag.PUB_OPT, "Parse requests: %s", requestsByPath.keySet());

		long start = System.currentTimeMillis();

		parseEngine.setResolveBindings(true);
		parseEngine.setStatementsRecovery(true);
		parseEngine.setBindingsRecovery(true);
		parseEngine.setIgnoreMethodBodies(false);
		parseEngine.setProject(project);
		parseEngine.createASTs(requestsByPath.values().toArray(new ICompilationUnit[0]), new String[0], this, null);

		Log.out(Tag.PUB_OPT, "Parsed %d sources in %dms", requestsByPath.size(), System.currentTimeMillis() - start);

		requestsByPath.clear();
	}

	@Override
	public void acceptAST(ICompilationUnit source, CompilationUnit ast)
	{
		for (Listener listener : listeners)
		{
			listener.sourceParsed(source, ast);
		}
	}

	private String getSourcePath(IFile file)
	{
		return "/" + file.getProject().getName() + "/" + file.getProjectRelativePath().toString();
	}

	private String getSourcePath(ICompilationUnit source)
	{
		return source.getPath().toString();
	}
}