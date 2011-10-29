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
package org.hawkinssoftware.rns.analysis.compile.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.hawkinssoftware.rns.analysis.compile.source.TypeHierarchyCache;
import org.hawkinssoftware.rns.analysis.compile.util.RNSBuildAnalyzerUtils;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.hawkinssoftware.rns.core.util.RNSUtils;

import com.google.common.collect.Multimap;

/**
 * For a particular build pass, no domain role type bindings may be accessed until all the domain roles for the
 * currently-building project and its dependencies have been resolved.
 * 
 * @author Byron Hawkins
 */
// TODO: check for circular @DomainRole.Join declarations
public class DomainRoleTypeBinding
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static class Cache
	{
		
		/**
		 * The listener interface for receiving change events. The class that is interested in processing a change event
		 * implements this interface, and the object created with that class is registered with a component using the
		 * component's <code>addChangeListener<code> method. When
		 * the change event occurs, that object's appropriate
		 * method is invoked.
		 * 
		 * @see ChangeEvent
		 */
		public interface ChangeListener
		{
			void domainsChanged(String typename);
		}

		private static final Cache INSTANCE = new Cache();

		private static final Set<DomainRoleTypeBinding> EMPTY = Collections.unmodifiableSet(new HashSet<DomainRoleTypeBinding>());

		public static Cache getInstance()
		{
			return INSTANCE;
		}

		private final Map<String, Set<DomainRoleTypeBinding>> domainRolesByParticipantTypename = new HashMap<String, Set<DomainRoleTypeBinding>>();
		private final Map<String, DomainRoleTypeBinding> domainRolesByTypename = new HashMap<String, DomainRoleTypeBinding>();

		// considering:
		private final Multimap<String, String> participantTypenamesByDomainRoleTypename = null;

		private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

		public void addListener(ChangeListener listener)
		{
			listeners.add(listener);
		}

		public void removeListener(ChangeListener listener)
		{
			listeners.remove(listener);
		}

		public void establishDomainRoles(ITypeHierarchy hierarchy)
		{
			boolean replaced = domainRolesByParticipantTypename.remove(hierarchy.getType().getFullyQualifiedName()) != null;
			establishDomainRoles(hierarchy.getType());
			for (IType type : hierarchy.getAllSupertypes(hierarchy.getType()))
			{
				establishDomainRoles(type);
			}

			if (replaced)
			{
				for (ChangeListener listener : listeners)
				{
					listener.domainsChanged(hierarchy.getType().getFullyQualifiedName());
				}
				for (IType type : hierarchy.getAllSubtypes(hierarchy.getType()))
				{
					for (ChangeListener listener : listeners)
					{
						listener.domainsChanged(type.getFullyQualifiedName());
					}
				}
			}
		}

		public DomainRoleTypeBinding getDomainRole(String fullyQualifiedTypename) throws JavaModelException
		{
			if (fullyQualifiedTypename == null)
			{
				return null;
			}
			
			DomainRoleTypeBinding role = domainRolesByTypename.get(fullyQualifiedTypename);
			if (role == null)
			{
				role = new DomainRoleTypeBinding(fullyQualifiedTypename);
				domainRolesByTypename.put(fullyQualifiedTypename, role);
			}
			return role;
		}

		public Set<DomainRoleTypeBinding> getDomainRoles(String typename)
		{
			Set<DomainRoleTypeBinding> roles = domainRolesByParticipantTypename.get(typename);
			if (roles == null)
			{
				return EMPTY;
			}
			else
			{
				return roles;
			}
		}

		private void establishDomainRoles(IType type)
		{
			if (domainRolesByParticipantTypename.containsKey(type.getFullyQualifiedName()))
			{
				return;
			}

			try
			{
				Set<DomainRoleTypeBinding> roles = new HashSet<DomainRoleTypeBinding>();
				for (IAnnotation annotation : type.getAnnotations())
				{
					if (annotation.getElementName().contains(RNSUtils.getPlainName(DomainRole.Join.class)))
					{
						if (annotation.getMemberValuePairs().length == 0)
						{
							Log.out(Tag.WARNING, "Warning: no membership declared for @DomainRole.Join in type %s.", type.getFullyQualifiedName());
							continue;
						}
						Object membership = annotation.getMemberValuePairs()[0].getValue();
						if (membership instanceof String)
						{
							addDomainRole(type, roles, (String) membership);
						}
						else
						{
							for (Object category : (Object[]) membership)
							{
								addDomainRole(type, roles, (String) category);
							}
						}
					}
				}
				domainRolesByParticipantTypename.put(type.getFullyQualifiedName(), roles);
			}
			catch (JavaModelException e)
			{
				Log.out(Tag.WARNING, "Failed to establish domain roles for type %s", type.getFullyQualifiedName());
			}
		}

		private void addDomainRole(IType annotatedType, Set<DomainRoleTypeBinding> roles, String membershipTypename) throws JavaModelException
		{
			String domainTypename = RNSBuildAnalyzerUtils.getFullyQualifiedTypename(annotatedType, membershipTypename);
			if (domainTypename == null)
			{
				// syntax errors exist, never mind
				return;
			}

			DomainRoleTypeBinding role = getDomainRole(domainTypename);
			roles.add(role);
		}
	}

	/**
	 * This tokens indicates to the engine that the domains of the enclosing type should be applied. It is assumed that
	 * the engine will acquire those types and will never put the `MY_DOMAINS token into any data structure.
	 */
	public static final DomainRoleTypeBinding MY_DOMAINS = new DomainRoleTypeBinding();

	private final String domainRoleType; // IType.getFullyQualifiedName()

	// for defining `MY_DOMAINS
	private DomainRoleTypeBinding()
	{
		domainRoleType = null;
	}

	private DomainRoleTypeBinding(String domainRoleType)
	{
		this.domainRoleType = domainRoleType;
	}

	public String getDomainTypename()
	{
		return domainRoleType;
	}

	public boolean isMember(String typename)
	{
		ITypeHierarchy hierarchy = TypeHierarchyCache.getInstance().get(typename);
		if (isImmediateMember(hierarchy.getType().getFullyQualifiedName()))
		{
			return true;
		}
		for (IType type : hierarchy.getAllSupertypes(hierarchy.getType()))
		{
			if (isImmediateMember(type.getFullyQualifiedName()))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isImmediateMember(String typename)
	{
		for (DomainRoleTypeBinding role : Cache.INSTANCE.getDomainRoles(typename))
		{
			if (isContainedIn(role))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isContainedIn(DomainRoleTypeBinding role)
	{
		if (role.domainRoleType.equals(this.domainRoleType))
		{
			return true;
		}
		
		ITypeHierarchy roleHierarchy = TypeHierarchyCache.getInstance().get(role.domainRoleType);
		IType roleType = roleHierarchy.getType();
		while (true)
		{
			roleType = roleHierarchy.getSuperclass(roleType);
			if ((roleType == null) || roleType.getFullyQualifiedName().equals(DomainRole.class.getCanonicalName()))
			{
				break;
			}
			if (roleType.getFullyQualifiedName().equals(this.domainRoleType))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainRoleType == null) ? 0 : domainRoleType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainRoleTypeBinding other = (DomainRoleTypeBinding) obj;
		if (domainRoleType == null)
		{
			if (other.domainRoleType != null)
				return false;
		}
		else if (!domainRoleType.equals(other.domainRoleType))
			return false;
		return true;
	}
}
