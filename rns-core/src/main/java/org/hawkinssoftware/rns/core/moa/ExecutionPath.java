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
package org.hawkinssoftware.rns.core.moa;

import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawkinssoftware.rns.core.role.RoleRegistry;
import org.hawkinssoftware.rns.core.role.TypeRole;
import org.hawkinssoftware.rns.core.util.EnumeratedProperties;
import org.hawkinssoftware.rns.core.util.EnumeratedProperties.PropertyStatus;
import org.hawkinssoftware.rns.core.util.RNSUtils;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class ExecutionPath
{
	// TODO: would be nice to apply this to every contained type, but it's hard to find them in BCEL
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	@Target(value = { ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.CLASS)
	public @interface NoFrame
	{
	}

	/**
	 * An asynchronous update interface for receiving notifications about Stack information as the Stack is constructed.
	 */
	public interface StackObserver
	{
		void sendingMessage(TypeRole senderRole, TypeRole receiverRole, Object receiver, String messageDescription);

		void messageReturningFrom(TypeRole receiverRole, Object receiver);

		/**
		 * DOC comment task awaits.
		 * 
		 * @param <ObserverType>
		 *            the generic type
		 * @author Byron Hawkins
		 */
		public interface Factory<ObserverType extends StackObserver>
		{
			ObserverType create();

			Class<? extends ObserverType> getObserverType();
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private static class CurrentCaller
	{
		private Class<?> caller;
		private String callDescription;
	}

	private static final ThreadLocal<ExecutionPath> CURRENT_PATH = new ThreadLocal<ExecutionPath>() {
		@Override
		protected ExecutionPath initialValue()
		{
			return Universe.getInstance().createExecutionPath();
		}
 	};

	private static final ThreadLocal<CurrentCaller> CURRENT_CALLER = new ThreadLocal<CurrentCaller>() {
		@Override
		protected CurrentCaller initialValue()
		{
			return new CurrentCaller();
		}
	};

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static class Universe
	{
		public static Universe getInstance()
		{
			return INSTANCE;
		}

		static final Universe INSTANCE = new Universe();

		final List<ExecutionPath> paths = new ArrayList<ExecutionPath>();
		final List<StackObserver.Factory<?>> observers = new ArrayList<StackObserver.Factory<?>>();

		public synchronized void addObserver(StackObserver.Factory<?> observer)
		{
			observers.add(observer);

			for (int i = paths.size() - 1; i >= 0; i--)
			{
				if (!paths.get(i).thread.isAlive())
				{
					paths.remove(i);
				}
				paths.get(i).observers.add(observer.create());
			}
		}

		public synchronized void removeObserver(StackObserver.Factory<?> observer)
		{
			observers.remove(observer);

			for (int i = paths.size() - 1; i >= 0; i--)
			{
				ExecutionPath path = paths.get(i);
				if (!path.thread.isAlive())
				{
					paths.remove(i);
				}

				for (StackObserver pathObserver : path.observers)
				{
					if (pathObserver.getClass() == observer.getObserverType())
					{
						path.observers.remove(pathObserver);
						break;
					}
				}
			}
		}

		synchronized ExecutionPath createExecutionPath()
		{
			ExecutionPath path = new ExecutionPath();

			for (StackObserver.Factory<?> observer : observers)
			{
				path.observers.add(observer.create());
			}
			paths.add(path);
			return path;
		}
	}

	private final EnumeratedProperties properties = new EnumeratedProperties();
	private final Set<StackObserver> observers = new HashSet<StackObserver>();

	private final MethodInvocationStack<MessageStackFrame> messageStack = new MethodInvocationStack<MessageStackFrame>();
	private final ExecutionStack executionStack = new ExecutionStack();

	private boolean executingMetaMethod = false;

	private final Thread thread = Thread.currentThread();

	private static void addMessageFrame(MessageStackFrame frame)
	{
		CURRENT_PATH.get().messageStack.push(frame);
	}

	private static MessageStackFrame removeMessageFrame()
	{
		return CURRENT_PATH.get().messageStack.pop();
	}

	private static void addExecutionFrame(ExecutionStackFrame frame)
	{
		CURRENT_PATH.get().executionStack.push(frame);
	}

	private static ExecutionStackFrame removeExecutionFrame()
	{
		return CURRENT_PATH.get().executionStack.pop();
	}

	private static TypeRole lookupReceiverRole(Object receiverInstance)
	{
		Class<?> receiverType = (receiverInstance instanceof Class) ? (Class<?>) receiverInstance : receiverInstance.getClass();
		return RoleRegistry.getRole(receiverType);
	}

	public static ExecutionStackFrame getExecutionFrame()
	{
		ExecutionPath path = CURRENT_PATH.get();
		if (path.executionStack.isEmpty())
		{
			return null;
		}
		return path.executionStack.peek();
	}

	public static <ContextType extends ExecutionContext> void installExecutionContext(ExecutionContext.Key<ContextType> key, ContextType context)
	{
		CURRENT_PATH.get().executionStack.installContext(key, context);
	}

	public static <ContextType extends ExecutionContext> ContextType getExecutionContext(ExecutionContext.Key<ContextType> key)
	{
		return CURRENT_PATH.get().executionStack.getContext(key);
	}

	public static void removeExecutionContext(ExecutionContext.Key<?> key)
	{
		CURRENT_PATH.get().executionStack.removeContext(key);
	}

	public static boolean isStackEmpty()
	{
		return CURRENT_PATH.get().messageStack.isEmpty();
	}

	public static MessageStackFrame getCurrentFrame()
	{
		MethodInvocationStack<MessageStackFrame> stack = CURRENT_PATH.get().messageStack;
		if (stack.isEmpty())
		{
			return MessageStackFrame.EMPTY;
		}
		else
		{
			return stack.peek();
		}
	}

	public static TypeRole getReceiverRole()
	{
		return getCurrentFrame().getReceiverRole();
	}

	public static TypeRole getSenderRole()
	{
		return getCurrentFrame().getSenderRole();
	}

	public static void validateMessage(TypeRole senderRole, TypeRole receiverRole)
	{
		if ((senderRole == null) || (receiverRole == null))
		{
			// no concerns about messages to or from disinterested parties
			return;
		}

		// seems like calls within the same class should be allowable
		if (!senderRole.allowsMessage(receiverRole))
		{
			System.err.println("Warning, suspicious activity: role " + senderRole + " is sending messages to role " + receiverRole);
			// throw new IllegalArgumentException("Role " + senderRole + " is not allowed to send messages to role " +
			// receiverRole);
		}
	}

	public static void postCaller(Class<?> caller, String callDescription)
	{
		CurrentCaller currentCaller = CURRENT_CALLER.get();
		currentCaller.caller = caller;
		currentCaller.callDescription = callDescription;
	}

	public static String getCurrentCallDescription()
	{
		return CURRENT_CALLER.get().callDescription;
	}

	/**
	 * Invoked by instrumentation in the RNS Agent ClassTransformer
	 */
	public static TypeRole getCallerRole()
	{
		Class<?> senderType = CURRENT_CALLER.get().caller;
		if (senderType == null)
		{
			senderType = Object.class;
		}
		return RoleRegistry.getRole(senderType);
	}

	/**
	 * Invoked by instrumentation in the RNS Agent ClassTransformer
	 */
	public static TypeRole getCalleeRole(Class<?> receiverType)
	{
		return RoleRegistry.getRole(receiverType);
	}

	public static void pushInvocationFrame(Object receiver, String methodDescription)
	{
		CurrentCaller currentCaller = CURRENT_CALLER.get();
		ExecutionPath currentPath = CURRENT_PATH.get();

		if (currentPath.pushingFrame)
		{
			// path mechanics get no path features
			return;
		}
		currentPath.pushingFrame = true;

		ExecutionStackFrame executionFrame = new ExecutionStackFrame(receiver, methodDescription);
		addExecutionFrame(executionFrame);

		TypeRole senderRole = getCallerRole();

		TypeRole receiverRole = lookupReceiverRole(receiver);

		if (isInMetaMethod() || ((senderRole == null) && (receiverRole == null)))
		{
			// no message frame
			return;
		}

		if (senderRole == null)
		{
			senderRole = TypeRole.NONE;
		}
		if (receiverRole == null)
		{
			receiverRole = TypeRole.NONE;
		}

		for (StackObserver observer : getObservers())
		{
			observer.sendingMessage(senderRole, receiverRole, receiver, currentCaller.callDescription);
		}

		executionFrame.setHasMessageFrame(true);

		// System.out.println("<< Now executing an instrumented stack frame push: " + methodDescription +
		// " | SenderType: "
		// + currentCaller.caller.getClass().getName() + " | ReceiverType: " + receiverType.getName() + " >>");

		MessageStackFrame frame = new MessageStackFrame(currentCaller.callDescription, methodDescription, senderRole, receiverRole);
		currentPath.messageStack.push(frame);
		// addMessageFrame(frame);

		currentPath.pushingFrame = false;
	}

	public static void popInvocationFrame()
	{
		ExecutionPath currentPath = CURRENT_PATH.get();
		if (currentPath.pushingFrame)
		{
			// path mechanics get no path features
			return;
		}
		
		ExecutionStackFrame frame = currentPath.executionStack.pop();
		// ExecutionStackFrame frame = removeExecutionFrame();
		if ((frame != null) && frame.hasMessageFrame())
		{
			for (StackObserver observer : getObservers())
			{
				observer.messageReturningFrom(lookupReceiverRole(frame.getReceiver()), frame.getReceiver());
			}

			removeMessageFrame();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getMostRecentCaller(Class<T> callerType)
	{
		ExecutionPath path = CURRENT_PATH.get();
		for (ExecutionStackFrame frame : path.executionStack.iterateHistory())
		{
			Object receiver = frame.getReceiver();
			if ((receiver != null) && ((callerType == receiver.getClass()) || callerType.isAssignableFrom(receiver.getClass())))
			{
				return (T) receiver;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getPriorCaller(Class<T> callerType, T referenceInstance)
	{
		boolean foundReference = false;
		ExecutionPath path = CURRENT_PATH.get();
		for (ExecutionStackFrame frame : path.executionStack.iterateHistory())
		{
			Object receiver = frame.getReceiver();
			if ((receiver != null) && ((callerType == receiver.getClass()) || callerType.isAssignableFrom(receiver.getClass())))
			{
				if (receiver == referenceInstance)
				{
					foundReference = true;
				}
				else if (foundReference)
				{
					return (T) receiver;
				}
			}
		}
		return null;
	}

	private static boolean isInMetaMethod()
	{
		return CURRENT_PATH.get().executingMetaMethod;
	}

	public static void beginMetaMethod()
	{
		ExecutionPath path = CURRENT_PATH.get();
		if (path.executingMetaMethod)
		{
			throw new IllegalStateException("Attempt to start a meta method while already in a meta method.");
		}
		path.executingMetaMethod = true;
	}

	public static void endMetaMethod()
	{
		ExecutionPath path = CURRENT_PATH.get();
		if (!path.executingMetaMethod)
		{
			throw new IllegalStateException("Attempt to end a meta method while not in a meta method.");
		}
		path.executingMetaMethod = false;
	}

	private static Set<StackObserver> getObservers()
	{
		return CURRENT_PATH.get().observers;
	}

	public static void addObserver(StackObserver observer)
	{
		getObservers().add(observer);
	}

	public static void removeObserver(StackObserver observer)
	{
		getObservers().remove(observer);
	}

	public static <E extends Enum<E>> E getProperty(Class<E> key)
	{
		return CURRENT_PATH.get().properties.getProperty(key);
	}

	//@SafeVarargs
	public static <E extends Enum<E>> PropertyStatus getPropertyStatus(E... queryValue)
	{
		return CURRENT_PATH.get().properties.getPropertyStatus(queryValue);
	}

	public static <E extends Enum<E>> void setProperty(Class<E> key, E value)
	{
		CURRENT_PATH.get().properties.setProperty(key, value);
	}

	public static <E extends Enum<E>> void clearProperty(Class<E> key, E value)
	{
		CURRENT_PATH.get().properties.clearProperty(key, value);
	}

	public static <E extends Enum<E>> void clearDomain(Class<E> key)
	{
		CURRENT_PATH.get().properties.clearDomain(key);
	}

	public static void changeRole(Class<?> newRoleType)
	{
		TypeRole newRole = RoleRegistry.getRole(newRoleType);
		if (getReceiverRole().allowsIdentityChange(newRole))
		{
			getCurrentFrame().setReceiverRole(newRole);
		}
		else
		{
			System.err.println("Warning: rejecting attempt to change roles from " + getReceiverRole() + " to " + newRoleType);
		}
	}

	public static void printStack(PrintStream out, String methodName, Class<?> type)
	{
		MethodInvocationStack<MessageStackFrame> stack = CURRENT_PATH.get().messageStack;
		for (int i = stack.stack.size() - 1; i >= 0; i--)
		{
			MessageStackFrame frame = stack.stack.get(i);
			out.println(frame.getInvocationDescription() + " | " + frame.getMethodDescription());
			out.println("        {sender: " + frame.getSenderRole() + "; receiver: " + frame.getReceiverRole() + "}");
		}
		out.println();
	}

	public static void printReceiverRole(PrintStream out, String methodName, Class<?> type)
	{
		out.println(RNSUtils.getPlainName(type) + "." + methodName + "() as " + getReceiverRole());
	}

	private boolean pushingFrame = false;
}
