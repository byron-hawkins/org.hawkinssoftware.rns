package org.hawkinssoftware.rns.core.util;

import org.hawkinssoftware.rns.core.log.LogTag;

public class RNSLogging
{
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

	public static enum Mode
	{
		CRITICAL,
		WARNING,
		INFO,
		DEBUG;
	}

	public static enum Task
	{
		PUB_OPT,
		CONTAINMENT_CONSTRAINT;
	}

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
