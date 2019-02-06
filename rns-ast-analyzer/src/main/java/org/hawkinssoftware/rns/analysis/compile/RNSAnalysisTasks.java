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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.hawkinssoftware.rns.analysis.compile.source.JavaSourceParser;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
interface RNSAnalysisTasks
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	static class ParseSources extends RNSBuildAnalyzer.Task implements JavaSourceParser.Listener
	{
		private static final int TICKS_PER_SOURCE = 15;

		@Override
		void initialize()
		{
			getBuilder().parser.addListener(this);
		}

		@Override
		int getTickCount()
		{
			return TICKS_PER_SOURCE * getBuilder().parser.getRequestCount();
		}

		@Override
		String describeTask()
		{
			return "Parse " + getBuilder().parser.getRequestCount() + " Java sources.";
		}

		@Override
		void execute() throws CoreException
		{
			getBuilder().parser.parseRequests();
		}

		@Override
		public void sourceParsed(ICompilationUnit source, CompilationUnit ast)
		{
			worked(TICKS_PER_SOURCE);
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	static class AnalyzeSources extends RNSBuildAnalyzer.Task
	{
		private static final int TICKS_PER_SOURCE = 10;

		@Override
		String describeTask()
		{
			return "Analyze " + getBuilder().sourcesToAnalyze.size() + " sources for RNS declarations.";
		}

		@Override
		int getTickCount()
		{
			return TICKS_PER_SOURCE * getBuilder().sourcesToAnalyze.size();
		}

		@Override
		void execute() throws CoreException
		{
			for (String sourceTypename : getBuilder().sourcesToAnalyze)
			{
				getBuilder().engine.analyzeSource(sourceTypename);
				worked(TICKS_PER_SOURCE);
			}
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	static class AnalyzeDependentTypes extends RNSBuildAnalyzer.Task
	{
		private static final int TICKS_PER_TYPE = 2;

		@Override
		String describeTask()
		{
			return "Analyze " + getBuilder().dependentTypesToAnalyze.size() + " dependent types.";
		}

		@Override
		int getTickCount()
		{
			return TICKS_PER_TYPE * getBuilder().dependentTypesToAnalyze.size();
		}

		@Override
		void execute() throws CoreException
		{
			for (String dependentType : getBuilder().dependentTypesToAnalyze)
			{
				getBuilder().engine.analyzeDependentType(dependentType);
				worked(TICKS_PER_TYPE);
			}
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	static class AnalyzeReferredTypes extends RNSBuildAnalyzer.Task
	{
		private static final int TICKS_PER_TYPE = 1;

		@Override
		String describeTask()
		{
			return "Analyze " + getBuilder().referredTypesToAnalyze.size() + " referred types.";
		}

		@Override
		int getTickCount()
		{
			return TICKS_PER_TYPE * getBuilder().referredTypesToAnalyze.size();
		}

		@Override
		void execute() throws CoreException
		{
			for (String referredType : getBuilder().referredTypesToAnalyze)
			{
				getBuilder().engine.analyzeReferredType(referredType);
				worked(TICKS_PER_TYPE);
			}
		}
	}
}
