package org.hawkinssoftware.rns.test.runtime;

import java.util.List;

import org.hawkinssoftware.rns.test.runtime.VeryParticularTestDirective.EspeciallyParticularTestDirective;

public interface ParticularTestHandler extends TestHandler
{
	void apply(ParticularTestDirective action);

	void apply(EspeciallyParticularTestDirective action);

	void actionPosted(ParticularTestNotification notification, List<Integer> pendingTransaction);

	void actionPosted(AnotherTestNotification notification, List<Integer> pendingTransaction);
}
