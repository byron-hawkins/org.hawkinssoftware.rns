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
package org.hawkinssoftware.rns.analysis.compile.publication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.hawkinssoftware.rns.analysis.compile.domain.DomainRoleTypeBinding;
import org.hawkinssoftware.rns.analysis.compile.util.RNSBuildAnalyzerUtils;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

/**
 * Container for the developer-assigned constraints on the publication scope of a type. An instance of
 * AggregatePublicationConstraint may refer to the constraints on a single Java member, or an aggregation of constraints
 * which has been compiled on the basis of constraint inheritance rules.
 * 
 * @author Byron Hawkins
 */
class AggregatePublicationConstraint
{
	private final IType constrainedType;

	final Set<String> packages;
	final Set<DomainRoleTypeBinding> domains;
	final Set<String> typenames;
	final Set<String> extendedTypenames;
	final boolean inherit;
	final boolean voidInheritance;

	AggregatePublicationConstraint(IType constrainedType, IAnnotation constraint) throws JavaModelException
	{
		this.constrainedType = constrainedType;

		Assembler assembler = new Assembler();
		assembler.assemble(constraint);

		packages = Collections.unmodifiableSet(assembler.packages);
		domains = Collections.unmodifiableSet(assembler.domains);
		typenames = Collections.unmodifiableSet(assembler.typenames);
		extendedTypenames = Collections.unmodifiableSet(assembler.extendedTypenames);
		inherit = assembler.inherit;
		voidInheritance = assembler.voidInheritance;
	}

	private AggregatePublicationConstraint(IType constrainedType, Assembler assembler)
	{
		this.constrainedType = constrainedType;
		packages = Collections.unmodifiableSet(assembler.packages);
		domains = Collections.unmodifiableSet(assembler.domains);
		typenames = Collections.unmodifiableSet(assembler.typenames);
		extendedTypenames = Collections.unmodifiableSet(assembler.extendedTypenames);
		inherit = assembler.inherit;
		voidInheritance = assembler.voidInheritance;
	}

	AggregatePublicationConstraint inheritFrom(AggregatePublicationConstraint additionalConstraintDefinitions)
	{
		Assembler assembler = new Assembler();
		assembler.copyEnclosedInstance();
		assembler.append(additionalConstraintDefinitions);
		return new AggregatePublicationConstraint(constrainedType, assembler);
	}

	/**
	 * An empty constraints clobbers all inheritance and fully constrains the element.
	 */
	boolean isEmpty()
	{
		return packages.isEmpty() && domains.isEmpty() && typenames.isEmpty() && extendedTypenames.isEmpty();
	}

	boolean exceeds(AggregatePublicationConstraint other)
	{
		for (String packagePattern : packages)
		{
			if (!other.packages.contains(packagePattern))
			{
				return true;
			}
		}

		for (DomainRoleTypeBinding role : domains)
		{
			boolean isContained = false;
			for (DomainRoleTypeBinding otherRole : other.domains)
			{
				if (otherRole.isContainedIn(role))
				{
					isContained = true;
					break;
				}
			}
			if (!isContained)
			{
				return true;
			}
		}

		for (String typename : typenames)
		{
			if (!other.typenames.contains(typename))
			{
				return true;
			}
		}

		for (String extendedTypename : extendedTypenames)
		{
			if (!other.extendedTypenames.contains(extendedTypename))
			{
				return true;
			}
		}

		return false;
	}

	public boolean applyConstraints()
	{
		if (voidInheritance)
		{
			return (inherit && !isEmpty());
		}
		return inherit;
	}

	public boolean traverse()
	{
		return !(voidInheritance || isEmpty());
	}

	private class Assembler
	{
		final Set<String> packages = new HashSet<String>();
		final Set<DomainRoleTypeBinding> domains = new HashSet<DomainRoleTypeBinding>();
		final Set<String> typenames = new HashSet<String>();
		final Set<String> extendedTypenames = new HashSet<String>();
		boolean inherit;
		boolean voidInheritance;

