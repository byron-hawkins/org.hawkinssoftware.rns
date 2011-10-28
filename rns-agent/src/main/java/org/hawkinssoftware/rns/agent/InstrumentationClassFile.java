package org.hawkinssoftware.rns.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;
import org.hawkinssoftware.rns.agent.util.ClassfileLoader;
import org.hs.rns.core.aop.ClassLoadObserver;
import org.hs.rns.core.aop.ClassLoadObserver.FilteredObserver;
import org.hs.rns.core.aop.ClassLoadObserver.MethodFilter;
import org.hs.rns.core.aop.ClassLoadObserver.ObservedMethod;
import org.hs.rns.core.aop.ClassLoadObserver.ObservedType;
import org.hs.rns.core.aop.ClassLoadObserver.TypeHierarchy;

public class InstrumentationClassFile implements RNSInstrumentationConstants
{
	private static class MethodCollector
	{
		final Map<String, ObservedMethod> methodsBySignature = new HashMap<String, ObservedMethod>();
		final FilteredObserver observer;

		MethodCollector(FilteredObserver observer)
		{
			this.observer = observer;
		}
	}

	private boolean hasChanges = false;
	public final JavaClass parsedType;
	public final ConstantPoolGen constants;
	public final Metadata metadata;

	public InstrumentationClassFile(String classname, byte[] classfileBytes) throws ClassFormatException, IOException, ClassNotFoundException
	{
		// parse this one separately from the ClassfileLoader because another agent may have instrumented it already
		ByteArrayInputStream in = new ByteArrayInputStream(classfileBytes);
		ClassParser parser = new ClassParser(in, classname.replace('.', '/') + ".class");
		parsedType = parser.parse();
		constants = new ConstantPoolGen(parsedType.getConstantPool());
		metadata = new Metadata();
	}

	// TODO: seems like this scroungy method and hierarchy snarfing belongs somewhere special (not here...)
	void observe(FilteredObserver observer) throws ClassFormatException, IOException
	{
		try
		{
			Set<String> observedTypes = getObservedTypes(observer.getObservedTypenames());
			if (observedTypes.isEmpty())
			{
				return;
			}

			MethodCollector methodCollector = new MethodCollector(observer);
			TypeHierarchy typeHierarchy = getTypeHierarchy(parsedType, methodCollector);
			if (typeHierarchy == null)
			{
				return;
			}

			ObservedType type = new ObservedType(observedTypes, parsedType.isInterface(), typeHierarchy, methodCollector.methodsBySignature.values());
			observer.matchingTypeObserved(type);
		}
		catch (Throwable t)
		{
			System.err.println("Failed to extract the type hierarchy for " + parsedType.getClassName());
			t.printStackTrace();
		}
	}

	private Set<String> getObservedTypes(String[] classnames) throws ClassNotFoundException
	{
		Set<String> observedTypes = new HashSet<String>();

		for (String classname : classnames)
		{
			for (JavaClass implemented : parsedType.getAllInterfaces())
			{
				if (implemented.getClassName().equals(classname))
				{
					observedTypes.add(classname);
				}
			}

			if (isSupertype(parsedType, classname))
			{
				observedTypes.add(classname);
			}
		}

		return observedTypes;
	}

	private boolean isSupertype(JavaClass type, String classname) throws ClassNotFoundException
	{
		if (type == null)
		{
			return false;
		}
		if (type.getClassName().equals(classname))
		{
			return true;
		}
		return isSupertype(type.getSuperClass(), classname);
	}

