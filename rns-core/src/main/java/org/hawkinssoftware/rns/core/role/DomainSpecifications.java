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
package org.hawkinssoftware.rns.core.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class DomainSpecifications
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
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
