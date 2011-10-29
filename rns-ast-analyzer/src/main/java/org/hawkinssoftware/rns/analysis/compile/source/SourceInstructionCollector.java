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

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class SourceInstructionCollector extends ASTVisitor
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	interface CollectionRequestor
	{
		void extensionFound(Type type, ASTNode markerNode, ITypeBinding binding);

		void expressionFound(Expression expression, ASTNode markerNode, ITypeBinding binding);

		void typeDeclarationFound(AbstractTypeDeclaration declaration, ASTNode markerNode, ITypeBinding binding);

		void typeReferenceFound(Type type, ASTNode markerNode, ITypeBinding binding);

		void methodCallFound(Expression invocation, ASTNode markerNode, IMethodBinding binding);

		void methodDeclarationFound(MethodDeclaration declaration, ASTNode markerNode, IMethodBinding binding);

		void fieldDeclarationFound(FieldDeclaration field, ASTNode markerNode, IVariableBinding binding);
	}

	private CollectionRequestor requestor;

	void findAllExternalTypeReferences(CollectionRequestor requestor, CompilationUnit ast)
	{
		this.requestor = requestor;
		ast.accept(this);
	}

	@Override
	public boolean visit(NormalAnnotation node)
	{
		requestor.expressionFound(node, node, node.resolveTypeBinding());
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node)
	{
		requestor.expressionFound(node, node, node.resolveTypeBinding());
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node)
	{
		requestor.expressionFound(node, node, node.resolveTypeBinding());
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node)
	{
		if (node.getParent() instanceof EnumConstantDeclaration)
		{
			// not interested in individual enum constants
			return true;
		}

		if (!(node.getParent() instanceof ClassInstanceCreation))
		{
			Log.out(Tag.WARNING, "%s found in an unexpected AST location: %s", node.getClass().getSimpleName(), node.getParent().getClass().getSimpleName());
			return true;
		}

		Type type = ((ClassInstanceCreation) node.getParent()).getType();
		requestor.extensionFound(type, type, type.resolveBinding());
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(ClassInstanceCreation node)
	{
		// TODO: could consider external constructor invocations ("new Type()") as method calls too
		requestor.expressionFound(node, node.getType(), node.getType().resolveBinding());
		for (Type typeArgument : (List<Type>) node.typeArguments())
		{
			requestor.typeReferenceFound(typeArgument, typeArgument, typeArgument.resolveBinding());
		}
		requestor.methodCallFound(node, node.getType(), node.resolveConstructorBinding());

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(EnumDeclaration node)
	{
		for (Type implemented : (List<Type>) node.superInterfaceTypes())
		{
			requestor.extensionFound(implemented, implemented, implemented.resolveBinding());
		}
		requestor.typeDeclarationFound(node, node.getName(), node.resolveBinding());

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TypeDeclaration node)
	{
		if (node.getSuperclassType() != null)
		{
			requestor.extensionFound(node.getSuperclassType(), node.getSuperclassType(), node.getSuperclassType().resolveBinding());
		}
		for (Type implemented : (List<Type>) node.superInterfaceTypes())
		{
			requestor.extensionFound(implemented, implemented, implemented.resolveBinding());
		}
		for (TypeParameter parameter : (List<TypeParameter>) node.typeParameters())
		{
			for (Type bound : (List<Type>) parameter.typeBounds())
			{
				requestor.typeReferenceFound(bound, bound, bound.resolveBinding());
			}
		}
		requestor.typeDeclarationFound(node, node.getName(), node.resolveBinding());

		return true;
	}

	@Override
	public boolean visit(CastExpression node)
	{
		requestor.expressionFound(node, node.getType(), node.getType().resolveBinding());
		return true;
	}

	public boolean visit(FieldDeclaration node)
	{
		// all variable annotations and field declarations are found in visit(VariableDeclarationFragment)
		requestor.typeReferenceFound(node.getType(), node.getType(), node.getType().resolveBinding());
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(ParameterizedType node)
	{
		for (Type typeArgument : (List<Type>) node.typeArguments())
		{
			requestor.typeReferenceFound(node, typeArgument, typeArgument.resolveBinding());
		}
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node)
	{
		requestor.typeReferenceFound(node.getType(), node.getType(), node.getType().resolveBinding());
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(ConstructorInvocation node)
	{
		// a `this(...) expression within a constructor body
		for (Type typeArgument : (List<Type>) node.typeArguments())
		{
			requestor.typeReferenceFound(typeArgument, typeArgument, typeArgument.resolveBinding());
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodDeclaration node)
	{
		if (node.getReturnType2() != null)
		{
			requestor.typeReferenceFound(node.getReturnType2(), node.getReturnType2(), node.getReturnType2().resolveBinding());
		}
		for (Name exception : (List<Name>) node.thrownExceptions())
		{
			// TODO: this is most definitely a direct type reference, not an expression of any kind--no expression
			// semantics are allowed in a throws clause. For now it doesn't matter, but hopefully these `Name instances
			// can somehow be substituted with the correct `Type instances.
			requestor.expressionFound(exception, exception, exception.resolveTypeBinding());
		}
		for (TypeParameter typeArgument : (List<TypeParameter>) node.typeParameters())
		{
			for (Type typeBound : (List<Type>) typeArgument.typeBounds())
			{
				requestor.typeReferenceFound(typeBound, typeBound, typeBound.resolveBinding());
			}
		}
		// parameters are visited as variable declarations

		IMethodBinding method = node.resolveBinding();
		if (method == null)
		{
			// build errors likely prevent the binding resolution
			return true;
		}
		requestor.methodDeclarationFound(node, node.getName(), method);

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodInvocation node)
	{
		for (Type typeArgument : (List<Type>) node.typeArguments())
		{
			requestor.typeReferenceFound(typeArgument, typeArgument, typeArgument.resolveBinding());
		}

		// TODO: seems like I could consider the invocation as having a reference to the return type of the called
		// method, but it has no token in the syntax since it implicitly goes on the stack.
		// requestor.typeReferenceFound(token?, token?, node.resolveTypeBinding());

		IMethodBinding method = node.resolveMethodBinding();
		if (method == null)
		{
			// build errors likely prevent the binding resolution
			return true;
		}
		requestor.methodCallFound(node, node.getName(), method);

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node)
	{
		requestor.typeReferenceFound(node.getType(), node.getType(), node.getType().resolveBinding());
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node)
	{
		if (node.resolveBinding() == null)
		{
			// probably a syntax issue
			return true;
		}

		if (node.getParent() instanceof FieldDeclaration)
		{
			requestor.fieldDeclarationFound((FieldDeclaration) node.getParent(), node.getName(), node.resolveBinding());
		}

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node)
	{
		requestor.typeReferenceFound(node.getType(), node.getType(), node.getType().resolveBinding());
		return true;
	}

	@Override
	public boolean visit(TypeLiteral node)
	{
		ITypeBinding typeBinding = node.getType().resolveBinding();
		requestor.typeReferenceFound(node.getType(), node.getType(), typeBinding);

		// establish the type hierarchy of every DomainRole to guarantee availability before any possible request
		ITypeBinding traversal = typeBinding.getSuperclass();
		while (traversal != null)
		{
			if (traversal.getQualifiedName().endsWith(DomainRole.class.getSimpleName()))
			{
				IType type = (IType) typeBinding.getJavaElement();
				TypeHierarchyCache.getInstance().establishHierarchy(type);
				break;
			}
			traversal = traversal.getSuperclass();
		}

		return true;
	}

	@Override
	public boolean visit(InstanceofExpression node)
	{
		requestor.typeReferenceFound(node.getRightOperand(), node.getRightOperand(), node.getRightOperand().resolveBinding());
		return true;
	}
}
