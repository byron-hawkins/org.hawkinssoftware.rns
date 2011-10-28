package org.hawkinssoftware.rns.core.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Arrays;

import org.hawkinssoftware.rns.core.moa.ExecutionPath;
import org.hawkinssoftware.rns.core.moa.ExecutionStackFrame;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.util.RNSUtils;

@InvocationConstraint(domains = InitializationDomain.class)
public class Log
{
	private static class Writer
	{
		private LogTag<?> filter;
		private final OutputStream out;
		final BufferedWriter writer;
		final PrintStream printer;

		Writer(OutputStream out, LogTag<?> filter)
		{
			this.filter = filter;
			this.out = out;
			writer = new BufferedWriter(new OutputStreamWriter(out));
			printer = new PrintStream(out);
		}

		@SuppressWarnings("unchecked")
		<E extends Enum<E>> void write(LogTag<E> tag, String message)
		{
			if ((filter != null) && !((LogTag<E>) filter).includes(tag))
			{
				return;
			}

			try
			{
				writer.write(message);
				writer.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace(System.err);
			}
		}

		@SuppressWarnings("unchecked")
		<E extends Enum<E>> void printStackTrace(LogTag<E> tag, Throwable t)
		{
			if ((filter != null) && !((LogTag<E>) filter).includes(tag))
			{
				return;
			}

			t.printStackTrace(printer);
		}
	}

	private static Writer[] writers = new Writer[0];
	private static LogTag<?>[] tagFilters;

	private static Writer getWriter(OutputStream out)
	{
		for (int i = 0; i < writers.length; i++)
		{
			if (writers[i].out == out)
			{
				return writers[i];
			}
		}
		return null;
	}

	public static void addOutput(OutputStream out)
	{
		addOutput(out, null);
	}

	public static void addOutput(OutputStream out, LogTag<?> filter)
	{
		Writer existing = getWriter(out);
		if (existing != null)
		{
			if (filter != null)
			{
				if (existing.filter == null)
				{
					existing.filter = filter;
				}
				else
				{
					System.err.println("Warning: attempt to add an output with filter when that output already exists with filter.");
				}
			}
			return;
		}

		Writer[] expanded = new Writer[writers.length + 1];
		for (int i = 0; i < writers.length; i++)
		{
			expanded[i] = writers[i];
		}
		expanded[expanded.length - 1] = new Writer(out, filter);

		writers = expanded;
	}

	public static void removeOutput(OutputStream out)
	{
		if (getWriter(out) == null)
		{
			System.err.println("Failed to remove logger " + out + " because I don't have it now.");
			return;
		}

		Writer[] contracted = new Writer[writers.length - 1];
		if (contracted.length > 0)
		{
			int putIndex = 0;
			for (int getIndex = 0; getIndex < writers.length; getIndex++)
			{
				if (writers[getIndex].out != out)
				{
					contracted[putIndex++] = writers[getIndex];
				}
			}
		}

		writers = contracted;
	}

	public static void addTagFilter(LogTag<?> tagFilter)
	{
		if (tagFilters == null)
		{
			tagFilters = new LogTag<?>[] { tagFilter };
		}
		else
		{
			tagFilters = Arrays.copyOf(tagFilters, tagFilters.length + 1);
			tagFilters[tagFilters.length - 1] = tagFilter;
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> boolean passesFilter(LogTag<E> tag)
	{
		if (tagFilters == null)
		{
			return true;
		}

		for (LogTag<?> tagFilter : tagFilters)
		{
			if (tagFilter == (Object) tag)
			{
				return true;
			}
		}

		for (LogTag<?> tagFilter : tagFilters)
		{
			if (tag.override())
			{
				if (tag.includes((LogTag<E>) tagFilter))
				{
					return true;
				}
			}
			else
			{
				if (((LogTag<E>) tagFilter).includes(tag))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static <E extends Enum<E>> void outPastFilter(LogTag<E> tag, String message, Object... args)
	{
		String outputMessage;
		ExecutionStackFrame frame = ExecutionPath.getExecutionFrame();
		if (frame == null)
		{
			outputMessage = "";
		}
		else
		{
			String methodDescription = frame.getMethodDescription();
			int dotIndex = methodDescription.lastIndexOf('.');
			if (dotIndex >= 0)
			{
				methodDescription = methodDescription.substring(dotIndex + 1);
			}

			Class<?> type;
			if (frame.getReceiver() instanceof Class)
			{
				type = (Class<?>) frame.getReceiver();
			}
			else
			{
				type = frame.getReceiver().getClass();
			}
			outputMessage = RNSUtils.getPlainName(type) + "." + methodDescription + " | ";
		}

		if (args.length == 0)
		{
			outputMessage += message;
		}
		else
		{
			outputMessage += String.format(message, args);
		}
		outputMessage += "\n";

		for (int i = 0; i < writers.length; i++)
		{
			writers[i].write(tag, outputMessage);
		}
	}

	@InvocationConstraint(voidInheritance = true)
	public static <E extends Enum<E>> void out(LogTag<E> tag, String message, Object... args)
	{
		if (!passesFilter(tag))
		{
			return;
		}

		outPastFilter(tag, message, args);
	}

	@InvocationConstraint(voidInheritance = true)
	public static <E extends Enum<E>> void out(LogTag<E> tag, Throwable t, String message, Object... args)
	{
		if (!passesFilter(tag))
		{
			return;
		}

		outPastFilter(tag, message, args);
		outPastFilter(tag, t);
	}

	@InvocationConstraint(voidInheritance = true)
	public static <E extends Enum<E>> void out(LogTag<E> tag, Throwable t)
	{
		if (!passesFilter(tag))
		{
			return;
		}

		outPastFilter(tag, t);
	}

	private static <E extends Enum<E>> void outPastFilter(LogTag<E> tag, Throwable t)
	{
		for (int i = 0; i < writers.length; i++)
		{
			writers[i].printStackTrace(tag, t);
		}
	}
}
