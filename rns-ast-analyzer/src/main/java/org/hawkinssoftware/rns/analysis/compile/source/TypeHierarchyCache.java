package org.hawkinssoftware.rns.analysis.compile.source;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.hawkinssoftware.rns.analysis.compile.util.RNSBuildAnalyzerUtils;
import org.hawkinssoftware.rns.analysis.compile.util.TypeVisitor;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

// TODO: hierarchies will become obsolete on type deletion or name change, and this is somewhat hard to detect
public class TypeHierarchyCache
{
	public static TypeHierarchyCache getInstance()
	{
		return INSTANCE;
	}

	private static final TypeHierarchyCache INSTANCE = new TypeHierarchyCache();

	/**
	 * Type hierarchies mapped by `IType.getFullyQualifiedName() of the `ParsedJavaSource.source.findPrimaryType()
	 */
	private final Map<String, ITypeHierarchy> hierarchiesByTypename = new HashMap<String, ITypeHierarchy>();
	private final Set<String> modifiedHierarchies = new HashSet<String>();

	private final HierarchyListener listener = new HierarchyListener();

	private TypeHierarchyCache()
	{
	}

	public JavaSourceParser.Listener getSourceParserListener()
	{
		return listener;
	}

	public ITypeHierarchy get(String typename)
	{
		ITypeHierarchy hierarchy = hierarchiesByTypename.get(typename);
		if (modifiedHierarchies.contains(typename))
		{
			Log.out(Tag.DEBUG, "Refreshing type hierarchy for %s", typename);

			try
			{
				hierarchy.refresh(null);
			}
			catch (CoreException e)
			{
				hierarchiesByTypename.remove(typename);
				throw new RuntimeException(String.format("Failed to refresh the type hierarchy for type %s", typename));
			}

			modifiedHierarchies.remove(typename);
		}
		return hierarchy;
	}

	public void put(String typename, ITypeHierarchy hierarchy)
	{
		modifiedHierarchies.remove(typename);

		ITypeHierarchy existing = hierarchiesByTypename.get(typename);
		if (existing != null)
		{
			existing.removeTypeHierarchyChangedListener(listener);
		}
		hierarchiesByTypename.put(typename, hierarchy);
		hierarchy.addTypeHierarchyChangedListener(listener);
	}

	public ITypeHierarchy establishHierarchy(IType type)
	{
		try
		{ 
			ITypeHierarchy hierarchy = get(type.getFullyQualifiedName());
			if (hierarchy == null)
			{
				hierarchy = type.newTypeHierarchy(null);
				hierarchy.addTypeHierarchyChangedListener(listener);
				hierarchiesByTypename.put(type.getFullyQualifiedName(), hierarchy);
			}
			return hierarchy;
		}
		catch (CoreException e)
		{
			hierarchiesByTypename.remove(type.getFullyQualifiedName());
			Log.out(Tag.CRITICAL, e, "Failed to generate the type hierarchy for type %s", type.getTypeQualifiedName());
			return null;
		}
	}

	public void remove(String typename)
	{
		modifiedHierarchies.remove(typename);
		hierarchiesByTypename.remove(typename).removeTypeHierarchyChangedListener(listener);
	}

	private class HierarchyListener implements ITypeHierarchyChangedListener, JavaSourceParser.Listener
	{
		private final Set<String> changedTypenames = new HashSet<String>();

		@Override
		public void typeHierarchyChanged(ITypeHierarchy typeHierarchy)
		{
			boolean isRelevant = true;

			// TODO: analyzing relevance is very messy here, b/c a type can be added in a source, and that addition
			// won't appear in this old version of the `typeHierarchy

			// Could potentially compare types between `typeHierarchy and the last parsed source, so that when no
			// differences occur, hierarchy delta can be reduced to strict analysis of changed sources.

			// Solution A
			// For each type hierarchy reported:
			// 1. is the focal type in a changed source?
			//      yes: does it have the same subs and supers?
			//      yes: skip it
			//      no:  find all types relating to the focal type and mark them dirty 
			// --> (mark all, before and after refresh, to get all obsoletes and news)

			if (isRelevant)
			{
				modifiedHierarchies.add(typeHierarchy.getType().getFullyQualifiedName());
			}
			else
			{
				Log.out(Tag.PUB_OPT, "Skipping type hierarchy change reported for %s", typeHierarchy.getType().getTypeQualifiedName());
			}
		}

		@Override
		public void sourceParsed(ICompilationUnit source, CompilationUnit ast)
		{
			try
			{
				// Solution A
				// map each types with immediate subs and supers

				RNSBuildAnalyzerUtils.visitAllTypes(source, new TypeVisitor() {
					@Override
					public void visit(IType type) throws CoreException
					{
						changedTypenames.add(type.getFullyQualifiedName());
					}
				});
			}
			catch (CoreException e)
			{
				Log.out(Tag.CRITICAL, "Failed to register type changes for source change in %s (%s).", source.findPrimaryType().getTypeQualifiedName(),
						source.getPath());
			}
		}
	}
}
