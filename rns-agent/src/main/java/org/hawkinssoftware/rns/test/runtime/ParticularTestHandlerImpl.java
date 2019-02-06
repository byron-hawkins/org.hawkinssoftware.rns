package org.hawkinssoftware.rns.test.runtime;

import java.util.List;

import org.hawkinssoftware.rns.test.runtime.VeryParticularTestDirective.EspeciallyParticularTestDirective;

public class ParticularTestHandlerImpl implements ParticularTestHandler
{
	@Override
	public void apply(ParticularTestDirective action)
	{
		System.out.println("apply: " + action.getClass().getSimpleName());
	}

	public void apply(VeryParticularTestDirective action)
	{
		System.out.println("apply: " + action.getClass().getSimpleName());
	}

	@Override
	public void apply(EspeciallyParticularTestDirective action)
	{
		System.out.println("apply: " + action.getClass().getSimpleName());
	}

	@Override
	public void actionPosted(ParticularTestNotification notification, List<Integer> pendingTransaction)
	{
		System.out.println("post: " + notification.getClass().getSimpleName() + " with transaction " + pendingTransaction);
		pendingTransaction.add(pendingTransaction.size() + 1);
	}

	@Override
	public void actionPosted(AnotherTestNotification notification, List<Integer> pendingTransaction)
	{
		System.out.println("post: " + notification.getClass().getSimpleName() + " with transaction " + pendingTransaction);
		pendingTransaction.add(pendingTransaction.size() + 1);
	}
 
	public void actionPosted(SeparateTestNotification notification, List<Integer> pendingTransaction)
	{
		System.out.println("post: " + notification.getClass().getSimpleName() + " with transaction " + pendingTransaction);
		pendingTransaction.add(pendingTransaction.size() + 1);
	}
}
