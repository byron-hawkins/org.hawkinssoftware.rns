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
package org.hawkinssoftware.rns.analysis.compile.publication;

import org.eclipse.core.resources.IMarker;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
class PublicationConstraintValidationStatus
{
	
	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	enum Evaluation
	{
		VALID(-1),
		INFO(IMarker.SEVERITY_INFO),
		WARNING(IMarker.SEVERITY_WARNING),
		ERROR(IMarker.SEVERITY_ERROR);

		final int jdtMarkerSeverity;

		private Evaluation(int jdtMarkerSeverity)
		{
			this.jdtMarkerSeverity = jdtMarkerSeverity;
		}
	}

	final Evaluation evaluation;
	final String message;

	PublicationConstraintValidationStatus(Evaluation evaluation, String message)
	{
		this.evaluation = evaluation;
		this.message = message;
	}
}
