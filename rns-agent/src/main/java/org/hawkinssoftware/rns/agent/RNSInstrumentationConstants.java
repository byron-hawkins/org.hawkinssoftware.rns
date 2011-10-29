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
package org.hawkinssoftware.rns.agent;

import org.hawkinssoftware.rns.core.aop.InitializationAspect;
import org.hawkinssoftware.rns.core.lock.HookSemaphores;
import org.hawkinssoftware.rns.core.moa.ExecutionPath;
import org.hawkinssoftware.rns.core.role.CommunicationRole;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.role.TypeRole;
import org.hawkinssoftware.rns.core.validation.ValidateInvocation;
import org.hawkinssoftware.rns.core.validation.ValidateRead;
import org.hawkinssoftware.rns.core.validation.ValidateWrite;
import org.hawkinssoftware.rns.core.validation.ValidationMethod;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public interface RNSInstrumentationConstants
{
	static final String[] omittedNamespaces = new String[] { "org\\.hawkinssoftware\\.rns\\.core\\..*", "org\\.hawkinssoftware\\.rns\\.agent\\..*", "\\$.*",
			"sun\\..*", "com\\.sun\\..*" };

	static final String EXECUTION_PATH_CLASSNAME = ExecutionPath.class.getName();
	static final String EXECUTION_PATH_CONSTRAINT_CLASSNAME = ExecutionPath.StackObserver.class.getName();

	static final String COMMUNICATION_ROLE_CLASSNAME = CommunicationRole.class.getCanonicalName();
	static final String DOMAIN_ROLE_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(DomainRole.Join.class);
	static final String TYPE_ROLE_CLASSNAME = BytecodeInstrumentationUtil.getBytecodeClassname(TypeRole.class);

	static final String HOOK_SEMAPHORES_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(HookSemaphores.class);
	static final String SEMAPHORE_HOOK_RELAY_CLASSNAME = HookSemaphores.Relay.class.getName();

	static final String NO_FRAME_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ExecutionPath.NoFrame.class);

	static final String VALIDATE_INVOCATION_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ValidateInvocation.class);
	static final String VALIDATE_READ_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ValidateRead.class);
	static final String VALIDATE_WRITE_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ValidateWrite.class);
	static final String READ_EXEMPTION_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ValidateRead.Exempt.class);
	static final String WRITE_EXEMPTION_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ValidateWrite.Exempt.class);
	static final String VALIDATION_METHOD_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ValidationMethod.class);
	static final String VALIDATION_METHOD_DELEGATE_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(ValidationMethod.Delegate.class);
	static final String WRITE_VALIDATION_AGENT_CLASSNAME = ValidateWrite.ValidationAgent.class.getName();
	static final String READ_VALIDATION_AGENT_CLASSNAME = ValidateRead.ValidationAgent.class.getName();
	static final String VALIDATE_WRITE_METHOD_NAME = "validateWrite";
	static final String VALIDATE_READ_METHOD_NAME = "validateRead";
	static final String DEFAULT_VALIDATION_METHOD_NAME = "validateMessage";
	static final String DEFAULT_VALIDATION_CALLEE_CLASSNAME = EXECUTION_PATH_CLASSNAME;

	static final String INSTRUMENTATION_ASPECT_ANNOTATION_TYPENAME = BytecodeInstrumentationUtil.getBytecodeClassname(InitializationAspect.class);
}
