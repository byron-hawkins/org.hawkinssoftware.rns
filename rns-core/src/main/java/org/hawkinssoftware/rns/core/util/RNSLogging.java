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
package org.hawkinssoftware.rns.core.util;

import org.hawkinssoftware.rns.core.log.LogTag;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class RNSLogging
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public enum TagCategory implements LogTag.Category
	{
		MODE(Mode.class),
		TASK(Task.class);

		private final Class<? extends Enum<?>> tokenType;

		private TagCategory(Class<? extends Enum<?>> tokenType)
		{
			this.tokenType = tokenType;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<Enum> getTokenType()
		{
			return (Class<Enum>) tokenType;
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static enum Mode
	{
		CRITICAL,
		WARNING,
		INFO,
		DEBUG;
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static enum Task
	{
		PUB_OPT,
		CONTAINMENT_CONSTRAINT;
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static class Tag
	{
		public static final LogTag<TagCategory> EVERYTHING = new LogTag<TagCategory>(TagCategory.class) {
			protected void initialize()
			{
				fill(TagCategory.MODE);
				fill(TagCategory.TASK);
			}
		};

		public static final LogTag<TagCategory> PUB_OPT = new LogTag<TagCategory>(TagCategory.class) {
			protected void initialize()
			{
				put(Task.PUB_OPT);
			}
		};

		public static final LogTag<TagCategory> CRITICAL = new LogTag<TagCategory>(TagCategory.class) {
			protected void initialize()
			{
				put(Mode.CRITICAL);
			}
		};
		public static final LogTag<TagCategory> WARNING = new LogTag<TagCategory>(TagCategory.class) {
			protected void initialize()
			{
				put(Mode.WARNING);
			}
		};
		public static final LogTag<TagCategory> DEBUG = new LogTag<TagCategory>(TagCategory.class) {
			protected void initialize()
			{
				put(Mode.DEBUG);
			}
		};
		public static final LogTag<TagCategory> INFO = new LogTag<TagCategory>(TagCategory.class) {
			protected void initialize()
			{
				put(Mode.INFO);
			}
		};
	}
}
