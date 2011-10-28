package org.hawkinssoftware.rns.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

// TODO: not sure this makes any sense--skipping for now

/**
 * Overlays annotations from an interface to the annotated field. Method annotations on the specified <code>type</code>
 * interface are matched by signature to methods of the instance referred to by the annotated field. Type annotations on
 * the specified <code>type</code> are applied to the entire instance referred to by the annotated field.
 * 
 * @author b
 */
@Target(ElementType.FIELD)
public @interface AnnotationOverlay
{
	/**
	 * An interface having methods matching the annotated field. Each type and method annotation on this
	 * <code>type</code> will be applied in parallel to the annotated field.
	 */
	Class<?> type();
}
