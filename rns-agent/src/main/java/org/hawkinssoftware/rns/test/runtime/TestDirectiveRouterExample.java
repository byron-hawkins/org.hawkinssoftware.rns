package org.hawkinssoftware.rns.test.runtime;

public class TestDirectiveRouterExample implements TestDirectiveRouter
{
	public void route(TestDirective directive, TestHandler handler)
	{
		((ParticularTestHandler) handler).apply((ParticularTestDirective) directive);
	}
}
