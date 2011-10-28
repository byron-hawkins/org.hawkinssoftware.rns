package org.hawkinssoftware.rns.analysis.compile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.hawkinssoftware.rns.analysis.compile.source.SourceDeclarationInstruction;
import org.hawkinssoftware.rns.core.util.DefinesIdentity;

/**
 * Checks the annotations @ValidateInvocation and @ValidationMethod for valid form.
 * 
 * @author b
 */
public class DefinedIdentityAnnotationChecker extends HierarchyAnnotationChecker
{
	private final List<IType> identityTypes = new ArrayList<IType>();

	public DefinedIdentityAnnotationChecker()
	{
		super("defined-identity");
	}

	@Override
	protected void startHierarchyAnalysis()
	{
		try
		{
			identityTypes.clear();

			if (isIdentity(hierarchy.getType()))
			{
				identityTypes.add(hierarchy.getType());
			}

			for (IType supertype : hierarchy.getAllSupertypes(hierarchy.getType()))
			{
				if (isIdentity(supertype))
				{
					identityTypes.add(supertype);
				}
			}

			if (identityTypes.size() > 1)
			{
				StringBuilder message = new StringBuilder("Type " + hierarchy.getType().getFullyQualifiedName() + " multiply @"
						+ DefinesIdentity.class.getSimpleName() + ": { ");
				for (IType identityType : identityTypes)
				{
					message.append(identityType.getFullyQualifiedName());
					message.append(" , ");
				}
				message.setLength(message.length() - 2);
				message.append(" }. A type @" + DefinesIdentity.class.getSimpleName() + " at most once.");

				SourceDeclarationInstruction<AbstractTypeDeclaration, ITypeBinding> declaration = source.getTypeDeclaration(hierarchy.getType());
				createError(message.toString(), declaration.markerReferenceNode);
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	private boolean isIdentity(IType type) throws JavaModelException
	{
		for (IAnnotation annotation : type.getAnnotations())
		{
			if (annotation.getElementName().contains(DefinesIdentity.class.getSimpleName()))
			{
				return true;
			}
		}
		return false;
	}
}
