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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.xml.sax.SAXException;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class DomainSpecificationRegistry
{
	public static DomainSpecificationRegistry getInstance()
	{
		return INSTANCE;
	}

	private static final DomainSpecificationRegistry INSTANCE = new DomainSpecificationRegistry();

	private final List<DomainRole.OrthogonalSet> orthogonalSetsAssembly = new ArrayList<DomainRole.OrthogonalSet>();

	public final List<DomainRole.OrthogonalSet> orthogonalSets;

	private DomainSpecificationRegistry()
	{
		orthogonalSets = Collections.unmodifiableList(orthogonalSetsAssembly);
	}

	public void register(InputStream specificationSource) throws SAXException, IOException, ParserConfigurationException
	{
		DomainSpecifications specifications = DomainSpecificationsLoader.load(specificationSource);

		for (DomainSpecifications.OrthogonalSet orthogonalTypenameSet : specifications.orthogonalSets)
		{
			DomainRole.OrthogonalSet orthogonalRoleSet = new DomainRole.OrthogonalSet();

			orthogonalRoleSet.packagePatternsAssembly.addAll(orthogonalTypenameSet.packagePatterns);

			for (String domainTypename : orthogonalTypenameSet.domainTypenames)
			{
				try
				{
					@SuppressWarnings("unchecked")
					Class<? extends DomainRole> domainType = (Class<? extends DomainRole>) Class.forName(domainTypename);
					orthogonalRoleSet.domainsAssembly.add(DomainRole.Resolver.getInstance(domainType));
				}
				catch (Exception e)
				{
					Log.out(Tag.CRITICAL, e, "Failed to find the instance for DomainRole %s", domainTypename);
				}
			}

			orthogonalSetsAssembly.add(orthogonalRoleSet);
		}
	}

	// WIP: evaluate package membership
	public CollaborationEvaluation evaluateCollaboration(TypeRole first, TypeRole second)
	{
		CollaborationEvaluation evaluation = new CollaborationEvaluation();

		Map<DomainRole.OrthogonalSet, OrthogonalSetMember> firstOrthogonalMembers = collectOrthogonalMembership(first);
		Map<DomainRole.OrthogonalSet, OrthogonalSetMember> secondOrthogonalMembers = collectOrthogonalMembership(second);

		for (Map.Entry<DomainRole.OrthogonalSet, OrthogonalSetMember> firstEntry : firstOrthogonalMembers.entrySet())
		{
			OrthogonalSetMember secondMember = secondOrthogonalMembers.get(firstEntry.getKey());
			if (secondMember == null)
			{
				continue;
			}

			if (firstEntry.getValue().orthogonalDomain != secondMember.orthogonalDomain)
			{
				evaluation.addConflict(firstEntry.getValue().typeDomain, secondMember.typeDomain);
			}
		}

		return evaluation;
	}

	private Map<DomainRole.OrthogonalSet, OrthogonalSetMember> collectOrthogonalMembership(TypeRole typeRole)
	{
		Map<DomainRole.OrthogonalSet, OrthogonalSetMember> orthogonalMembers = new HashMap<DomainRole.OrthogonalSet, OrthogonalSetMember>();
		orthogonals: for (DomainRole.OrthogonalSet orthogonalSet : orthogonalSets)
		{
			for (DomainRole firstRole : typeRole.membership)
			{
				for (DomainRole orthogonalRole : orthogonalSet.domains)
				{
					if (orthogonalRole.getClass().isAssignableFrom(firstRole.getClass()))
					{
						orthogonalMembers.put(orthogonalSet, new OrthogonalSetMember(typeRole, firstRole, orthogonalRole));
						continue orthogonals;
					}
				}
			}
		}
		return orthogonalMembers;
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static class CollaborationEvaluation
	{
		
		/**
		 * DOC comment task awaits.
		 * 
		 * @author Byron Hawkins
		 */
		public class Conflict
		{
			public final DomainRole first;
			public final DomainRole second;

			Conflict(DomainRole first, DomainRole second)
			{
				this.first = first;
				this.second = second;
			}
		}

		private final List<Conflict> conflictAssembly = new ArrayList<Conflict>();
		public final List<Conflict> conflicts;

		CollaborationEvaluation()
		{
			conflicts = Collections.unmodifiableList(conflictAssembly);
		}

		void addConflict(DomainRole firstDomain, DomainRole secondDomain)
		{
			conflictAssembly.add(new Conflict(firstDomain, secondDomain));
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private static class OrthogonalSetMember
	{
		final TypeRole typeRole;
		final DomainRole typeDomain;
		final DomainRole orthogonalDomain;

		public OrthogonalSetMember(TypeRole typeRole, DomainRole typeDomain, DomainRole orthogonalDomain)
		{
			this.typeRole = typeRole;
			this.typeDomain = typeDomain;
			this.orthogonalDomain = orthogonalDomain;
		}
	}
}
