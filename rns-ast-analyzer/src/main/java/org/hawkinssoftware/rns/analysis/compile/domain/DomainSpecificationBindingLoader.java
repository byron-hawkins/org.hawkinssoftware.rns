package org.hawkinssoftware.rns.analysis.compile.domain;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainSpecifications;
import org.hawkinssoftware.rns.core.role.DomainSpecificationsLoader;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.hawkinssoftware.rns.core.util.RNSUtils;

public class DomainSpecificationBindingLoader
{
	static DomainSpecificationBindings load(IProject project)
	{
		String specificationsFilePath = String.format("src/main/resources/%s/%s", RNSUtils.RNS_RESOURCE_FOLDER_NAME,
				DomainSpecifications.getSpecificationFilename(project.getName()));

		try
		{
			Log.out(Tag.DEBUG, "Loading %s for project %s", DomainSpecifications.class.getSimpleName(), project.getName());

			IResource specificationFile = project.findMember(specificationsFilePath);
			if ((specificationFile == null) || !specificationFile.exists())
			{
				return null;
			}
			InputStream in = specificationFile.getLocationURI().toURL().openStream();
			DomainSpecifications specifications = DomainSpecificationsLoader.load(in);

			DomainSpecificationBindings bindings = new DomainSpecificationBindings();
			for (DomainSpecifications.OrthogonalSet orthogonalSet : specifications.orthogonalSets)
			{
				Set<DomainRoleTypeBinding> domains = new HashSet<DomainRoleTypeBinding>();
				for (String domainTypename : orthogonalSet.domainTypenames)
				{
					domains.add(DomainRoleTypeBinding.Cache.getInstance().getDomainRole(domainTypename));
				}
				bindings.orthogonalSets.add(new DomainSpecificationBindings.OrthogonalSet(domains, orthogonalSet.packagePatterns));
			}
			return bindings;
		}
		catch (Exception e)
		{
			Log.out(Tag.CRITICAL, e, "Failed to load the domain specification file %s for project %s.", specificationsFilePath, project.getName());
			return null;
		}
	}
}
