package org.hawkinssoftware.rns.analysis.compile.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;

public interface TypeVisitor
{
	void visit(IType type) throws CoreException;
}
