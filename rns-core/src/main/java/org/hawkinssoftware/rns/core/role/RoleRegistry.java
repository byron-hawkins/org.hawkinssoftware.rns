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
import java.util.List;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class RoleRegistry
{
	private static final TypeRole.Map roles = new TypeRole.Map();

	private static void register(Class<?> roleParticipant)
	{
		if (roleParticipant == System.class)
		{
			// hack, but it doesn't work
			return;
		}

		// not sure if this is a hack...
		if (roleParticipant == Object.class)
		{
			roles.add(new TypeRole(roleParticipant));
			return;
		}

		try
		{
			TypeRole role = new TypeRole(roleParticipant);
			List<Class<?>> unvisitedTypes = new ArrayList<Class<?>>();
			unvisitedTypes.add(roleParticipant);

			while (!unvisitedTypes.isEmpty())
			{
				Class<?> roleParticipantType = unvisitedTypes.remove(unvisitedTypes.size() - 1);
				DomainRole.Join registration = roleParticipantType.getAnnotation(DomainRole.Join.class);
				if (registration != null)
				{
					for (Class<? extends DomainRole> type : registration.membership())
					{
						DomainRole instance = DomainRole.Resolver.getInstance(type);
						role.join(instance);
					}

					// System.out.println("Registering " + role);
				}

				Class<?> superclass = roleParticipantType.getSuperclass();
				if (superclass != null)
				{
					unvisitedTypes.add(superclass);
				}
				for (Class<?> implemented : roleParticipantType.getInterfaces())
				{
					unvisitedTypes.add(implemented);
				}
			}

			roles.add(role);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to register participant " + roleParticipant.getName(), e);
		}
	}

	public static TypeRole getRole(Class<?> roleParticipant)
	{
		if (!roles.containsKey(roleParticipant))
		{
			register(roleParticipant);
		}
		return roles.get(roleParticipant);
	}
}
