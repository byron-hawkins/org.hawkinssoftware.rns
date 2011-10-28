package org.hawkinssoftware.rns.test.runtime;

import java.util.List;

public interface TestNotificationRouter
{
	public void route(TestNotification notification, TestHandler handler, List<Integer> pendingTransaction);
}