		void assemble(IAnnotation constraint) throws JavaModelException
		{
			boolean foundInheritance = false;
			for (IMemberValuePair entry : constraint.getMemberValuePairs())
			{
				if (entry.getMemberName().equals("packages"))
				{
					if (entry.getValue() instanceof String)
					{
						addPackagePattern((String) entry.getValue());
					}
					else
					{
						for (Object packagePattern : (Object[]) entry.getValue())
						{
							addPackagePattern((String) packagePattern);
						}
					}
				}
				else if (entry.getMemberName().equals("domains"))
				{
					if (entry.getValue() instanceof String)
					{
						addDomain(constrainedType, (String) entry.getValue());
					}
					else
					{
						for (Object domain : (Object[]) entry.getValue())
						{
							addDomain(constrainedType, (String) domain);
						}
					}
				}
				else if (entry.getMemberName().equals("types"))
				{
					if (entry.getValue() instanceof String)
					{
						typenames.add(RNSBuildAnalyzerUtils.getFullyQualifiedTypename(constrainedType, (String) entry.getValue()));
					}
					else
					{
						for (Object type : (Object[]) entry.getValue())
						{
							typenames.add(RNSBuildAnalyzerUtils.getFullyQualifiedTypename(constrainedType, (String) type));
						}
					}
				}
				else if (entry.getMemberName().equals("extendedTypes"))
				{
					if (entry.getValue() instanceof String)
					{
						extendedTypenames.add(RNSBuildAnalyzerUtils.getFullyQualifiedTypename(constrainedType, (String) entry.getValue()));
					}
					else
					{
						for (Object type : (Object[]) entry.getValue())
						{
							extendedTypenames.add(RNSBuildAnalyzerUtils.getFullyQualifiedTypename(constrainedType, (String) type));
						}
					}
				}
				else if (entry.getMemberName().equals("inherit"))
				{
					foundInheritance = (Boolean) entry.getValue();
				}
				else if (entry.getMemberName().equals("voidInheritance"))
				{
					voidInheritance = (Boolean) entry.getValue();
				}
			}

			boolean hasInheritOption = RNSBuildAnalyzerUtils.hasMethod(constrainedType, constraint, "inherit");
			boolean inheritsByDefault = true;
			if (hasInheritOption)
			{
				inheritsByDefault = RNSBuildAnalyzerUtils.getDefaultBooleanValue(constrainedType, constraint, "inherit");
			}
			inherit = foundInheritance || inheritsByDefault;
		}

		void copyEnclosedInstance()
		{
			append(AggregatePublicationConstraint.this);
			inherit = AggregatePublicationConstraint.this.inherit;
			voidInheritance = AggregatePublicationConstraint.this.voidInheritance;
		}

		void append(AggregatePublicationConstraint additionalConstraintDefinitions)
		{
			packages.addAll(additionalConstraintDefinitions.packages);
			domains.addAll(additionalConstraintDefinitions.domains);
			typenames.addAll(additionalConstraintDefinitions.typenames);
			extendedTypenames.addAll(additionalConstraintDefinitions.extendedTypenames);
		}

		private void addPackagePattern(String packagePattern)
		{
			// TODO: lame this doesn't work right...
			if (packagePattern.endsWith(".MY_PACKAGE"))
			{
				packages.add(constrainedType.getPackageFragment().getElementName());
			}
			else
			{
				packages.add(packagePattern);
			}
		}

		private void addDomain(IType constrainedType, String constraintEntry) throws JavaModelException
		{
			DomainRoleTypeBinding domainRole = null;
			if (constraintEntry.equals(InvocationConstraint.MyDomains.class.getSimpleName()))
			{
				// TODO: this case should match on the value of `InvocationConstraint.MY_DOMAINS, not the string
				domains.addAll(DomainRoleTypeBinding.Cache.getInstance().getDomainRoles(constrainedType.getFullyQualifiedName()));
			}
			else
			{
				String fullyQualifiedTypename = RNSBuildAnalyzerUtils.getFullyQualifiedTypename(constrainedType, constraintEntry);
				if (fullyQualifiedTypename != null)
				{
					domainRole = DomainRoleTypeBinding.Cache.getInstance().getDomainRole(fullyQualifiedTypename);
				}
				if (domainRole == null)
				{
					Log.out(Tag.WARNING, "Unable to find domain %s referenced in type %s.", constraintEntry, constrainedType);
				}
				domains.add(domainRole);
			}
		}
	}
}
