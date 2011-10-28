package org.hawkinssoftware.rns.core.aop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hawkinssoftware.rns.core.role.DomainRole;

public class ClassLoadObserver
{
	public static class ClassLoadObservationDomain extends DomainRole
	{
		@DomainRole.Instance
		public static final ClassLoadObservationDomain INSTANCE = new ClassLoadObservationDomain();
	}

	public interface FilteredObserver
	{
		String[] getObservedTypenames();

		MethodFilter[] getMethodFilters();

		void matchingTypeObserved(ObservedType type);
	}

	public interface MethodFilter
	{
		boolean acceptMethodName(String name);

		int getParameterCount();

		String getParameterType(int index);
	}

	public static class ObservedType
	{
		public final Set<String> observedTypes;

		public final boolean isInterface;
		public final TypeHierarchy typeHierarchy;
		public final Collection<ObservedMethod> observedMethods;

		public ObservedType(Set<String> observedTypes, boolean isInterface, TypeHierarchy typeHierarchy, Collection<ObservedMethod> observedMethods)
		{
			this.observedTypes = observedTypes;
			this.isInterface = isInterface;
			this.typeHierarchy = typeHierarchy;
			this.observedMethods = observedMethods;
		}
	}

	public static class ObservedMethod
	{
		public final String name;
		public final TypeHierarchy[] parameters;

		public ObservedMethod(String name, TypeHierarchy[] parameters)
		{
			this.name = name;
			this.parameters = parameters;
		}
	}

	public static class TypeHierarchy
	{
		public final String qualifiedName;
		public final TypeHierarchy supertype;
		public final TypeHierarchy[] interfaces;

		public TypeHierarchy(String qualifiedName)
		{
			this.qualifiedName = qualifiedName;
			supertype = null;
			interfaces = new TypeHierarchy[0];
		}

		public TypeHierarchy(String qualifiedName, TypeHierarchy supertype, TypeHierarchy[] interfaces)
		{
			this.qualifiedName = qualifiedName;
			this.supertype = supertype;
			this.interfaces = interfaces;
		}
	}

	public static Collection<FilteredObserver> getObservers()
	{
		return INSTANCE.observers;
	}

	public static void observe(FilteredObserver observer)
	{
		INSTANCE.observers.add(observer);
	}

	private static final ClassLoadObserver INSTANCE = new ClassLoadObserver();

	private final List<FilteredObserver> observers = new ArrayList<FilteredObserver>();
}
