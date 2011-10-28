package org.hawkinssoftware.rns.core.role;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawkinssoftware.rns.core.util.EnumeratedProperties;
import org.hawkinssoftware.rns.core.util.EnumeratedProperties.PropertyStatus;
import org.hawkinssoftware.rns.core.util.RNSUtils;

public abstract class CommunicationRole
{
	private final EnumeratedProperties properties = new EnumeratedProperties();

	/**
	 * <pre>
	 * TODO: role property behavior needs to be specified in more detail:
	 *    1. Should there be a distinction between permanent and transitory properties of a role?
	 *    2. Should permanence be specified by the property domain (the enum)?
	 *    3. Should it be possible to override properties of joined roles?
	 *    4. Should inclusive properties be accumulated amongst all joined roles?
	 * </pre>
	 */
	final Set<DomainRole> membership = new HashSet<DomainRole>();

	public Set<DomainRole> getMembership()
	{
		return membership;
	}
	
	public boolean hasRole(DomainRole role)
	{
		for (DomainRole category : membership)
		{
			if (role.getClass().isAssignableFrom(category.getClass()))
			{
				return true;
			}

			if (category.hasRole(role))
			{
				return true;
			}
		}

		return false;
	}

	public <E extends Enum<E>> E getProperty(Class<E> key)
	{
		E value = (E) properties.getProperty(key);
		for (DomainRole category : membership)
		{
			E categoryValue = category.getProperty(key);
			if (categoryValue != null)
			{
				if (value == null)
				{
					value = categoryValue;
				}
				else
				{
					System.err.println("Warning: dulicate role property " + key + " found in " + RNSUtils.getPlainName(category.getClass()) + " of "
							+ RNSUtils.getPlainName(getClass()));
				}
			}
		}
		return value;
	}

	public <E extends Enum<E>> PropertyStatus getPropertyStatus(E queryValue)
	{
		@SuppressWarnings("unchecked")
		PropertyStatus status = properties.getPropertyStatus(queryValue);
		if (status != PropertyStatus.DOMAIN_ABSENT)
		{
			return status;
		}
		for (DomainRole category : membership)
		{
			status = category.getPropertyStatus(queryValue);
			if (status != PropertyStatus.DOMAIN_ABSENT)
			{
				return status;
			}
		}
		return PropertyStatus.DOMAIN_ABSENT;
	}

	public Map<Class<?>, Object> getProperties()
	{
		Map<Class<?>, Object> flatProperties = new HashMap<Class<?>, Object>();
		flattenProperties(flatProperties);
		return flatProperties;
	}

	void flattenProperties(Map<Class<?>, Object> flatProperties)
	{
		flatProperties.putAll(properties.getExclusivePropertyDomains());
		for (DomainRole category : membership)
		{
			category.flattenProperties(flatProperties);
		}
	}

	public <E extends Enum<E>> void setProperty(Class<E> key, E value)
	{
		properties.setProperty(key, value);
	}

	public void join(DomainRole role)
	{
		membership.add(role);
	}

	public void cede(DomainRole role)
	{
		membership.remove(role);
	}

	// TODO: do constraints provide convenient and basic enough orthogonality rejection for these conditions? Maybe I
	// should drop this concept of identity change
	public boolean allowsIdentityChange(CommunicationRole other)
	{
		for (DomainRole category : membership)
		{
			if (!category.allowsIdentityChange(other))
			{
				return false;
			}
		}
		return true;
	}

	public boolean allowsMessage(CommunicationRole receiver)
	{
		for (DomainRole category : membership)
		{
			if (!category.allowsMessage(receiver))
			{
				return false;
			}
		}
		return true;
	}
}
