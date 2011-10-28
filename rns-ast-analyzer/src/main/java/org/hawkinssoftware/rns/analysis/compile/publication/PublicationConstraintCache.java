package org.hawkinssoftware.rns.analysis.compile.publication;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

class PublicationConstraintCache
{
	private final Map<String, Map<String, AggregatePublicationConstraint>> constraintsByTypeThenMethod = new HashMap<String, Map<String, AggregatePublicationConstraint>>();
	private final Map<String, AggregatePublicationConstraint> constraintsByType = new HashMap<String, AggregatePublicationConstraint>();

	void typeChanged(IType type)
	{
		getMethodConstraints(type).clear();
		constraintsByType.remove(type.getFullyQualifiedName());
	}

	AggregatePublicationConstraint getTypeConstraints(IType type)
	{
		return constraintsByType.get(type.getFullyQualifiedName());
	}

	AggregatePublicationConstraint getMethodConstraints(IMethod method)
	{
		return getMethodConstraints(method.getDeclaringType()).get(method.getElementName());
	}

	boolean hasTypeConstraints(IType type)
	{
		return constraintsByType.containsKey(type.getFullyQualifiedName());
	}

	boolean hasMethodConstraints(IMethod method)
	{
		Map<String, AggregatePublicationConstraint> constraints = constraintsByTypeThenMethod.get(method.getDeclaringType().getFullyQualifiedName());
		if (constraints == null)
		{
			return false;
		}
		return constraints.containsKey(method.getElementName());
	}

	void putTypeConstraints(IType type, AggregatePublicationConstraint constraints)
	{
		constraintsByType.put(type.getFullyQualifiedName(), constraints);
	}

	void putMethodConstraints(IMethod method, AggregatePublicationConstraint constraints)
	{
		getMethodConstraints(method.getDeclaringType()).put(method.getElementName(), constraints);
	}

	private Map<String, AggregatePublicationConstraint> getMethodConstraints(IType type)
	{
		Map<String, AggregatePublicationConstraint> constraints = constraintsByTypeThenMethod.get(type.getFullyQualifiedName());
		if (constraints == null)
		{
			constraints = new HashMap<String, AggregatePublicationConstraint>();
			constraintsByTypeThenMethod.put(type.getFullyQualifiedName(), constraints);
		}
		return constraints;
	}
}