	private TypeHierarchy getTypeHierarchy(JavaClass type, MethodCollector methodCollector) throws ClassNotFoundException, ClassFormatException, IOException
	{
		if (type == null)
		{
			return null;
		}

		boolean leaf = false;
		if (type.isInterface())
		{
			leaf = (type.getInterfaceNames().length == 0);
		}
		else
		{
			leaf = (type.getSuperClass() == null);
		}
		if (leaf)
		{
			return new ClassLoadObserver.TypeHierarchy(type.getClassName(), null, new TypeHierarchy[0]);
		}

		if ((methodCollector != null) && !type.isInterface())
		{
			for (Method method : type.getMethods())
			{
				String methodSignature = method.getName() + method.getSignature();
				if (methodCollector.methodsBySignature.containsKey(methodSignature))
				{
					continue;
				}

				Type[] arguments = method.getArgumentTypes();
				for (MethodFilter filter : methodCollector.observer.getMethodFilters())
				{
					if (!filter.acceptMethodName(method.getName()))
					{
						continue;
					}
					if (filter.getParameterCount() != arguments.length)
					{
						continue;
					}
					boolean match = true;
					TypeHierarchy[] argumentTypes = new TypeHierarchy[arguments.length];
					for (int i = 0; i < arguments.length; i++)
					{
						if (!matchArgument(arguments[i], filter.getParameterType(i)))
						{
							match = false;
							break;
						}
						if (arguments[i] instanceof BasicType)
						{
							argumentTypes[i] = new TypeHierarchy(BytecodeInstrumentationUtil.getTypename((BasicType) arguments[i]));
						}
						else
						{
							argumentTypes[i] = getTypeHierarchy(loadType(arguments[i]), null);
						}
					}
					if (match)
					{
						ObservedMethod observedMethod = new ObservedMethod(method.getName(), argumentTypes);
						methodCollector.methodsBySignature.put(methodSignature, observedMethod);
					}
				}
			}
		}

		TypeHierarchy supertype = getTypeHierarchy(type.getSuperClass(), methodCollector);
		List<TypeHierarchy> interfaces = new ArrayList<TypeHierarchy>();
		for (JavaClass implemented : type.getInterfaces())
		{
			interfaces.add(getTypeHierarchy(implemented, methodCollector));
		}
		return new TypeHierarchy(type.getClassName(), supertype, interfaces.toArray(new TypeHierarchy[0]));
	}

	private boolean matchArgument(Type bytecodeArgument, String parameterType) throws ClassNotFoundException, ClassFormatException, IOException
	{
		if (bytecodeArgument instanceof BasicType)
		{
			return BytecodeInstrumentationUtil.getTypename((BasicType) bytecodeArgument).equals(parameterType);
		}

		if (parameterType.indexOf(".") < 0)
		{
			// assuming it's a primitive--this decision will fail for classes in the default package, but who cares...
			return false;
		}

		JavaClass bytecodeArgumentType = loadType(bytecodeArgument);
		JavaClass parsedParameterType = ClassfileLoader.getInstance().loadClass(parameterType.replace('.', '/') + ".class");
		return bytecodeArgumentType.instanceOf(parsedParameterType);
	}

	private JavaClass loadType(Type type) throws ClassFormatException, IOException
	{
		String signature = type.getSignature();
		return ClassfileLoader.getInstance().loadClass(signature.substring(1, signature.length() - 1) + ".class");
	}

	public final boolean hasChanges()
	{
		return hasChanges;
	}

	public final void setChanged()
	{
		hasChanges = true;
	}

	public final byte[] compile()
	{
		parsedType.setConstantPool(constants.getFinalConstantPool());
		return parsedType.getBytes();
	}

	public class Metadata
	{
		public final boolean isExcluded;
		public final boolean isConstraint;
		public final boolean isCommunicationRole;
		public final boolean hasCommunicationRole;
		public final boolean noFrame;
		public final AnnotationEntry classValidationMethodAnnotation;
		public final AnnotationEntry hookSemaphoresAnnotation;

