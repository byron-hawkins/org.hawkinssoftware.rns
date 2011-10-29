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
package org.hawkinssoftware.rns.analysis.compile.source;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.hawkinssoftware.rns.analysis.compile.source.SourceReferenceInstruction.Kind;
import org.hawkinssoftware.rns.analysis.compile.util.RNSBuildAnalyzerUtils;
import org.hawkinssoftware.rns.analysis.compile.util.TypeVisitor;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class ParsedJavaSource
{
	private static final String RELATED_TYPENAME_MARKER_ATTRIBUTE = "org.hawkinssoftware.rns.analysis.compile.related-typename";
	private static final String MEMBER_TYPENAME_MARKER_ATTRIBUTE = "org.hawkinssoftware.rns.analysis.compile.member-typename";

	private final Multimap<InstructionKey, SourceReferenceInstruction<?, ?>> references = ArrayListMultimap.create();
	private final Map<InstructionKey, SourceDeclarationInstruction<?, ?>> declarations = new HashMap<InstructionKey, SourceDeclarationInstruction<?, ?>>();
	private final SourceInstructionCollector referenceCollector = new SourceInstructionCollector();
	private final ReferenceRequestor requestor = new ReferenceRequestor();

	private ICompilationUnit source;
	private CompilationUnit ast;

	public ParsedJavaSource(ICompilationUnit source, CompilationUnit ast)
	{
		update(source, ast);
	}

	public boolean exists()
	{
		return source.exists();
	}

	public void createReferredTypeMarker(String markerType, String message, int severity, ASTNode markerNode, String relatedTypename, String memberTypename)
			throws CoreException
	{
		IMarker marker = createMarker(markerType, message, severity, markerNode);
		marker.setAttribute(RELATED_TYPENAME_MARKER_ATTRIBUTE, relatedTypename);
		marker.setAttribute(MEMBER_TYPENAME_MARKER_ATTRIBUTE, memberTypename);
	}

	public void createMemberTypeMarker(String markerType, String message, int severity, ASTNode markerNode, String memberTypename) throws CoreException
	{
		createMarker(markerType, message, severity, markerNode).setAttribute(MEMBER_TYPENAME_MARKER_ATTRIBUTE, memberTypename);
	}

	public IMarker createMarker(String markerType, String message, int severity, ASTNode markerNode) throws CoreException
	{
		IMarker marker = source.getResource().createMarker(markerType);
		marker.setAttribute(IMarker.LINE_NUMBER, ast.getLineNumber(markerNode.getStartPosition()));
		marker.setAttribute(IMarker.CHAR_START, markerNode.getStartPosition());
		marker.setAttribute(IMarker.CHAR_END, markerNode.getStartPosition() + markerNode.getLength());
		marker.setAttribute(IMarker.LOCATION, "Line " + ast.getLineNumber(markerNode.getStartPosition()));
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.MESSAGE, message);
		return marker;
	}

	public void traverse(ASTVisitor visitor)
	{
		ast.accept(visitor);
	}

	public void visitAllTypes(TypeVisitor visitor) throws CoreException
	{
		RNSBuildAnalyzerUtils.visitAllTypes(source, visitor);
	}

	@SuppressWarnings("unchecked")
	public SourceDeclarationInstruction<AbstractTypeDeclaration, ITypeBinding> getTypeDeclaration(IType type)
	{
		return (SourceDeclarationInstruction<AbstractTypeDeclaration, ITypeBinding>) declarations.get(new InstructionKey(
				SourceDeclarationInstruction.Kind.TYPE_DECLARATION, type.getFullyQualifiedName()));
	}

	@SuppressWarnings("unchecked")
	public SourceDeclarationInstruction<FieldDeclaration, IVariableBinding> getFieldDeclaration(IField field)
	{
		return (SourceDeclarationInstruction<FieldDeclaration, IVariableBinding>) declarations.get(new InstructionKey(
				SourceDeclarationInstruction.Kind.FIELD_DECLARATION, field.getDeclaringType().getFullyQualifiedName() + "." + field.getElementName()));
	}

	public void deleteMarkers(String problemMarkerType) throws CoreException
	{
		source.getResource().deleteMarkers(problemMarkerType, true, IResource.DEPTH_ZERO);
	}

	public void deleteRelatedTypeMarkers(String problemMarkerType, String relatedTypename) throws CoreException
	{
		deleteMarkers(problemMarkerType, RELATED_TYPENAME_MARKER_ATTRIBUTE, relatedTypename);
	}

	public void deleteRelatedTypeMarkers(String problemMarkerType, String relatedTypename, String memberTypename) throws CoreException
	{
		deleteMarkers(problemMarkerType, RELATED_TYPENAME_MARKER_ATTRIBUTE, relatedTypename);
	}

	public void deleteMemberTypeMarkers(String problemMarkerType, String memberTypename) throws CoreException
	{
		deleteMarkers(problemMarkerType, MEMBER_TYPENAME_MARKER_ATTRIBUTE, memberTypename);
	}

	private void deleteMarkers(String problemMarkerType, String typenameAttribute, String typename) throws CoreException
	{
		for (IMarker marker : source.getResource().findMarkers(problemMarkerType, true, IResource.DEPTH_ZERO))
		{
			if (marker.getAttribute(typenameAttribute, "<none>").equals(typename))
			{
				marker.delete();
			}
		}
	}

	public void update(ICompilationUnit source, CompilationUnit ast)
	{
		this.source = source;
		this.ast = ast;

		buildReferenceCache();
	}

	public Collection<SourceReferenceInstruction<?, ?>> getAllReferences()
	{
		return references.values();
	}

	public void appendReferencesTo(IType referredType, Collection<SourceReferenceInstruction<?, ?>> destination)
	{
		for (SourceReferenceInstruction.Kind kind : SourceReferenceInstruction.Kind.values())
		{
			// need to use the method key for overrides...
			destination.addAll(references.get(new InstructionKey(kind, referredType.getFullyQualifiedName())));
		}
	}

	private void buildReferenceCache()
	{
		references.clear();
		referenceCollector.findAllExternalTypeReferences(requestor, ast);
	}

	@Override
	public String toString()
	{
		return source.findPrimaryType().getTypeQualifiedName();
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private class ReferenceRequestor implements SourceInstructionCollector.CollectionRequestor
	{
		@Override
		public void expressionFound(Expression expression, ASTNode markerNode, ITypeBinding binding)
		{
			typeReferenceFound(Kind.EXPRESSION, expression, markerNode, binding);
		}

		@Override
		public void extensionFound(Type type, ASTNode markerNode, ITypeBinding binding)
		{
			typeReferenceFound(Kind.EXTENSION, type, markerNode, binding);
		}

		@Override
		public void fieldDeclarationFound(FieldDeclaration field, ASTNode markerNode, IVariableBinding binding)
		{
			if (binding.getType().isPrimitive() || binding.getType().isTypeVariable() || binding.getType().isCapture())
			{
				return;
			}

			SourceDeclarationInstruction<FieldDeclaration, IVariableBinding> fieldDeclaration = new SourceDeclarationInstruction<FieldDeclaration, IVariableBinding>(
					SourceDeclarationInstruction.Kind.FIELD_DECLARATION, field, markerNode, binding);
			fieldDeclaration.setContainingSource(ParsedJavaSource.this);

			declarations.put(new InstructionKey(fieldDeclaration), fieldDeclaration);
		}

		@Override
		public void methodCallFound(Expression invocation, ASTNode markerNode, IMethodBinding binding)
		{
			if (binding == null)
			{
				// workspace errors prevent analysis
				return;
			}

			SourceReferenceInstruction<Expression, IMethodBinding> methodCall = new SourceReferenceInstruction<Expression, IMethodBinding>(
					SourceReferenceInstruction.Kind.METHOD_CALL, invocation, markerNode, binding, (IType) binding.getDeclaringClass().getJavaElement());
			methodCall.setContainingSource(ParsedJavaSource.this);

			references.put(new InstructionKey(methodCall), methodCall);
		}

		@Override
		public void methodDeclarationFound(MethodDeclaration declaration, ASTNode markerNode, IMethodBinding binding)
		{
			{
				SourceDeclarationInstruction<MethodDeclaration, IMethodBinding> methodDeclaration = new SourceDeclarationInstruction<MethodDeclaration, IMethodBinding>(
						SourceDeclarationInstruction.Kind.METHOD_DECLARATION, declaration, markerNode, binding);
				methodDeclaration.setContainingSource(ParsedJavaSource.this);
				declarations.put(new InstructionKey(methodDeclaration), methodDeclaration);
			}

			IType overriddenType = getOverriddenType(binding);
			if (overriddenType != null)
			{
				SourceReferenceInstruction<MethodDeclaration, IMethodBinding> methodOverride = new SourceReferenceInstruction<MethodDeclaration, IMethodBinding>(
						SourceReferenceInstruction.Kind.METHOD_OVERRIDE, declaration, markerNode, binding, overriddenType);
				methodOverride.setContainingSource(ParsedJavaSource.this);
				references.put(new InstructionKey(methodOverride), methodOverride);
			}
		}

		@Override
		public void typeDeclarationFound(AbstractTypeDeclaration declaration, ASTNode markerNode, ITypeBinding binding)
		{
			SourceDeclarationInstruction<AbstractTypeDeclaration, ITypeBinding> typeDeclaration = new SourceDeclarationInstruction<AbstractTypeDeclaration, ITypeBinding>(
					SourceDeclarationInstruction.Kind.TYPE_DECLARATION, declaration, markerNode, binding);
			declarations.put(new InstructionKey(typeDeclaration), typeDeclaration);
		}

		@Override
		public void typeReferenceFound(Type type, ASTNode markerNode, ITypeBinding binding)
		{
			typeReferenceFound(Kind.TYPE_REFERENCE, type, markerNode, binding);
		}

		private IType getOverriddenType(IMethodBinding methodBinding)
		{
			if (methodBinding.isConstructor())
			{
				// ignoring constructors because they are not automatically inherited as members
				return null;
			}

			IMethod method = (IMethod) methodBinding.getJavaElement();
			ITypeHierarchy hierarchy = TypeHierarchyCache.getInstance().establishHierarchy(method.getDeclaringType());
			for (IType type : hierarchy.getAllSuperclasses(hierarchy.getType()))
			{
				if (type.getMethod(method.getElementName(), method.getParameterTypes()).exists())
				{
					return type;
				}
			}
			return null;
		}

		private void typeReferenceFound(SourceReferenceInstruction.Kind kind, ASTNode node, ASTNode markerNode, ITypeBinding type)
		{
			if (type == null)
			{
				// syntax errors exist, punt
				return;
			}

			IJavaElement javaElement = type.getJavaElement();
			if (javaElement == null)
			{
				// TODO: these are primitives, void, and generic wildcards. Do I need any of them?
				// Log.out(Tag.PUB_OPT, "No java element for node %s which references type %s",
				// node.getClass().getSimpleName(), type.getName());
				return;
			}

			if (javaElement instanceof ITypeParameter)
			{
				// will get the type bindings later
				return;
			}
			if (!(javaElement instanceof IType))
			{
				Log.out(Tag.PUB_OPT, "The java element for type %s is a %s, but IType is expected.", node.getClass().getSimpleName(), javaElement.getClass()
						.getSimpleName());
				return;
			}

			SourceReferenceInstruction<ASTNode, ITypeBinding> reference = new SourceReferenceInstruction<ASTNode, ITypeBinding>(kind, node, markerNode, type,
					(IType) javaElement);
			reference.setContainingSource(ParsedJavaSource.this);
			references.put(new InstructionKey(reference), reference);
		}
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private static class InstructionKey
	{
		final Enum<?> kind;
		/**
		 * Qualified name of the reference.
		 * 
		 * <pre>
		 * IType: IType.getFullyQualifiedName()
		 * IField: IField.getDeclaringType().getFullyQualifiedName() + "." + IField.getElementName()
		 * </pre>
		 */
		final String qualifiedName;

		InstructionKey(Enum<?> kind, String qualifiedName)
		{
			this.kind = kind;
			this.qualifiedName = qualifiedName;
		}

		InstructionKey(SourceInstruction<? extends SourceInstruction.InstructionKind, ?, ?> instruction)
		{
			this.kind = (Enum<?>) instruction.kind;
			this.qualifiedName = instruction.getQualifiedName();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InstructionKey other = (InstructionKey) obj;
			if (kind != other.kind)
				return false;
			if (qualifiedName == null)
			{
				if (other.qualifiedName != null)
					return false;
			}
			else if (!qualifiedName.equals(other.qualifiedName))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "ReferenceKey(" + kind + ":" + qualifiedName + ")";
		}
	}
}
