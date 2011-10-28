package org.hawkinssoftware.rns.test.runtime;

public interface TestDirectiveRouter
{
	public void route(TestDirective directive, TestHandler handler);
}
