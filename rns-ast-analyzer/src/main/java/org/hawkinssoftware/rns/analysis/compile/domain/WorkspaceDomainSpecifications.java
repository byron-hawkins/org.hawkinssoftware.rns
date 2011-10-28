package org.hawkinssoftware.rns.analysis.compile.domain;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

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
