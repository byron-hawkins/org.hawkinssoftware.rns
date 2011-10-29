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
package org.hawkinssoftware.rns.analysis.compile.domain;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class WorkspaceDomainSpecifications
{
	public static WorkspaceDomainSpecifications getInstance()
	{
		return INSTANCE;
	}
	
	private static final WorkspaceDomainSpecifications INSTANCE = new WorkspaceDomainSpecifications();
	
	public final DomainSpecificationBindings compilation = new DomainSpecificationBindings();

	private final Map<IProject, DomainSpecificationBindings> specificationsByProject = new HashMap<IProject, DomainSpecificationBindings>();

	public void load(IProject project)
	{
		DomainSpecificationBindings bindings = DomainSpecificationBindingLoader.load(project);
		if (bindings == null)
		{
			return;
		}
		
		specificationsByProject.put(project, bindings);
		
		compilation.clear();
		for (DomainSpecificationBindings specification : specificationsByProject.values())
		{
			compilation.append(specification);
		}
	}
}
