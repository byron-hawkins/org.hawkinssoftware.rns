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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.hawkinssoftware.rns.analysis.compile.domain.DomainRelationshipChecker;
import org.hawkinssoftware.rns.analysis.compile.source.JavaSourceParser;
import org.hawkinssoftware.rns.analysis.compile.source.ParsedJavaSource;
import org.hawkinssoftware.rns.analysis.compile.source.TypeHierarchyCache;
import org.hawkinssoftware.rns.analysis.compile.util.RNSBuildAnalyzerUtils;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class RNSBuildAnalyzer extends IncrementalProjectBuilder implements RNSAnalysisEngine.DependencyCollector
{
	public static RNSBuildAnalyzer getAnalyzer(IProject project)
	{
		return ALL_BUILDERS.get(project);
	}
	
	public static final String BUILDER_ID = "org.hawkinssoftware.rns.analysis.compile.builder";

	private static Map<IProject, RNSBuildAnalyzer> ALL_BUILDERS = new HashMap<IProject, RNSBuildAnalyzer>();

	IJavaProject javaProject;

	private final Map<String, ParsedJavaSource> parsedSources = new HashMap<String, ParsedJavaSource>();
	final Set<String> sourcesToAnalyze = new HashSet<String>(); // set of IType.getFullyQualifiedName()
	final Set<String> dependentTypesToAnalyze = new HashSet<String>(); // set of IType.getFullyQualifiedName()
	final Set<String> referredTypesToAnalyze = new HashSet<String>(); // set of IType.getFullyQualifiedName()

	private final DeltaVisitor deltaVisitor = new DeltaVisitor();
	JavaSourceParser parser;
	RNSAnalysisEngine engine;

	private final Task[] tasks = new Task[] { new RNSAnalysisTasks.ParseSources(), new RNSAnalysisTasks.AnalyzeSources(),
			new RNSAnalysisTasks.AnalyzeDependentTypes(), new RNSAnalysisTasks.AnalyzeReferredTypes() };

	private IProgressMonitor progressMonitor;

	public DomainRelationshipChecker getDomainRelationshipChecker()
	{
		return engine.domainRelationshipChecker;
	}
	
	@Override
	protected void startupOnInitialize()
	{
		super.startupOnInitialize();

		try
		{
			Log.addOutput(System.out);
			
			javaProject = JavaCore.create(getProject());
			parser = new JavaSourceParser(javaProject);
			engine = new RNSAnalysisEngine(javaProject, parser, this);
			parser.addListener(TypeHierarchyCache.getInstance().getSourceParserListener());
			
			ALL_BUILDERS.put(getProject(), this);

			try
			{
				requestFullReparse();
			}
			catch (CoreException e)
			{
				Log.out(Tag.CRITICAL, e, "Failed to request full reparse on project %s", getProject().getName());
			}

			for (Task task : tasks)
			{
				task.builder = this;
				task.initialize();
			}
		}
		catch (Throwable t)
		{
			Log.out(Tag.CRITICAL, t, "Failed to initialize the builder for project %s", getProject().getName());
		}
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException
	{
		requestFullReparse();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int buildKind, Map args, IProgressMonitor progressMonitor) throws CoreException
	{
		try
		{
			RNSBuildAnalyzerUtils.setStatusBarText("Executing RNS analysis of project %s", getProject().getName());
			Log.out(Tag.PUB_OPT, "Start RNS analysis of project %s", getProject().getName());

			this.progressMonitor = progressMonitor;

			try
			{
				analyzeProject(buildKind);
			}
			finally
			{
				sourcesToAnalyze.clear();
				dependentTypesToAnalyze.clear();
				referredTypesToAnalyze.clear();

				engine.buildFinished();
			}

			RNSBuildAnalyzerUtils.clearStatusBarText();
		}
		catch (Throwable t)
		{
			// WIP: how to keep the status error beyond the subsequent builds of the workspace?
			Log.out(Tag.CRITICAL, t, "Failed to analyze RNS declarations for project %s", getProject().getName());
			RNSBuildAnalyzerUtils.setStatusBarError("Failed to analyze RNS declarations for project %s: %s", getProject().getName(), t.getClass()
					.getSimpleName());
		}
		return null;
	}

	private void requestFullReparse() throws CoreException
	{
		for (IPackageFragment fragment : javaProject.getPackageFragments())
		{
			if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE)
			{
				for (ICompilationUnit source : fragment.getCompilationUnits())
				{
					parser.requestParsing(source);
					sourcesToAnalyze.add(source.getPrimary().findPrimaryType().getFullyQualifiedName());
				}
			}
		}
	}

	private void analyzeProject(int buildKind) throws CoreException
	{
		if (buildKind == FULL_BUILD)
		{
			requestFullReparse();
		}
		else
		{
			deltaVisitor.reset();
			IResourceDelta delta = getDelta(getProject());
			if (delta == null)
			{
				Log.out(Tag.INFO, "Building project %s: no sources changed.", getProject().getName());
			}
			else
			{
				delta.accept(deltaVisitor);
			}
		}

		if ((parser.getRequestCount() == 0) && sourcesToAnalyze.isEmpty() && dependentTypesToAnalyze.isEmpty() && referredTypesToAnalyze.isEmpty())
		{
			Log.out(Tag.INFO, "Building project %s: nothing to analyze.", getProject().getName());
			return;
		}

		Log.out(Tag.INFO, "Building project %s: %d sources to parse | %d sources changed | %d sources to analyze.", getProject().getName(),
				parser.getRequestCount(), deltaVisitor.changedSources, sourcesToAnalyze.size());

		analyze();
	}

	private void analyze() throws CoreException
	{
		int totalTicks = 0;
		for (Task task : tasks)
		{
			totalTicks += task.getTickCount();
		}

		progressMonitor.beginTask("", totalTicks);
		progressMonitor.setTaskName("Analyze RNS declarations in project " + getProject().getName());
		try
		{
			for (Task task : tasks)
			{
				task.start();
			}
		}
		finally
		{
			progressMonitor.done();
		}
	}

	@Override
	public void includeDependentType(IType type)
	{
		if (!type.getResource().exists())
		{
			Log.out(Tag.WARNING, "Skipping analysis of dependent type %s because its resource has been deleted.", type.getFullyQualifiedName());
			return;
		}

		IProject containingProject = type.getJavaProject().getProject();
		if (containingProject == null)
		{
			Log.out(Tag.WARNING, "Unable to find the project for dependent %s of %s", type.getFullyQualifiedName(), type.getFullyQualifiedName());
			return;
		}
		RNSBuildAnalyzer analyzer = ALL_BUILDERS.get(containingProject);
		if (analyzer == null)
		{
			// some dependent projects may not be RNS projects, and in this case the deltas are discarded
			return;
		}

		analyzer.dependentTypesToAnalyze.add(type.getFullyQualifiedName());
		analyzer.referredTypesToAnalyze.add(type.getFullyQualifiedName());

		for (IProject project : containingProject.getReferencingProjects())
		{
			RNSBuildAnalyzer referencingAnalyzer = ALL_BUILDERS.get(project);
			if (referencingAnalyzer == null)
			{
				// not an RNS project--no problem
				continue;
			}
			// I also need the types in the changed sources
			referencingAnalyzer.referredTypesToAnalyze.add(type.getFullyQualifiedName());
		}
	}
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	static abstract class Task
	{
		private IProgressMonitor progressMonitor;

		private RNSBuildAnalyzer builder;

		void initialize()
		{
			// hook
		}

		abstract int getTickCount();

		abstract void execute() throws CoreException, JavaModelException;

		abstract String describeTask();

		void worked(int ticks)
		{
			if (builder.progressMonitor.isCanceled())
			{
				throw new OperationCanceledException();
			}

			progressMonitor.worked(ticks);
		}

		RNSBuildAnalyzer getBuilder()
		{
			return builder;
		}

		private void start() throws CoreException, JavaModelException
		{
			if (builder.progressMonitor.isCanceled())
			{
				throw new OperationCanceledException();
			}

			progressMonitor = new SubProgressMonitor(builder.progressMonitor, getTickCount());
			try
			{
				Log.out(Tag.DEBUG, describeTask());

				builder.progressMonitor.setTaskName(describeTask());
				progressMonitor.beginTask(describeTask(), getTickCount());
				execute();
			}
			finally
			{
				progressMonitor.done();
			}
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private class DeltaVisitor implements IResourceDeltaVisitor
	{
		private int changedSources;

		void reset()
		{
			changedSources = 0;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException
		{
			IResource resource = delta.getResource();
			if (!((resource instanceof IFile) && resource.exists()))
			{
				// true: keep traversing in
				return true;
			}

			IFile file = (IFile) resource;
			if (file.getFileExtension() == null)
			{
				return true;
			}

			if (file.getFileExtension().equals("java"))
			{
				if (RNSBuildAnalyzerUtils.hasCompilationErrors(file))
				{
					Log.out(Tag.DEBUG, "Compilation errors exist on source %s; skipping RNS analysis.", file.getName());
				}
				else
				{
					visitJavaSource(file);
				}
			}
			else if (file.getFileExtension().equals("xml"))
			{
				if (RNSBuildAnalyzerUtils.hasErrors(file))
				{
					Log.out(Tag.DEBUG, "Errors exist on file %s; skipping RNS analysis.", file.getName());
				}
				else
				{
					visitMetaFile(file);
				}
			}

			// false: no need to continue traversing in (will return to the containing package and continue)
			return false;
		}

		private void visitJavaSource(IFile file)
		{
			ICompilationUnit source;
			if (file.getParent() instanceof IPackageFragment)
			{
				source = ((IPackageFragment) file.getParent()).getCompilationUnit(file.getName());
			}
			else
			{
				source = JavaCore.createCompilationUnitFrom(file);
			}

			parser.requestParsing(source);
			sourcesToAnalyze.add(source.getPrimary().findPrimaryType().getFullyQualifiedName());
			changedSources++;
		}

		private void visitMetaFile(IFile file)
		{
			engine.metaFileChanged(file);
		}
	}
}
