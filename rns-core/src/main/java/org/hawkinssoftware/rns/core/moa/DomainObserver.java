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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainSpecificationRegistry;
import org.hawkinssoftware.rns.core.role.TypeRole;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.hawkinssoftware.rns.core.util.RNSUtils;

/**
 * An asynchronous update interface for receiving notifications about Domain information as the Domain is constructed.
 */
public class DomainObserver implements ExecutionPath.StackObserver
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public interface Listener
	{
		void orthogonalityViolation(DomainSpecificationRegistry.CollaborationEvaluation evaluation);
	}

	// WIP: better listener handling (complicated with the distribution of observer instances...)
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	public static class Factory implements ExecutionPath.StackObserver.Factory<DomainObserver>
	{
		private final Listener listener;

		public Factory(Listener listener)
		{
			this.listener = listener;
		}

		@Override
		public DomainObserver create()
		{
			DomainObserver observer = new DomainObserver();
			observer.listeners.add(listener);
			return observer;
		}

		@Override
		public Class<? extends DomainObserver> getObserverType()
		{
			return DomainObserver.class;
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private class Frame
	{
		final TypeRole receiverRole;
		int depth = 0;

		public Frame(TypeRole receiverRole)
		{
			this.receiverRole = receiverRole;
		}
	}

	private final Map<TypeRole, Frame> framesByTypeRole = new HashMap<TypeRole, Frame>();

	private final List<Listener> listeners = new ArrayList<Listener>();

	@Override
	public void sendingMessage(TypeRole senderRole, TypeRole receiverRole, Object receiver, String messageDescription)
	{
		for (TypeRole type : framesByTypeRole.keySet())
		{
			if (type == receiverRole)
			{
				continue;
			}

			if (type.getType().isAssignableFrom(receiverRole.getType()) || receiverRole.getType().isAssignableFrom(type.getType()))
			{
				continue;
			}

			DomainSpecificationRegistry.CollaborationEvaluation evaluation = DomainSpecificationRegistry.getInstance().evaluateCollaboration(type, receiverRole);
			if (!evaluation.conflicts.isEmpty())
			{
				for (Listener listener : listeners)
				{
					listener.orthogonalityViolation(evaluation);
				}
			}
		}

		Frame frame = framesByTypeRole.get(receiverRole);
		if (frame == null)
		{
			frame = new Frame(receiverRole);
			framesByTypeRole.put(receiverRole, frame);
		}
		else
		{
			frame.depth++;
		}
	}

	@Override
	public void messageReturningFrom(TypeRole receiverRole, Object receiver)
	{
		if (framesByTypeRole.size() > 15)
		{
			Log.out(Tag.DEBUG, "Large stack: %s", this);
		}

		Frame frame = framesByTypeRole.get(receiverRole);
		if (frame == null)
		{
			// WIP: logger fails because it has different tags registered. These tags are foreign and useless.

			Log.out(Tag.CRITICAL, "Cannot find the %s frame for receiver %s", getClass().getSimpleName(), receiverRole.getType().getName());
			return;
		}

		if (frame.depth == 0)
		{
			framesByTypeRole.remove(receiverRole);
		}
		else
		{
			frame.depth--;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder("{");
		for (TypeRole frameRole : framesByTypeRole.keySet())
		{
			buffer.append(RNSUtils.getPlainName(frameRole.getType()));
			buffer.append(", ");
		}
		buffer.setLength(buffer.length() - 2);
		buffer.append("}");

		return buffer.toString();
	}
}
