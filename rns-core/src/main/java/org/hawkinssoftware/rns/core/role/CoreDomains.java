package org.hawkinssoftware.rns.core.role;

public interface CoreDomains
{
	public static class InitializationDomain extends DomainRole
	{
		@DomainRole.Instance
		public static final InitializationDomain INSTANCE = new InitializationDomain();
	}
}
