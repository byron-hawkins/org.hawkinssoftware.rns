package org.hawkinssoftware.rns.test.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public class RouterInstrumentation implements Constants
{
	enum RouterType
	{
		DIRECTIVE,
		NOTIFICATION;
	}

	interface BaseClassnames
	{
		String getRouterBaseClassname(RouterType routerType);

		String getTransactionElementBaseClassname(RouterType routerType);

		String getHandlerBaseClassname();

		String getTransactionClassname();
	}

	interface Definition
	{
		RouterType getRouterType();

		String getParameterClassname();

		String getHandlerClassname();

		String getHandlerMethodName();

		String getInstrumentedRouterClassname();

		String getInstrumentedRouterSimpleName();
	}

	private final BaseClassnames baseClassnames;

	public RouterInstrumentation(BaseClassnames baseClassnames)
	{
		this.baseClassnames = baseClassnames;
	}

	public byte[] create(Definition definition) throws IOException
	{
		RouterConstructor constructor = new RouterConstructor(definition);
		return constructor.create();
	}

	private class RouterConstructor
	{
		private final Definition definition;

		private final InstructionFactory factory;
		private final ConstantPoolGen constants;
		private final ClassGen classnameGenerator;

		RouterConstructor(Definition definition)
		{
			this.definition = definition;

			classnameGenerator = new ClassGen(definition.getInstrumentedRouterClassname(), "java.lang.Object", definition.getInstrumentedRouterSimpleName()
					+ ".java", ACC_PUBLIC | ACC_SUPER, new String[] { routerBaseClassname() });

			constants = classnameGenerator.getConstantPool();
			factory = new InstructionFactory(classnameGenerator, constants);
		}

		byte[] create() throws IOException
		{
			instrumentConstructor();
			instrumentHandlerMethod();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			JavaClass type = classnameGenerator.getJavaClass();
			type.dump(out);
			return out.toByteArray();
		}

		private void instrumentConstructor()
		{
			InstructionList instructions = new InstructionList();
			MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {}, "<init>", routerClassname(), instructions, constants);

			instructions.append(InstructionFactory.createLoad(Type.OBJECT, 0));
			instructions.append(factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
			instructions.append(InstructionFactory.createReturn(Type.VOID));
			method.setMaxStack();
			method.setMaxLocals();
			classnameGenerator.addMethod(method.getMethod());
			instructions.dispose();
		}

		private void instrumentHandlerMethod()
		{
			InstructionList instructions = new InstructionList();

			MethodGen method;
			switch (definition.getRouterType())
			{
				case DIRECTIVE:
					method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { elementBaseType(), handlerBaseType() }, new String[] { "action", "handler" },
							"route", routerClassname(), instructions, constants);
					break;
				case NOTIFICATION:
					method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { elementBaseType(), handlerBaseType(), transactionType() }, new String[] {
							"notification", "handler", "pendingTransaction" }, "route", routerClassname(), instructions, constants);
					break;
				default:
					throw new IllegalArgumentException();
			}

			instructions.append(InstructionFactory.createLoad(Type.OBJECT, 2));
			instructions.append(factory.createCheckCast(handlerType()));
			instructions.append(InstructionFactory.createLoad(Type.OBJECT, 1));
			instructions.append(factory.createCheckCast(parameterType()));

			Type[] arguments;
			switch (definition.getRouterType())
			{
				case DIRECTIVE:
					arguments = new Type[] { parameterType() };
					break;
				case NOTIFICATION:
					instructions.append(InstructionFactory.createLoad(Type.OBJECT, 3));
					arguments = new Type[] { parameterType(), transactionType() };
					break;
				default:
					throw new IllegalArgumentException();
			}

			instructions.append(factory.createInvoke(definition.getHandlerClassname(), definition.getHandlerMethodName(), Type.VOID, arguments,
					Constants.INVOKEINTERFACE));
			instructions.append(InstructionFactory.createReturn(Type.VOID));
			method.setMaxStack();
			method.setMaxLocals();
			classnameGenerator.addMethod(method.getMethod());
			instructions.dispose();
		}

		private String routerClassname()
		{
			return definition.getInstrumentedRouterClassname();
		}

		private String routerBaseClassname()
		{
			return baseClassnames.getRouterBaseClassname(definition.getRouterType());
		}

		private ObjectType elementBaseType()
		{
			return new ObjectType(baseClassnames.getTransactionElementBaseClassname(definition.getRouterType()));
		}

		private ObjectType handlerBaseType()
		{
			return new ObjectType(baseClassnames.getHandlerBaseClassname());
		}

		private ObjectType transactionType()
		{
			return new ObjectType(baseClassnames.getTransactionClassname());
		}

		private ObjectType handlerType()
		{
			return new ObjectType(definition.getHandlerClassname());
		}

		private ObjectType parameterType()
		{
			return new ObjectType(definition.getParameterClassname());
		}
	}
}