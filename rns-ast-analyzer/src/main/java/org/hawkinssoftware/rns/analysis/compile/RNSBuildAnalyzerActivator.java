package org.hawkinssoftware.rns.analysis.compile;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.osgi.framework.BundleContext;

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
