package org.hawkinssoftware.rns.core.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainSpecifications
{
	public static class OrthogonalSet
	{
		// WIP: need to validate the set: throw an error if hierarchically related domains are
		// specified as orthogonal
		final List<String> domainTypenamesAssembly = new ArrayList<String>();
		final List<String> packagePatternsAssembly = new ArrayList<String>();

		public final List<String> domainTypenames = Collections.unmodifiableList(domainTypenamesAssembly);
		public final List<String> packagePatterns = Collections.unmodifiableList(packagePatternsAssembly);
	}

	public static String getSpecificationFilename(String projectName)
	{
		return projectName + SPECIFICATION_FILENAME_SUFFIX;
	}
	
	public static final String SPECIFICATION_FILENAME_SUFFIX = ".domains.xml";

	final List<OrthogonalSet> orthogonalSetsAssembly = new ArrayList<OrthogonalSet>();
	final Map<String, String> parentDomainByChildDomain = new HashMap<String, String>();

	public final List<OrthogonalSet> orthogonalSets;

	public DomainSpecifications()
	{
		orthogonalSets = Collections.unmodifiableList(orthogonalSetsAssembly);
	}

	public String getParentDomain(String childDomainTypename)
	{
		return parentDomainByChildDomain.get(childDomainTypename);
	}

	public void append(DomainSpecifications addition)
	{
		orthogonalSetsAssembly.addAll(addition.orthogonalSetsAssembly);
		parentDomainByChildDomain.putAll(addition.parentDomainByChildDomain);
	}

	public void clear()
	{
		orthogonalSetsAssembly.clear();
		parentDomainByChildDomain.clear();
	}
}
