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
package org.hawkinssoftware.rns.analysis.compile;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.osgi.framework.BundleContext;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class RNSBuildAnalyzerActivator extends AbstractUIPlugin
{

	public RNSBuildAnalyzerActivator()
	{
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		Log.addOutput(System.out);
		Log.out(Tag.PUB_OPT, "Logger installed");
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);

		Log.removeOutput(System.out);
		Log.out(Tag.PUB_OPT, "Logger uninstalled");
	}
}
