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
package org.hawkinssoftware.rns.analysis.compile.nature;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class RNSNatureContextMenuToggleAction implements IObjectActionDelegate
{
	private ISelection selection;

	public void run(IAction action)
	{
		if (selection instanceof IStructuredSelection)
		{
			@SuppressWarnings("unchecked")
			List<Object> selectedObjects = ((IStructuredSelection) selection).toList();
			for (Object selectedObject : selectedObjects)
			{
				IProject project = null;
				if (selectedObject instanceof IProject)
				{
					project = (IProject) selectedObject;
				}
				else if (selectedObject instanceof IAdaptable)
				{
					project = (IProject) ((IAdaptable) selectedObject).getAdapter(IProject.class);
				}

				if (project != null)
				{
					toggleNature(project);
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
		this.selection = selection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have RNS nature added or removed
	 */
	private void toggleNature(IProject project)
	{
		try
		{
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i)
			{
				if (RNSNature.NATURE_ID.equals(natures[i]))
				{
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					return;
				}
			}

			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = RNSNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		}
		catch (CoreException e)
		{
		}
	}
}
