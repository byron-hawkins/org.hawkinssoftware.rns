package org.hawkinssoftware.rns.core.test;

import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.log.LogTag;

public class LogTest
{
	private enum LogCategory implements LogTag.Category
	{
		MODE(Mode.class),
		TASK(Task.class);

		private final Class<? extends Enum<?>> tokenType;

		private LogCategory(Class<? extends Enum<?>> tokenType)
		{
			this.tokenType = tokenType;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class<Enum> getTokenType()
		{
			return (Class) tokenType;
		}
	}

	private enum Mode
	{
		CRITICAL,
		WARNING,
		INFO,
		DEBUG;
	}

	private enum Task
	{
		FIX_LOCKING,
		OPTIMIZE_SERVICES;
	}

	private static LogTag<LogCategory> EMPTY = new LogTag<LogCategory>(LogCategory.class);
	private static LogTag<LogCategory> FIX_LOCKING = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			put(Task.FIX_LOCKING);
		}
	};
	private static LogTag<LogCategory> OPTIMIZE_SERVICES = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			put(Task.OPTIMIZE_SERVICES);
		}
	};
	private static LogTag<LogCategory> EVERYTHING = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			fill(LogCategory.MODE);
		}
	};
	private static LogTag<LogCategory> BAD = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			put(Mode.CRITICAL);
			put(Mode.WARNING);
		}
	};
	private static LogTag<LogCategory> CRITICAL = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			put(Mode.CRITICAL);
		}
	};
	private static LogTag<LogCategory> WARNING = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			put(Mode.WARNING);
		}
	};
	private static LogTag<LogCategory> INFO = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			put(Mode.INFO);
		}
	};
	private static LogTag<LogCategory> DEBUG = new LogTag<LogCategory>(LogCategory.class) {
		@Override
		protected void initialize()
		{
			put(Mode.DEBUG);
		}
	};
	private static LogTag<LogCategory> DYNAMIC_DEBUG = new LogTag<LogCategory>(LogCategory.class) {
		protected boolean override()
		{
			return true;
		}

		public boolean includes(LogTag<LogCategory> tag)
		{
			return tag.get(Mode.class).contains(Mode.DEBUG);
		}
	};

	private void logStuff()
	{
		Log.out(CRITICAL, "Here is a critical message with no args.");
		Log.out(WARNING, "Here is a warning message with %d arg.", 1);
		Log.out(INFO, "Here is an %s message with %d args.", Mode.INFO, 2);
		Log.out(DYNAMIC_DEBUG, "Here is an %s message with %d args: %f.", Mode.DEBUG, 3, 3.3);

		Log.out(OPTIMIZE_SERVICES, "Service executing at %d", System.currentTimeMillis());
		Log.out(OPTIMIZE_SERVICES, "Service ran %d times in the last %dms", 37, 2000);

		Log.out(FIX_LOCKING, "Thread %s owns locks {%d, %d}", Thread.currentThread(), 3, 4);
		Log.out(FIX_LOCKING, "Reading %s with locks {%d}", "data", 2);
		Log.out(FIX_LOCKING, "Writing %s with locks {%d, %d}", "configuration", 2, 4);
	}

	private void logStuff(LogTag<LogCategory> tag, String tagName)
	{
		System.out.println();
		System.out.println(" ## Setting log filter to " + tagName);
		Log.addTagFilter(tag);
		logStuff();
	}

	void start()
	{
		Log.addOutput(System.out);

		Log.out(INFO, "ClassLoader: %s", getClass().getClassLoader());
		Log.out(INFO, "Log ClassLoader: %s", Log.class.getClassLoader());

		logStuff(BAD, "BAD");
		logStuff(INFO, "INFO");
		logStuff(DEBUG, "DEBUG");
		logStuff(EVERYTHING, "EVERYTHING");
		logStuff(OPTIMIZE_SERVICES, "OPTIMIZE_SERVICES");
		logStuff(FIX_LOCKING, "FIX_LOCKING");
	}
}
