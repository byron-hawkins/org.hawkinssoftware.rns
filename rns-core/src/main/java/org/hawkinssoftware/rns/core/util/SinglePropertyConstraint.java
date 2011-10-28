package org.hawkinssoftware.rns.core.util;

import java.util.EnumSet;
import java.util.Set;

import org.hawkinssoftware.rns.core.moa.ExecutionPath;
import org.hawkinssoftware.rns.core.role.TypeRole;

public class SinglePropertyConstraint<E extends Enum<E>> implements ExecutionPath.StackObserver
{
	public static final Set<EnumeratedProperties.PropertyStatus> EXCLUDED = EnumSet.of(EnumeratedProperties.PropertyStatus.DOMAIN_ABSENT,
			EnumeratedProperties.PropertyStatus.NO_MATCH);
	public static final Set<EnumeratedProperties.PropertyStatus> REQUIRED_UNIQUE = EnumSet.of(EnumeratedProperties.PropertyStatus.EXACT_MATCH);
	public static final Set<EnumeratedProperties.PropertyStatus> REQUIRED = EnumSet.of(EnumeratedProperties.PropertyStatus.EXACT_MATCH,
			EnumeratedProperties.PropertyStatus.PARTIAL_MATCH);

	private final E property;
	private final Set<EnumeratedProperties.PropertyStatus> allowedStatuses;

	public SinglePropertyConstraint(E property, Set<EnumeratedProperties.PropertyStatus> allowedStatuses)
	{
		this.property = property;
		this.allowedStatuses = allowedStatuses;
	}

	@Override
	public void sendingMessage(TypeRole senderRole, TypeRole receiverRole, Object receiver, String messageDescription)
	{
		if (!allowedStatuses.contains(receiverRole.getPropertyStatus(property)))
		{
			throw new IllegalStateException("Property status " + receiverRole.getPropertyStatus(property) + " is not allowed for property "
					+ RNSUtils.getPlainName(property.getClass()) + "." + property.name() + " in the current execution path.");
		}
	}

	@Override
	public void messageReturningFrom(TypeRole receiverRole, Object receiver)
	{
	}
}
