package org.hawkinssoftware.rns.core.util;

public class UnknownEnumConstantException extends IllegalStateException
{
	public UnknownEnumConstantException(Enum<?> constant)
	{
		super("Unknown " + constant.getClass().getSimpleName() + " constant " + constant.name());
	}
}
