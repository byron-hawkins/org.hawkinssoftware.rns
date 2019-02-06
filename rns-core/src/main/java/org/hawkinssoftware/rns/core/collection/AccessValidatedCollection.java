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
package org.hawkinssoftware.rns.core.collection;

/**
 * An interface facilitating generic reference to a collection having an integrated
 * <code>CollectionAccessValidator</code>. The collection may be a <code>java.util.Collection</code> or a
 * <code>java.util.Map</code> or any other kind of collection.
 * 
 * @param <CollectionType>
 *            the specific kind of collection being wrapped
 * @author Byron Hawkins
 */
public interface AccessValidatedCollection<CollectionType>
{
	static final boolean enabled = System.getProperty("disable-access-validation") == null;

	void setValidator(CollectionAccessValidator<CollectionType> validator);
}
