package org.hawkinssoftware.rns.core.collection;

public interface AccessValidatedCollection<CollectionType>
{
	static final boolean enabled = System.getProperty("disable-access-validation") == null;

	void setValidator(CollectionAccessValidator<CollectionType> validator);
}
