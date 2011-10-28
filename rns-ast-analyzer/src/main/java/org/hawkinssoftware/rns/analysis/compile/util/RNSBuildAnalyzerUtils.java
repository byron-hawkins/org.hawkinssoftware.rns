package org.hawkinssoftware.rns.analysis.compile.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

public class RNSBuildAnalyzerUtils
{
	private static final String COMPILATION_ERROR_MARKER_TYPE = "org.eclipse.jdt.core.problem";

	public static boolean hasCompilationErrors(IResource resource) throws CoreException
	{
		return resource.findMaxProblemSeverity(COMPILATION_ERROR_MARKER_TYPE, false, 0) >= IMarker.SEVERITY_ERROR;
	}

	public static boolean hasErrors(IResource resource) throws CoreException
	{
		return resource.findMaxProblemSeverity(null, false, 0) >= IMarker.SEVERITY_ERROR;
	}

	public static boolean hasMethod(IType annotatedType, IAnnotation annotation, String methodName) throws JavaModelException
	{
		IType annotationType = annotation.getJavaProject().findType(annotation.getElementName());
		if (annotationType == null)
		{
			String[][] typename = annotatedType.resolveType(annotation.getElementName());
			annotationType = annotation.getJavaProject().findType(typename[0][0], typename[0][1]);
		}

		return annotationType.getMethod(methodName, new String[0]).exists();
	}

	public static boolean getDefaultBooleanValue(IType annotatedType, IAnnotation annotation, String methodName) throws JavaModelException
	{
		IType annotationType = annotation.getJavaProject().findType(annotation.getElementName());
		if (annotationType == null)
		{
			String[][] typename = annotatedType.resolveType(annotation.getElementName());
			annotationType = annotation.getJavaProject().findType(typename[0][0], typename[0][1]);
		}

		IMethod method = annotationType.getMethod(methodName, new String[0]);
		if (method == null)
		{
			return false;
		}

		Object value = method.getDefaultValue().getValue();
		if (value instanceof Boolean)
		{
			return (Boolean) value;
		}
		else
		{
			Log.out(Tag.WARNING, "Attempt to get the default boolean value from annotation %s, which has default value of type %s.",
					annotation.getElementName(), value == null ? "null" : value.getClass().getName());
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static ITypeBinding getTypeBinding(CompilationUnit ast)
	{
		for (AbstractTypeDeclaration type : (Iterable<AbstractTypeDeclaration>) ast.types())
		{
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION)
			{
				return type.resolveBinding();
			}
		}
		throw new IllegalArgumentException("Could not find the top-level type for ast " + ast.getTypeRoot().getElementName());
	}

	public static ITypeBinding getContainingTypeBinding(ASTNode node)
	{
		ASTNode traversal = node;
		while (!(traversal instanceof AbstractTypeDeclaration))
		{
			traversal = traversal.getParent();

			if (traversal == null)
			{
				throw new IllegalArgumentException("Could not find the containing type for ast node " + node);
			}
		}
		return ((AbstractTypeDeclaration) traversal).resolveBinding();
	}

	public static List<ITypeBinding> getAllSupertypes(ITypeBinding type)
	{
		List<ITypeBinding> supertypes = new ArrayList<ITypeBinding>();

		Set<String> visitedTypes = new HashSet<String>();
		List<ITypeBinding> unvisitedTypes = new ArrayList<ITypeBinding>();
		unvisitedTypes.add(type);
		visitedTypes.add(type.getQualifiedName());
		while (!unvisitedTypes.isEmpty())
		{
			ITypeBinding visitingType = unvisitedTypes.remove(unvisitedTypes.size() - 1);

			ITypeBinding supertype = visitingType.getSuperclass();
			if ((supertype != null) && !visitedTypes.contains(supertype.getQualifiedName()))
			{
				visitedTypes.add(supertype.getQualifiedName());
				unvisitedTypes.add(supertype);
				supertypes.add(supertype);
			}
			for (ITypeBinding implemented : visitingType.getInterfaces())
			{
				if (!visitedTypes.contains(implemented.getQualifiedName()))
				{
					visitedTypes.add(implemented.getQualifiedName());
					unvisitedTypes.add(implemented);
					supertypes.add(implemented);
				}
			}
		}

		return supertypes;
	}

	public static String getFullyQualifiedTypename(IType annotatedType, String annotationTypeEntry) throws JavaModelException
	{
		String[][] resolvedName = annotatedType.resolveType(annotationTypeEntry);
		if (resolvedName == null)
		{
			return null;
		}
		if (resolvedName.length != 1)
		{
			throw new RuntimeException("Failed to resolve @DomainRole.Join reference '" + annotationTypeEntry + "' in type "
					+ annotatedType.getFullyQualifiedName());
		}

		String typeQualifiedName = resolvedName[0][1].replace('.', '$');
		if (resolvedName[0][0].length() > 0)
		{
			return resolvedName[0][0] + "." + typeQualifiedName;
		}
		else
		{
			return typeQualifiedName;
		}
	}

	public static void visitAllTypes(IParent parent, TypeVisitor visitor) throws CoreException
	{
		for (IJavaElement child : parent.getChildren())
		{
			if (child.getElementType() == IJavaElement.TYPE)
			{
				IType type = (IType) child;
				visitor.visit(type);
				visitAllTypes(type, visitor);
			}
		}
	}

	private enum StatusBarAction
	{
		SHOW_MESSAGE,
		SHOW_ERROR,
		CLEAR;
	}

	public static void setStatusBarText(String text, Object... args)
	{
		setStatusBarText(StatusBarAction.SHOW_MESSAGE, text, args);
	}

	public static void setStatusBarError(String text, Object... args)
	{
		setStatusBarText(StatusBarAction.SHOW_ERROR, text, args);
	}

	public static void clearStatusBarText()
	{
		setStatusBarText(StatusBarAction.CLEAR, "");
	}

	private static void setStatusBarText(final StatusBarAction action, String text, Object... args)
	{
		final String statusText = String.format(text, args);
		UIJob setter = new UIJob("Set status bar text") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = win.getActivePage();
				IWorkbenchPart part = page.getActivePart();
				IWorkbenchPartSite site = part.getSite();

				IActionBars actionBars = null;
				if (site instanceof IEditorSite)
				{
					IEditorSite eSite = (IEditorSite) site;
					actionBars = eSite.getActionBars();
				}
				else if (site instanceof IViewSite)
				{
					IViewSite vSite = (IViewSite) site;
					actionBars = vSite.getActionBars();
				}
				else
				{
					return Status.OK_STATUS;
				}

				if (actionBars == null)
				{
					return Status.OK_STATUS;
				}
				IStatusLineManager statusLineManager = actionBars.getStatusLineManager();
				if (statusLineManager == null)
				{
					return Status.OK_STATUS;
				}

				switch (action)
				{
					case CLEAR:
						statusLineManager.setMessage(null);
						statusLineManager.setErrorMessage(null);
						break;
					case SHOW_ERROR:
						statusLineManager.setMessage(null);
						statusLineManager.setErrorMessage(statusText);
						break;
					case SHOW_MESSAGE:
						statusLineManager.setErrorMessage(null);
						statusLineManager.setMessage(statusText);
						break;
				}

				return Status.OK_STATUS;
			}
		};
		setter.schedule();
	}

	public static boolean isDefaultNoArgConstructor(IMethodBinding method)
	{
		return method.isConstructor() && (method.getParameterTypes().length == 0);
	}
}