		Metadata() throws ClassNotFoundException
		{
			boolean inOmmittedNamespace = false;
			String classname = BytecodeInstrumentationUtil.getCanonicalClassname(parsedType);
			for (String omittedNamespace : omittedNamespaces)
			{
				if (classname.matches(omittedNamespace))
				{
					inOmmittedNamespace = true;
					break;
				}
			}

			boolean foundConstraintInterface = false;
			for (String parsedTypeInterface : parsedType.getInterfaceNames())
			{
				if (parsedTypeInterface.equals(EXECUTION_PATH_CONSTRAINT_CLASSNAME))
				{
					foundConstraintInterface = true;
					break;
				}
			}

			boolean foundCommunicationRole = false;
			for (JavaClass supertype : parsedType.getSuperClasses())
			{
				if (BytecodeInstrumentationUtil.getCanonicalClassname(supertype).equals(COMMUNICATION_ROLE_CLASSNAME))
				{
					foundCommunicationRole = true;
					break;
				}
			}

			boolean foundCommunicationRoleAssociation = false;
			boolean foundNoFrame = false;
			AnnotationEntry foundClassValidationMethod = null;
			AnnotationEntry foundHookSemaphoresAnnotation = null;
			AnnotationEntry[] annotations = parsedType.getAnnotationEntries();
			for (AnnotationEntry annotation : annotations)
			{
				if (annotation.getAnnotationType().equals(DOMAIN_ROLE_ANNOTATION_TYPENAME))
				{
					foundCommunicationRoleAssociation = true;
				}
				if (annotation.getAnnotationType().equals(VALIDATE_INVOCATION_ANNOTATION_TYPENAME))
				{
					foundClassValidationMethod = annotation;
				}
				if (annotation.getAnnotationType().equals(HOOK_SEMAPHORES_ANNOTATION_TYPENAME))
				{
					foundHookSemaphoresAnnotation = annotation;
				}
				if (annotation.getAnnotationType().equals(NO_FRAME_ANNOTATION_TYPENAME))
				{
					foundNoFrame = true;
				}
			}

			hasCommunicationRole = foundCommunicationRoleAssociation;
			classValidationMethodAnnotation = foundClassValidationMethod;
			hookSemaphoresAnnotation = foundHookSemaphoresAnnotation;
			isExcluded = inOmmittedNamespace;
			isConstraint = foundConstraintInterface;
			isCommunicationRole = foundCommunicationRole;
			noFrame = foundNoFrame;
		}

		public boolean canInstrument()
		{
			return !(isExcluded || isCommunicationRole);
		}

		public boolean isInMetaDomain(Method method)
		{
			return isConstraint || isValidationMethod(method);
		}

		public boolean isMetaMethod(Method method)
		{
			if (isConstraint && method.getName().equals("validateMessage"))
			{
				if (hasValidationMethodSignature(method))
				{
					return true;
				}
			}
			else if (isValidationMethod(method))
			{
				return true;
			}

			return false;
		}

		public boolean canInstrument(Method method)
		{
			return !isValidationMetaMethod(method);
		}
		
		// TODO: prebuild the list?
		public List<String> getInitializationAgents() throws ClassNotFoundException
		{
			List<String> agentClassnames = new ArrayList<String>();

			for (JavaClass implemented : parsedType.getAllInterfaces())
			{
				for (AnnotationEntry annotation : implemented.getAnnotationEntries())
				{
					if (annotation.getAnnotationType().equals(INSTRUMENTATION_ASPECT_ANNOTATION_TYPENAME))
					{
						String agentClassname = annotation.getElementValuePairs()[0].getValue().stringifyValue();
						agentClassnames.add(agentClassname);
					}
				}
			}

			return agentClassnames;
		}

		private boolean isValidationMetaMethod(Method method)
		{
			AnnotationEntry[] annotations = method.getAnnotationEntries();
			for (AnnotationEntry annotation : annotations)
			{
				if (annotation.getAnnotationType().equals(VALIDATION_METHOD_DELEGATE_ANNOTATION_TYPENAME))
				{
					return true;
				}
			}
			return false;
		}

		private boolean isValidationMethod(Method method)
		{
			AnnotationEntry[] annotations = method.getAnnotationEntries();
			for (AnnotationEntry annotation : annotations)
			{
				if (annotation.getAnnotationType().equals(VALIDATION_METHOD_ANNOTATION_TYPENAME))
				{
					if (!hasValidationMethodSignature(method))
					{
						throw new IllegalArgumentException("Method '" + method
								+ "' is annotated with @ValidationMethod but has the wrong signature. The signature must be 'void method(TypeRole, TypeRole)'.");
					}
					return true;
				}
			}

			return false;
		}

		private boolean hasValidationMethodSignature(Method method)
		{
			Type[] parameterTypes = method.getArgumentTypes();
			return ((parameterTypes.length == 2) && parameterTypes[0].getSignature().equals(TYPE_ROLE_CLASSNAME) && parameterTypes[1].getSignature().equals(
					TYPE_ROLE_CLASSNAME));
		}
	}
}
