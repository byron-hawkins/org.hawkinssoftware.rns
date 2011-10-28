package org.hawkinssoftware.rns.analysis.compile.publication;

import org.eclipse.core.resources.IMarker;

class PublicationConstraintValidationStatus
{
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
