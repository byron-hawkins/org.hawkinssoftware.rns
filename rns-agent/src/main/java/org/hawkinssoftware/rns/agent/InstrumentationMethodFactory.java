package org.hawkinssoftware.rns.agent;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import org.hs.rns.core.role.TypeRole;

public class InstrumentationMethodFactory implements RNSInstrumentationConstants
{
	public enum MethodInvocation
	{
		POST_CALLER
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(EXECUTION_PATH_CLASSNAME, "postCaller", Type.VOID, POST_CALLER_PARAMETERS, Constants.INVOKESTATIC);
			}
		},
		INVOCATION_STACK_PUSH
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(EXECUTION_PATH_CLASSNAME, "pushInvocationFrame", Type.VOID, PUSH_MESSAGE_FRAME_PARAMETERS, Constants.INVOKESTATIC);
			}
		},
		INVOCATION_STACK_POP
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(EXECUTION_PATH_CLASSNAME, "popInvocationFrame", Type.VOID, new Type[0], Constants.INVOKESTATIC);
			}
		},
		BEGIN_META_METHOD
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(EXECUTION_PATH_CLASSNAME, "beginMetaMethod", Type.VOID, new Type[0], Constants.INVOKESTATIC);
			}
		},
		END_META_METHOD
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(EXECUTION_PATH_CLASSNAME, "endMetaMethod", Type.VOID, new Type[0], Constants.INVOKESTATIC);
			}
		},
		GET_CALLER_ROLE
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(EXECUTION_PATH_CLASSNAME, "getCallerRole", Type.getType(TypeRole.class), new Type[0], Constants.INVOKESTATIC);
			}
		},
		GET_CALLEE_ROLE
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(EXECUTION_PATH_CLASSNAME, "getCalleeRole", Type.getType(TypeRole.class), CALLEE_QUERY_PARAMETERS,
						Constants.INVOKESTATIC);
			}
		},
		SYNCHRONIZED_METHOD_ENTRY_RELAY
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(SEMAPHORE_HOOK_RELAY_CLASSNAME, "methodEntry", Type.VOID, SEMAPHORE_HOOK_PARAMETERS, Constants.INVOKESTATIC);
			}
		},
		SYNCHRONIZED_METHOD_EXIT_RELAY
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(SEMAPHORE_HOOK_RELAY_CLASSNAME, "methodExit", Type.VOID, new Type[0], Constants.INVOKESTATIC);
			}
		},
		ATTEMPTING_ACQUISITION_RELAY
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(SEMAPHORE_HOOK_RELAY_CLASSNAME, "attemptingAcquisition", Type.VOID, SEMAPHORE_HOOK_PARAMETERS,
						Constants.INVOKESTATIC);
			}
		},
		SEMAPHORE_ACQUIRED_RELAY
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(SEMAPHORE_HOOK_RELAY_CLASSNAME, "semaphoreAcquired", Type.VOID, SEMAPHORE_HOOK_PARAMETERS, Constants.INVOKESTATIC);
			}
		},
		SEMAPHORE_RELEASED_RELAY
		{
			public InvokeInstruction createInvocation(InstructionFactory factory)
			{
				return factory.createInvoke(SEMAPHORE_HOOK_RELAY_CLASSNAME, "semaphoreReleased", Type.VOID, SEMAPHORE_HOOK_PARAMETERS, Constants.INVOKESTATIC);
			}
		};

		public abstract InvokeInstruction createInvocation(InstructionFactory factory);
	}

	static final Type[] POST_CALLER_PARAMETERS = Type.getTypes(new Class<?>[] { Class.class, String.class });
	static final Type[] PUSH_MESSAGE_FRAME_PARAMETERS = Type.getTypes(new Class<?>[] { Object.class, String.class });
	static final Type[] VALIDATION_METHOD_PARAMETERS = Type.getTypes(new Class<?>[] { TypeRole.class, TypeRole.class });
	static final Type[] VALIDATE_FIELD_ACCESS_PARAMETERS = Type.getTypes(new Class<?>[] { Object.class, Object.class, String.class });
	static final Type[] CALLEE_QUERY_PARAMETERS = Type.getTypes(new Class<?>[] { Class.class });
	static final Type[] SEMAPHORE_HOOK_PARAMETERS = Type.getTypes(new Class<?>[] { Class.class, Object.class });

	public final InstructionFactory factory;

	private final Map<MethodInvocation, InvokeInstruction> invocations = new HashMap<MethodInvocation, InvokeInstruction>();

	InstrumentationMethodFactory(ConstantPoolGen constants)
	{
		factory = new InstructionFactory(constants);
	}

	public InvokeInstruction buildInvocation(MethodInvocation methodFactory)
	{
		InvokeInstruction invocation = invocations.get(methodFactory);
		if (invocation == null)
		{
			invocation = methodFactory.createInvocation(factory);
			invocations.put(methodFactory, invocation);
		}
		return invocation;
	}

	public InvokeInstruction buildValidationMethodInvocation(String calleeClassname, String methodName)
	{
		return factory.createInvoke(calleeClassname, methodName, Type.VOID, VALIDATION_METHOD_PARAMETERS, Constants.INVOKESTATIC);
	}

	public InvokeInstruction buildAccessValidationMethodInvocation(String calleeClassname, String methodName)
	{
		return factory.createInvoke(calleeClassname, methodName, Type.VOID, VALIDATE_FIELD_ACCESS_PARAMETERS, Constants.INVOKESTATIC);
	}
}
