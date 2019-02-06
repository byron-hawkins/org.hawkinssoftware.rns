package org.hawkinssoftware.rns.test.runtime;

import java.util.List;

public class TestNotificationRouterExample implements TestNotificationRouter
{
	@Override
	public void route(TestNotification notification, TestHandler handler, List<Integer> pendingTransaction)
	{
		((ParticularTestHandlerImpl) handler).actionPosted((SeparateTestNotification) notification, pendingTransaction);
	}
}
