package org.hawkinssoftware.rns.core.collection;

public interface CollectionAccessValidator<CollectionType>
{
	void validateRead(CollectionType collection, String methodName, Object... args);

	void validateWrite(CollectionType collection, String methodName, Object... args);
}
