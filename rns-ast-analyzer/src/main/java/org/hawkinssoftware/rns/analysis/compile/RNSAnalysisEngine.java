package org.hawkinssoftware.rns.analysis.compile;

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
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.hawkinssoftware.rns.analysis.compile.domain.DomainRelationshipChecker;
import org.hawkinssoftware.rns.analysis.compile.domain.DomainRoleChecker;
import org.hawkinssoftware.rns.analysis.compile.domain.DomainRoleTypeBinding;
import org.hawkinssoftware.rns.analysis.compile.publication.PublicationConstraintChecker;
import org.hawkinssoftware.rns.analysis.compile.source.JavaSourceParser;
import org.hawkinssoftware.rns.analysis.compile.source.ParsedJavaSource;
import org.hawkinssoftware.rns.analysis.compile.source.SourceReferenceInstruction;
import org.hawkinssoftware.rns.analysis.compile.source.TypeHierarchyCache;
import org.hawkinssoftware.rns.analysis.compile.util.TypeVisitor;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class RNSAnalysisEngine implements JavaSourceParser.Listener, DomainRoleTypeBinding.Cache.ChangeListener
{
	interface DependencyCollector
	{
		void includeDependentType(IType type);
	}

	/**
	 * Parsed sources mapped by `IType.getFullyQualifiedName() of the `ParsedJavaSource.source.findPrimaryType()
	 */
	private final Map<String, ParsedJavaSource> sourcesByTypename = new HashMap<String, ParsedJavaSource>();

	private final IJavaProject project;

	private final Set<String> domainChanges = new HashSet<String>();

	private final DefinedIdentityAnnotationChecker definedIdentityChecker = new DefinedIdentityAnnotationChecker();
	private final DomainRoleChecker domainRoleChecker = new DomainRoleChecker();
	final DomainRelationshipChecker domainRelationshipChecker;
	private final PublicationConstraintChecker publicationConstraintChecker = new PublicationConstraintChecker();

	private final DependencyCollector dependencyCollector;

	public RNSAnalysisEngine(IJavaProject project, JavaSourceParser parser, DependencyCollector dependencyCollector)
	{
		this.project = project;
		parser.addListener(this);
		domainRelationshipChecker = new DomainRelationshipChecker(project);
		this.dependencyCollector = dependencyCollector;
		DomainRoleTypeBinding.Cache.getInstance().addListener(this);
	}

	void buildFinished()
	{
		domainChanges.clear();
	}

	void metaFileChanged(IFile file)
	{
		if (domainRelationshipChecker.isSpecificationFile(file))
		{
			domainRelationshipChecker.refreshSpecifications();
		}
	}

	void analyzeSource(final String sourceTypename) throws CoreException
	{
		final ParsedJavaSource source = sourcesByTypename.get(sourceTypename);

		analyzePublicationConstraints(source, null);

		// TODO: if a type has been removed or renamed, it seems like there could be some obsolete content
		source.visitAllTypes(new TypeVisitor() {
			@Override
			public void visit(IType type) throws CoreException
			{
				ITypeHierarchy hierarchy = TypeHierarchyCache.getInstance().get(type.getFullyQualifiedName());
				if (hierarchy == null)
				{
					Log.out(Tag.WARNING, "Can't find hierarchy for type %s contained in %s", type.getFullyQualifiedName(), sourceTypename);
					return;
				}

				publicationConstraintChecker.typeChanged(type);
				domainRoleChecker.analyzeHierarchy(source, hierarchy);

				dependencyCollector.includeDependentType(hierarchy.getType());
				Log.out(Tag.DEBUG, "Include %s as dependent type (within source %s)", hierarchy.getType().getFullyQualifiedName(), source);
				for (IType subtype : hierarchy.getAllSubtypes(hierarchy.getType()))
				{
					Log.out(Tag.DEBUG, "  Include subtype %s as dependent type (within source %s)", subtype.getFullyQualifiedName(), source);
					dependencyCollector.includeDependentType(subtype);
				}
			}
		});
	}

	void analyzeDependentType(String dependentType) throws CoreException
	{
		ITypeHierarchy hierarchy = TypeHierarchyCache.getInstance().get(dependentType);
		if (hierarchy == null)
		{
			Log.out(Tag.WARNING, "Can't find hierarchy for dependency %s", dependentType);
			return;
		}

		ParsedJavaSource source = getSourceForMemberType(hierarchy.getType());
		if (source == null)
		{
			Log.out(Tag.WARNING, "Can't find source for dependent type %s", dependentType);
		}
		else
		{
			definedIdentityChecker.analyzeHierarchy(source, hierarchy);
			domainRelationshipChecker.analyzeHierarchy(source, hierarchy);

			if (domainChanges.contains(dependentType))
			{
				analyzePublicationConstraints(source, hierarchy.getType());
			}
		}
	}

	void analyzeReferredType(String referredType) throws CoreException
	{
		ITypeHierarchy hierarchy = TypeHierarchyCache.getInstance().get(referredType);
		if (hierarchy == null)
		{
			Log.out(Tag.WARNING, "Can't find hierarchy for referred type %s", referredType);
			return;
		}

		constrainPublication(hierarchy);
	}

	private void analyzePublicationConstraints(ParsedJavaSource source, IType containedType) throws CoreException
	{
		if (containedType == null)
		{
			source.deleteMarkers(PublicationConstraintChecker.MARKER_ID);
		}
		else
		{
			source.deleteMemberTypeMarkers(PublicationConstraintChecker.MARKER_ID, containedType.getFullyQualifiedName());
		}

		Multimap<ITypeHierarchy, SourceReferenceInstruction<?, ?>> referencesByReferredType = ArrayListMultimap.create();
		for (SourceReferenceInstruction<?, ?> reference : source.getAllReferences())
		{
			if ((containedType != null) && !containedType.getFullyQualifiedName().equals(reference.getContainingTypename()))
			{
				continue;
			}
			referencesByReferredType.put(TypeHierarchyCache.getInstance().establishHierarchy(reference.referredType), reference);
		}
		for (ITypeHierarchy hierarchy : referencesByReferredType.keySet())
		{
			publicationConstraintChecker.analyzeReferences(hierarchy, referencesByReferredType.get(hierarchy));
		}
	}

	private void constrainPublication(ITypeHierarchy hierarchy) throws CoreException
	{
		List<SourceReferenceInstruction<?, ?>> references = new ArrayList<SourceReferenceInstruction<?, ?>>();

		for (ParsedJavaSource source : sourcesByTypename.values())
		{
			if (!source.exists())
			{
				continue;
			}
			source.appendReferencesTo(hierarchy.getType(), references);
			source.deleteRelatedTypeMarkers(PublicationConstraintChecker.MARKER_ID, hierarchy.getType().getFullyQualifiedName());
		}

		publicationConstraintChecker.analyzeReferences(hierarchy, references);
	}

	private ParsedJavaSource getSourceForMemberType(IType type)
	{
		IType traversal = type;
		while (traversal.getDeclaringType() != null)
		{
			traversal = traversal.getDeclaringType();
		}
		return sourcesByTypename.get(traversal.getFullyQualifiedName());
	}

	@Override
	public void domainsChanged(String typename)
	{
		// may not be relevant to this project, but I am only using the list for lookup
		domainChanges.add(typename);
	}

	@Override
	public void sourceParsed(ICompilationUnit source, CompilationUnit ast)
	{
		IType type = source.findPrimaryType();

		ParsedJavaSource parsedSource = sourcesByTypename.get(type.getFullyQualifiedName());
		if (parsedSource == null)
		{
			parsedSource = new ParsedJavaSource(source, ast);
			sourcesByTypename.put(type.getFullyQualifiedName(), parsedSource);
		}
		else
		{
			parsedSource.update(source, ast);
		}

		try
		{
			parsedSource.visitAllTypes(new TypeVisitor() {
				@Override
				public void visit(IType type)
				{
					TypeHierarchyCache.getInstance().establishHierarchy(type);
					DomainRoleTypeBinding.Cache.getInstance().establishDomainRoles(TypeHierarchyCache.getInstance().get(type.getFullyQualifiedName()));
				}
			});
		}
		catch (CoreException e)
		{
			Log.out(Tag.CRITICAL, "Failed to generate the type hierarchy for all types of %s", type.getTypeQualifiedName());
		}
	}
}
