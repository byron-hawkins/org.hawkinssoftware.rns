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
package org.hawkinssoftware.rns.core.aop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hawkinssoftware.rns.core.role.DomainRole;

/**
 * Facilitates observation of every class loaded into the JVM.
 * <p>
 * <b>Usage:</b>An application is expected to implement the enclosed <code>FilteredObserver</code> interface and
 * register with <code>observe()</code>.
 */
public class ClassLoadObserver
{
	/**
	 * Domain role for restricting access to public content of the class load observation system.
	 * 
	 * @author Byron Hawkins
	 */
	public static class ClassLoadObservationDomain extends DomainRole
	{
		@DomainRole.Instance
		public static final ClassLoadObservationDomain INSTANCE = new ClassLoadObservationDomain();
	}

	/**
	 * The implementor is eligible to receive notification of classes loaded into the JVM; notification is
	 * pre-filtered according to the implementors <code>getObservedTypenames()</code> and
	 * <code>getMethodFilters()</code>. It would be more convenient to simply allow observation of all types, but there
	 * are complications:
	 * 
	 * <ol>
	 * <li>The observation is implemented by the bytecode instrumentation agent, and for that reason, the observed class
	 * has actually not been initialized yet. Any attempt to access it in terms of a <code>java.lang.Class</code>, or to
	 * invoke <code>Class.forName()</code> will cause JVM implosion.</li>
	 * <li>It is substantially expensive to construct the method and type hierarchy information, because it is scarfed
	 * out of the .class files on disk.</li>
	 * </ol>
	 * 
	 * For these reasons, it is advised that the filter be as strict as possible, and that no
	 * <code>java.lang.Class</code> functionality be executed on the observed type.
	 */
	public interface FilteredObserver
	{
		String[] getObservedTypenames();

		MethodFilter[] getMethodFilters();

		void matchingTypeObserved(ObservedType type);
	}

	/**
	 * Method filter member of <code>FilteredObserver</code>.
	 * 
	 * @author Byron Hawkins
	 */
	public interface MethodFilter
	{
		boolean acceptMethodName(String name);

		int getParameterCount();

		String getParameterType(int index);
	}

	/**
	 * Metadata about a class that is in the process of being loaded. Instances are passed to
	 * <code>FilteredObserver</code> for which the filter criteria are met. Be aware that the ObservedType is not yet a
	 * <code>java.lang.Class</code> and cannot be accessed as such in any way at all (attempting to do so will cause JVM
	 * implosion).
	 * 
	 * @author Byron Hawkins
	 */
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

	/**
	 * Method metadata member of <code>ObservedType</code>.
	 * 
	 * @author Byron Hawkins
	 */
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

	/**
	 * Type hierarchy member of <code>ObservedType</code>.
	 * 
	 * @author Byron Hawkins
	 */
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
