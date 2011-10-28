package org.hawkinssoftware.rns.analysis.compile.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;
import org.hawkinssoftware.rns.core.role.DomainSpecifications;

public class DomainSpecificationBindings
{
	final List<OrthogonalSet> orthogonalSets = new ArrayList<OrthogonalSet>();
	final DomainSpecifications specifications = new DomainSpecifications();

	void clear()
	{
		orthogonalSets.clear();
		specifications.clear();
	}

	void append(DomainSpecificationBindings addition)
	{
		orthogonalSets.addAll(addition.orthogonalSets);
		specifications.append(addition.specifications);
	}

	EvaluationResult evaluate(String memberTypename, Collection<DomainRoleTypeBinding> domains) throws JavaModelException
	{
		EvaluationResult result = new EvaluationResult();

		for (OrthogonalSet set : orthogonalSets)
		{
			set.evaluate(result, memberTypename, domains);
		}

		for (DomainRoleTypeBinding domain : domains)
		{
			DomainRoleTypeBinding parentDomain = DomainRoleTypeBinding.Cache.getInstance().getDomainRole(
					specifications.getParentDomain(domain.getDomainTypename()));
			if ((parentDomain != null) && !containsAny(parentDomain, domains))
			{
				result.addProblem("Containment violation: a member of %s must also be a member of %s", domain.getDomainTypename(),
						parentDomain.getDomainTypename());
			}
		}

		return result;
	}

	private boolean containsAny(DomainRoleTypeBinding matchDomain, Collection<DomainRoleTypeBinding> candidateDomains)
	{
		for (DomainRoleTypeBinding candidateDomain : candidateDomains)
		{
			if (matchDomain.isContainedIn(candidateDomain))
			{
				return true;
			}
		}
		return false;
	}

	static class OrthogonalSet
	{
		final Set<DomainRoleTypeBinding> domains;
		final List<String> packagePatterns;

		OrthogonalSet(Set<DomainRoleTypeBinding> domains, List<String> packagePatterns) throws JavaModelException
		{
			this.domains = domains;
			this.packagePatterns = packagePatterns;
		}

		void evaluate(EvaluationResult result, String memberTypename, Collection<DomainRoleTypeBinding> typeDomains)
		{
			Set<DomainRoleTypeBinding> matches = new HashSet<DomainRoleTypeBinding>();
			for (DomainRoleTypeBinding typeDomain : typeDomains)
			{
				for (DomainRoleTypeBinding domain : domains)
				{
					if (domain.isContainedIn(typeDomain))
					{
						matches.add(domain);
						break;
					}
				}
			}

			if (matches.size() > 0)
			{
				StringBuilder buffer = new StringBuilder("{");
				for (DomainRoleTypeBinding match : matches)
				{
					buffer.append(match.getDomainTypename());
					buffer.append(", ");
				}
				buffer.replace(buffer.length() - 2, buffer.length(), "}");

				if (matches.size() > 1)
				{
					result.addProblem("Domain orthogonal set collision: %s must not coincide on any type.", buffer.toString());
				}

				for (String packagePattern : packagePatterns)
				{
					if (packagePattern.matches(memberTypename))
					{
						result.addProblem("Package orthogonality violation: %s must not coincide with package %s", buffer.toString());
					}
				}
			}
		}
	}

	static class EvaluationResult
	{
		private List<String> problems = null;

		public List<String> getProblems()
		{
			if (problems == null)
			{
				return Collections.emptyList();
			}
			else
			{
				return problems;
			}
		}

		void addProblem(String problem, Object... args)
		{
			if (problems == null)
			{
				problems = new ArrayList<String>();
			}
			problems.add(String.format(problem, args));
		}
	}
}
